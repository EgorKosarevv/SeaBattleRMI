package client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import server.SeaBattleInterface;
import server.Player;
import server.Coordinate;
import server.Board;
import server.CellState;



public class SeaBattleController {
    @FXML
    private Button placeShipsButton;
    @FXML
    private Button registerButton;
    @FXML
    private TextField playerNameField;  // Для ввода имени игрока
    @FXML
    private Button attackButton;
    private SeaBattleClientInterface client;
    private SeaBattleInterface game;
    private Player player;
    private Player currentPlayer;
    private int currentShipSize = 4; // Начальный размер корабля
    private List<Coordinate> currentShipCoordinates = new ArrayList<>();
    private boolean isPlacingShips = false;
    private boolean isAttacking = false;

    // Количество оставшихся кораблей для расстановки
    private int remainingOneCellShips = 4;
    private int remainingTwoCellShips = 3;
    private int remainingThreeCellShips = 2;
    private int remainingFourCellShips = 1;

    @FXML
    private GridPane player1Board;
    @FXML
    private GridPane player2Board;

    private Button[][] player1BoardButtons = new Button[10][10];
    private Button[][] player2BoardButtons = new Button[10][10];

    // Добавим флаг для определения текущего игрока
    private boolean isFirstPlayer;

    public void setGame(SeaBattleInterface game) {
        this.game = game;
    }
    public void setClient(SeaBattleClientInterface client) {
        this.client = client;
    }

    @FXML
    public void initialize() {
        BoardFirst();
        BoardSecond();
        placeShipsButton.setOnAction(event -> startPlacingShips());
        attackButton.setOnAction(event -> startAttacking());
    }

    @FXML
    private void registerPlayer() throws RemoteException {
        String playerName = playerNameField.getText();

        if (playerName.isEmpty()) {
            System.out.println("Имя игрока не может быть пустым.");
            return;
        }

        this.player = new Player(playerName);
        boolean registered = game.registerPlayer(player);

        if (registered) {
            System.out.println("Игрок зарегистрирован на сервере: " + player.getName());
            clearInterface();

            // Определим, кто из игроков первый
            isFirstPlayer = game.isFirstPlayer(player); // Запросим сервер, кто первый

            // После регистрации второго игрока вызываем метод startGame
            if (game.getPlayer2() != null && game.getPlayer1() != null) {
                startGame();
            }
        } else {
            System.out.println("Не удалось зарегистрировать игрока.");
        }
    }


    private void startGame() throws RemoteException {
        game.startGame();
    }

    private void clearInterface() {
        registerButton.setDisable(true);
        playerNameField.setDisable(true);
    }

