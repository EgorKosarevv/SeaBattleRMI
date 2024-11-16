module SeaBattle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;

    exports client;
    exports server;
    opens client to javafx.fxml;
    opens server to javafx.fxml;
}
