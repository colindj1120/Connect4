package com.colin.game.player;

import com.colin.game.state.GameState;

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
    void makeMove(GameState gameState);
}

