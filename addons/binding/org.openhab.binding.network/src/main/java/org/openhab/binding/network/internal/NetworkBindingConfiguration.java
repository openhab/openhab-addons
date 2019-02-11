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
package org.openhab.binding.network.internal;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.network.internal.utils.NetworkUtils;
import org.openhab.binding.network.internal.utils.NetworkUtils.ArpPingUtilEnum;

/**
 * Contains the binding configuration and default values. The field names represent the configuration names,
 * do not rename them if you don't intend to break the configuration interface.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class NetworkBindingConfiguration {
    public Boolean allowSystemPings = true;
    public Boolean allowDHCPlisten = true;
    public BigDecimal cacheDeviceStateTimeInMS = BigDecimal.valueOf(2000);
    public String arpPingToolPath = "arping";
    public @NonNullByDefault({}) ArpPingUtilEnum arpPingUtilMethod;

    public void update(NetworkBindingConfiguration newConfiguration) {
        this.allowSystemPings = newConfiguration.allowSystemPings;
        this.allowDHCPlisten = newConfiguration.allowDHCPlisten;
        this.cacheDeviceStateTimeInMS = newConfiguration.cacheDeviceStateTimeInMS;
        this.arpPingToolPath = newConfiguration.arpPingToolPath;

        NetworkUtils networkUtils = new NetworkUtils();
        this.arpPingUtilMethod = networkUtils.determineNativeARPpingMethod(arpPingToolPath);
    }
}
