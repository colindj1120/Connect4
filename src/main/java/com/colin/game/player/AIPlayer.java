package com.colin.game.player;

import com.colin.game.algorithms.algorithm.*;
import com.colin.game.algorithms.enums.DifficultyLevel;
import com.colin.game.state.GameState;
import javafx.scene.control.Button;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AIPlayer implements Player {
    private final Algorithm algorithm;
    private final List<Button> columnButtons;

    public AIPlayer(DifficultyLevel difficultyLevel, Predicate<Integer> isColumnAvailable, Supplier<int[][]> getBoardState, int aiPlayerNum, List<Button> columnButtons) {
        switch (difficultyLevel) {
            case EASY -> this.algorithm = new EasyAlgorithm(isColumnAvailable, getBoardState, aiPlayerNum);
            case MEDIUM -> this.algorithm = new MediumAlgorithm(isColumnAvailable, getBoardState, aiPlayerNum, 3 - aiPlayerNum);
            case HARD -> this.algorithm = new HardAlgorithm(isColumnAvailable, getBoardState, aiPlayerNum, 3 - aiPlayerNum);
            case EXPERT -> this.algorithm = new ExpertAlgorithm(isColumnAvailable, getBoardState, aiPlayerNum, 3 - aiPlayerNum);
            case MASTER -> this.algorithm = new MasterAlgorithm(isColumnAvailable, getBoardState);
            default -> throw new IllegalArgumentException("Invalid difficulty level");
        }

        this.columnButtons = columnButtons;
    }

    @Override
    public void makeMove(GameState gameState) {
        int col = algorithm.makeMove();
        columnButtons.get(col).fire();
    }
}


