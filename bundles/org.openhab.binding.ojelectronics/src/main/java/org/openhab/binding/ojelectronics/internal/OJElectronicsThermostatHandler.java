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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ojelectronics.internal.config.OJElectronicsThermostatConfiguration;
import org.openhab.binding.ojelectronics.internal.models.groups.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OJElectronicsThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author EvilPingu - Initial contribution
 */
@NonNullByDefault
public class OJElectronicsThermostatHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OJElectronicsThermostatHandler.class);

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
     * Handles changes of the state of the bridge of this thing.
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
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
        logger.debug("Finished initializing!");
    }

    public void handleThermostatRefresh(Thermostat thermostat) {
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_GROUPNAME, StringType.valueOf(thermostat.groupName));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_GROUPID, new DecimalType(thermostat.groupId));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_ONLINE, OnOffType.from(thermostat.online));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_HEATING, OnOffType.from(thermostat.heating));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_ROOMTEMPERATURE,
                new DecimalType(thermostat.roomTemperature / (double) 100));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_FLOORTEMPERATURE,
                new DecimalType(thermostat.floorTemperature / (double) 100));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_THERMOSTATNAME,
                StringType.valueOf(thermostat.thermostatName));
        updateState(OJElectronicsBindingConstants.CHANNEL_OWD5_REGULATIONMODE,
                StringType.valueOf(thermostat.regulationMode.toString()));
    }
}
