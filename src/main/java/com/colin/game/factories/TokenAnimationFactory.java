package com.colin.game.factories;

import com.colin.game.token.TokenAnimator;
import javafx.animation.TranslateTransition;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class TokenAnimationFactory {

    /**
     * Creates a token drop animator.
     *
     * @return A TokenAnimator that animates the dropping of a token.
     */
    public static TokenAnimator createTokenDropAnimator() {
        return (token, playerId, animationFinished) -> {
            TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5), token);
            animation.setFromY(-300);
            animation.setToY(0);
            animation.setOnFinished(e -> {
                System.out.println("ANIMATION FINISHED");
                animationFinished.complete(null);
            });
            if (playerId == 1) {
                token.setFill(Color.RED);
            } else if (playerId == 2) {
                token.setFill(Color.YELLOW);
            }
            animation.play();
        };
    }
}

