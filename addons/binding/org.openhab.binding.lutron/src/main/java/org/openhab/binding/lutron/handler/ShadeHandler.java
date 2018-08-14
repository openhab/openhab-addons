/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import static org.openhab.binding.lutron.LutronBindingConstants.CHANNEL_SHADELEVEL;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
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
 * @author Bob Adair - Initial contribution
 *         Based on Alan Tong's DimmerHandler
 */
public class ShadeHandler extends LutronHandler {
    private static final Integer ACTION_ZONELEVEL = 1;
    private static final Integer ACTION_STARTRAISING = 2;
    private static final Integer ACTION_STARTLOWERING = 3;
    private static final Integer ACTION_STOP = 4;

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

        if (getThing().getBridgeUID() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        queryOutput(ACTION_ZONELEVEL);
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
        if (type == LutronCommandType.OUTPUT && parameters.length > 1
                && ACTION_ZONELEVEL.toString().equals(parameters[0])) {
            BigDecimal level = new BigDecimal(parameters[1]);
            updateState(CHANNEL_SHADELEVEL, new PercentType(level));
        }
    }
}
