import java.util.Scanner;
import java.util.ArrayList;
import java.util.InputMismatchException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;

import java.io.File;

//for some stupid reason, there doesn't seem to be a clear way to detect key presses from the command line
//I would have to create an entire window and interface in order to detect key presses
//I don't want to do that, so I have to settle for scanner which can't read non-text

import geometry.Point;

import objects.Player;
import objects.Item;
import objects.Tile;
import objects.Door;
import objects.Stairs;
import objects.Creature;

import structure.GameSystem;

import consts.ANSI;
import consts.BASETILES;

//basic movement distance: diagonals are a regular move, therefore a+b=2c, therefore c = (a+b)/2
//the diagonal distance is just the average of the horizontal and vertical distances
//this is known as Chebyshev distance

//TODO: ADD FEATURES
//dual wielding
//shields
//saved map (greyed out when previously seen but not currently visible)
//monster variety
//Amulet of Yendor & winning

public class Main {
  //public static Level level = new Level();
  public static Player play = new Player(20, new ArrayList<Item>(), ANSI.MAGENTA);

  static int actions = 0;
    
  static void clearScreen() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }
  static void redrawScreen(GameSystem game) {
    clearScreen();
    System.out.print(game.getLevel());
    System.out.println(play.status());
    System.out.println("Moves: "+actions);
    if (game.hasMessages()) {
      game.printMessages();
    }
  }

  static Point locByChar(char in, Point start) {
    switch(in) {
      case '8': return new Point(start, 0, 1);
      case '7': return new Point(start, -1, 1);
      case '4': return new Point(start, -1, 0);
      case '1': return new Point(start, -1, -1);
      case '2': return new Point(start, 0, -1);
      case '3': return new Point(start, 1, -1);
      case '6': return new Point(start, 1, 0);
      case '9': return new Point(start, 1, 1);
      default: return new Point(start, 0, 0);
    }
  }
  static Point locByChar(char in) {
    return locByChar(in, play.getLocation());
  }

  static void saveGame(GameSystem game) {
    play.save();
    game.getLevel().save();
  }

  static void deleteSave() {
    File playerFile = new File("save/player.ser");
    playerFile.delete();
    File levelFolder = new File("save/levels");
    for (File f : levelFolder.listFiles()) {
      f.delete();
    }
    System.out.println("Save deleted.");
  }
  
  public static void main(String[] args) throws ClassNotFoundException, IOException {
    //initialize scanner
    Scanner scan = new Scanner(System.in);

    try {
      play.load();
    } catch (FileNotFoundException|InvalidClassException f) {
      //if there is no existing player, allow the user to customize the playerd
      boolean colorSelected = false;
      while (!colorSelected) {
        System.out.println("Please select a color for your character:");
        System.out.println(ANSI.RED.toString()+"1. Red "+ANSI.GREEN+"2. Green "+ANSI.YELLOW+"3. Yellow "+ANSI.BLUE+"4. Blue "+ANSI.PURPLE+"5. Purple "+ANSI.CYAN+"6. Cyan "+ANSI.MAGENTA+"7. Magenta"+ANSI.RESET);
        int colorSelect;
        try {
          colorSelect = scan.nextInt();
        }
        catch (InputMismatchException e) {
          colorSelect = -1;
        }
        scan.nextLine(); //throw away anything left over from the scan so it doesn't confuse things later
        switch(colorSelect) {
          case 1: play.setColor(ANSI.RED);
            colorSelected = true;
            System.out.println("You have selected "+ANSI.RED+"red"+ANSI.RESET+".");
            break;
          case 2: play.setColor(ANSI.GREEN);
            colorSelected = true;
            System.out.println("You have selected "+ANSI.GREEN+"green"+ANSI.RESET+".");
            break;
          case 3: play.setColor(ANSI.YELLOW);
            colorSelected = true;
            System.out.println("You have selected "+ANSI.YELLOW+"yellow"+ANSI.RESET+".");
            break;
          case 4: play.setColor(ANSI.BLUE);
            colorSelected = true;
            System.out.println("You have selected "+ANSI.BLUE+"blue"+ANSI.RESET+".");
            break;
          case 5: play.setColor(ANSI.PURPLE);
            colorSelected = true;
            System.out.println("You have selected "+ANSI.PURPLE+"purple"+ANSI.RESET+".");
            break;
          case 6: play.setColor(ANSI.CYAN);
            colorSelected = true;
            System.out.println("You have selected "+ANSI.CYAN+"cyan"+ANSI.RESET+".");
            break;
          case 7: play.setColor(ANSI.MAGENTA);
            colorSelected = true;
            System.out.println("You have selected "+ANSI.MAGENTA+"magenta"+ANSI.RESET+".");
            break;
          default: System.out.println("Invalid selection. Please select a color based on the number associated with that color.");
            colorSelected = false;
            break;
        }
      }
      System.out.println("Please input a name for yourself.");
      String charName = scan.nextLine();
      play.setName(charName);
    }

    //initialize game system
    GameSystem game = new GameSystem();
    game.addMessage("Welcome to the dungeon! Your goal is to find the Amulet of Yendor. For gameplay help, type #help or submit an empty line.");
    
    //initialize the level
    game.load(play);
    //System.out.println(play.getLocation());

    if (!(game.getLevel().tileAt(play.getLocation()).getCreature() instanceof Player)) {
      deleteSave();
      scan.close();
      System.out.println("Level loaded in invalid state! The nonfunctional save has been deleted. Please restart the game.");
      System.exit(1);
      //throw new IllegalStateException("Player loaded in non-matching tile");
    }
    else {
      game.getLevel().tileAt(play.getLocation()).setCreature(play);
    }

    //intialize action variables
    boolean actionTaken;
    String actionIn;
    Point selectedLoc = play.getLocation(); //necessary for the inspect action

    //initialize visibility
    game.getLevel().updateVisibility(play);
    
    //throw new Exception("Stop here and find the FOV bugs");

    //main gameplay loop
    while (play.isAlive()) { //the actual game loop
      redrawScreen(game);
      game.clearMessages();

      //accept player actions
      actionTaken = false;
      actionIn = "";
      while (!actionTaken) {
        actionIn += scan.nextLine();
        if (actionIn.isEmpty()) {
          System.out.println("No action submitted! Defaulting to '#help'");
          actionIn = "#help";
        }
        switch(actionIn.charAt(0)) {
          case 'o': // open or close
            Point doorloc = null;
            if (actionIn.length() > 1) {
              doorloc = locByChar(actionIn.charAt(1));
              if (game.getLevel().openDoorAt(doorloc)) {
                game.getLevel().updateVisibility(play);
                actionTaken = true;
              }
              else {
                System.out.println("No door in that direction!");
                actionIn = ""; //reset action value to allow you to take a different action
              }
            }
            else {
              System.out.println("Open door in what direction?");
            }
            break;

          //movement, using numpad or wasd
          case '7', '8', '9', '4', '6', '1', '2', '3':
            //System.out.println("Moving...");
            if (play.moveTo(locByChar(actionIn.charAt(0)), game.getLevel())) {
              actionTaken = true;
              game.getLevel().updateVisibility(play);
            }
            else {
              System.out.println("You can't move there!");
              actionIn = "";
            }
            break;

          case 'd': //descend or ascend
            if (game.getLevel().tileAt(play.getLocation()) instanceof Stairs) {
              Stairs stair = (Stairs)game.getLevel().tileAt(play.getLocation());
              if (stair.useStairs(game, play)) {
                game.getLevel().updateVisibility(play);
                actionTaken = true;
              }
              else {
                redrawScreen(game);
                actionIn = "";
              }
            }
            else {
              System.out.println("No stairs here!");
              actionIn = "";
            }
            break;

            
          case 'g': //get item
            if (game.getLevel().tileAt(play.getLocation()).getItems().size() == 0) {
              System.out.println("No items here!");
              actionIn = "";
            }
            else if (actionIn.length() > 1) {
              int objIndex = -1;
              try {
                objIndex = Integer.parseInt(actionIn.substring(1));
              } 
              catch(NumberFormatException e) {
                System.out.println("Can't get item: invalid selector!");
                actionIn = "g";
              }
              if ( (objIndex <= game.getLevel().tileAt(play.getLocation()).getItems().size()) && (objIndex >= 0 ) ) {
                Item newItem = game.getLevel().tileAt(play.getLocation()).getItems().get(objIndex);
                play.pickUp(newItem);
                game.addMessage("Got "+newItem.getName()+"!");
                game.getLevel().tileAt(play.getLocation()).getItems().remove(objIndex);
                actionTaken = true;
              }
            }
            else if (game.getLevel().tileAt(play.getLocation()).getItems().size() == 1) {
              Item newItem = game.getLevel().tileAt(play.getLocation()).getItems().get(0);
              play.pickUp(newItem);
              game.addMessage("Got "+newItem.getName()+"!");
              game.getLevel().tileAt(play.getLocation()).getItems().remove(0);
              actionTaken = true;
            }
            else {
              System.out.println("Select an item to get:");
              System.out.println(game.getLevel().tileAt(play.getLocation()).itemListString());
            }
            break;

          case '?':
            boolean inspectExited = false;
            game.getLevel().tileAt(selectedLoc).deselect();
            selectedLoc = play.getLocation();
            if (actionIn.length() > 1) {
              for (int i = 1; i < actionIn.length(); i++) {
                if (actionIn.charAt(i) == 'x') {
                  actionIn = "";
                  game.getLevel().tileAt(selectedLoc).deselect();
                  redrawScreen(game);
                  inspectExited = true;
                  break;
                }
                else {
                  if (locByChar(actionIn.charAt(i), selectedLoc).getX() > 0 && locByChar(actionIn.charAt(i), selectedLoc).getY() > 0) {
                    selectedLoc = locByChar(actionIn.charAt(i), selectedLoc);
                  }
                }
              }
            }
            if (!inspectExited) {
              game.getLevel().tileAt(selectedLoc).select();
              redrawScreen(game);
              System.out.println("Inspecting "+game.getLevel().tileAt(selectedLoc)+" at "+selectedLoc);
              if (game.getLevel().tileAt(selectedLoc).isVisible()) {
                if (game.getLevel().tileAt(selectedLoc).getCreature() != null) {
                  System.out.println("Creature:\n"+game.getLevel().tileAt(selectedLoc).getCreature()+" - "+game.getLevel().tileAt(selectedLoc).getCreature().getName());
                }
                if (!game.getLevel().tileAt(selectedLoc).getItems().isEmpty()) {
                  System.out.println("Items:");
                  System.out.println(game.getLevel().tileAt(selectedLoc).itemListString());
                }
                System.out.println("Tile:\n"+game.getLevel().tileAt(selectedLoc).getColorString()+game.getLevel().tileAt(selectedLoc).getSymbol()+ANSI.RESET+" : "+game.getLevel().tileAt(selectedLoc).getName());
              }
              else {
                System.out.println("You can't tell what's here!");
              }
              System.out.println("Move your cursor using the movement keys, or press 'x' to exit inspection mode.");
            }
            break;
            
          case 'i': //inventory
            boolean invExited = false;
            boolean didThing = false; //you can make any number of inventory adjustments in one action, but it still costs an action. Simply checking your inventory is free.
            String invAction = "";
            while (!invExited) {
              play.printInventory(); //-1 and not hands means no item is selected
              if (invAction.length() < 1) {
                System.out.println("(e)quip an item, (d)rop an item, or e(x)it inventory?");
              }
              invAction += scan.nextLine();
              //TODO: remember to parse the actual action
              if (invAction.charAt(0) == 'x') {
                invExited = true;
              }
              else if (invAction.length() > 1) {
                if (invAction.charAt(1) == 'm' || invAction.charAt(1) == 'M') {
                  if (play.getHandItem(0) == null) {
                    System.out.println("No item in main hand!");
                    invAction = "";
                  }
                  else if (invAction.charAt(0) == 'e') { //equip (unequip)
                    game.addMessage("Unequipped "+play.getHandItem(0).getName());
                    play.moveToPack(0);
                    invAction = "";
                    didThing = true;
                  }
                  else if (invAction.charAt(0) == 'd') { //drop
                    game.addMessage("Dropped "+play.getHandItem(0).getName());
                    play.dropFromIndex(0, true, game.getLevel());
                    invAction = "";
                    didThing = true;
                  }
                }
                else if (invAction.charAt(1) == 'o' || invAction.charAt(1) == 'O') {
                  if (play.getHandItem(1) == null) {
                    System.out.println("No item in off hand!");
                    invAction = "";
                  }
                  else if (invAction.charAt(0) == 'e') { //equip (unequip)
                    game.addMessage("Unequipped "+play.getHandItem(1).getName());
                    play.moveToPack(1);
                    invAction = "";
                    didThing = true;
                  }
                  else if (invAction.charAt(0) == 'd') { //drop
                    game.addMessage("Dropped "+play.getHandItem(1).getName());
                    play.dropFromIndex(1, true, game.getLevel());
                    invAction = "";
                    didThing = true;
                  }
                }
                else {
                  try {
                    int index = Integer.parseInt(invAction.substring(1));
                    if (invAction.charAt(0) == 'e') { //equip (unequip)
                      game.addMessage("Equipped "+play.getPackItem(index).getName());
                      play.equipFromPack(index);
                      invAction = "";
                      didThing = true;
                    }
                    else if (invAction.charAt(0) == 'd') { //drop
                      game.addMessage("Dropped "+play.getPackItem(index).getName());
                      play.dropFromIndex(index, false, game.getLevel());
                      invAction = "";
                      didThing = true;
                    }
                  } catch (NumberFormatException|IndexOutOfBoundsException e) {
                    System.out.println("Unknown item!");
                    invAction = "";
                  }
                }
              }
              else {
                if (invAction.charAt(0) == 'e') {
                  System.out.println("Equip what?");
                }
                else if (invAction.charAt(0) == 'd') {
                  System.out.println("Drop what?");
                }
                else {
                  System.out.println("Unknown action!");
                  invAction = "";
                }
              }
            }
            if (didThing) {
              actionTaken = true;
            }
            redrawScreen(game);
            actionIn = "";
            break;
            
          case 'a': //attack
            Point atkLoc = null;
            if (actionIn.length() > 1) {
              Creature target = game.getLevel().tileAt(locByChar(actionIn.charAt(1))).getCreature();
              if (target != null) {
                play.attack(target, game);
                actionTaken = true;
              }
              else {
                System.out.println("No monster in that direction!");
                actionIn = ""; //reset action value to allow you to take a different action
              }
            }
            else {
              System.out.println("Attack in what direction?");
            }
            break;

          case '#': //long command
            if (actionIn.substring(1).trim().equals("help")) {
              System.out.println(
                "Actions:\n"
                +"Numpad: move in the appropriate direction (if numpad does not work properly, try turning on num lock)\n"
                +"o: open or close door\n"
                +"g: get item in your space\n"
                +"i: open inventory\n"
                +"?: inspect a location\n"
                +"d: descend or ascend stairs\n"
                +"a: attack monster\n"
                +"#save: save game\n"
                +"#quit: quit game\n"
                +"#clearsave: delete save file (to restart the game)\n"
                +"\n"
                +"Common tiles:\n"
                +ANSI.BROWN+"■"+ANSI.RESET+" Open door\n"
                +ANSI.BROWN+"+"+ANSI.RESET+" Closed door\n"
                +"| - Wall\n"
                +". Empty floor\n"
                +"< Stairs up\n"
                +"> Stairs down\n"
                +play+" You!\n"
                +"Most grey symbols represent terrain, most colored symbols represent items, and most letters represent creatures.\n"
                +"\nSubmit another action when you are done reading.\n"
              );
            }
            else if (actionIn.substring(1).trim().equals("clear")) {
              redrawScreen(game);
            }
            else if (actionIn.substring(1).trim().equals("save")) {
              saveGame(game);
            }
            else if (actionIn.substring(1).trim().equals("quit") || actionIn.substring(1).trim().equals("exit")) {
              saveGame(game); //don't let them quit without saving, otherwise they will be sad
              System.exit(0); //exit code zero: exited normally
            }
            else if (actionIn.substring(1).trim().equals("clearsave") || actionIn.substring(1).trim().equals("wipesave")) {
              deleteSave();
              System.exit(0);
            }
            else if (actionIn.substring(1, 5).equals("edit")) {
              boolean editExited = false;
              game.getLevel().tileAt(selectedLoc).deselect();
              selectedLoc = play.getLocation();
              if (actionIn.length() > 1) {
                //System.out.println(actionIn);
                for (int i = 5; i < actionIn.length(); i++) {
                  //System.out.println(actionIn.charAt(i));
                  if (actionIn.charAt(i) == 'x') {
                    actionIn = "";
                    game.getLevel().save(); //don't need to save anything else because the level is the only thing that got edited
                    game.getLevel().tileAt(selectedLoc).deselect();
                    redrawScreen(game);
                    editExited = true;
                    break;
                  }
                  else if (!locByChar(actionIn.charAt(i), selectedLoc).equals(new Point(selectedLoc, 0, 0))) {
                    selectedLoc = locByChar(actionIn.charAt(i), selectedLoc);
                  }
                  else if (actionIn.charAt(i) == 'w') {
                    System.out.println("Placing wall");
                    game.getLevel().setTileAt(selectedLoc, new Tile(BASETILES.WALL));
                    game.getLevel().updateVisibility(play);
                    actionIn = actionIn.substring(0, i)+actionIn.substring(i+1);
                    i--;
                  }
                  else if (actionIn.charAt(i) == 'd') {
                    System.out.println("Placing door");
                    game.getLevel().setTileAt(selectedLoc, new Door(false));
                    game.getLevel().updateVisibility(play);
                    actionIn = actionIn.substring(0, i)+actionIn.substring(i+1);
                    i--;
                  }
                  else if (actionIn.charAt(i) == 'f') {
                    System.out.println("Placing floor");
                    game.getLevel().setTileAt(selectedLoc, new Tile(BASETILES.FLOOR));
                    game.getLevel().updateVisibility(play);
                    actionIn = actionIn.substring(0, i)+actionIn.substring(i+1);
                    i--;
                  }
                  else {
                    System.out.println("Unknown edit action!");
                  }
                }
              }
              if (!editExited) {
                game.getLevel().tileAt(selectedLoc).select();
                redrawScreen(game);
                System.out.println("Move your cursor using the movement keys, or press 'x' to exit editing mode. Type the first letter of the tile you want to change the tile's type.");
              }
              break;
            }
            else {
              System.out.println("Unknown long command!");
            }
            actionIn = "";
            break;
            
          default: System.out.println("Unknown action!");
            actionIn = "";
        }
      }
      actions++;
      game.runMonsters(play);
    }
    redrawScreen(game);
    System.out.println("You have died!");
    scan.close();
  }
}