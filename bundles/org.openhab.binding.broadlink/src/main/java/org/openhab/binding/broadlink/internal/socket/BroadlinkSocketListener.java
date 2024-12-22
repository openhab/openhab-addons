/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.internal.socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Interface for something that is interested in being informed when data arrives on a socket
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public interface BroadlinkSocketListener {

    /**
     * Method triggered when the defined socket receives data
     *
     * @param remoteAddress the remote address to receive data from
     * @param remotePort the remote port to receive data from
     * @param remoteMAC the remote MAC address to receive data from
     * @param thingTypeUID the defined thing type id to receive data from
     * @param model the device type of thing to receive data from
     */
    public abstract void onDataReceived(String remoteAddress, int remotePort, String remoteMAC,
            ThingTypeUID thingTypeUID, int model);
}
