package objects;

import consts.ANSI;

public class Armor extends Item {
  int ac;
  public Armor(String n, int d, char s, ANSI c) {
    super(n, s, c);
    this.ac = d;
  }
}