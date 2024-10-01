/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
    return value;
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

