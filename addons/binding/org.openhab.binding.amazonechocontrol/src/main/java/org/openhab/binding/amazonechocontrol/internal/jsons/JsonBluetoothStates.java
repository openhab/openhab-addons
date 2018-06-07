/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;

/**
 * The {@link JsonBluetoothStates} encapsulate the GSON data of bluetooth state
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonBluetoothStates {

    public @Nullable BluetoothState findStateByDevice(@Nullable Device device) {
        if (device == null) {
            return null;
        }
        @Nullable
        BluetoothState @Nullable [] bluetoothStates = this.bluetoothStates;
        if (bluetoothStates == null) {
            return null;
        }
        for (BluetoothState state : bluetoothStates) {
            if (state != null && StringUtils.equals(state.deviceSerialNumber, device.serialNumber)) {
                return state;
            }
        }
        return null;
    }

    public @Nullable BluetoothState @Nullable [] bluetoothStates;

    public class PairedDevice {
        public @Nullable String address;
        public boolean connected;
        public @Nullable String deviceClass;
        public @Nullable String friendlyName;
        public @Nullable String @Nullable [] profiles;

    }

    public class BluetoothState {
        public @Nullable String deviceSerialNumber;
        public @Nullable String deviceType;
        public @Nullable String friendlyName;
        public boolean gadgetPaired;
        public boolean online;
        public @Nullable PairedDevice @Nullable [] pairedDeviceList;

    }
}
