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
package org.openhab.binding.innogysmarthome.internal.client.entity.device;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.innogysmarthome.internal.client.entity.message.Message;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.BooleanState;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class DeviceTest {

    @Test
    public void testSetMessageListLowBatteryMessage() {
        Device device = createDevice();

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        device.setMessageList(Collections.singletonList(createMessage(Message.TYPE_DEVICE_LOW_BATTERY)));

        assertTrue(device.isReachable());
        assertTrue(device.hasLowBattery());
    }

    @Test
    public void testSetMessageListUnreachableMessage() {
        Device device = createDevice();

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        device.setMessageList(Collections.singletonList(createMessage(Message.TYPE_DEVICE_UNREACHABLE)));

        assertFalse(device.isReachable());
        assertFalse(device.hasLowBattery());
    }

    @Test
    public void testSetMessageListResetByEmpty() {
        Device device = createDevice();

        assertNull(device.getMessageList());
        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        List<Message> messages = Arrays.asList(createMessage(Message.TYPE_DEVICE_LOW_BATTERY),
                createMessage(Message.TYPE_DEVICE_UNREACHABLE));
        device.setMessageList(messages);

        assertEquals(messages, device.getMessageList());
        assertFalse(device.isReachable());
        assertTrue(device.hasLowBattery());

        device.setMessageList(Collections.emptyList());

        // Nothing should get changed.
        // New messages are only set in real-life when the device is refreshed with new data of the API.
        // Therefore the data of the API should be kept / not overwritten when no corresponding messages are available.
        assertEquals(Collections.emptyList(), device.getMessageList());
        assertFalse(device.isReachable());
        assertTrue(device.hasLowBattery());
    }

    @Test
    public void testSetMessageListResetByNULL() {
        Device device = createDevice();

        assertNull(device.getMessageList());
        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        List<Message> messages = Arrays.asList(createMessage(Message.TYPE_DEVICE_LOW_BATTERY),
                createMessage(Message.TYPE_DEVICE_UNREACHABLE));
        device.setMessageList(messages);

        assertEquals(messages, device.getMessageList());
        assertFalse(device.isReachable());
        assertTrue(device.hasLowBattery());

        device.setMessageList(null);

        // Nothing should get changed.
        // New messages are only set in real-life when the device is refreshed with new data of the API.
        // Therefore the data of the API should be kept / not overwritten when no corresponding messages are available.
        assertNull(device.getMessageList());
        assertFalse(device.isReachable());
        assertTrue(device.hasLowBattery());
    }

    @Test
    public void testSetMessageListResetByUnimportantMessage() {
        Device device = createDevice();

        assertNull(device.getMessageList());
        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        List<Message> messages = Arrays.asList(createMessage(Message.TYPE_DEVICE_LOW_BATTERY),
                createMessage(Message.TYPE_DEVICE_UNREACHABLE));
        device.setMessageList(messages);

        assertEquals(messages, device.getMessageList());
        assertFalse(device.isReachable());
        assertTrue(device.hasLowBattery());

        messages = Collections.singletonList(createMessage("UNKNOWN"));
        device.setMessageList(messages);

        // Nothing should get changed.
        // New messages are only set in real-life when the device is refreshed with new data of the API.
        // Therefore the data of the API should be kept / not overwritten when no corresponding messages are available.
        assertEquals(messages, device.getMessageList());
        assertFalse(device.isReachable());
        assertTrue(device.hasLowBattery());
    }

    @Test
    public void testSetMessageListUnimportantMessage() {
        Device device = createDevice();

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        device.setMessageList(Collections.singletonList(createMessage("UNKNOWN")));

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());
    }

    private Message createMessage(String messageType) {
        Message message = new Message();
        message.setType(messageType);
        return message;
    }

    @Test
    public void testSetMessageListNULL() {
        Device device = createDevice();

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        device.setMessageList(null);

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());
    }

    @Test
    public void testSetMessageListEmpty() {
        Device device = createDevice();

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        device.setMessageList(Collections.emptyList());

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());
    }

    private static Device createDevice() {
        BooleanState isReachableState = new BooleanState();
        isReachableState.setValue(true);

        State state = new State();
        state.setIsReachable(isReachableState);

        DeviceState deviceState = new DeviceState();
        deviceState.setState(state);

        Device device = new Device();
        device.setDeviceState(deviceState);
        return device;
    }
}
