package geometry;

import java.io.Serializable;

public class Point implements Serializable {
  double x;
  double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }
  public Point(Point original, double xDiff, double yDiff) {
    this.x = original.getX() + xDiff;
    this.y = original.getY() + yDiff;
  }

  public double getX() {
    return this.x;
  }
  public double getY() {
    return this.y;
  }

  public String toString() {
    return "("+this.x+","+this.y+")";
  }

  public boolean equals(Point other) {
    if (this.x == other.getX() && this.y == other.getY()) {
      return true;
    }
    return false;
  }

  public double distanceFrom(Point other) {
    //uses Chebyshev distance (diagonal = side) since it takes the same number of actions to travel diagonally n squares as to travel orthagonally that many
    return Math.max(Math.abs(this.getX()-other.getX()), Math.abs(this.getY()-other.getY()));
  }
}