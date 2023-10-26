package com.colin.game.algorithms.evaluators;

import com.colin.game.enums.Direction;
import com.colin.game.state.GameConfig;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * Evaluates the center control of a game board for AI decision making.
 * <p>
 * The class encompasses several heuristics for determining control such as adjacency,
 * diagonals, and more. Each heuristic is weighted according to importance.
 * <p>
 * The following weights are defined:
 * ADJACENT_WEIGHT: Weight of adjacent control around the center.
 * TWO_IN_A_ROW_WEIGHT: Weight of having two in a row around center.
 * CONTROLLED_DIAGONAL_WEIGHT: Weight of controlling the diagonals.
 * BLOCK_OPPONENT_CENTER_WEIGHT: Weight of blocking opponent at the center.
 * CONNECTIVITY_WEIGHT: Weight of being connected around the center.
 * MOBILITY_WEIGHT: Weight of possible moves around the center.
 *
 * @version 1.0
 * @author Colin Jokisch
 */
public class CenterControlEvaluator {
    private final int[][] board;
    private final int aiToken;
    private final int opponentToken;
    private final int steps;


    private static final int CENTER_COLUMN = GameConfig.getInstance().getNumCols() / 2;
    private static final int CENTER_ROW = GameConfig.getInstance().getNumRows() / 2;

    private static final int ADJACENT_WEIGHT = 50;
    private static final int TWO_IN_A_ROW_WEIGHT = 30;
    private static final int CONTROLLED_DIAGONAL_WEIGHT = 40;
    private static final int BLOCK_OPPONENT_CENTER_WEIGHT = 20;
    private static final int CONNECTIVITY_WEIGHT = 20;
    private static final int MOBILITY_WEIGHT = 10;
    private static final int OPPONENT_ADJACENT_WEIGHT = 25;
    private static final int DEFENSIVE_BLOCK_WEIGHT = 100;
    private static final int AGGRESSIVE_BLOCK_WEIGHT = 20;

    private static final int REQUIRED_TOKENS_FOR_CONTROL = 2;

    /**
     * Constructs a CenterControlEvaluator with the given board, AI token, and opponent token.
     *
     * @param board        2D array representing the game board.
     * @param aiToken      Token representing the AI on the board.
     * @param opponentToken Token representing the opponent on the board.
     */
    public CenterControlEvaluator(int[][] board, int aiToken, int opponentToken, int depth) {
        this.board = board;
        this.aiToken = aiToken;
        this.opponentToken = opponentToken; // Initialize the opponent token
        this.steps = calculateStepsBasedOnDepth(depth); // Initialize steps based on depth
    }

    /**
     * Calculates the number of steps to evaluate in each direction based on the provided depth.
     *
     * @param depth The depth of the algorithm's search tree.
     * @return The calculated number of steps.
     */
    private int calculateStepsBasedOnDepth(int depth) {
        // Take the minimum of either rows or columns to make sure not to go out of bounds
        int minDimension = Math.min(EvaluatorConstants.NUM_ROWS, EvaluatorConstants.NUM_COLUMNS);

        // Divide by 2 to ensure steps from the center won't exceed the board boundaries
        int maxSteps = minDimension / 2;

        // Limit the steps to maxSteps if depth exceeds it
        return Math.min(depth, maxSteps);
    }


    /**
     * The main evaluation function, summing up various individual criteria.
     *
     * @return The overall center control score.
     */
    public int evaluate() {
        int opponentThreatLevel = evaluateOpponentThreatLevel();

        int dynamicBlockingWeight = opponentThreatLevel >= 3 ? DEFENSIVE_BLOCK_WEIGHT :
                                    opponentThreatLevel == 2 ? DEFENSIVE_BLOCK_WEIGHT / 2 : AGGRESSIVE_BLOCK_WEIGHT;

        return IntStream.of(
                evaluateAdjacentToCenter(),
                evaluateTwoInARowAroundCenter(),
                evaluateControlOfDiagonals(),
                evaluateDynamicBlocking(dynamicBlockingWeight),
                evaluateBlockingOpponentCenterControl(),
                evaluateConnectivity(),
                evaluateMobility(),
                evaluateForRowsAndColumns(),
                evaluateDiagonals(),
                evaluateOpponentAdjacentToCenter()
        ).sum();
    }

