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
import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.*;
import static org.openhab.core.library.unit.Units.KILOWATT_HOUR;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcCarCharger;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcCarChargerEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcCarCharger2;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlCarChargerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlCarChargerHandler extends NikoHomeControlBaseHandler implements NhcCarChargerEvent {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlCarChargerHandler.class);

    private volatile @Nullable NhcCarCharger nhcCarCharger;

    public NikoHomeControlCarChargerHandler(Thing thing) {
        super(thing);
    }

    @Override
    void handleCommandSelection(ChannelUID channelUID, Command command) {
        NhcCarCharger nhcCarCharger = this.nhcCarCharger;
        if (nhcCarCharger == null) {
            logger.debug("car charger device with ID {} not initialized", deviceId);
            return;
        }

        logger.debug("handle command {} for {}", command, channelUID);

        if (REFRESH.equals(command)) {
            switch (channelUID.getId()) {
                case CHANNEL_STATUS:
                case CHANNEL_CHARGING_STATUS:
                case CHANNEL_EV_STATUS:
                case CHANNEL_COUPLING_STATUS:
                case CHANNEL_POWER:
                    chargingStatusEvent(nhcCarCharger.getStatus(), nhcCarCharger.getChargingStatus(),
                            nhcCarCharger.getEvStatus(), nhcCarCharger.getCouplingStatus(),
                            nhcCarCharger.getElectricalPower());
                    break;
                case CHANNEL_CHARGING_MODE:
                case CHANNEL_TARGET_DISTANCE:
                case CHANNEL_TARGET_TIME:
                case CHANNEL_BOOST:
                case CHANNEL_REACHABLE_DISTANCE:
                case CHANNEL_NEXT_CHARGING_TIME:
                    chargingModeEvent(nhcCarCharger.getChargingMode(), nhcCarCharger.getTargetDistance(),
                            nhcCarCharger.getTargetTime(), nhcCarCharger.isBoost(),
                            nhcCarCharger.getReachableDistance(), nhcCarCharger.getNextChargingTime());
                    break;
                case CHANNEL_ENERGY:
                case CHANNEL_ENERGY_DAY:
                case CHANNEL_MEASUREMENT_TIME:
                    LocalDateTime lastReadingUTC = nhcCarCharger.getLastReading();
                    if (lastReadingUTC != null) {
                        meterReadingEvent(nhcCarCharger.getReading(), nhcCarCharger.getDayReading(), lastReadingUTC);
                    }
                    break;
                default:
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_STATUS:
                    if (command instanceof OnOffType onOffCommand) {
                        nhcCarCharger.executeCarChargerStatus(OnOffType.ON.equals(onOffCommand));
                    }
                    break;
                case CHANNEL_CHARGING_MODE:
                    if (command instanceof StringType stringTypeCommand) {
                        String stringCommand = stringTypeCommand.toString();
                        String chargingMode = CHARGINGMODES.entrySet().stream()
                                .filter(e -> stringCommand.equals(e.getValue())).map(e -> e.getKey()).findFirst()
                                .orElse(null);
                        if (chargingMode != null) {
                            nhcCarCharger.executeCarChargerChargingMode(chargingMode, nhcCarCharger.getTargetDistance(),
                                    nhcCarCharger.getTargetTime());
                        }
                    }
                    break;
                case CHANNEL_TARGET_DISTANCE:
                    if (command instanceof QuantityType<?> quantityCommand) {
                        QuantityType<?> distance = quantityCommand.toUnit(MetricPrefix.KILO(SIUnits.METRE));
                        if (distance != null) {
                            nhcCarCharger.executeCarChargerChargingMode(nhcCarCharger.getChargingMode(),
                                    Math.round(distance.floatValue()), nhcCarCharger.getTargetTime());
                        }
                    } else if (command instanceof DecimalType decimalCommand) {
                        BigDecimal distance = decimalCommand.toBigDecimal();
                        nhcCarCharger.executeCarChargerChargingMode(nhcCarCharger.getChargingMode(),
                                Math.round(distance.floatValue()), nhcCarCharger.getTargetTime());
                    }
                    break;
                case CHANNEL_TARGET_TIME:
                    if (command instanceof DateTimeType dateTimeCommand) {
                        String targetTime = dateTimeCommand.format("%tR");
                        nhcCarCharger.executeCarChargerChargingMode(nhcCarCharger.getChargingMode(),
                                nhcCarCharger.getTargetDistance(), targetTime);
                    }
                    break;
                case CHANNEL_BOOST:
                    if (command instanceof OnOffType onOffCommand) {
                        nhcCarCharger.executeCarChargerChargingBoost(OnOffType.ON.equals(onOffCommand));
                    }
                    break;
                default:
                    logger.debug("unexpected command for channel {}", channelUID.getId());
                    break;
            }
        }
    }

    @Override
    public void initialize() {
        initialized = false;

        NikoHomeControlCarChargerConfig config = getConfig().as(NikoHomeControlCarChargerConfig.class);
        deviceId = config.carChargerId;

        NikoHomeControlBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.invalid-bridge-handler");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        Bridge bridge = getBridge();
        if (bridge != null && ThingStatus.ONLINE.equals(bridge.getStatus())) {
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

        NhcCarCharger nhcCarCharger = nhcComm.getCarChargerDevices().get(deviceId);
        if (nhcCarCharger == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.deviceId");
            return;
        }

        String startDate = getConfig().as(NikoHomeControlMeterConfig.class).startDate;
        if (startDate.isEmpty()) {
            startDate = Instant.now().atZone(nhcComm.getTimeZone()).truncatedTo(ChronoUnit.DAYS)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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

        nhcCarCharger.setEventHandler(this);

        updateProperties(nhcCarCharger);

        String location = nhcCarCharger.getLocation();
        if (thing.getLocation() == null) {
            thing.setLocation(location);
        }

        this.nhcCarCharger = nhcCarCharger;

        initialized = true;
        deviceInitialized();
    }

    @Override
    void refresh() {
        NhcCarCharger carCharger = nhcCarCharger;
        if (carCharger != null) {
            chargingModeEvent(carCharger.getChargingMode(), carCharger.getTargetDistance(), carCharger.getTargetTime(),
                    carCharger.isBoost(), carCharger.getReachableDistance(), carCharger.getNextChargingTime());
            chargingStatusEvent(carCharger.getStatus(), carCharger.getChargingStatus(), carCharger.getEvStatus(),
                    carCharger.getCouplingStatus(), carCharger.getElectricalPower());
        }
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());
        if (nhcComm != null) {
            NikoHomeControlMeterConfig config = getConfig().as(NikoHomeControlMeterConfig.class);
            nhcComm.startMeter(deviceId, config.refresh, config.startDate);
        }
    }

    @Override
    public void dispose() {
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());
        if (nhcComm != null) {
            nhcComm.stopMeter(deviceId);
            NhcCarCharger carCharger = nhcComm.getCarChargerDevices().get(deviceId);
            if (carCharger != null) {
                carCharger.unsetEventHandler();
            }
        }
        nhcCarCharger = null;
        super.dispose();
    }

    private void updateProperties(NhcCarCharger nhcCarCharger) {
        Map<String, String> properties = new HashMap<>();

        if (nhcCarCharger instanceof NhcCarCharger2 access) {
            properties.put(PROPERTY_DEVICE_TYPE, access.getDeviceType());
            properties.put(PROPERTY_DEVICE_TECHNOLOGY, access.getDeviceTechnology());
            properties.put(PROPERTY_DEVICE_MODEL, access.getDeviceModel());
        }

        thing.setProperties(properties);
    }

    @Override
    public void chargingStatusEvent(boolean status, @Nullable String chargingStatus, @Nullable String evStatus,
            @Nullable String couplingStatus, @Nullable Integer electricalPower) {
        NhcCarCharger nhcCarCharger = this.nhcCarCharger;
        if (nhcCarCharger == null) {
            logger.debug("car charger device with ID {} not initialized", deviceId);
            return;
        }

        updateState(CHANNEL_STATUS, OnOffType.from(status));
        if (chargingStatus != null) {
            updateState(CHANNEL_CHARGING_STATUS, StringType.valueOf(CHARGINGSTATES.get(chargingStatus)));
        }
        if (evStatus != null) {
            updateState(CHANNEL_EV_STATUS, StringType.valueOf(EVSTATES.get(evStatus)));
        }
        if (couplingStatus != null) {
            updateState(CHANNEL_COUPLING_STATUS, StringType.valueOf(COUPLINGSTATES.get(couplingStatus)));
        }
        if (electricalPower != null) {
            updateState(CHANNEL_POWER, new QuantityType<>(electricalPower, Units.WATT));
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void chargingModeEvent(@Nullable String chargingMode, float targetDistance, @Nullable String targetTime,
            boolean boost, float reachableDistance, @Nullable String nextChargingTime) {
        NhcCarCharger nhcCarCharger = this.nhcCarCharger;
        if (nhcCarCharger == null) {
            logger.debug("car charger device with ID {} not initialized", deviceId);
            return;
        }

        if (chargingMode != null) {
            updateState(CHANNEL_CHARGING_MODE, StringType.valueOf(CHARGINGMODES.get(chargingMode)));
            updateState(CHANNEL_TARGET_DISTANCE, new QuantityType<>(targetDistance, MetricPrefix.KILO(SIUnits.METRE)));
            updateState(CHANNEL_REACHABLE_DISTANCE,
                    new QuantityType<>(reachableDistance, MetricPrefix.KILO(SIUnits.METRE)));
        }
        updateState(CHANNEL_BOOST, OnOffType.from(boost));
        if (targetTime != null) {
            updateState(CHANNEL_TARGET_TIME, DateTimeType.valueOf(targetTime));
        }
        if (nextChargingTime != null) {
            updateState(CHANNEL_NEXT_CHARGING_TIME, DateTimeType.valueOf(nextChargingTime));
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void meterReadingEvent(double reading, double dayReading, LocalDateTime lastReadingUTC) {
        NhcCarCharger nhcCarCharger = this.nhcCarCharger;
        if (nhcCarCharger == null) {
            logger.debug("car charger device with ID {} not initialized", deviceId);
            return;
        }

        NikoHomeControlBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("Cannot update car charger channels, no bridge handler");
            return;
        }

        ZonedDateTime lastReading = lastReadingUTC.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(bridgeHandler.getTimeZone());

        boolean invert = getConfig().as(NikoHomeControlCarChargerConfig.class).invert;
        double value = (invert ? -1 : 1) * reading;
        double dayValue = (invert ? -1 : 1) * dayReading;
        updateState(CHANNEL_ENERGY, new QuantityType<>(value, KILOWATT_HOUR));
        updateState(CHANNEL_ENERGY_DAY, new QuantityType<>(dayValue, KILOWATT_HOUR));
        updateState(CHANNEL_MEASUREMENT_TIME, new DateTimeType(lastReading));
        updateStatus(ThingStatus.ONLINE);
    }
}
