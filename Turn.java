import java.util.ArrayList;


/**
 * A class that represents the turns of each player
 *
 * @Team MAGA
 * @Author Gajun Young - 16440714
 * @Author Royal Thomas - 16326926
 * @Author Richard  Otroshchenko - 16353416
 */

public class Turn {

	private CluedoUI ui;
	private TileGrid grid = new TileGrid();
	private Weapons items;
	private Players players;
	private Players dummies;
	ArrayList<Card> murderEnvelope = new ArrayList<>(); //Holds the murder envelope contents

	String[] suspects = {"List of tokens", "Plum", "White", "Scarlet", "Green", "Mustard", "Peacock"};
	String[] weapons  = {"List of weapons", "Candle Stick", "Dagger", "Lead Pipe", "Revolver", "Rope", "Spanner"};
	String[] rooms    = {"Kitchen", "Ball Room", "Conservatory", "Dinning Room","Billiard Room", "library", "Lounge", "Hall", "Study"};
	
	public Turn(CluedoUI ui, Players players, Weapons weapons, Players dummies) {
		this.ui = ui;
		this.players = players;
		this.items = weapons;
		this.dummies = dummies;
	}
	
	public void setTurn(Players players, Weapons weapons, Players dummies) {
		this.players = players;
		this.items = weapons;
		this.dummies = dummies;
	}

