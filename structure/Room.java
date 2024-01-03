package structure;

import java.util.Random;

import geometry.Rectangle;
import geometry.Point;

class Room extends Rectangle { //part of level generation
  boolean connected;
  boolean spawnsMonsters;

  public Room(Random rand, int maxX, int maxY) {
    super(rand.nextInt(maxX-3), rand.nextInt(maxY-3), rand.nextInt(7)+3, rand.nextInt(7)+3); //dimensions between 3 and 10, inclusive
    this.connected = false;
    this.spawnsMonsters = true;
  }
  public Room(Point origin, int width, int height) {
    super(origin, width, height);
    this.connected = false;
    this.spawnsMonsters = true;
  }
  public Room(Point origin, int width, int height, boolean mons) {
    super(origin, width, height);
    this.connected = false;
    this.spawnsMonsters = false;
  }

  public boolean isConnected() {
    return connected;
  }
  public void setConnected(boolean newVal) {
    this.connected = newVal;
  }

  public boolean spawnsMons() {
    return this.spawnsMonsters;
  }
}