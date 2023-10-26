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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
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
    private static final Executor executor = Runnable::run;


    @FXML
    private GridPane gameBoard;

    @FXML
    private GridPane buttonGrid;

    private final List<Button> columnButtons = new ArrayList<>();

    private GameState gameState;
    private GameUI    gameUI;
    private Player    player1;
    private Player    player2;
    private CompletableFuture<Integer> currentMoveFuture;

    @FXML
    public void initialize() {
        // Initialization logic remains the same
        currentMoveFuture = new CompletableFuture<>();
        initializeButtonGrid(currentMoveFuture);
        proceedWithGame(currentMoveFuture);
    }

    private void proceedWithGame(CompletableFuture<Integer> moveFuture) {
        if (!gameState.isGameOver()) {
            int currentPlayerId = gameState.getCurrentPlayerId();
            Player currentPlayer = (currentPlayerId == 1) ? player1 : player2;

            if (currentPlayer instanceof AIPlayer) {
                currentPlayer.makeMove(gameState);
            }

            moveFuture.thenRunAsync(() -> {
                gameState.switchPlayer();
                this.currentMoveFuture = new CompletableFuture<>();
                proceedWithGame(this.currentMoveFuture);
            }, Runnable::run);
        } else {
            LOGGER.info("GAME OVER");
        }
    }

    private void initializeButtonGrid(CompletableFuture<Integer> initialMoveFuture) {
        IntStream.range(0, gameState.getNumCols())
                 .forEach(col -> {
                     Button button = ButtonFactory.createDropButton(col, initialMoveFuture, gameUI::handleTokenDrop);
                     columnButtons.add(button);
                     buttonGrid.add(button, col, 0);
                     CompletableFutureUtility.futureFromButtonPress(
                             button,
                             event -> gameUI.handleTokenDrop(col, this.currentMoveFuture),
                             Runnable::run
                     );
                 });
    }

