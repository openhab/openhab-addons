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
package org.openhab.binding.freebox.internal;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.freebox.internal.api.model.FreeboxAirMediaReceiver;
import org.openhab.binding.freebox.internal.api.model.FreeboxLanHost;

/**
 * The {@link FreeboxDataListener} is notified by the bridge thing handler
 * with updated data from the Freebox server.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Laurent Garnier - add discovery configuration
 * @author Laurent Garnier - use new internal classes
 */
public interface FreeboxDataListener {

    /**
     * Update the discovery configuration.
     *
     * @param configProperties the configuration
     */
    public void applyConfig(Map<String, Object> configProperties);

    /**
     * This method is called just after the bridge thing handler fetched new data
     * from the Freebox server.
     *
     * @param bridge the Freebox server bridge.
     * @param lanHosts the LAN data received from the Freebox server.
     * @param airPlayDevices the list of AirPlay devices received from the Freebox server.
     */
    public void onDataFetched(ThingUID bridge, List<FreeboxLanHost> lanHosts,
            List<FreeboxAirMediaReceiver> airPlayDevices);
}
