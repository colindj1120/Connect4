package com.colin.game;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * FireworksUtil is a utility class responsible for animating fireworks in a JavaFX Pane. This version includes customization features for firework sizes, fade durations, explosion sizes,
 * gravitational pull, particle lifespan, and function-based particle direction.
 *
 * @author Colin Jokisch
 * @version 1.3
 */
public class FireworksUtil {
    private static final Logger logger = Logger.getLogger(FireworksUtil.class.getName());

    private static class Particle extends Circle {
        double velocityX;
        double velocityY;
        double totalDistance;
        double distanceTraveled;

        Particle(double x, double y, double radius, Paint color, double velocityX, double velocityY) {
            super(x, y, radius, color);
            this.velocityX = velocityX;
            this.velocityY = velocityY;
        }

        Particle(double x, double y, double radius, Paint color, double velocityX, double velocityY, double totalDistance) {
            this(x, y, radius, color, velocityX, velocityY);
            this.totalDistance = totalDistance;
        }
    }

    private static final int      NUM_CYCLES        = 300; // Add this line
    private static final double   TIME_STEP         = 0.05; // Adjusted time step; Add this line
    // Single Timeline for all animations
    private final        Timeline fireworksTimeline = new Timeline();

    private static final int    NUM_KEYFRAMES = 20;
    private static final int    NUM_PARTICLES = 30;
    private static final double MIN_DISTANCE  = 50;
    private static final int    TAIL_LENGTH   = 10;

    private final Random          random;
    private final PaintRandomizer colorRandomizer;
    private final ShapeRandomizer shapeRandomizer;

    private final Pane fireworksPane;

    private final Supplier<Integer> randomFireworkCountSupplier;
    private final Supplier<Double>  fireworkSizeSupplier;
    private final Supplier<Double>  fadeDurationSupplier;
    private final Supplier<Double>  particleDistanceSupplier;
    private final Supplier<Double>  gravitySupplier;
    private final Supplier<Double>  particleLifespanSupplier;
    private final Supplier<Double>  angleMinSupplier;
    private final Supplier<Double>  angleMaxSupplier;
    private final Supplier<Double>  noiseFactorSupplier;
    private final Supplier<Double>  secondaryExplosionChanceSupplier;

    /**
     * Constructs a FireworksUtil object.
     *
     * @param fireworksPane
     *         the JavaFX Pane where the fireworks will be displayed
     * @param fireworkCountSupplier
     *         Supplier for the number of fireworks to be generated
     * @param fireworkSizeSupplier
     *         Supplier for the size of each firework
     * @param fadeDurationSupplier
     *         Supplier for the duration of fade transition for each tail particle
     * @param particleDistanceSupplier
     *         Supplier for the distance to which particles will explode
     * @param gravitySupplier
     *         Supplier for the gravity value affecting the particles
     * @param particleLifespanSupplier
     *         Supplier for the lifespan of each particle after explosion
     * @param angleMinSupplier
     *         Supplier for the minimum angle of firework launch
     * @param angleMaxSupplier
     *         Supplier for the maximum angle of firework launch
     * @param noiseFactorSupplier
     *         Supplier for the noise factor in particle trajectories
     * @param secondaryExplosionChanceSupplier
     *         Supplier for the chance of a secondary explosion
     */
    public FireworksUtil(Pane fireworksPane, Supplier<Integer> fireworkCountSupplier, Supplier<Double> fireworkSizeSupplier, Supplier<Double> fadeDurationSupplier,
                         Supplier<Double> particleDistanceSupplier, Supplier<Double> gravitySupplier, Supplier<Double> particleLifespanSupplier, Supplier<Double> angleMinSupplier,
                         Supplier<Double> angleMaxSupplier, Supplier<Double> noiseFactorSupplier, Supplier<Double> secondaryExplosionChanceSupplier) {
        PaintRandomizer.Builder builder = new PaintRandomizer.Builder();

        this.fireworksPane               = fireworksPane;
        this.random                      = new Random();
        this.randomFireworkCountSupplier = fireworkCountSupplier;
        this.colorRandomizer             = builder.build();

        this.fireworkSizeSupplier             = fireworkSizeSupplier;
        this.fadeDurationSupplier             = fadeDurationSupplier;
        this.particleDistanceSupplier         = particleDistanceSupplier;
        this.gravitySupplier                  = gravitySupplier;
        this.particleLifespanSupplier         = particleLifespanSupplier;
        this.angleMinSupplier                 = angleMinSupplier;
        this.angleMaxSupplier                 = angleMaxSupplier;
        this.noiseFactorSupplier              = noiseFactorSupplier;
        this.secondaryExplosionChanceSupplier = secondaryExplosionChanceSupplier;
        this.shapeRandomizer                  = new ShapeRandomizer();

    }

    public void startFireworks() {
        fireworksTimeline.getKeyFrames()
                         .add(new KeyFrame(Duration.seconds(4), this::launchRandomFireworks));
        fireworksTimeline.setCycleCount(Timeline.INDEFINITE);
        fireworksTimeline.play();
    }

    private void launchRandomFireworks(ActionEvent ignoredEvent) {
        Optional<Integer> countOpt = Optional.ofNullable(randomFireworkCountSupplier.get());
        int               count    = countOpt.orElse(1);
        IntStream.range(0, count)
                 .forEach(i -> launchSingleFirework());
    }

