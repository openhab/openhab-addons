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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
 * The {@link OpenWebNetLightingGroupHandler} is responsible for handling
 * commands/messages for a Lighting OpenWebNet group.
 * It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution.
 */
@NonNullByDefault
public class OpenWebNetLightingGroupHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetLightingGroupHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.LIGHTING_GROUP_SUPPORTED_THING_TYPES;

    protected Set<String> listOn = new HashSet<String>();

    private boolean isGeneral = false;

    public OpenWebNetLightingGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        WhereLightAutom w = (WhereLightAutom) deviceWhere;
        isGeneral = w.isGeneral();

        OpenWebNetBridgeHandler bridge = bridgeHandler;
        if (w != null && bridge != null) {
            int area = w.getArea();
            bridge.addLight(area, this);
        }
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        super.requestChannelState(channel);
        if (deviceWhere != null) {
            try {
                send(Lighting.requestStatus(deviceWhere.value()));
            } catch (OWNException e) {
                logger.debug("Exception while requesting state for channel {}: {} ", channel, e.getMessage());
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
            ThingTypeUID thingType = thing.getThingTypeUID();

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
        if (command instanceof OnOffType) {
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

    protected void handlePropagatedMessage(Lighting lmsg, String oId) {
        logger.debug("handlePropagatedMessage({}) for thing: {}", lmsg, thing.getUID());

        WhereLightAutom dw = (WhereLightAutom) deviceWhere;
        if (dw != null) {
            int sizeBefore = listOn.size();
            if (!lmsg.isOff()) {
                if (listOn.add(oId)) {
                    logger.debug("/////////// ADDED {} to listOn for {}", oId, deviceWhere);
                }
            } else {
                if (listOn.remove(oId)) {
                    logger.debug("/////////// REMOVED {} from listOn for {}", oId, dw);
                }
            }
            logger.debug("/////////// listOn for {}: {}", dw, listOn);

            boolean listOnChanged = false;

            if (listOn.size() > 0) {
                // some light still on
                logger.debug("/////////// some light ON... switching group {} to ON", dw);
                updateState(CHANNEL_SWITCH, OnOffType.ON);
                listOnChanged = (sizeBefore == 0);
            } else {
                // no light is ON anymore
                logger.debug("/////////// all lights OFF ... switching group {} to OFF ", dw);
                updateState(CHANNEL_SWITCH, OnOffType.OFF);
                listOnChanged = (sizeBefore > 0);
            }
            if (listOnChanged && !dw.isGeneral()) {
                // Area has changed state, propagate APL msg to GEN handler, if exists
                OpenWebNetBridgeHandler brH = this.bridgeHandler;
                String genOwnId = this.getManagedWho().value() + ".0";
                OpenWebNetLightingGroupHandler genHandler = (OpenWebNetLightingGroupHandler) brH
                        .getRegisteredDevice(genOwnId);
                if (genHandler != null) {
                    logger.debug("//////////////////// device {} is Propagating msg {} to GEN handler", dw, lmsg);
                    genHandler.handlePropagatedMessage(lmsg, this.ownId);

                }
            }

            handleMessage(lmsg); // to make handler come online when a light of its group comes online
        }
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        logger.debug("handleMessage({}) for thing: {}", msg, thing.getUID());
        super.handleMessage(msg);
    }

    /*
     * private String getAPLorAreaFromMessage(Lighting lmsg) {
     * String oId = null;
     * if (!isGeneral) {
     * oId = bridgeHandler.ownIdFromMessage(lmsg);
     * } else { // add A to GEN
     * WhereLightAutom wl = (WhereLightAutom) lmsg.getWhere();
     * int area = wl.getArea();
     * if (area > 0) {
     * oId = this.getManagedWho().value() + "." + area;
     * }
     * }
     * return oId;
     * }
     */

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereLightAutom(wStr);
    }

    @Override
    public void dispose() {
        // TODO
        Where w = deviceWhere;
        if (w != null) {
            int area = ((WhereLightAutom) w).getArea();
            OpenWebNetBridgeHandler brH = this.bridgeHandler;

            // remove light from lightsMap
            brH.removeLight(area, this);
        }
        super.dispose();
    }
}
