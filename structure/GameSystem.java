package structure;

import java.util.ArrayList;
import java.util.Random;

import objects.Player;

public class GameSystem {
  private static final String SAVEPATH = "save/";
  private static final String SAVEFILE = "levelNum.bin";
  
  private Random rand;
  Level level;
  
  ArrayList<String> messages;

  public GameSystem() {
    this.messages = new ArrayList<String>();
    this.rand = new Random();
    this.level = new Level();
  }

  public int rollDie(int sides) {
    return rand.nextInt(sides)+1; //integer from 1 to sides, inclusive
  }

  public Random getRand() {
    return this.rand;
  }

  public Level getLevel() {
    return this.level;
  }

  public void runMonsters(Player play) {
    this.level.runMonsters(play, this);
  }

  public void load(Player play) throws ClassNotFoundException {
    this.level.load(play.getCurrentLevel(), play);
  }

  public boolean changeLevel(Player play, boolean up) throws ClassNotFoundException {
    this.level.tileAt(play.getLocation()).setCreature(null); //take the player out of the level
    this.level.save();
    if (up && this.level.getNum()-1 >= 0) { //can't go higher than level zero
      this.addMessage("Climbing stairs!");
      this.level = new Level();
      this.level.load(this.level.getNum()-1, play);
      this.level.tileAt(play.getLocation()).setCreature(play); //put the player back
      return true;
    }
    else if (!up && this.level.getNum()+1 < 101) { //can't go lower than level 100
      this.addMessage("Descending stairs!");
      this.level = new Level();
      this.level.load(this.level.getNum()+1, play);
      this.level.tileAt(play.getLocation()).setCreature(play); //put the player back
      return true;
    }
    else {
      this.addMessage("A mysterious force prevents your passage!");
      this.level.tileAt(play.getLocation()).setCreature(play); //put the player back
      return false;
    }
  }

  public void addMessage(String message) {
    messages.add(message);
  }
  public void clearMessages() {
    messages.clear();
  }

  public boolean hasMessages() {
    if (messages.size() > 0) {
      return true;
    }
    return false;
  }
  
  public void printMessages() {
    for (String message : messages) {
      System.out.print(message);
      System.out.print(" ");
    }
    System.out.print("\n");
  }
}