/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.grundfosalpha.internal.handler;

import static org.openhab.binding.bluetooth.grundfosalpha.internal.GrundfosAlphaBindingConstants.*;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.core.library.dimension.VolumetricFlowRate;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;

/**
 * The {@link GrundfosAlphaReaderHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
public class GrundfosAlphaReaderHandler extends BeaconBluetoothHandler {

    public GrundfosAlphaReaderHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        super.onScanRecordReceived(scanNotification);
        byte[] data = scanNotification.getManufacturerData();
        if (data.length == 21) {
            int batteryLevel = (data[5] & 0xFF) * 25;
            QuantityType<Dimensionless> quantity = new QuantityType<>(batteryLevel, Units.PERCENT);
            updateState(CHANNEL_BATTERY_LEVEL, quantity);

            float flowRate = ((data[9] & 0xFF) << 8 | (data[8] & 0xFF)) / 6553.5f;
            QuantityType<VolumetricFlowRate> quantity2 = new QuantityType<>(flowRate, Units.CUBICMETRE_PER_HOUR);
            updateState(CHANNEL_FLOW_RATE, quantity2);

            float pumpHead = ((data[11] & 0xFF) << 8 | (data[10] & 0xFF)) / 3276.7f;
            QuantityType<Length> quantity3 = new QuantityType<>(pumpHead, SIUnits.METRE);
            updateState(CHANNEL_PUMP_HEAD, quantity3);

            float pumpTemperature = data[14] & 0xFF;
            QuantityType<Temperature> quantity4 = new QuantityType<>(pumpTemperature, SIUnits.CELSIUS);
            updateState(CHANNEL_PUMP_TEMPERATURE, quantity4);
        }
    }
}
