package com.colin.game.factories;

import com.colin.game.algorithms.enums.DifficultyLevel;
import com.colin.game.player.AIPlayer;
import com.colin.game.player.HumanPlayer;
import com.colin.game.player.Player;
import com.colin.game.state.GameConfig;
import javafx.scene.control.Button;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PlayerFactory {
    public static Player createHumanPlayer() {
        return new HumanPlayer();
    }

    public static Player createAIPlayer(Predicate<Integer> isColumnAvailable, Supplier<int[][]> getBoardState, int aiPlayerNum, List<Button> columnButtons) {
        DifficultyLevel difficultyLevel = GameConfig.getInstance()
                                                    .getDifficultyLevel();
        return new AIPlayer(difficultyLevel, isColumnAvailable, getBoardState, aiPlayerNum, columnButtons);
    }
}

