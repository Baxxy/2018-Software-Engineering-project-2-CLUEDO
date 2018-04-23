package bots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import gameengine.*;

public class Bot2 implements BotAPI {

    // The public API of Bot must not change
    // This is ONLY class that you can edit in the program
    // Rename Bot to the name of your team. Use camel case.
    // Bot may not alter the state of the board or the player objects
    // It may only inspect the state of the board and the player objects

    private Player player;
    private PlayersInfo playersInfo;
    private Map map;
    private Dice dice;
    private Log log;
    private Deck deck;
    private int squaresMoved = 0;
    private int pathLeft;
    private Random rand = new Random();
    private String mapDirections[] = {"u", "d", "l", "r"};
    private Boolean hasAccused = false;
    private ArrayList<Coordinates> path;
    private String goToRoom = null;
    private boolean accuse = false;
    private boolean hasRolled = false;
    private int switchX = 1;


    public Bot2(Player player, PlayersInfo playersInfo, Map map, Dice dice, Log log, Deck deck) {
        this.player = player;
        this.playersInfo = playersInfo;
        this.map = map;
        this.dice = dice;
        this.log = log;
        this.deck = deck;
    }

    public String getName() {
        return "MAGA2"; // must match the class name
    }

    public String getVersion() {
        return "0.1";   // change on a new release
    }

    public String getCommand() {

        if (player.getToken().isInRoom()) {
            pathLeft = 0;
            goToRoom = null;
        }

        if (goToRoom == null) {
            if (getUnseenWeapons().size() > 1 && getUnseenTokens().size() > 1) {
                goToRoom = getRoomCard();
            } else if (accuse) {
                goToRoom = "Cellar";
            } else {
                goToRoom = getRandomRoomCard();
            }
        }


        if (player.getToken().isInRoom()) {
            if (player.getToken().getRoom().hasPassage()) {
                if (goToRoom.equals(
                        player.getToken().getRoom().getPassageDestination().toString())) {
                    try {
                        Thread.sleep(5000);
                    }catch (Exception ex){
                        System.out.println(ex);
                    }
                    return "passage";

                }
            }
        }


        if (getUnseenRooms().size() == 1 && getUnseenTokens().size() == 1
                && getUnseenWeapons().size() == 1) {
            System.out.println("TIME TO ACCUSE");
            System.out.println(getUnseenRooms().get(0));
            System.out.println(getUnseenTokens().get(0));
            System.out.println(getUnseenWeapons().get(0));
            accuse = true;
        } else {
            System.out.println(
                    "Remaining: " + getUnseenTokens().size() + "," + getUnseenWeapons().size() + ","
                            + getUnseenRooms().size());
        }

        //Has the player rolled their dice for the start of the round
        if (!hasRolled) {
            //resets (start of turn)
            hasAccused = false;
            hasRolled = true;
            squaresMoved = 0;
            return "roll";
        } else if (!map.isCorridor(player.getToken().getPosition())
                && player.getToken().getRoom().accusationAllowed()) {
            return "accuse";
        } else if (!map.isCorridor(player.getToken().getPosition()) && squaresMoved > 0) {
            if (!hasAccused) {
                System.out.println("I'm in a room can accuse");
                // accuse
                hasAccused = true;
                return "question";
            } else { //Player already questioned, nothing left to do
                hasRolled = false;
                return "done";
            }
        }//In room just rolled, so he can leave the room or passage
        else if (!map.isCorridor(player.getToken().getPosition()) && squaresMoved == 0) {
            //passage
            //exit
            //(might need to incooperate to A*)
            hasRolled = false;
            return "done";
        }

        //resets
        hasRolled = false;
        return "done";
    }

    private ArrayList<String> getUnseenRooms() {
        ArrayList<String> unseenRooms = new ArrayList<>();
        for (String room : Names.ROOM_CARD_NAMES) {
            if (!player.hasCard(room) && !player.hasSeen(room)) {
                unseenRooms.add(room);
            }
        }
        return unseenRooms;
    }


    private ArrayList<String> getUnseenTokens() {
        ArrayList<String> unseenTokens = new ArrayList<>();
        for (String token : Names.SUSPECT_NAMES) {
            if (!player.hasCard(token) && !player.hasSeen(token)) {
                unseenTokens.add(token);
            }
        }
        return unseenTokens;
    }

    private ArrayList<String> getUnseenWeapons() {
        ArrayList<String> unseenWeapons = new ArrayList<>();
        for (String weapon : Names.WEAPON_NAMES) {
            if (!player.hasCard(weapon) && !player.hasSeen(weapon)) {
                unseenWeapons.add(weapon);
            }
        }
        return unseenWeapons;
    }


