package com.colin.game;

import javafx.scene.paint.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@code PaintRandomizer} class is designed for generating vibrant random paint objects that can range from simple colors to complex gradients like radial or linear gradients.
 * <p>
 * This class has been built leveraging Java 17 features and incorporates functional programming constructs like Suppliers, Predicates, and Streams for more efficient and clean code.
 * </p>
 * <p>
 * This class is thread-safe. It can safely be used in a multi-threaded environment.
 * </p>
 * <h2>Features:</h2>
 * <ul>
 *     <li>Generate unique vibrant paints.</li>
 *     <li>Define undesirable colors with a Predicate.</li>
 *     <li>Control the memory limit for generated paints.</li>
 *     <li>Choose between different gradient styles: Simple, Radial, Linear.</li>
 * </ul>
 * <h2>Examples:</h2>
 * <pre>{@code
 *     // Creating a builder
 *     PaintRandomizer.Builder builder = new PaintRandomizer.Builder();
 *
 *     // Customizing the builder
 *     builder.minHue(() -> 50.0);
 *     builder.maxHue(() -> 200.0);
 *
 *     // Building the PaintRandomizer object
 *     PaintRandomizer paintRandomizer = builder.build();
 *
 *     // Generating a random paint
 *     Paint randomPaint = paintRandomizer.generateRandomVibrantPaint();
 *
 *     // Generating a list of random paints
 *     List<Paint> randomPaints = paintRandomizer.generateVibrantPaints(5);
 * }
 * </pre>
 *
 * @author Colin Jokisch
 * @version 4.5
 */
public class PaintRandomizer {
    public enum GradientStyle {
        SIMPLE,
        RADIAL,
        LINEAR
    }

    private final Random                  random; // Random object for generating random numbers
    private final Supplier<Integer>       paintMemorySupplier; // Defines the size of memory for unique paints
    private final Predicate<Color>        isUndesirable; // Predicate to filter out undesirable colors
    private final Supplier<Double>        hueSupplier; // Supplies the hue value for the color
    private final Supplier<Double>        saturationSupplier; // Supplies the saturation value for the color
    private final Supplier<Double>        brightnessSupplier; // Supplies the brightness value for the color
    private final Supplier<GradientStyle> gradientStyleSupplier; // Supplies the gradient style to apply

