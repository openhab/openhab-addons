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
package org.openhab.binding.lutron.internal.handler;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.CHANNEL_OCCUPANCYSTATUS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added initDeviceState method
 */
@NonNullByDefault
public class OccupancySensorHandler extends LutronHandler {
    private static final String OCCUPIED_STATE_UPDATE = "2";
    private static final String STATE_OCCUPIED = "3";
    private static final String STATE_UNOCCUPIED = "4";

    private final Logger logger = LoggerFactory.getLogger(OccupancySensorHandler.class);

    private int integrationId;

    public OccupancySensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        integrationId = id.intValue();
        logger.debug("Initializing Occupancy Sensor handler for integration ID {}", id);

        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Occupancy Sensor {}", getIntegrationId());
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE); // can't poll this device, so assume it is online if the bridge is
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public int getIntegrationId() {
        return this.integrationId;
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.DEVICE && parameters.length == 2 && OCCUPIED_STATE_UPDATE.equals(parameters[0])) {
            if (STATE_OCCUPIED.equals(parameters[1])) {
                updateState(CHANNEL_OCCUPANCYSTATUS, OnOffType.ON);
            } else if (STATE_UNOCCUPIED.equals(parameters[1])) {
                updateState(CHANNEL_OCCUPANCYSTATUS, OnOffType.OFF);
            }
        }
    }
}
