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
package org.openhab.binding.connectedcar.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class ThingConfiguration {
    public String vin = "", pin = "";

    public int numShortTrip = 1; // number of entries from short trip data history
    public int numLongTrip = 1; // number of entries from long trip data history
    public int numRluHistory = 3; // number of entries from RLU action history
    public int numDestinations = 1; // number of entries from the destination history;
    public int numSpeedAlerts = 1;
    public int numGeoFenceAlerts = 1;
    public int numChargingRecords = 1;

    public boolean enableAddressLookup = false;
    public Integer pollingInterval = 15;
}
