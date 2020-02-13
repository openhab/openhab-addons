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

import org.openhab.binding.bluetooth.BluetoothDevice;

/**
 * The {@link BluetoothDeviceSnapshot} acts as a dummy {@link BluetoothDevice} implementation that simply acts as a
 * snapshot for device data at the time of creation.
 *
 * @author Connor Petty - Initial Contribution
 */
public class BluetoothDeviceSnapshot extends BluetoothDevice {

    public BluetoothDeviceSnapshot(BluetoothDevice source) {
        // snapshots don't have adapters so we use null
        super(null, source.getAddress());
        this.txPower = source.getTxPower();
        this.manufacturer = source.getManufacturerId();
        this.name = source.getName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getTxPower(), getManufacturerId(), getName());
    }

    @Override
    public boolean equals(Object obj) {
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

        if (!Objects.equals(this.getAddress(), other.getAddress())) {
            return false;
        }
        if (!Objects.equals(this.getTxPower(), other.getTxPower())) {
            return false;
        }
        if (!Objects.equals(this.getManufacturerId(), other.getManufacturerId())) {
            return false;
        }
        if (!Objects.equals(this.getName(), other.getName())) {
            return false;
        }
        return true;
    }

    /**
     * This method handles merging of
     *
     */
    public boolean merge(BluetoothDeviceSnapshot snapshot) {
        if (snapshot == null) {
            return false;
        }
        BluetoothDeviceSnapshot original = new BluetoothDeviceSnapshot(this);
        if (this.name == null && snapshot.getName() != null) {
            this.name = snapshot.getName();
        }
        if (this.txPower == null && snapshot.getTxPower() != null) {
            this.txPower = snapshot.getTxPower();
        }
        if (this.manufacturer == null && snapshot.getManufacturerId() != null) {
            this.manufacturer = snapshot.getManufacturerId();
        }
        return !this.equals(original);
    }

}
