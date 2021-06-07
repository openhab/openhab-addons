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

    public abstract void onDataReceived(String remoteAddress, int remotePort, String remoteMAC,
            ThingTypeUID thingTypeUID, int model);
}
