package dev.fxe.recaf4forge.gui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import me.coley.recaf.ui.controls.popup.DragPopup;

public class Notification extends DragPopup {

    private Notification(Node content, Control handle) {
        super(content, handle);
    }

    public static Notification create(String title, String msg) {
        Label lblTitle = new Label(title);
        Label lblMsg = new Label(msg);
        lblTitle.getStyleClass().add("h1");
        GridPane grid = new GridPane();
        GridPane.setHalignment(lblMsg, HPos.CENTER);
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.add(lblMsg, 0, 1, 2, 1);
        return new Notification(grid, lblTitle);
    }
}
