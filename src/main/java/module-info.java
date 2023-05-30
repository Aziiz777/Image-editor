module com.example.multimediaproject {
    requires javafx.controls;
    requires java.desktop;


    opens com.example.multimediaproject to javafx.fxml;
    exports com.example.multimediaproject;
}