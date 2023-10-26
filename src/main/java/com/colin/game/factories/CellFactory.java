package com.colin.game.factories;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class CellFactory {
    public static StackPane createCell(int col, int row) {
        Rectangle slot = new Rectangle(50, 50);
        slot.getStyleClass().add("slot");
        Circle token = new Circle(20);
        token.getStyleClass().add("empty-token");
        Label label = new Label(String.format("%d,%d", col, row));
        return new StackPane(slot, token, label);
    }
}