    private String getRoomCard() {
        for (Card card : player.getCards()) {
            for (String room : Names.ROOM_CARD_NAMES) {
                System.out.println("Comparing:" + card + room);
                if (card.toString().equals(room)) {
                    return card.toString();
                }
            }
        }

        return Names.ROOM_CARD_NAMES[rand.nextInt(Names.ROOM_CARD_NAMES.length)];
    }

    private String getRandomRoomCard() {
        ArrayList<String> rooms = new ArrayList<>();
        for (String room : Names.ROOM_CARD_NAMES) {
            if (getUnseenRooms().contains(room)) {
                rooms.add(room);
            }
        }
        return rooms.get(rand.nextInt(rooms.size()));
    }

    public String getMove() {


        Coordinates playerPosition = player.getToken().getPosition();
        if (pathLeft == 0) {
            System.out.println(player.getName() + "is moving towards room: " + goToRoom);
            path = calculatePath(player.getToken().getPosition(),
                    map.getRoom(goToRoom).getDoorCoordinates(0));
            pathLeft += path.size();
        }

        if (path.size() == 0) {
            // When the AI tries to go back into the room it is in.
            Coordinates up = map.getNewPosition(playerPosition, "u");
            Coordinates down = map.getNewPosition(playerPosition, "d");
            Coordinates left = map.getNewPosition(playerPosition, "l");
            Coordinates right = map.getNewPosition(playerPosition, "r");

            if (map.isDoor(up, playerPosition)) {
                path.add(playerPosition);
                path.add(up);
            } else if (map.isDoor(down, playerPosition)) {
                path.add(playerPosition);
                path.add(down);
            } else if (map.isDoor(left, playerPosition)) {
                path.add(playerPosition);
                path.add(left);
            } else if (map.isDoor(right, playerPosition)) {
                path.add(playerPosition);
                path.add(right);
            }

            System.out.println("Path: " + path);
        }
        String randMove = getDirection(player.getToken().getPosition(),
                path.remove(path.size() - 1));
        System.out.println("Direction:" + randMove);
        pathLeft--;
        squaresMoved += 1;
        return randMove;

    }


    private ArrayList<Coordinates> calculatePath(Coordinates s, Coordinates e) {

        BZAstar pathFinder = new BZAstar(24, 25);
        ArrayList<Coordinates> path = pathFinder.calculateAStarNoTerrain(s, e);
        return path;
    }

    private String getDirection(Coordinates start, Coordinates end) {

        System.out.println("Moving from: " + start + " to: " + end);
        if (start.getRow() < end.getRow()) {
            return "d";
        } else if (start.getRow() > end.getRow()) {
            return "u";
        } else if (start.getCol() > end.getCol()) {
            return "l";
        } else if (start.getCol() < end.getCol()) {
            return "r";
        }

        return null;
    }

    public String getSuspect() {
        String suspect = Names.SUSPECT_NAMES[0]; //Default

        if (accuse) {
            return getUnseenTokens().get(0);
        }

        //Ask random cards as long as its not seen
        do {
            suspect = Names.SUSPECT_NAMES[rand.nextInt(Names.SUSPECT_NAMES.length)];

            //If last turn he bluffed, next card is not a bluff
            if (!player.hasCard(suspect) && switchX == 0) {
                switchX = 1;
            }
        } while (player.hasSeen(suspect) && switchX != 0);

        //If you bluffed last turn, you cant bluff again
        if (player.hasCard(suspect)) {
            System.out.println("Just bluffed haha");
            switchX = 0;
        } else {
            switchX = 1;
        }

        return suspect;
    }

    public String getWeapon() {
        // Add your code here
        if (accuse) {
            return getUnseenWeapons().get(0);
        }
        ArrayList<String> unseen = getUnseenWeapons();
        return unseen.get(rand.nextInt(unseen.size()));
    }

    public String getRoom() {
        if (accuse) {
            return getUnseenRooms().get(0);
        }
        // Add your code here
        return Names.ROOM_NAMES[0];
    }

    public String getDoor() {
        // Add your code here
        return "1";
    }

    public String getCard(Cards matchingCards) {

        // Basic strategy for getCard. Returns room if possible since they are harder to access.
        // Then returns suspect, then weapon rather arbitrarily.
        boolean cardFound = false;
        String bestChoice = matchingCards.get().toString();
        for (String room : Names.ROOM_NAMES) {
            for (Card card : matchingCards) {
                if (card.hasName(room)) {
                    bestChoice = card.toString();
                    cardFound = true;
                }
            }
        }
        if (!cardFound) {
            for (String suspect : Names.SUSPECT_NAMES) {
                for (Card card : matchingCards) {
                    if (card.hasName(suspect)) {
                        bestChoice = card.toString();
                        cardFound = true;
                    }
                }
            }
        }
        if (!cardFound) {
            for (String weapon : Names.WEAPON_NAMES) {
                for (Card card : matchingCards) {
                    if (card.hasName(weapon)) {
                        bestChoice = card.toString();
                    }
                }
            }
        }
        return bestChoice;
    }

