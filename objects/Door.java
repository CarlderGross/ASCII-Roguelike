package objects;

import geometry.Point;
import consts.ANSI;

public class Door extends Tile {
  
  public Door(boolean open) {
    super("door", !open, open, '+');
    if (open) {
      this.symbol = '■';
      this.name = "door (open)";
    }
    else {
      this.name = "door (closed)";
    }
    this.color = ANSI.BROWN;
  }
  public Door(boolean open, Point loc) {
    super("door", !open, open, '+', loc);
    if (open) {
      this.symbol = '■';
      this.name = "door (open)";
    }
    else {
      this.name = "door (closed)";
    }
    this.color = ANSI.BROWN;
  }

  public void changeState() {
    this.solid = !this.solid;
    this.transparent = !this.solid;
    if (!this.solid) { //open doors are a square (ASCII 254)
      this.symbol = '■';
      this.name = "door (open)";
    }
    else { //closed doors are a plus sign
      this.symbol = '+';
      this.name = "door (closed)";
    }
  }
}