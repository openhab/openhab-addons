package org.openhab.binding.amplipi.internal.model;

/**
 * An enumeration.
 */
public enum StreamCommand {
  
  PLAY("play"),
  
  PAUSE("pause"),
  
  NEXT("next"),
  
  STOP("stop"),
  
  LIKE("like"),
  
  BAN("ban"),
  
  SHELVE("shelve");

  private String value;

  StreamCommand(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static StreamCommand fromValue(String value) {
    for (StreamCommand b : StreamCommand.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
  
}

