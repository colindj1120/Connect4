package com.colin.game.state;

import com.colin.game.factories.CellFactory;
import com.colin.game.factories.TokenAnimationFactory;
import com.colin.game.token.TokenAnimator;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * The GameUI class is responsible for handling the user interface aspects of the game. It initializes the game board and button grid and updates them based on user interaction. It also includes logic
 * for animating token drops.
 *
 * @author Colin Jokisch
 * @version 1.0
 */
public class GameUI {
    private static final TokenAnimator tokenAnimator = TokenAnimationFactory.createTokenDropAnimator();
    private              GameState     gameState;
    private              GridPane      gameBoard;

    public void initializeUI(GameState gameState, GridPane gameBoard) {
        this.gameState = gameState;
        this.gameBoard = gameBoard;
        initializeGameBoard();
    }

    private void initializeGameBoard() {
        IntStream.range(0, gameState.getNumRows())
                 .forEach(row -> IntStream.range(0, gameState.getNumCols())
                                          .forEach(col -> gameBoard.add(CellFactory.createCell(col, row), col, row)));
    }

    public void handleTokenDrop(int column, CompletableFuture<Void> animationFinished) {
        // Drop the token in the GameState object and get the row where it was placed.
        int currentPlayer = gameState.getCurrentPlayerId();
        int row           = gameState.dropToken(column, currentPlayer);

        if (row != -1) {
            getCircleFromCell(column, row).ifPresent(token -> {
                // Animate the token dropping
                tokenAnimator.animate(token, currentPlayer, animationFinished);
            });
        }
    }

    private Optional<Circle> getCircleFromCell(int col, int row) {
        return Optional.ofNullable((StackPane) getNodeFromGridPane(col, row))
                       .map(stackPane -> (Circle) stackPane.getChildren()
                                                           .get(1));
    }

    private Node getNodeFromGridPane(int col, int row) {
        return gameBoard.getChildren()
                        .stream()
                        .filter(node -> col == GridPane.getColumnIndex(node) && row == GridPane.getRowIndex(node))
                        .findFirst()
                        .orElse(null);
    }
}