package objects;

import java.io.Serializable;

import geometry.Point;
import consts.ANSI;

abstract class GameObject implements Serializable { //we should never just have a generic GameObject but it has useful functionality that we would lose if it was an interface
  char symbol;
  ANSI color;
  Point location;
  String name;
  
  public GameObject(char s, ANSI c, String name) {
    this.symbol = s;
    this.color = c;
    this.location = new Point(-1, -1);
    this.name = name;
  }
  public GameObject(char s, ANSI c, Point l, String name) {
    this.symbol = s;
    this.color = c;
    this.location = l;
    this.name = name;
  }

  public String toString() {
    return this.color.toString()+this.symbol+ANSI.RESET;
  }

  public void setSymbol(char c) {
    this.symbol = c;
  }
  public char getSymbol() {
    return this.symbol;
  }
  
  public void setColor(ANSI s) {
    this.color = s;
  }
  public ANSI getColor() {
    return this.color;
  }
  public String getColorString() {
    return this.color.toString();
  }

  void setLocation(Point loc) {
    this.location = loc;
  }
  public Point getLocation() {
    return this.location;
  }

  public void setName(String name) {
    this.name = name;
  }
  public String getName() {
    return this.name;
  }
}