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

import static org.openhab.binding.mercedesme.internal.Constants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AccountConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AccountConfiguration {

    public String clientId = NOT_SET;
    public String clientSecret = NOT_SET;
    public String imageApiKey = NOT_SET;

    // Advanced Parameters
    public String callbackIP = NOT_SET;
    public int callbackPort = -1;
    public boolean odoScope = true;
    public boolean vehicleScope = true;
    public boolean lockScope = true;
    public boolean fuelScope = true;
    public boolean evScope = true;

    // https://developer.mercedes-benz.com/products/electric_vehicle_status/docs#_required_scopes
    public String getScope() {
        StringBuffer sb = new StringBuffer();
        sb.append(SCOPE_OFFLINE);
        if (odoScope) {
            sb.append(SPACE).append(SCOPE_ODO);
        }
        if (vehicleScope) {
            sb.append(SPACE).append(SCOPE_STATUS);
        }
        if (lockScope) {
            sb.append(SPACE).append(SCOPE_LOCK);
        }
        if (fuelScope) {
            sb.append(SPACE).append(SCOPE_FUEL);
        }
        if (evScope) {
            sb.append(SPACE).append(SCOPE_EV);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ID " + clientId + ", Secret " + clientSecret + ", IP " + callbackIP + ", Port " + callbackPort
                + ", scope " + getScope();
    }
}
