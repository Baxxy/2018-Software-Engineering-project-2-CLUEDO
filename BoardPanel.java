
import java.awt.*;
import java.util.ArrayList;

/**
 * A board panel which draws all the images and sets up the token locations
 * @Author Richard, Gajun
 */
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class BoardPanel extends JPanel{

	private ArrayList<Weapon> logo;
	private ArrayList<Player> icon;
	
	//Graphic objects
	private TileGrid test3;
	private Board test;
	
	//Constructor panel
	public BoardPanel() {
		logo = new ArrayList<Weapon>();
		icon = new ArrayList<Player>();
		test3 = new TileGrid();
		test = new Board();
		createWeapons();
		createPlayers();
	}
	
	//Candlestick, Dagger, Lead Pipe, Revolver, Rope, Spanner
	public void createWeapons() {
		logo.add(new Weapon("C", test3.map[21][1])); //Candlestick
		logo.add(new Weapon("D", test3.map[2][21])); //Dagger
		logo.add(new Weapon("L", test3.map[1][12])); //Lead pipe
		logo.add(new Weapon("Re", test3.map[22][22])); //Revolver
		logo.add(new Weapon("R", test3.map[19][16])); //Rope
		logo.add(new Weapon("S", test3.map[2][2])); //Spanner
		
	}

	//Creating a list of players [Fixed starting location]
	public void createPlayers(){
		icon.add(new Player("Plum", Color.magenta,  test3.map[21][1]));
		icon.add(new Player("White", Color.white, test3.map[21][1]));
		icon.add(new Player("Scarlet", Color.red,  test3.map[21][1]));
		icon.add(new Player("Green", Color.green,  test3.map[21][1]));
		icon.add(new Player("Mustard", Color.yellow,  test3.map[21][1]));
		icon.add(new Player("Peacock", Color.blue,  test3.map[23][6]));
	}
	
	/*
	public void movement() {
		
	//	logo[0].setTile(test3.map[5][5]);
		repaint();
	}
	*/


	//Draw the objects together
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		test.paintComponent(g2); //Board image never changes
		test3.drawGrid(g2); //Draw grid
		
		//Draw all the weapons in their current position
		for(Weapon li: logo) {
			li.drawWeapon(g2);
		}
		
		//Draw all the players in their first location
		for(Player pi: icon) {
			pi.drawPlayer(g2);
		}	
	}
}
