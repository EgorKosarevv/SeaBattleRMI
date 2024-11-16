package server;

import client.SeaBattleClientInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SeaBattleInterface extends Remote {



    boolean registerPlayer(Player player) throws RemoteException;

    Player getPlayer1() throws RemoteException;
    Player getPlayer2() throws RemoteException;


    void startGame() throws RemoteException;


    Player getCurrentPlayer() throws RemoteException;


    void makeMove(int x, int y) throws RemoteException;


    Board getOpponentBoardForPlayer(Player player) throws RemoteException;


    boolean placeShip(List<Coordinate> coordinates, int size, Player player) throws RemoteException;



    boolean isGameOver() throws RemoteException;


    Player getWinner() throws RemoteException;

    boolean isFirstPlayer(Player player) throws RemoteException;

    Board getPlayerBoard(Player player) throws RemoteException;
    boolean isCellOccupied(int row, int col, Player player) throws RemoteException;


    void registerClient(SeaBattleClientInterface client) throws RemoteException;
}
