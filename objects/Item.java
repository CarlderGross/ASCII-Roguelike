package objects;

import geometry.Point;
import consts.ANSI;
import structure.Level;

public class Item extends GameObject {
  public Item(String n, char s, ANSI c) {
    super(s, c, n);
  }

  public void setLocation(Point loc, Level level) {
    level.tileAt(loc).removeItem(this);
    super.setLocation(loc);
    level.tileAt(loc).addItem(this);
  }
}