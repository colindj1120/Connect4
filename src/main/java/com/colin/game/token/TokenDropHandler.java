package com.colin.game.token;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface TokenDropHandler {
    void handle(int column, CompletableFuture<Void> animationFinished);
}

