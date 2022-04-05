module com.github.martonr.picalc.gui {
    requires com.github.martonr.picalc.engine;
    requires javafx.controls;
    requires javafx.fxml;

    opens com.github.martonr.picalc.gui to javafx.fxml, javafx.graphics;
    opens com.github.martonr.picalc.gui.controller to javafx.fxml, javafx.graphics;
}
