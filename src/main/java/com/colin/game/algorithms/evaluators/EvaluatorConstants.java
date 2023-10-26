package com.colin.game.algorithms.evaluators;

import com.colin.game.state.GameConfig;

public class EvaluatorConstants {
    public static final int NUM_COLUMNS = GameConfig.getInstance()
                                                .getNumCols();
    public static final int NUM_ROWS = GameConfig.getInstance()
                                                .getNumRows();

    public static final int AI_WIN_SCORE       = 1000;
    public static final int OPPONENT_WIN_SCORE = -1000;
    public static final int THREE_IN_A_ROW     = 100;
    public static final int TWO_IN_A_ROW       = 10;
    public static final int CONSECUTIVE_COUNT  = 4;
}
