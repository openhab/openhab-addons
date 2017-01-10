/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.etapu.handler;

import static org.openhab.binding.etapu.EtaPUBindingConstants.CHANNEL_1;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.etapu.SourceConfig;
import org.openhab.binding.etapu.channels.ETAChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EtaPUHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Huber - Initial contribution
 */
public class EtaPUHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(EtaPUHandler.class);
    private SourceConfig sourceConfig;

    public EtaPUHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_1)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {

        sourceConfig = getConfigAs(SourceConfig.class);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    refresh();
                    updateStatus(ThingStatus.ONLINE);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            e.getClass().getName() + ":" + e.getMessage());
                    logger.debug("Error refreshing source " + getThing().getUID(), e);
                }
            }

        }, 0, sourceConfig.refreshInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");

    }

    private void refresh() {

    }

    private void refreshChannel(ETAChannel channel) {
        ClientResource resource;
    }
}