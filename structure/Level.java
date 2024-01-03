package structure;

import java.util.ArrayList;
import java.util.Random;

import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.InvalidClassException;

import geometry.Point;
import geometry.Sector;

import consts.*;
import objects.*;

public class Level implements Serializable {
  public static final String SAVEPATH = "save/levels/";
  private static final Random rand = new Random();
  
  static final Tile outOfBoundsDefault = new Tile("", true, false, ' ');

  private int levelNum = 0;
  
  Tile[][] tiles;
  /*
   * arranged as a row of columns, so conceptually printed on screen it looks like:
   * [
     0123
   * [[[[
   *3####
   *2####
   *1####
   *0####
   * ]]]]
   * ]
   * this way, list 1 is x and list 2 is y resulting in list[x][y]
    point (0,0) is therefore in the lower left corner
   */
  ArrayList<Monster> mons;

  //CONSTRUCTORS & INITIALIZATION
  public Level() {
    tiles = new Tile[70][26];
    mons = new ArrayList<Monster>();
  }
  public Level(int wide, int tall) {
    tiles = new Tile[wide][tall];
    mons = new ArrayList<Monster>();
  }

  public void generate(Player play) {
    //reset monster list
    mons = new ArrayList<Monster>();
    
    // make everything wall
    for (int x = 0; x < tiles.length; x++) {
      for (int y = 0; y < tiles[x].length; y++) {
        this.setTileAt(x, y, new Tile(BASETILES.WALL));
      }
    }

    if (levelNum == 0) { //level 0 is special
          /* 
           -----
           |..>|
           |g|.|
      -----|...|
      |...|-|.|
      |.@.+.+.|
      |...|----
      -----
*/
      this.setTileAt(7, 10, new Tile(BASETILES.FLOOR));
      this.setTileAt(7, 9, new Tile(BASETILES.FLOOR));
      this.setTileAt(7, 8, new Tile(BASETILES.FLOOR));
      this.setTileAt(8, 10, new Tile(BASETILES.FLOOR));
      this.setTileAt(8, 8, new Tile(BASETILES.FLOOR));
      this.setTileAt(9, 10, new Tile(BASETILES.FLOOR));
      this.setTileAt(9, 9, new Tile(BASETILES.FLOOR));
      this.setTileAt(9, 8, new Tile(BASETILES.FLOOR));
      this.setTileAt(10, 9, new Door(false));
      this.setTileAt(11, 9, new Tile(BASETILES.FLOOR));
      this.tileAt(11, 9).addItem(new Weapon("longsword", 8, '/', ANSI.GREY));

      this.setTileAt(12, 9, new Door(false));
      this.setTileAt(13, 9, new Tile(BASETILES.FLOOR));
      this.setTileAt(13, 10, new Door(false));

      this.setTileAt(12, 11, new Tile(BASETILES.FLOOR));
      this.setTileAt(13, 11, new Tile(BASETILES.FLOOR));
      this.setTileAt(14, 11, new Tile(BASETILES.FLOOR));
      this.setTileAt(12, 12, new Tile(BASETILES.FLOOR));
      this.setTileAt(14, 12, new Tile(BASETILES.FLOOR));
      this.setTileAt(12, 13, new Tile(BASETILES.FLOOR));
      this.setTileAt(13, 13, new Tile(BASETILES.FLOOR));
      this.setTileAt(14, 13, new Stairs(false));

      Monster boblin = BASEMONS.GOBLIN.getMon();
      boblin.setName("Boblin the goblin");
      this.spawnMonsterAt(boblin, new Point(12, 12));
    }
      
    else {
      //generate some random rooms
      ArrayList<Room> roomList = new ArrayList<Room>(8);
      int numRooms = rand.nextInt(6)+1; //between 1 and 6 rooms
      for (int i = 0; i < numRooms; i++) {
        roomList.add(new Room(rand, this.getWidth()-1, this.getHeight()-1));
      }

      //make stair rooms
      int stairsX = rand.nextInt(this.getWidth()/2);
      int stairsY = rand.nextInt(this.getHeight()/2);
      if (play.getLocation().getX() > this.getWidth()/2) {
        stairsX = this.getWidth()/2 - stairsX;
      }
      else {
        stairsX += this.getWidth()/2;
      }
      if (play.getLocation().getY() > this.getHeight()/2) {
        stairsY = this.getHeight()/2 - stairsY;
      }
      else {
        stairsY += this.getHeight()/2;
      }
      Point stairsLoc = new Point(stairsX, stairsY);
      
      roomList.add(new Room(new Point(stairsLoc, -1, -1), 3, 3, true)); //room for stairs down
      roomList.add(new Room(new Point(play.getLocation(), -1, -1), 3, 3, false)); //room for stairs up (note that monsters are not spawned)

      for (Room r : roomList) {
        //shrink the room down so that it's fully inside the level
        while (r.getOrigin().getX()+r.getWidth() > this.getWidth()-1) {
          r.setWidth(r.getWidth()-1);
        }
        while (r.getOrigin().getY()+r.getHeight() > this.getHeight()-1) {
          r.setHeight(r.getHeight()-1);
        }
        //make all tiles in the room floor
        for (int x = (int)r.getOrigin().getX(); (x < r.getOrigin().getX()+r.getWidth()); x++) {
          for (int y = (int)r.getOrigin().getY(); (y < r.getOrigin().getY()+r.getHeight()) && (y < this.getHeight()-1); y++) {
            setTileAt(x, y, new Tile(BASETILES.FLOOR));
          }
        }
        //find the closest room, only counting connected rooms if it isn't connected
        Room closest = roomList.get(0);
        for (Room other : roomList) {
          if (r.isConnected() || other.isConnected()) {
            if (other.getCenter().distanceFrom(r.getCenter()) < closest.getCenter().distanceFrom(r.getCenter())) {
              closest = other;
            }
          }
        }
        //connect it to that room (remember, only connected rooms were valid if the current room wasn't, so this should work fine)
        int xDir;
        if (closest.getCenter().getX() < r.getCenter().getX()) {
          xDir = -1;
        }
        else {
          xDir = 1;
        }
        int yDir;
        if (closest.getCenter().getY() < r.getCenter().getY()) {
          yDir = -1;
        }
        else {
          yDir = 1;
        }
        if ((closest.getOrigin().getY() < r.getOrigin().getY()+r.getHeight()) && (closest.getOrigin().getY()+closest.getHeight() > r.getOrigin().getY())) { //they are aligned horizontally (one is to the left or right of the other, such that if they were extended horizontally they would overlap)
          int y = (int)r.getCenter().getY();
          while ((y != (int)closest.getCenter().getY()) && (y < r.getOrigin().getY()+r.getHeight()) && (y > r.getOrigin().getY())) {
            y += yDir;
          }
          for (int x = (int)r.getCenter().getX(); x != (int)closest.getCenter().getX(); x += xDir) {
            this.setTileAt(x, y, new Tile(BASETILES.FLOOR));
          }
        }
        else if ((closest.getOrigin().getX() < r.getOrigin().getX()+r.getWidth()) && (closest.getOrigin().getX()+closest.getWidth() > r.getOrigin().getX())) {         //vertical align (one is above the other)
          int x = (int)r.getCenter().getX();
          while ((x != (int)closest.getCenter().getX()) && (x < r.getOrigin().getX()+r.getWidth()) && (x > r.getOrigin().getX())) {
            x += xDir;
          }
          for (int y = (int)r.getCenter().getY(); y != (int)closest.getCenter().getY(); y += yDir) {
            this.setTileAt(x, y, new Tile(BASETILES.FLOOR));
          }
        }
        else { //it's not aligned along either axis, so the hall will need to be angled
          int x = (int)r.getCenter().getX();
          int y = (int)r.getCenter().getY();
          while (x != (int)closest.getCenter().getX()) {
            this.setTileAt(x, y, new Tile(BASETILES.FLOOR));
            x += xDir;
          }
          while (y != (int)closest.getCenter().getY()) {
            this.setTileAt(x, y, new Tile(BASETILES.FLOOR));
            y += yDir;
          }
        }

        if (r.spawnsMons()) {
          //spawn some monsters in the room
          int numMons = (int)Math.ceil(Math.sqrt((double)(r.getWidth()*r.getHeight()))); //spawn a number of goblins equal to the square root of the area, rounded up
          for (int n = 0; n < numMons; n++) {
            Point spawnPoint = new Point(r.getOrigin(), rand.nextInt(r.getWidth()-1), rand.nextInt(r.getHeight()-1));
            while (this.tileAt(spawnPoint).getCreature() != null) { //reroll if it would spawn in an invalid location
              spawnPoint = new Point(r.getOrigin(), rand.nextInt(r.getWidth()-1), rand.nextInt(r.getHeight()-1));
            }
            this.spawnMonsterAt(BASEMONS.GOBLIN, spawnPoint);
          }
        }
      }

      this.setTileAt(stairsLoc, new Stairs(false));
    }

    //place player and player stairs
    this.setTileAt(play.getLocation(), new Stairs(true));
    play.setLocation(play.getLocation(), this); //make sure everything lines up

    //update the wall symbols
    for (int x = 0; x < tiles.length; x++) {
      for (int y = 0; y < tiles[x].length; y++) {
        updateTile(this.tileAt(x,y));
      }
    }
  }

