package objects;

import structure.GameSystem;

public class Stairs extends Tile {
  boolean isUp;
  
  public Stairs(boolean isUp) {
    super("stairs", false, true, '>');
    this.isUp = isUp;
    if (isUp) {
      this.symbol = '<';
      this.name = "stairs up";
    }
    else {
      this.symbol = '>';
      this.name = "stairs down";
    }
  }

  public boolean useStairs(GameSystem game, Player play) throws ClassNotFoundException {
    return game.changeLevel(play, this.isUp);
  }
}