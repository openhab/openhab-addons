/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.HashMap;
import java.util.Map;

/***
 * {@link ShellyDeviceStats} some statistical values for the thing
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyDeviceStats {
    public long lastUptime = 0;
    public long unexpectedRestarts = 0;
    public long timeoutErrors = 0;
    public long timeoutsRecorvered = 0;
    public long remainingWatchdog = 0;
    public long alarms = 0;

    public Map<String, String> asProperties() {
        Map<String, String> prop = new HashMap<>();
        prop.put("lastUptime", String.valueOf(lastUptime));
        prop.put("unexpectedRestarts", String.valueOf(unexpectedRestarts));
        prop.put("timeoutErrors", String.valueOf(timeoutErrors));
        prop.put("timeoutsRecovered", String.valueOf(timeoutsRecorvered));
        prop.put("remainingWatchdog", String.valueOf(remainingWatchdog));
        prop.put("alarmCount", String.valueOf(alarms));
        return prop;
    }
}
