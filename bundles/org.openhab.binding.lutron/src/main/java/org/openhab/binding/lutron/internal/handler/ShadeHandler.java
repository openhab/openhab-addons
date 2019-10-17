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

import static org.openhab.binding.lutron.internal.LutronBindingConstants.CHANNEL_SHADELEVEL;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with a Lutron Sivoia QS shade
 *
 * @author Bob Adair - Initial contribution based on Alan Tong's DimmerHandler
 */
public class ShadeHandler extends LutronHandler {
    private static final Integer ACTION_ZONELEVEL = 1;
    private static final Integer ACTION_STARTRAISING = 2;
    private static final Integer ACTION_STARTLOWERING = 3;
    private static final Integer ACTION_STOP = 4;
    private static final Integer ACTION_POSITION_UPDATE = 32; // undocumented in integration protocol guide
    private static final Integer PARAMETER_POSITION_UPDATE = 2; // undocumented in integration protocol guide

    private final Logger logger = LoggerFactory.getLogger(ShadeHandler.class);

    protected int integrationId;

    public ShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        return integrationId;
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        integrationId = id.intValue();
        logger.debug("Initializing Shade handler for integration ID {}", id);

        initDeviceState();
    }

    @Override
    protected void initDeviceState() {
        logger.debug("Initializing device state for Shade {}", getIntegrationId());
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
    public void channelLinked(ChannelUID channelUID) {
        // Refresh state when new item is linked.
        if (channelUID.getId().equals(CHANNEL_SHADELEVEL)) {
            queryOutput(ACTION_ZONELEVEL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_SHADELEVEL)) {
            if (command instanceof PercentType) {
                int level = ((PercentType) command).intValue();
                output(ACTION_ZONELEVEL, level, 0);
            } else if (command.equals(UpDownType.UP)) {
                output(ACTION_STARTRAISING);
            } else if (command.equals(UpDownType.DOWN)) {
                output(ACTION_STARTLOWERING);
            } else if (command.equals(StopMoveType.STOP)) {
                output(ACTION_STOP);
            } else if (command instanceof RefreshType) {
                queryOutput(ACTION_ZONELEVEL);
            }
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.OUTPUT && parameters.length >= 2) {
            if (ACTION_ZONELEVEL.toString().equals(parameters[0])) {
                BigDecimal level = new BigDecimal(parameters[1]);
                if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                    updateStatus(ThingStatus.ONLINE);
                }
                logger.trace("Shade {} received zone level: {}", getIntegrationId(), level);
                updateState(CHANNEL_SHADELEVEL, new PercentType(level));
            } else if (ACTION_POSITION_UPDATE.toString().equals(parameters[0])
                    && PARAMETER_POSITION_UPDATE.toString().equals(parameters[1]) && parameters.length >= 3) {
                BigDecimal level = new BigDecimal(parameters[2]);
                logger.trace("Shade {} received position update: {}", getIntegrationId(), level);
                updateState(CHANNEL_SHADELEVEL, new PercentType(level));
            }
        }
    }
}
