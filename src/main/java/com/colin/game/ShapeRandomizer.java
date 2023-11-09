package com.colin.game;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * The ShapeRandomizer class is responsible for generating random shapes.
 *
 * @version 1.0
 * @author Colin Jokisch
 */
public class ShapeRandomizer {
    private final Random random;
    private final List<Supplier<Shape>> shapeSuppliers;

    /**
     * Constructs a ShapeRandomizer object with a random seed.
     */
    public ShapeRandomizer() {
        this.random = new Random();
        this.shapeSuppliers = Arrays.asList(
                () -> new Circle(0, 0, 2),
                () -> new Rectangle(0, 0, 4, 4)
        );
    }

    /**
     * Generates a random shape based on the available shape suppliers.
     *
     * @return a random Shape object.
     */
    public Shape generateRandomShape() {
        int index = random.nextInt(shapeSuppliers.size());
        return shapeSuppliers.get(index).get();
    }
}

