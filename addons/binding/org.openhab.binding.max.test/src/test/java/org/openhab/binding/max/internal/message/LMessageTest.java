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
package org.openhab.binding.max.internal.message;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.device.DeviceConfiguration;
import org.openhab.binding.max.internal.device.DeviceInformation;
import org.openhab.binding.max.internal.device.DeviceType;
import org.openhab.binding.max.internal.device.HeatingThermostat;
import org.openhab.binding.max.internal.device.ShutterContact;

/**
 * Tests cases for {@link LMessage}.
 *
 * @author Dominic Lerbs - Initial contribution
 * @author Christoph Weitkamp - OH2 Version and updates
 */
public class LMessageTest {

    private static final String rawData = "L:BgVPngkSEAsLhBkJEhkLJQDAAAsLhwwJEhkRJwDKAAYO8ZIJEhAGBU+kCRIQCwxuRPEaGQMmAMcACwxuQwkSGQgnAM8ACwQd5t0SGQ0oAMsA";

    private final Map<String, Device> testDevices = new HashMap<>();

    private LMessage message;
    private final List<DeviceConfiguration> configurations = new ArrayList<>();

    @Before
    public void setUp() {
        message = new LMessage(rawData);
        createTestDevices();
    }

    private void createTestDevices() {
        addShutterContact("054f9e");
        addShutterContact("0ef192");
        addShutterContact("054fa4");
        addHeatingThermostat("0b8419");
        addHeatingThermostat("0b870c");
        addHeatingThermostat("0c6e43");
        addHeatingThermostat("041de6");
        addHeatingThermostat("0c6e44").setError(true);
    }

    private ShutterContact addShutterContact(String rfAddress) {
        ShutterContact device = new ShutterContact(createConfiguration(DeviceType.ShutterContact, rfAddress));
        testDevices.put(rfAddress, device);
        return device;
    }

    private HeatingThermostat addHeatingThermostat(String rfAddress) {
        HeatingThermostat device = new HeatingThermostat(createConfiguration(DeviceType.HeatingThermostat, rfAddress));
        testDevices.put(rfAddress, device);
        return device;
    }

    private DeviceConfiguration createConfiguration(DeviceType type, String rfAddress) {
        DeviceConfiguration configuration = DeviceConfiguration
                .create(new DeviceInformation(type, "", rfAddress, "", 1));
        configurations.add(configuration);
        return configuration;
    }

    @Test
    public void isCorrectMessageType() {
        MessageType messageType = ((Message) message).getType();
        assertEquals(MessageType.L, messageType);
    }

    @Test
    public void allDevicesCreatedFromMessage() {
        Collection<? extends Device> devices = message.getDevices(configurations);
        assertEquals("Incorrect number of devices created", testDevices.size(), devices.size());
        for (Device device : devices) {
            assertTrue("Unexpected device created: " + device.getRFAddress(),
                    testDevices.containsKey(device.getRFAddress()));
        }
    }

    @Test
    public void isCorrectErrorState() {
        for (Device device : message.getDevices(configurations)) {
            Device testDevice = testDevices.get(device.getRFAddress());
            assertEquals("Error set incorrectly in Device", testDevice.isError(), device.isError());
        }
    }
}
