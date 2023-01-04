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
package org.openhab.binding.network.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    public boolean allowSystemPings = true;
    public boolean allowDHCPlisten = true;
    public BigDecimal cacheDeviceStateTimeInMS = BigDecimal.valueOf(2000);
    public String arpPingToolPath = "arping";
    public ArpPingUtilEnum arpPingUtilMethod = ArpPingUtilEnum.DISABLED;
    // For backwards compatibility reasons, the default is to use the ping method execution time as latency value
    public boolean preferResponseTimeAsLatency = false;

    private List<NetworkBindingConfigurationListener> listeners = new ArrayList<>();

    public void update(NetworkBindingConfiguration newConfiguration) {
        this.allowSystemPings = newConfiguration.allowSystemPings;
        this.allowDHCPlisten = newConfiguration.allowDHCPlisten;
        this.cacheDeviceStateTimeInMS = newConfiguration.cacheDeviceStateTimeInMS;
        this.arpPingToolPath = newConfiguration.arpPingToolPath;
        this.preferResponseTimeAsLatency = newConfiguration.preferResponseTimeAsLatency;

        NetworkUtils networkUtils = new NetworkUtils();
        this.arpPingUtilMethod = networkUtils.determineNativeARPpingMethod(arpPingToolPath);

        notifyListeners();
    }

    public void addNetworkBindingConfigurationListener(NetworkBindingConfigurationListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        listeners.forEach(NetworkBindingConfigurationListener::bindingConfigurationChanged);
    }

    @Override
    public String toString() {
        return "NetworkBindingConfiguration{" + "allowSystemPings=" + allowSystemPings + ", allowDHCPlisten="
                + allowDHCPlisten + ", cacheDeviceStateTimeInMS=" + cacheDeviceStateTimeInMS + ", arpPingToolPath='"
                + arpPingToolPath + '\'' + ", arpPingUtilMethod=" + arpPingUtilMethod + ", preferResponseTimeAsLatency="
                + preferResponseTimeAsLatency + '}';
    }
}
