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
package org.openhab.binding.bluetooth.discovery.internal;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;

/**
 * The {@link BluetoothDeviceSnapshot} acts as a dummy {@link BluetoothDevice} implementation that simply acts as a
 * snapshot for device data at the time of creation.
 *
 * @author Connor Petty - Initial Contribution
 */
@NonNullByDefault
public class BluetoothDeviceSnapshot extends BluetoothDiscoveryDevice {

    private @Nullable String name;
    private @Nullable Integer manufacturer;
    private @Nullable Integer txPower;

    public BluetoothDeviceSnapshot(BluetoothDevice device) {
        super(device);
        this.txPower = device.getTxPower();
        this.manufacturer = device.getManufacturerId();
        this.name = device.getName();
    }

    @Override
    public @Nullable String getName() {
        return name;
    }

    /**
     * Set the name of the device
     *
     * @param name a {@link String} defining the device name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the manufacturer id for the device
     *
     * @param manufacturer the manufacturer id
     */
    public void setManufacturerId(int manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Returns the manufacturer ID of the device
     *
     * @return an integer with manufacturer ID of the device, or null if not known
     */
    @Override
    public @Nullable Integer getManufacturerId() {
        return manufacturer;
    }

    /**
     * Sets the device transmit power
     *
     * @param txPower the current transmitter power in dBm
     */
    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    /**
     * Returns the last Transmit Power value or null if no transmit power has been received
     *
     * @return the last reported transmitter power value in dBm
     */
    @Override
    public @Nullable Integer getTxPower() {
        return txPower;
    }

    /**
     * Set the model of the device
     *
     * @param model a {@link String} defining the device model
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Set the serial number of the device
     *
     * @param serialNumber a {@link String} defining the serial number
     */
    public void setSerialNumberl(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Set the hardware revision of the device
     *
     * @param hardwareRevision a {@link String} defining the hardware revision
     */
    public void setHardwareRevision(String hardwareRevision) {
        this.hardwareRevision = hardwareRevision;
    }

    /**
     * Set the firmware revision of the device
     *
     * @param firmwareRevision a {@link String} defining the firmware revision
     */
    public void setFirmwareRevision(String firmwareRevision) {
        this.firmwareRevision = firmwareRevision;
    }

    /**
     * Set the software revision of the device
     *
     * @param softwareRevision a {@link String} defining the software revision
     */
    public void setSoftwareRevision(String softwareRevision) {
        this.softwareRevision = softwareRevision;
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
        return Objects.equals(softwareRevision, other.softwareRevision);
    }

    /**
     * This merges non-null identity fields from the given device into this snapshot.
     */
    public void merge(BluetoothDeviceSnapshot device) {
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
