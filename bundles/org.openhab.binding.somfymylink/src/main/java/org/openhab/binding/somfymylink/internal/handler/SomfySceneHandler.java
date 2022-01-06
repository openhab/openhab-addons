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
package org.openhab.binding.somfymylink.internal.handler;

import static org.openhab.binding.somfymylink.internal.SomfyMyLinkBindingConstants.CHANNEL_SCENECONTROL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfySceneHandler} is responsible for handling commands for scenes
 *
 * @author Chris Johnson - Initial contribution
 */
@NonNullByDefault
public class SomfySceneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfySceneHandler.class);

    public SomfySceneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (CHANNEL_SCENECONTROL.equals(channelUID.getId()) && command instanceof OnOffType) {
                Integer targetId = Integer.decode(channelUID.getThingUID().getId());

                if (command.equals(OnOffType.ON)) {
                    getBridgeHandler().commandScene(targetId);
                    updateState(channelUID, OnOffType.OFF);
                }
            }
        } catch (SomfyMyLinkException e) {
            logger.warn("Error handling command: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected SomfyMyLinkBridgeHandler getBridgeHandler() {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            throw new SomfyMyLinkException("No bridge was found");
        }

        BridgeHandler handler = bridge.getHandler();
        if (handler == null) {
            throw new SomfyMyLinkException("No handler was found");
        }

        return (SomfyMyLinkBridgeHandler) handler;
    }
}
