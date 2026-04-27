module at.uastw.energygui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;

    opens at.uastw.energygui to javafx.fxml;
    exports at.uastw.energygui;
}
