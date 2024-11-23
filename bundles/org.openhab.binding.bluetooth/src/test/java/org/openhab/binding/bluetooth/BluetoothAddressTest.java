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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link BluetoothAddress}.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class BluetoothAddressTest {

    @Test
    public void testConstructorWithNullParam() {
        assertThrows(IllegalArgumentException.class, () -> new BluetoothAddress(null));
    }

    @Test
    public void testConstructorWithoutColons() {
        assertThrows(IllegalArgumentException.class, () -> new BluetoothAddress("123456789ABC"));
    }

    @Test
    public void testConstructorCorrect() {
        new BluetoothAddress("12:34:56:78:9A:BC");
    }
}
