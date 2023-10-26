package com.colin.game.algorithms.objects;

import com.colin.game.algorithms.AlgorithmUtility;
import com.colin.game.algorithms.enums.PlayStyle;
import com.colin.game.algorithms.evaluators.CenterControlEvaluator;
import com.colin.game.algorithms.interfaces.AlphaBetaUpdater;
import com.colin.game.algorithms.interfaces.BestMoveUpdater;
import com.colin.game.state.GameConfig;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.colin.game.algorithms.evaluators.EvaluatorConstants.AI_WIN_SCORE;
import static com.colin.game.algorithms.evaluators.EvaluatorConstants.OPPONENT_WIN_SCORE;

public class Minimax {
    private final Predicate<Integer> isColumnAvailable;
    private final Supplier<int[][]>  getBoardState;
    private final int                aiToken;
    private final int                opponentToken;
    private final int maxDepth;
    private final double errorFactor; // Error factor between 0.0 (never make an error) and 1.0 (always make an error)
    private final Random random = new Random();

    public Minimax(Supplier<int[][]> getBoardState, Predicate<Integer> isColumnAvailable,
                   int aiToken, int opponentToken, int maxDepth, double errorFactor) {
        this.isColumnAvailable = isColumnAvailable;
        this.getBoardState     = getBoardState;
        this.aiToken           = aiToken;
        this.opponentToken     = opponentToken;
        this.maxDepth = maxDepth;
        this.errorFactor = errorFactor;
    }

    public Move minimax(int depth, boolean maximizingPlayer) {
        // Introduce human-like error based on the errorFactor
        // This will make the AI occasionally return a random move without considering the optimal move
        if (maximizingPlayer && (random.nextDouble() < errorFactor)) {
            return makeRandomMove(AlgorithmUtility.cloneGameState(getBoardState));
        }

        int[][]                    board     = AlgorithmUtility.cloneGameState(getBoardState);
        AtomicReference<AlphaBeta> alphaBeta = new AtomicReference<>(new AlphaBeta(Integer.MIN_VALUE, Integer.MAX_VALUE));
        AtomicReference<Move>      bestMove  = new AtomicReference<>(maximizingPlayer ? new Move(-1, Integer.MIN_VALUE) : new Move(-1, Integer.MAX_VALUE));

        // Check for terminal conditions (win/loss/tie) or reach max depth
        int score = evaluateBoard(board);
        if (score == AI_WIN_SCORE || score == OPPONENT_WIN_SCORE || depth == 0) {
            return new Move(-1, score);
        }

        AlphaBetaUpdater updateAlphaBeta = getAlphaBetaUpdater(maximizingPlayer);
        BestMoveUpdater  updateBestMove  = getBestMoveUpdater(maximizingPlayer);

        IntStream.range(0, board[0].length)
                 .filter(isColumnAvailable::test)
                 .forEach(column -> {
                     int row       = dropToken(board, column, maximizingPlayer ? aiToken : opponentToken);
                     int moveScore = minimax(depth - 1, !maximizingPlayer).score();
                     undoMove(board, row, column);

                     bestMove.updateAndGet(move -> updateBestMove.apply(move, column, moveScore));
                     alphaBeta.updateAndGet(ab -> updateAlphaBeta.apply(ab, moveScore));
                 });

        return bestMove.get();
    }

    /**
     * Makes a random move on the board, choosing from the available columns.
     *
     * <p>This method is used to introduce a human-like error in the game AI's decisions.
     * It selects a random column where a token can be dropped, performs the move,
     * and then undoes it to return the board to its original state. Finally, it returns
     * the {@code Move} object that encapsulates the chosen random column and its evaluated score.</p>
     *
     * @param board The current state of the game board.
     * @return The random move made by the AI, encapsulated as a {@code Move} object.
     *         If no columns are available for a move, returns a move with column as -1 and score as Integer.MIN_VALUE.
     *
     * @see Move
     * @see IntStream
     * @see #dropToken(int[][], int, int)
     * @see #undoMove(int[][], int, int)
     * @see #evaluateBoard(int[][])
     */
    private Move makeRandomMove(int[][] board) {
        int[] availableColumns = IntStream.range(0, board[0].length)
                                          .filter(isColumnAvailable::test)
                                          .toArray();
        if (availableColumns.length > 0) {
            int randomColumn = availableColumns[random.nextInt(availableColumns.length)];
            int row = dropToken(board, randomColumn, aiToken);
            int score = evaluateBoard(board);  // Or you can assign a random score here
            undoMove(board, row, randomColumn);
            return new Move(randomColumn, score);
        } else {
            return new Move(-1, Integer.MIN_VALUE);  // No available moves, though this shouldn't happen
        }
    }

    private BestMoveUpdater getBestMoveUpdater(boolean maximizingPlayer) {
        return (bestMove, column, moveScore) -> {
            if ((maximizingPlayer && moveScore > bestMove.score()) || (!maximizingPlayer && moveScore < bestMove.score())) {
                return new Move(column, moveScore);
            }
            return bestMove;
        };
    }

    private AlphaBetaUpdater getAlphaBetaUpdater(boolean maximizingPlayer) {
        return (alphaBeta, moveScore) -> {
            AlphaBeta newAlphaBeta;
            if (maximizingPlayer) {
                newAlphaBeta = new AlphaBeta(Math.max(alphaBeta.alpha(), moveScore), alphaBeta.beta());
            } else {
                newAlphaBeta = new AlphaBeta(alphaBeta.alpha(), Math.min(alphaBeta.beta(), moveScore));
            }
            return newAlphaBeta;
        };
    }

    private int dropToken(int[][] board, int column, int token) {
        int row = IntStream.range(0, board.length)
                           .filter(rowIdx -> board[rowIdx][column] == 0)
                           .findFirst()
                           .orElseThrow(() -> new IllegalStateException("No valid row found in the specified column."));

        board[row][column] = token;
        return row;
    }

    private void undoMove(int[][] board, int row, int column) {
        board[row][column] = 0;
    }

    private int evaluateBoard(int[][] board) {
        PlayStyle selectedPlayStyle = GameConfig.getInstance()
                                                .getAiPlayStyle();

        return switch (selectedPlayStyle) {
            case CENTER_CONTROL -> {
                CenterControlEvaluator centerControlEvaluator = new CenterControlEvaluator(board, aiToken, opponentToken, maxDepth);
                yield centerControlEvaluator.evaluate();
            }
            case EDGE_CONTROL, CORNER_CONTROL, OPEN_LINES, BLOCK_OPPONENT_MOVES, CREATE_WINNING_COMBINATIONS, BALANCE_OFFENSIVE_DEFENSIVE -> -1;
            //            case EDGE_CONTROL -> evaluateEdgeControl(board);
//            case CORNER_CONTROL -> evaluateCornerControl(board);
//            case OPEN_LINES -> evaluateOpenLines(board);
//            case BLOCK_OPPONENT_MOVES -> evaluateBlockOpponentMoves(board);
//            case CREATE_WINNING_COMBINATIONS -> evaluateCreateWinningCombinations(board);
//            case BALANCE_OFFENSIVE_DEFENSIVE -> evaluateBalanceOffensiveDefensive(board);
        };
    }
}