    /**
     * Evaluates the dynamic blocking based on the opponent's threat level.
     *
     * @param dynamicBlockingWeight The weight to apply for dynamic blocking.
     * @return The score based on dynamic blocking.
     */
    private int evaluateDynamicBlocking(int dynamicBlockingWeight) {
        return evaluateForDirections(Direction.values(), dynamicBlockingWeight);
    }

    /**
     * Evaluate the opponent's threat level based on their tokens.
     *
     * @return Threat level (0, 1, 2, 3 etc.)
     */
    private int evaluateOpponentThreatLevel() {
        return Arrays.stream(Direction.values())
                     .mapToInt(direction -> countTokensInDirection(direction, opponentToken))
                     .max().orElse(0);
    }

    /**
     * Count tokens in a specific direction from a starting point.
     *
     * @param direction
     *         Direction to count
     * @param token
     *         Token to count
     *
     * @return The count of tokens
     */
    private int countTokensInDirection(Direction direction, int token) {
        return IntStream.rangeClosed(1, steps)
                        .map(i -> checkPosition(CenterControlEvaluator.CENTER_ROW + i * direction.getDy(), CenterControlEvaluator.CENTER_COLUMN + i * direction.getDx()).orElse(0))
                        .filter(t -> t == token)
                        .sum();
    }

    /**
     * Evaluates rows and columns around the center for potential scoring.
     * Uses utility functions for horizontal and vertical evaluation.
     *
     * @return The sum of scores for all rows and columns.
     */
    private int evaluateForRowsAndColumns() {
        IntUnaryOperator rowEvaluator = row -> MinimaxEvaluatorUtility.evaluateHorizontal(aiToken, board, row, CENTER_COLUMN);
        IntUnaryOperator colEvaluator = col -> MinimaxEvaluatorUtility.evaluateVertical(aiToken, board, CENTER_ROW, col);

        return IntStream.range(0, EvaluatorConstants.NUM_ROWS).map(rowEvaluator)
                        .sum() + IntStream.range(0, EvaluatorConstants.NUM_COLUMNS).map(colEvaluator).sum();
    }

    /**
     * Evaluates the AI's control over the diagonals passing through the center of the board.
     *
     * @return The combined score for control of both the main and anti-diagonals.
     */
    private int evaluateDiagonals() {
        return MinimaxEvaluatorUtility.evaluateDiagonal(aiToken, board, CENTER_ROW, CENTER_COLUMN, true) +
               MinimaxEvaluatorUtility.evaluateDiagonal(aiToken, board, CENTER_ROW, CENTER_COLUMN, false);
    }

    /**
     * Evaluates the AI's control adjacent to the center of the board.
     * Utilizes the {@code evaluateForDirections} method to check each direction around the center.
     *
     * @return The score based on the AI's control of adjacent positions around the center.
     */
    private int evaluateAdjacentToCenter() {
        return evaluateForDirections(Direction.values(), ADJACENT_WEIGHT);
    }

    /**
     * Evaluates the AI's potential to form two-in-a-row configurations around the center.
     * Utilizes the {@code evaluateForDirections} method to count the AI's tokens around the center.
     *
     * @return The score based on the AI's two-in-a-row configurations around the center.
     */
    private int evaluateTwoInARowAroundCenter() {
        int horizontalOrVerticalCount = evaluateForDirections(Direction.values(), 1);
        return (horizontalOrVerticalCount == REQUIRED_TOKENS_FOR_CONTROL) ? TWO_IN_A_ROW_WEIGHT : 0;
    }

