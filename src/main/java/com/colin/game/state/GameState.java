package com.colin.game.state;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Manages the state of the Connect4 game, including the board and players. Handles operations such as placing a token and checking the game state.
 *
 * @author Colin Jokisch
 * @version 1.1
 */
public class GameState {
    private final int     numRows         = GameConfig.getInstance()
                                                      .getNumRows();
    private final int     numCols         = GameConfig.getInstance()
                                                      .getNumCols();
    private final int[][] board           = new int[numRows][numCols];
    private       int     currentPlayerId = 1; // Start with player 1

    /**
     * Predicate for checking if a column is available for a token drop.
     */
    public final Predicate<Integer> isColumnAvailable = column -> board[0][column] == 0;

    public GameState() {}

    public int[][] getBoard() {
        return board;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public void setToken(int row, int col, int playerId) {
        // Validation omitted for brevity
        board[row][col] = playerId;
    }

    public boolean isGameOver() {
        // Implement game over logic here
        return false;
    }

    public int getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void switchPlayer() {
        currentPlayerId = 3 - currentPlayerId;
    }

    /**
     * Goes through each cell and applies the given consumer.
     *
     * @param cellConsumer
     *         the consumer to apply to each cell.
     */
    public void forEachCell(Consumer<int[]> cellConsumer) {
        IntStream.range(0, numRows)
                 .forEach(row -> IntStream.range(0, numCols)
                                          .forEach(col -> cellConsumer.accept(new int[]{row, col, board[row][col]})));
    }

    /**
     * Drops a token into a specified column.
     *
     * @param column
     *         the column where the token should be dropped.
     * @param playerId
     *         the ID of the player making the move.
     *
     * @return the row where the token was placed or -1 if the column is full.
     */
    public int dropToken(int column, int playerId) {
        System.out.println("ROWS - 1 " + (numRows - 1));
        for (int row = numRows - 1; row >= 0; row--) {
            if (board[row][column] == 0) {
                board[row][column] = playerId;
                return row;
            }
        }
        return -1; // Column is full
    }
}
