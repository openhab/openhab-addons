/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.ojelectronics.internal.config.OJElectronicsThermostatConfiguration;
import org.openhab.binding.ojelectronics.internal.models.groups.Thermostat;

/**
 * The {@link ThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class ThermostatHandler extends BaseThingHandler {

    private final String serialNumber;
    private @Nullable Thermostat currentThermostat;
    private static final Map<Integer, String> REGULATION_MODES = createRegulationMap();
    private final Map<String, Consumer<Thermostat>> channelrefreshActions = createChannelRefreshActionMap();

    /**
     * Creates a new instance of {@link ThermostatHandler}
     *
     * @param thing Thing
     */
    public ThermostatHandler(Thing thing) {
        super(thing);
        serialNumber = getConfigAs(OJElectronicsThermostatConfiguration.class).serialNumber;
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
                channelrefreshActions.get(channelUID.getId()).accept(thermostat);
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

    private void updateManualSetpoint(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_MANUALSETPOINT,
                new QuantityType<Temperature>(thermostat.manualModeSetpoint / (double) 100, SIUnits.CELSIUS));
    }

    private void updateBoostEndTime(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_BOOSTENDTIME,
                new DateTimeType(ZonedDateTime.ofInstant(thermostat.boostEndTime.toInstant(), ZoneId.systemDefault())));
    }

    private void updateComfortEndTime(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_COMFORTENDTIME, new DateTimeType(
                ZonedDateTime.ofInstant(thermostat.comfortEndTime.toInstant(), ZoneId.systemDefault())));
    }

    private void updateComfortSetpoint(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_COMFORTSETPOINT,
                new QuantityType<Temperature>(thermostat.comfortSetpoint / (double) 100, SIUnits.CELSIUS));
    }

    private void updateRegulationMode(Thermostat thermostat) {
        updateState(BindingConstants.CHANNEL_OWD5_REGULATIONMODE,
                StringType.valueOf(getRegulationMode(thermostat.regulationMode)));
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

    private String getRegulationMode(int regulationMode) {
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
        return map;
    }
}
