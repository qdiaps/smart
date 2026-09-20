package io.github.qdiaps;

import atlantafx.base.theme.NordLight;
import io.github.qdiaps.domain.command.Command;
import io.github.qdiaps.domain.command.DataCommand;
import io.github.qdiaps.domain.event.EventBus;
import io.github.qdiaps.domain.model.DataModel;
import io.github.qdiaps.domain.model.ExperimentSession;
import io.github.qdiaps.domain.service.TimeProvider;
import io.github.qdiaps.infrastructure.config.Config;
import io.github.qdiaps.infrastructure.event.EventBusImpl;
import io.github.qdiaps.infrastructure.logging.EventLogger;
import io.github.qdiaps.infrastructure.serial.Serial;
import io.github.qdiaps.infrastructure.serial.SerialEvent;
import io.github.qdiaps.infrastructure.time.SystemTimeProvider;
import io.github.qdiaps.service.*;
import io.github.qdiaps.ui.Notifier;
import io.github.qdiaps.ui.builder.ChartBuilder;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Setup dependencies
        TimeProvider timeProvider = new SystemTimeProvider();
        var experimentSession = new ExperimentSession(timeProvider);
        var dataModel = new DataModel();

        Map<String, Command> commandRegistry = new HashMap<>();
        commandRegistry.put("data", new DataCommand(dataModel, experimentSession));

        List<String> ackCommandRegistry = new ArrayList<>();
        ackCommandRegistry.add("pong");
        ackCommandRegistry.add("pid:ack");

        EventBus eventBus = new EventBusImpl();
        new EventLogger(eventBus);
        var serial = new Serial(eventBus);
        var notifier = new Notifier();
        var chartBuilder = new ChartBuilder(Config.CHART_TITLE);

        var serialSendService = new SerialSendService(serial);
        var reliableSerialService = new ReliableSerialService(serialSendService, eventBus);
        var messageRouteService = new MessageRouteService(eventBus, reliableSerialService, commandRegistry, ackCommandRegistry);
        var serialConnectionService = new SerialConnectionService(serial);
        var experimentControlService = new ExperimentControlService(experimentSession);


        // Setup fxml
        var loader = new FXMLLoader(getClass().getResource("main.fxml"));


        // Setup controllers
        loader.setControllerFactory(type -> {
            if (type == TopPanelController.class)
                return new TopPanelController(serialConnectionService, eventBus, notifier);
            else if (type == ChartController.class)
                return new ChartController(chartBuilder, eventBus);
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

        // Test DataCommand
        // TODO: remove
        eventBus.post(new SerialEvent.DataReceived("data:10.0"));
        eventBus.post(new SerialEvent.DataReceived("data:1t"));
        eventBus.post(new SerialEvent.DataReceived("data:10.0;20.0;30.0"));
        eventBus.post(new SerialEvent.DataReceived("data:10.0;200.0;30.0"));
    }

    @Override
    public void stop() {
        log.info("Shutdown");
    }
}
