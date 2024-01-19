package student;

import java.awt.*;
import java.util.*;


/**
 * Keeps track of the functionality of a display on which a user can draw falling sand (and other
 * particles).
 */
public class SandLab {

    /**
     * Creates and runs a falling sand display with {@link #NUM_ROWS} rows and {@link #NUM_COLS} columns.
     */
    public static void main(String[] args) {
        SandDisplay display = new SandDisplay("Falling Sand", NUM_ROWS, NUM_COLS);
        SandLab lab = new SandLab(display);
        lab.run();
    }

    protected final ISandDisplay display;

    /**
     * The number of rows of the display to create.
     */
    public static final int NUM_ROWS = 120;

    public static final int NUM_WATER_MOVEMENTS = 3;

    /**
     * The number of columns of the display to create.
     */
    public static final int NUM_COLS = 80;

    private final Random rand;

    /**
     * Constructs a new SandLab display.
     *
     * @param display the GUI
     */
    public SandLab(ISandDisplay display) {
        this.display = display;
        rand = new Random();
    }

    /**
     * Updates the value in the grid to reflect the user painting that pixel using the given tool.
     *
     * @param row  row where the user clicked
     * @param col  column where the user clicked
     * @param tool Particle tool with which the user is painting. If the user is erasing, the tool is
     *             null.
     */
    protected void locationClicked(int row, int col, Particle tool) {
        display.setValue(row, col, tool);
    }

    /**
     * Updates the display to show the right color in each pixel in the grid
     */
    public void updateDisplay() {
        for (int row = 0; row < display.getNumRows(); row++) {
            for (int col = 0; col < display.getNumCols(); col++) {
                Particle tool = display.getValue(row, col);
                if (tool == null) {
                    display.setColor(row, col, Color.BLACK);
                } else if (tool == Particle.METAL) {
                    display.setColor(row, col, Color.GRAY);
                } else if (tool == Particle.SAND) {
                    display.setColor(row, col, Color.YELLOW);
                } else {
                    display.setColor(row, col, Color.CYAN);
                }
            }
        }
    }

    /**
     * Chooses a random spot on the grid, and if it contains a
     * particle, then moves that particle in a manner appropriate to the particle type.
     */
    public void step() {
        int row = rand.nextInt(display.getNumRows());
        int col = rand.nextInt(display.getNumCols());

        Particle tool = display.getValue(row, col);
        if (tool == Particle.SAND) {
            sandMove(row, col);
        } else if (tool == Particle.WATER) {
            waterMove(row, col, rand.nextInt(NUM_WATER_MOVEMENTS) - 1);
        }
    }

    private void sandMove(int row, int col) {
        if (row < display.getNumRows() - 1 && display.getValue(row + 1, col) == null) {
            display.setValue(row + 1, col, Particle.SAND);
            display.setValue(row, col, null);
        }
    }

    private void waterMove(int row, int col, int direction) {
        if (row < display.getNumRows() - 1
                && col + direction >= 0
                && col + direction < display.getNumCols()
                && display.getValue(row + 1, col + direction) == null) {
            display.setValue(row + 1, col + direction, Particle.WATER);
            display.setValue(row, col, null);
        }

    }

    /**
     * Keeps the program running (updating the display based on mouseclicks) until the user quits the
     * program.
     */
    public void run() {
        while (true) {
            for (int i = 0; i < display.getSpeed(); i++) {
                step();
            }
            updateDisplay();
            display.repaint();
            display.pause(1);  //wait for redrawing and for mouse
            int[] mouseLoc = display.getMouseLocation();
            if (mouseLoc != null) { //test if mouse clicked
                locationClicked(mouseLoc[0], mouseLoc[1], display.getTool());
            }
        }
    }
}