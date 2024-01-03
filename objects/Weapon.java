package objects;

import consts.ANSI;

public class Weapon extends Item {
  int dmg;
  
  public Weapon(String n, int d, char s, ANSI c) {
    super(n, s, c);
    this.dmg = d;
  }

  public int getDamage() {
    return dmg;
  }
}