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

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.riscocloud.RiscoCloudBindingConstants;
import org.openhab.binding.riscocloud.json.ServerDatasHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SiteBridgeHandler} is the handler for RiscoCloud API and connects it
 * to the webservice.
 *
 * @author Sebastien Cantineau - Initial contribution
 *
 */
@NonNullByDefault
public class SiteBridgeHandler extends RiscoCloudBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SiteBridgeHandler.class);

    private @Nullable Configuration config = null;
    private @Nullable ServerDatasHandler serverDatasHandler;
    private @Nullable ScheduledFuture<?> refreshJob;
    private LoginResult loginResult = new LoginResult();

    public SiteBridgeHandler(Bridge bridge) {
        super(bridge);
        serverDatasHandler = new ServerDatasHandler();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RiscoCloud site bridge handler.");
        config = getThing().getConfiguration();
        startAutomaticRefresh();
    }

    /**
     * Start the job refreshing the data
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                try {
                    updateThings();
                } catch (Exception e) {
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                }
            };

            int delay = 10;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.SECONDS);
        }
    }

    public @Nullable ServerDatasHandler getServerDatasHandler() {
        return serverDatasHandler;
    }

    public String getSiteName() {
        return (String) config.get(RiscoCloudBindingConstants.SITE_NAME);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not needed
    }

    public void handleSiteUpdate(String command, int idPart) {
        if (HANDLED_COMMANDS.contains(command)) {
            try {
                WebSiteInterface.webSiteSendCommand(config, command, idPart);
            } catch (IOException e) {
                logger.debug("handleSiteUpdate() : function = '{}': idPart = '{}' : got exception = '{}'", command,
                        idPart, e);
            }
        } else {
            logger.debug("handleSiteUpdate() : command = '{}': idPart = '{}'", command, idPart);
        }
    }

    private void updateThings() {
        try {
            loginResult = WebSiteInterface.webSitePoll(config);
        } catch (IOException e) {
            loginResult.error = loginResult.error.trim();
            logger.debug("Disabling thing '{}': Error '{}': {}", getThing().getUID(), loginResult.error,
                    loginResult.errorDetail);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, loginResult.statusDescr);
        }
        // Updates the thing status accordingly
        if (loginResult.serverDatasHandler != null && loginResult.serverDatasHandler.isValidObject()) {
            // logger.debug("serverDatasHandler = {}", loginResult.serverDatasHandler.toString());
            // logger.debug("newServerDatasHandler = {}", loginResult.serverDatasHandler.toString());
            serverDatasHandler = loginResult.serverDatasHandler;
        }
        if (loginResult.error == null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            // logger.debug("loginResult '{}'", loginResult.toString());
            loginResult.error = loginResult.error.trim();
            logger.debug("Disabling thing '{}': Error '{}': {}", getThing().getUID(), loginResult.error,
                    loginResult.errorDetail);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, loginResult.statusDescr);
            return;
        }

        getThing().getThings().forEach(thing -> {
            SiteHandler handler = new SiteHandler(thing);
            handler.updateStatus(ThingStatus.ONLINE);
            if (thing.getThingTypeUID().equals(OVERVIEW_THING_TYPE)) {
                handler.getChannels().forEach(channel -> {
                    logger.debug("Update channel '{}': with type '{}': and label {} : and id {}", channel.getUID(),
                            channel.getChannelTypeUID(), channel.getLabel(), channel.getUID().getId());
                    switch (channel.getUID().getId()) {
                        case CHANNEL_ONLINE_STATUS:
                            updateState(channel.getUID(), serverDatasHandler.getIsOnline());
                            break;
                        case CHANNEL_ONGOING_ALARM:
                            updateState(channel.getUID(), serverDatasHandler.getIsOngoingAlarm());
                            break;
                        case CHANNEL_ARMED_PARTS_NB:
                            updateState(channel.getUID(), serverDatasHandler.getArmedPartNb());
                            break;
                        case CHANNEL_DISARMED_PARTS_NB:
                            updateState(channel.getUID(), serverDatasHandler.getDisarmedPartNb());
                            break;
                        case CHANNEL_PARTIALLYARMED_PARTS_NB:
                            updateState(channel.getUID(), serverDatasHandler.getPartiallyArmedPartNb());
                            break;
                    }
                });
            } else if (thing.getThingTypeUID().equals(PART_THING_TYPE)) {
                handler.getChannels().forEach(channel -> {
                    logger.debug("Update channel '{}': with type '{}': and label {} : and id {}", channel.getUID(),
                            channel.getChannelTypeUID(), channel.getLabel(), channel.getUID().getId());
                    switch (channel.getUID().getId()) {
                        case CHANNEL_PART_ARM:
                            try {
                                updateState(channel.getUID(), serverDatasHandler.getIsPartArmed(handler.getPartId()));
                            } catch (Exception e) {
                                logger.warn("Got Exception during update state : CHANNEL_PART_ARM : Message = {}", e);
                            }
                            break;
                        case CHANNEL_PART_PARTIALLYARM:
                            try {
                                updateState(channel.getUID(),
                                        serverDatasHandler.getIsPartPartiallyArmed(handler.getPartId()));
                            } catch (Exception e) {
                                logger.warn(
                                        "Got Exception during update state : CHANNEL_PART_PARTIALLYARM : Message = {}",
                                        e);
                            }
                            break;
                        case CHANNEL_PART_DISARM:
                            try {
                                updateState(channel.getUID(),
                                        serverDatasHandler.getIsPartDisarmed(handler.getPartId()));
                            } catch (Exception e) {
                                logger.warn("Got Exception during update state : CHANNEL_PART_DISARM : Message = {}",
                                        e);
                            }
                            break;
                    }
                });
            }
        });
    }

}
