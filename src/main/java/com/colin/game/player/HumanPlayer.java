package com.colin.game.player;

import com.colin.game.state.GameState;

import java.util.concurrent.CompletableFuture;

/**
 * HumanPlayer for the Connect 4 game.
 *
 * <p>
 * This class represents a human player in the Connect 4 game.
 * </p>
 *
 * @version 1.1
 * @author Colin Jokisch
 */
public class HumanPlayer implements Player {
    private volatile int selectedColumn;

    public void setSelectedColumn(int selectedColumn) {
        this.selectedColumn = selectedColumn;
    }

    @Override
    public int makeMove(GameState gameState) {
        return selectedColumn;
    }
}


