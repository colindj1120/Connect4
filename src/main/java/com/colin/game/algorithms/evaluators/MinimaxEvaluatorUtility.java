package com.colin.game.algorithms.evaluators;


import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.colin.game.algorithms.evaluators.EvaluatorConstants.*;

public class MinimaxEvaluatorUtility {
    public static int evaluateHorizontal(int playerToken, int[][] board, int row, int col) {
        // Implement evaluation criteria for horizontal positions.
        int boardWidth       = board[0].length;

        return IntStream.range(0, boardWidth - CONSECUTIVE_COUNT + 1)
                        .mapToObj(c -> IntStream.range(0, CONSECUTIVE_COUNT)
                                                .map(i -> board[row][c + i] == playerToken ? 1 : 0)
                                                .sum())
                        .map(MinimaxEvaluatorUtility::mapScoreToResult)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Horizontal score not calculated"));
    }

    public static int evaluateVertical(int playerToken, int[][] board, int row, int col) {
        // Implement evaluation criteria for vertical positions.
        int boardHeight      = board.length;

        return IntStream.range(0, boardHeight - CONSECUTIVE_COUNT + 1)
                        .mapToObj(r -> IntStream.range(0, CONSECUTIVE_COUNT)
                                                .map(i -> board[r + i][col] == playerToken ? 1 : 0)
                                                .sum())
                        .map(MinimaxEvaluatorUtility::mapScoreToResult)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Vertical score not calculated"));
    }

    public static int evaluateDiagonal(int playerToken, int[][] board, int row, int col, boolean isMainDiagonal) {
        // Implement evaluation criteria for diagonal positions.
        int boardHeight = board.length;
        int boardWidth  = board[0].length;

        Predicate<Integer> isValidPosition = index -> index >= 0 && index < boardHeight;

        int score = IntStream.range(0, CONSECUTIVE_COUNT)
                             .map(i -> {
                                 int r = row + (isMainDiagonal ? i : -i);
                                 int c = col + i;

                                 return isValidPosition.test(r) && c >= 0 && c < boardWidth && board[r][c] == playerToken ? 1 : 0;
                             })
                             .sum();

        return switch (score) {
            case CONSECUTIVE_COUNT -> AI_WIN_SCORE;
            case CONSECUTIVE_COUNT - 1 -> THREE_IN_A_ROW;
            case CONSECUTIVE_COUNT - 2 -> TWO_IN_A_ROW;
            default -> 0;
        };
    }

    public static int evaluatePlayer(int playerToken, int[][] board) {
        // Implement evaluation criteria for the given player.
        int playerScore = 0;
        // Your logic here
        return playerScore;
    }

    private static int mapScoreToResult(int score) {
        if (score == CONSECUTIVE_COUNT) {
            return AI_WIN_SCORE;
        }
        if (score == CONSECUTIVE_COUNT - 1) {
            return THREE_IN_A_ROW;
        }
        if (score == CONSECUTIVE_COUNT - 2) {
            return TWO_IN_A_ROW;
        }
        return 0;
    }
}