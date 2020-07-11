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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
    private static final Map<Integer, String> REGULATION_MODES = createRegulationMap();

    /**
     * Creates a new instance of {@link OJElectronicsThermostatHandler}
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
        // nothing do here
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
        updateState(BindingConstants.CHANNEL_OWD5_GROUPNAME, StringType.valueOf(thermostat.groupName));
        updateState(BindingConstants.CHANNEL_OWD5_GROUPID, new DecimalType(thermostat.groupId));
        updateState(BindingConstants.CHANNEL_OWD5_ONLINE,
                thermostat.online ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(BindingConstants.CHANNEL_OWD5_HEATING,
                thermostat.heating ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(BindingConstants.CHANNEL_OWD5_ROOMTEMPERATURE,
                new QuantityType<Temperature>(thermostat.roomTemperature / (double) 100, SIUnits.CELSIUS));
        updateState(BindingConstants.CHANNEL_OWD5_FLOORTEMPERATURE,
                new QuantityType<Temperature>(thermostat.floorTemperature / (double) 100, SIUnits.CELSIUS));
        updateState(BindingConstants.CHANNEL_OWD5_THERMOSTATNAME, StringType.valueOf(thermostat.thermostatName));
        updateState(BindingConstants.CHANNEL_OWD5_REGULATIONMODE,
                StringType.valueOf(getRegulationMode(thermostat.regulationMode)));
        updateState(BindingConstants.CHANNEL_OWD5_COMFORTSETPOINT,
                new QuantityType<Temperature>(thermostat.comfortSetpoint / (double) 100, SIUnits.CELSIUS));
        updateState(BindingConstants.CHANNEL_OWD5_COMFORTENDTIME, new DateTimeType(
                ZonedDateTime.ofInstant(thermostat.comfortEndTime.toInstant(), ZoneId.systemDefault())));
        updateState(BindingConstants.CHANNEL_OWD5_BOOSTENDTIME,
                new DateTimeType(ZonedDateTime.ofInstant(thermostat.boostEndTime.toInstant(), ZoneId.systemDefault())));
        updateState(BindingConstants.CHANNEL_OWD5_MANUALSETPOINT,
                new QuantityType<Temperature>(thermostat.manualModeSetpoint / (double) 100, SIUnits.CELSIUS));
    }

    private String getRegulationMode(int regulationMode) {
        return REGULATION_MODES.get(regulationMode);
    }

    private static HashMap<Integer, String> createRegulationMap() {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        map.put(1, "Auto");
        map.put(2, "Comfort");
        map.put(3, "Manual");
        map.put(4, "Vacation");
        map.put(6, "Frost Protection");
        map.put(8, "Boost");
        map.put(9, "Eco");
        return map;
    };
}
