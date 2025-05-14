/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.smokedetector;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ALARM;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.openhab.binding.boschshc.internal.devices.AbstractSmokeDetectorHandlerTest;
import org.openhab.binding.boschshc.internal.services.alarm.dto.AlarmServiceState;
import org.openhab.binding.boschshc.internal.services.alarm.dto.AlarmState;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link AbstractSmokeDetectorHandlerWithAlarmService}.
 * 
 * @author David Pace - Initial contribution
 *
 * @param <T> type of the smoke detector
 */
@NonNullByDefault
public abstract class AbstractSmokeDetectorHandlerWithAlarmServiceTest<T extends AbstractSmokeDetectorHandlerWithAlarmService>
        extends AbstractSmokeDetectorHandlerTest<T> {

    @Captor
    private @NonNullByDefault({}) ArgumentCaptor<AlarmServiceState> alarmStateCaptor;

    @Test
    public void testUpdateChannelsAlarm() {
        String json = """
                {
                    "@type": "alarmState",
                    "value": IDLE_OFF
                }
                """;
        JsonElement jsonObject = JsonParser.parseString(json);
        getFixture().processUpdate("Alarm", jsonObject);
        verify(getCallback()).stateUpdated(new ChannelUID(getThing().getUID(), CHANNEL_ALARM),
                new StringType("IDLE_OFF"));
    }

    @Test
    public void testHandleCommandAlarm() throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_ALARM),
                new StringType("INTRUSION_ALARM_ON_REQUESTED"));
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("Alarm"), alarmStateCaptor.capture());
        AlarmServiceState state = alarmStateCaptor.getValue();
        assertSame(AlarmState.INTRUSION_ALARM_ON_REQUESTED, state.value);
    }

    @Test
    public void testHandleCommandAlarmUnknownAlarmState()
            throws InterruptedException, TimeoutException, ExecutionException {
        getFixture().handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_ALARM), new StringType("INVALID"));
        verify(getBridgeHandler()).putState(eq(getDeviceID()), eq("Alarm"), alarmStateCaptor.capture());
        AlarmServiceState state = alarmStateCaptor.getValue();
        assertSame(AlarmState.IDLE_OFF, state.value);
    }
}
