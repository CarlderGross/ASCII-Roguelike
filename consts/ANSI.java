package consts;

public enum ANSI {
  //colors
  RESET("\u001B[0m"),
  BLACK("\u001B[30m"),
  RED("\u001B[31m"),
  GREEN("\u001B[32m"),
  BROWN("\u001B[33m"),
  BLUE("\u001B[34m"),
  PURPLE("\u001B[35m"),
  CYAN("\u001B[36m"),
  WHITE("\u001B[37m"),
  YELLOW("\u001B[93m"),
  MAGENTA("\u001B[95m"),
  GREY("\u001B[90m"),

  //modifiers
  INVERT("\u001B[7m"),
  FAINT("\u001B[2m");

  private final String value;

  private ANSI(String s) {
    this.value = s;
  }

  public String toString() {
    return value;
  }
}