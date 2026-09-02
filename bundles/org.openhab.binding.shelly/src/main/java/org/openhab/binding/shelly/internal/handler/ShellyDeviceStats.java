/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.util.ShellyUtils;

/***
 * {@link ShellyDeviceStats} some statistical values for the thing
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyDeviceStats {

    public record ShellyDeviceAlarm(String message, long timeStamp) {
    }

    public final AtomicLong lastUptime = new AtomicLong(0);
    public final AtomicLong restarts = new AtomicLong(0);
    public final AtomicInteger timeoutErrors = new AtomicInteger(0);
    public final AtomicInteger timeoutsRecovered = new AtomicInteger(0);
    public final AtomicLong remainingWatchdog = new AtomicLong(0);
    public final AtomicLong alarms = new AtomicLong(0);
    public final AtomicReference<@Nullable ShellyDeviceAlarm> lastAlarm = new AtomicReference<>();
    public final AtomicLong protocolMessages = new AtomicLong(0);
    public final AtomicInteger protocolErrors = new AtomicInteger(0);
    public final AtomicInteger wifiRssi = new AtomicInteger(0);
    public final AtomicInteger maxInternalTemp = new AtomicInteger(0);

    public Map<String, String> asProperties() {
        Map<String, String> prop = new HashMap<>();
        prop.put("lastUptime", String.valueOf(lastUptime));
        prop.put("deviceRestarts", String.valueOf(restarts));
        prop.put("timeoutErrors", String.valueOf(timeoutErrors));
        prop.put("timeoutsRecovered", String.valueOf(timeoutsRecovered));
        prop.put("remainingWatchdog", String.valueOf(remainingWatchdog));
        prop.put("alarmCount", String.valueOf(alarms));
        ShellyDeviceAlarm alarm = lastAlarm.get();
        if (alarm != null) {
            prop.put("lastAlarm", alarm.message);
            prop.put("lastAlarmTs", ShellyUtils.convertTimestamp(alarm.timeStamp));
        }
        prop.put("protocolMessages", String.valueOf(protocolMessages));
        prop.put("protocolErrors", String.valueOf(protocolErrors));
        prop.put("wifiRssi", String.valueOf(wifiRssi));
        prop.put("maxInternalTemp", String.valueOf(maxInternalTemp.get()));
        return prop;
    }
}
