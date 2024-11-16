package server;

import java.io.Serializable;

public class Cell implements Serializable {
    private CellState state;

    public Cell(CellState state) {
        this.state = state;
    }

    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;
    }
}
