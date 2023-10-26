package com.colin.game;

import com.colin.game.factories.ButtonFactory;
import com.colin.game.factories.PlayerFactory;
import com.colin.game.player.AIPlayer;
import com.colin.game.player.Player;
import com.colin.game.state.GameState;
import com.colin.game.state.GameUI;
import com.colin.game.token.TokenDropHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
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
    private static final Logger LOGGER = Logger.getLogger(Connect4Controller.class.getName());

    @FXML
    private GridPane gameBoard;

    @FXML
    private GridPane buttonGrid;

    private final List<Button> columnButtons = new ArrayList<>();

    private GameState gameState;
    private GameUI    gameUI;
    private Player    player1;
    private Player    player2;

    @FXML
    public void initialize() {
        gameState = new GameState();
        gameUI    = new GameUI();
        initializeButtonGrid();

        player1   = PlayerFactory.createHumanPlayer();
        player2   = PlayerFactory.createAIPlayer(gameState.isColumnAvailable, gameState::getBoard, 2, columnButtons);

        Supplier<CompletableFuture<Integer>> humanMoveFutureSupplier = CompletableFuture::new;
        gameUI.initializeUI(gameState, gameBoard);
        proceedWithGame(humanMoveFutureSupplier);
    }

    private void proceedWithGame(Supplier<CompletableFuture<Integer>> futureSupplier) {
        if (!gameState.isGameOver()) {
            CompletableFuture<Integer> moveFuture = futureSupplier.get();
            updateButtons(moveFuture);

            int currentPlayerId = gameState.getCurrentPlayerId();
            Player currentPlayer = (currentPlayerId == 1) ? player1 : player2;

            System.out.println("Current Player ID: " + currentPlayerId);

            if (currentPlayer instanceof AIPlayer) {
                currentPlayer.makeMove(gameState);
            }

            moveFuture.thenRun(() -> {
                System.out.println("Proceeding");
                proceedWithGame(futureSupplier);
            });
        } else {
            System.out.println("GAME OVER");
        }

    }

    private void initializeButtonGrid() {
        IntStream.range(0, gameState.getNumCols())
                 .forEach(col -> {
                     Button button = ButtonFactory.createDropButton(col, new CompletableFuture<>(), gameUI::handleTokenDrop);
                     columnButtons.add(button);
                     buttonGrid.add(button, col, 0);
                 });
    }

    public void updateButtons(CompletableFuture<Integer> newFuture) {
        Consumer<Button> buttonConsumer = createButtonConsumer(newFuture);

        Optional.of(columnButtons)
                .ifPresent(buttons -> buttons.forEach(buttonConsumer));
    }

    private Consumer<Button> createButtonConsumer(CompletableFuture<Integer> newFuture) {
        return button -> {
            int col = GridPane.getColumnIndex(button);
            TokenDropHandler tokenDropHandler = gameUI::handleTokenDrop;
            CompletableFuture<Void> animationFinished = new CompletableFuture<>();
            button.setOnAction(event -> tokenDropHandler.handle(col, animationFinished));
            animationFinished.thenRun(() -> {
                gameState.switchPlayer();
                newFuture.complete(col);
            });
        };
    }
}

