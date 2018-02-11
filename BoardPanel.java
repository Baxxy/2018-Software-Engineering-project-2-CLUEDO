
import java.awt.*;
import java.util.ArrayList;

/**
 * A board panel which draws all the images and sets up the token locations
 * @Author Richard, Gajun
 */
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class BoardPanel extends JPanel {

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
    public void createPlayers() {
        icon.add(new Player("Plum", Color.magenta, test3.map[23][19]));
        icon.add(new Player("White", Color.white, test3.map[9][0]));
        icon.add(new Player("Scarlet", Color.red, test3.map[7][24]));
        icon.add(new Player("Green", Color.green, test3.map[14][0]));
        icon.add(new Player("Mustard", Color.yellow, test3.map[0][17]));
        icon.add(new Player("Peacock", Color.blue, test3.map[23][6]));
    }

    //Method for moving weapons that APPARENTLY isnt allowed. So i just put them with the player
    // movements.
    /*public void setWeaponTile(String name, int newRow, int newColumn){
        for(int j = 0; j < 6; j++){
			if(logo.get(j).getName() == name){
				Tile tempTile = new Tile((23*newColumn), 23.5 + (23*newRow), newRow, newColumn);
				logo.get(j).setTile(tempTile);
			}
		}
		repaint();
	}*/

    //For now these move both players and weapons. The input is the direction to move and name of
    // what to move.
    //They work by cycling through directions then all 12 players/weapons and when the name
    // matches, it moves it.
    //Ive considered ending it after a name matches but 12 loops is literally NOTHING for the CPU!
    //Method to move player/weapon.
    public boolean moveToken(String direction, String name) {
        boolean moved;
        boolean correctName = false;
        boolean correctDirection = false;

        //Moving player up
        if (direction.equals("up") && correctName == false) {
            correctDirection = true;
            for (int j = 0; j < 6; j++) {
                if (icon.get(j).getPlayerName().equals(name)) {
                    Tile tempTile = new Tile(icon.get(j).getTile().getXCord(), icon.get(
                            j).getTile().getYCord() - 23, icon.get(j).getTile().getRow() - 1,
                            icon.get(j).getTile().getColumn());
                    icon.get(j).setTile(tempTile);
                    correctName = true;
                }
                if (logo.get(j).getWeaponName().equals(name)) {
                    Tile tempTile = new Tile(logo.get(j).getTile().getXCord(), logo.get(
                            j).getTile().getYCord() - 23, logo.get(j).getTile().getRow() - 1,
                            logo.get(j).getTile().getColumn());
                    logo.get(j).setTile(tempTile);
                    correctName = true;
                }
            }
            repaint();
        } else if (direction.equals("right") && correctName == false) {
            // move player right
            correctDirection = true;
            for (int j = 0; j < 6; j++) {
                if (icon.get(j).getPlayerName().equals(name)) {
                    Tile tempTile = new Tile(icon.get(j).getTile().getXCord() + 23, icon.get(
                            j).getTile().getYCord(), icon.get(j).getTile().getRow(), icon.get(
                            j).getTile().getColumn() + 1);
                    icon.get(j).setTile(tempTile);
                    correctName = true;
                }
                if (logo.get(j).getWeaponName().equals(name)) {
                    Tile tempTile = new Tile(logo.get(j).getTile().getXCord() + 23, logo.get(
                            j).getTile().getYCord(), logo.get(j).getTile().getRow(), logo.get(
                            j).getTile().getColumn() + 1);
                    logo.get(j).setTile(tempTile);
                    correctName = true;
                }
            }
            repaint();
        } else if (direction.equals("down") && correctName == false) {
            //Moving player down
            correctDirection = true;
            for (int j = 0; j < 6; j++) {
                if (icon.get(j).getPlayerName().equals(name)) {
                    Tile tempTile = new Tile(icon.get(j).getTile().getXCord(), icon.get(
                            j).getTile().getYCord() + 23, icon.get(j).getTile().getRow() + 1,
                            icon.get(j).getTile().getColumn());
                    icon.get(j).setTile(tempTile);
                    correctName = true;
                }
                if (logo.get(j).getWeaponName().equals(name)) {
                    Tile tempTile = new Tile(logo.get(j).getTile().getXCord(), logo.get(
                            j).getTile().getYCord() + 23, logo.get(j).getTile().getRow() + 1,
                            logo.get(j).getTile().getColumn());
                    logo.get(j).setTile(tempTile);
                    correctName = true;
                }
            }
            repaint();
        } else if (direction.equals("left") && correctName == false) {
            //Moving player left
            correctDirection = true;
            for (int j = 0; j < 6; j++) {
                if (icon.get(j).getPlayerName().equals(name)) {
                    Tile tempTile = new Tile(icon.get(j).getTile().getXCord() - 23, icon.get(
                            j).getTile().getYCord(), icon.get(j).getTile().getRow(), icon.get(
                            j).getTile().getColumn() - 1);
                    icon.get(j).setTile(tempTile);
                    correctName = true;
                }
                if (logo.get(j).getWeaponName().equals(name)) {
                    Tile tempTile = new Tile(logo.get(j).getTile().getXCord() - 23, logo.get(
                            j).getTile().getYCord(), logo.get(j).getTile().getRow(), logo.get(
                            j).getTile().getColumn() - 1);
                    logo.get(j).setTile(tempTile);
                    correctName = true;
                }
            }
            repaint();
        }
        //When this is false, no move has been made and the info box should return an error to
        // the player, prompting them to try again.
        moved = correctDirection && correctName;
        return moved;
    }


    //Draw the objects together
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        test.paintComponent(g2); //Board image never changes
        test3.drawGrid(g2); //Draw grid

        //Draw all the weapons in their current position
        for (Weapon li : logo) {
            li.drawWeapon(g2);
        }

        //Draw all the players in their first location
        for (Player pi : icon) {
            pi.drawPlayer(g2);
        }
    }

}
