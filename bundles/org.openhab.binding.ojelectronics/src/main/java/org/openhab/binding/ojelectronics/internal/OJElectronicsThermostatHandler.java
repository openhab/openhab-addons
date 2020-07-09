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
 * The {@link OJElectronicsThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author EvilPingu - Initial contribution
 */
@NonNullByDefault
public class OJElectronicsThermostatHandler extends BaseThingHandler {

    private String serialNumber;

    /**
     * Creates a new instance of {@link OJElectronicsThermostatHandler}
     *
     * @param thing Thing
     */
    public OJElectronicsThermostatHandler(Thing thing) {
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
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_GROUPNAME, StringType.valueOf(thermostat.groupName));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_GROUPID, new DecimalType(thermostat.groupId));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_ONLINE,
                thermostat.online ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_HEATING,
                thermostat.heating ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_ROOMTEMPERATURE,
                new QuantityType<Temperature>(thermostat.roomTemperature / (double) 100, SIUnits.CELSIUS));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_FLOORTEMPERATURE,
                new QuantityType<Temperature>(thermostat.floorTemperature / (double) 100, SIUnits.CELSIUS));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_THERMOSTATNAME,
                StringType.valueOf(thermostat.thermostatName));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_REGULATIONMODE,
                StringType.valueOf(thermostat.regulationMode.toString()));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_COMFORTSETPOINT,
                new QuantityType<Temperature>(thermostat.comfortSetpoint / (double) 100, SIUnits.CELSIUS));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_COMFORTENDTIME, new DateTimeType(
                ZonedDateTime.ofInstant(thermostat.comfortEndTime.toInstant(), ZoneId.systemDefault())));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_BOOSTENDTIME,
                new DateTimeType(ZonedDateTime.ofInstant(thermostat.boostEndTime.toInstant(), ZoneId.systemDefault())));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_MANUALSETPOINT,
                new QuantityType<Temperature>(thermostat.manualModeSetpoint / (double) 100, SIUnits.CELSIUS));
    }
}