    /**
     * Evaluates the AI's control over the diagonals (main and anti-diagonal) around the center.
     * Utilizes the {@code evaluateForDirections} method to count the AI's tokens on the diagonals.
     *
     * @return The score based on the AI's control of the diagonals around the center.
     */
    private int evaluateControlOfDiagonals() {
        int mainDiagonalCount = evaluateForDirections(new Direction[]{Direction.NORTHEAST, Direction.SOUTHWEST}, 1);
        int antiDiagonalCount = evaluateForDirections(new Direction[]{Direction.NORTHWEST, Direction.SOUTHEAST}, 1);

        return (mainDiagonalCount == REQUIRED_TOKENS_FOR_CONTROL || antiDiagonalCount == REQUIRED_TOKENS_FOR_CONTROL)
               ? CONTROLLED_DIAGONAL_WEIGHT : 0;
    }

    /**
     * Evaluates the AI's capability to block the opponent from controlling the center.
     * Utilizes the {@code evaluateForDirections} method to check each direction around the center.
     *
     * @return The score based on the AI's ability to block the opponent in the center.
     */
    private int evaluateBlockingOpponentCenterControl() {
        return evaluateForDirections(Direction.values(), BLOCK_OPPONENT_CENTER_WEIGHT);
    }

    /**
     * Evaluates the AI's connectivity around the center.
     * Utilizes the {@code evaluateForDirections} method to check each direction around the center.
     *
     * @return The score based on the AI's connectivity around the center.
     */
    private int evaluateConnectivity() {
        return evaluateForDirections(Direction.values(), CONNECTIVITY_WEIGHT);
    }

    /**
     * Evaluates the AI's mobility around the center.
     * Utilizes the {@code evaluateForDirections} method to check each direction around the center.
     *
     * @return The score based on the AI's mobility around the center.
     */
    private int evaluateMobility() {
        return evaluateForDirections(Direction.values(), MOBILITY_WEIGHT);
    }

    /**
     * Evaluates the opponent's control adjacent to the center of the board.
     * Utilizes the {@code evaluateForDirections} method to check each direction around the center.
     * The weight for opponent's adjacency is less than that of AI's.
     *
     * @return The score based on the opponent's control of adjacent positions around the center.
     */
    private int evaluateOpponentAdjacentToCenter() {
        return evaluateForDirections(Direction.values(), OPPONENT_ADJACENT_WEIGHT);  // Using opponentToken
    }

    /**
     * A utility function to evaluate the board in multiple directions from the center.
     * Removed the `steps` parameter, which was not being used.
     *
     * @param directions An array of {@code Direction} enumerations representing the directions to evaluate.
     * @param weight     The weight to apply to the evaluations in each direction.
     * @return The sum of weighted evaluation scores for all specified directions.
     */
    private int evaluateForDirections(Direction[] directions, int weight) {
        return Arrays.stream(directions)
                     .mapToInt(direction -> IntStream.rangeClosed(-steps, steps)
                                                     .mapToObj(i -> checkPosition(CENTER_ROW + i * direction.getDy(), CENTER_COLUMN + i * direction.getDx()))
                                                     .filter(Optional::isPresent)
                                                     .mapToInt(Optional::get)
                                                     .sum() * weight)
                     .sum();
    }

    /**
     * Checks a specific board position for tokens.
     *
     * @param row The row index to check.
     * @param col The column index to check.
     * @return An Optional containing 1 if the AI's token is found, -1 if the opponent's token is found, or 0 otherwise.
     * Empty if the position is out of bounds.
     */
    private Optional<Integer> checkPosition(int row, int col) {
        if ((row >= 0) && (row < EvaluatorConstants.NUM_ROWS) &&
            (col >= 0) && (col < EvaluatorConstants.NUM_COLUMNS)) {
            return Optional.of(board[row][col] == aiToken ? 1 : board[row][col] == opponentToken ? -1 : 0);
        }
        return Optional.empty();
    }
}
