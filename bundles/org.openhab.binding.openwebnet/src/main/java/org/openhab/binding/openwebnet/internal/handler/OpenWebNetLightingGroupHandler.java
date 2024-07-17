/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereLightAutom;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetLightingGroupHandler} is responsible for handling commands/messages for a Lighting OpenWebNet
 * group.
 * It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution.
 */
@NonNullByDefault
public class OpenWebNetLightingGroupHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetLightingGroupHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.LIGHTING_GROUP_SUPPORTED_THING_TYPES;

    protected Set<String> listOn = new HashSet<String>();

    public OpenWebNetLightingGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        super.requestChannelState(channel);
        Where deviceWhere = this.deviceWhere;
        if (deviceWhere != null) {
            try {
                send(Lighting.requestStatus(deviceWhere.value()));
            } catch (OWNException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        if (refreshAll) {
            logger.debug("--- refreshDevice() : refreshing GENERAL... ({})", thing.getUID());
            try {
                send(Lighting.requestStatus(WhereLightAutom.GENERAL.value()));
            } catch (OWNException e) {
                logger.warn("Excpetion while requesting all devices refresh: {}", e.getMessage());
            }
        } else {
            logger.debug("--- refreshDevice() : refreshing SINGLE... ({})", thing.getUID());

            requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_SWITCH_01));
        }
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        switch (channel.getId()) {
            case CHANNEL_SWITCH:
                handleSwitchCommand(channel, command);
                break;
            default: {
                logger.warn("Unsupported ChannelUID {}", channel);
            }
        }
    }

    /**
     * Handles Lighting switch command for a channel
     *
     * @param channel the channel
     * @param command the command
     */
    private void handleSwitchCommand(ChannelUID channel, Command command) {
        logger.debug("handleSwitchCommand() (command={} - channel={})", command, channel);
        Where deviceWhere = this.deviceWhere;
        if (command instanceof OnOffType && deviceWhere != null) {
            try {
                if (OnOffType.ON.equals(command)) {
                    send(Lighting.requestTurnOn(deviceWhere.value()));
                } else if (OnOffType.OFF.equals(command)) {
                    send(Lighting.requestTurnOff(deviceWhere.value()));
                }
            } catch (OWNException e) {
                logger.warn("Exception while processing command {}: {}", command, e.getMessage());
            }
        } else {
            logger.warn("Unsupported command: {}", command);
        }
    }

    @Override
    protected Who getManagedWho() {
        return Who.LIGHTING;
    }

    protected void handlePropagatedMessage(Lighting lmsg, @Nullable String oId) {
        logger.debug("handlePropagatedMessage({}) for thing: {}", lmsg, thing.getUID());

        WhereLightAutom deviceWhere = (WhereLightAutom) this.deviceWhere;
        if (deviceWhere != null && oId != null) {
            int sizeBefore = listOn.size();
            if (!lmsg.isOff()) {
                if (listOn.add(oId)) {
                    logger.debug("ADDED {} to listOn for {}", oId, deviceWhere);
                }
            } else {
                if (listOn.remove(oId)) {
                    logger.debug("REMOVED {} from listOn for {}", oId, deviceWhere);
                }
            }
            logger.debug("listOn for {}: {}", deviceWhere, listOn);

            boolean listOnChanged = false;

            if (!listOn.isEmpty()) {
                // some light still on
                logger.debug("some light ON... switching group {} to ON", deviceWhere);
                updateState(CHANNEL_SWITCH, OnOffType.ON);
                listOnChanged = (sizeBefore == 0);
            } else {
                // no light is ON anymore
                logger.debug("all lights OFF ... switching group {} to OFF ", deviceWhere);
                updateState(CHANNEL_SWITCH, OnOffType.OFF);
                listOnChanged = (sizeBefore > 0);
            }
            if (listOnChanged && !deviceWhere.isGeneral()) {
                // Area has changed state, propagate APL msg to GEN handler, if exists
                OpenWebNetBridgeHandler bridgeHandler = this.bridgeHandler;
                if (bridgeHandler != null) {
                    String genOwnId = this.getManagedWho().value() + ".0";
                    OpenWebNetLightingGroupHandler genHandler = (OpenWebNetLightingGroupHandler) bridgeHandler
                            .getRegisteredDevice(genOwnId);
                    if (genHandler != null && this.ownId != null) {
                        logger.debug("device {} is Propagating msg {} to GEN handler", deviceWhere, lmsg);
                        genHandler.handlePropagatedMessage(lmsg, this.ownId);
                    }
                }
            }

            handleMessage(lmsg); // to make handler come online when a light of its group comes online
        }
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        logger.debug("handleMessage({}) for thing: {}", msg, thing.getUID());
        super.handleMessage(msg);

        WhereLightAutom w = (WhereLightAutom) deviceWhere;
        if (w != null && w.isGroup()) {
            if (((Lighting) msg).isOff()) {
                updateState(CHANNEL_SWITCH, OnOffType.OFF);
            } else {
                updateState(CHANNEL_SWITCH, OnOffType.ON);
            }
        }
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereLightAutom(wStr);
    }

    @Override
    public void dispose() {
        if (this.deviceWhere instanceof WhereLightAutom whereLightAutom) {
            int area = whereLightAutom.getArea();
            OpenWebNetBridgeHandler bridgeHandler = this.bridgeHandler;
            if (bridgeHandler != null) {
                bridgeHandler.removeLight(area, this);
            }
        }
        super.dispose();
    }
}
