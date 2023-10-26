package com.colin.game.player;

import com.colin.game.state.GameState;

import java.util.concurrent.CompletableFuture;

/**
 * Player interface for the Connect 4 game.
 *
 * <p>
 * This interface defines the contract for a player in the Connect 4 game.
 * </p>
 *
 * @version 1.1
 * @author Colin Jokisch
 */
public interface Player {
    int makeMove(GameState gameState);
}

