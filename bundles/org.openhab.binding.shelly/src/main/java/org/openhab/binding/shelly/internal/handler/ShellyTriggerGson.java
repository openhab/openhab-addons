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
package org.openhab.binding.shelly.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/***
 * The{@link ShellyTriggerGson} provides the Json mapping for triggers
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyTriggerGson {

    public String              device               = "";
    public String              timestamp            = "";

    public @Nullable String    uptime;
    public @Nullable String    lastUpdate;
    public @Nullable String    signal;
    public @Nullable String    type;
    public @Nullable String    temp;
    public @Nullable String    hum;
    public @Nullable String    flood;
    public @Nullable String    batv;
    public @Nullable String    vatv;
    public @Nullable String    alarm;
    public @Nullable String    unknown;

    public String              category             = "null";
    public static final String EVENT_TYPE_DEVICE    = "device";    // event on the device level
    public static final String EVENT_TYPE_EVENT     = "event";     // HTTP Event
    public static final String EVENT_TYPE_COMPONENT = "component"; // Event from component, e.g. relay

    @SuppressWarnings("null")
    public ShellyTriggerGson(String thingName, String category) {
        this.device = thingName;
        this.category = category;
        if (category == null) {
            this.category = "n/a";
        }
    }

    public void setPayload(Map<String, String> args) {
        for (Map.Entry<String, String> p : args.entrySet()) {
            String value = p.getValue();
            switch (p.getKey().toLowerCase()) {
                case "uptime":
                    uptime = value;
                    break;
                case "lastupdate":
                    lastUpdate = value;
                    break;
                case "type":
                    type = value;
                    break;
                case "temp": // temperature
                    temp = value;
                    break;
                case "hum": // temperature
                    hum = value;
                    break;
                case "flood": // Flood alarm status
                    flood = value;
                    break;
                case "batv": // Battery Volt
                    batv = value;
                    break;
                case "signal": // WiFi RSSI
                    signal = value;
                    break;
                case "alarm":
                    if (!value.isEmpty()) {
                        alarm = value;
                    }
                    break;
                default:
                    unknown = value;

            }
        }
    }

}
