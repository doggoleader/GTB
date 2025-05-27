module org.doggo.bbgtb {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens org.doggo.bbgtb to javafx.fxml;
    opens org.doggo.bbgtb.updates to com.google.gson;
    exports org.doggo.bbgtb;
}