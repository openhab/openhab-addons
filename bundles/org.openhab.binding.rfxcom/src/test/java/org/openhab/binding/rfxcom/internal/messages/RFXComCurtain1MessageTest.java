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
package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.RFXComTestHelper.thingUID;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.CURTAIN1;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.RFXComBindingConstants;
import org.openhab.binding.rfxcom.internal.RFXComTestHelper;
import org.openhab.binding.rfxcom.internal.config.RFXComGenericDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComCurtain1MessageTest {
    private static ChannelUID shutterChannelUID = new ChannelUID(thingUID, RFXComBindingConstants.CHANNEL_SHUTTER);
    private static RFXComGenericDeviceConfiguration config = new RFXComGenericDeviceConfiguration();

    static {
        config.deviceId = "1.2";
        config.subType = RFXComCurtain1Message.SubType.HARRISON.toString();
    }

    @Test
    public void checkForSupportTest() throws RFXComException {
        RFXComMessageFactoryImpl.INSTANCE.createMessage(CURTAIN1, config, shutterChannelUID, OpenClosedType.OPEN);
    }

    @Test
    public void basicBoundaryCheck() throws RFXComException {
        RFXComCurtain1Message message = (RFXComCurtain1Message) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(CURTAIN1, config, shutterChannelUID, OpenClosedType.OPEN);

        RFXComTestHelper.basicBoundaryCheck(CURTAIN1, message);
    }

    // TODO please add tests for real messages
}
