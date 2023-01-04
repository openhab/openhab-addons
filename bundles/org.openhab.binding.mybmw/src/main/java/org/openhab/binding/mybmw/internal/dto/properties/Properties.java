/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.dto.properties;

import java.util.List;

/**
 * The {@link Properties} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Properties {
    public String lastUpdatedAt;// ": "2021-12-21T16:46:02Z",
    public boolean inMotion;// ": false,
    public boolean areDoorsLocked;// ": true,
    public String originCountryISO;// ": "DE",
    public boolean areDoorsClosed;// ": true,
    public boolean areDoorsOpen;// ": false,
    public boolean areWindowsClosed;// ": true,
    public DoorsWindows doorsAndWindows;// ":
    public boolean isServiceRequired;// ":false
    public FuelLevel fuelLevel;
    public ChargingState chargingState;// ":
    public Range combustionRange;
    public Range combinedRange;
    public Range electricRange;
    public Range electricRangeAndStatus;
    public List<CCM> checkControlMessages;
    public List<CBS> serviceRequired;
    public Location vehicleLocation;
    public Tires tires;
    // "climateControl":{} [todo] definition currently unknown
}
