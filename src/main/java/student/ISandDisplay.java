package student;

import java.awt.*;

public interface ISandDisplay {
    void setValue(int row, int col, Particle value);

    int getNumRows();

    int getNumCols();

    Particle getValue(int row, int col);

    void setColor(int row, int col, Color color);

    int getSpeed();

    void repaint();

    void pause(int i);

    int[] getMouseLocation();

    Particle getTool();

    Object[] getGrid();

    Object[] getColors();
}