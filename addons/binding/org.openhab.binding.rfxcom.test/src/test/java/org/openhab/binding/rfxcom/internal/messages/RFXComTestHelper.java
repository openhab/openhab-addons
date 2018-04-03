/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;

/**
 * Helper class for testing the RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComTestHelper {
    static void basicBoundaryCheck(PacketType packetType, RFXComMessage message) throws RFXComException {
        // This is a place where its easy to make mistakes in coding, and can result in errors, normally
        // array bounds errors
        byte[] binaryMessage = message.decodeMessage();
        assertEquals("Wrong packet length", binaryMessage[0], binaryMessage.length - 1);
        assertEquals("Wrong packet type", packetType.toByte(), binaryMessage[1]);
    }

    static void checkDiscoveryResult(RFXComDeviceMessage msg, String deviceId, Integer pulse, String subType,
            int offCommand, int onCommand) throws RFXComException {
        String thingUID = "homeduino:rfxcom:fssfsd:thing";
        DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(new ThingUID(thingUID));

        // check whether the pulse is stored
        msg.addDevicePropertiesTo(builder);

        Map<String, Object> properties = builder.build().getProperties();
        assertEquals("Device Id", deviceId, properties.get("deviceId"));
        assertEquals("Sub type", subType, properties.get("subType"));
        if (pulse != null) {
            assertEquals("Pulse", pulse, properties.get("pulse"));
        }
        assertEquals("On command", onCommand, properties.get("onCommandId"));
        assertEquals("Off command", offCommand, properties.get("offCommandId"));
    }

    static int getActualIntValue(RFXComDeviceMessage msg, String channelId) throws RFXComException {
        return ((DecimalType) msg.convertToState(channelId)).intValue();
    }
}
