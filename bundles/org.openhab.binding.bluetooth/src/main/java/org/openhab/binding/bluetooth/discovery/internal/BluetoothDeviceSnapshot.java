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
package org.openhab.binding.bluetooth.discovery.internal;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothDevice;

/**
 * The {@link BluetoothDeviceSnapshot} acts as a dummy {@link BluetoothDevice} implementation that simply acts as a
 * snapshot for device data at the time of creation.
 *
 * @author Connor Petty - Initial Contribution
 */
@NonNullByDefault
public class BluetoothDeviceSnapshot {

    private @Nullable BluetoothAddress address;
    private @Nullable Integer txPower;
    private @Nullable Integer manufacturer;
    private @Nullable String name;

    public BluetoothDeviceSnapshot() {
    }

    public BluetoothDeviceSnapshot(BluetoothDevice device) {
        this.address = device.getAddress();
        this.txPower = device.getTxPower();
        this.manufacturer = device.getManufacturerId();
        this.name = device.getName();
    }

    public BluetoothDeviceSnapshot(BluetoothDeviceSnapshot device) {
        this.address = device.address;
        this.txPower = device.txPower;
        this.manufacturer = device.manufacturer;
        this.name = device.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        Integer manufacturerLocal = manufacturer;
        result = prime * result + ((manufacturerLocal == null) ? 0 : manufacturerLocal.hashCode());
        String nameLocal = name;
        result = prime * result + ((nameLocal == null) ? 0 : nameLocal.hashCode());
        Integer txPowerLocal = txPower;
        result = prime * result + ((txPowerLocal == null) ? 0 : txPowerLocal.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BluetoothDeviceSnapshot other = (BluetoothDeviceSnapshot) obj;
        if (!Objects.equals(address, other.address)) {
            return false;
        }
        if (!Objects.equals(manufacturer, other.manufacturer)) {
            return false;
        }
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (!Objects.equals(txPower, other.txPower)) {
            return false;
        }
        return true;
    }

    /**
     * This merges non-null identity fields from the given device into this snapshot.
     *
     * @return true if this snapshot changed as a result of this operation
     */
    public boolean merge(BluetoothDevice device) {
        BluetoothDeviceSnapshot original = new BluetoothDeviceSnapshot(this);
        BluetoothAddress deviceAddr = device.getAddress();
        if (this.address == null && deviceAddr != null) {
            this.address = deviceAddr;
        }
        String deviceName = device.getName();
        if (this.name == null && deviceName != null) {
            this.name = deviceName;
        }
        Integer deviceTxPower = device.getTxPower();
        if (this.txPower == null && deviceTxPower != null) {
            this.txPower = deviceTxPower;
        }
        Integer deviceManufacturer = device.getManufacturerId();
        if (this.manufacturer == null && deviceManufacturer != null) {
            this.manufacturer = deviceManufacturer;
        }
        return !this.equals(original);
    }

}
