package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class SeaBattleServer {
    public static void main(String[] args) {
        try {
            // Создаем и запускаем RMI реестр
            Registry registry = LocateRegistry.createRegistry(1099);

            // Создаем и регистрируем игру
            SeaBattleInterface game = new SeaBattleImpl();
            Naming.rebind("rmi://localhost/SeaBattleGame", game);

            System.out.println("Сервер игры 'Морской бой' запущен.");
        } catch (Exception e) {
            System.out.println("Ошибка сервера: " + e);
        }
    }
}
