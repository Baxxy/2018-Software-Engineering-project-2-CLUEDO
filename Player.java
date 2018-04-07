import java.util.ArrayList;

/**
 * A class that represents a player containing their name and player they choose
 *
 * @Team MAGA
 * @Author Gajun Young - 16440714
 * @Author Royal Thomas - 16326926
 * @Author Richard  Otroshchenko - 16353416
 */
public class Player {
    private String name;    //Name of player
    private int choice;    //player's choice
    private Token token;
    private String imagePath;    //tokens image
    private ArrayList<Card> cards = new ArrayList<>();		//Cards that a player has
    private NoteBook note; 							//A players notebook 
    
    //Constructor
    public Player(String name, int choice) {
        this.name = name;
        this.choice = choice;
        this.token = null;
        this.note = null;
    }

    //Accessor of player name
    public String getName() {
        return name;
    }

    //Returns the player's choice
    public int getChoice() {
        return choice;
    }

    //Sets the player's token
    public void setToken(Token token) {
        this.token = token;
    }

    //Return the player's token
    public Token getToken() {
        return token;
    }

    //Sets the path of the token image
    public void setImagePath(String path) {
        this.imagePath = path;
    }

    //Returns the tokens image
    public String getImagePath() {
        return imagePath;
    }

    //Returns true if matching token
    public boolean hasName(String name) {
        return this.name.toLowerCase().equals(name.toLowerCase().trim());
    }

    //Returns true if matching choice
    public boolean hasChoice(int choice) {
        return !(this.choice == choice);
    }

    //Checks if two tokens are on the same tile
    public boolean hasTile(Tile tile) {
        return this.token.getPosition().equals(tile);
    }

    public void giveCard(Card card) {
        this.cards.add(card);
    }

    public ArrayList<Card> getCards() {
        return cards;
    }
    
    //Sets notebook
    public void setNoteBook(ArrayList<Card> undealt) {
    	this.note = new NoteBook(undealt, cards);
    }
    
    //Displays player's note
    public void displayNote() {
    	note.showNotes();
    }

}
