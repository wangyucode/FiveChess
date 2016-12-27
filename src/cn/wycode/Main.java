package cn.wycode;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application implements EventHandler<WindowEvent> {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("五子棋");
        primaryStage.setScene(new Scene(root, 740, 520));
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(this);
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void handle(WindowEvent event) {
        try {
            NetService.getInstance(Controller.netType).close();
        } catch (Exception e) {
            //ignore this
        }
        System.exit(0);

    }
}