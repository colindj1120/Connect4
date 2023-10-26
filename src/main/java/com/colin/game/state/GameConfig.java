package com.colin.game.state;

import com.colin.game.algorithms.enums.DifficultyLevel;
import com.colin.game.algorithms.enums.PlayStyle;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton class to hold game configuration.
 *
 * @author Colin Jokisch
 * @version 1.0
 */
public class GameConfig {
    private static GameConfig instance;
    private static final Object lock = new Object();
    private final int numRows;
    private final int numCols;
    private final PlayStyle aiPlayStyle; // Add AI playstyle field
    private final DifficultyLevel difficultyLevel;

    private static final int DEFAULT_NUM_ROWS = 6;
    private static final int DEFAULT_NUM_COLS = 7;
    private static final PlayStyle DEFAULT_AI_PLAY_STYLE = PlayStyle.CENTER_CONTROL; // Set a default AI playstyle
    private static final DifficultyLevel DEFAULT_DIFFICULTLY_LEVEL = DifficultyLevel.MEDIUM;

    private GameConfig() {
        // Load properties from a file
        Properties properties = new Properties();
        try (InputStream in = GameConfig.class.getResourceAsStream("/game.properties")) {
            properties.load(in);
        } catch (IOException e) {
            // Log or handle error more robustly if necessary
            e.printStackTrace();
        }

        // Initialize fields
        this.numRows = Integer.parseInt(properties.getProperty("numRows", String.valueOf(DEFAULT_NUM_ROWS)));
        this.numCols = Integer.parseInt(properties.getProperty("numCols", String.valueOf(DEFAULT_NUM_COLS)));
        this.aiPlayStyle = PlayStyle.valueOf(properties.getProperty("aiPlayStyle", DEFAULT_AI_PLAY_STYLE.name()));
        this.difficultyLevel = DifficultyLevel.valueOf(properties.getProperty("difficultyLevel", DEFAULT_DIFFICULTLY_LEVEL.name()));

    }

    /**
     * Returns the singleton instance of GameConfig.
     *
     * @return The singleton instance.
     */
    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }

    /**
     * Gets the number of rows in the game board.
     *
     * @return The number of rows.
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * Gets the number of columns in the game board.
     *
     * @return The number of columns.
     */
    public int getNumCols() {
        return numCols;
    }

    /**
     * Gets the AI playstyle for the game.
     *
     * @return The AI playstyle.
     */
    public PlayStyle getAiPlayStyle() {
        return aiPlayStyle;
    }

    /**
     * Gets the AI difficulty level for the game.
     *
     * @return The AI difficulty level.
     */
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }
}
