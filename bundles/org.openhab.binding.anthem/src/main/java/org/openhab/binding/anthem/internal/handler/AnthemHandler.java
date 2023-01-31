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
package org.openhab.binding.anthem.internal.handler;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.anthem.internal.AnthemConfiguration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AnthemHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AnthemHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(AnthemHandler.class);

    private @Nullable AnthemConnectionManager anthemConnectionManager;

    public AnthemHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        AnthemConfiguration configuration = getConfig().as(AnthemConfiguration.class);
        logger.debug("AnthemHandler: Configuration of thing {} is {}", thing.getUID().getId(), configuration);

        if (!configuration.isValid()) {
            logger.debug("AnthemHandler: Config of thing '{}' is invalid", thing.getUID().getId());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Anthem thing configuration.");
            return;
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Connecting");
        anthemConnectionManager = new AnthemConnectionManager(this, configuration);
    }

    @Override
    public void dispose() {
        AnthemConnectionManager localAnthemConnectionManager = anthemConnectionManager;
        if (localAnthemConnectionManager != null) {
            localAnthemConnectionManager.dispose();
            anthemConnectionManager = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AnthemConnectionManager localConnectionManager = anthemConnectionManager;
        if (localConnectionManager != null) {
            localConnectionManager.handleCommand(channelUID, command);
        }
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public void setThingProperty(String property, String value) {
        thing.setProperty(property, value);
    }

    public void updateThingStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail, String status) {
        updateStatus(thingStatus, thingStatusDetail, status);
    }

    public void updateThingStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail) {
        updateStatus(thingStatus, thingStatusDetail);
    }

    public void updateThingStatus(ThingStatus thingStatus) {
        updateStatus(thingStatus);
    }

    public void updateChannelState(String zone, String channelId, State state) {
        updateState(zone + "#" + channelId, state);
    }
}
