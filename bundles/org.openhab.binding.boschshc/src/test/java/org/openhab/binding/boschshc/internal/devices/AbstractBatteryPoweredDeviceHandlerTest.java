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
package org.openhab.binding.boschshc.internal.devices;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Abstract test implementation for battery-powered devices.
 *
 * @author David Pace - Initial contribution
 *
 * @param <T> type of the battery-powered device to be tested
 */
@NonNullByDefault
public abstract class AbstractBatteryPoweredDeviceHandlerTest<T extends AbstractBatteryPoweredDeviceHandler>
        extends AbstractBoschSHCDeviceHandlerTest<T> {

    @BeforeEach
    @Override
    public void beforeEach(TestInfo testInfo)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        DeviceServiceData deviceServiceData = new DeviceServiceData();
        deviceServiceData.path = "/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel";
        deviceServiceData.id = "BatteryLevel";
        deviceServiceData.deviceId = "hdm:ZigBee:000d6f0004b93361";
        when(getBridgeHandler().getServiceData(anyString(), anyString())).thenReturn(deviceServiceData);

        super.beforeEach(testInfo);
    }

    @Test
    public void testProcessUpdateBatteryLevelLowBattery() {
        JsonElement deviceServiceData = JsonParser.parseString("""
                {
                    "@type":"DeviceServiceData",
                    "path":"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel",
                    "id":"BatteryLevel",
                    "deviceId":"hdm:ZigBee:000d6f0004b93361",
                    "faults":{
                        "entries":[
                          {
                            "type":"LOW_BATTERY",
                            "category":"WARNING"
                          }
                        ]
                    }
                }\
                """);
        getFixture().processUpdate("BatteryLevel", deviceServiceData);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL),
                new DecimalType(10));
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_LOW_BATTERY), OnOffType.ON);
    }

    @Test
    public void testProcessUpdateBatteryLevelCriticalLow() {
        JsonElement deviceServiceData = JsonParser.parseString("""
                {
                    "@type":"DeviceServiceData",
                    "path":"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel",
                    "id":"BatteryLevel",
                    "deviceId":"hdm:ZigBee:000d6f0004b93361",
                    "faults":{
                        "entries":[
                          {
                            "type":"CRITICAL_LOW",
                            "category":"WARNING"
                          }
                        ]
                    }
                }\
                """);
        getFixture().processUpdate("BatteryLevel", deviceServiceData);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL),
                new DecimalType(1));
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_LOW_BATTERY), OnOffType.ON);
    }

    @Test
    public void testProcessUpdateBatteryLevelCriticallyLowBattery() {
        JsonElement deviceServiceData = JsonParser.parseString("""
                {
                    "@type":"DeviceServiceData",
                    "path":"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel",
                    "id":"BatteryLevel",
                    "deviceId":"hdm:ZigBee:000d6f0004b93361",
                    "faults":{
                        "entries":[
                          {
                            "type":"CRITICALLY_LOW_BATTERY",
                            "category":"WARNING"
                          }
                        ]
                    }
                }\
                """);
        getFixture().processUpdate("BatteryLevel", deviceServiceData);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL),
                new DecimalType(1));
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_LOW_BATTERY), OnOffType.ON);
    }

    @Test
    public void testProcessUpdateBatteryLevelOK() {
        JsonElement deviceServiceData = JsonParser.parseString("""
                {
                    "@type":"DeviceServiceData",
                    "path":"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel",
                    "id":"BatteryLevel",
                    "deviceId":"hdm:ZigBee:000d6f0004b93361" }\
                """);
        getFixture().processUpdate("BatteryLevel", deviceServiceData);
        // state is updated twice: via short poll in initialize() and via long poll result in this test
        verify(getCallback(), times(2)).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL),
                new DecimalType(100));
        verify(getCallback(), times(2)).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LOW_BATTERY), OnOffType.OFF);
    }

    @Test
    public void testProcessUpdateBatteryLevelNotAvailable() {
        JsonElement deviceServiceData = JsonParser.parseString("""
                {
                    "@type":"DeviceServiceData",
                    "path":"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel",
                    "id":"BatteryLevel",
                    "deviceId":"hdm:ZigBee:000d6f0004b93361",
                    "faults":{
                        "entries":[
                          {
                            "type":"NOT_AVAILABLE",
                            "category":"WARNING"
                          }
                        ]
                    }
                }\
                """);
        getFixture().processUpdate("BatteryLevel", deviceServiceData);
        verify(getCallback()).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL),
                UnDefType.UNDEF);
        // state is updated twice: via short poll in initialize() and via long poll result in this test
        verify(getCallback(), times(2)).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_LOW_BATTERY),
                OnOffType.OFF);
    }

    @Test
    public void testHandleCommandRefreshBatteryLevelChannel() {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL), RefreshType.REFRESH);
        // state is updated twice: via short poll in initialize() and via long poll result in this test
        verify(getCallback(), times(2)).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL),
                new DecimalType(100));
    }

    @Test
    public void testHandleCommandRefreshLowBatteryChannel() {
        getFixture().handleCommand(getChannelUID(BoschSHCBindingConstants.CHANNEL_LOW_BATTERY), RefreshType.REFRESH);
        // state is updated twice: via short poll in initialize() and via long poll result in this test
        verify(getCallback(), times(2)).stateUpdated(getChannelUID(BoschSHCBindingConstants.CHANNEL_LOW_BATTERY),
                OnOffType.OFF);
    }
}
