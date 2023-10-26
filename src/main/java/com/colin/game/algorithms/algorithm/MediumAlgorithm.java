package com.colin.game.algorithms.algorithm;

import com.colin.game.enums.Direction;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * MediumAlgorithm class implements the Algorithm interface to provide a medium level AI for Connect 4.
 * This AI tries to win if possible, block the opponent from winning, and can also avoid traps and find safe spots.
 *
 * @author Colin Jokisch
 * @version 1.0
 */
public class MediumAlgorithm implements Algorithm {
    private static final int THREE_TOKENS = 3;
    private static final int TWO_TOKENS = 2;
    private static final int DIRECTION_STEPS = 4;

    private final Algorithm easyAlgorithm;
    private final Predicate<Integer> isColumnAvailable;
    private final Supplier<int[][]> getBoardState;
    private final int aiToken;
    private final int opponentToken;

    /**
     * Constructs an instance of the MediumAlgorithm class.
     *
     * @param isColumnAvailable A Predicate that tests if a given column index is available for making a move.
     * @param getBoardState    A Supplier that provides the current state of the game board as a 2D array.
     * @param aiToken          The token representing the AI.
     * @param opponentToken    The token representing the opponent.
     */
    public MediumAlgorithm(Predicate<Integer> isColumnAvailable,
                           Supplier<int[][]> getBoardState, int aiToken, int opponentToken) {
        this.easyAlgorithm = new EasyAlgorithm(isColumnAvailable, getBoardState, aiToken);
        this.isColumnAvailable = isColumnAvailable;
        this.getBoardState = getBoardState;
        this.aiToken = aiToken;
        this.opponentToken = opponentToken;
    }

    /**
     * Makes a move in the game by choosing from a set of strategies.
     * Each strategy is executed in the following order:
     * 1. Attempt to win with three connecting AI tokens
     * 2. Block the opponent from winning with three connecting tokens
     * 3. Attempt to win with two connecting AI tokens
     * 4. Block the opponent from winning with two connecting tokens
     *
     * @return The index of the column where the move is made.
     */
    public int makeMove() {
        return findStrategicMove(aiToken, THREE_TOKENS)
                .filter(isColumnAvailable)
                .or(() -> findStrategicMove(opponentToken, THREE_TOKENS).filter(isColumnAvailable))
                .or(() -> findStrategicMove(aiToken, TWO_TOKENS).filter(isColumnAvailable))
                .or(() -> findStrategicMove(opponentToken, TWO_TOKENS).filter(isColumnAvailable))
                .orElseGet(easyAlgorithm::makeMove);
    }

    /**
     * Checks if a token sequence of specified length exists in any direction starting from the given coordinates.
     *
     * @param coord      An array representing the starting coordinates as [row, column].
     * @param token      The token value to check for in the sequence.
     * @param tokenCount The number of tokens in sequence to look for.
     * @return True if a sequence of length {@code tokenCount} exists in any direction starting from {@code coord},
     *         otherwise returns false.
     */
    private boolean matchesTokenCountInAnyDirection(int[] coord, int token, int tokenCount) {
        int[][] boardState = getBoardState.get();
        Predicate<int[]> isValidCoord = c -> c[0] >= 0 && c[0] < boardState.length && c[1] >= 0 && c[1] < boardState[0].length;
        Predicate<int[]> isCellEqualToToken = c -> boardState[c[0]][c[1]] == token;

        return Stream.of(Direction.values())
                     .anyMatch(dir -> IntStream.range(0, DIRECTION_STEPS)
                                               .mapToObj(step -> new int[]{coord[0] + dir.getDx() * step, coord[1] + dir.getDy() * step})
                                               .filter(isValidCoord)
                                               .filter(isCellEqualToToken)
                                               .count() == tokenCount);
    }

    /**
     * Searches for a strategic move based on the specified token and token count.
     *
     * @param token      The token value to look for on the board.
     * @param tokenCount The number of consecutive tokens to look for.
     * @return An {@link Optional} containing the column index for the strategic move, or empty if no such move exists.
     */
    private Optional<Integer> findStrategicMove(int token, int tokenCount) {
        int[][] boardState = getBoardState.get();
        int rows = boardState.length;
        int cols = boardState[0].length;

        return IntStream.range(0, rows)
                        .boxed()
                        .flatMap(row -> IntStream.range(0, cols).mapToObj(col -> new int[]{row, col}))
                        .filter(coord -> matchesTokenCountInAnyDirection(coord, token, tokenCount))
                        .map(coord -> coord[1])
                        .findAny();
    }
}
