/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNOperationList.CarNetOperationList;

/**
 * The {@link CarNetVehicleConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetVehicleConfiguration {
    public String vin = "";
    public String pin = "";

    public int numShortTrip = 1; // number of entries from short trip data history
    public int numLongTrip = 1; // number of entries from long trip data history
    public int numActionHistory = 1; // number of entries from action history
    public int numDestinations = 1; // number of entries from the destination history;
    public int numSpeedAlerts = 1;
    public int numGeoFenceAlerts = 1;

    public boolean enableAddressLookup = false;
    public Integer pollingInterval = 15;

    public @Nullable CarNetOperationList operationList;
    public String rolesRightsUrl = "";
    public String homeRegionUrl = "";
    public String apiUrlPrefix = "";
}
