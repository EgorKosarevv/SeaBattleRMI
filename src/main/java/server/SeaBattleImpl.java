package server;

import client.SeaBattleClientInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.ArrayList;

public class SeaBattleImpl extends UnicastRemoteObject implements SeaBattleInterface {

    private Game game;
    private Player player1;
    private Player player2;


    private List<SeaBattleClientInterface> clients;


    public SeaBattleImpl() throws RemoteException {
        super();
        clients = new ArrayList<>();

    }


    public void registerClient(SeaBattleClientInterface client) throws RemoteException {
        clients.add(client);
    }

    @Override
    public boolean registerPlayer(Player player) throws RemoteException {
        if (player1 == null) {
            player1 = player;
            System.out.println("Игрок 1 зарегистрирован: " + player.getName());
            return true;  // Регистрация успешна
        } else if (player2 == null) {
            player2 = player;
            System.out.println("Игрок 2 зарегистрирован: " + player.getName());
            return true;  // Регистрация успешна
        } else {
            System.out.println("Все игроки уже зарегистрированы.");
            return false;  // Регистрация не удалась
        }
    }



    @Override
    public Player getPlayer1() throws RemoteException {
        return player1;
    }

    @Override
    public Player getPlayer2() throws RemoteException {
        return player2;
    }

    @Override
    public void startGame() throws RemoteException {
        if (player1 != null && player2 != null) {
            this.game = new Game(player1, player2);
            System.out.println("Игра началась!");



        } else {
            throw new RemoteException("Необходимо зарегистрировать двух игроков.");
        }
    }



    public Player getCurrentPlayer() throws RemoteException {

        return game.getCurrentPlayer();

    }

    @Override
    public boolean isFirstPlayer(Player player) throws RemoteException {
        return player.equals(player1);
    }


    @Override
    public void makeMove(int x, int y) throws RemoteException {

        if (game.isGameOver()) {
            throw new RemoteException("Игра завершена.");
        }
        System.out.println("Сервер: первый игрок - " + game.getPlayer1());
        System.out.println("Сервер: второй игрок - " + game.getPlayer2());
        System.out.println("Сервер: текущий игрок до хода - " + game.getCurrentPlayer());


        game.makeMove(x, y); // Выполнение хода


        // Отправляем обновления всем клиентам
        for (int i = 0; i < clients.size(); i++) {
            SeaBattleClientInterface client = clients.get(i);

            // Обновляем доски для каждого игрока
            if (i == 0) { // Первый клиент - Player 1
                client.updatePlayerBoard(game.getPlayer1());
            } else { // Второй клиент - Player 2
                client.updatePlayerBoard(game.getPlayer2());
            }


            client.updateMessage("Ход игрока: " + game.getCurrentPlayer().getName());
        }
    }



    @Override
    public Board   getOpponentBoardForPlayer(Player requestingPlayer) throws RemoteException {
        System.out.println("Вызов getOpponentBoardForPlayer для игрока: " + requestingPlayer.getName());

        Board opponentBoard;
        if (requestingPlayer.equals(game.getPlayer1())) {
            System.out.println("Возвращаем доску игрока 2 (" + game.getPlayer2().getName() + ")");
            opponentBoard = game.getPlayer2().getBoard(); // Для игрока 1 возвращаем поле игрока 2
        } else if (requestingPlayer.equals(game.getPlayer2())) {
            System.out.println("Возвращаем доску игрока 1 (" + game.getPlayer1().getName() + ")");
            opponentBoard = game.getPlayer1().getBoard(); // Для игрока 2 возвращаем поле игрока 1
        } else {
            System.out.println("Ошибка: запрашивающий игрок не найден в текущей игре");
            throw new IllegalArgumentException("Запрашивающий игрок не найден в текущей игре");
        }

        logBoardState(opponentBoard);
        return opponentBoard;
    }

    // Метод для логирования состояния доски
    private void logBoardState(Board board) {
        System.out.println("Состояние доски противника:");
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                CellState state = board.getCell(row, col).getState();
                System.out.print(state + " ");
            }
            System.out.println();
        }
    }



    @Override
    public boolean placeShip(List<Coordinate> coordinates, int size, Player player) throws RemoteException {
        if (game == null) {
            throw new RemoteException("Game object is not initialized.");
        }


        if (player.equals(game.getPlayer1())) {
            return game.getPlayer1().placeShip(coordinates, size, player);
        } else if (player.equals(game.getPlayer2())) {
            return game.getPlayer2().placeShip(coordinates, size, player);
        } else {
            throw new RemoteException("Игрок не найден в текущей игре.");
        }
    }

    @Override
    public boolean isGameOver() throws RemoteException {
        return game.isGameOver();
    }

    @Override
    public Player getWinner() throws RemoteException {
        if (game.isGameOver()) {
            return game.getCurrentPlayer();
        }
        return null;
    }
    public Board getPlayerBoard(Player player) throws RemoteException {
        System.out.println("Запрос на получение доски для игрока: " + player.getName());

        if (player1 == null || player2 == null) {
            System.out.println("Игроки не инициализированы. player1: " + player1 + ", player2: " + player2);
        }

        System.out.println("Сравниваем игрока с player1: " + (player.equals(player1)));
        System.out.println("Сравниваем игрока с player2: " + (player.equals(player2)));

        if (player.equals(player1)) {
            System.out.println("Возвращаем доску для первого игрока: " + player1.getName());
            return player1.getBoard();
        } else if (player.equals(player2)) {
            System.out.println("Возвращаем доску для второго игрока: " + player2.getName());
            return player2.getBoard();
        } else {
            System.out.println("Ошибка: Игрок не найден.");
            throw new RemoteException("Игрок не найден");
        }
    }

    @Override
    public boolean isCellOccupied(int row, int col, Player player) throws RemoteException {
        System.out.println("Проверка клетки: (" + row + ", " + col + ") для игрока: " + player.getName());

        // Получаем доску игрока
        Board playerBoard = getPlayerBoard(player);

        // Получаем состояние клетки на доске
        Cell cell = playerBoard.getCell(row, col);
        boolean isOccupied = cell.getState() != CellState.EMPTY;  // Сравниваем состояние клетки с EMPTY
        System.out.println("Клетка (" + row + ", " + col + ") для игрока " + player.getName() + " занята: " + isOccupied);

        return isOccupied;
    }


}