    private final Map<Paint, Boolean> lruCache = Collections.synchronizedMap(new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Paint, Boolean> eldest) {
            return size() > paintMemorySupplier.get();
        }
    });

    private final int maxRetryAttempts;

    //Gradient Specific
    private final Supplier<Double>            focusAngleSupplier; // Supplies the focus angle for radial gradient
    private final Supplier<Double>            focusDistanceSupplier; // Supplies the focus distance for radial gradient
    private final Supplier<Double>            centerXSupplier;
    private final Supplier<Double>            centerYSupplier;
    private final Supplier<Double>            radiusSupplier; // Supplies the radius for radial gradient
    private final Supplier<Boolean>           proportionalSupplier;
    private final Supplier<CycleMethod>       cycleMethodSupplier;
    private final Function<Color, List<Stop>> stopRandomizer;  // Function to generate random Stops for RadialGradient

    /**
     * Private constructor to initialize the PaintRandomizer with the builder.
     *
     * @param builder
     *         The builder containing the necessary suppliers and predicates.
     */
    private PaintRandomizer(Builder builder) {
        this.random                = builder.randomSupplier.get();
        this.paintMemorySupplier   = builder.paintMemorySupplier;
        this.isUndesirable         = builder.isUndesirable;
        this.hueSupplier           = builder.hueSupplier;
        this.saturationSupplier    = builder.saturationSupplier;
        this.brightnessSupplier    = builder.brightnessSupplier;
        this.gradientStyleSupplier = builder.gradientStyleSupplier;
        this.maxRetryAttempts      = builder.maxRetryAttempts;
        this.focusAngleSupplier    = builder.focusAngleSupplier;
        this.focusDistanceSupplier = builder.focusDistanceSupplier;
        this.centerXSupplier       = builder.centerXSupplier;
        this.centerYSupplier       = builder.centerYSupplier;
        this.radiusSupplier        = builder.radiusSupplier;
        this.proportionalSupplier  = builder.proportionalSupplier;
        this.cycleMethodSupplier   = builder.cycleMethodSupplier;
        this.stopRandomizer        = builder.stopRandomizer;
    }

    /**
     * Generate a unique random vibrant paint.
     *
     * @return A unique vibrant paint.
     */
    public synchronized Paint generateRandomVibrantPaint() {
        Supplier<Paint> paintSupplier = () -> {
            Color color;
            do {
                color = Color.hsb(hueSupplier.get(),
                                  saturationSupplier.get(),
                                  brightnessSupplier.get());
            } while (isUndesirable.test(color));

            return applyGradientStyle(color, gradientStyleSupplier.get());
        };

        return generateUniquePaint(paintSupplier);
    }

    /**
     * Generate a list of unique random vibrant paints.
     *
     * @param numberOfPaints
     *         Number of vibrant paints to generate.
     *
     * @return List of unique vibrant paints.
     *
     * @throws IllegalArgumentException
     *         if numberOfPaints is greater than the size of paint memory.
     */
    public List<Paint> generateVibrantPaints(int numberOfPaints) {
        int paintMemorySize = paintMemorySupplier.get();
        if (numberOfPaints > paintMemorySize) {
            throw new IllegalArgumentException("Number of paints to generate should be less than or equal to paint memory size");
        }

        return Stream.generate(this::generateRandomVibrantPaint)
                     .limit(numberOfPaints)
                     .collect(Collectors.toList());
    }

    /**
     * Retrieves a thread-safe, unmodifiable map containing all the generated Paint objects.
     * <p>
     * The returned map is a snapshot of the underlying LRU cache used to store the unique paints. This map uses a Boolean flag as the value to signify the existence of the corresponding Paint key.
     * </p>
     * <p>
     * Being unmodifiable, this map cannot be altered. It serves as a way to inspect the paints that have been generated by this instance of {@code PaintRandomizer}.
     * </p>
     *
     * @return An unmodifiable, thread-safe map of generated Paint objects.
     */
    public Map<Paint, Boolean> getGeneratedPaints() {
        return Collections.unmodifiableMap(Collections.synchronizedMap(lruCache));
    }

    /**
     * Applies a gradient style to a given base color.
     *
     * @param baseColor
     *         The base color to apply gradient on.
     * @param gradientStyle
     *         The gradient style to apply.
     *
     * @return A new Paint object with the gradient applied.
     */
    private Paint applyGradientStyle(Color baseColor, GradientStyle gradientStyle) {
        return switch (gradientStyle) {
            case RADIAL -> new RadialGradient(focusAngleSupplier.get(),
                                              focusDistanceSupplier.get(),
                                              centerXSupplier.get(),
                                              centerYSupplier.get(),
                                              radiusSupplier.get(),
                                              proportionalSupplier.get(),
                                              cycleMethodSupplier.get(),
                                              stopRandomizer.apply(baseColor));
            case LINEAR -> new LinearGradient(random.nextDouble(), //TODO: make suppliers for startX, startY, endX, endY
                                              random.nextDouble(),
                                              random.nextDouble(),
                                              random.nextDouble(),
                                              proportionalSupplier.get(),
                                              cycleMethodSupplier.get(),
                                              stopRandomizer.apply(baseColor));
            default -> baseColor;
        };
    }

    /**
     * Generates a unique paint that hasn't been generated before.
     *
     * @param paintSupplier
     *         Supplier that provides new Paint objects.
     *
     * @return A unique Paint object.
     *
     * @throws IllegalStateException
     *         if a unique paint cannot be generated after multiple attempts.
     */
    private Paint generateUniquePaint(Supplier<Paint> paintSupplier) {
        Paint newPaint;
        int   attempts = 0;
        synchronized (lruCache) {
            do {
                if (attempts++ > maxRetryAttempts) {
                    throw new IllegalStateException("Could not generate a unique paint after %d attempts".formatted(maxRetryAttempts));
                }
                newPaint = paintSupplier.get();
            } while (lruCache.containsKey(newPaint));
            lruCache.put(newPaint, Boolean.TRUE);
        }
        System.out.println(newPaint);
        return newPaint;
    }

    /**
     * Builder class for PaintRandomizer.
     */
    public static class Builder {
        private Supplier<Random>        randomSupplier;
        private Supplier<Integer>       paintMemorySupplier;
        private Predicate<Color>        isUndesirable;
        private Supplier<Double>        hueSupplier;
        private Supplier<Double>        saturationSupplier;
        private Supplier<Double>        brightnessSupplier;
        private Supplier<Double>        focusAngleSupplier;
        private Supplier<Double>        focusDistanceSupplier;
        private Supplier<Double>        centerXSupplier;
        private Supplier<Double>        centerYSupplier;
        private Supplier<Double>        radiusSupplier;
        private Supplier<Boolean>       proportionalSupplier;
        private Supplier<CycleMethod>   cycleMethodSupplier;
        private Supplier<GradientStyle> gradientStyleSupplier;
        private int                     maxRetryAttempts;

        private Function<Color, List<Stop>> stopRandomizer;  // Default to start and end with base color

        /**
         * Default constructor initializes with default suppliers and predicates.
         */
        public Builder() {
            randomSupplier      = Random::new;
            paintMemorySupplier = () -> 10;
            isUndesirable       = color -> false;
            hueSupplier         = () -> {
                Random hueRandom = new Random();
                return hueRandom.nextDouble() * 360.0;
            };
            saturationSupplier  = () -> {
                Random saturationRandom = new Random();
                return saturationRandom.nextDouble(.7, 1);
            };
            brightnessSupplier  = () -> {
                Random brightnessRandom = new Random();
                return brightnessRandom.nextDouble(.7, 1);
            };

            gradientStyleSupplier = () -> GradientStyle.LINEAR;
            maxRetryAttempts      = 10000;

            //Gradient Specific Suppliers
            focusAngleSupplier    = () -> 0.0;
            focusDistanceSupplier = () -> 0.0;
            centerXSupplier       = () -> 0.5;
            centerYSupplier       = () -> 0.5;
            proportionalSupplier  = () -> true;
            cycleMethodSupplier   = () -> CycleMethod.NO_CYCLE;
            radiusSupplier        = () -> 0.5;
            stopRandomizer        = color -> List.of(new Stop(0, Color.WHITE),
                                                     new Stop(.2, Color.hsb(color.getHue(), 1, 1)),
                                                     new Stop(.6, Color.hsb(color.getHue(), 1, 1, .1)),
                                                     new Stop(1, Color.hsb(color.getHue(), 1, 1, 0)));

        }

        public Builder randomSupplier(Supplier<Random> randomSupplier) {
            requireNonNullSupplierAndValue(new Object[]{randomSupplier, "Random Supplier"});
            this.randomSupplier = randomSupplier;
            return this;
        }

        public Builder paintMemory(Supplier<Integer> paintMemorySupplier) {
            requireNonNullSupplierAndValue(new Object[]{paintMemorySupplier, "Paint Memory Supplier"});
            this.paintMemorySupplier = paintMemorySupplier;
            return this;
        }

        public Builder isUndesirable(Predicate<Color> isUndesirable) {
            Objects.requireNonNull(isUndesirable, "isUndesirable Predicate cannot be null");
            this.isUndesirable = isUndesirable;
            return this;
        }

        public Builder hue(Supplier<Double> hueSupplier) {
            requireNonNullSupplierAndValue(new Object[]{hueSupplier, "Hue Supplier"});
            this.hueSupplier = hueSupplier;
            return this;
        }

        public Builder saturation(Supplier<Double> saturationSupplier) {
            requireNonNullSupplierAndValue(new Object[]{saturationSupplier, "Saturation Supplier"});
            this.saturationSupplier = saturationSupplier;
            return this;
        }

        public Builder brightness(Supplier<Double> brightnessSupplier) {
            requireNonNullSupplierAndValue(new Object[]{brightnessSupplier, "Brightness Supplier"});
            this.brightnessSupplier = brightnessSupplier;
            return this;
        }

        public Builder focusAngle(Supplier<Double> focusAngleSupplier) {
            requireNonNullSupplierAndValue(new Object[]{focusAngleSupplier, "Focus Angle Supplier"});
            this.focusAngleSupplier = focusAngleSupplier;
            return this;
        }

        public Builder focusDistance(Supplier<Double> focusDistanceSupplier) {
            requireNonNullSupplierAndValue(new Object[]{focusDistanceSupplier, "Focus Distance Supplier"});
            this.focusDistanceSupplier = focusDistanceSupplier;
            return this;
        }

        public Builder centerX(Supplier<Double> centerXSupplier) {
            requireNonNullSupplierAndValue(new Object[]{focusDistanceSupplier, "Center X Supplier"});
            this.centerXSupplier = centerXSupplier;
            return this;
        }

        public Builder centerY(Supplier<Double> centerYSupplier) {
            requireNonNullSupplierAndValue(new Object[]{focusDistanceSupplier, "Center Y Supplier"});
            this.centerYSupplier = centerYSupplier;
            return this;
        }

        public Builder radius(Supplier<Double> radiusSupplier) {
            requireNonNullSupplierAndValue(new Object[]{radiusSupplier, "Max Radius Supplier"});
            this.radiusSupplier = radiusSupplier;
            return this;
        }

        public Builder proportional(Supplier<Boolean> proportionalSupplier) {
            requireNonNullSupplierAndValue(new Object[]{radiusSupplier, "Proportional Supplier"});
            this.proportionalSupplier = proportionalSupplier;
            return this;
        }

        public Builder cycleMethod(Supplier<CycleMethod> cycleMethodSupplier) {
            requireNonNullSupplierAndValue(new Object[]{radiusSupplier, "Cycle Method Supplier"});
            this.cycleMethodSupplier = cycleMethodSupplier;
            return this;
        }

        public Builder gradientStyle(Supplier<GradientStyle> gradientStyleSupplier) {
            requireNonNullSupplierAndValue(new Object[]{gradientStyleSupplier, "Gradient Style Supplier"});
            this.gradientStyleSupplier = gradientStyleSupplier;
            return this;
        }

        public Builder maxRetryAttempts(int maxRetryAttempts) {
            this.maxRetryAttempts = maxRetryAttempts;
            return this;
        }

        public Builder stopRandomizer(Function<Color, List<Stop>> stopRandomizer) {
            Objects.requireNonNull(stopRandomizer, "Stop Randomizer cannot be null");
            this.stopRandomizer = stopRandomizer;
            return this;
        }

        /**
         * Constructs a new {@code PaintRandomizer} instance.
         *
         * @return A new {@code PaintRandomizer} instance.
         *
         * @throws IllegalArgumentException
         *         if any of the max values are less than or equal to their corresponding min values, or if the paint memory size is non-positive.
         * @throws NullPointerException
         *         if any of the supplied objects is null.
         */
        public PaintRandomizer build() {
            requireGreaterThan0(paintMemorySupplier.get(), "Paint Memory Size");
            requireGreaterThan0(maxRetryAttempts, "Max Retry Attempts");

            return new PaintRandomizer(this);
        }

        /**
         * Validates that the supplied integer value is greater than 0.
         *
         * <p>
         * This method takes an integer value and a string name as its arguments. The integer value is expected to be greater than 0. The string name serves as a descriptive name for the integer
         * value, which is used in the exception message if the validation fails.
         * </p>
         *
         * <p>
         * If the provided integer value is less than or equal to 0, this method will throw an {@link IllegalArgumentException} with a detailed message.
         * </p>
         *
         * @param value
         *         The integer value to validate.
         * @param name
         *         A descriptive name for the integer value for use in exception messages.
         *
         * @throws IllegalArgumentException
         *         if the integer value is less than or equal to 0.
         */
        public void requireGreaterThan0(int value, String name) {
            if (value <= 1) {
                throw new IllegalArgumentException("%s should be a positive number greater than 0".formatted(name));
            }
        }

        /**
         * Validates that the supplied {@code Supplier} and the value it provides are not null.
         *
         * <p>
         * This method takes a single {@code Object} array as its argument which is expected to contain exactly two elements:
         * <ol>
         *   <li>An instance of {@link Supplier}, whose non-nullity is being checked.</li>
         *   <li>A {@link String}, which serves as the name for the {@code Supplier} for use in exception messages.</li>
         * </ol>
         * </p>
         *
         * <p>
         * If the provided array contains a valid {@code Supplier} and a valid {@code String} name, this method performs the following validations:
         * <ul>
         *   <li>Checks that the {@code Supplier} instance itself is not null.</li>
         *   <li>Checks that the value supplied by the {@code Supplier} is not null.</li>
         * </ul>
         * </p>
         *
         * @param supplierAndName
         *         An {@link Object} array containing a {@link Supplier} and a {@link String} name.
         *
         * @throws NullPointerException
         *         if either the {@code Supplier} instance is null or if it supplies a null value.
         * @throws IllegalArgumentException
         *         if the array does not contain exactly one {@code Supplier} and one {@code String} name.
         */
        public static void requireNonNullSupplierAndValue(Object[] supplierAndName) {
            if ((supplierAndName[0] instanceof Supplier<?> supplier) && (supplierAndName[1] instanceof String name) && supplierAndName.length == 2) {
                Objects.requireNonNull(supplier, () -> String.format("Supplier '%s' must not be null", name));
                Objects.requireNonNull(supplier.get(), () -> String.format("Value from supplier '%s' must not be null", name));
            } else {
                throw new IllegalArgumentException("The Object in the array must be exactly one Supplier and one String");
            }
        }
    }
}
