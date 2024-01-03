package geometry;

public class Rectangle {
  Point origin; //lower left corner
  int width;
  int height;

  public Rectangle(Point o, int w, int h) {
    this.origin = o;
    this.width = w;
    this.height = h;
  }
  public Rectangle(double x, double y, int w, int h) {
    this.origin = new Point(x, y);
    this.width = w;
    this.height = h;
  }

  public Point getCenter() {
    return new Point(this.origin.getX()+(this.width/2.0), this.origin.getY()+(this.height/2.0));
  }

  public int getWidth() {
    return this.width;
  }
  public int getHeight() {
    return this.height;
  }
  public Point getOrigin() {
    return this.origin;
  }

  public void setWidth(int newWidth) {
    this.width = newWidth;
  }
  public void setHeight(int newHeight) {
    this.height = newHeight;
  }
}