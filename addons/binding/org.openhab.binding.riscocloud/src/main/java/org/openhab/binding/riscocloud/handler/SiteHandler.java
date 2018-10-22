/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.riscocloud.handler;

import static org.openhab.binding.riscocloud.RiscoCloudBindingConstants.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.riscocloud.RiscoCloudBindingConstants;
import org.openhab.binding.riscocloud.json.ServerDatasHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SiteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sebastien Cantineau - Initial contribution
 *
 */
@NonNullByDefault
public class SiteHandler extends BaseThingHandler {
    private static final int DEFAULT_REFRESH_PERIOD = 30;
    private final Logger logger = LoggerFactory.getLogger(SiteHandler.class);

    private @Nullable SiteBridgeHandler bridge;
    private @Nullable ServerDatasHandler datas;

    private @Nullable ScheduledFuture<?> refreshJob;

    public SiteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing {} handler.", getThing().getThingTypeUID());

        String errorMsg = null;

        if (getBridge() == null) {
            errorMsg = "Invalid bridge";
        } else {
            bridge = (SiteBridgeHandler) getBridge().getHandler();
        }

        if (errorMsg == null) {
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing {} handler with UID {}.", getThing().getThingTypeUID(), getThing().getUID());

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handled command {}", command);

        if (command.equals(OnOffType.ON)) {
            try {
                if (CHANNEL_PART_ARM.equals(channelUID.getId())) {
                    bridge.handleSiteUpdate(ARM_FULL, getPartId());
                } else if (CHANNEL_PART_PARTIALLYARM.equals(channelUID.getId())) {
                    bridge.handleSiteUpdate(ARM_PART, getPartId());
                } else if (CHANNEL_PART_DISARM.equals(channelUID.getId())) {
                    bridge.handleSiteUpdate(DISARM, getPartId());
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            logger.debug("Cannot handle : {}", command);
        }
    }

    protected List<Channel> getChannels() {
        return getThing().getChannels();
    }

    @Override
    protected void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    protected int getPartId() throws Exception {
        if (getThing().getThingTypeUID() == PART_THING_TYPE) {
            throw new Exception("BAD_THING_TYPE");
        }
        Configuration config = getThing().getConfiguration();
        logger.debug("config.get(RiscoCloudBindingConstants.PART_ID) = {}",
                config.get(RiscoCloudBindingConstants.PART_ID));
        return ((BigDecimal) config.get(RiscoCloudBindingConstants.PART_ID)).intValueExact();
    }

}
