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
package org.openhab.binding.fineoffsetweatherstation.internal.handler;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

/**
 * The {@link FineOffsetSensorHandler} keeps track of the signal and battery of the sensor attached to the gateway.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class FineOffsetSensorHandler extends BaseThingHandler {
    private boolean disposed;

    public FineOffsetSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        disposed = false;
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    public void updateSensorState(@Nullable SensorDevice sensorDevice) {
        if (disposed) {
            return;
        }
        if (sensorDevice == null) {
            // this only happens, if sensor data was read out correctly from the gateway, but the things' device
            // (sensor) is no longer part of the paired sensors
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
            getThing().getChannels().forEach(c -> updateState(c.getUID(), UnDefType.UNDEF));
            return;
        }
        if (sensorDevice.getSignal() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
        updateState(FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_SIGNAL,
                new DecimalType(sensorDevice.getSignal()));
        BatteryStatus batteryStatus = sensorDevice.getBatteryStatus();

        updateState(FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_LOW_BATTERY,
                batteryStatus.isLow() ? OnOffType.ON : OnOffType.OFF);
        Integer percentage = batteryStatus.getPercentage();
        if (percentage != null) {
            updateState(FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_BATTERY_LEVEL,
                    new DecimalType(new BigDecimal(percentage)));
        } else {
            @Nullable
            Channel channel = thing.getChannel(FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_BATTERY_LEVEL);
            if (channel != null) {
                updateThing(editThing().withoutChannels(channel).build());
            }
        }
        Double voltage = batteryStatus.getVoltage();
        if (voltage != null) {
            updateState(FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_BATTERY_VOLTAGE,
                    new QuantityType<>(voltage, Units.VOLT));
        } else {
            @Nullable
            Channel channel = thing.getChannel(FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_BATTERY_VOLTAGE);
            if (channel != null) {
                updateThing(editThing().withoutChannels(channel).build());
            }
        }
    }
}
