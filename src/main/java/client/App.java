package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import server.Board;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import server.Player;
import server.SeaBattleInterface;


public class App extends Application implements SeaBattleClientInterface {

    private SeaBattleInterface game;
    private SeaBattleController controller;  // Ссылка на контроллер

    @Override
    public void start(Stage primaryStage) {
        try {
            // Подключаемся к серверу RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            game = (SeaBattleInterface) registry.lookup("SeaBattleGame");

            // Создаем объект для клиента с методом обновления
            SeaBattleClientInterface client = (SeaBattleClientInterface) UnicastRemoteObject.exportObject(this, 0);

            // Регистрируем клиента на сервере
            game.registerClient(client);

            // Загружаем FXML файл для интерфейса игры
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game.fxml"));
            Parent root = loader.load();  // Загружаем родительский элемент
            controller = loader.getController();  // Сохраняем ссылку на контроллер
            controller.setGame(game);  // Передаем объект игры в контроллер
            controller.setClient(client);  // Передаем объект клиента в контроллер

            // Создаем сцену и отображаем ее
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Морской бой");
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void updatePlayerBoard(Player player) throws RemoteException {

        if (controller != null) {
            System.out.println("Ваша доска обновлена.");
            controller.updateBoard(player);  // Передаем Board вместо Player
        }
    }


    @Override
    public void updateMessage(String message) throws RemoteException {
        System.out.println("Сообщение от сервера: " + message);


    }
}
