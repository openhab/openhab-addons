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
package org.openhab.binding.bluetooth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link BluetoothCharacteristic}.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public class CharacteristicPropertiesTest {
    private BluetoothCharacteristic characteristic = new BluetoothCharacteristic(UUID.randomUUID(), 0);

    @Test
    public void testAllSupportedProperties() {
        // given
        // when
        int properties = 0;
        properties |= BluetoothCharacteristic.PROPERTY_BROADCAST;
        properties |= BluetoothCharacteristic.PROPERTY_READ;
        properties |= BluetoothCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
        properties |= BluetoothCharacteristic.PROPERTY_WRITE;
        properties |= BluetoothCharacteristic.PROPERTY_NOTIFY;
        properties |= BluetoothCharacteristic.PROPERTY_INDICATE;
        characteristic.setProperties(properties);

        // then
        assertTrue(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_BROADCAST), "Broastcast not set");
        assertTrue(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_READ), "Read not set");
        assertTrue(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_WRITE_NO_RESPONSE),
                "Write not response not set");
        assertTrue(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_WRITE), "Write not set");
        assertTrue(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_NOTIFY), "Notify not set");
        assertTrue(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_INDICATE), "Indicate not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_SIGNED_WRITE),
                "Signed write set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_EXTENDED_PROPS),
                "Extended props set");
    }

    @Test
    public void testNoProperties() {
        // given
        // when
        int properties = 0;
        characteristic.setProperties(properties);

        // then
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_BROADCAST),
                "Broastcast not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_READ), "Read not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_WRITE_NO_RESPONSE),
                "Write not response not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_WRITE), "Write not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_NOTIFY), "Notify not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_INDICATE), "Indicate not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_SIGNED_WRITE),
                "Signed write set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_EXTENDED_PROPS),
                "Extended props set");
    }

    @Test
    public void testSomeSupportedProperties() {
        // given
        // when
        int properties = 0;
        properties |= BluetoothCharacteristic.PROPERTY_READ;
        properties |= BluetoothCharacteristic.PROPERTY_NOTIFY;
        characteristic.setProperties(properties);

        // then
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_BROADCAST),
                "Broastcast not set");
        assertTrue(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_READ), "Read not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_WRITE_NO_RESPONSE),
                "Write not response not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_WRITE), "Write not set");
        assertTrue(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_NOTIFY), "Notify not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_INDICATE), "Indicate not set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_SIGNED_WRITE),
                "Signed write set");
        assertFalse(characteristic.hasPropertyEnabled(BluetoothCharacteristic.PROPERTY_EXTENDED_PROPS),
                "Extended props set");
    }
}
