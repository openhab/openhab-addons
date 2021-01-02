/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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
    public String bondId = API_MISSING_BOND_ID;
    public String localToken = API_MISSING_LOCAL_TOKEN;
    public @Nullable String bondIpAddress;

    public @Nullable String getIpAddress() {
        return bondIpAddress;
    }

    public void setIpAddress(String bondIpAddress) {
        this.bondIpAddress = bondIpAddress;
    }
}
