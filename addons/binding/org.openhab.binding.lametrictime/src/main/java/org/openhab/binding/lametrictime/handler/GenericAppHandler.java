/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lametrictime.handler;

import static org.openhab.binding.lametrictime.LaMetricTimeBindingConstants.CHANNEL_APP_FRAMES;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lametrictime.config.LaMetricTimeAppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syphr.lametrictime.api.common.impl.GsonGenerator;
import org.syphr.lametrictime.api.local.model.WidgetUpdates;

import com.google.gson.Gson;

/**
 * The {@link GenericAppHandler} is the parent of all app handlers for
 * the LaMetric Time device.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class GenericAppHandler extends AbstractLaMetricTimeAppHandler {

    private final Logger logger = LoggerFactory.getLogger(GenericAppHandler.class);

    private final Gson gson = GsonGenerator.create();

    public GenericAppHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleAppCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case CHANNEL_APP_FRAMES: {
                    WidgetUpdates frames = gson.fromJson(command.toString(), WidgetUpdates.class);
                    getDevice().getLocalApi().updateApplication(getWidget().getPackageName(),
                            getConfigAs(LaMetricTimeAppConfiguration.class).accessToken, frames);
                    break;
                }
                default:
                    logger.debug("Channel '{}' not supported", channelUID);
                    break;
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.debug("Failed to send frames - taking app offline", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