	/**
	 * Each player takes turns
	 */
	public void turns() {
		String command; 				//Text that is entered
		boolean valid;  				//Check if action is valid
		Dice dice = new Dice();

		do {
			for (int i = 0; i < players.getCapacity(); i++) {
				valid = false;

				//First prompt for first board action
				ui.displayString("===" + players.currPlayer(i) + "'s TURN===");
				ui.displayString("Type a command");
				CommandPanel.updateUserImage(players.getPlayer(i).getImagePath());

				//Available commands
				String[] commands = {"roll","notes","cheat","help"};
				CommandPanel.updateCommands(commands);
				CommandPanel.updateMovesReamining(-1);

				do {
					command = ui.getCommand();
					ui.displayString(players.currPlayer(i) + ": " + command);

					if (command.equalsIgnoreCase("roll")) {

						//Rolls the dice and displays the result on-screen
						dice.rollDice();
						CommandPanel.updateCommands();
						ui.drawDice(dice.getRoll1(), dice.getRoll2());
						ui.displayString(players.currPlayer(i) + " rolled " + (dice.getRoll1() + dice.getRoll2()));
						ui.display();
						try { //The resulting dice roll is on-screen for 2 seconds
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						ui.drawDice(0, 0); //This hides the dice
						ui.display();
						valid = true;
					} else if (command.equalsIgnoreCase("notes")) {
						//Displays all cards apart from murder envelope ones, indicating cards they own or cards everyone can see
						players.getPlayer(i).displayNote();
					} else if (command.equalsIgnoreCase("cheat")) {
						//Displays the murder envelope cards
						cheat();
					} else if(command.equalsIgnoreCase("help")){
						//Displays various commands and an explanation on what they do
						help();
					}else {
						ui.displayString("Whoops! Wrong command.\nType 'help' if you're unsure what to do.");
					}
				} while (!valid); //The first part of their turn ends only when they make a roll

				//After rolling, the player decides where and how to move
				movement(dice.getRoll1()+dice.getRoll2(), i);

				//After all actions
				do {
					valid = false;
					ui.displayString(players.currPlayer(i) + " no moves left.\nType a command");
					String[] commandsEnd = {"done","notes","cheat","help"};
					if(players.getTile(i).getSlot() == 5){
						String[] extra = {"done","notes","cheat","help", "question"};
						CommandPanel.updateCommands(extra);
					}else {
						CommandPanel.updateCommands(commandsEnd);
					}
				
					command = ui.getCommand();
					ui.displayString(players.currPlayer(i) + ": " + command);
					
					if (command.equalsIgnoreCase("done")){
						valid = true;
					} else if (command.equalsIgnoreCase("notes")){
						players.getPlayer(i).displayNote();
					}else if(command.equalsIgnoreCase("cheat")){
						cheat();
					}else if(command.equalsIgnoreCase("help")){
						help();
					}else if(players.getTile(i).getSlot() == 5){
						if(command.equalsIgnoreCase("question")) {
							question(players, i);
							valid = true;
						}
					}else {
						ui.displayString("Whoops! Wrong command.\nType 'help' if you're unsure what to do.");
					}
				} while (!valid); //Their turn ends after they type the 'done' command
			}
		} while (true);
	}
	//The following method iterates through the contents of the murder envelope and displays it to the player
	private void cheat(){
		ui.displayString("======CHEATS======");
		for(Card x: murderEnvelope){
			ui.displayString(x.toString());
		}
	}
	//The following method displays info about each command to the player
	private void help(){
		ui.displayString("======HELP======");
		ui.displayString("'roll' - to roll the dice and begin your turn."
				+ "\nA roll ranges from 1 to 6 and you can move that many spaces on the board."
				+ "\n\n'notes' - Type this to inspect your notes."
				+ "\nThis lists all players, weapons and rooms,\nand shows an 'X' mark for cards"
				+ " you own and an 'A' mark for cards everybody sees."
				+ "\n\n'cheat' - Allows you to inspect the murder envelope."
				+ "\n\n'u,r,d,l' - Type one of these to move up, right, down or left respectively."
				+ "\n\n'Passage' - Type to move from one corner of the board using a room to room passageway"
				+ "\n\n'quit' - This ends the game immediately.");
	}
	
	//Method used in main to tell the turn class the contents of the murder envelope
	public void setMurderEnvelope(ArrayList<Card> array){
		murderEnvelope = array;
	}

	/**
	 * Allows players to move depending on their dice roll
	 *
	 * @param players
	 * @param dice
	 * @param currPlayer
	 */
	public void movement(int dice, int currPlayer) {
		String direction; 									//Contains direction of where the user wants to go
		boolean validDirection;								//If direction is valid
		int sentinel = 0; 									//Ensures right warning is displayed
		Tile currTile = players.getTile(currPlayer);


		ui.displayString(players.currPlayer(currPlayer) + "make your move!");

		//Set of commands a player could possibly use
		String[] commands = {"u - up", "d - down", "l - left", "r - right"};
		CommandPanel.updateCommands(commands);
		CommandPanel.updateMovesReamining(dice);

		do {
			validDirection = false; //Reset
			sentinel = 0;  //Reset

			if(players.getTile(currPlayer).getSlot() == 5 || players.getTile(currPlayer).getSlot() == 3 || players.getTile(currPlayer).getSlot() == 4) {
				if(players.getTile(currPlayer).getSlot() == 3) {
					ui.displayString("Choose another exit");
				}
				Boolean tookSecretPath = exitRoom(currPlayer, players.getTile(currPlayer).getRoom());
				ui.display();
				if(!tookSecretPath){
					ui.displayString(players.currPlayer(currPlayer) + " now choose a direction");
					CommandPanel.updateMovesReamining(dice);
				}else{
					dice = 0;
					break;
				}
			}

			direction = ui.getCommand();
			ui.displayString(players.currPlayer(currPlayer) + ": " + direction);
			//Catch if array is out of bounds
			//Move character to another tile depending on direction choosen
			try {

				if (direction.equalsIgnoreCase("u")) {
					currTile = grid.map[players.getTile(currPlayer).getRow() - 1][players.getTile(currPlayer).getColumn()];
					validDirection = true;

				}
				else if(direction.equalsIgnoreCase("d")) {
					currTile = grid.map[players.getTile(currPlayer).getRow() + 1][players.getTile(currPlayer).getColumn()];
					validDirection = true;

				}
				else if(direction.equalsIgnoreCase("l")) {
					currTile = grid.map[players.getTile(currPlayer).getRow()][players.getTile(currPlayer).getColumn() - 1];
					validDirection = true;
				}
				else if (direction.equalsIgnoreCase("r")){
					currTile = grid.map[players.getTile(currPlayer).getRow()][players.getTile(currPlayer).getColumn() + 1];
					validDirection = true;
				}
				else {
					ui.displayString("'" + direction + "'" + " is not a valid direction");
					sentinel = -1;
				}
			}
			catch(ArrayIndexOutOfBoundsException e) {
				ui.displayString("Invalid direction...[Off the board]");
				sentinel = -1;
			}

			//Ensures no two players are on the same slot
			for(int i = 0; i < players.getCapacity(); i++) {
				if(currTile == players.getTile(i) && validDirection && players.getPlayer(i) != players.getPlayer(currPlayer)) {
					validDirection = false;
					ui.displayString(players.currPlayer(currPlayer) + " cannot move onto an occupied tile");
					sentinel = -1;
					break;
				}

			}

			//If the moved tile is a path & the move is valid
			if((currTile.getSlot() == 1 || currTile.getSlot() == 7) && validDirection) {

				if(players.getTile(currPlayer).getSlot() == 3 && currTile.getSlot() == 7) {
					ui.displayString("There's a wall in your way...");
				}else {
					players.getPlayer(currPlayer).getToken().moveBy(currTile);
					ui.display();
					dice--;
					CommandPanel.updateMovesReamining(dice);
				}
			}
			else if((currTile.getSlot() == 3 && players.getTile(currPlayer).getSlot() != 7) && validDirection) {
				//Enter a room
				players.getPlayer(currPlayer).getToken().moveBy(currTile);

				//Move into center of room and no more movement
				roomCenter(currPlayer, players.getTile(currPlayer).getRoom());
				ui.display();
				dice = 0;
				CommandPanel.updateMovesReamining(dice);
			}
			else if(sentinel == 0 && (currTile.getSlot() != 3 || (currTile.getSlot() == 3 && players.getTile(currPlayer).getSlot() == 7)
					|| currTile.getSlot() == 7 && players.getTile(currPlayer).getSlot() == 3)){
				ui.displayString("There's a wall in your way...");
			}

		}while(dice > 0);

	}

	/**
	 * Reposition players onto the centre of the room
	 * @param players
	 * @param currPlayer
	 * @param room
	 */
	public void roomCenter(int currPlayer, int room) {
		Tile currTile = players.getTile(currPlayer);
		boolean invalidRoom = true;

		
		//Looks for the centre in which a character is positioned
		for(int i = 0; i < grid.map.length; i++) {
			//Quick escape from nested for loop if the value is found early.
			if(!invalidRoom) {
				break;
			}
			for(int j = 0; j < grid.map[i].length; j++) {
				if(grid.map[i][j].getSlot() == 5 && grid.map[i][j].getRoom() == room && invalidRoom) {
					currTile = grid.map[i][j];
					invalidRoom = players.getSameTile(currTile);
					
					//Might be in dummies array
					if(invalidRoom) {
						invalidRoom = dummies.getSameTile(currTile);
					}
				}
			}
		}

		players.getPlayer(currPlayer).getToken().moveBy(currTile);
	}

	/**
	 * Gives a list of exits to players if there are exits
	 * @param players
	 * @param currPlayer
	 * @param room
	 * @return
	 */
	public Boolean exitRoom(int currPlayer, int room) {
		ArrayList<Tile> exits = new ArrayList<Tile>();

		//Searches for possible exits
		for(int i = 0; i < grid.map.length; i++) {
			for(int j = 0; j < grid.map[i].length; j++) {
				if(room == grid.map[i][j].getRoom() && grid.map[i][j].getSlot() == 3) {
					exits.add(grid.map[i][j]);
				}
			}
		}

		//Displays an array of exits for players
		int numExits = 0;
		String exitChoice;
		ui.displayString("Available exits for " + players.currPlayer(currPlayer));
		for(Tile t: exits) {
			ui.displayString(++numExits + ". Exit location" + " " + t.showRoom());
		}
		if(room == 9 || room == 7 || room == 1 || room  == 3){
			ui.displayString("Enter 'passage' to take the secret passage!");
		}
		CommandPanel.updateMovesReamining(-2);

		//Player chooses an exit that isn't blocked
		do {
			do {
				exitChoice = ui.getCommand();
				ui.displayString(players.currPlayer(currPlayer) + ": " + exitChoice);
				if(exitChoice.equalsIgnoreCase("passage") && (room == 9 || room == 7 || room == 1 || room  == 3)){
					break;
				}
				if(!StartUp.isNum(exitChoice)) {
					ui.displayString("'" + exitChoice +"'" + " is not a valid choice");
				}
			}while(!StartUp.isNum(exitChoice));

			if(exitChoice.equalsIgnoreCase("passage") && (room == 9 || room == 7 || room == 1 || room  == 3)){
				break;
			}
			if(Integer.parseInt(exitChoice) < 1 || Integer.parseInt(exitChoice) > exits.size()) {
				ui.displayString(exitChoice + " is not a valid exit choice");
			}
		}while(Integer.parseInt(exitChoice) < 1 || Integer.parseInt(exitChoice) > exits.size());

		if(exitChoice.equalsIgnoreCase("passage") && ((room == 9 || room == 7 || room == 1 || room  == 3))){
			switch(room){
				case 9:
					roomCenter(currPlayer, 1);
					break;
				case 3:
					roomCenter(currPlayer, 7);
					break;
				case 7:
					roomCenter(currPlayer, 3);
					break;
				case 1:
					roomCenter( currPlayer, 9);
				default:
					break;
			}
			return true;
		}else {
			players.getPlayer(currPlayer).getToken().moveBy(
					exits.get(Integer.parseInt(exitChoice) - 1));
			return false;
		}

	}
	
	/**
	 * A suggestion of who was the murder, murder weapon & the room it was done in
	 * @param player
	 * @param curr
	 */
	public void question(Players player, int curr) {
		ui.displayString("==========SUGGESTION==========");
		ui.displayString(player.currPlayer(curr) + ": " + "I suggest it was done by[token]");
		CommandPanel.updateCommands(suspects);
		
		String name, weapon;
		boolean valid = false;
		
		do{
			name = ui.getCommand();
			ui.displayString(player.currPlayer(curr)  + ": " + name);
			
			//Search if name input is correct
			for(int i = 1; i < suspects.length;i++) {
				if(name.equalsIgnoreCase(suspects[i])) {
					valid = true;
					break;
				}
			}
			
			//Error message
			if(!valid) {
				ui.displayString(name + " is not a valid token");
			}
		}while(!valid);
		
		ui.displayString(player.currPlayer(curr)  + ": "+ "I suggest it was done with[weapon]");
		CommandPanel.updateCommands(weapons);
		
		do {
			weapon = ui.getCommand();
			ui.displayString(player.currPlayer(curr) + ": " + weapon);
			
			for(int i = 1; i < weapons.length; i++) {
				if(weapon.equalsIgnoreCase(weapons[i])) {
					valid = true;
					break;
				}else {
					valid = false;
				}
			}
			
			if(!valid) {
				ui.displayString(weapon + " is not a valid weapon");
			}
		}while(!valid);
		
		//The room they are in
		ui.displayString(player.currPlayer(curr) + ": " + "I suggest it was done in[room]");
		ui.displayString(player.currPlayer(curr) + ": "+ rooms[player.getTile(curr).getRoom() - 1]);
		
		weaponTeleport(weapon, player.getTile(curr).getRoom());
		
		ui.display();
	
	}
	
	/**
	 * Teleports weapon token, based on player's suggestion
	 * @param name
	 * @param room
	 */
	public void weaponTeleport(String name, int room) {
		Tile currTile = items.get(name).getPosition();		//Tile of current weapon
		boolean invalidRoom = true;						

		if(currTile.getRoom() != room) {
			
			//Looks for the position in which a weapon is positioned
			for(int i = 0; i < grid.map.length; i++) {
				//Quick escape from nested for loop if the value is found early.
				if(!invalidRoom) {
					break;
				}
				for(int j = 0; j < grid.map[i].length; j++) {
					if(grid.map[i][j].getSlot() == 6 && grid.map[i][j].getRoom() == room && invalidRoom) {
						currTile = grid.map[i][j];
						invalidRoom = items.getSameTile(currTile);
					}
				}
			}
		}
		items.get(name).moveBy(currTile); //Move weapon to new position
	}
}