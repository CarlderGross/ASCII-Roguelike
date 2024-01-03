package consts;

import objects.Monster;
import geometry.Point;
import structure.Level;

public enum BASEMONS {
  //TRAINING(5, "training golem", 't', ANSI.YELLOW, 0, 0, 12),
  GOBLIN(5, "goblin", 'g', ANSI.GREEN, 0, 3, 11);

  private final Monster stored;

  private BASEMONS(int hp, String name, char symbol, ANSI color, int toHit, int dmg, int armor) {
    this.stored = new Monster(hp, name, symbol, color, toHit, dmg, armor);
  }

  public Monster spawnAt(Point p, Level level) {
    Monster out = stored.clone();
    if (level.tileAt(p).getCreature() != null) {
      System.out.println(level.tileAt(p).getCreature());
      throw new IllegalStateException("Creature already exists at point "+p+"!");
    }
    else if (level.tileAt(p).isSolid()) {
      throw new IllegalStateException("Can't spawn creature inside solid wall!");
    }
    out.setLocation(p, level);
    return out;
  }
  public Monster getMon() {
    return stored.clone();
  }
}