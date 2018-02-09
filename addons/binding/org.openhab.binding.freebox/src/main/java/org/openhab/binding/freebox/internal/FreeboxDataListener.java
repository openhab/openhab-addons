/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal;

import java.util.List;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.matmaul.freeboxos.airmedia.AirMediaReceiver;
import org.matmaul.freeboxos.lan.LanHostsConfig;

/**
 * The {@link FreeboxDataListener} is notified by the bridge thing handler
 * with updated data from the Freebox server.
 *
 * @author Laurent Garnier
 */
public interface FreeboxDataListener {

    /**
     * This method is called just after the bridge thing handler fetched new data
     * from the Freebox server.
     *
     * @param bridge
     *            The Freebox server bridge.
     * @param hostsConfig
     *            The LAN data received from the Freebox server.
     * @param airPlayDevices
     *            The list of AirPlay devices received from the Freebox server.
     */
    public void onDataFetched(ThingUID bridge, LanHostsConfig hostsConfig, List<AirMediaReceiver> airPlayDevices);
}
