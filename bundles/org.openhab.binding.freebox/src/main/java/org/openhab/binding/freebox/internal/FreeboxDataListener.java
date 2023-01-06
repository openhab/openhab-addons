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
package org.openhab.binding.freebox.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freebox.internal.api.model.FreeboxAirMediaReceiver;
import org.openhab.binding.freebox.internal.api.model.FreeboxLanHost;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link FreeboxDataListener} is notified by the bridge thing handler
 * with updated data from the Freebox server.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Laurent Garnier - add discovery configuration
 * @author Laurent Garnier - use new internal classes
 */
@NonNullByDefault
public interface FreeboxDataListener {

    /**
     * This method is called just after the bridge thing handler fetched new data
     * from the Freebox server.
     *
     * @param bridge the Freebox server bridge.
     * @param lanHosts the LAN data received from the Freebox server.
     * @param airPlayDevices the list of AirPlay devices received from the Freebox server.
     */
    public void onDataFetched(ThingUID bridge, @Nullable List<FreeboxLanHost> lanHosts,
            @Nullable List<FreeboxAirMediaReceiver> airPlayDevices);
}
