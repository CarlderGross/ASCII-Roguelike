package objects;

import consts.ANSI;
import geometry.Point;
import structure.GameSystem;
import structure.Level;

public class Monster extends Creature implements Cloneable {
  int hitBonus;
  int dmg;
  int ac;
  private boolean alerted = false;

  public Monster(int hp, String name, char symbol, ANSI color, int toHit, int damg, int armor) {
    super(hp, name, symbol, color);
    this.hitBonus = toHit;
    this.dmg = damg;
    this.ac = armor;
  }

  public int getArmorClass() {
    return this.ac;
  }

  public boolean isAlerted() {
    return this.alerted;
  }
  public void alert() {
    this.alerted = true;
  }

  public void die(Level level, GameSystem game) {
    super.die(game);
    level.tileAt(location).setCreature(null);
    this.location = new Point(-2, -2); //go outside the world
  }

  public void takeAction(Player play, Level level, GameSystem game) {
    if (!this.isAlive()) {
      this.die(game); //die if you are not alive
    }
    else if (alerted) { //only hunt the player if you are alerted
      if (this.getLocation().distanceFrom(play.getLocation()) <= 1) {
        this.attack(play, this.hitBonus, this.dmg, game);
      }
      else { //basic straight-line pathing, mostly ignores obstacles
        //maybe will be converted to A* pathing later
        double xDiff = play.getLocation().getX() - this.location.getX();
        double yDiff = play.getLocation().getY() - this.location.getY();

        //scale to 1
        double xUnit = 0;
        if (xDiff > 0) {
          xUnit = 1;
        }
        else if (xDiff < 0) {
          xUnit = -1;
        }
        double yUnit = 0;
        if (yDiff > 0) {
          yUnit = 1;
        }
        else if (yDiff < 0) {
          yUnit = -1;
        }
        
        if (!(this.moveTo(new Point(this.getLocation(), xUnit, yUnit), level))) {
          if (yDiff > xDiff) {
            if (!this.moveTo(new Point(this.getLocation(), 0, yUnit), level)) {
              if (!this.moveTo(new Point(this.getLocation(), 1, yUnit), level)) {
                this.moveTo(new Point(this.getLocation(), -1, yUnit), level);
              }
            }
          }
          else {
            if (!this.moveTo(new Point(this.getLocation(), xUnit, 0), level)) {
              if (!this.moveTo(new Point(this.getLocation(), xUnit, 1), level)) {
                this.moveTo(new Point(this.getLocation(), xUnit, -1), level);
              }
            }
          }
        }
      }
    }
  }

  public Monster clone() {
    return new Monster(this.hp, this.name, this.symbol, this.color, this.hitBonus, this.dmg, this.ac);
  }
}