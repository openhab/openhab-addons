/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.blukii.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.blukii.BlukiiBindingConstants;
import org.openhab.binding.bluetooth.blukii.internal.data.BlukiiData;
import org.openhab.binding.bluetooth.blukii.internal.data.BlukiiDataDecoder;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;

/**
 * The {@link BlukiiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Markus Rathgeb - Split data (decoding and types) and handler
 */
@NonNullByDefault
public class BlukiiHandler extends BeaconBluetoothHandler implements BluetoothDeviceListener {

    private final BlukiiDataDecoder decoder = new BlukiiDataDecoder();

    public BlukiiHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        final byte[] manufacturerData = scanNotification.getManufacturerData();
        if (manufacturerData != null) {
            final BlukiiData blukiiData = decoder.decode(manufacturerData);
            if (blukiiData != null) {
                updateState(BlukiiBindingConstants.CHANNEL_ID_BATTERY, new DecimalType(blukiiData.battery));
                blukiiData.environment.ifPresent(environment -> {
                    updateState(BlukiiBindingConstants.CHANNEL_ID_TEMPERATURE,
                            new QuantityType<>(environment.temperature, SIUnits.CELSIUS));
                    updateState(BlukiiBindingConstants.CHANNEL_ID_HUMIDITY,
                            new QuantityType<>(environment.humidity, Units.PERCENT));
                    updateState(BlukiiBindingConstants.CHANNEL_ID_PRESSURE,
                            new QuantityType<>(environment.pressure, MetricPrefix.HECTO(SIUnits.PASCAL)));
                    updateState(BlukiiBindingConstants.CHANNEL_ID_LUMINANCE,
                            new QuantityType<>(environment.luminance, Units.LUX));
                });
                blukiiData.accelerometer.ifPresent(accelerometer -> {
                    updateState(BlukiiBindingConstants.CHANNEL_ID_TILTX,
                            new QuantityType<>(accelerometer.tiltX, Units.DEGREE_ANGLE));
                    updateState(BlukiiBindingConstants.CHANNEL_ID_TILTY,
                            new QuantityType<>(accelerometer.tiltY, Units.DEGREE_ANGLE));
                    updateState(BlukiiBindingConstants.CHANNEL_ID_TILTZ,
                            new QuantityType<>(accelerometer.tiltZ, Units.DEGREE_ANGLE));
                });
                blukiiData.magnetometer.ifPresent(magnetometer -> {
                    // It isn't easy to get a heading from these values without any calibration, so we ignore those
                    // right
                    // now.
                });
            }
        }
        super.onScanRecordReceived(scanNotification);
    }
}
