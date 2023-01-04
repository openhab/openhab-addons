/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
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

    @Test
    public void testProcessUpdate_BatteryLevel_LowBattery() {
        JsonElement deviceServiceData = JsonParser.parseString("{ \n" + "    \"@type\":\"DeviceServiceData\",\n"
                + "    \"path\":\"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel\",\n"
                + "    \"id\":\"BatteryLevel\",\n" + "    \"deviceId\":\"hdm:ZigBee:000d6f0004b93361\",\n"
                + "    \"faults\":{ \n" + "        \"entries\":[\n" + "          {\n"
                + "            \"type\":\"LOW_BATTERY\",\n" + "            \"category\":\"WARNING\"\n" + "          }\n"
                + "        ]\n" + "    }\n" + "}");
        getFixture().processUpdate("BatteryLevel", deviceServiceData);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL),
                new DecimalType(10));
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LOW_BATTERY), OnOffType.ON);
    }

    @Test
    public void testProcessUpdate_BatteryLevel_CriticalLow() {
        JsonElement deviceServiceData = JsonParser.parseString("{ \n" + "    \"@type\":\"DeviceServiceData\",\n"
                + "    \"path\":\"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel\",\n"
                + "    \"id\":\"BatteryLevel\",\n" + "    \"deviceId\":\"hdm:ZigBee:000d6f0004b93361\",\n"
                + "    \"faults\":{ \n" + "        \"entries\":[\n" + "          {\n"
                + "            \"type\":\"CRITICAL_LOW\",\n" + "            \"category\":\"WARNING\"\n"
                + "          }\n" + "        ]\n" + "    }\n" + "}");
        getFixture().processUpdate("BatteryLevel", deviceServiceData);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL),
                new DecimalType(1));
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LOW_BATTERY), OnOffType.ON);
    }

    @Test
    public void testProcessUpdate_BatteryLevel_CriticallyLowBattery() {
        JsonElement deviceServiceData = JsonParser.parseString("{ \n" + "    \"@type\":\"DeviceServiceData\",\n"
                + "    \"path\":\"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel\",\n"
                + "    \"id\":\"BatteryLevel\",\n" + "    \"deviceId\":\"hdm:ZigBee:000d6f0004b93361\",\n"
                + "    \"faults\":{ \n" + "        \"entries\":[\n" + "          {\n"
                + "            \"type\":\"CRITICALLY_LOW_BATTERY\",\n" + "            \"category\":\"WARNING\"\n"
                + "          }\n" + "        ]\n" + "    }\n" + "}");
        getFixture().processUpdate("BatteryLevel", deviceServiceData);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL),
                new DecimalType(1));
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LOW_BATTERY), OnOffType.ON);
    }

    @Test
    public void testProcessUpdate_BatteryLevel_OK() {
        JsonElement deviceServiceData = JsonParser.parseString("{ \n" + "    \"@type\":\"DeviceServiceData\",\n"
                + "    \"path\":\"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel\",\n"
                + "    \"id\":\"BatteryLevel\",\n" + "    \"deviceId\":\"hdm:ZigBee:000d6f0004b93361\" }");
        getFixture().processUpdate("BatteryLevel", deviceServiceData);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL),
                new DecimalType(100));
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LOW_BATTERY), OnOffType.OFF);
    }

    @Test
    public void testProcessUpdate_BatteryLevel_NotAvailable() {
        JsonElement deviceServiceData = JsonParser.parseString("{ \n" + "    \"@type\":\"DeviceServiceData\",\n"
                + "    \"path\":\"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel\",\n"
                + "    \"id\":\"BatteryLevel\",\n" + "    \"deviceId\":\"hdm:ZigBee:000d6f0004b93361\",\n"
                + "    \"faults\":{ \n" + "        \"entries\":[\n" + "          {\n"
                + "            \"type\":\"NOT_AVAILABLE\",\n" + "            \"category\":\"WARNING\"\n"
                + "          }\n" + "        ]\n" + "    }\n" + "}");
        getFixture().processUpdate("BatteryLevel", deviceServiceData);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_BATTERY_LEVEL), UnDefType.UNDEF);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_LOW_BATTERY), OnOffType.OFF);
    }
}
