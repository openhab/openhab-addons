/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import static org.openhab.binding.lutron.LutronBindingConstants.CHANNEL_LIGHTLEVEL;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.internal.config.DimmerConfig;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with a light dimmer.
 *
 * @author Allan Tong - Initial contribution
 */
public class DimmerHandler extends LutronHandler {
    private static final Integer ACTION_ZONELEVEL = 1;

    private Logger logger = LoggerFactory.getLogger(DimmerHandler.class);

    private DimmerConfig config;

    public DimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public int getIntegrationId() {
        if (this.config == null) {
            throw new IllegalStateException("handler not initialized");
        }

        return this.config.getIntegrationId();
    }

    @Override
    public void initialize() {
        this.config = getThing().getConfiguration().as(DimmerConfig.class);

        if (this.config.getIntegrationId() <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");

            return;
        }

        if (getThing().getBridgeUID() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");

            return;
        }

        updateStatus(ThingStatus.ONLINE);
        queryOutput(ACTION_ZONELEVEL);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            // Refresh state when new item is linked.
            queryOutput(ACTION_ZONELEVEL);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            if (command instanceof Number) {
                int level = ((Number) command).intValue();

                output(ACTION_ZONELEVEL, level, 0.25);
            } else if (command.equals(OnOffType.ON)) {
                output(ACTION_ZONELEVEL, 100, this.config.getFadeInTime());
            } else if (command.equals(OnOffType.OFF)) {
                output(ACTION_ZONELEVEL, 0, this.config.getFadeOutTime());
            }
        }
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        if (type == LutronCommandType.OUTPUT && parameters.length > 1
                && ACTION_ZONELEVEL.toString().equals(parameters[0])) {
            BigDecimal level = new BigDecimal(parameters[1]);

            updateState(CHANNEL_LIGHTLEVEL, new PercentType(level));
        }
    }
}
