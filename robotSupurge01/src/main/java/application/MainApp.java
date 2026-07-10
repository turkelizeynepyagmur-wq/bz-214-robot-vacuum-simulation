package application;

import controller.SimulationController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.SimulationView;

/**
 * Ana uygulama sınıfı. JavaFX uygulamasını başlatır.
 */
public class MainApp extends Application {


    // start() metodu abstract olmalı ve override etmek zorundadır
    @Override
    public void start(Stage primaryStage) {

            // Controller ve View oluşturup birbiriyle ilişkilendiriyoruz (MVC yapısı)
            SimulationController controller = new SimulationController(20, 14);
            SimulationView view = new SimulationView(20, 14, controller);
            controller.setView(view);
            
            // View'dan ana arayüz görünümünü (BorderPane) alıyoruz
            Scene scene = new Scene(view.getRootView(), 1200, 820);
            controller.initKey(scene);
            
            primaryStage.setTitle("MVC Robot Süpürge Simülasyonu");

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            // stage.show() pencereyi görünür hale getirmek için
            primaryStage.show();
    }


    public static void main(String[] args) {

        launch(args);

    }
}
