package bots;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

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
    private int logSizeCounter = 0;
    private HashMap<String, HashMap<String, ArrayList<Integer>>> guessGame = new HashMap<>();
    private HashMap<String, Integer> answerCounter = new HashMap<>();
    private ArrayList<String> privateSeen = new ArrayList<>();
    private String[] found = {null, null, null};

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

        if (guessGame.isEmpty()) {
            for (String s : playersInfo.getPlayersNames()) {
                answerCounter.put(s, 0);
                if (s.equals(player.getName())) {
                    continue;
                }
                HashMap<String, ArrayList<Integer>> cardMap = new HashMap<>();
                for (String y : Names.SUSPECT_NAMES) {
                    cardMap.put(y, null);
                }
                for (String y : Names.WEAPON_NAMES) {
                    cardMap.put(y, null);
                }
                for (String y : Names.ROOM_CARD_NAMES) {
                    cardMap.put(y, null);
                }
                System.out.println(cardMap);
                guessGame.put(s, cardMap);
            }
        }


        if (!log.isEmpty()) {
            int count = 0;
            ArrayList<String> tmp = new ArrayList<>();
            for (String s : log) {
                tmp.add(s);
                count++;

            }

            while (tmp.size() != count - logSizeCounter) {
                tmp.remove(0);
            }

            analyseLog(tmp);

            logSizeCounter += count - logSizeCounter;

        }

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


        if (player.getToken().isInRoom() && !hasRolled) {
            if (player.getToken().getRoom().hasPassage()) {
                if (goToRoom.equals(
                        player.getToken().getRoom().getPassageDestination())) {
                    //hasRolled = true;
                    JOptionPane.showMessageDialog(null, "used passage");
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
                    "===================  Remaining: BOT2 ==================="
                            + getUnseenTokens().size() + "," + getUnseenWeapons().size() + ","
                            + getUnseenRooms().size());
            System.out.println(privateSeen);
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
        if (found[2] != null) {
            unseenRooms.add(found[2]);
            return unseenRooms;
        }

        for (String room : Names.ROOM_CARD_NAMES) {
            if (!player.hasCard(room) && !hasSeen(room)) {
                unseenRooms.add(room);
            }
        }

        if (unseenRooms.size() == 0) {
            System.exit(39);
        }
        return unseenRooms;
    }


    private ArrayList<String> getUnseenTokens() {
        ArrayList<String> unseenTokens = new ArrayList<>();
        if (found[0] != null) {
            unseenTokens.add(found[0]);
            return unseenTokens;
        }
        for (String token : Names.SUSPECT_NAMES) {
            if (!player.hasCard(token) && !hasSeen(token)) {
                unseenTokens.add(token);
            }
        }
        if (unseenTokens.size() == 0) {

            System.out.println("Player's Cards:");
            for (Card c : player.getCards()) {
                System.out.println(c.toString());
            }
            System.out.println("All of em");
            for (String token : Names.SUSPECT_NAMES) {
                System.out.println(
                        token + " hasseen?" + hasSeen(token) + " playerseen?" + player.hasSeen(
                                token) + " hascard?" + player.hasCard(token));
            }
            System.out.println("Private seen:");
            System.out.println(privateSeen);
            System.exit(29);
        }
        return unseenTokens;
    }

    private ArrayList<String> getUnseenWeapons() {
        ArrayList<String> unseenWeapons = new ArrayList<>();
        if (found[1] != null) {
            unseenWeapons.add(found[1]);
            return unseenWeapons;
        }
        for (String weapon : Names.WEAPON_NAMES) {
            if (!player.hasCard(weapon) && !hasSeen(weapon)) {
                unseenWeapons.add(weapon);
            }
        }
        if (unseenWeapons.size() == 0) {
            System.exit(19);
        }
        return unseenWeapons;
    }


    private void analyseLog(ArrayList<String> logx) {

        for (int i = 0; i < logx.size(); i++) {
            System.out.println(logx.get(i));
            if (logx.get(i).contains("questioned")) {
                int z = i + 1;
                System.out.println(logx.get(z));
                if (logx.get(z).contains("showed")) {
                    String token = logx.get(i).split("with", 2)[0].trim();
                    token = token.split("about", 2)[1].trim();
                    String rest = logx.get(i).split("with the ", 2)[1];
                    String room = rest.split("in the", 2)[1];
                    room = room.substring(1, room.length() - 1);
                    String weapon = rest.split(" in", 2)[0];
                    String user = logx.get(z).split(" ", 2)[0];
                    System.out.println(user + "XD" + token + "xd" + weapon + "XD" + room + "XD");
                    learn(user, token, weapon, room);
                } else {
                    System.out.print(logx.get(z));
                    String token = logx.get(i).split("with", 2)[0].trim();
                    token = token.split("about", 2)[1].trim();
                    String rest = logx.get(i).split("with the ", 2)[1];
                    String room = rest.split("in the", 2)[1];
                    room = room.substring(1, room.length() - 1);
                    String weapon = rest.split(" in", 2)[0];
                    String user = logx.get(z).split(" ", 2)[0];
                    System.out.println(user + "XD" + token + "xd" + weapon + "XD" + room + "XD");
                    System.out.println(user + "Doesnt have :" + token + weapon + room);
                    removeGuess(user, token, weapon, room);
                }

            }
        }
    }


    public void removeGuess(String user, String token, String weapon, String room){
        if(!user.equals(player.getName())) {
            if (guessGame.get(user).get(token) != null) {
                if (guessGame.get(user).get(token).size() != 0) {
                    guessGame.get(user).put(token, new ArrayList<Integer>());
                }
            }

            if (guessGame.get(user).get(weapon) != null) {
                if (guessGame.get(user).get(weapon).size() != 0) {
                    guessGame.get(user).put(weapon, new ArrayList<Integer>());
                }
            }

            if (guessGame.get(user).get(weapon) != null) {
                if (guessGame.get(user).get(weapon).size() != 0) {
                    guessGame.get(user).put(weapon, new ArrayList<Integer>());
                }
            }
        }

        reloadGuessMap();
    }

    private void learn(String user, String token, String weapon, String room) {

        if (!user.equals(player.getName())) {
            int currentToken = answerCounter.get(user) + 1;
            int counter = 0;
            String singleValue = "";


            if (!player.hasCard(token)) {
                if (guessGame.get(user).get(token) != null && guessGame.get(user).get(token).size()
                        == 0) {
                } else {
                    ArrayList<Integer> privateList = new ArrayList<>();
                    if (guessGame.get(user).get(token) != null) {
                        privateList = guessGame.get(user).get(token);
                    }
                    privateList.add(currentToken);

                    System.out.println("Added " + token + privateList);
                    guessGame.get(user).put(token, privateList);
                    singleValue = token;
                    counter++;

                }
            }

            if (!player.hasCard(weapon)) {
                if (guessGame.get(user).get(weapon) != null && guessGame.get(user).get(weapon).size()
                        == 0) {
                } else {
                    ArrayList<Integer> privateList = new ArrayList<>();
                    privateList.clear();
                    if (guessGame.get(user).get(weapon) != null) {
                        privateList = guessGame.get(user).get(weapon);
                    }
                    privateList.add(currentToken);
                    System.out.println("Added " + weapon + privateList);
                    guessGame.get(user).put(weapon, privateList);
                    singleValue = weapon;
                    counter++;
                }
            }

            if (!player.hasCard(room)) {
                if (guessGame.get(user).get(room) != null && guessGame.get(user).get(room).size()
                        == 0) {
                } else {
                    ArrayList<Integer> privateList = new ArrayList<>();
                    privateList.clear();
                    if (guessGame.get(user).get(room) != null) {
                        privateList = guessGame.get(user).get(room);
                    }
                    privateList.add(currentToken);
                    guessGame.get(user).put(room, privateList);
                    System.out.println("Added " + room + privateList);
                    singleValue = room;
                    counter++;
                }
            }

            answerCounter.put(user, currentToken);


            if (counter == 1) {
                if (!privateSeen.contains(singleValue)) {
                    privateSeen.add(singleValue);
                    System.out.println("SEEN:");
                    System.out.println(privateSeen);
                    for (String s : Names.ROOM_CARD_NAMES) {
                        if (player.hasSeen(s)) {
                            System.out.println(s);
                        }
                    }
                    for (String s : Names.SUSPECT_NAMES) {
                        if (player.hasSeen(s)) {
                            System.out.println(s);
                        }
                    }
                    for (String s : Names.WEAPON_NAMES) {
                        if (player.hasSeen(s)) {
                            System.out.println(s);
                        }
                    }
                    System.out.println("Player's cards:");
                    for (Card c : player.getCards()) {
                        System.out.print("[" + c.toString() + "]");
                    }
                    System.out.println(
                            "EHM?\n\n\n\\n\\n\n\n????" + singleValue + room + weapon + token);
                    reloadGuessMap();
                    //System.exit(67);
                }
            } else {
                System.out.println(counter);
            }
            //System.out.println(guessGame);
            System.out.println(answerCounter);
        }

        System.out.println(guessGame);
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

        ArrayList<String> unseen = getUnseenTokens();
        String suspect = Names.SUSPECT_NAMES[0]; //Default

        if (accuse) {
            System.out.println(privateSeen);
            System.out.println(guessGame);
            return getUnseenTokens().get(0);
        }

        if (!accuse && getUnseenTokens().size() == 1) {
            String randCard = Names.SUSPECT_NAMES[rand.nextInt(Names.SUSPECT_NAMES.length)];
            for (String s : Names.SUSPECT_NAMES) {
                if (player.hasCard(s)) {
                    return s;
                }
            }
            return unseen.get(rand.nextInt(unseen.size()));
        }

        return unseen.get(rand.nextInt(unseen.size()));
    }

    public String getWeapon() {

        ArrayList<String> unseen = getUnseenWeapons();
        // Add your code here
        if (accuse) {
            return getUnseenWeapons().get(0);
        }

        if (!accuse && getUnseenWeapons().size() == 1) {
            String randCard = Names.WEAPON_NAMES[rand.nextInt(Names.WEAPON_NAMES.length)];
            for (String s : Names.WEAPON_NAMES) {
                if (player.hasCard(s)) {
                    return s;
                }
            }
            return unseen.get(rand.nextInt(unseen.size()));
        }
        return unseen.get(rand.nextInt(unseen.size()));
    }

    public String getRoom() {
        if (accuse) {
            return getUnseenRooms().get(0);
        }
        // Add your code here
        System.exit(0);
        ArrayList<String> unseen = getUnseenRooms();
        return unseen.get(rand.nextInt(unseen.size()));
    }

    public String getDoor() {
        // Add your code here
        int i = 0;

        ArrayList<Coordinates> doorPath = calculatePath(
                player.getToken().getRoom().getDoorCoordinates(i),
                map.getRoom(goToRoom).getDoorCoordinates(i));
        ArrayList<Coordinates> tmp = new ArrayList<Coordinates>();

        //Finds best path between my current room doors and the next room doors
        for (; i < player.getToken().getRoom().getNumberOfDoors(); i++) {

            for (int j = 0; j < map.getRoom(goToRoom).getNumberOfDoors(); j++) {
                tmp = calculatePath(player.getToken().getRoom().getDoorCoordinates(i),
                        map.getRoom(goToRoom).getDoorCoordinates(j));

                if (doorPath.size() > tmp.size()) {
                    doorPath = tmp;

                }
            }
        }
        return Integer.toString(i);
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
        String user = "";
        String cardShown = "";
        Boolean saw = false;
        for (String s : response) {
            if (s.contains("showed")) {
                saw = true;
                System.out.println(s);
                user = s.split(" ", 2)[0];
                System.out.println("User: " + user);
                cardShown = s.split(": ", 2)[1];
                cardShown = cardShown.substring(0, cardShown.length() - 1);
                System.out.println("Card: " + cardShown);
            }
        }

        if (saw) {
            updateGuessMap(user, cardShown);
            reloadGuessMap();
        } else {
            String token = "";
            String weapon = "";
            String room = "";
            Boolean foundQ = false;
            for (String c : response) {
                System.out.println(c);
                if (c.contains("questioned")) {
                    foundQ = true;
                    token = c.split("with", 2)[0].trim();
                    token = token.split("about", 2)[1].trim();
                    String rest = c.split("with the ", 2)[1];
                    room = rest.split("in the", 2)[1];
                    room = room.substring(1, room.length() - 1);
                    weapon = rest.split(" in", 2)[0];
                    System.out.println(user + "XD" + token + "xd" + weapon + "XD" + room + "XD");
                }
            }
            if (foundQ) {
                if (!player.hasCard(token)) {
                    found[0] = token;
                }
                if (!player.hasCard(weapon)) {
                    found[1] = weapon;
                }
                if (!player.hasCard(room)) {
                    found[2] = room;
                }
            }

        }

    }

    public void updateGuessMap(String user, String card) {
        for (String u : playersInfo.getPlayersNames()) {
            if (u.equals(user) || u.equals(player.getName())) {
                continue;
            } else if (guessGame.get(u).get(card) != null) {
                guessGame.get(u).put(card, new ArrayList<Integer>());
                System.out.println("Updated value of card " + card + " on user " + u);
            }
        }
    }

    public void reloadGuessMap() {
        System.out.println(guessGame);
        String current = "";
        for (String c : playersInfo.getPlayersNames()) {
            System.out.println(c + " IS MY NAME !");
            int currentCount = answerCounter.get(c);
            if (c != player.getName()) {
                for (int i = 1; i <= currentCount; i++) {
                    Iterator<java.util.Map.Entry<String, ArrayList<Integer>>> it = guessGame.get(
                            c).entrySet().iterator();
                    int d = 0;
                    String found = "";
                    while (it.hasNext()) {
                        java.util.Map.Entry<String, ArrayList<Integer>> pair =
                                (java.util.Map.Entry) it.next();
                        if (pair.getValue() != null && pair.getValue().contains(i)) {
                            d++;
                            found = pair.getKey();
                        }
                    }

                    if (d == 1) {
                        if (!privateSeen.contains(found)) {
                            JOptionPane.showMessageDialog(null, "We've found" + found);
                            privateSeen.add(found);
                        }
                    }
                }

            }
        }

    }

    public void notifyPlayerName(String playerName) {
        // Add your code here
        System.out.println("PLAYER NAME:" + playerName);
    }


    public boolean hasSeen(String card) {
        if (player.hasSeen(card) || privateSeen.contains(card)) {
            return true;
        }
        return false;
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

