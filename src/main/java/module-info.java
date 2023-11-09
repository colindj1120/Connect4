module com.colin.game{
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens com.colin.game to javafx.fxml;
    exports com.colin.game;
    exports com.colin.game.token;
    opens com.colin.game.token to javafx.fxml;
    exports com.colin.game.player;
    opens com.colin.game.player to javafx.fxml;
    exports com.colin.game.factories;
    opens com.colin.game.factories to javafx.fxml;
    exports com.colin.game.state;
    opens com.colin.game.state to javafx.fxml;
    exports com.colin.game.gameover;
    opens com.colin.game.gameover to javafx.fxml;

    exports com.colin.game.algorithms.enums;
    exports com.colin.game.enums;
}