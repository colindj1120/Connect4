package com.colin.game.algorithms.algorithm;

import com.colin.game.enums.Direction;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * MediumAlgorithm class provides a simplified medium-level AI for the game Connect 4. It employs a series of strategies to determine the optimal move. Specifically, in this version, it only tries to
 * block the opponent from winning; if it can't, it defaults to an easier algorithm.
 *
 * @author Colin Jokisch
 * @version 1.0
 */
public class MediumAlgorithm implements Algorithm {
    private static final Logger LOGGER = Logger.getLogger(MediumAlgorithm.class.getName());

    private static final Random RANDOM     = new Random();
    private static final double ERROR_RATE = .3;

    private static final int THREE_TOKENS = 3;
    private static final int TWO_TOKENS   = 2;
    private static final int ONE_TOKEN    = 1;

    private final Algorithm         easyAlgorithm;
    private final Supplier<int[][]> getBoardState;
    private final int               aiToken;
    private final int               opponentToken;

    public MediumAlgorithm(Predicate<Integer> isColumnAvailable, Supplier<int[][]> getBoardState, int aiToken, int opponentToken) {
        this.easyAlgorithm = new EasyAlgorithm(isColumnAvailable, getBoardState, aiToken);
        this.getBoardState = getBoardState;
        this.opponentToken = opponentToken;
        this.aiToken       = aiToken;
    }

    /**
     * Makes a move for the AI by considering winning moves, blocking moves, and eventually falling back to an easy algorithm if no such moves are possible.
     *
     * @return the column index of the chosen move.
     */
    public int makeMove() {
        return Stream.<Supplier<Optional<Integer>>>of(() -> checkForWinningMove(aiToken, THREE_TOKENS), () -> checkForWinningMove(opponentToken, THREE_TOKENS),
                                                      () -> checkForWinningMove(aiToken, TWO_TOKENS), () -> checkForWinningMove(opponentToken, TWO_TOKENS))
//                             () -> checkForWinningMove(aiToken, ONE_TOKEN),
//                             () -> checkForWinningMove(opponentToken, ONE_TOKEN))
                     .map(Supplier::get)
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .findFirst()
                     .orElseGet(easyAlgorithm::makeMove);
    }

    /**
     * Checks for a winning move based on a given token and token count.
     *
     * @param token
     *         The token to look for in potential winning sequences.
     * @param tokenCount
     *         The minimum number of tokens that must appear in the sequence for it to be considered a winning move.
     *
     * @return An Optional containing the column index for a potential winning move, or an empty Optional if no such move exists.
     */
    public Optional<Integer> checkForWinningMove(int token, int tokenCount) {
        int[][] boardState     = getBoardState.get();
        int     rows           = boardState.length;
        int     cols           = boardState[0].length;
        int     sequenceLength = tokenCount + 1;

        Optional<Integer> move = generateAllCoordinates(rows, cols).flatMap(coord -> checkAllDirectionsFromCoordinate(coord, sequenceLength, token, tokenCount))
                                                                   .flatMap(Optional::stream)
                                                                   .findFirst();

        if (RANDOM.nextDouble() < ERROR_RATE) {
            LOGGER.info("Human Error move missed");
            return Optional.empty();
        }

        move.ifPresent(winningCol -> LOGGER.info("Found a sequence of " + tokenCount + " tokens with gap. Next move column: " + winningCol));

        return move;
    }

    /**
     * Generates all possible coordinates on the board.
     *
     * @param rows
     *         Number of rows on the board.
     * @param cols
     *         Number of columns on the board.
     *
     * @return A Stream of integer arrays representing the coordinates.
     */
    private Stream<int[]> generateAllCoordinates(int rows, int cols) {
        return IntStream.range(0, rows)
                        .boxed()
                        .flatMap(row -> IntStream.range(0, cols)
                                                 .mapToObj(col -> new int[]{row, col}));
    }

    /**
     * Checks for potential moves in all directions from a given coordinate.
     *
     * @param coord
     *         Coordinate to start from.
     * @param sequenceLength
     *         The length of the sequence to check.
     * @param token
     *         The token to look for.
     * @param tokenCount
     *         The minimum number of tokens needed for a potential win.
     *
     * @return A Stream of Optional<Integer> representing potential moves.
     */
    private Stream<Optional<Integer>> checkAllDirectionsFromCoordinate(int[] coord, int sequenceLength, int token, int tokenCount) {
        return Arrays.stream(Direction.values())
                     .map(dir -> findPotentialMove(coord, dir, sequenceLength, token, tokenCount));
    }

    /**
     * Finds a potential move based on a starting coordinate, direction, and sequence length.
     *
     * @param coord
     *         The starting coordinate (row, column) for checking the sequence.
     * @param dir
     *         The direction in which to check.
     * @param sequenceLength
     *         The length of the sequence to check.
     * @param token
     *         The token to match in the sequence.
     * @param tokenCount
     *         The minimum count of tokens that must be found.
     *
     * @return An Optional containing the column index for a potential move, or an empty Optional if no such move exists.
     */
    private Optional<Integer> findPotentialMove(int[] coord, Direction dir, int sequenceLength, int token, int tokenCount) {
        int[][] boardState = getBoardState.get();
        int     rows       = boardState.length;
        int     cols       = boardState[0].length;

        Predicate<int[]> isValidCoord        = newCoord -> newCoord[0] >= 0 && newCoord[0] < rows && newCoord[1] >= 0 && newCoord[1] < cols;
        Predicate<int[]> isPotentialNextCell = getIsPotentialNextCell(boardState, rows, isValidCoord);

        int[] tokenSequence = IntStream.range(0, sequenceLength)
                                       .mapToObj(step -> new int[]{coord[0] + dir.getDx() * step, coord[1] + dir.getDy() * step})
                                       .filter(isValidCoord)
                                       .mapToInt(cell -> boardState[cell[0]][cell[1]])
                                       .toArray();

        long count = Arrays.stream(tokenSequence)
                           .filter(t -> t == token)
                           .count();

        long emptyCount = Arrays.stream(tokenSequence)
                                .filter(t -> t == 0)
                                .count();

        if (count >= tokenCount && emptyCount > 0) {
            return IntStream.range(0, sequenceLength)
                            .mapToObj(step -> new int[]{coord[0] + dir.getDx() * step, coord[1] + dir.getDy() * step})
                            .filter(isValidCoord)
                            .filter(isPotentialNextCell)
                            .map(cell -> cell[1])
                            .findFirst();
        }
        return Optional.empty(); // No winning move found in this direction
    }

    /**
     * Combines predicates to form a valid potential next cell for a move.
     *
     * @param boardState
     *         The current state of the board.
     * @param rows
     *         The number of rows in the board.
     * @param isValidCoord
     *         Predicate to check if a coordinate is valid.
     *
     * @return A Predicate to check if a cell is a valid potential next cell.
     */
    private static Predicate<int[]> getIsPotentialNextCell(int[][] boardState, int rows, Predicate<int[]> isValidCoord) {
        // Checks if the cell at the coordinate is empty
        Predicate<int[]> isCellEmpty = coord -> boardState[coord[0]][coord[1]] == 0;

        // Checks if the cell at the coordinate is a playable move
        Predicate<int[]> isPlayableCell = coord -> coord[0] == rows - 1 || boardState[coord[0] + 1][coord[1]] != 0;

        return coord -> isValidCoord.test(coord) && isCellEmpty.test(coord) && isPlayableCell.test(coord);
    }
}