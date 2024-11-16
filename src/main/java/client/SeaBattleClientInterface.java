package client;


import server.Player;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SeaBattleClientInterface extends Remote {
    void updatePlayerBoard(Player player) throws RemoteException;
    void updateMessage(String message) throws RemoteException;
}
