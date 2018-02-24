import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;

/**
 * A panel that allows users to type information which in turns interacts with the board
 * 
 * @Team MAGA
 * @Author Gajun Young - 16440714
 * @Author Royal Thomas - 16326926
 * @Author Richard  Otroshchenko
 */
public class CommandPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final int SIZE = 30;
	private static final int FONT_SIZE = 14;
	
	private JTextField commandField = new JTextField("", SIZE);
//	private JButton submit = new JButton("Submit");
	private final LinkedList<String> commandBuffer = new LinkedList<>();
    public static JLabel picLabel;
	//Constructor
	public CommandPanel() {
		JPanel inputPanel = new JPanel(); //Panel that displays input 
		JPanel availableInput = new JPanel(); //Panel that displays available commands that users can use
		
		//A Label that contains the current player icon

		try {
            picLabel = CluedoUI.imageToLabel("Profiler/default.png");
            picLabel.setBorder(BorderFactory.createTitledBorder("Current Player"));
            add(picLabel);

		}catch(IOException ex){
			System.out.println("Unable to load ballroom image.");
		}

		
		//Beautification
		setPreferredSize(new Dimension(1000, 145));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(new EmptyBorder(0, 10, 0, 0));
		
		//A container that holds the textbox and the submit button
		inputPanel.setBorder(new EmptyBorder(50, 0, 0, 0));
		inputPanel.add(new JLabel("To test player and weapon movement on the current player"));
		//inputPanel.add(submit);
		
		//Add a panel to make a list of labels and set its layout to allow for that
		availableInput.setLayout(new BoxLayout(availableInput, BoxLayout.Y_AXIS));
	    availableInput.setPreferredSize(new Dimension(300, 0));
	    availableInput.setBorder(BorderFactory.createTitledBorder("Available Inputs"));
	    String[] listInputs = {"u(up)", "d(down)", "l(left)", "r(right)"};
        for (String x : listInputs) {
            availableInput.add(new JLabel(x));
        }
        add(inputPanel);
        add(availableInput);
        
        //An actionListener to listen for user inputs and respond
        class AddActionListener implements ActionListener {
        	public void actionPerformed(ActionEvent event)	{
        		synchronized (commandBuffer) {
        			commandBuffer.add(commandField.getText());
        			commandField.setText("");
        			commandBuffer.notify();
        		}
        	}

        }
        
        ActionListener listener = new AddActionListener();
        commandField.addActionListener(listener);
        commandField.setFont(new Font("Times New Roman", Font.PLAIN, FONT_SIZE));
        inputPanel.add(commandField);
	        
        //border style
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Command Panel"));
		
	}
	
	/**
	 * A method that takes in a string of information
	 * 
	 * @return A string that user typed
	 */
	public String getCommand() {
		String command;
		synchronized(commandBuffer) {
			while (commandBuffer.isEmpty()) {
				try {
					commandBuffer.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		command = commandBuffer.pop();
		}
	    
		return command.replaceAll("\\s+","");
	}

	public static void updateUserImage(String path) {
        try {
            System.out.println(path);
            BufferedImage myPicture = ImageIO.read(CluedoUI.class.getClassLoader().getResourceAsStream(path));
            picLabel.setIcon(new ImageIcon(myPicture));
            picLabel.revalidate();
            picLabel.repaint();
            picLabel.update(picLabel.getGraphics());
        }catch(IOException ex){
            System.out.println("Unable to load user image.");
        }
    }
}
