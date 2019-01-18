/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bluetooth;

import org.junit.Test;

public class BluetoothAddressTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullParam() {
        new BluetoothAddress(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithoutColons() {
        new BluetoothAddress("123456789ABC");
    }

    @Test
    public void testConstructorCorrect() {
        new BluetoothAddress("12:34:56:78:9A:BC");
    }
}