  //FILESYSTEM
  public boolean save() {
    try {
      String filename = String.format("level%d.ser", this.levelNum);
      
      //CREATE FILE IF IT DOESN'T EXIST
      File saveFile = new File(SAVEPATH+filename);
      saveFile.createNewFile();
      
      //SAVE DATA TO THE FILE
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFile));
      out.writeObject(this);
      out.close();
      System.out.println("Level "+this.levelNum+" saved.");
      return true;
    } catch (IOException i) {
      i.printStackTrace();
      return false;
    }
  }
  public void load(int levelNum, Player play) throws ClassNotFoundException {
    String filepath = SAVEPATH + "level"+levelNum+".ser";
    this.levelNum = levelNum;
    try {
      ObjectInputStream obj_in = new ObjectInputStream(new FileInputStream(filepath));
      Level in_level = (Level)obj_in.readObject();
      obj_in.close();
      this.tiles = in_level.tiles;
      this.levelNum = in_level.levelNum;
      this.mons = in_level.mons;
    } catch (FileNotFoundException fileEx) {
      System.out.println("No level found, generating new level...");
      this.generate(play); //if there's no file to load, then we should just generate
    } catch (InvalidClassException inv) {
      System.out.println("Class data out of date, generating new level...");
      this.generate(play);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //STRUCTURE
  public int getWidth() {
    return tiles.length; //first array is x-axis
  }
  public int getHeight() {
    return tiles[0].length; //second array is y-axis
  }

  public int getNum() {
    return levelNum;
  }

  //TILES
  public Tile tileAt(Point loc) {
    if ((loc.getX() < 0) || (loc.getX() >= this.getWidth()) || (loc.getY() < 0) || loc.getY() >= this.getHeight()) { //if tile is out of bounds
      Tile result = outOfBoundsDefault;
      result.setLocation(loc);
      return result; //return a special out of bounds tile
    }
    //System.out.println(loc);
    return tiles[(int)loc.getX()][(int)loc.getY()];
  }
  public Tile tileAt(double x, double y) {
    return this.tileAt(new Point(x, y));
  }

  public void setTileAt(Point loc, Tile tile) {
    tiles[(int)loc.getX()][(int)loc.getY()] = tile;
    tile.setLocation(loc);
    //updateTilesSurrounding(loc); don't put this in, we need it for level generation where some tiles might be null
  }
  void setTileAt(int x, int y, Tile tile) {
    this.setTileAt(new Point(x, y), tile);
  }

  void updateTile(Tile t) {
    if (t.getName().equals("wall")) { //should only be done to walls right now
      Point loc = t.getLocation();
      if (
        (!tileAt(loc.getX()-1, loc.getY()).isSolid())
        || (!tileAt(loc.getX()+1, loc.getY()).isSolid())
      ) {
        t.setSymbol('|');
      }
      else if (
        (!tileAt(loc.getX(), loc.getY()-1).isSolid())
        || (!tileAt(loc.getX(), loc.getY()+1).isSolid())
      ) {
        t.setSymbol('-');
      }
      else if (
        (!tileAt(loc.getX()-1, loc.getY()-1).isSolid())
        || (!tileAt(loc.getX()+1, loc.getY()-1).isSolid())
        || (!tileAt(loc.getX()-1, loc.getY()+1).isSolid())
        || (!tileAt(loc.getX()+1, loc.getY()+1).isSolid())
      ) {
        t.setSymbol('-');
      }
      else {
        t.setSymbol('#');
      }
    }
  }

  void updateTilesSurrounding(Tile t) {
    for (double x = t.getLocation().getX()-1; x <= t.getLocation().getX()+1; x++) {
      for (double y = t.getLocation().getY()-1; y <= t.getLocation().getY()+1; y++) {
        updateTile(tileAt(x, y));
      }
    }
  }
  void updateTilesSurrounding(Point loc) {
    for (double x = loc.getX()-1; x <= loc.getX()+1; x++) {
      for (double y = loc.getY()-1; y <= loc.getY()+1; y++) {
        updateTile(tileAt(x, y));
      }
    }
  }

  public boolean openDoorAt(Point loc) {
    if (this.tileAt(loc) instanceof Door) {
      ((Door)this.tileAt(loc)).changeState(); //cast to door to access changeState() since we have just determined that it is in fact a door
      updateTilesSurrounding(loc);
      return true;
    }
    else {
      return false;
    }
  }

  //MONSTERS
  void spawnMonsterAt(BASEMONS mon, Point p) {
    mons.add(mon.spawnAt(p, this));
  }
  void spawnMonsterAt(Monster mon, Point p) {
    Monster out = mon.clone();
    if (this.tileAt(p).getCreature() != null) {
      //System.out.println(this.tileAt(p).getCreature());
      throw new IllegalStateException("Creature already exists at point "+p+"!");
    }
    else if (this.tileAt(p).isSolid()) {
      throw new IllegalStateException("Can't spawn creature inside solid wall!");
    }
    out.setLocation(p, this);
    mons.add(out);
  }
  public void runMonsters(Player play, GameSystem game) {
    //System.out.println("Running monsters...");
    monLoop: for (int i = 0; i < mons.size(); i++) {
      while (!mons.get(i).isAlive()) { //keeps checking the same spot until the monster passes, in order to catch after index update
        this.tileAt(mons.get(i).getLocation()).setCreature(null);
        mons.remove(i);
        if (mons.size() <= i) {
          break monLoop; //stop here, there aren't any more monsters
        }
      }
      Monster currentMon = mons.get(i);
      if (!currentMon.isAlerted() && tileAt(currentMon.getLocation()).isVisible()) {
        currentMon.alert();
      }
      currentMon.takeAction(play, this, game);
    }
  }

  //VISIBILITY
  public void updateVisibility(Player play) { //calculates which tiles are visible and which tiles are not visible for a specific creature, and returns the results as a boolean array equivalent to the level (i.e. each boolean in the array corresponds to the visibility of the corresponding tile in the level)
    /*SOURCES FOR ALGORITHM DESIGN:
     Björn Bergström -- http://www.roguebasin.com/index.php?title=FOV_using_recursive_shadowcasting
        For explaining the basic algorithm concept
     Albert Ford -- https://www.albertford.com/shadowcasting/
        For providing an easily usable implementation to reference behavior
     Adam Milazzo -- http://www.adammil.net/blog/v125_roguelike_vision_algorithms.html
        For explaining refinements to the algorithm to improve its functionality
*/
    /*
    a------b   
    |  /\  |  
    |i/__\j|
    |/|  |\| 
    |\|__|/|   
    |k\  /m|
    |  \/  |
    c------d
  in order to translate this onto a standard coordinate plane where c is any arbitrary point, simply add c to all points (i.e. if c is (6, 5) add 6 to all x-values and 5 to all y-values)

solution for sector boundaries: if centerpoint (player) is more down than left, move upper to leftmost corner in wall; if player is more left than down, move upper to downmost corner
Invert this when appropriate
In a square wall the downmost and leftmost corner would be the same but these walls are beveled

this is a variation of shadow casting algorithm which creates sectors (of a circle) out from the center of the player's tile and then narrows or splits the sectors based on encounters with walls
*/
    boolean[][] visibleTiles = new boolean[this.getWidth()][this.getHeight()];
    
    Point center = new Point(play.getLocation().getX()+0.5, play.getLocation().getY()+0.5); //centerpoint of the tile that the creature is on
    Sector[] sectors = {new Sector(center, Double.MAX_VALUE, 1), new Sector(center, 1, 0), new Sector(center, 0, -1), new Sector(center, -1, -Double.MAX_VALUE), new Sector(center, 1, Double.MAX_VALUE), new Sector(center, 0, 1), new Sector(center, -1, 0), new Sector(center, -Double.MAX_VALUE, -1)};
    /*
          \     |     / 
           \7777|0000/
           6\777|000/1
           66\77|00/11
           666\7|0/111
           6666\|/1111  
         -------@-------
           5555/|\2222  
           555/4|3\222
           55/44|33\22
           5/444|333\2
           /4444|3333\
          /     |     \ 

    0 and 3: row by row, left to right
    7 and 4: row by row, right to left
    1 and 6: column by column, bottom to top (upwards)
    2 and 5: column by column, top to bottom (downwards)
    All scans should be moving away from the origin
*/
    int xDir;
    int yDir;
    boolean rowByRow; //if false, column by column
    for (int sectorIndex = 0; sectorIndex < sectors.length; sectorIndex++) {
      //ASSIGN VALUES FOR SECTOR
      if (sectorIndex == 0 || sectorIndex == 3 || sectorIndex == 4 || sectorIndex == 7) {
        rowByRow = true;
      }
      else {
        rowByRow = false;
      }

      if (sectorIndex < 4) {
        xDir = 1;
      }
      else {
        xDir = -1;
      }

      if (sectorIndex < 2 || sectorIndex > 5) {
        yDir = 1;
      }
      else {
        yDir = -1;
      }

      scan(center, sectors[sectorIndex], rowByRow, xDir, yDir, visibleTiles, 0);
    }
    for (int x = 0; x < visibleTiles.length; x++) {
      for (int y = 0; y < visibleTiles[x].length; y++) {
        this.tileAt(x, y).setVisibility(visibleTiles[x][y]);
      }
    }
  }
  private void scan(Point center, Sector sect, boolean rowByRow, int xDir, int yDir, boolean[][] visibilities, double startDist) {
    //System.out.println("New scan!");
    Tile prevTile = null;//don't make it transparent, you fool
    if (rowByRow) {
      for (double y = center.getY()+startDist; y >= 0 && y < this.getHeight(); y+=yDir) {
        //System.out.println("rowbyrow yLoop");
        if (prevTile != null) { //prevTile is updated at the end of every x loop
          if (prevTile.isTransparent()) {
            prevTile = null; //reset for the new loop
          }
          else {
            //System.out.println("Terminating scan");
            break; //if the last tile wasn't transparent, this scan should be discarded
          }
        }
        double startX = center.getX();
        double endX = center.getX();
        if (yDir > 0) {
          startX = sect.getXOnUpper(y);
          endX = sect.getXOnLower(y);
        }
        else {
          startX = sect.getXOnLower(y);
          endX = sect.getXOnUpper(y);
        }
        //System.out.println("Start at "+startX);
        //System.out.println(sect.inSector(startX, y));
        for (double x = startX; (sect.inSector(new Point(x, y)) || sect.inSector(Math.floor(x+((1-xDir)/2)), y)) && (x >= 0 && x < this.getWidth()); x+=xDir) {
          /*System.out.println("UpperSlope: "+sect.getUpperSlope());
          System.out.println("LowerSlope: "+sect.getLowerSlope());
          System.out.println("Scanning "+new Point(x, y));*/
          
          if (this.tileAt(x, y).isTransparent()) {
            //System.out.println("Transparent!");
            
            if (prevTile != null && !prevTile.isTransparent()) {
              if (yDir > 0) {
                sect.setUpperThroughPoint(new Point(prevTile.getLocation(), (0.5*(1+xDir)), 0.5)); //0.5*(1+xDir) is 1 if xDir==1 and 0 if xDir==-1. This gives us the left side if x is right to left (negative) and the right side if x is left to right (positive).
              }
              else {
                sect.setLowerThroughPoint(new Point(prevTile.getLocation(), (0.5*(1+xDir)), 0.5));
              }
            }

            //catches problems where visibility would check the wrong tile for the visual box
            double squareX = x;
            if (!sect.inSector(x, y)) {
              squareX = endX;
            }
            
            Point[] innerSquare = {
              new Point(Math.floor(squareX) + 0.25, Math.floor(y) + 0.25),
              new Point(Math.floor(squareX) + 0.25, Math.floor(y) + 0.75),
              new Point(Math.floor(squareX) + 0.75, Math.floor(y) + 0.75),
              new Point(Math.floor(squareX) + 0.75, Math.floor(y) + 0.25)
            };
            //then make sure that the sector collides with the center square, and if it does, mark it as visible
            Point prev = innerSquare[0];
            for (Point p : innerSquare) {
              if (sect.inSector(p)) {
                //System.out.println("Visible!");
                visibilities[(int)x][(int)y] = true;
                break; //we don't need to loop through the points any more
              }
              else if ( (p.getY() > sect.getYOnUpper(p.getX()) && prev.getY() < sect.getYOnLower(prev.getX())) || (prev.getY() > sect.getYOnUpper(prev.getX()) && p.getY() < sect.getYOnLower(p.getX())) ) {
                //if the sector passes through one of the line segments, i.e. one endpoint is above the sector and one endpoint is below the sector, it must go through the box
                //this catches cases where the sector is so thin that it passes through the entire box without containing a corner
                //System.out.println("Visible!");
                visibilities[(int)x][(int)y] = true;
                break; //we don't need to loop through the points any more
              }
            }
          }
          else { //if the current tile is NOT transparent
            //System.out.println("Opaque!");
            if (prevTile != null && prevTile.isTransparent()) {
              Sector newSect = sect.clone();
              if (yDir > 0) {
                newSect.setLowerThroughPoint(new Point(prevTile.getLocation(), (0.5*(1+xDir)), 0.5)); //through center of left side, since this is not transparent and the previous tile was
              }
              else {
                newSect.setUpperThroughPoint(new Point(prevTile.getLocation(), (0.5*(1+xDir)), 0.5));
              }
              if (!(newSect.getUpperSlope() == newSect.getLowerSlope())) {
                scan(center, newSect, rowByRow, xDir, yDir, visibilities, y-center.getY()+yDir); //start at the current distance from the center plus one increment
              }
            }
            
            if (sect.inSectorExcludeEdges(new Point(x, y))) {
              visibilities[(int)x][(int)y] = true; //I feel fairly confident that there is no case where the sector won't intersect with the wall in this scenario
            }
            else {
              if ( (Math.floor(endX)==Math.floor(x) && endX!=Math.floor(x)) || (Math.floor(startX)==Math.floor(x) && startX!=Math.floor(x)) ) {
                visibilities[(int)x][(int)y] = true;
              }
            }
          }
          prevTile = this.tileAt(x, y);
        }
      }
    }
    else { //column by column
      //System.out.println("New sector!");
      for (double x = center.getX()+startDist; x >= 0 && x < this.getWidth(); x+=xDir) {
        //System.out.println("colbycol xLoop");
        if (prevTile != null) { //prevTile is updated at the end of every y loop
          if (prevTile.isTransparent()) {
            prevTile = null; //reset for the new loop
          }
          else {
            break; //if the last tile wasn't transparent, this scan should be discarded
          }
        }
        double startY = center.getY();
        double endY = center.getY();
        if (yDir > 0) {
          startY = sect.getYOnLower(x);
          endY = sect.getYOnUpper(x);
        }
        else {
          startY = sect.getYOnUpper(x);
          endY = sect.getYOnLower(x);
        }
        for (double y = startY; (sect.inSector(new Point(x, y)) || sect.inSector(new Point(x, Math.floor(y+((1-yDir)/2))))) && (y >= 0 && y < this.getHeight()); y+=yDir) {
          /*System.out.println("UpperSlope: "+sect.getUpperSlope());
          System.out.println("LowerSlope: "+sect.getLowerSlope());
          System.out.println("Scanning "+new Point(x, y));*/
          
          if (this.tileAt(x, y).isTransparent()) {
            //System.out.println("Transparent!");
            
            if (prevTile != null && !prevTile.isTransparent()) {
              //System.out.println("Previous was solid, updating slope");
              if (yDir > 0) {
                sect.setLowerThroughPoint(new Point(prevTile.getLocation(), 0.5, 1));
              }
              else {
                sect.setUpperThroughPoint(new Point(prevTile.getLocation(), 0.5, 0));
              }
              //then update start and end values
              if (yDir > 0) {
                startY = sect.getYOnLower(x);
                endY = sect.getYOnUpper(x);
              }
              else {
                startY = sect.getYOnUpper(x);
                endY = sect.getYOnLower(x);
              }
            }

            double squareY = y;
            if (!sect.inSector(x, y)) {
              squareY = endY; //catches problems where visibility would check the wrong tile for the visual box
            }
            
            Point[] innerSquare = {
              new Point(Math.floor(x) + 0.25, Math.floor(squareY) + 0.25),
              new Point(Math.floor(x) + 0.25, Math.floor(squareY) + 0.75),
              new Point(Math.floor(x) + 0.75, Math.floor(squareY) + 0.75),
              new Point(Math.floor(x) + 0.75, Math.floor(squareY) + 0.25)
            };
            Point prev = innerSquare[0];
            for (Point p : innerSquare) {
              if (sect.inSector(p)) {
                visibilities[(int)x][(int)y] = true;
                break; //we don't need to loop through the points any more
              }
              else if ( (p.getY() > sect.getYOnUpper(p.getX()) && prev.getY() < sect.getYOnLower(prev.getX())) || (prev.getY() > sect.getYOnUpper(prev.getX()) && p.getY() < sect.getYOnLower(p.getX())) ) {
                //if the sector passes through one of the line segments, i.e. one endpoint is above the sector and one endpoint is below the sector, it must go through the box
                //this catches cases where the sector is so thin that it passes through the entire box without containing a corner
                //System.out.println("Visible!");
                visibilities[(int)x][(int)y] = true;
                break; //we don't need to loop through the points any more
              }
            }
          }
          else { //not transparent
            //System.out.println("Opaque!");
            if (prevTile != null && prevTile.isTransparent()) {
              Sector newSect = sect.clone();
              if (yDir > 0) {
                newSect.setUpperThroughPoint(new Point(prevTile.getLocation(), 0.5, 1));
              }
              else {
                newSect.setLowerThroughPoint(new Point(prevTile.getLocation(), 0.5, 0));
              }
              if (!(newSect.getUpperSlope() == newSect.getLowerSlope())) { //catch zero width sectors
                //System.out.println("Recursion!");
                scan(center, newSect, rowByRow, xDir, yDir, visibilities, x-center.getX()+xDir);
              }
            }
            
            if (sect.inSectorExcludeEdges(new Point(x, y))) {
              visibilities[(int)x][(int)y] = true; //I feel fairly confident that there is no case where the sector won't intersect with the wall in this scenario
            }
            else {
              if ( (Math.floor(endY) == Math.floor(y) && endY != Math.floor(y)) || (Math.floor(startY) == Math.floor(y) && startY != Math.floor(y)) ) {
                //if the sample point was outside the sector, but either of the rays pass though the same tile, it's still visible
                //excluding times when the line is only tangent to the diamond, i.e. when the line passes exactly through the edge point
                visibilities[(int)x][(int)y] = true;
              }
            }
          }
          prevTile = this.tileAt(x, y);
        }
      }
    }
  }

  public String toString() {
    String levelString = "";

    // ╔ ═ ╗ ║ ╚╝
    levelString += "╔";
    for (int i = 0; i < this.tiles.length; i++) {
      levelString += "═";
    }
    levelString += "╗\n";

    for (int y = this.tiles[0].length-1; y >= 0; y--) { //y-value must be traversed backwards when printing because lower values are lower down and therefore must be printed after higher numbers
      for (int x = 0; x < this.tiles.length; x++) {
        if (x == 0) {
          levelString += "║";
        }
        //this.tiles[x][y].setVisibility(true); //debug statement: force all visibility to true
        levelString += this.tiles[x][y];
        if (x == this.tiles.length - 1) {
          levelString += "║\n";
        }
      }
    }

    levelString += "╚";
    for (int i = 0; i < this.tiles.length; i++) {
      levelString += "═";
    }
    levelString += "╝\n";

    return levelString;
  }
}