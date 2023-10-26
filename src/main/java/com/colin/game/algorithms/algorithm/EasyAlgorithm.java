package com.colin.game.algorithms.algorithm;

import com.colin.game.algorithms.AlgorithmUtility;

import java.util.Optional;
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * EasyAlgorithm class implements the Algorithm interface to provide an easy level AI for Connect 4.
 * This AI tries to win if possible, blocks the opponent if they are about to win, or makes a random move.
 * It also prefers the center column if available.
 *
 * @author Colin Jokisch
 * @version 1.2
 */
public class EasyAlgorithm implements Algorithm {
    private static final int EMPTY_SLOT = 0;
    private static final int FULL_COLUMN = -1;
    private static final int CONSECUTIVE_TOKENS_NEEDED = 4;
    private static final int DIRECTION_RANGE = 3;

    private final Predicate<Integer> isColumnAvailable;
    private final Supplier<int[][]> getBoardState;
    private final Random random = new Random();
    private final int aiToken;
    private final int numColumns;
    private final int numRows;

    /**
     * Constructs an instance of the EasyAlgorithm class.
     *
     * @param isColumnAvailable A Predicate that tests if a given column index is available for making a move.
     * @param getBoardState     A Supplier that provides the current state of the game board as a 2D array.
     * @param aiToken           An integer representing the AI's token.
     */
    public EasyAlgorithm(Predicate<Integer> isColumnAvailable, Supplier<int[][]> getBoardState, int aiToken) {
        this.isColumnAvailable = isColumnAvailable;
        this.getBoardState = getBoardState;
        this.aiToken = aiToken;

        int[][] boardState = getBoardState.get();

        this.numColumns = boardState[0].length;
        this.numRows = boardState.length;
    }

    /**
     * Makes a move in the game.
     * @return The index of the column where the move is made.
     */
    @Override
    public int makeMove() {
        return findMove(aiToken)
                .orElseGet(this::randomMove);
    }

    /**
     * Finds a suitable move for the given player ID.
     * It iterates through all available columns and checks for a winning move without altering board state.
     *
     * @param playerId The ID of the player for whom to find a move.
     * @return An Optional containing the index of the column if a suitable move is found; otherwise, an empty Optional.
     */
    private Optional<Integer> findMove(int playerId) {
        int[][] tempBoardState = AlgorithmUtility.cloneGameState(getBoardState);
        return IntStream.range(0, numColumns)
                        .filter(isColumnAvailable::test)
                        .filter(col -> checkMove(col, tempBoardState, playerId))
                        .boxed()
                        .findFirst();
    }

    /**
     * Makes a random move.
     *
     * @return The index of the column where the move is made.
     */
    private int randomMove() {
        int randomMove;
        do {
            randomMove = random.nextInt(numColumns);
        } while (!isColumnAvailable.test(randomMove));
        return randomMove;
    }

    /**
     * Checks if making a move in the given column for the given player ID would result in a winning move.
     * It temporarily updates the board state to simulate the move and checks for 4 consecutive tokens.
     *
     * @param col       The index of the column where the move is to be made.
     * @param boardState The current state of the board.
     * @param playerId  The ID of the player for whom to check the move.
     * @return true if the move would result in a winning situation; false otherwise.
     */
    private boolean checkMove(int col, int[][] boardState, int playerId) {
        int row = findEmptyRow(col, boardState);
        if (row == FULL_COLUMN) {
            return false;
        }
        boardState[row][col] = playerId;
        boolean isDesiredMove = checkConsecutiveTokens(row, col, boardState, playerId);
        boardState[row][col] = EMPTY_SLOT;
        return isDesiredMove;
    }


    /**
     * Checks if there are 4 consecutive tokens in a given direction from a given cell.
     *
     * @param row        The row index of the cell.
     * @param col        The column index of the cell.
     * @param boardState The current state of the board.
     * @param playerId   The ID of the player whose tokens to check.
     * @param dx         The change in x (column) for each step.
     * @param dy         The change in y (row) for each step.
     * @return true if there are 4 consecutive tokens in the given direction, false otherwise.
     */
    private boolean checkDirection(int row, int col, int[][] boardState, int playerId, int dx, int dy) {
        final int[] count = {0};

        IntPredicate isInBounds = step -> row + step * dy >= 0 && row + step * dy < numRows &&
                                          col + step * dx >= 0 && col + step * dx < numColumns;

        IntPredicate isConsecutiveToken = step -> boardState[row + step * dy][col + step * dx] == playerId;

        return IntStream.rangeClosed(-DIRECTION_RANGE, DIRECTION_RANGE)
                        .filter(isInBounds)
                        .anyMatch(step -> {
                            if (isConsecutiveToken.test(step)) {
                                count[0]++;
                                return count[0] == CONSECUTIVE_TOKENS_NEEDED;
                            } else {
                                count[0] = 0;
                                return false;
                            }
                        });
    }

    /**
     * Finds the first empty row in a given column.
     *
     * @param col        The column index to check.
     * @param boardState The current state of the board.
     * @return The row index of the first empty slot, or -1 if the column is full.
     */
    private int findEmptyRow(int col, int[][] boardState) {
        return IntStream.rangeClosed(0, numRows - 1)
                        .map(i -> numRows - i - 1)
                        .filter(row -> boardState[row][col] == EMPTY_SLOT)
                        .findFirst()
                        .orElse(FULL_COLUMN);
    }

    /**
     * Checks if there are 4 consecutive tokens horizontally, vertically, or diagonally from a given cell.
     *
     * @param row        The row index of the cell.
     * @param col        The column index of the cell.
     * @param boardState The current state of the board.
     * @param playerId   The ID of the player whose tokens to check.
     * @return true if there are 4 consecutive tokens, false otherwise.
     */
    private boolean checkConsecutiveTokens(int row, int col, int[][] boardState, int playerId) {
        return IntStream.of(1, 0, 1, 1, 1, -1)
                        .boxed()
                        .anyMatch(dx -> checkDirection(row, col, boardState, playerId, dx, dx == 0 ? 1 : 0));
    }
}
