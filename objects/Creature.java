package objects;

import java.util.Random;

import geometry.Point;
import consts.ANSI;
import structure.Level;
import structure.GameSystem;

public abstract class Creature extends GameObject {
  int hp;
  
  public Creature(int h, String n, char s, ANSI c) {
    super(s, c, n);
    this.hp = h;
    this.name = n;
  }

  public void attack(Creature target, int hitBonus, int damage, GameSystem game) { //this will use a d20 system because that's the most simple
    game.addMessage(this.name+" attacks "+target.name+"...");
    int roll = game.rollDie(20);
    if (target.getArmorClass() <= roll+hitBonus) {
      //you hit
      int resultantDamage = target.takeDamage(game.rollDie(damage));
      game.addMessage("and hits for "+resultantDamage+" damage!");
    }
    else {
      if (roll+hitBonus < 10) {
        game.addMessage("and misses!");
      }
      else {
        game.addMessage("but is blocked by their armor!");
      }
    }
  }

  void die(GameSystem game) {
    this.hp = 0;
    game.addMessage(this.name+" has died!");
  }
  public boolean isAlive() {
    if (this.hp > 0) {
      return true;
    }
    return false;
  }
  public int getHp() {
    return this.hp;
  }

  public int takeDamage(int damage) {
    this.hp -= damage;
    return damage; //later we might have resistances and whatnot so let's return the damage
  }

  public boolean moveTo(Point loc, Level level) {
    if ((level.tileAt(loc).getCreature() == null) && !level.tileAt(loc).isSolid()) {
      this.setLocation(loc, level);
      return true;
    }
    return false;
  }
  public boolean moveTo(double x, double y, Level level) {
    return this.moveTo(new Point(x, y), level);
  }

  public void setLocation(Point loc, Level level) {
    level.tileAt(this.location).setCreature(null);
    //System.out.println(level.tileAt(this.location).getCreature());
    super.setLocation(loc); //defines location variable
    level.tileAt(loc).setCreature(this);
  }
  public void setLocation(double x, double y, Level level) {
    this.setLocation(new Point(x, y), level);
  }

  public abstract int getArmorClass();
} 