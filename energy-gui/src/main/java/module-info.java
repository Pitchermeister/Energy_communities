module at.uastw.energygui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    opens at.uastw.energygui to javafx.fxml;
    exports at.uastw.energygui;
}
