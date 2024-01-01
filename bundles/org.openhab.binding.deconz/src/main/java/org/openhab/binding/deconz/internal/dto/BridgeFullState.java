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
package org.openhab.binding.deconz.internal.dto;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.types.ResourceType;

/**
 * http://dresden-elektronik.github.io/deconz-rest-doc/configuration/
 * # Get full state
 * {@code GET /api/<apikey>}
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class BridgeFullState {
    public Config config = new Config();

    public static class Config {
        public String apiversion = ""; // "1.0.0"
        public String ipaddress = ""; // "192.168.80.142",
        public String name = ""; // "deCONZ-GW",
        public String swversion = ""; // "20405"
        public String fwversion = ""; // "0x262e0500"
        public String uuid = ""; // "a65d80a1-975a-4598-8d5a-2547bc18d63b",
        public int websocketport = 0; // 8088
        public int zigbeechannel = 0;
    }

    public Map<String, SensorMessage> sensors = Collections.emptyMap();
    public Map<String, LightMessage> lights = Collections.emptyMap();
    public Map<String, GroupMessage> groups = Collections.emptyMap();

    public @Nullable DeconzBaseMessage getMessage(ResourceType resourceType, String id) {
        switch (resourceType) {
            case LIGHTS:
                return lights.get(id);
            case SENSORS:
                return sensors.get(id);
            case GROUPS:
                return groups.get(id);
            default:
                return null;
        }
    }
}
