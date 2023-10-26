package com.colin.game.algorithms;

import java.util.Arrays;
import java.util.function.Supplier;

public class AlgorithmUtility {
    public static int[][] cloneGameState(Supplier<int[][]> getBoardState) {
        return Arrays.stream(getBoardState.get())
                     .map(int[]::clone)
                     .toArray(int[][]::new);
    }
}
