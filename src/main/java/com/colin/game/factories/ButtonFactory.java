package com.colin.game.factories;

import javafx.scene.control.Button;

import java.util.function.Consumer;

public class ButtonFactory {
    public static Button createDropButton(int column, Consumer<Button> handler) {
        Button button = new Button("Drop");
        handler.accept(button);
        return button;
    }
}

