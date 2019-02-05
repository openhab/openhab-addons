/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.blukii.BlukiiBindingConstants;
import org.openhab.binding.bluetooth.blukii.internal.data.BlukiiData;
import org.openhab.binding.bluetooth.blukii.internal.data.BlukiiDataDecoder;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;

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
                            new QuantityType<Temperature>(environment.temperature, SIUnits.CELSIUS));
                    updateState(BlukiiBindingConstants.CHANNEL_ID_HUMIDITY,
                            new QuantityType<Dimensionless>(environment.humidity, SmartHomeUnits.PERCENT));
                    updateState(BlukiiBindingConstants.CHANNEL_ID_PRESSURE,
                            new QuantityType<Pressure>(environment.pressure, MetricPrefix.HECTO(SIUnits.PASCAL)));
                    updateState(BlukiiBindingConstants.CHANNEL_ID_LUMINANCE,
                            new QuantityType<Illuminance>(environment.luminance, SmartHomeUnits.LUX));
                });
                blukiiData.accelerometer.ifPresent(accelerometer -> {
                    updateState(BlukiiBindingConstants.CHANNEL_ID_TILTX,
                            new QuantityType<Angle>(accelerometer.tiltX, SmartHomeUnits.DEGREE_ANGLE));
                    updateState(BlukiiBindingConstants.CHANNEL_ID_TILTY,
                            new QuantityType<Angle>(accelerometer.tiltY, SmartHomeUnits.DEGREE_ANGLE));
                    updateState(BlukiiBindingConstants.CHANNEL_ID_TILTZ,
                            new QuantityType<Angle>(accelerometer.tiltZ, SmartHomeUnits.DEGREE_ANGLE));
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
