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

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@NonNullByDefault
class ShellyBaseHandlerAlarmFilterTest {

    @ParameterizedTest
    @ValueSource(strings = { SHELLY_WAKEUPT_NONE, SHELLY_WAKEUPT_SENSOR, SHELLY_WAKEUPT_PERIODIC, SHELLY_WAKEUPT_BUTTON,
            SHELLY_WAKEUPT_POWERON, SHELLY_WAKEUPT_EXT_POWER, SHELLY_WAKEUPT_UNKNOWN, SHELLY2_WAKEUPOCAUSE_USB,
            SHELLY2_WAKEUPOCAUSE_UPDATE, SHELLY2_WAKEUPOCAUSE_UNDEFINED, SHELLY2_WAKEUPOCAUSE_BUTTON,
            SHELLY2_WAKEUPOCAUSE_PERIODIC, SHELLY2_WAKEUPOCAUSE_ALARM, SHELLY2_WAKEUPOCAUSE_ALARM_TEST,
            SHELLY2_EVENT_OTASTART, SHELLY2_EVENT_OTAPROGRESS, SHELLY2_EVENT_OTADONE, SHELLY_EVENT_ROLLER_CALIB,
            ALARM_TYPE_NONE })
    void wakeupReasonsAndStatusEventsAreNotAlarms(String event) {
        assertFalse(ShellyBaseHandler.isAlarmEvent(event));
        assertFalse(ShellyBaseHandler.isAlarmEvent(event.toUpperCase(Locale.ROOT)));
    }

    @Test
    void emptyEventIsNotAnAlarm() {
        assertFalse(ShellyBaseHandler.isAlarmEvent(""));
    }

    @ParameterizedTest
    @ValueSource(strings = { ALARM_TYPE_RESTARTED, ALARM_TYPE_OVERTEMP, ALARM_TYPE_OVERPOWER, ALARM_TYPE_LOW_BATTERY,
            ALARM_TYPE_FLOOD, SHELLY2_EVENT_WIFICONNFAILED })
    void realAlarmsAreTriggered(String event) {
        assertTrue(ShellyBaseHandler.isAlarmEvent(event));
    }
}
