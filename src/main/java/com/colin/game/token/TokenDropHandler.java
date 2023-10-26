package com.colin.game.token;

@FunctionalInterface
public interface TokenDropHandler {
    void handle(int column);
}

