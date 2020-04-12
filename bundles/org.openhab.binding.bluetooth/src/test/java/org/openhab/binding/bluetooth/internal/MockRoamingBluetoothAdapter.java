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
package org.openhab.binding.bluetooth.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.MockBluetoothAdapter;

/**
 * Mock implementation of a {@link RoamingBluetoothAdapter}.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class MockRoamingBluetoothAdapter extends MockBluetoothAdapter implements RoamingBluetoothAdapter {

    @Override
    public void addBluetoothAdapter(BluetoothAdapter adapter) {
    }

    @Override
    public void removeBluetoothAdapter(BluetoothAdapter adapter) {
    }

    @Override
    public boolean isDiscoveryEnabled() {
        return true;
    }

}
