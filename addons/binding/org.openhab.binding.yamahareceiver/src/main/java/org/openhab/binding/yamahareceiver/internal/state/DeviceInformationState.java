/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.state;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;

/**
 * Basic AVR state (name, version, available zones, etc)
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - DAB support, Spotify support
 */
public class DeviceInformationState implements Invalidateable {
    public String host = null;
    // Some AVR information
    public String name = "N/A";
    public String id = "";
    public String version = "0.0";
    public List<YamahaReceiverBindingConstants.Zone> zones = new ArrayList<>();

    /**
     * Flag indicating if DAB input (dual band digital tuner) is supported
     */
    public boolean supportDAB = false;
    /**
     * Flag indicating if Tuner input is supported
     */
    public boolean supportTuner = false;
    /**
     * Flag indicating if Spotify input is supported
     */
    public boolean supportSpotify = false;

    // If we lost the connection, invalidate the state.
    @Override
    public void invalidate() {
        zones.clear();
        version = "0.0";
        name = "N/A";
        id = "";
        supportDAB = false;
        supportTuner = false;
        supportSpotify = false;
    }
}
