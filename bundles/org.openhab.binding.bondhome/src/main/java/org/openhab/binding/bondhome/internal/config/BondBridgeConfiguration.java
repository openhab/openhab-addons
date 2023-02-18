/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bondhome.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BondBridgeConfiguration} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BondBridgeConfiguration {

    /**
     * Configuration for a Bond Bridge
     */
    public String serialNumber = "";
    public String localToken = "";
    public String ipAddress = "";

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isValid() {
        return !(serialNumber.isEmpty() || localToken.isEmpty() || ipAddress.isEmpty());
    }
}
