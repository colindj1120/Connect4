package com.colin.game.algorithms.enums;

/**
 * Enum representing directions for evaluation.
 * Includes 8 possible directions: North, Northeast, East, Southeast, South, Southwest, West, Northwest.
 *
 * @version 1.1
 * @author Colin Jokisch
 */
public enum Direction {
    NORTH(0, -1),
    NORTHEAST(1, -1),
    EAST(1, 0),
    SOUTHEAST(1, 1),
    SOUTH(0, 1),
    SOUTHWEST(-1, 1),
    WEST(-1, 0),
    NORTHWEST(-1, -1);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
