/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.dto;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * http://dresden-elektronik.github.io/deconz-rest-doc/configuration/
 * # Get full state
 * GET /api/<apikey>
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
}
