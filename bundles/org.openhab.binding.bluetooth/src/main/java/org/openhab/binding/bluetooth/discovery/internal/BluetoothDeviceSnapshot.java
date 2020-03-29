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

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.BluetoothService;

/**
 * The {@link BluetoothDeviceSnapshot} acts as a dummy {@link BluetoothDevice} implementation that simply acts as a
 * snapshot for device data at the time of creation.
 *
 * @author Connor Petty - Initial Contribution
 */
@NonNullByDefault
public class BluetoothDeviceSnapshot extends BluetoothDevice {

    private BluetoothDevice delegate;

    public BluetoothDeviceSnapshot(BluetoothDevice device) {
        super(device.getAdapter(), device.getAddress());
        this.delegate = device;
        this.txPower = device.getTxPower();
        this.manufacturer = device.getManufacturerId();
        this.name = device.getName();
        this.model = device.getModel();
        this.serialNumber = device.getSerialNumber();
        this.hardwareRevision = device.getHardwareRevision();
        this.firmwareRevision = device.getFirmwareRevision();
        this.softwareRevision = device.getSoftwareRevision();
    }

    @Override
    public boolean connect() {
        return delegate.connect();
    }

    @Override
    public boolean disconnect() {
        return delegate.disconnect();
    }

    @Override
    public boolean enableNotifications(BluetoothCharacteristic characteristic) {
        return delegate.enableNotifications(characteristic);
    }

    @Override
    public boolean enableNotifications(BluetoothDescriptor descriptor) {
        return delegate.enableNotifications(descriptor);
    }

    @Override
    public boolean disableNotifications(BluetoothCharacteristic characteristic) {
        return delegate.disableNotifications(characteristic);
    }

    @Override
    public boolean disableNotifications(BluetoothDescriptor descriptor) {
        return delegate.disableNotifications(descriptor);
    }

    @Override
    public boolean discoverServices() {
        return delegate.discoverServices();
    }

    @Override
    public void addListener(BluetoothDeviceListener listener) {
        delegate.addListener(listener);
    }

    @Override
    public void removeListener(BluetoothDeviceListener listener) {
        delegate.removeListener(listener);
    }

    @Override
    public ConnectionState getConnectionState() {
        return delegate.getConnectionState();
    }

    @Override
    public Collection<BluetoothService> getServices() {
        return delegate.getServices();
    }

    @Override
    public @Nullable BluetoothService getServices(UUID uuid) {
        return delegate.getServices(uuid);
    }

    @Override
    public @Nullable BluetoothCharacteristic getCharacteristic(UUID uuid) {
        return delegate.getCharacteristic(uuid);
    }

    @Override
    public boolean hasListeners() {
        return delegate.hasListeners();
    }

    @Override
    public boolean supportsService(UUID uuid) {
        return delegate.supportsService(uuid);
    }

    @Override
    public boolean readCharacteristic(BluetoothCharacteristic characteristic) {
        return delegate.readCharacteristic(characteristic);
    }

    @Override
    public boolean writeCharacteristic(BluetoothCharacteristic characteristic) {
        return delegate.writeCharacteristic(characteristic);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        BluetoothAddress address = this.address;
        Integer manufacturer = this.manufacturer;
        Integer txPower = this.txPower;
        String name = this.name;

        String model = this.model;
        String serialNumber = this.serialNumber;
        String hardwareRevision = this.hardwareRevision;
        String firmwareRevision = this.firmwareRevision;
        String softwareRevision = this.softwareRevision;

        result = prime * result + address.hashCode();
        result = prime * result + ((firmwareRevision == null) ? 0 : firmwareRevision.hashCode());
        result = prime * result + ((hardwareRevision == null) ? 0 : hardwareRevision.hashCode());
        result = prime * result + ((manufacturer == null) ? 0 : manufacturer.hashCode());
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((serialNumber == null) ? 0 : serialNumber.hashCode());
        result = prime * result + ((softwareRevision == null) ? 0 : softwareRevision.hashCode());
        result = prime * result + ((txPower == null) ? 0 : txPower.hashCode());
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
        if (!Objects.equals(txPower, other.txPower)) {
            return false;
        }
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (!Objects.equals(model, other.model)) {
            return false;
        }
        if (!Objects.equals(serialNumber, other.serialNumber)) {
            return false;
        }
        if (!Objects.equals(hardwareRevision, other.hardwareRevision)) {
            return false;
        }
        if (!Objects.equals(firmwareRevision, other.firmwareRevision)) {
            return false;
        }
        if (!Objects.equals(softwareRevision, other.softwareRevision)) {
            return false;
        }
        return true;
    }

    /**
     * This merges non-null identity fields from the given device into this snapshot.
     *
     * @return true if this snapshot changed as a result of this operation
     */
    public void merge(BluetoothDevice device) {

        Integer txPower = device.getTxPower();
        Integer manufacturer = device.getManufacturerId();
        String name = device.getName();

        String model = device.getModel();
        String serialNumber = device.getSerialNumber();
        String hardwareRevision = device.getHardwareRevision();
        String firmwareRevision = device.getFirmwareRevision();
        String softwareRevision = device.getSoftwareRevision();

        if (this.txPower == null && txPower != null) {
            this.txPower = txPower;
        }
        if (this.manufacturer == null && manufacturer != null) {
            this.manufacturer = manufacturer;
        }
        if (this.name == null && name != null) {
            this.name = name;
        }
        if (this.model == null && model != null) {
            this.model = model;
        }
        if (this.serialNumber == null && serialNumber != null) {
            this.serialNumber = serialNumber;
        }
        if (this.hardwareRevision == null && hardwareRevision != null) {
            this.hardwareRevision = hardwareRevision;
        }
        if (this.firmwareRevision == null && firmwareRevision != null) {
            this.firmwareRevision = firmwareRevision;
        }
        if (this.softwareRevision == null && softwareRevision != null) {
            this.softwareRevision = softwareRevision;
        }
    }

}