    public void notifyResponse(Log response) {
        // Add your code here


    }

    public void notifyPlayerName(String playerName) {
        // Add your code here
        System.out.println("PLAYER NAME:" + playerName);


    }

    public void notifyTurnOver(String playerName, String position) {
        // Add your code here
        System.out.println(playerName + " " + position);

    }

    public void notifyQuery(String playerName, String query) {
        // Add your code here
        System.out.println(playerName + query);


    }

    public void notifyReply(String playerName, boolean cardShown) {
        // Add your code here
    }


    class BZAstar {

        private final int width;
        private final int height;

        private final HashMap<String, AStarNode> nodes = new HashMap<>();

        @SuppressWarnings("rawtypes")
        private final Comparator fComparator = new Comparator<AStarNode>() {
            public int compare(AStarNode a, AStarNode b) {
                return Integer.compare(a.getFValue(), b.getFValue()); //ascending to get the lowest
            }
        };

        public BZAstar(int width, int height) {
            this.width = width;
            this.height = height;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    String pointS = "(" + x + "," + y + ")";
                    Coordinates point = new Coordinates(x, y);
                    this.nodes.put(pointS, new AStarNode(point));
                }
            }


        }

        @SuppressWarnings("unchecked")
        public ArrayList<Coordinates> calculateAStarNoTerrain(Coordinates p1, Coordinates p2) {

            List<AStarNode> openList = new ArrayList<AStarNode>();
            List<AStarNode> closedList = new ArrayList<AStarNode>();

            AStarNode currentNode, destNode;
            destNode = nodes.get("(" + p2.getCol() + "," + p2.getRow() + ")");
            currentNode = nodes.get("(" + p1.getCol() + "," + p1.getRow() + ")");
            currentNode.parent = null;
            currentNode.setGValue(0);
            openList.add(currentNode);


            while (!openList.isEmpty()) {

                Collections.sort(openList, this.fComparator);
                currentNode = openList.get(0);
                if (currentNode.point.equals(destNode.point)) {
                    return this.calculatePath(destNode);
                }

                if (!map.isCorridor(currentNode.point) && map.getRoom(
                        currentNode.point).toString().equals(
                        map.getRoom(destNode.point).toString())) {
                    return this.calculatePath(currentNode);
                }

                openList.remove(currentNode);
                closedList.add(currentNode);

                for (String direction : mapDirections) {
                    Coordinates adjPoint = map.getNewPosition(currentNode.point, direction);
                    if (!this.isInsideBounds(adjPoint)) {
                        continue;
                    }
                    AStarNode adjNode = nodes.get(
                            "(" + adjPoint.getCol() + "," + adjPoint.getRow() + ")");
                    if (!map.isValidMove(currentNode.point, direction)) {
                        continue;
                    }

                    if (!closedList.contains(adjNode)) {
                        if (!openList.contains(adjNode)) {
                            adjNode.parent = currentNode;
                            adjNode.calculateGValue(currentNode);
                            adjNode.calculateHValue(destNode);
                            openList.add(adjNode);
                        } else {
                            if (adjNode.gValue < currentNode.gValue) {
                                adjNode.calculateGValue(currentNode);
                                currentNode = adjNode;
                            }
                        }
                    }
                }
            }

            return null;
        }

        private ArrayList<Coordinates> calculatePath(AStarNode destinationNode) {
            ArrayList<Coordinates> path = new ArrayList<Coordinates>();
            AStarNode node = destinationNode;
            while (node.parent != null) {
                path.add(node.point);
                node = node.parent;
            }
            return path;
        }

        private boolean isInsideBounds(Coordinates point) {
            return point.getCol() >= 0 &&
                    point.getCol() < this.width &&
                    point.getRow() >= 0 &&
                    point.getRow() < this.height;
        }


    }


    class AStarNode {

        public final Coordinates point;

        public AStarNode parent;

        public int gValue; //points from start
        public int hValue; //distance from target

        private final int MOVEMENT_COST = 10;

        public AStarNode(Coordinates point) {
            this.point = point;
        }

        /**
         * Used for setting the starting node value to 0
         */
        public void setGValue(int amount) {
            this.gValue = amount;
        }

        public void calculateHValue(AStarNode destPoint) {
            this.hValue = (Math.abs(point.getCol() - destPoint.point.getCol()) + Math.abs(
                    point.getRow() - destPoint.point.getRow())) * this.MOVEMENT_COST;
        }

        public void calculateGValue(AStarNode point) {
            this.gValue = point.gValue + this.MOVEMENT_COST;
        }

        public int getFValue() {
            return this.gValue + this.hValue;
        }
    }

}

