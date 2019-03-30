/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.binding.lutron.internal.LutronBindingConstants.CHANNEL_SWITCH;

import java.math.BigDecimal;

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
 * Handler responsible for communicating with a switch.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added initDeviceState method
 */
public class SwitchHandler extends LutronHandler {
    private static final Integer ACTION_ZONELEVEL = 1;

    private final Logger logger = LoggerFactory.getLogger(SwitchHandler.class);

    private int integrationId;

    public SwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        integrationId = id.intValue();
        logger.debug("Initializing Switch handler for integration ID {}", id);

        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Switch {}", getIntegrationId());
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
            queryOutput(ACTION_ZONELEVEL); // handleUpdate() will set thing status to online when response arrives
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_SWITCH)) {
            if (command.equals(OnOffType.ON)) {
                output(ACTION_ZONELEVEL, 100);
            } else if (command.equals(OnOffType.OFF)) {
                output(ACTION_ZONELEVEL, 0);
            }
        }
    }

    @Override
    public int getIntegrationId() {
        return this.integrationId;
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.OUTPUT && parameters.length > 1
                && ACTION_ZONELEVEL.toString().equals(parameters[0])) {
            BigDecimal level = new BigDecimal(parameters[1]);
            if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                updateStatus(ThingStatus.ONLINE);
            }
            postCommand(CHANNEL_SWITCH, level.compareTo(BigDecimal.ZERO) == 0 ? OnOffType.OFF : OnOffType.ON);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_SWITCH)) {
            // Refresh state when new item is linked.
            queryOutput(ACTION_ZONELEVEL);
        }
    }
}
