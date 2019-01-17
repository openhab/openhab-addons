/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.elkm1.ElkM1BindingConstants;
import org.openhab.binding.elkm1.internal.elk.ElkDefinition;
import org.openhab.binding.elkm1.internal.elk.ElkZoneConfig;
import org.openhab.binding.elkm1.internal.elk.ElkZoneStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Zone handler to handle updates to the zones (locations)
 *
 * @author David Bennett - Initial Contribution
 */
public class ElkM1ZoneHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(ElkM1ZoneHandler.class);

    public ElkM1ZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            getElkM1BridgeHandler().refreshZones();
        }
    }

    /**
     * Updates the zone config with the new zone config.
     *
     * @param config the config to update
     * @param status the status to update
     */
    public void updateZoneConfig(ElkZoneConfig config, ElkZoneStatus status) {
        logger.debug("Update zone config {} {}", config, status);
        Channel chan = getThing().getChannel(ElkM1BindingConstants.CHANNEL_ZONE_CONFIG);
        updateState(chan.getUID(), new StringType(config.toString()));
        chan = getThing().getChannel(ElkM1BindingConstants.CHANNEL_ZONE_STATUS);
        updateState(chan.getUID(), new StringType(status.toString()));
        chan = getThing().getChannel(ElkM1BindingConstants.CHANNEL_ZONE_DEFINITION);
    }

    /**
     * The definition of the zone to update.
     *
     * @param definition the new defintion
     */
    public void updateZoneDefinition(ElkDefinition definition) {
        logger.debug("Update zone definition {}", definition);
        Channel chan = getThing().getChannel(ElkM1BindingConstants.CHANNEL_ZONE_DEFINITION);
        updateState(chan.getUID(), new StringType(definition.toString()));
    }

    /**
     *
     * @param area the new area the zone is inside
     */
    public void updateZoneArea(int area) {
        logger.debug("Update zone area {}", area);
        Channel chan = getThing().getChannel(ElkM1BindingConstants.CHANNEL_ZONE_AREA);
        updateState(chan.getUID(), new DecimalType(area));
    }

    private ElkM1BridgeHandler getElkM1BridgeHandler() {
        return (ElkM1BridgeHandler) getBridge().getHandler();
    }
}
