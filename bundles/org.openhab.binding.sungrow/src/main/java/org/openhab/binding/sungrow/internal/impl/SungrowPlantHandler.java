/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sungrow.internal.impl;

import static org.openhab.binding.sungrow.internal.SungrowBindingConstants.CHANNEL_1;

import java.util.concurrent.TimeUnit;

import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.afrouper.server.sungrow.api.dto.BasicPlantInfo;
import de.afrouper.server.sungrow.api.dto.SungrowApiException;

/**
 * The {@link SungrowPlantHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Kemper - Initial contribution
 */
public class SungrowPlantHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SungrowPlantHandler.class);

    private volatile PlantConfiguration plantConfiguration = new PlantConfiguration();

    private SungrowBridgeHandler sungrowBridgeHandler;

    public SungrowPlantHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

        }
    }

    @Override
    public void initialize() {
        plantConfiguration = getConfigAs(PlantConfiguration.class);
        if (!plantConfiguration.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid configuration.");
            return;
        }
        sungrowBridgeHandler = getSungrowBridgeHandler();

        if (sungrowBridgeHandler != null) {
            Integer interval = sungrowBridgeHandler.getConfiguration().getInterval();
            scheduler.scheduleWithFixedDelay(this::updatePlant, interval, interval, TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    private void updatePlant() {
        try {
            BasicPlantInfo basicPlantInfo = sungrowBridgeHandler.getSungrowClient()
                    .getBasicPlantInfo(plantConfiguration.getPlant().plantId());

            // ToDo Update channel

        } catch (SungrowApiException e) {
            logger.error("Unable to update sungrow plant", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private SungrowBridgeHandler getSungrowBridgeHandler() {
        BridgeHandler bridgeHandler = getBridge().getHandler();
        if (bridgeHandler instanceof SungrowBridgeHandler) {
            return (SungrowBridgeHandler) bridgeHandler;
        } else {
            logger.error("Sungrow Bridge not initialized");
            return null;
        }
    }
}
