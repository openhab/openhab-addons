/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.etherrain.internal.api;

/**
 * The {@link EtherRainCommandStatus} is the response packet for Command Status
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
public enum EtherRainCommandStatus {
  STATUS_OK("OK"), STATUS_ERROR("ER"), STATUS_UNATHORIZED("NA");

  protected String status;

  EtherRainCommandStatus(String status) {
    this.status = status;
  }

  public static EtherRainCommandStatus fromString(String text) {
    for (EtherRainCommandStatus b : EtherRainCommandStatus.values()) {
      if (b.status.equalsIgnoreCase(text)) {
        return b;
      }
    }
    return null;
  }
}
