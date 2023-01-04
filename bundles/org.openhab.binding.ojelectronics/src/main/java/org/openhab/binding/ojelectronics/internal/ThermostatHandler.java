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
package org.openhab.binding.ojelectronics.internal;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ojelectronics.internal.config.OJElectronicsThermostatConfiguration;
import org.openhab.binding.ojelectronics.internal.models.Thermostat;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class ThermostatHandler extends BaseThingHandler {

    private static final Map<Integer, String> REGULATION_MODES = createRegulationMap();
    private static final Map<String, Integer> REVERSE_REGULATION_MODES = createRegulationReverseMap();

    private final String serialNumber;
    private final Logger logger = LoggerFactory.getLogger(ThermostatHandler.class);
    private final Map<String, Consumer<Thermostat>> channelrefreshActions = createChannelRefreshActionMap();
    private final Map<String, Consumer<Command>> updateThermostatValueActions = createUpdateThermostatValueActionMap();
    private final TimeZoneProvider timeZoneProvider;

    private LinkedList<AbstractMap.SimpleImmutableEntry<String, Command>> updatedValues = new LinkedList<>();
    private @Nullable Thermostat currentThermostat;

    /**
     * Creates a new instance of {@link ThermostatHandler}
     *
     * @param thing Thing
     * @param timeZoneProvider Time zone
     */
    public ThermostatHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        serialNumber = getConfigAs(OJElectronicsThermostatConfiguration.class).serialNumber;
        this.timeZoneProvider = timeZoneProvider;
    }

    /**
     * Gets the thing's serial number.
     *
     * @return serial number
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Handles commands to this thing.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            final Thermostat thermostat = currentThermostat;
            if (thermostat != null && channelrefreshActions.containsKey(channelUID.getId())) {
                final @Nullable Consumer<Thermostat> consumer = channelrefreshActions.get(channelUID.getId());
                if (consumer != null) {
                    consumer.accept(thermostat);
                }
            }
        } else {
            synchronized (this) {
                updatedValues.add(new AbstractMap.SimpleImmutableEntry<String, Command>(channelUID.getId(), command));
            }
        }
    }

    /**
     * Initializes the thing handler.
     */
    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Sets the values after refreshing the thermostats values
     *
     * @param thermostat thermostat values
     */
    public void handleThermostatRefresh(Thermostat thermostat) {
        currentThermostat = thermostat;
        channelrefreshActions.forEach((channelUID, action) -> action.accept(thermostat));
    }

    /**
     * Gets a {@link Thermostat} with changed values or null if nothing has changed
     *
     * @return The changed {@link Thermostat}
     */
    public @Nullable Thermostat tryHandleAndGetUpdatedThermostat() {
        final LinkedList<SimpleImmutableEntry<String, Command>> updatedValues = this.updatedValues;
        if (updatedValues.isEmpty()) {
            return null;
        }
        this.updatedValues = new LinkedList<>();
        updatedValues.forEach(item -> {
            if (updateThermostatValueActions.containsKey(item.getKey())) {
                final @Nullable Consumer<Command> consumer = updateThermostatValueActions.get(item.getKey());
                if (consumer != null) {
                    consumer.accept(item.getValue());
                }
            }
        });
        return currentThermostat;
    }

    private void updateManualSetpoint(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_MANUALSETPOINT,
                new QuantityType<Temperature>(thermostat.manualModeSetpoint / (double) 100, SIUnits.CELSIUS));
    }

    private void updateManualSetpoint(Command command) {
        if (command instanceof QuantityType<?>) {
            currentThermostat.manualModeSetpoint = (int) (((QuantityType<?>) command).floatValue() * 100);
        } else {
            logger.warn("Unable to set value {}", command);
        }
    }

    private void updateBoostEndTime(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_BOOSTENDTIME, new DateTimeType(
                ZonedDateTime.ofInstant(thermostat.boostEndTime.toInstant(), timeZoneProvider.getTimeZone())));
    }

    private void updateBoostEndTime(Command command) {
        if (command instanceof DateTimeType) {
            currentThermostat.boostEndTime = Date.from(((DateTimeType) command).getZonedDateTime().toInstant());
        } else {
            logger.warn("Unable to set value {}", command);
        }
    }

    private void updateComfortEndTime(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_COMFORTENDTIME, new DateTimeType(
                ZonedDateTime.ofInstant(thermostat.comfortEndTime.toInstant(), timeZoneProvider.getTimeZone())));
    }

    private void updateComfortEndTime(Command command) {
        if (command instanceof DateTimeType) {
            currentThermostat.comfortEndTime = Date.from(((DateTimeType) command).getZonedDateTime().toInstant());
        } else {
            logger.warn("Unable to set value {}", command);
        }
    }

    private void updateComfortSetpoint(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_COMFORTSETPOINT,
                new QuantityType<Temperature>(thermostat.comfortSetpoint / (double) 100, SIUnits.CELSIUS));
    }

    private void updateComfortSetpoint(Command command) {
        if (command instanceof QuantityType<?>) {
            currentThermostat.comfortSetpoint = (int) (((QuantityType<?>) command).floatValue() * 100);
        } else {
            logger.warn("Unable to set value {}", command);
        }
    }

    private void updateRegulationMode(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_REGULATIONMODE,
                StringType.valueOf(getRegulationMode(thermostat.regulationMode)));
    }

    private void updateRegulationMode(Command command) {
        if (command instanceof StringType && (REVERSE_REGULATION_MODES.containsKey(command.toString().toLowerCase()))) {
            final @Nullable Integer mode = REVERSE_REGULATION_MODES.get(command.toString().toLowerCase());
            if (mode != null) {
                currentThermostat.regulationMode = mode;
            }
        } else {
            logger.warn("Unable to set value {}", command);
        }
    }

    private void updateThermostatName(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_THERMOSTATNAME, StringType.valueOf(thermostat.thermostatName));
    }

    private void updateFloorTemperature(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_FLOORTEMPERATURE,
                new QuantityType<Temperature>(thermostat.floorTemperature / (double) 100, SIUnits.CELSIUS));
    }

    private void updateRoomTemperature(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_ROOMTEMPERATURE,
                new QuantityType<Temperature>(thermostat.roomTemperature / (double) 100, SIUnits.CELSIUS));
    }

    private void updateHeating(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_HEATING,
                thermostat.heating ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
    }

    private void updateOnline(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_ONLINE,
                thermostat.online ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
    }

    private void updateGroupId(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_GROUPID, new DecimalType(thermostat.groupId));
    }

    private void updateGroupName(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_GROUPNAME, StringType.valueOf(thermostat.groupName));
    }

    private void updateVacationEnabled(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_VACATIONENABLED,
                thermostat.online ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
    }

    private void updateVacationBeginDay(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_VACATIONBEGINDAY,
                new DateTimeType(
                        ZonedDateTime.ofInstant(thermostat.vacationBeginDay.toInstant(), timeZoneProvider.getTimeZone())
                                .truncatedTo(ChronoUnit.DAYS)));
    }

    private void updateVacationBeginDay(Command command) {
        if (command instanceof DateTimeType) {
            currentThermostat.vacationBeginDay = Date
                    .from(((DateTimeType) command).getZonedDateTime().toInstant().truncatedTo(ChronoUnit.DAYS));
        } else {
            logger.warn("Unable to set value {}", command);
        }
    }

    private void updateVacationEndDay(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_VACATIONENDDAY,
                new DateTimeType(
                        ZonedDateTime.ofInstant(thermostat.vacationEndDay.toInstant(), timeZoneProvider.getTimeZone())
                                .truncatedTo(ChronoUnit.DAYS)));
    }

    private void updateVacationEndDay(Command command) {
        if (command instanceof DateTimeType) {
            currentThermostat.vacationEndDay = Date
                    .from(((DateTimeType) command).getZonedDateTime().toInstant().truncatedTo(ChronoUnit.DAYS));
        } else {
            logger.warn("Unable to set value {}", command);
        }
    }

    private @Nullable String getRegulationMode(int regulationMode) {
        return REGULATION_MODES.get(regulationMode);
    }

    private static Map<Integer, String> createRegulationMap() {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(1, "auto");
        map.put(2, "comfort");
        map.put(3, "manual");
        map.put(4, "vacation");
        map.put(6, "frostProtection");
        map.put(8, "boost");
        map.put(9, "eco");
        return map;
    };

    private static Map<String, Integer> createRegulationReverseMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("auto", 1);
        map.put("comfort", 2);
        map.put("manual", 3);
        map.put("vacation", 4);
        map.put("frostprotection", 6);
        map.put("boost", 8);
        map.put("eco", 9);
        return map;
    };

    private Map<String, Consumer<Thermostat>> createChannelRefreshActionMap() {
        HashMap<String, Consumer<Thermostat>> map = new HashMap<>();
        map.put(BindingConstants.CHANNEL_OWD5_GROUPNAME, this::updateGroupName);
        map.put(BindingConstants.CHANNEL_OWD5_GROUPID, this::updateGroupId);
        map.put(BindingConstants.CHANNEL_OWD5_ONLINE, this::updateOnline);
        map.put(BindingConstants.CHANNEL_OWD5_HEATING, this::updateHeating);
        map.put(BindingConstants.CHANNEL_OWD5_ROOMTEMPERATURE, this::updateRoomTemperature);
        map.put(BindingConstants.CHANNEL_OWD5_FLOORTEMPERATURE, this::updateFloorTemperature);
        map.put(BindingConstants.CHANNEL_OWD5_THERMOSTATNAME, this::updateThermostatName);
        map.put(BindingConstants.CHANNEL_OWD5_REGULATIONMODE, this::updateRegulationMode);
        map.put(BindingConstants.CHANNEL_OWD5_COMFORTSETPOINT, this::updateComfortSetpoint);
        map.put(BindingConstants.CHANNEL_OWD5_COMFORTENDTIME, this::updateComfortEndTime);
        map.put(BindingConstants.CHANNEL_OWD5_BOOSTENDTIME, this::updateBoostEndTime);
        map.put(BindingConstants.CHANNEL_OWD5_MANUALSETPOINT, this::updateManualSetpoint);
        map.put(BindingConstants.CHANNEL_OWD5_VACATIONENABLED, this::updateVacationEnabled);
        map.put(BindingConstants.CHANNEL_OWD5_VACATIONBEGINDAY, this::updateVacationBeginDay);
        map.put(BindingConstants.CHANNEL_OWD5_VACATIONENDDAY, this::updateVacationEndDay);
        return map;
    }

    private Map<String, Consumer<Command>> createUpdateThermostatValueActionMap() {
        HashMap<String, Consumer<Command>> map = new HashMap<>();
        map.put(BindingConstants.CHANNEL_OWD5_REGULATIONMODE, this::updateRegulationMode);
        map.put(BindingConstants.CHANNEL_OWD5_MANUALSETPOINT, this::updateManualSetpoint);
        map.put(BindingConstants.CHANNEL_OWD5_BOOSTENDTIME, this::updateBoostEndTime);
        map.put(BindingConstants.CHANNEL_OWD5_COMFORTENDTIME, this::updateComfortEndTime);
        map.put(BindingConstants.CHANNEL_OWD5_COMFORTSETPOINT, this::updateComfortSetpoint);
        map.put(BindingConstants.CHANNEL_OWD5_VACATIONBEGINDAY, this::updateVacationBeginDay);
        map.put(BindingConstants.CHANNEL_OWD5_VACATIONENDDAY, this::updateVacationEndDay);
        return map;
    }
}
