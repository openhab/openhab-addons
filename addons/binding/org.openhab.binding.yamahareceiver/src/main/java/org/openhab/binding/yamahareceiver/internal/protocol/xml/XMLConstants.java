/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Feature;

import java.util.HashMap;
import java.util.Map;

/**
 * XML protocol constants.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class XMLConstants {

    public static final String ON = "On";
    public static final String OFF = "Off";
    public static final String POWER_STANDBY = "Standby";
    public static final Map<String, Feature> FEATURE_BY_YNC_TAG;

    static {
        FEATURE_BY_YNC_TAG = new HashMap<>();
        FEATURE_BY_YNC_TAG.put("Tuner", Feature.TUNER);
        FEATURE_BY_YNC_TAG.put("DAB", Feature.DAB);
        FEATURE_BY_YNC_TAG.put("Spotify", Feature.SPOTIFY);
        FEATURE_BY_YNC_TAG.put("Bluetooth", Feature.BLUETOOTH);
        FEATURE_BY_YNC_TAG.put("AirPlay", Feature.AIRPLAY);
        FEATURE_BY_YNC_TAG.put("NET_RADIO", Feature.NET_RADIO);
        FEATURE_BY_YNC_TAG.put("USB", Feature.USB);
    }

}
