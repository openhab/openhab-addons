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
package org.openhab.binding.connectedcar.internal.api.carnet;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNAPI_SERVICE_VEHICLE_STATUS_REPORT;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import javax.measure.IncommensurableException;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiBaseService;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetVehicleStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetVehicleStatus.CNStoredVehicleDataResponse.CNVehicleData.CNStatusData;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetVehicleStatus.CNStoredVehicleDataResponse.CNVehicleData.CNStatusData.CNStatusField;
import org.openhab.binding.connectedcar.internal.handler.CarNetVehicleHandler;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
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
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class CarNetServiceStatus extends ApiBaseService {
    private final Logger logger = LoggerFactory.getLogger(CarNetServiceStatus.class);

    public CarNetServiceStatus(CarNetVehicleHandler thingHandler, CarNetApi api) {
        super(CNAPI_SERVICE_VEHICLE_STATUS_REPORT, thingHandler, api);
    }

    @Override
    public boolean createChannels(Map<String, ChannelIdMapEntry> channels) throws ApiException {
        boolean updated = false;

        // Try to query status information from vehicle
        CarNetVehicleStatus status = api.getVehicleStatus().cnStatus;
        if (status == null) {
            return false;
        }
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

                        logger.debug("{}: {}={}{} (channel {}#{})", thingId, definition.symbolicName,
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

        addChannels(channels, CHANNEL_GROUP_GENERAL, true, CHANNEL_GENERAL_ACTION, CHANNEL_GENERAL_ACTION_STATUS,
                CHANNEL_GENERAL_ACTION_PENDING);
        return updated;
    }

    @Override
    public boolean serviceUpdate() throws ApiException {
        // Try to query status information from vehicle
        logger.debug("{}: Get Vehicle Status", thingId);
        boolean maintenanceRequired = false; // true if any maintenance is required
        boolean vehicleLocked = true; // aggregates all lock states
        boolean windowsClosed = true; // true if all Windows are closed
        boolean tiresOk = true; // tire if all tire pressures are ok
        boolean updated = false;
        api.setConfig(getConfig());
        CarNetVehicleStatus status = api.getVehicleStatus().cnStatus;
        if (status == null) {
            return false; // make the compiler happy, should never happen, because on failure an exception is thrown
        }
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
        updated |= updateChannel(CHANNEL_STATUS_LOCKED, getOnOff(vehicleLocked));
        updated |= updateChannel(CHANNEL_CONTROL_LOCK, getOnOff(vehicleLocked));
        updated |= updateChannel(CHANNEL_STATUS_MAINTREQ, getOnOff(maintenanceRequired));
        updated |= updateChannel(CHANNEL_STATUS_TIRESOK, getOnOff(tiresOk));
        updated |= updateChannel(CHANNEL_STATUS_WINCLOSED, getOnOff(windowsClosed));

        return updated;
    }

    private boolean checkMaintenance(CNStatusField field, ChannelIdMapEntry definition) {
        if (definition.symbolicName.contains("MAINT_")) {
            if (definition.symbolicName.contains("ALARM_") && "1".equals(field.value)) {
                // MAINT_ALARM_INSPECTION+MAINT_ALARM_OIL_CHANGE + MAINT_ALARM_OIL_MINIMUM
                logger.debug("{}: Maintenance required: Alarm {} was detected", thingId, definition.symbolicName);
                return true;
            } else if (definition.symbolicName.contains("DISTANCE_")
                    && Math.abs(Double.parseDouble(field.value)) < 1000.0) {
                // MAINT_DISTANCE_INSPECTION | MAINT_OIL_DISTANCE_CHANGE MAINT_| AD_BLUE_DISTANCE
                logger.debug("{}: Maintenance required: Remaining distance for {} is {}km, therefore < 1.000km",
                        thingId, definition.symbolicName, field.value);
                return true;
            }
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
        if ((definition.symbolicName.contains("_WINDOW") /* || definition.symbolicName.contains("_ROOF_") */)
                && definition.symbolicName.contains("STATE3") && !"3".equals(field.value) && !"0".equals(field.value)) {
            logger.debug("{}: Window {} is not closed ({})", thingId, definition.channelName, field.value);
            return false;
        }
        return true;
    }

    private boolean checkTires(CNStatusField field, ChannelIdMapEntry definition) {
        if (definition.symbolicName.contains("TPRESS") && definition.symbolicName.contains("CURRENT")
                && !"1".equals(field.value)) {
            logger.debug("{}: Tire pressure for {} is not ok", thingId, definition.channelName);
            return false;
        }
        return true;
    }

    private boolean updateNumberChannel(Channel channel, ChannelIdMapEntry definition, CNStatusField field) {
        State state = UnDefType.UNDEF;
        String val = getString(field.value);
        if (!val.isEmpty() && isNumeric(val)) {
            double value = Double.parseDouble(val);
            if (value < 0) {
                value = definition.symbolicName.startsWith("GT0") ? 0 : value * -1.0; // no egative values
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
        return updateChannel(channel, state);
    }

    private boolean updateSwitchChannel(Channel channel, ChannelIdMapEntry definition, CNStatusField field) {
        try {
            int value = Integer.parseInt(getString(field.value));
            boolean on = false;
            State state;

            String symbolicName = definition.symbolicName.toUpperCase();
            if (symbolicName.startsWith("STATE") || symbolicName.startsWith("LOCK")
                    || symbolicName.startsWith("SAFETY")) {
                if (symbolicName.startsWith("STATE1")) {
                    on = value == 1; // 1=active, 0=not active
                } else if (symbolicName.startsWith("STATE2") || symbolicName.startsWith("LOCK2")) {
                    on = value == 2; // 3=open, 2=closed
                } else if (symbolicName.startsWith("STATE3") || symbolicName.startsWith("LOCK3")
                        || symbolicName.startsWith("SAFETY")) {
                    on = value == 3; // 2=open, 3=closed
                } else {
                    on = value == 1;
                }
            } else if (value == 0) {
                on = value == 1;
            }

            state = ITEMT_SWITCH.equals(definition.itemType) ? (on ? OnOffType.ON : OnOffType.OFF)
                    : (on ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
            logger.debug("{}: Map {}={} to state {} for channel {}", thingId, definition.symbolicName, value, state,
                    definition.channelName);
            return updateChannel(channel, state);
        } catch (NumberFormatException e) {
            logger.debug("{}: Unable to parse field value for {}: {}", thingId, field.id, field.value);
            return false;
        }
    }
}
