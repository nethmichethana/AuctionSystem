module lk.java.auctionsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires javafx.graphics;

    opens lk.java.auctionsystem to javafx.fxml;
    exports lk.java.auctionsystem;
}