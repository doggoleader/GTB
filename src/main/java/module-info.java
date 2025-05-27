module org.doggo.bbgtb {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.doggo.bbgtb to javafx.fxml;
    exports org.doggo.bbgtb;
}