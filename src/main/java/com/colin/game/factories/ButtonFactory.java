package com.colin.game.factories;

import com.colin.game.token.TokenDropHandler;
import javafx.scene.control.Button;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ButtonFactory {
    public static Button createDropButton(int column, CompletableFuture<Void> animationFinished, TokenDropHandler handler) {
        Button button = new Button("Drop");
        button.setOnAction(event -> handler.handle(column, animationFinished));
        return button;
    }
}

