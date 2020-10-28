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
package org.openhab.binding.bluetooth.generic.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
class CharacteristicChannelTypeTest {

    @Test
    void isValidUIDTest() {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BluetoothBindingConstants.BINDING_ID,
                "characteristic-advncd-readable-00002a04-0000-1000-8000-00805f9b34fb-Battery_Level");
        assertTrue(CharacteristicChannelType.isValidUID(channelTypeUID));
    }

    @Test
    void invalidBindingIdUIDTest() {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID("mock",
                "characteristic-advncd-readable-00002a04-0000-1000-8000-00805f9b34fb-Battery_Level");
        assertFalse(CharacteristicChannelType.isValidUID(channelTypeUID));
    }

    @Test
    void invalidFormatTest() {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID("mock",
                "characteristic-ggg-readable-00002a04-0000-1000-8000-00805f9b34fb-Battery_Level");
        assertFalse(CharacteristicChannelType.isValidUID(channelTypeUID));
    }
}
