package com.colin.game;

import com.colin.game.factories.PlayerFactory;
import com.colin.game.player.AIPlayer;
import com.colin.game.player.HumanPlayer;
import com.colin.game.player.Player;
import com.colin.game.state.GameState;
import com.colin.game.state.GameUI;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Connect 4 game.
 *
 * @author Colin Jokisch
 * @version 1.8
 */
public class Connect4Controller {
    private static final Logger LOGGER = Logger.getLogger(Connect4Controller.class.getName());

    @FXML
    private GridPane gameBoard;

    @FXML
    private GridPane buttonGrid;

    private GameState gameState;
    private GameUI    gameUI;
    private Player    player1;
    private Player    player2;

    @FXML
    public void initialize() {
        gameState = new GameState();
        gameUI    = new GameUI();
        player1   = PlayerFactory.createHumanPlayer();
        player2   = PlayerFactory.createAIPlayer(gameState.isColumnAvailable, gameState::getBoard, 2);

        Supplier<CompletableFuture<Integer>> humanMoveFutureSupplier = CompletableFuture::new;
        gameUI.initializeUI(gameState, gameBoard, buttonGrid);
        proceedWithGame(humanMoveFutureSupplier);
    }

    private void proceedWithGame(Supplier<CompletableFuture<Integer>> futureSupplier) {
        if (!gameState.isGameOver()) {
            CompletableFuture<Integer> moveFuture = futureSupplier.get();

            if (gameState.getCurrentPlayerId() == 1) { // Assuming 1 is the ID for the human player
                gameUI.updateCompletableFutureForButtons(moveFuture);
            }

            int currentPlayerId = gameState.getCurrentPlayerId();
            Player currentPlayer = (currentPlayerId == 1) ? player1 : player2;
            if (currentPlayer instanceof HumanPlayer) {
                moveFuture.thenRun(() -> proceedWithGame(futureSupplier));
            } else if (currentPlayer instanceof AIPlayer) {
                int col = currentPlayer.makeMove(gameState);
                CompletableFuture<Void> animationFinished = new CompletableFuture<>();
                gameUI.handleTokenDrop(col, animationFinished);
                animationFinished.thenRun(() -> proceedWithGame(futureSupplier));
            }
        }
    }
}

