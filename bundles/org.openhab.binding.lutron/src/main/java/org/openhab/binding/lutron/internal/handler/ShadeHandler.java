/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.protocol.OutputCommand;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with a Lutron Sivoia QS shade
 *
 * @author Bob Adair - Initial contribution based on Alan Tong's DimmerHandler
 */
@NonNullByDefault
public class ShadeHandler extends LutronHandler {
    private static final Integer PARAMETER_POSITION_UPDATE = 2; // undocumented in integration protocol guide

    private final Logger logger = LoggerFactory.getLogger(ShadeHandler.class);

    protected int integrationId;
    private boolean leap = false;

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

        LutronBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler instanceof LeapBridgeHandler) {
            leap = true;
        }

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
            queryOutput(TargetType.SHADE, OutputCommand.ACTION_ZONELEVEL);
            // handleUpdate() will set thing status to online when response arrives
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // Refresh state when new item is linked.
        if (channelUID.getId().equals(CHANNEL_SHADELEVEL)) {
            queryOutput(TargetType.SHADE, OutputCommand.ACTION_ZONELEVEL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_SHADELEVEL)) {
            if (command instanceof PercentType) {
                int level = ((PercentType) command).intValue();
                output(TargetType.SHADE, OutputCommand.ACTION_ZONELEVEL, level, null, null);
                if (leap) {
                    // LEAP may not send back a position update
                    updateState(CHANNEL_SHADELEVEL, new PercentType(level));
                }
            } else if (command.equals(UpDownType.UP)) {
                output(TargetType.SHADE, OutputCommand.ACTION_STARTRAISING, null, null, null);
                if (leap) {
                    // LEAP won't send a position update when fully open
                    updateState(CHANNEL_SHADELEVEL, new PercentType(100));
                }
            } else if (command.equals(UpDownType.DOWN)) {
                output(TargetType.SHADE, OutputCommand.ACTION_STARTLOWERING, null, null, null);
                if (leap) {
                    // LEAP won't send a position update when fully closed
                    updateState(CHANNEL_SHADELEVEL, new PercentType(0));
                }
            } else if (command.equals(StopMoveType.STOP)) {
                output(TargetType.SHADE, OutputCommand.ACTION_STOP, null, null, null);
            } else if (command instanceof RefreshType) {
                queryOutput(TargetType.SHADE, OutputCommand.ACTION_ZONELEVEL);
            }
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.OUTPUT && parameters.length >= 2) {
            if (OutputCommand.ACTION_ZONELEVEL.toString().equals(parameters[0])) {
                BigDecimal level = new BigDecimal(parameters[1]);
                if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                    updateStatus(ThingStatus.ONLINE);
                }
                logger.trace("Shade {} received zone level: {}", getIntegrationId(), level);
                updateState(CHANNEL_SHADELEVEL, new PercentType(level));
            } else if (OutputCommand.ACTION_POSITION_UPDATE.toString().equals(parameters[0])
                    && PARAMETER_POSITION_UPDATE.toString().equals(parameters[1]) && parameters.length >= 3) {
                BigDecimal level = new BigDecimal(parameters[2]);
                logger.trace("Shade {} received position update: {}", getIntegrationId(), level);
                updateState(CHANNEL_SHADELEVEL, new PercentType(level));
            }
        }
    }
}
