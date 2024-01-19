package student;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class SandDisplay extends JComponent implements MouseListener, MouseMotionListener,
        ActionListener, ChangeListener, ISandDisplay {

    public static final String ERASE = "ERASE";
    public static final int SLIDER_MAX = 100;
    public static final int PIXELS = 600;
    public static final int SPEED_BASE = 10;
    public static final int SPEED_OFFSET = 3;
    public static final double SPEED_SCALE = 0.03;
    private final Image image;
    private final int cellSize;
    private final JFrame frame;
    private Particle tool;
    private int[] mouseLoc;
    private final Map<Particle, JButton> buttons;
    private final JSlider slider;
    private int speed;
    protected Particle[][] grid;

    public SandDisplay(String title, int numRows, int numCols) {
        grid = new Particle[numRows][numCols];
        tool = null;
        mouseLoc = null;
        speed = computeSpeed(SLIDER_MAX / 2);

        // pixelate
        cellSize = Math.max(1, PIXELS / Math.max(numRows, numCols));
        image = new BufferedImage(numCols * cellSize, numRows * cellSize, BufferedImage.TYPE_INT_RGB);

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        frame.getContentPane().add(topPanel);

        // clickable pixels
        setPreferredSize(new Dimension(numCols * cellSize, numRows * cellSize));
        addMouseListener(this);
        addMouseMotionListener(this);
        topPanel.add(this);

        // particle buttons
        buttons = new HashMap<>();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        topPanel.add(buttonPanel);
        buttonPanel.add(makeButton(null));
        for (Particle particle : Particle.values()) {
            buttonPanel.add(makeButton(particle));
        }
        buttons.get(tool).setSelected(true);

        // speed slider
        slider = new JSlider(JSlider.HORIZONTAL, 0, SLIDER_MAX, SLIDER_MAX / 2);
        slider.addChangeListener(this);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("Slow"));
        labelTable.put(SLIDER_MAX, new JLabel("Fast"));
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        frame.getContentPane().add(slider);

        // format and make visible
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * @param particle the painting tool (or null if it should erase particles)
     * @return a button that users can click to use the painting tool
     */
    private JButton makeButton(Particle particle) {
        String buttonName = particle == null ? ERASE : particle.toString();
        JButton button = new JButton(buttonName);
        button.setActionCommand(buttonName);
        button.addActionListener(this);
        buttons.put(particle, button);
        return button;
    }

    /**
     * Provides the ability to draw on the screen
     *
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

    /**
     * Pauses the program (e.g., to wait for redrawing to compete)
     *
     * @param milliseconds the number of milliseconds to pause
     */
    public void pause(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the (row, col) location of the mouse as an array of two ints
     */
    public int[] getMouseLocation() {
        return mouseLoc;
    }

    /**
     * @return the Particle painting tool that is currently selected
     */
    public Particle getTool() {
        return tool;
    }

    @Override
    public Particle[][] getGrid() {
        return grid;
    }

    @Override
    public Color[][] getColors() {
        Color[][] colors = new Color[getNumRows()][getNumCols()];
        for (int row = 0; row < getNumRows(); row++) {
            for (int col = 0; col < getNumCols(); col++) {
                Particle particle = grid[row][col];
                if (particle == Particle.METAL) {
                    colors[row][col] = Color.GRAY;
                } else if (particle == Particle.SAND) {
                    colors[row][col] = Color.YELLOW;
                } else if (particle == Particle.WATER) {
                    colors[row][col] = Color.CYAN;
                }
            }
        }
        return colors;
    }

    /**
     * Sets the color of a given pixel in the grid.
     *
     * @param row   x-coordinate of the pixel
     * @param col   y-coordinate of the pixel
     * @param color the Color to which to set the pixel
     */
    public void setColor(int row, int col, Color color) {
        Graphics g = image.getGraphics();
        g.setColor(color);
        g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseLoc = toLocation(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseLoc = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseLoc = toLocation(e);
    }

    /**
     * @param e the mouseclick event
     * @return the location of the mouseclick as an array of two ints: the row and the col
     */
    private int[] toLocation(MouseEvent e) {
        int row = e.getY() / cellSize;
        int col = e.getX() / cellSize;
        if (row < 0 || row >= grid.length || col < 0 || col >= grid[0].length) {
            return null;
        }
        int[] loc = new int[2];
        loc[0] = row;
        loc[1] = col;
        return loc;
    }

    /**
     * Process an event
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        tool = command.equals(ERASE) ? null : Particle.valueOf(command);
        for (JButton button : buttons.values()) {
            button.setSelected(false);
        }
        ((JButton) e.getSource()).setSelected(true);
    }

    /**
     * Update the speed because the user moved the slider
     *
     * @param e a ChangeEvent object
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        speed = computeSpeed(slider.getValue());
    }

    /**
     * @return the current speed: the number of times to step between repainting and processing mouse
     * input
     */
    public int getSpeed() {
        return speed;
    }


    /**
     * @param sliderValue the value on the speed slider
     * @return the resulting speed based on the slider value. E.g., speed of 0 returns 10^3, speed of
     * 100 returns 10^6
     */
    private int computeSpeed(int sliderValue) {
        return (int) Math.pow(SPEED_BASE, SPEED_SCALE * sliderValue + SPEED_OFFSET);
    }

    /**
     * @param row the row in the grid
     * @param col the column in the grid
     * @return the Particle at the given spot in the grid
     */
    public Particle getValue(int row, int col) {
        return grid[row][col];
    }

    /**
     * @param row   row in the grid
     * @param col   column in the grid
     * @param value the Particle to which to set the given spot on the grid
     */
    public void setValue(int row, int col, Particle value) {
        grid[row][col] = value;
    }

    /**
     * @return the number of rows in the grid
     */
    public int getNumRows() {
        return grid.length;
    }

    /**
     * @return the number of columns in the grid
     */
    public int getNumCols() {
        return grid[0].length;
    }
}