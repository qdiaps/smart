package io.github.qdiaps;

import atlantafx.base.theme.NordLight;
import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.infrastructure.config.Config;
import io.github.qdiaps.infrastructure.event.EventBusImpl;
import io.github.qdiaps.infrastructure.logging.EventLogger;
import io.github.qdiaps.infrastructure.serial.Serial;
import io.github.qdiaps.service.SerialConnectionService;
import io.github.qdiaps.service.SerialSendService;
import io.github.qdiaps.ui.Notifier;
import io.github.qdiaps.ui.controller.ChartController;
import io.github.qdiaps.ui.controller.MainController;
import io.github.qdiaps.ui.controller.RightPanelController;
import io.github.qdiaps.ui.controller.TopPanelController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Setup dependencies
        EventBus eventBus = new EventBusImpl();
        new EventLogger(eventBus);
        var serial = new Serial(eventBus);
        var notifier = new Notifier();

        var serialConnectionService = new SerialConnectionService(serial);
        var serialSendService = new SerialSendService(serial);


        // Setup fxml
        var loader = new FXMLLoader(getClass().getResource("main.fxml"));


        // Setup controllers
        loader.setControllerFactory(type -> {
            if (type == TopPanelController.class)
                return new TopPanelController(serialConnectionService, eventBus, notifier);
            else if (type == ChartController.class)
                return new ChartController();
            else if (type == RightPanelController.class)
                return new RightPanelController();
            else if (type == MainController.class)
                return new MainController(notifier);

            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        // Setup GUI
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        stage.setTitle(Config.PROGRAM_NAME);
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();


        // Configure shutdown
        stage.setOnCloseRequest(event -> {
            serialConnectionService.disconnect();
        });

        log.info("Program {} v{} started", Config.PROGRAM_NAME, Config.getVersion());
    }

    @Override
    public void stop() {
        log.info("Shutdown");
    }
}
