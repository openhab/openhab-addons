/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.handler;

import org.eclipse.smarthome.core.thing.Thing;

/**
 * Builds a {@see NetworkHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class NetworkHandlerBuilder {
    private Thing thing;
    private boolean isTCPServiceDevice;
    private boolean allowSystemPings;
    private boolean allowDHCPlisten;
    private int cacheDeviceStateTimeInMS;
    private String arpPingToolPath;

    private NetworkHandlerBuilder() {
    }

    /**
     * Creates a ping device
     */
    public static NetworkHandlerBuilder createPingDevice(Thing thing) {
        NetworkHandlerBuilder v = new NetworkHandlerBuilder();
        v.thing = thing;
        v.isTCPServiceDevice = false;
        return v;
    }

    /**
     * Creates a tcp service device
     */
    public static NetworkHandlerBuilder createServiceDevice(Thing thing) {
        NetworkHandlerBuilder v = new NetworkHandlerBuilder();
        v.thing = thing;
        v.isTCPServiceDevice = true;
        return v;
    }

    /**
     * Build the ThingHandler
     */
    public NetworkHandler build() {
        return new NetworkHandler(thing, isTCPServiceDevice, allowSystemPings, allowDHCPlisten,
                cacheDeviceStateTimeInMS, arpPingToolPath);
    }

    public NetworkHandlerBuilder allowSystemPings(boolean enable) {
        allowSystemPings = enable;
        return this;
    }

    public NetworkHandlerBuilder allowDHCPListen(boolean enable) {
        allowDHCPlisten = enable;
        return this;
    }

    public NetworkHandlerBuilder cacheTimeInMS(int cacheTime) {
        cacheDeviceStateTimeInMS = cacheTime;
        return this;
    }

    public NetworkHandlerBuilder arpPingToolPath(String arpPingToolPath) {
        this.arpPingToolPath = arpPingToolPath;
        return this;
    }
}
