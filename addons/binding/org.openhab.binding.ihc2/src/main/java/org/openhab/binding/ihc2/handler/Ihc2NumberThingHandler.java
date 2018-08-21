/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.handler;

import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.ihc2.Ihc2BindingConstants.CHANNEL_NUMBER;

import java.util.EventObject;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.ihc2.internal.config.Ihc2NumberThingConfig;
import org.openhab.binding.ihc2.internal.ws.Ihc2Client;
import org.openhab.binding.ihc2.internal.ws.Ihc2EventListener;
import org.openhab.binding.ihc2.internal.ws.Ihc2Execption;
import org.openhab.binding.ihc2.internal.ws.Ihc2TypeUtils;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSResourceValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ihc2NumberThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Niels Peter Enemark - Initial contribution
 */
@NonNullByDefault
public class Ihc2NumberThingHandler extends BaseThingHandler implements Ihc2EventListener {

    private final Logger logger = LoggerFactory.getLogger(Ihc2NumberThingHandler.class);

    private final Ihc2Client ihcClient = Ihc2Client.getInstance();

    @Nullable
    private Ihc2NumberThingConfig config = null;

    public Ihc2NumberThingHandler(Thing thing) {
        super(thing);
        logger.debug("Ihc2NumberThingHandler() for: {}", thing.getUID());
        config = thing.getConfiguration().as(Ihc2NumberThingConfig.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand() {}", command.toFullString());
        if (command == REFRESH) {
            try {
                Command cmd = Ihc2TypeUtils.ihc2oh(CHANNEL_NUMBER, ihcClient.resourceQuery(config.getResourceId()));
                postCommand(CHANNEL_NUMBER, cmd);
            } catch (Ihc2Execption e) {
                logger.warn("handleCommand() ", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_REGISTERING_ERROR, e.getMessage());
            }
            return;
        }

        try {
            WSResourceValue rv;
            rv = Ihc2TypeUtils.oh2ihc(channelUID, command, config.getResourceId());
            ihcClient.resourceUpdate(rv);
        } catch (Ihc2Execption e) {
            logger.error("handleCommand() ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_REGISTERING_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("initialize()");

        config = thing.getConfiguration().as(Ihc2NumberThingConfig.class);

        List<Channel> channelList = thing.getChannels();
        for (Channel c : channelList) {
            ihcClient.setStateDecription(c.getUID(), new StateDescription(config.getMinimum(), config.getMaximum(),
                    config.getStep(), config.getPattern(), config.isReadonly(), null));
        }

        ihcClient.addEventListener(this, config.getResourceId());

        try {
            ihcClient.addResourceId(config.getResourceId());
            updateStatus(ThingStatus.ONLINE);
        } catch (Ihc2Execption e) {
            logger.debug("enableRuntimeValueNotifications() FAILED", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("dispose()");
        ihcClient.removeEventListener(this);
    }

    @Override
    public void statusUpdateReceived(@Nullable EventObject event, @Nullable WSControllerState status) {
        logger.debug("statusUpdateReceived()");
    }

    @Override
    public void resourceValueUpdateReceived(@Nullable EventObject event, @Nullable WSResourceValue value) {
        logger.debug("resourceValueUpdateReceived() {}", value.getResourceID());
        try {
            Command cmd = Ihc2TypeUtils.ihc2oh(CHANNEL_NUMBER, value);
            postCommand(CHANNEL_NUMBER, cmd);
        } catch (Ihc2Execption e) {
            logger.error("resourceValueUpdateReceived() FAILED", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, e.getMessage());
        }
    }

    @Override
    public void errorOccured(@Nullable EventObject event, @Nullable Ihc2Execption e) {
        logger.debug("errorOccured()");
    }
}
