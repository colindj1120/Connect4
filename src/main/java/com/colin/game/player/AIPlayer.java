package com.colin.game.player;

import com.colin.game.algorithms.algorithm.*;
import com.colin.game.algorithms.enums.DifficultyLevel;
import com.colin.game.state.GameState;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AIPlayer implements Player {
    private final Algorithm algorithm;

    public AIPlayer(DifficultyLevel difficultyLevel, Predicate<Integer> isColumnAvailable, Supplier<int[][]> getBoardState, int aiPlayerNum) {
        switch (difficultyLevel) {
            case EASY:
                this.algorithm = new EasyAlgorithm(isColumnAvailable, getBoardState, aiPlayerNum);
                break;
            case MEDIUM:
                this.algorithm = new MediumAlgorithm(isColumnAvailable, getBoardState, aiPlayerNum, 3 - aiPlayerNum);
                break;
            case HARD:
                this.algorithm = new HardAlgorithm(isColumnAvailable, getBoardState, aiPlayerNum, 3 - aiPlayerNum);
                break;
            case EXPERT:
                this.algorithm = new ExpertAlgorithm(isColumnAvailable, getBoardState, aiPlayerNum, 3 - aiPlayerNum);
                break;
            case MASTER:
                this.algorithm = new MasterAlgorithm(isColumnAvailable, getBoardState);
                break;
            default:
                throw new IllegalArgumentException("Invalid difficulty level");
        }
    }

    @Override
    public int makeMove(GameState gameState) {
        return algorithm.makeMove();
    }
}


