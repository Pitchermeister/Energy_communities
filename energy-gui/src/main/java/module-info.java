module at.uastw.energygui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens at.uastw.energygui to javafx.fxml;
    exports at.uastw.energygui;
}