    private void launchSingleFirework() {
        double paneWidth  = fireworksPane.getWidth();
        double paneHeight = fireworksPane.getHeight();

        Paint fireworkColor = colorRandomizer.generateRandomVibrantPaint();

        double angle    = random.nextDouble() * angleMaxSupplier.get() - angleMinSupplier.get();
        double radians  = Math.toRadians(angle);
        double distance = MIN_DISTANCE + random.nextDouble() * (paneHeight - MIN_DISTANCE);
        double gravity      = gravitySupplier.get();
        double speedFactor  = random.nextDouble(1, 2);
        double initialSpeed = Math.sqrt(2 * gravity * distance) * speedFactor;

        double fireworkSize = fireworkSizeSupplier.get();

        Particle firework = new Particle(paneWidth / 2, paneHeight, fireworkSize, fireworkColor, initialSpeed * Math.sin(radians), initialSpeed * Math.cos(radians), distance);
        fireworksPane.getChildren()
                     .add(firework);

        AtomicReference<Timeline> animationRef = new AtomicReference<>();
        animationRef.set(new Timeline(new KeyFrame(Duration.seconds(TIME_STEP), event -> {
            // Previous velocityY before gravity adjustment.
            double oldVelocityY = firework.velocityY;

            // Calculate the remaining distance
            double remainingDistance = firework.totalDistance - firework.distanceTraveled;

            // Adjust the vertical speed due to gravity.
            // Scale the gravity based on the remaining distance
            firework.velocityY = remainingDistance - (Math.sqrt(9.8) * TIME_STEP);
//            if(speedFactor > 1) {
//                double factor = speedFactor/gravity;
//                firework.velocityY -= speedFactor - .95;
//            }

            // Average velocityY used for this time step.
            double avgVelocityY = (oldVelocityY + firework.velocityY) / 2.0;

            // Calculating changes in position.
            double deltaX = firework.velocityX * TIME_STEP;
            double deltaY = avgVelocityY * TIME_STEP;

            double newDistance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            firework.distanceTraveled += newDistance;
            firework.setCenterX(firework.getCenterX() + deltaX);
            firework.setCenterY(firework.getCenterY() - deltaY);

            // Tail animation logic
            Circle tailPart = new Circle(firework.getCenterX(), firework.getCenterY(), fireworkSize, firework.getFill());
            fireworksPane.getChildren()
                         .add(tailPart);
            FadeTransition ft = new FadeTransition(Duration.seconds(fadeDurationSupplier.get()), tailPart);
            ft.setToValue(0);
            ft.setOnFinished(e -> fireworksPane.getChildren()
                                               .remove(tailPart));
            ft.play();


            if(firework.velocityY < 10) {
                fireworksPane.getChildren()
                             .remove(firework);
                double chance = secondaryExplosionChanceSupplier.get();
                if (random.nextDouble() < chance) {
                    explodeFirework(firework.getCenterX(), firework.getCenterY(), firework.getFill());
                }
                explodeFirework(firework.getCenterX(), firework.getCenterY(), firework.getFill());
                animationRef.get()
                            .stop();
            }
        })));
        animationRef.get()
                    .setCycleCount(Animation.INDEFINITE);
        animationRef.get()
                    .play();
    }

    private void explodeFirework(double x, double y, Paint color) {
        double particleDistance = particleDistanceSupplier.get();
        double particleLifespan = particleLifespanSupplier.get();
        IntStream.range(0, NUM_PARTICLES)
                 .forEach(i -> {
                     Shape particle = shapeRandomizer.generateRandomShape();
                     particle.setLayoutX(x);
                     particle.setLayoutY(y);
                     particle.setFill(color);
                     fireworksPane.getChildren()
                                  .add(particle);

                     double angle = 2 * Math.PI * i / NUM_PARTICLES;
                     double dx    = particleDistance * Math.cos(angle);
                     double dy    = particleDistance * Math.sin(angle);

                     TranslateTransition tt = new TranslateTransition(Duration.seconds(particleLifespan), particle);
                     tt.setByX(dx);
                     tt.setByY(dy);
                     tt.setOnFinished(e -> fireworksPane.getChildren()
                                                        .remove(particle));
                     tt.play();

                     FadeTransition ft = new FadeTransition(Duration.seconds(particleLifespan), particle);
                     ft.setToValue(0);
                     ft.setOnFinished(e -> fireworksPane.getChildren()
                                                        .remove(particle));
                     ft.play();
                 });
    }
}

//TODO:
/*
Instead of distance think frames dynamically calculate how many frames a firework can travel based on its intial speed, and how that changes over time. Then get a random number between 1/2 half and
 that

Particle Lifetime: Right now, the lifespan of each particle is set at creation. You might want to add more dynamics, like reducing size or changing color as the particle "ages".

Improve Performance: While the JavaFX Pane makes it easy to add and remove nodes, each node has a performance cost. You could use more complex Shape nodes for the fireworks particles or even go to
a lower level and draw directly on a Canvas to improve performance.

Angle Calculation: The calculation of the angle of launch seems to depend only on a Random object. You could consider using more complex calculations for even more realistic behavior.

Error Handling: If any of the Suppliers return null or invalid values, the code doesn't handle these scenarios. You might want to add some checks or fallbacks for these.

Reuse of Timeline: Each firework uses a new timeline for its animation. While this is not a significant problem for a small number of fireworks, for a large number of fireworks, it can be more
efficient to reuse timelines or use a shared animation timer.

JavaFX Property Binding: Consider using JavaFX properties and bindings for some of the configurable parameters. This would make it easy to dynamically adjust the settings of the fireworks in
real-time.

Functional Programming: Your use of Suppliers is a great touch, and you can potentially add more functional aspects to your code by, for instance, allowing users to provide custom functions for
other aspects of the fireworks like shape, velocity curve, or color changes.
 */