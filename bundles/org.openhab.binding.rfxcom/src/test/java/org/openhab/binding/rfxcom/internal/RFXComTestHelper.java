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
package org.openhab.binding.rfxcom.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.MockDeviceState;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.binding.rfxcom.internal.messages.RFXComDeviceMessage;
import org.openhab.binding.rfxcom.internal.messages.RFXComMessage;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Helper class for testing the RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComTestHelper {
    public static final ThingUID bridgeUID = new ThingUID("rfxcom", "tcpbridge", "rfxtrx0");
    public static final ThingUID thingUID = new ThingUID("rfxcom", bridgeUID, "mocked");
    public static final ThingTypeUID thingTypeUID = new ThingTypeUID("rfxcom", "raw");

    public static final ChannelUID commandChannelUID = new ChannelUID(thingUID, RFXComBindingConstants.CHANNEL_COMMAND);

    public static void basicBoundaryCheck(PacketType packetType, RFXComMessage message) throws RFXComException {
        // This is a place where its easy to make mistakes in coding, and can result in errors, normally
        // array bounds errors
        byte[] binaryMessage = message.decodeMessage();
        assertEquals(binaryMessage[0], binaryMessage.length - 1, "Wrong packet length");
        assertEquals(packetType.toByte(), binaryMessage[1], "Wrong packet type");
    }

    public static int getActualIntValue(RFXComDeviceMessage msg, RFXComDeviceConfiguration config, String channelId)
            throws RFXComException {
        return ((DecimalType) msg.convertToState(channelId, config, new MockDeviceState())).intValue();
    }
}
