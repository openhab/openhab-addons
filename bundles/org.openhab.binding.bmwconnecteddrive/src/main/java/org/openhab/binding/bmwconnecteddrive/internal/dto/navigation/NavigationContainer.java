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
package org.openhab.binding.bmwconnecteddrive.internal.dto.navigation;

/**
 * The {@link NavigationContainer} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class NavigationContainer {
    // "latitude": 56.789,
    // "longitude": 8.765,
    // "isoCountryCode": "DEU",
    // "auxPowerRegular": 1.4,
    // "auxPowerEcoPro": 1.2,
    // "auxPowerEcoProPlus": 0.4,
    // "soc": 25.952999114990234,
    // "pendingUpdate": false,
    // "vehicleTracking": true,
    public double socmax;// ": 29.84
}
