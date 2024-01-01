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
package org.openhab.binding.livisismarthome.internal.client.entity.device;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.DeviceStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.device.StateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.message.MessageDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.BooleanStateDTO;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class DeviceDTOTest {

    @Test
    public void testSetMessageListLowBatteryMessage() {
        DeviceDTO device = createDevice();

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        device.setMessageList(List.of(createMessage(MessageDTO.TYPE_DEVICE_LOW_BATTERY)));

        assertTrue(device.isReachable());
        assertTrue(device.hasLowBattery());
    }

    @Test
    public void testSetMessageListUnreachableMessage() {
        DeviceDTO device = createDevice();

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        device.setMessageList(List.of(createMessage(MessageDTO.TYPE_DEVICE_UNREACHABLE)));

        assertFalse(device.isReachable());
        assertFalse(device.hasLowBattery());
    }

    @Test
    public void testSetMessageListResetByEmpty() {
        DeviceDTO device = createDevice();

        assertTrue(device.getMessageList().isEmpty());
        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        List<MessageDTO> messages = Arrays.asList(createMessage(MessageDTO.TYPE_DEVICE_LOW_BATTERY),
                createMessage(MessageDTO.TYPE_DEVICE_UNREACHABLE));
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
        DeviceDTO device = createDevice();

        assertTrue(device.getMessageList().isEmpty());
        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        List<MessageDTO> messages = Arrays.asList(createMessage(MessageDTO.TYPE_DEVICE_LOW_BATTERY),
                createMessage(MessageDTO.TYPE_DEVICE_UNREACHABLE));
        device.setMessageList(messages);

        assertEquals(messages, device.getMessageList());
        assertFalse(device.isReachable());
        assertTrue(device.hasLowBattery());

        device.setMessageList(null);

        // Nothing should get changed.
        // New messages are only set in real-life when the device is refreshed with new data of the API.
        // Therefore the data of the API should be kept / not overwritten when no corresponding messages are available.
        assertTrue(device.getMessageList().isEmpty());
        assertFalse(device.isReachable());
        assertTrue(device.hasLowBattery());
    }

    @Test
    public void testSetMessageListResetByUnimportantMessage() {
        DeviceDTO device = createDevice();

        assertTrue(device.getMessageList().isEmpty());
        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        List<MessageDTO> messages = Arrays.asList(createMessage(MessageDTO.TYPE_DEVICE_LOW_BATTERY),
                createMessage(MessageDTO.TYPE_DEVICE_UNREACHABLE));
        device.setMessageList(messages);

        assertEquals(messages, device.getMessageList());
        assertFalse(device.isReachable());
        assertTrue(device.hasLowBattery());

        messages = List.of(createMessage("UNKNOWN"));
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
        DeviceDTO device = createDevice();

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        device.setMessageList(List.of(createMessage("UNKNOWN")));

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());
    }

    private MessageDTO createMessage(String messageType) {
        MessageDTO message = new MessageDTO();
        message.setType(messageType);
        return message;
    }

    @Test
    public void testSetMessageListNULL() {
        DeviceDTO device = createDevice();

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        device.setMessageList(null);

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());
    }

    @Test
    public void testSetMessageListEmpty() {
        DeviceDTO device = createDevice();

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());

        device.setMessageList(Collections.emptyList());

        assertTrue(device.isReachable());
        assertFalse(device.hasLowBattery());
    }

    private static DeviceDTO createDevice() {
        BooleanStateDTO isReachableState = new BooleanStateDTO();
        isReachableState.setValue(true);

        StateDTO state = new StateDTO();
        state.setIsReachable(isReachableState);

        DeviceStateDTO deviceState = new DeviceStateDTO();
        deviceState.setState(state);

        DeviceDTO device = new DeviceDTO();
        device.setDeviceState(deviceState);
        return device;
    }
}
