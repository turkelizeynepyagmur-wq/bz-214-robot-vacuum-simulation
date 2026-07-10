module robot.supurge {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens application to javafx.graphics; // MainApp'in olduğu yer

    exports application;
    exports controller;
    exports model;
    exports view;
    exports algorithm;
}