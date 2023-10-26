package com.colin.game.token;

import javafx.scene.shape.Circle;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface TokenAnimator {
    /**
     * Animate the dropping of a token into a slot.
     *
     * @param token The Circle object representing the token.
     * @param playerId The ID of the player who owns this token.
     */
    void animate(Circle token, int playerId, CompletableFuture<Void> animationFished);
}

