package com.colin.game.algorithms.objects;

public record Move(int column, int score) implements Comparable<Move> {
    @Override
    public int compareTo(Move other) {
        return Integer.compare(this.score, other.score);
    }
}
