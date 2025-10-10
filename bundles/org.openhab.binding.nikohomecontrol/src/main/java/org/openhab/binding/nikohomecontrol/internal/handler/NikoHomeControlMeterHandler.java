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
package org.openhab.binding.nikohomecontrol.internal.handler;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;
import static org.openhab.core.library.unit.Units.KILOWATT_HOUR;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcMeter;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcMeterEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.MeterType;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc1.NhcMeter1;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcMeter2;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlMeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Herwege - Initial Contribution
 * @author Mark Herwege - Add home digital meter power readings
 */
@NonNullByDefault
public class NikoHomeControlMeterHandler extends NikoHomeControlBaseHandler implements NhcMeterEvent {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlMeterHandler.class);

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private volatile @Nullable NhcMeter nhcMeter;

    private final Map<String, Boolean> powerChannelLinked = new ConcurrentHashMap<>();

    public NikoHomeControlMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    void handleCommandSelection(ChannelUID channelUID, Command command) {
        NhcMeter nhcMeter = this.nhcMeter;
        if (nhcMeter == null) {
            logger.debug("meter with ID {} not initialized", deviceId);
            return;
        }

        if (REFRESH.equals(command)) {
            switch (channelUID.getId()) {
                case CHANNEL_POWER:
                    meterPowerEvent(nhcMeter.getPower());
                    break;
                case CHANNEL_ENERGY:
                case CHANNEL_GAS:
                case CHANNEL_WATER:
                case CHANNEL_ENERGY_DAY:
                case CHANNEL_GAS_DAY:
                case CHANNEL_WATER_DAY:
                case CHANNEL_MEASUREMENT_TIME:
                    LocalDateTime lastReadingUTC = nhcMeter.getLastReading();
                    if (lastReadingUTC != null) {
                        meterReadingEvent(nhcMeter.getReading(), nhcMeter.getDayReading(), lastReadingUTC);
                    }
                    break;
                default:
                    logger.debug("unexpected command for channel {}", channelUID.getId());
            }
        }
    }

    @Override
    public void initialize() {
        initialized = false;

        NikoHomeControlMeterConfig config = getConfig().as(NikoHomeControlMeterConfig.class);
        deviceId = config.meterId;

        NikoHomeControlBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.invalid-bridge-handler");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        Bridge bridge = getBridge();
        if ((bridge != null) && ThingStatus.ONLINE.equals(bridge.getStatus())) {
            // We need to do this in a separate thread because we may have to wait for the
            // communication to become active
            commStartThread = scheduler.submit(this::startCommunication);
        }
    }

    @Override
    synchronized void startCommunication() {
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());

        if (nhcComm == null) {
            return;
        }

        if (!nhcComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error");
            return;
        }

        NhcMeter nhcMeter = nhcComm.getMeters().get(deviceId);
        if (nhcMeter == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.deviceId");
            return;
        }

        MeterType meterType = nhcMeter.getType();
        if (!(MeterType.ENERGY_LIVE.equals(meterType) || MeterType.ENERGY.equals(meterType)
                || MeterType.GAS.equals(meterType) || MeterType.WATER.equals(meterType))) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.meterType");
            return;
        }

        String startDate = getConfig().as(NikoHomeControlMeterConfig.class).startDate;
        if (startDate.isEmpty()) {
            LocalDateTime referenceDate = nhcMeter.getReferenceDate();
            if (referenceDate != null) {
                startDate = referenceDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                startDate = Instant.now().atZone(nhcComm.getTimeZone()).truncatedTo(ChronoUnit.DAYS)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            Configuration config = editConfiguration();
            config.put(CONFIG_METER_START_DATE, startDate);
            updateConfiguration(config);
        }
        try {
            LocalDateTime.parse(startDate);
        } catch (DateTimeParseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.meterStartDate");
            return;
        }

        nhcMeter.setEventHandler(this);

        updateProperties(nhcMeter);

        String location = nhcMeter.getLocation();
        if (thing.getLocation() == null) {
            thing.setLocation(location);
        }

        this.nhcMeter = nhcMeter;

        initialized = true;
        deviceInitialized();
    }

    @Override
    void refresh() {
        NhcMeter meter = nhcMeter;
        if (meter != null) {
            Double peakPower = meter.getPeakPowerFromGrid();
            if (peakPower != null) {
                meterPeakPowerFromGridEvent(peakPower);
            }
        }
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());
        if (nhcComm != null) {
            NikoHomeControlMeterConfig config = getConfig().as(NikoHomeControlMeterConfig.class);
            nhcComm.startMeter(deviceId, config.refresh, config.startDate);
            // Subscribing to power readings starts an intensive data flow, therefore only do it when there is an item
            // linked to the channel
            if (isLinked(CHANNEL_POWER) || isLinked(CHANNEL_POWER_TO_GRID) || isLinked(CHANNEL_POWER_FROM_GRID)) {
                nhcComm.startMeterLive(deviceId);
            }
        }
    }

    @Override
    public void dispose() {
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());
        if (nhcComm != null) {
            nhcComm.stopMeterLive(deviceId);
            nhcComm.stopMeter(deviceId);
            NhcMeter meter = nhcComm.getMeters().get(deviceId);
            if (meter != null) {
                meter.unsetEventHandler();
            }
        }
        nhcMeter = null;
        super.dispose();
    }

    private void updateProperties(NhcMeter nhcMeter) {
        Map<String, String> properties = new HashMap<>();

        if (nhcMeter instanceof NhcMeter1 meter) {
            properties.put("type", meter.getMeterType());
            LocalDateTime referenceDate = meter.getReferenceDate();
            if (referenceDate != null) {
                properties.put("startdateUTC", referenceDate.format(DATE_TIME_FORMAT));
            }
        } else if (nhcMeter instanceof NhcMeter2 meter) {
            properties.put(PROPERTY_DEVICE_TYPE, meter.getDeviceType());
            properties.put(PROPERTY_DEVICE_TECHNOLOGY, meter.getDeviceTechnology());
            properties.put(PROPERTY_DEVICE_MODEL, meter.getDeviceModel());
        }

        thing.setProperties(properties);
    }

    @Override
    public void meterPowerEvent(@Nullable Double power) {
        meterPowerEvent(power, null, null);
    }

    @Override
    public void meterPowerEvent(@Nullable Double power, @Nullable Double powerFromGrid, @Nullable Double powerToGrid) {
        NhcMeter nhcMeter = this.nhcMeter;
        if (nhcMeter == null) {
            logger.debug("meter with ID {} not initialized", deviceId);
            return;
        }

        MeterType meterType = nhcMeter.getType();
        if (meterType != MeterType.ENERGY_LIVE) {
            logger.debug("meter with ID {} does not support live readings", deviceId);
            return;
        }

        if (power == null) {
            updateState(CHANNEL_POWER, UnDefType.UNDEF);
        } else {
            boolean invert = getConfig().as(NikoHomeControlMeterConfig.class).invert;
            double value = (invert ? -1 : 1) * power;
            updateState(CHANNEL_POWER, new QuantityType<>(value, Units.WATT));
        }
        if (powerFromGrid == null) {
            updateState(CHANNEL_POWER_FROM_GRID, UnDefType.UNDEF);
        } else {
            updateState(CHANNEL_POWER_FROM_GRID, new QuantityType<>(powerFromGrid, Units.WATT));
        }
        if (powerToGrid == null) {
            updateState(CHANNEL_POWER_TO_GRID, UnDefType.UNDEF);
        } else {
            updateState(CHANNEL_POWER_TO_GRID, new QuantityType<>(powerToGrid, Units.WATT));
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void meterPeakPowerFromGridEvent(double peakPowerFromGrid) {
        NhcMeter nhcMeter = this.nhcMeter;
        if (nhcMeter == null) {
            logger.debug("meter with ID {} not initialized", deviceId);
            return;
        }

        updateState(CHANNEL_PEAK_POWER_FROM_GRID, new QuantityType<>(peakPowerFromGrid, Units.WATT));
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void meterReadingEvent(double meterReading, double meterReadingDay, LocalDateTime lastReadingUTC) {
        NhcMeter nhcMeter = this.nhcMeter;
        if (nhcMeter == null) {
            logger.debug("meter with ID {} not initialized", deviceId);
            return;
        }

        NikoHomeControlBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("Cannot update meter channels, no bridge handler");
            return;
        }
        ZonedDateTime lastReading = lastReadingUTC.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(bridgeHandler.getTimeZone());

        boolean invert = getConfig().as(NikoHomeControlMeterConfig.class).invert;
        double value = (invert ? -1 : 1) * meterReading;
        double dayValue = (invert ? -1 : 1) * meterReadingDay;

        MeterType meterType = nhcMeter.getType();
        switch (meterType) {
            case ENERGY_LIVE:
            case ENERGY:
                updateState(CHANNEL_ENERGY, new QuantityType<>(value, KILOWATT_HOUR));
                updateState(CHANNEL_ENERGY_DAY, new QuantityType<>(dayValue, KILOWATT_HOUR));
                updateState(CHANNEL_MEASUREMENT_TIME, new DateTimeType(lastReading));
                updateStatus(ThingStatus.ONLINE);
                break;
            case GAS:
                updateState(CHANNEL_GAS, new QuantityType<>(value, SIUnits.CUBIC_METRE));
                updateState(CHANNEL_GAS_DAY, new QuantityType<>(dayValue, SIUnits.CUBIC_METRE));
                updateState(CHANNEL_MEASUREMENT_TIME, new DateTimeType(lastReading));
                updateStatus(ThingStatus.ONLINE);
                break;
            case WATER:
                updateState(CHANNEL_WATER, new QuantityType<>(value, SIUnits.CUBIC_METRE));
                updateState(CHANNEL_WATER_DAY, new QuantityType<>(dayValue, SIUnits.CUBIC_METRE));
                updateState(CHANNEL_MEASUREMENT_TIME, new DateTimeType(lastReading));
                updateStatus(ThingStatus.ONLINE);
                break;
            default:
                break;
        }
    }

    @Override
    public void meterReadingEvent(Map<String, Double> meterReadings, Map<String, Double> meterReadingsDay,
            LocalDateTime lastReadingUTC) {
        NhcMeter nhcMeter = this.nhcMeter;
        if (nhcMeter == null) {
            logger.debug("meter with ID {} not initialized", deviceId);
            return;
        }

        NikoHomeControlBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("Cannot update meter channels, no bridge handler");
            return;
        }

        ZonedDateTime lastReading = lastReadingUTC.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(bridgeHandler.getTimeZone());

        boolean invert = getConfig().as(NikoHomeControlMeterConfig.class).invert;
        meterReadings.forEach((reading, v) -> {
            double value = (invert ? -1 : 1) * v;
            Double dayValue = meterReadingsDay.get(reading);
            if (dayValue != null) {
                dayValue = (invert ? -1 : 1) * dayValue;
            }
            switch (reading) {
                case NikoHomeControlConstants.NHC_ELECTRICAL_ENERGY:
                case NikoHomeControlConstants.NHC_ELECTRICAL_ENERGY_CONSUMPTION:
                    updateState(CHANNEL_ENERGY, new QuantityType<>(value, KILOWATT_HOUR));
                    if (dayValue != null) {
                        updateState(CHANNEL_ENERGY_DAY, new QuantityType<>(value, KILOWATT_HOUR));
                    }
                    break;
                case NikoHomeControlConstants.NHC_ELECTRICAL_ENERGY_FROM_GRID:
                    updateState(CHANNEL_ENERGY_FROM_GRID, new QuantityType<>(value, KILOWATT_HOUR));
                    if (dayValue != null) {
                        updateState(CHANNEL_ENERGY_FROM_GRID_DAY, new QuantityType<>(value, KILOWATT_HOUR));
                    }
                    break;
                case NikoHomeControlConstants.NHC_ELECTRICAL_ENERGY_TO_GRID:
                    updateState(CHANNEL_ENERGY_TO_GRID, new QuantityType<>(value, KILOWATT_HOUR));
                    if (dayValue != null) {
                        updateState(CHANNEL_ENERGY_TO_GRID_DAY, new QuantityType<>(value, KILOWATT_HOUR));
                    }
                    break;
                case NikoHomeControlConstants.NHC_ELECTRICAL_ENERGY_SELF_CONSUMPTION:
                    updateState(CHANNEL_ENERGY_SELF_CONSUMPTION, new QuantityType<>(value, KILOWATT_HOUR));
                    if (dayValue != null) {
                        updateState(CHANNEL_ENERGY_SELF_CONSUMPTION_DAY, new QuantityType<>(value, KILOWATT_HOUR));
                    }
                    break;
                case NikoHomeControlConstants.NHC_GAS_VOLUME:
                    updateState(CHANNEL_GAS, new QuantityType<>(value, SIUnits.CUBIC_METRE));
                    if (dayValue != null) {
                        updateState(CHANNEL_GAS_DAY, new QuantityType<>(value, SIUnits.CUBIC_METRE));
                    }
                    break;
                case NikoHomeControlConstants.NHC_WATER_VOLUME:
                    updateState(CHANNEL_WATER, new QuantityType<>(value, SIUnits.CUBIC_METRE));
                    if (dayValue != null) {
                        updateState(CHANNEL_WATER_DAY, new QuantityType<>(value, SIUnits.CUBIC_METRE));
                    }
                    break;
                default:
                    break;
            }
        });

        if (!meterReadings.isEmpty()) {
            updateState(CHANNEL_MEASUREMENT_TIME, new DateTimeType(lastReading));
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // Subscribing to power readings starts an intensive data flow, therefore only do it when there is an item
        // linked to the channel
        String channelId = channelUID.getId();
        if (!(CHANNEL_POWER.equals(channelId) || CHANNEL_POWER_FROM_GRID.equals(channelId)
                || CHANNEL_POWER_TO_GRID.equals(channelId))) {
            return;
        }
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());
        if (nhcComm != null && !powerChannelLinked.values().stream().anyMatch(Boolean::booleanValue)) {
            // This can be expensive, therefore do it in a job.
            scheduler.submit(() -> {
                if (!nhcComm.communicationActive()) {
                    restartCommunication(nhcComm);
                }

                if (nhcComm.communicationActive()) {
                    nhcComm.startMeterLive(deviceId);
                }
            });
        }
        powerChannelLinked.put(channelId, true);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        String channelId = channelUID.getId();
        if (!(CHANNEL_POWER.equals(channelId) || CHANNEL_POWER_FROM_GRID.equals(channelId)
                || CHANNEL_POWER_TO_GRID.equals(channelId))) {
            return;
        }
        powerChannelLinked.put(channelId, false);
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());
        if (nhcComm != null && !powerChannelLinked.values().stream().anyMatch(Boolean::booleanValue)) {
            // This can be expensive, therefore do it in a job.
            scheduler.submit(() -> {
                if (!nhcComm.communicationActive()) {
                    restartCommunication(nhcComm);
                }

                if (nhcComm.communicationActive()) {
                    nhcComm.stopMeterLive(deviceId);
                    // as this is momentary power production/consumption, we set it UNDEF as we do not get readings
                    // anymore
                    updateState(CHANNEL_POWER, UnDefType.UNDEF);
                    updateState(CHANNEL_POWER_FROM_GRID, UnDefType.UNDEF);
                    updateState(CHANNEL_POWER_TO_GRID, UnDefType.UNDEF);
                }
            });
        }
    }
}
