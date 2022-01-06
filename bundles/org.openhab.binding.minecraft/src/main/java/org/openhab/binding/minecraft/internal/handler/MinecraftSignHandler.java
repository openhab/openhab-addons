/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.minecraft.internal.handler;

import org.openhab.binding.minecraft.internal.MinecraftBindingConstants;
import org.openhab.binding.minecraft.internal.config.SignConfig;
import org.openhab.binding.minecraft.internal.message.OHMessage;
import org.openhab.binding.minecraft.internal.message.data.SignData;
import org.openhab.binding.minecraft.internal.message.data.commands.SignCommandData;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import rx.Observable;
import rx.Subscription;

/**
 * The {@link MinecraftSignHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class MinecraftSignHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(MinecraftSignHandler.class);

    private MinecraftServerHandler bridgeHandler;
    private Subscription signSubscription;
    private SignConfig config;

    private Gson gson = new GsonBuilder().create();

    public MinecraftSignHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.bridgeHandler = getBridgeHandler();
        this.config = getThing().getConfiguration().as(SignConfig.class);

        if (getThing().getBridgeUID() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");

            return;
        }

        updateStatus(ThingStatus.ONLINE);
        hookupListeners(bridgeHandler);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (!signSubscription.isUnsubscribed()) {
            signSubscription.unsubscribe();
        }
    }

    private String getSignName() {
        return config.getName();
    }

    private void hookupListeners(MinecraftServerHandler bridgeHandler) {
        signSubscription = bridgeHandler.getSignsRx().flatMap(signs -> Observable.from(signs))
                .filter(sign -> config.getName().equals(sign.getName())).subscribe(sign -> updateSignState(sign));
    }

    /**
     * Updates sign state of player.
     *
     * @param sign the sign to update
     */
    private void updateSignState(SignData sign) {
        State activeState = sign.getState() ? OnOffType.ON : OnOffType.OFF;
        updateState(MinecraftBindingConstants.CHANNEL_SIGN_ACTIVE, activeState);
    }

    private synchronized MinecraftServerHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device {}.", getThing().getUID());
            return null;
        } else {
            return getBridgeHandler(bridge);
        }
    }

    private synchronized MinecraftServerHandler getBridgeHandler(Bridge bridge) {
        MinecraftServerHandler bridgeHandler = null;

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof MinecraftServerHandler) {
            bridgeHandler = (MinecraftServerHandler) handler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
            bridgeHandler = null;
        }
        return bridgeHandler;
    }

    @Override
    public void updateState(String channelID, State state) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelID);
        updateState(channelUID, state);
    }

    /**
     * Send a sign command to server.
     *
     * @param type the type of command to send
     * @param signName the sign that the command targets
     * @param value the value related to command
     */
    private void sendSignCommand(String type, String signName, String value) {
        SignCommandData signCommand = new SignCommandData(type, signName, value);
        JsonElement serializedCommand = gson.toJsonTree(signCommand);
        bridgeHandler.sendMessage(new OHMessage(OHMessage.MESSAGE_TYPE_SIGN_COMMANDS, serializedCommand));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case MinecraftBindingConstants.CHANNEL_SIGN_ACTIVE:
                Boolean activeState = command == OnOffType.ON ? true : false;
                sendSignCommand(SignCommandData.COMMAND_SIGN_ACTIVE, getSignName(), activeState.toString());
        }
    }
}
