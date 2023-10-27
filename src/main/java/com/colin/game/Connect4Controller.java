package com.colin.game;

import com.colin.game.factories.ButtonFactory;
import com.colin.game.factories.PlayerFactory;
import com.colin.game.player.AIPlayer;
import com.colin.game.player.Player;
import com.colin.game.state.GameState;
import com.colin.game.state.GameUI;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Controller for the Connect 4 game.
 *
 * @author Colin Jokisch
 * @version 1.8
 */
public class Connect4Controller {
    private static final Logger   LOGGER   = Logger.getLogger(Connect4Controller.class.getName());

    @FXML
    private GridPane gameBoard;

    @FXML
    private HBox buttons;

    private final List<Button> columnButtons = new ArrayList<>();

    private GameState                  gameState;
    private GameUI                     gameUI;
    private Player                     player1;
    private Player                     player2;
    private CompletableFuture<Integer> moveFuture;
    private CompletableFuture<Void>    animationFinished;

    @FXML
    public void initialize() {
        // Initialization logic remains the same
        gameState         = new GameState();
        gameUI            = new GameUI();
        moveFuture        = new CompletableFuture<>();
        animationFinished = new CompletableFuture<>();
        initializeButtonGrid();
        player1 = PlayerFactory.createHumanPlayer();
        player2 = PlayerFactory.createAIPlayer(gameState.isColumnAvailable, gameState::getBoard, 2, columnButtons);
        gameUI.initializeUI(gameState, gameBoard);
        proceedWithGame();
    }

    private void proceedWithGame() {
        if (!gameState.isGameOver()) {
            int    currentPlayerId = gameState.getCurrentPlayerId();
            Player currentPlayer   = (currentPlayerId == 1) ? player1 : player2;

            if (currentPlayer instanceof AIPlayer) {
                currentPlayer.makeMove(gameState);
            }

            moveFuture.thenRun(() -> {
                gameState.switchPlayer();
                moveFuture        = new CompletableFuture<>();
                animationFinished = new CompletableFuture<>();
                proceedWithGame();
            });
        } else {
            if(gameState.isStalemate()) {
                LOGGER.info("Game ended in stalemate");
            } else {
                int currentPlayerId = gameState.getCurrentPlayerId();
                LOGGER.info("Player " + currentPlayerId + " Won the game. Game is Over");
            }
        }
    }

    private void initializeButtonGrid() {
        IntStream.range(0, gameState.getNumCols())
                 .forEach(col -> {
                     Button button = ButtonFactory.createDropButton(col, animationFinished, gameUI::handleTokenDrop);
                     columnButtons.add(button);
                     buttons.getChildren().add(button);
                     button.setOnAction(event -> {
                         columnButtons.forEach(btn -> btn.setDisable(true));
                         gameUI.handleTokenDrop(col, animationFinished);
                         animationFinished.thenRun(() -> {
                             columnButtons.forEach(btn -> btn.setDisable(false));
                             moveFuture.complete(col);
                         });
                     });
                 });
    }
}

