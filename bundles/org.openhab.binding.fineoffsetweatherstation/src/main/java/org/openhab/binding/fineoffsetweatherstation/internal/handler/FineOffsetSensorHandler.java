/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
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
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FineOffsetSensorHandler} keeps track of the signal and battery of the sensor attached to the gateway,
 * and mirrors the sensor's measured values onto dynamically created channels.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class FineOffsetSensorHandler extends BaseThingHandler {

    /** Static channels declared by the sensor thing-type; protected so the reconciler never removes them. */
    private static final Set<String> STATIC_CHANNEL_IDS = Set.of(
            FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_SIGNAL,
            FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_LOW_BATTERY,
            FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_BATTERY_LEVEL,
            FineOffsetWeatherStationBindingConstants.SENSOR_CHANNEL_BATTERY_VOLTAGE);

    private final Logger logger = LoggerFactory.getLogger(FineOffsetSensorHandler.class);
    private final ChannelTypeRegistry channelTypeRegistry;
    private final DynamicChannelReconciler reconciler = new DynamicChannelReconciler(STATIC_CHANNEL_IDS);

    private boolean disposed;

    public FineOffsetSensorHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        reconciler.reset();
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
                OnOffType.from(batteryStatus.isLow()));
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

    /**
     * Mirrors the sensor's measured values onto dynamic channels: creates channels that do not exist yet, updates the
     * state of existing ones, and removes a managed channel once its value has been absent for
     * {@link DynamicChannelReconciler#MISSING_VALUE_REMOVAL_THRESHOLD} consecutive dispatches. Static signal/battery
     * channels are never affected. Called every poll by the gateway, with an empty collection when this sensor
     * reported nothing.
     */
    public void updateMeasuredValues(Collection<MeasuredValue> values) {
        if (disposed) {
            return;
        }
        DynamicChannelReconciler.Plan plan = reconciler.reconcile(values, thing.getChannels(),
                MeasuredValue::getChannelPrefix, this::createChannel);
        if (plan.hasChannelChanges()) {
            List<Channel> channels = new ArrayList<>(thing.getChannels());
            channels.addAll(plan.channelsToAdd);
            channels.removeAll(plan.channelsToRemove);
            updateThing(editThing().withChannels(channels).build());
        }
        plan.statesToPost.forEach(this::updateState);
    }

    /**
     * Sets the mirrored measurement channels to {@link UnDefType#UNDEF} when the gateway poll fails, so they do not
     * keep stale state. The static signal/battery channels are left untouched (they are polled separately), and the
     * reconciler is deliberately not run, so a poll failure does not advance the missing-value removal debounce.
     */
    public void markMeasuredValuesUndefined() {
        if (disposed) {
            return;
        }
        for (Channel channel : thing.getChannels()) {
            if (!STATIC_CHANNEL_IDS.contains(channel.getUID().getId())) {
                updateState(channel.getUID(), UnDefType.UNDEF);
            }
        }
    }

    private @Nullable Channel createChannel(MeasuredValue value) {
        ChannelTypeUID channelTypeId = value.getChannelTypeUID();
        if (channelTypeId == null) {
            logger.debug("cannot create channel for {}", value.getDebugName());
            return null;
        }
        // No explicit label: let the (translatable) ChannelType label apply. On a per-sensor Thing the value needs no
        // channel-number disambiguation, unlike the aggregated gateway channels.
        ChannelBuilder builder = ChannelBuilder.create(new ChannelUID(thing.getUID(), value.getChannelPrefix()))
                .withKind(ChannelKind.STATE).withType(channelTypeId);
        @Nullable
        ChannelType type = channelTypeRegistry.getChannelType(channelTypeId);
        if (type != null) {
            builder.withAcceptedItemType(type.getItemType());
        }
        return builder.build();
    }
}
