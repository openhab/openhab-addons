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
package org.openhab.binding.mercedesme.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.Constants;

/**
 * The {@link VehicleConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleConfiguration {

    public String vin = Constants.NOT_SET;
    public int refreshInterval = 5;
    public float batteryCapacity = -1;
    public float fuelCapacity = -1;

    // Advanced
    public boolean background = false;
    public boolean night = false;
    public boolean cropped = false;
    public boolean roofOpen = false;
    public String format = "webp";
}
