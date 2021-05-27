/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api.services;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.getString;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_SERVICE_VEHICLE_STATUS_REPORT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import javax.measure.IncommensurableException;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleStatus;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleStatus.CNStoredVehicleDataResponse.CNVehicleData.CNStatusData;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetVehicleStatus.CNStoredVehicleDataResponse.CNVehicleData.CNStatusData.CNStatusField;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.handler.CarNetVehicleHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetServiceStatus} implements fetching the basic vehicle status data.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetServiceStatus extends CarNetBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceStatus.class);

    public CarNetServiceStatus(CarNetVehicleHandler thingHandler, CarNetApiBase api) {
        super(CNAPI_SERVICE_VEHICLE_STATUS_REPORT, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws CarNetException {
        boolean updated = false;

        // Try to query status information from vehicle
        CarNetVehicleStatus status = api.getVehicleStatus();
        for (CNStatusData data : status.storedVehicleDataResponse.vehicleData.data) {
            for (CNStatusField field : data.fields) {
                try {
                    ChannelIdMapEntry definition = idMapper.find(field.id);
                    if (definition != null) {
                        if (definition.channelName.isEmpty()) {
                            logger.debug("{}: {}={}{}, no channel defined -> ignore", thingId, definition.symbolicName,
                                    getString(field.value), getString(field.unit));
                            continue;
                        }

                        logger.info("{}: {}={}{} (channel {}#{})", thingId, definition.symbolicName,
                                getString(field.value), getString(field.unit), definition.groupName,
                                definition.channelName);
                        if ((definition.symbolicName.startsWith("STATE2_")
                                || definition.symbolicName.startsWith("STATE3_")
                                || definition.symbolicName.startsWith("LOCK_")) && field.value.equals("0")) {
                            // Data is reported, but equippment is not available, e.g. convertable top
                            logger.debug("{}: Data point not available, removing channel {}", thingId,
                                    definition.channelName);
                            definition.disabled = true;
                            continue;
                        }
                        if (!definition.channelName.startsWith(CHANNEL_GROUP_TIRES) || !field.value.contains("1")) {
                            if (!channels.containsKey(definition.id)) {
                                channels.put(definition.id, definition);
                                updated = true;
                            }
                        }
                    } else {
                        logger.debug("{}: Unknown data field {}.{}, value={}{}", thingId, data.id, field.id,
                                field.value, getString(field.unit));
                    }
                } catch (RuntimeException e) {

                }
            }
        }

        addChannel(channels, CHANNEL_GROUP_GENERAL, CHANNEL_GENERAL_ACTION, ITEMT_STRING, null, false, true);
        addChannel(channels, CHANNEL_GROUP_GENERAL, CHANNEL_GENERAL_ACTION_STATUS, ITEMT_STRING, null, false, true);
        addChannel(channels, CHANNEL_GROUP_GENERAL, CHANNEL_GENERAL_ACTION_PENDING, ITEMT_SWITCH, null, false, true);

        return updated;
    }

    @Override
    public boolean serviceUpdate() throws CarNetException {
        // Try to query status information from vehicle
        logger.debug("{}: Get Vehicle Status", thingId);
        boolean maintenanceRequired = false; // true if any maintenance is required
        boolean vehicleLocked = true; // aggregates all lock states
        boolean windowsClosed = true; // true if all Windows are closed
        boolean tiresOk = true; // tire if all tire pressures are ok
        boolean updated = false;

        CarNetVehicleStatus status = api.getVehicleStatus();
        logger.debug("{}: Vehicle Status:\n{}", thingId, status);
        for (CNStatusData data : status.storedVehicleDataResponse.vehicleData.data) {
            for (CNStatusField field : data.fields) {
                ChannelIdMapEntry definition = idMapper.find(field.id);
                if (definition != null) {
                    logger.debug("{}: {}={}{} (channel {}#{})", thingId, definition.symbolicName,
                            getString(field.value), getString(field.unit), getString(definition.groupName),
                            getString(definition.channelName));
                    if (!definition.channelName.isEmpty()) {
                        Channel channel = thingHandler.getThing()
                                .getChannel(definition.groupName + "#" + definition.channelName);
                        if (channel != null) {
                            logger.debug("Updading channel {} with value {}", channel.getUID(), getString(field.value));
                            switch (definition.itemType) {
                                case ITEMT_SWITCH:
                                case ITEMT_CONTACT:
                                    updated |= updateSwitchChannel(channel, definition, field);
                                    break;
                                case ITEMT_STRING:
                                    updated |= thingHandler.updateChannel(channel,
                                            new StringType(getString(field.value)));
                                    break;
                                case ITEMT_NUMBER:
                                case ITEMT_PERCENT:
                                default:
                                    updated |= updateNumberChannel(channel, definition, field);
                                    break;
                            }
                        } else {
                            logger.debug("Channel {}#{} not found", definition.groupName, definition.channelName);
                        }

                        if ((field.value != null) && !field.value.isEmpty()) {
                            vehicleLocked &= checkLocked(field, definition);
                            maintenanceRequired |= checkMaintenance(field, definition);
                            tiresOk &= checkTires(field, definition);
                            windowsClosed &= checkWindows(field, definition);
                        }
                    }
                } else {
                    logger.debug("{}: Unknown data field  {}.{}, value={} {}", thingId, data.id, field.id, field.value,
                            field.unit);
                }
            }
        }

        // Update aggregated status
        updated |= thingHandler.updateChannel(CHANNEL_GROUP_STATUS, CHANNEL_GENERAL_LOCKED,
                vehicleLocked ? OnOffType.ON : OnOffType.OFF);
        updated |= thingHandler.updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_LOCK,
                vehicleLocked ? OnOffType.ON : OnOffType.OFF);
        updated |= thingHandler.updateChannel(CHANNEL_GROUP_STATUS, CHANNEL_GENERAL_MAINTREQ,
                maintenanceRequired ? OnOffType.ON : OnOffType.OFF);
        updated |= thingHandler.updateChannel(CHANNEL_GROUP_STATUS, CHANNEL_GENERAL_TIRESOK,
                tiresOk ? OnOffType.ON : OnOffType.OFF);
        updated |= thingHandler.updateChannel(CHANNEL_GROUP_STATUS, CHANNEL_GENERAL_WINCLOSED,
                windowsClosed ? OnOffType.ON : OnOffType.OFF);

        return updated;
    }

    private boolean checkMaintenance(CNStatusField field, ChannelIdMapEntry definition) {
        if (definition.symbolicName.contains("MAINT_ALARM") && "1".equals(field.value)) {
            // MAINT_ALARM_INSPECTION+MAINT_ALARM_OIL_CHANGE + MAINT_ALARM_OIL_MINIMUM
            logger.debug("{}: Maintenance required: {} has incorrect pressure", thingId, definition.symbolicName);
            return true;
        }
        if (definition.symbolicName.contains("AD_BLUE_RANGE") && (Integer.parseInt(field.value) < 1000)) {
            logger.debug("{}: Maintenance required: Ad Blue at {} (< 1.000km)", thingId, field.value);
            return true;
        }
        return false;
    }

    private boolean checkLocked(CNStatusField field, ChannelIdMapEntry definition) {
        if (definition.symbolicName.contains("LOCK")) {
            boolean result = (definition.symbolicName.contains("LOCK2") && "2".equals(field.value))
                    || (definition.symbolicName.contains("LOCK3") && "3".equals(field.value));
            if (!result) {
                logger.debug("{}: Vehicle is not completetly locked: {}={}", thingId, definition.channelName,
                        field.value);
                return false;
            }
        }
        return true;
    }

    private boolean checkWindows(CNStatusField field, ChannelIdMapEntry definition) {
        if ((definition.symbolicName.contains("WINDOWS") || definition.symbolicName.contains("SUN_ROOF_MOTOR_COVER"))
                && definition.symbolicName.contains("STATE3") && !"3".equals(field.value)) {
            logger.debug("{}: Window {} is not closed ({})", thingId, definition.channelName, field.value);
        }
        return true;
    }

    private boolean checkTires(CNStatusField field, ChannelIdMapEntry definition) {
        if (definition.symbolicName.contains("TIREPRESS") && definition.symbolicName.contains("CURRENT")
                && !"1".equals(field.value)) {
            logger.debug("{}: Tire pressure for {} is not ok", thingId, definition.channelName);
        }
        return true;
    }

    private boolean updateNumberChannel(Channel channel, ChannelIdMapEntry definition, CNStatusField field) {
        State state = UnDefType.UNDEF;
        String val = getString(field.value);
        if (!val.isEmpty()) {
            double value = Double.parseDouble(val);
            if (value < 0) {
                value = value * -1.0; // no egative values
            }
            BigDecimal bd = new BigDecimal(value);
            if (definition.unit != null) {
                ChannelIdMapEntry fromDef = idMapper.updateDefinition(field, definition);
                Unit<?> fromUnit = fromDef.fromUnit;
                Unit<?> toUnit = definition.unit;
                if ((fromUnit != null) && (toUnit != null) && !fromUnit.equals(toUnit)) {
                    try {
                        // Convert between units
                        bd = new BigDecimal(fromUnit.getConverterToAny(toUnit).convert(value));
                    } catch (UnconvertibleException | IncommensurableException e) {
                        logger.debug("{}: Unable to covert value", thingId, e);
                    }
                }
            }
            value = bd.setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            Unit<?> unit = definition.unit;
            if (unit != null) {
                state = new QuantityType<>(value, unit);
            } else {
                state = new DecimalType(val);
            }
        }
        logger.debug("{}: Updating channel {} with {}", thingId, channel.getUID().getId(), state);
        return thingHandler.updateChannel(channel, state);
    }

    private boolean updateSwitchChannel(Channel channel, ChannelIdMapEntry definition, CNStatusField field) {
        int value = Integer.parseInt(getString(field.value));
        State state;

        if (value == 0) {
            state = UnDefType.UNDEF;
        } else {
            boolean on;
            if (definition.symbolicName.toUpperCase().contains("STATE1_")) {
                on = value == 1; // 1=active, 0=not active
            } else if (definition.symbolicName.toUpperCase().contains("STATE2_")) {
                on = value == 2; // 3=open, 2=closed
            } else if (definition.symbolicName.toUpperCase().contains("STATE3_")
                    || definition.symbolicName.toUpperCase().contains("SAFETY_")) {
                on = value == 3; // 2=open, 3=closed
            } else if (definition.symbolicName.toUpperCase().contains("LOCK2_")) {
                // mark a closed lock ON
                on = value == 2; // 2=open, 3=closed
            } else if (definition.symbolicName.toUpperCase().contains("LOCK3_")) {
                // mark a closed lock ON
                on = value == 3; // 3=open, 2=closed
            } else {
                on = value == 1;
            }
            state = ITEMT_SWITCH.equals(definition.itemType) ? (on ? OnOffType.ON : OnOffType.OFF)
                    : (on ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
        }
        logger.debug("{}: Map value {} to state {} for channe {}, symnolicName{}", thingId, value, state,
                definition.channelName, definition.symbolicName);
        return thingHandler.updateChannel(channel, state);
    }
}
