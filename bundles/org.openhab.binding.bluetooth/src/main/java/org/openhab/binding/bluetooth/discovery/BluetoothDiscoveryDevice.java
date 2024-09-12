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
package org.openhab.binding.bluetooth.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCompanyIdentifiers;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.DelegateBluetoothDevice;

/**
 * The {@link BluetoothDiscoveryDevice} is the BluetoothDevice subclass passed to
 * BluetoothDiscoveryParticipants as part of discovery. It includes extra fields
 * provided for the convenience of participant implementations.
 *
 * @author Connor Petty - Initial Contribution
 */
@NonNullByDefault
public class BluetoothDiscoveryDevice extends DelegateBluetoothDevice {

    private @NonNullByDefault({}) BluetoothDevice delegate;

    protected @Nullable String model;
    protected @Nullable String serialNumber;
    protected @Nullable String hardwareRevision;
    protected @Nullable String firmwareRevision;
    protected @Nullable String softwareRevision;

    public BluetoothDiscoveryDevice(BluetoothDevice device) {
        super(device.getAdapter(), device.getAddress());
        this.delegate = device;
    }

    @Override
    protected @Nullable BluetoothDevice getDelegate() {
        return delegate;
    }

    /**
     * Returns the model of the Bluetooth device.
     *
     * @return The devices model, null if not known
     */
    public @Nullable String getModel() {
        return model;
    }

    /**
     * Returns the serial number of the Bluetooth device.
     *
     * @return The serial model, null if not known
     */
    public @Nullable String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Returns the hardware revision of the Bluetooth device.
     *
     * @return The hardware revision, null if not known
     */
    public @Nullable String getHardwareRevision() {
        return hardwareRevision;
    }

    /**
     * Returns the firmware revision of the Bluetooth device.
     *
     * @return The firmware revision, null if not known
     */
    public @Nullable String getFirmwareRevision() {
        return firmwareRevision;
    }

    /**
     * Returns the software revision of the Bluetooth device.
     *
     * @return The software revision, null if not known
     */
    public @Nullable String getSoftwareRevision() {
        return softwareRevision;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BluetoothDevice [address=");
        builder.append(address);
        builder.append(", manufacturer=");

        Integer manufacturer = getManufacturerId();
        builder.append(manufacturer);
        if (BluetoothCompanyIdentifiers.get(manufacturer) != null) {
            builder.append(" (");
            builder.append(BluetoothCompanyIdentifiers.get(manufacturer));
            builder.append(')');
        }
        builder.append(", name=");
        builder.append(getName());
        builder.append(", model=");
        builder.append(model);
        builder.append(", serialNumber=");
        builder.append(serialNumber);
        builder.append(", hardwareRevision=");
        builder.append(hardwareRevision);
        builder.append(", firmwareRevision=");
        builder.append(firmwareRevision);
        builder.append(", softwareRevision=");
        builder.append(softwareRevision);
        builder.append(", rssi=");
        builder.append(getRssi());
        builder.append(']');
        return builder.toString();
    }
}
