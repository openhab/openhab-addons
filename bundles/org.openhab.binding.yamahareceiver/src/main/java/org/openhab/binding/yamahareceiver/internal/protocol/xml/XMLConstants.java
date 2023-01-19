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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Feature;

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
    public static final String GET_PARAM = "GetParam";
    public static final String UP = "Up";
    public static final String DOWN = "Down";

    public static class Commands {

        public static final String SYSTEM_STATUS_CONFIG_CMD = "<System><Config>GetParam</Config></System>";
        public static final String SYSTEM_STATUS_CONFIG_PATH = "System/Config";

        public static final String ZONE_BASIC_STATUS_CMD = "<Basic_Status>GetParam</Basic_Status>";
        public static final String ZONE_BASIC_STATUS_PATH = "Basic_Status";

        public static final String ZONE_INPUT_QUERY = "<Input><Input_Sel_Item>GetParam</Input_Sel_Item></Input>";
        public static final String ZONE_INPUT_PATH = "Input/Input_Sel_Item";

        public static final String PLAYBACK_STATUS_CMD = "<Play_Info>GetParam</Play_Info>";
    }

    static {
        FEATURE_BY_YNC_TAG = new HashMap<>();
        FEATURE_BY_YNC_TAG.put("Tuner", Feature.TUNER);
        FEATURE_BY_YNC_TAG.put("DAB", Feature.DAB);
        FEATURE_BY_YNC_TAG.put("Spotify", Feature.SPOTIFY);
        FEATURE_BY_YNC_TAG.put("Bluetooth", Feature.BLUETOOTH);
        FEATURE_BY_YNC_TAG.put("AirPlay", Feature.AIRPLAY);
        FEATURE_BY_YNC_TAG.put("NET_RADIO", Feature.NET_RADIO);
        FEATURE_BY_YNC_TAG.put("USB", Feature.USB);
        FEATURE_BY_YNC_TAG.put("NET_USB", Feature.NET_USB);
    }
}
