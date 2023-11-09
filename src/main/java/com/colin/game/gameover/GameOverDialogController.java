package com.colin.game.gameover;

import com.colin.game.FireworksUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Controller for the GameOver dialog. Handles the fireworks animation and outcome label.
 *
 * @author Colin Jokisch
 * @version 1.1
 */
public class GameOverDialogController {
    private static final Logger logger             = Logger.getLogger(GameOverDialogController.class.getName());
    private static final int    FIREWORK_MIN_COUNT = 1;
    private static final int    FIREWORK_MAX_COUNT = 4;
    private static final Random random             = new Random();

    @FXML
    private Label outcomeLabel;

    @FXML
    private Pane fireworksPane;

    private FireworksUtil fireworksUtil;

    public void initialize() {
        logger.info("Initialization logic");

        // Randomly generate between 1 and 3 fireworks
        Supplier<Integer> fireworkCountSupplier = () -> 1 + random.nextInt(4);

        // Randomly generate a firework size between 2.0 and 5.0
        Supplier<Double> fireworkSizeSupplier = () -> 2.0 + random.nextDouble() * 5.0;

        // Randomly generate a fade duration between 0.3 and 0.7 seconds
        Supplier<Double> fadeDurationSupplier = () -> 0.3 + random.nextDouble() * 1;

        // Randomly generate a particle distance between 50.0 and 100.0
        Supplier<Double> particleDistanceSupplier = () -> 50.0 + random.nextDouble() * 50.0;

        // Randomly generate a gravity value between 0.1 and 0.5
        Supplier<Double> gravitySupplier = () -> 0.1 + random.nextDouble() * 0.2;

        // Randomly generate a particle lifespan between 1.0 and 3.0 seconds
        Supplier<Double> particleLifespanSupplier = () -> 1.0 + random.nextDouble() * 2.0;

        // Sets the minimum launch angle to -45 degrees
        Supplier<Double> angleMinSupplier = () -> 25.0;

        // Sets the maximum launch angle to 45 degrees
        Supplier<Double> angleMaxSupplier = () -> 120.0;

        Supplier<Double> noiseFactorSupplier = () -> .2;
        Supplier<Double> secondaryExplosionChanceSupplier = () -> .4;

        fireworksUtil = new FireworksUtil(fireworksPane, fireworkCountSupplier, fireworkSizeSupplier, fadeDurationSupplier, particleDistanceSupplier, gravitySupplier,
                                                        particleLifespanSupplier, angleMinSupplier, angleMaxSupplier, noiseFactorSupplier, secondaryExplosionChanceSupplier);
    }

    public void setOutcome(String outcome) {
        logger.info("Setting outcome: " + outcome);
        outcomeLabel.setText(outcome);
    }

    public void startFireworks() {
        logger.info("Starting fireworks");
        fireworksUtil.startFireworks();
    }
}
