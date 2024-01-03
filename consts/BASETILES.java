package consts;

public enum BASETILES {
  WALL("wall", true, false, ' '), FLOOR("floor", false, true, '.');

  public final String type;
  public final boolean solid;
  public final boolean transparent;
  public final char symbol;

  BASETILES(String type, boolean solid, boolean transparent, char symbol) {
    this.type = type;
    this.solid = solid;
    this.transparent = transparent;
    this.symbol = symbol;
  }
}