    // Инициализация доски для кораблей
    private void BoardFirst() {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Button cellButton = new Button();
                cellButton.setMinSize(30, 30);
                final int finalRow = row;
                final int finalCol = col;
                cellButton.setOnAction(event -> onPlaceShipClicked(finalRow, finalCol));
                player1BoardButtons[row][col] = cellButton;
                player1Board.add(cellButton, col, row);
            }
        }
    }

    // Инициализация доски для ходов
    private void BoardSecond() {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                final int finalRow = row;
                final int finalCol = col;
                Button cellButton = new Button();
                cellButton.setMinSize(30, 30);
                cellButton.setOnAction(event -> onAttackClicked(finalRow, finalCol));
                player2BoardButtons[row][col] = cellButton;
                player2Board.add(cellButton, col, row);
            }
        }
    }

    private void startPlacingShips() {
        isPlacingShips = true;
        showInfo("Режим расстановки", "Теперь выберите клетки для размещения кораблей.");
        currentShipCoordinates.clear(); // Очистка предыдущих координат
        updateShipSize(); // Обновление размера корабля
    }

    // Метод для расстановки кораблей
    private void onPlaceShipClicked(int row, int col)  {
        if (!isPlacingShips) {
            showError("Ошибка", "Сначала нажмите 'Расставить корабли'.");
            return;
        }

        if (game == null) {
            showError("Ошибка", "Вы не подключены к серверу.");
            return;
        }

        System.out.println("Текущий игрок: " + player.getName());
        Board playerBoard;
        try {
            playerBoard = getPlayerBoard();
        } catch (RemoteException e) {
            showError("Ошибка", "Не удалось получить доску игрока: " + e.getMessage());
            return;
        }

        // Проверка, занята ли клетка
        try {
            boolean isOccupied = game.isCellOccupied(row, col, player);
            if (isOccupied) {
                System.out.println("Ошибка: клетка (" + row + ", " + col + ") уже занята!");
                showError("Ошибка", "Эта клетка уже занята. Выберите другую.");
                return;
            }
        } catch (RemoteException e) {
            System.out.println("Ошибка при проверке клетки: " + e.getMessage());
            showError("Ошибка", "Не удалось проверить клетку.");
            return;
        }

        if (currentShipCoordinates.size() >= currentShipSize) {
            showError("Ошибка", "Невозможно добавить больше клеток для текущего корабля.");
            return;
        }

        addShipCoordinateAndUpdateCell(playerBoard, row, col);

        if (currentShipCoordinates.size() == currentShipSize) {
            confirmShipPlacement(playerBoard);
        }
    }

    private Board getPlayerBoard() throws RemoteException {
        try {
            System.out.println("Запрашиваем доску для игрока: " + player.getName());
            return game.getPlayerBoard(player);
        } catch (RemoteException e) {
            System.out.println("Ошибка при запросе доски: " + e.getMessage());
            throw e;
        }
    }

    private void addShipCoordinateAndUpdateCell(Board playerBoard, int row, int col) {
        Coordinate coordinate = new Coordinate(row, col);
        currentShipCoordinates.add(coordinate);
        playerBoard.setCell(row, col, CellState.SHIP);

        // Определение массива кнопок для текущего игрока
        Button[][] playerBoardButtons = player1BoardButtons;
        playerBoardButtons[row][col].setStyle("-fx-background-color: gray;");
    }

    private void confirmShipPlacement(Board playerBoard) {
        try {
            boolean success = game.placeShip(currentShipCoordinates, currentShipSize, player);

            if (success) {
                updateShipCells(playerBoard, "-fx-background-color: black;");
                showInfo("Успех", "Корабль размещен.");
                updateRemainingShips();
                updateShipSize();
            } else {
                updateShipCells(playerBoard, "-fx-background-color: lightgray;");
                showError("Ошибка", "Невозможно разместить корабль. Попробуйте снова.");
            }
        } catch (RemoteException e) {
            showError("Ошибка", "Не удалось разместить корабль.");
        }
        currentShipCoordinates.clear();
    }

    private void updateShipCells(Board playerBoard, String color) {
        Button[][] boardButtons = player1BoardButtons;

        for (Coordinate coordinate : currentShipCoordinates) {
            playerBoard.setCell(coordinate.getX(), coordinate.getY(), CellState.SHIP);
            boardButtons[coordinate.getX()][coordinate.getY()].setStyle(color);
        }
    }

    private void updateRemainingShips() {
        switch (currentShipSize) {
            case 1:
                remainingOneCellShips--;
                break;
            case 2:
                remainingTwoCellShips--;
                break;
            case 3:
                remainingThreeCellShips--;
                break;
            case 4:
                remainingFourCellShips--;
                break;
        }
    }

    private void updateShipSize() {
        if (remainingFourCellShips > 0) {
            currentShipSize = 4;
        } else if (remainingThreeCellShips > 0) {
            currentShipSize = 3;
        } else if (remainingTwoCellShips > 0) {
            currentShipSize = 2;
        } else if (remainingOneCellShips > 0) {
            currentShipSize = 1;
        } else {
            showInfo("Готово", "Все корабли расставлены!");
        }
    }


    private void startAttacking() {
        isAttacking = true;
        showInfo("Режим атаки", "Теперь выберите клетки для атаки.");
    }

    private void onAttackClicked(int row, int col) {

        if (!isAttacking) {
            showError("Ошибка", "Сначала нажмите 'Сделать ход' для начала атаки.");
            return;
        }

        if (game == null) {
            showError("Ошибка", "Вы не подключены к серверу.");
            return;
        }

        try {

            System.out.println("Клиент: первый игрок - " + game.getPlayer1());
            System.out.println("Клиент: второй игрок - " + game.getPlayer2());

            // Выполнение хода

            System.out.println("Это игрок " +player);
            if (player.equals(game.getCurrentPlayer())){

                System.out.println("Клиент: текущий игрок до хода - " + game.getCurrentPlayer());
                game.makeMove(row, col);
                System.out.println("Клиент: текущий игрок после хода - " + game.getCurrentPlayer());

                updateOpponentBoard(game.getOpponentBoardForPlayer(player), true);

            } else {
                showInfo("Ошибка","Не ваша очередь, подождите");
                System.out.println("Не ваша очередь, подождите");
            }

            if (isGameOver()) {
                Player winner = getWinner(); // Получаем победителя
                showInfo("Игра завершена", "Победитель: " + winner.getName());
                lockGameInterface();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            showError("Ошибка", "Не удалось выполнить атаку. Попробуйте снова.");
        }
    }



    protected void updateBoard(Player player) {
        Board playerBoard;
        try {
            playerBoard = game.getPlayerBoard(player);
            Button[][] boardButtons =  player1BoardButtons;
            // Обновляем отображение клеток на доске игрока
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    CellState state = playerBoard.getCell(row, col).getState();
                    updateCellDisplay(row, col, state, boardButtons);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            showError("Ошибка", "Не удалось обновить доску игрока.");
        }
    }



    // Метод для обновления доски противника
    private void updateOpponentBoard(Board opponentBoard, boolean isPlayer1) {
        // Выбираем доску противника

        Button[][] boardButtons = isPlayer1 ? player2BoardButtons : player1BoardButtons;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                CellState state = opponentBoard.getCell(row, col).getState();
                if (state == CellState.SHIP) {
                    // Показываем корабль противника только если по нему было попадание
                    updateCellDisplay(row, col, CellState.EMPTY, boardButtons);
                } else {
                    // Обновляем ячейки для отображения состояния (попадание или промах)
                    updateCellDisplay(row, col, state, boardButtons);
                }
            }
        }

    }


    private void updateCellDisplay(int row, int col, CellState state, Button[][] boardButtons) {
        Button cellButton = boardButtons[row][col];


        switch (state) {
            case HIT:
                cellButton.setStyle("-fx-background-color: red;"); // Попадание
                break;
            case MISS:
                cellButton.setStyle("-fx-background-color: blue;"); // Промах
                break;
            case SHIP:
                cellButton.setStyle("-fx-background-color: black;"); // Корабль (видно на доске игрока)
                break;
        }
    }





    private boolean isGameOver() {
        try {
            return game.isGameOver();
        } catch (RemoteException e) {
            e.printStackTrace();
            showError("Ошибка", "Не удалось проверить завершение игры.");
            return false;
        }
    }


    private Player getWinner() {
        try {
            return game.getWinner();
        } catch (RemoteException e) {
            e.printStackTrace();
            showError("Ошибка", "Не удалось получить победителя.");
            return null;
        }
    }

    private void lockGameInterface() {
        // Блокируем кнопки на доске игрока
        disableBoard(player1BoardButtons);
        disableBoard(player2BoardButtons);
        registerButton.setDisable(true);
        playerNameField.setDisable(true);
        attackButton.setDisable(true);
        placeShipsButton.setDisable(true);
    }

    private void disableBoard(Button[][] boardButtons) {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                boardButtons[row][col].setDisable(true); // Делаем кнопки недоступными
            }
        }
    }
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

