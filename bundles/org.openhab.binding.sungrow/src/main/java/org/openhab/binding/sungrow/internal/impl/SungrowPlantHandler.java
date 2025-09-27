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

import java.util.concurrent.TimeUnit;

import org.openhab.binding.sungrow.internal.SungrowBindingConstants;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.afrouper.server.sungrow.api.dto.BasicPlantInfo;
import de.afrouper.server.sungrow.api.dto.Plant;
import de.afrouper.server.sungrow.api.dto.SungrowApiException;
import de.afrouper.server.sungrow.api.dto.UnitValuePair;

/**
 * The {@link SungrowPlantHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Kemper - Initial contribution
 */
public class SungrowPlantHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SungrowPlantHandler.class);

    private Plant plant;

    private SungrowBridgeHandler sungrowBridgeHandler;

    public SungrowPlantHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        sungrowBridgeHandler = getSungrowBridgeHandler();
        plant = sungrowBridgeHandler.getPlant(getThing().getUID());

        if (sungrowBridgeHandler != null) {
            scheduler.execute(this::initPlant);
            Integer interval = sungrowBridgeHandler.getConfiguration().getInterval();
            scheduler.scheduleWithFixedDelay(this::updatePlant, interval, interval, TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    private void initPlant() {
        updateStringChannel("name", plant.plantName());
        updateStringChannel("type", plant.plantType().name());
        updateStringChannel("connect-type", plant.connectType().name());
        updateQuantityChannel("total-energy", format(plant.totalEnergy()));
        updateQuantityChannel("today-energy", format(plant.todayEnergy()));
        updateQuantityChannel("current-power", format(plant.currentPower()));
        updateQuantityChannel("total-co2-reduce", format(plant.co2ReduceTotal()));
        updateQuantityChannel("today-co2-reduce", format(plant.co2Reduce()));
        updateQuantityChannel("total-income", format(plant.totalIncome()));
        updateQuantityChannel("today-income", format(plant.todayIncome()));
    }

    private void updatePlant() {
        try {
            BasicPlantInfo basicPlantInfo = sungrowBridgeHandler.getSungrowClient().getBasicPlantInfo(plant.plantId());

            // ToDo Update channel

        } catch (SungrowApiException e) {
            logger.error("Unable to update sungrow plant", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateQuantityChannel(String channelId, String value) {
        updateState(new ChannelUID(thing.getUID(), SungrowBindingConstants.CHANNEL_GROUP_PLANT, channelId),
                QuantityType.valueOf(value));
    }

    private void updateStringChannel(String channelId, String value) {
        updateState(new ChannelUID(thing.getUID(), SungrowBindingConstants.CHANNEL_GROUP_PLANT, channelId),
                StringType.valueOf(value));
    }

    private void updateNumberChannel(String channelId, String value) {
        updateState(new ChannelUID(thing.getUID(), SungrowBindingConstants.CHANNEL_GROUP_PLANT, channelId),
                QuantityType.valueOf(value));
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

    private String format(UnitValuePair pair) {
        if (pair == null) {
            return null;
        } else {
            return pair.value() + " " + pair.unit();
        }
    }
}
