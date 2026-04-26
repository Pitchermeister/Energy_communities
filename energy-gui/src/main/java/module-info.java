module at.uastw.energygui {
    requires javafx.controls;
    requires javafx.fxml;

    opens at.uastw.energygui to javafx.fxml;
    exports at.uastw.energygui;
}
