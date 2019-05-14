/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.somfymylink.internal;

import static org.openhab.binding.somfymylink.internal.SomfyMyLinkBindingConstants.CHANNEL_SHADELEVEL;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyMyLinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Johnson - Initial contribution
 */
public class SomfyShadeHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyShadeHandler.class);

    @Nullable
    private SomfyMyLinkConfiguration config;

    public SomfyShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.config = getThing().getConfiguration().as(SomfyMyLinkConfiguration.class);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        try {
            if (CHANNEL_SHADELEVEL.equals(channelUID.getId())) {

                String targetId = channelUID.getThingUID().getId();

                if (command instanceof RefreshType) {
                    // TODO: handle data refresh
                    return;
                }
                if (CHANNEL_SHADELEVEL.equals(channelUID.getId()) && command instanceof UpDownType) {
                    if (command.equals(UpDownType.DOWN)) {
                        getBridgeHandler().commandShadeDown(targetId);
                    } else {
                        getBridgeHandler().commandShadeUp(targetId);
                    }
                }
                if (CHANNEL_SHADELEVEL.equals(channelUID.getId()) && command instanceof StopMoveType) {
                    getBridgeHandler().commandShadeStop(targetId);
                }
            }
        } catch (SomfyMyLinkException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected SomfyMyLinkHandler getBridgeHandler() {
        return this.getBridge() != null ? (SomfyMyLinkHandler) this.getBridge().getHandler() : null;
    }

}
