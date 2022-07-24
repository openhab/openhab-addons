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
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

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
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        if (sensorDevice.getSignal() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
        updateState(FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_SIGNAL,
                new DecimalType(sensorDevice.getSignal()));
        updateState(FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_LOW_BATTERY,
                sensorDevice.getBatteryStatus().isLow() ? OnOffType.ON : OnOffType.OFF);
        Integer percentage = sensorDevice.getBatteryStatus().getPercentage();
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
    }
}
