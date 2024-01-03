package geometry;

//NOTE: Approximate a vertical line using Integer.MAX_VALUE

public class Sector implements Cloneable {
  //a sector of a circle (i.e. that pie slice thing)
  
  Point center;
  double upperSlope;
  double lowerSlope;
  //effectively two lines in point-slope form which share the same point
  //y-y1 = m(x-x1)    remember y and x are variables and y1 and x1 are the point coords
  
  public Sector() { //creates a 45 degree sector centered on the origin by default
    this.center = new Point(0, 0);
    this.upperSlope = 1;
    this.lowerSlope = 0;
  }
  
  public Sector(Point center, double upperSlope, double lowerSlope) {
    this.center = center;
    this.upperSlope = upperSlope;
    this.lowerSlope = lowerSlope;
  }
  
  //important note: if upperSlope < lowerSlope it just means that the sector points towards negative x

  public double getUpperSlope() {
    return this.upperSlope;
  }
  public double getLowerSlope() {
    return this.lowerSlope;
  }
  public Point getCenter() {
    return this.center;
  }

  public void setUpperSlope(double newSlope) {
    this.upperSlope = newSlope;
  }
  public void setUpperThroughPoint(Point p) {
    //slope = rise/run
    this.upperSlope = (p.getY()-this.center.getY()) / (p.getX()-this.center.getX());
  }
  public void setLowerSlope(double newSlope) {
    this.lowerSlope = newSlope;
  }
  public void setLowerThroughPoint(Point p) {
    this.lowerSlope = (p.getY()-this.center.getY()) / (p.getX()-this.center.getX());
  }

  public double getYOnUpper(double x) {
    //y = m(x-x1)+y1
    //center is (x1, y1) and then we calculate y from x
    return (this.upperSlope*(x-this.center.getX()) + this.center.getY());
  }
  public double getYOnLower(double x) {
    return (this.lowerSlope*(x-this.center.getX()) + this.center.getY());
  }

  public double getXOnUpper(double y) {
    //(y-y1)/m + x1 = x
    //center is (x1, y1)
    return ((y-this.center.getY())/this.upperSlope) + this.center.getX();
  }
  public double getXOnLower(double y) {
    return ((y-this.center.getY())/this.lowerSlope) + this.center.getX();
  }
  
  public boolean inSector(Point p) {
    //special case: slope of max possible value should represent vertical line
    if ((upperSlope == Double.MAX_VALUE || upperSlope == -Double.MAX_VALUE) && (p.getX() == center.getX()) && p.getY() >= center.getY()) {
      return true;
    }
    else if ((lowerSlope == Double.MAX_VALUE || lowerSlope == -Double.MAX_VALUE) && (p.getX() == center.getX()) && (p.getY() <= center.getY())) {
      return true;
    }
    
    if (p.getY() >= getYOnLower(p.getX())) {
      if (p.getY() <= getYOnUpper(p.getX())) {
        //if the point is above the lower line and below the upper line then it's in the sector
        //sector edges are included
        return true;
      }
    }

    //provide tolerance of +- 0.0001 on the lines to handle rounding errors
    //since the edges are included in the sector, anything within 0.0001 of them should also be included as well
    //I don't really need any more precision than the ten-thousandths place
    if (Math.abs(p.getY()-getYOnLower(p.getX())) < 0.0001 || Math.abs(p.getY()-getYOnUpper(p.getX())) < 0.0001) {
      return true;
    }
    
    return false;
  }
  public boolean inSector(double x, double y) {
    return inSector(new Point(x, y));
  }

  public boolean inSectorExcludeEdges(Point p) {
    if (p.getY() > getYOnLower(p.getX())) {
      if (p.getY() < getYOnUpper(p.getX())) {
        //if the point is above the lower line and below the upper line then it's in the sector
        //sector edges are excluded
        return true;
      }
    }
    return false;
  }
  public boolean inSectorExcludeEdges(double x, double y) {
    return inSectorExcludeEdges(new Point(x, y));
  }

  public String toString() {
    return "Upper: y-"+this.center.y+" = "+this.upperSlope+"(x-"+this.center.x+") | Lower: y-"+this.center.y+" = "+this.lowerSlope+"(x-"+this.center.x+")";
  }

  public Sector clone() {
    return new Sector(center, upperSlope, lowerSlope);
  }

  public boolean equals(Sector other) {
    if (this.center.equals(other.getCenter()) && this.upperSlope == other.getUpperSlope() && this.lowerSlope == other.getLowerSlope()) {
      return true;
    }
    return false;
  }
}