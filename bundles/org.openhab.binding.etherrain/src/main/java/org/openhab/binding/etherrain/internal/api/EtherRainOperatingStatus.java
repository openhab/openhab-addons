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
 * The {@link EtherRainOperatingStatus} is the response packet for Operating Status
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
public enum EtherRainOperatingStatus {
  STATUS_READY("RD"), STATUS_WAITING("WT"), STATUS_BUSY("BZ");

  protected String status;

  EtherRainOperatingStatus(String status) {
    this.status = status;
  }

  public static EtherRainOperatingStatus fromString(String text) {
    for (EtherRainOperatingStatus b : EtherRainOperatingStatus.values()) {
      if (b.status.equalsIgnoreCase(text)) {
        return b;
      }
    }
    return null;
  }
}
