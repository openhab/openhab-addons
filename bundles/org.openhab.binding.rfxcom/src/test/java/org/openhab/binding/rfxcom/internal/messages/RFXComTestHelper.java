/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ThingUID;

/**
 * Helper class for testing the RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
class RFXComTestHelper {
    static void basicBoundaryCheck(PacketType packetType, RFXComMessage message) throws RFXComException {
        // This is a place where its easy to make mistakes in coding, and can result in errors, normally
        // array bounds errors
        byte[] binaryMessage = message.decodeMessage();
        assertEquals(binaryMessage[0], binaryMessage.length - 1, "Wrong packet length");
        assertEquals(packetType.toByte(), binaryMessage[1], "Wrong packet type");
    }

    static void checkDiscoveryResult(RFXComDeviceMessage msg, String deviceId, @Nullable Integer pulse, String subType,
            int offCommand, int onCommand) throws RFXComException {
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
        assertEquals(onCommand, properties.get("onCommandId"), "On command");
        assertEquals(offCommand, properties.get("offCommandId"), "Off command");
    }

    static int getActualIntValue(RFXComDeviceMessage msg, String channelId) throws RFXComException {
        return ((DecimalType) msg.convertToState(channelId, new MockDeviceState())).intValue();
    }
}
