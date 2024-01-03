package objects;

import java.util.ArrayList;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;

import consts.ANSI;
import structure.*;
import geometry.Point;

public class Player extends Creature {
  private static final String SAVEPATH = "save/";
  private static final String SAVEFILE = "player.ser";
  
  ArrayList<Item> pack;
  Item[] hands = new Item[2];
  int currentLevel;
  
  public Player(int h, ArrayList<Item> i, ANSI c) {
    super(h, "Player", '@', c);
    pack = i;
    this.location = new Point(8, 9); //the starting tile of the game right now
    this.currentLevel = 0;
  }

  public void load() throws ClassNotFoundException, IOException {
    ObjectInputStream obj_in = new ObjectInputStream(new FileInputStream(this.SAVEPATH+this.SAVEFILE));
    Player in_player = (Player)obj_in.readObject();
    obj_in.close();
    this.symbol = in_player.symbol;
    this.color = in_player.color;
    this.location = in_player.location;
    this.name = in_player.name;
    this.pack = in_player.pack;
    this.hands = in_player.hands;
    this.currentLevel = in_player.currentLevel;
  }

  public String status() {
    String handString = " Hands: [";
    for (int i = 0; i < this.hands.length; i++) {
      if (this.hands[i] != null) {
        handString += this.hands[i];
      }
      else {
        handString += " ";
      }
      if (i < this.hands.length-1) {
        handString += " ";
      }
    }
    handString += "]";
    return this.name+"\tLevel: "+this.currentLevel+"\tLocation: "+this.location+"\nHP: "+this.hp+"\t"+handString+" Pack: "+this.pack;
  }

  public Item getHandItem(int index) {
    return this.hands[index];
  }
  public Item getPackItem(int index) {
    return this.pack.get(index);
  }
  
  public void moveToPack(int handIndex) {
    addToPack(this.hands[handIndex]);
    this.hands[handIndex] = null;
  }
  public void addToPack(Item i) {
    pack.add(i);
  }
  public void equipFromPack(int index) {
    Item i = pack.get(index);
    if (!(i instanceof Armor)) {
      if (hands[0] == null) {
        hands[0] = i;
        pack.remove(index);
      }
      else if (hands[1] == null) {
        hands[1] = i;
        pack.remove(index);
      }
    }
    //else, equip as armor, which isn't a thing yet
  }
  
  public void pickUp(Item i) {
    if (hands[0] == null) {
      hands[0] = i;
    }
    else if (hands[1] == null) {
      hands[1] = i;
    }
    else {
      this.addToPack(i);
    }
  }
  public void dropFromIndex(int index, boolean isHands, Level level) {
    if (isHands) {
      level.tileAt(this.location).addItem(this.hands[index]);
      this.hands[index] = null;
    }
    else {
      level.tileAt(this.location).addItem(this.pack.get(index));
      this.pack.remove(index);
    }
  }

  public void printInventory() { //arguments are to identify if an item has been selected by the player
    System.out.print("(M)ain hand: "+hands[0]+" ");
    if (hands[0] != null) {
      System.out.println(hands[0].getName());
    }
    else {
      System.out.println();
    }
    System.out.print("(O)ff Hand: "+hands[1]+" ");
    if (hands[1] != null) {
      System.out.println(hands[1].getName());
    }
    else {
      System.out.println();
    }
    System.out.println("Pack:");
    for (int i = 0; i < pack.size(); i++) {
      System.out.println(""+i+") "+pack.get(i)+" : "+pack.get(i).getName());
    }
    if (pack.size() == 0) {
      System.out.println("Empty");
    }
  }

  public int getArmorClass() {
    return 13; //TODO: have armor actually exist
  }
  public int getDamage() {
    if (hands[0] instanceof Weapon) {
      Weapon mainWep = (Weapon)hands[0];
      return mainWep.getDamage();
    }
    return 1;
  }
  public int getHitBonus() {
    return 0; //there should be a way to make this larger later
  }

  public void attack(Creature target, GameSystem game) {
    super.attack(target, this.getHitBonus(), this.getDamage(), game);
  }

  public void setLocation(Point loc, Level level) {
    super.setLocation(loc, level);
    this.currentLevel = level.getNum();
  }
  public void setLocation(double x, double y, Level level) {
    this.setLocation(new Point(x, y), level);
  }

  public int getCurrentLevel() {
    return this.currentLevel;
  }

  public void save() {
    try {
      //CREATE FILE IF IT DOESN'T EXIST
      File saveFile = new File(SAVEPATH+SAVEFILE);
      saveFile.createNewFile();
      
      //SAVE DATA TO THE FILE
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFile));
      out.writeObject(this);
      out.close();
      
      System.out.println("Player "+this.getName()+" saved.");
    } catch (IOException i) {
      i.printStackTrace();
    }
  }
}