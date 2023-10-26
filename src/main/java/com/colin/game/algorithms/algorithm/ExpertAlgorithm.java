package com.colin.game.algorithms.algorithm;

import com.colin.game.algorithms.objects.Minimax;
import com.colin.game.algorithms.objects.Move;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * ExpertAlgorithm class implements the Algorithm interface to provide an expert level AI for Connect 4.
 * This AI uses a highly optimized version of the minimax algorithm with alpha-beta pruning,
 * deeper search, and sophisticated evaluation functions.
 *
 * @author Colin Jokisch
 * @version 1.0
 */
public class ExpertAlgorithm implements Algorithm {
    private static final double ERROR_FACTOR = .01;
    private static final int MAX_DEPTH = 7;

    private final Minimax minimax;

    /**
     * Constructs an ExpertAlgorithm object.
     *
     * @param isColumnAvailable A Predicate to check if a column is available for a move.
     * @param getBoardState A Supplier that provides the current board state.
     */
    public ExpertAlgorithm(Predicate<Integer> isColumnAvailable, Supplier<int[][]> getBoardState,
                           int aiToken, int opponentToken) {
        this.minimax = new Minimax(getBoardState, isColumnAvailable, aiToken, opponentToken, MAX_DEPTH, ERROR_FACTOR);
    }

    /**
     * Makes a move based on the current board state.
     * Uses a highly optimized version of the minimax algorithm with alpha-beta pruning,
     * deeper search, and sophisticated evaluation functions.
     *
     * @return The column index where the token should be placed.
     */
    @Override
    public int makeMove() {
        Move bestMove = minimax.minimax(MAX_DEPTH, true);
        return bestMove.column();
    }
}

