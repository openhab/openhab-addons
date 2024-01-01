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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.*;
import static org.openhab.binding.rfxcom.internal.RFXComTestHelper.*;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.LIGHTING4;
import static org.openhab.binding.rfxcom.internal.messages.RFXComLighting4Message.SubType.PT2262;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.RFXComTestHelper;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.config.RFXComLighting4DeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComInvalidStateException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComLighting4MessageTest {
    public static final ChannelUID contactChannelUID = new ChannelUID(thingUID, CHANNEL_CONTACT);

    public static void checkDiscoveryResult(RFXComDeviceMessage<RFXComLighting4Message.SubType> msg, String deviceId,
            @Nullable Integer pulse, String subType) throws RFXComException {
        String thingUID = "homeduino:rfxcom:fssfsd:thing";
        DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(new ThingUID(thingUID));

        // check whether the pulse is stored
        msg.addDevicePropertiesTo(builder);

        Map<String, Object> properties = builder.build().getProperties();
        assertEquals(deviceId, properties.get("deviceId"), "Device Id");
        assertEquals(subType, properties.get("subType"), "Sub type");
        if (pulse != null) {
            assertEquals(pulse, properties.get("pulse"), "Pulse");
        }
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComLighting4DeviceConfiguration config = new RFXComLighting4DeviceConfiguration();
        config.deviceId = "90000";
        config.subType = "PT2262";
        config.pulse = 300;

        RFXComLighting4Message message = (RFXComLighting4Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(LIGHTING4, config, commandChannelUID, OnOffType.ON);

        byte[] binaryMessage = message.decodeMessage();
        RFXComLighting4Message msg = (RFXComLighting4Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(binaryMessage);

        assertEquals("90000", msg.getDeviceId(), "Sensor Id");
    }

    private void testMessageWithoutCommandIds(String hexMsg, RFXComLighting4Message.SubType subType, String deviceId,
            @Nullable Integer pulse, int commandByte, @Nullable Integer seqNbr, int signalLevel, Command command)
            throws RFXComException {
        // These tests rely on the deprecated behaviour of a "known" set of ON/OFF values and will
        // be removed in a later release to be replaced with test that check we throw an exception
        // if the config isn't specified (see the open/closed tests).
        RFXComLighting4DeviceConfiguration config = new RFXComLighting4DeviceConfiguration();
        config.deviceId = deviceId;
        config.subType = subType.toString();

        RFXComLighting4Message msg = (RFXComLighting4Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(deviceId, msg.getDeviceId(), "Sensor Id");
        assertEquals(commandByte, RFXComTestHelper.getActualIntValue(msg, config, CHANNEL_COMMAND_ID), "Command");
        if (seqNbr != null) {
            assertEquals(seqNbr.shortValue(), (short) (msg.seqNbr & 0xFF), "Seq Number");
        }
        assertEquals(signalLevel, RFXComTestHelper.getActualIntValue(msg, config, CHANNEL_SIGNAL_LEVEL),
                "Signal Level");
        assertEquals(command, msg.convertToCommand(CHANNEL_COMMAND, config, null));

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");

        checkDiscoveryResult(msg, deviceId, pulse, subType.toString());
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessageWithoutCommandIds("091300E1D8AD59018F70", PT2262, "887509", 399, 9, 225, 2, OnOffType.ON);
        testMessageWithoutCommandIds("0913005FA9A9C901A170", PT2262, "694940", 417, 9, 95, 2, OnOffType.ON);
        testMessageWithoutCommandIds("091300021D155C01E960", PT2262, "119125", 489, 12, 2, 2, OnOffType.ON);
        testMessageWithoutCommandIds("091300D345DD99018C50", PT2262, "286169", 396, 9, 211, 2, OnOffType.ON);
        testMessageWithoutCommandIds("09130035D149A2017750", PT2262, "857242", 375, 2, 53, 2, OnOffType.OFF);
        testMessageWithoutCommandIds("0913000B4E462A012280", PT2262, "320610", 290, 10, 11, 3, OnOffType.ON);
        testMessageWithoutCommandIds("09130009232D2E013970", PT2262, "144082", 313, 14, 9, 2, OnOffType.OFF);
        testMessageWithoutCommandIds("091300CA0F8D2801AA70", PT2262, "63698", 426, 8, 202, 2, OnOffType.ON);
    }

    @Test
    public void testSomeAlarmRemote() throws RFXComException {
        testMessageWithoutCommandIds("0913004A0D8998016E60", PT2262, "55449", 366, 8, 74, 2, OnOffType.ON);
    }

    @Test
    public void testCheapPirSensor() throws RFXComException {
        testMessageWithoutCommandIds("091300EF505FC6019670", PT2262, "329212", 406, 6, 239, 2, OnOffType.ON);
    }

    @Test
    public void testSomeConradMessages() throws RFXComException {
        testMessageWithoutCommandIds("0913003554545401A150", PT2262, "345413", 417, 4, 53, 2, OnOffType.OFF);
    }

    @Test
    public void testPhenixMessages() throws RFXComException {
        List<String> onMessages = Arrays.asList("09130046044551013780", "09130048044551013780", "0913004A044551013980",
                "0913004C044551013780", "0913004E044551013780");

        for (String message : onMessages) {
            testMessageWithoutCommandIds(message, PT2262, "17493", null, 1, null, 3, OnOffType.ON);
        }

        List<String> offMessages = Arrays.asList("09130051044554013980", "09130053044554013680", "09130055044554013680",
                "09130057044554013680", "09130059044554013680", "0913005B044554013680", "0913005D044554013480",
                "09130060044554013980", "09130062044554013680", "09130064044554013280");

        for (String message : offMessages) {
            testMessageWithoutCommandIds(message, PT2262, "17493", null, 4, null, 3, OnOffType.OFF);
        }
    }

    private void testRxWithConfig(String hexMsg, RFXComDeviceConfiguration config,
            RFXComLighting4Message.SubType subType, String deviceId, @Nullable Integer pulse, int commandByte,
            @Nullable Integer seqNbr, int signalLevel, ChannelUID channelUID, Command command) throws RFXComException {
        RFXComLighting4Message msg = (RFXComLighting4Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(deviceId, msg.getDeviceId(), "Sensor Id");
        assertEquals(commandByte, RFXComTestHelper.getActualIntValue(msg, config, CHANNEL_COMMAND_ID), "Command");
        if (seqNbr != null) {
            assertEquals(seqNbr.shortValue(), (short) (msg.seqNbr & 0xFF), "Seq Number");
        }
        assertEquals(signalLevel, RFXComTestHelper.getActualIntValue(msg, config, CHANNEL_SIGNAL_LEVEL),
                "Signal Level");
        assertEquals(command, msg.convertToCommand(channelUID.getId(), config, null));

        byte[] decoded = msg.decodeMessage();

        assertEquals(hexMsg, HexUtils.bytesToHex(decoded), "Message converted back");

        checkDiscoveryResult(msg, deviceId, pulse, subType.toString());
    }

    @Test
    public void testRxWithFullConfig() throws RFXComException {
        RFXComLighting4DeviceConfiguration config = new RFXComLighting4DeviceConfiguration();
        config.deviceId = "12345";
        config.subType = PT2262.toString();
        config.onCommandId = 0xA;
        config.offCommandId = 0xB;
        config.openCommandId = 0xC;
        config.closedCommandId = 0xD;

        testRxWithConfig("0913003503039A01A150", config, PT2262, "12345", 417, 0xA, 53, 2, commandChannelUID,
                OnOffType.ON);
        testRxWithConfig("0913003503039B01A150", config, PT2262, "12345", 417, 0xB, 53, 2, commandChannelUID,
                OnOffType.OFF);
        testRxWithConfig("0913003503039C01A150", config, PT2262, "12345", 417, 0xC, 53, 2, contactChannelUID,
                OpenClosedType.OPEN);
        testRxWithConfig("0913003503039D01A150", config, PT2262, "12345", 417, 0xD, 53, 2, contactChannelUID,
                OpenClosedType.CLOSED);
    }

    @Test
    public void testRxWithPartialConfig() throws RFXComException {
        RFXComLighting4DeviceConfiguration config = new RFXComLighting4DeviceConfiguration();
        config.deviceId = "12345";
        config.subType = PT2262.toString();
        config.onCommandId = 0xA;
        config.openCommandId = 0xC;

        testRxWithConfig("0913003503039A01A150", config, PT2262, "12345", 417, 0xA, 53, 2, commandChannelUID,
                OnOffType.ON);
        assertThrows(RFXComInvalidStateException.class, () -> testRxWithConfig("0913003503039B01A150", config, PT2262,
                "12345", 417, 0xB, 53, 2, commandChannelUID, OnOffType.OFF));
        testRxWithConfig("0913003503039C01A150", config, PT2262, "12345", 417, 0xC, 53, 2, contactChannelUID,
                OpenClosedType.OPEN);
        assertThrows(RFXComInvalidStateException.class, () -> testRxWithConfig("0913003503039D01A150", config, PT2262,
                "12345", 417, 0xD, 53, 2, contactChannelUID, OpenClosedType.CLOSED));
    }

    @Test
    public void testRxWithNoConfig() throws RFXComException {
        RFXComLighting4DeviceConfiguration config = new RFXComLighting4DeviceConfiguration();
        config.deviceId = "12345";
        config.subType = PT2262.toString();

        // These will fall back on deprecated behaviour, but should all be assertThrows in the future.
        testRxWithConfig("0913003503039A01A150", config, PT2262, "12345", 417, 0xA, 53, 2, commandChannelUID,
                OnOffType.ON);
        testRxWithConfig("0913003503039B01A150", config, PT2262, "12345", 417, 0xB, 53, 2, commandChannelUID,
                OnOffType.ON);
        testRxWithConfig("0913003503039C01A150", config, PT2262, "12345", 417, 0xC, 53, 2, contactChannelUID,
                OpenClosedType.OPEN);
        testRxWithConfig("0913003503039D01A150", config, PT2262, "12345", 417, 0xD, 53, 2, contactChannelUID,
                OpenClosedType.OPEN);
    }

    private void testTxWithConfig(RFXComDeviceConfiguration config, ChannelUID channelUID, Command command,
            RFXComLighting4Message.SubType subType, String deviceId, @Nullable Integer pulse, int commandByte,
            String hexMsg) throws RFXComException {
        RFXComLighting4Message msg = (RFXComLighting4Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(PacketType.LIGHTING4, config, channelUID, command);
        assertEquals(deviceId, msg.getDeviceId(), "Sensor Id");
        assertEquals(commandByte, RFXComTestHelper.getActualIntValue(msg, config, CHANNEL_COMMAND_ID), "Command");
        assertEquals(0, msg.seqNbr & 0xFF, "Seq Number");
        assertEquals(0, RFXComTestHelper.getActualIntValue(msg, config, CHANNEL_SIGNAL_LEVEL), "Signal Level");
        assertEquals(hexMsg, HexUtils.bytesToHex(msg.decodeMessage()), "Message bytes");
    }

    @Test
    void testTxWithFullConfig() throws RFXComException {
        RFXComLighting4DeviceConfiguration config = new RFXComLighting4DeviceConfiguration();
        config.deviceId = "703696";
        config.subType = PT2262.toString();
        config.onCommandId = 0xA;
        config.offCommandId = 0xB;
        config.openCommandId = 0xC;
        config.closedCommandId = 0xD;
        config.pulse = 417;

        testTxWithConfig(config, commandChannelUID, OnOffType.ON, PT2262, "703696", 417, 0xA, "09130000ABCD0A01A100");
        testTxWithConfig(config, commandChannelUID, OnOffType.OFF, PT2262, "703696", 417, 0xB, "09130000ABCD0B01A100");
        testTxWithConfig(config, contactChannelUID, OpenClosedType.OPEN, PT2262, "703696", 417, 0xC,
                "09130000ABCD0C01A100");
        testTxWithConfig(config, contactChannelUID, OpenClosedType.CLOSED, PT2262, "703696", 417, 0xD,
                "09130000ABCD0D01A100");
    }

    @Test
    void testTxWithPartialConfig() throws RFXComException {
        RFXComLighting4DeviceConfiguration config = new RFXComLighting4DeviceConfiguration();
        config.deviceId = "703696";
        config.subType = PT2262.toString();
        config.onCommandId = 0xA;
        config.openCommandId = 0xC;
        config.pulse = 417;

        testTxWithConfig(config, commandChannelUID, OnOffType.ON, PT2262, "703696", 417, 0xA, "09130000ABCD0A01A100");
        // Falls back on deprecated behaviour, but should be assertThrows in the future.
        testTxWithConfig(config, commandChannelUID, OnOffType.OFF, PT2262, "703696", 417, 0x4, "09130000ABCD0401A100");
        testTxWithConfig(config, contactChannelUID, OpenClosedType.OPEN, PT2262, "703696", 417, 0xC,
                "09130000ABCD0C01A100");
        assertThrows(RFXComInvalidStateException.class, () -> testTxWithConfig(config, contactChannelUID,
                OpenClosedType.CLOSED, PT2262, "703696", 417, 0xD, "??"));
    }

    @Test
    void testTxWithNoConfig() throws RFXComException {
        RFXComLighting4DeviceConfiguration config = new RFXComLighting4DeviceConfiguration();
        config.deviceId = "703696";
        config.subType = PT2262.toString();
        config.pulse = 417;

        // Falls back on deprecated behaviour, but should be assertThrows in the future.
        testTxWithConfig(config, commandChannelUID, OnOffType.ON, PT2262, "703696", 417, 0x1, "09130000ABCD0101A100");
        // Falls back on deprecated behaviour, but should be assertThrows in the future.
        testTxWithConfig(config, commandChannelUID, OnOffType.OFF, PT2262, "703696", 417, 0x4, "09130000ABCD0401A100");
        assertThrows(RFXComInvalidStateException.class, () -> testTxWithConfig(config, contactChannelUID,
                OpenClosedType.OPEN, PT2262, "703696", 417, 0xC, "??"));
        assertThrows(RFXComInvalidStateException.class, () -> testTxWithConfig(config, contactChannelUID,
                OpenClosedType.CLOSED, PT2262, "703696", 417, 0xD, "??"));
    }
}
