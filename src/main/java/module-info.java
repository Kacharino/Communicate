module kacharino.communicate {
    requires javafx.controls;
    requires javafx.fxml;


    opens kacharino.communicate to javafx.fxml;
    exports kacharino.communicate;
}