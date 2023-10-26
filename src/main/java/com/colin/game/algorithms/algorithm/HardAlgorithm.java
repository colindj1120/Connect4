package com.colin.game.algorithms.algorithm;

import com.colin.game.algorithms.AlgorithmUtility;
import com.colin.game.algorithms.interfaces.AlphaBetaUpdater;
import com.colin.game.algorithms.interfaces.BestMoveUpdater;
import com.colin.game.algorithms.objects.AlphaBeta;
import com.colin.game.algorithms.objects.Minimax;
import com.colin.game.algorithms.objects.Move;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * HardAlgorithm class for providing a hard level AI for Connect 4.
 * This AI uses the minimax algorithm with alpha-beta pruning to look several moves ahead.
 *
 * @author Colin Jokisch
 * @version 1.10
 */
public class HardAlgorithm implements Algorithm {
    private static final double ERROR_FACTOR = .05;
    private static final int MAX_DEPTH = 4;

    private final Minimax minimax;

    public HardAlgorithm(Predicate<Integer> isColumnAvailable, Supplier<int[][]> getBoardState,
                         int aiToken, int opponentToken) {
        this.minimax = new Minimax(getBoardState, isColumnAvailable, aiToken, opponentToken, MAX_DEPTH, ERROR_FACTOR);
    }

    /**
     * Calls the minimax algorithm to decide the best move for the AI.
     *
     * @return the column number for the optimal move
     */
    @Override
    public int makeMove() {
        Move bestMove = minimax.minimax(MAX_DEPTH, true);
        return bestMove.column();
    }
}
