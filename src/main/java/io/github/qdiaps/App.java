package io.github.qdiaps;

import atlantafx.base.theme.NordLight;
import io.github.qdiaps.ui.controller.ChartController;
import io.github.qdiaps.ui.controller.RightPanelController;
import io.github.qdiaps.ui.controller.TopPanelController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {
    private final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Setup fxml
        var loader = new FXMLLoader(getClass().getResource("main.fxml"));

        // Setup controllers
        loader.setControllerFactory(type -> {
            if (type == TopPanelController.class)
                return new TopPanelController();
            else if (type == ChartController.class)
                return new ChartController();
            else if (type == RightPanelController.class)
                return new RightPanelController();

            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Setup GUI
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        stage.setTitle("SMART");
        stage.setMinHeight(610.0D);
        stage.setMinWidth(900.0D);

        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();

        logger.info("Started");
    }

    @Override
    public void stop() {
        logger.info("Shutdown");
    }
}
