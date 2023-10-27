package com.colin.game.state;

import com.colin.game.enums.Direction;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Manages the state of the Connect4 game, including the board and players. Handles operations such as placing a token and checking the game state.
 *
 * @author Colin Jokisch
 * @version 1.1
 */
public class GameState {
    private static final int MIN_TOKENS_FOR_WIN = 4;

    private final int     numRows = GameConfig.getInstance()
                                              .getNumRows();
    private final int     numCols = GameConfig.getInstance()
                                              .getNumCols();
    private final int[][] board   = new int[numRows][numCols];

    private int     currentPlayerId = 1; // Start with player 1
    private boolean gameOver        = false; // flag to track if the game is over
    private boolean stalemate       = false; // flag to track if the game is in a stalemate

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

    /**
     * @return true if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        return gameOver;
    }

    public int getCurrentPlayerId() {
        return currentPlayerId;
    }

    /**
     * Switches the current player after checking for a win.
     */
    public void switchPlayer() {
        checkWin();  // check for a win before switching
        checkStalemate();
        if (!gameOver) {
            currentPlayerId = 3 - currentPlayerId; // Switch player only if the game is not over
        }
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
        OptionalInt optionalRow = IntStream.rangeClosed(0, numRows - 1)
                                           .map(i -> numRows - 1 - i) // reverse the order
                                           .filter(row -> board[row][column] == 0)
                                           .findFirst();

        optionalRow.ifPresent(row -> board[row][column] = playerId);

        return optionalRow.orElse(-1);
    }

    /**
     * Checks if a win condition is met and sets the gameOver flag accordingly.
     */
    public void checkWin() {
        gameOver = IntStream.range(0, numRows)
                            .anyMatch(row -> IntStream.range(0, numCols)
                                                      .anyMatch(col -> checkAllDirections(row, col)));
    }

    private boolean checkAllDirections(int startRow, int startCol) {
        return Arrays.stream(Direction.values())
                     .anyMatch(direction -> checkDirection(startRow, startCol, direction));
    }

    /**
     * Checks a single direction for a winning condition based on a starting point.
     *
     * @param startRow
     *         The starting row index.
     * @param startCol
     *         The starting column index.
     * @param direction
     *         The direction to check.
     *
     * @return true if a winning condition is found, false otherwise.
     */
    private boolean checkDirection(int startRow, int startCol, Direction direction) {
        int dx = direction.getDx();
        int dy = direction.getDy();

        return IntStream.range(0, MIN_TOKENS_FOR_WIN)
                        .mapToObj(i -> new int[]{startRow + i * dx, startCol + i * dy})
                        .allMatch(coord -> coord[0] >= 0 && coord[0] < numRows && coord[1] >= 0 && coord[1] < numCols && board[coord[0]][coord[1]] == currentPlayerId);
    }

    /**
     * Checks if the game is in a stalemate condition.
     */
    public void checkStalemate() {
        if (gameOver) {
            return; // No need to check if the game is already over
        }

        stalemate = !winPossibleForEitherPlayer();
    }

    /**
     * @return true if the game is in a stalemate condition, false otherwise.
     */
    public boolean isStalemate() {
        return stalemate;
    }

    /**
     * Checks if a win is still possible for either player.
     *
     * @return true if a win is possible, false otherwise.
     */
    private boolean winPossibleForEitherPlayer() {
        Predicate<int[]> isValidCoord = coord -> coord[0] >= 0 && coord[0] < numRows && coord[1] >= 0 && coord[1] < numCols;

        return IntStream.range(0, numRows)
                        .boxed()
                        .flatMap(row -> IntStream.range(0, numCols)
                                                 .mapToObj(col -> new int[]{row, col}))
                        .anyMatch(coord -> isWinPossibleFromCoord(coord, isValidCoord));
    }

    /**
     * Checks if a win is possible from a given coordinate.
     *
     * @param coord The coordinate to check from.
     * @param isValidCoord Predicate to validate coordinates.
     * @return true if a win is possible from that coordinate for either player, false otherwise.
     */
    private boolean isWinPossibleFromCoord(int[] coord, Predicate<int[]> isValidCoord) {
        return Arrays.stream(Direction.values())
                     .anyMatch(direction -> IntStream.range(1, 3)
                                                     .anyMatch(playerId -> isWinPossibleFromCoordForPlayer(coord, direction, playerId, isValidCoord)));
    }

    /**
     * Checks if a win is possible for a sequence of cells in a specific direction and for a specific player.
     *
     * @param coord The starting coordinate.
     * @param direction The direction to check.
     * @param playerId The player ID to check for.
     * @param isValidCoord Predicate to validate coordinates.
     * @return true if a win is possible in that direction for the specified player, false otherwise.
     */
    private boolean isWinPossibleFromCoordForPlayer(int[] coord, Direction direction, int playerId, Predicate<int[]> isValidCoord) {
        return IntStream.range(0, MIN_TOKENS_FOR_WIN)
                        .mapToObj(i -> new int[]{coord[0] + i * direction.getDx(), coord[1] + i * direction.getDy()})
                        .allMatch(isValidCoord) &&
               IntStream.range(0, MIN_TOKENS_FOR_WIN)
                        .mapToObj(i -> new int[]{coord[0] + i * direction.getDx(), coord[1] + i * direction.getDy()})
                        .map(cell -> board[cell[0]][cell[1]])
                        .filter(token -> token == 0 || token == playerId)
                        .count() >= MIN_TOKENS_FOR_WIN;
    }
}
