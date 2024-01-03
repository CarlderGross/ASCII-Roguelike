package objects;

import java.util.ArrayList;

import geometry.Point;
import consts.ANSI;
import consts.BASETILES;

public class Tile extends GameObject {
  
  ArrayList<Item> objsOnTile = new ArrayList<Item>();
  Creature creatureOnTile = null;
  boolean solid; //whether you can move through it
  boolean transparent; /*whether you can see through it
  examples:
  transparent + non-solid: floor
  transparent + solid: iron bars
  non-transparent + non-solid: fog
  non-transparent + solid: wall
  */
  boolean seen = false;
  transient boolean selected = false;
  transient boolean visible = false;
  
 public Tile(String t, boolean so, boolean trans, char sy) {
    super(sy, ANSI.RESET, t);
    this.solid = so;
    this.transparent = trans;
  }
  public Tile(String t, boolean so, boolean trans, char sy, Point loc) {
    this(t, so, trans, sy);
    this.location = loc;
  }
  public Tile(BASETILES tile) {
    this(tile.type, tile.solid, tile.transparent, tile.symbol);
  }
  public Tile(BASETILES tile, Point loc) {
    this(tile);
    this.location = loc;
  }

  public boolean isSolid() {
    return solid;
  }
  public boolean isTransparent() {
    return transparent;
  }
  public boolean isVisible() {
    return visible;
  }

  public boolean isSelected() {
    return selected;
  }
  public void select() {
    this.selected = true;
  }
  public void deselect() {
    this.selected = false;
  }

  public void addItem(Item i) {
    this.objsOnTile.add(i);
  }
  public void removeItem(Item i) {
    this.objsOnTile.remove(i);
  }
  public ArrayList<Item> getItems() {
    return this.objsOnTile;
  }
  
  public void setCreature(Creature c) {
    this.creatureOnTile = c;
  }
  public Creature getCreature() {
    return this.creatureOnTile;
  }

  public String itemListString() {
    String output = "";
    for (int i = 0; i < this.objsOnTile.size(); i++) {
      output += i+") "+this.objsOnTile.get(i)+" : "+this.objsOnTile.get(i).name;
    }
    return output;
  }

  public String toString() {
    if (!this.visible) {
      if (!this.selected) {
        return " ";
      }
      return ""+ANSI.INVERT+" "+ANSI.RESET;
    }
    if (creatureOnTile != null) { //creature should always go on top
      if (this.selected) {
        return creatureOnTile.color.toString()+ANSI.INVERT+creatureOnTile.symbol+ANSI.RESET;
      }
      return creatureOnTile.toString();
    }
    else if (objsOnTile.isEmpty()) { //if there are no objects, just print the tile
      if (this.selected) {
        return super.color.toString()+ANSI.INVERT+this.symbol+ANSI.RESET;
      }
      return super.toString();
    }
    else { //when there is no creature and yes objects then print an object
      if (this.selected) {
        return objsOnTile.get(objsOnTile.size()-1).color.toString()+ANSI.INVERT+objsOnTile.get(objsOnTile.size()-1).symbol+ANSI.RESET;
      }
      return objsOnTile.get(objsOnTile.size()-1).toString(); //should print the most recent object to be added to the tile, which would be 'on top of the pile'
    }
  }

  public void setLocation(Point loc) { //tiles are the only class in which setLocation should be accessed directly
    super.setLocation(loc);
  }
  public void setVisibility(boolean visible) {
    this.visible = visible;
  }
}