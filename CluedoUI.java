import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * This UI combines three main panel concepts together onto one frame in which
 * users can interact with.
 *
 * @Team MAGA
 * @Author Gajun Young - 16440714
 * @Author Royal Thomas - 16326926
 * @Author Richard  Otroshchenko
 */
public class CluedoUI {

    //Size of the frame
    private static final int FRAME_WIDTH = 1015;
    private static final int FRAME_HEIGHT = 830;

    private BoardPanel board;
    private InformationPanel info;
    private CommandPanel command;
    private JFrame frame;
    private Random rand = new Random();

    //Constructor
    public CluedoUI(Players players, Weapons weapons) {
    	frame = new JFrame();
        JPanel mainPanel = new JPanel();
        board = new BoardPanel(players, weapons);
        command = new CommandPanel();
        info = new InformationPanel();

        // Set up the main panel to look good, this panel contains two panels.
        mainPanel.add(board);
        mainPanel.add(info);
        mainPanel.setPreferredSize(new Dimension(1000, 750));
        mainPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Cluedo"));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));


        //Position all the panels into their correct places

        frame.getContentPane().setBackground(Color.decode("#ebe0ca"));
        frame.add(mainPanel);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setTitle("Cluedo");
        frame.add(mainPanel, BorderLayout.LINE_START);
        frame.add(command, BorderLayout.PAGE_END);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    /**
     * Sets the board with weapons[default] and tokens which depends on the user inputs
     */
    public void setBoard(Players players, Weapons weapons) {
        board.set(players, weapons);
    }

    public void drawDice(int roll1, int roll2){
        if(roll1!=0) { //The following code plays a nice roll animation
            for (int i = 0; i < 8; i++) {
                board.drawDice(rand.nextInt(6) + 1);
                board.drawDice2(rand.nextInt(6)+1);
                display();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        board.drawDice(roll1); //Draws the final result
        board.drawDice2(roll2);
    }

    /**
     * @return A String that the user types
     */
    public String getCommand() {
        String input = command.getCommand();
    	if(input.equalsIgnoreCase("quit")) {
    		System.exit(0);
    	}
        return input;
    }

    /**
     * Repaint the board to show updated tokens
     */
    public void display() {
        board.repaint();
    }

    /**
     * Takes a string in which it gets updated onto the information panel
     *
     * @param string contains information of a tokens turn
     */
    public void displayString(String string) {
        info.updateContent(string);
    }

    /**
     * Finds the path of an image
     * @param path
     * @return
     * @throws IOException
     */
    public static JLabel imageToLabel(String path) throws IOException {
        BufferedImage myPicture = ImageIO.read(CluedoUI.class.getClassLoader().getResourceAsStream(path));
        JLabel picLabel = new JLabel(new ImageIcon(myPicture));
        return picLabel;
    }
}
