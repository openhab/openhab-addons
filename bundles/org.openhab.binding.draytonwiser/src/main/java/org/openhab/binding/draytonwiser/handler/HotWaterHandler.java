/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.internal.config.Device;
import org.openhab.binding.draytonwiser.internal.config.HotWater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HotWaterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class HotWaterHandler extends DraytonWiserThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HotWaterHandler.class);

    @Nullable
    Device device;

    org.openhab.binding.draytonwiser.internal.config.@Nullable System system;

    @Nullable
    List<HotWater> hotWaterChannels;

    public HotWaterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
            return;
        }
    }

    @Override
    protected void refresh() {
        try {
            boolean updated = updateControllerData();
            if (updated) {
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HOT_WATER_OVERRIDE),
                        getHotWaterOverride());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HOTWATER_DEMAND_STATE),
                        getHotWaterDemandState());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }

        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean updateControllerData() {
        HeatHubHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            return false;
        }

        device = bridgeHandler.getExtendedDeviceProperties(0);
        system = bridgeHandler.getSystem();
        hotWaterChannels = bridgeHandler.getHotWater();

        return device != null && system != null;
    }

    private State getHotWaterOverride() {
        if (system != null) {
            if (system.getHotWaterButtonOverrideState().toUpperCase().equals("ON")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    @SuppressWarnings("null")
    private State getHotWaterDemandState() {
        if (hotWaterChannels != null && hotWaterChannels.size() >= 1) {
            if (hotWaterChannels.get(0).getHotWaterRelayState().toUpperCase().equals("ON")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }
}
