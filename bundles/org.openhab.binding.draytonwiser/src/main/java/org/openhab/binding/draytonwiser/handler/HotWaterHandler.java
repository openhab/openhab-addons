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
import org.eclipse.smarthome.core.library.types.DecimalType;
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

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_MANUAL_MODE_STATE)) {
            boolean manualMode = command.toString().toUpperCase().equals("ON");
            setManualMode(manualMode);
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_HOT_WATER_SETPOINT)) {
            boolean setPoint = command.toString().toUpperCase().equals("ON");
            setSetPoint(setPoint);
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_HOT_WATER_BOOST_DURATION)) {
            int boostDuration = Math.round((Float.parseFloat(command.toString()) * 60));
            setBoostDuration(boostDuration);
        }
    }

    @Override
    protected void refresh() {
        try {
            boolean updated = updateControllerData();
            if (updated) {
                updateStatus(ThingStatus.ONLINE);
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HOT_WATER_OVERRIDE),
                        getHotWaterOverride());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HOTWATER_DEMAND_STATE),
                        getHotWaterDemandState());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_MANUAL_MODE_STATE),
                        getManualModeState());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HOT_WATER_SETPOINT),
                        getSetPointState());
                updateState(new ChannelUID(getThing().getUID(),
                        DraytonWiserBindingConstants.CHANNEL_HOT_WATER_BOOST_DURATION), new DecimalType(0));
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HOT_WATER_BOOSTED),
                        getBoostedState());
                updateState(
                        new ChannelUID(getThing().getUID(),
                                DraytonWiserBindingConstants.CHANNEL_HOT_WATER_BOOST_REMAINING),
                        getBoostRemainingState());
            }

        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean updateControllerData() {
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

    private State getManualModeState() {
        if (hotWaterChannels != null && hotWaterChannels.size() >= 1) {
            if (hotWaterChannels.get(0).getMode().toUpperCase().equals("MANUAL")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private State getSetPointState() {
        if (hotWaterChannels != null && hotWaterChannels.size() >= 1) {
            if (hotWaterChannels.get(0).getWaterHeatingState().toUpperCase().equals("ON")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private void setManualMode(Boolean manualMode) {
        if (bridgeHandler != null) {
            bridgeHandler.setHotWaterManualMode(manualMode);
        }
    }

    private void setSetPoint(Boolean setPointMode) {
        if (bridgeHandler != null) {
            bridgeHandler.setHotWaterSetPoint(setPointMode ? 1100 : -200);
        }
    }

    private void setBoostDuration(Integer durationMinutes) {
        if (bridgeHandler != null) {
            if (durationMinutes > 0) {
                bridgeHandler.setHotWaterBoostActive(durationMinutes);
            } else {
                bridgeHandler.setHotWaterBoostInactive();
            }
        }
    }

    private State getBoostedState() {
        if (hotWaterChannels != null && hotWaterChannels.size() >= 1) {
            if (hotWaterChannels.get(0).getOverrideTimeoutUnixTime() != null
                    && !hotWaterChannels.get(0).getOverrideType().toUpperCase().equals("NONE")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private State getBoostRemainingState() {
        if (hotWaterChannels != null && hotWaterChannels.size() >= 1) {
            if (hotWaterChannels.get(0).getOverrideTimeoutUnixTime() != null
                    && !hotWaterChannels.get(0).getOverrideType().toUpperCase().equals("NONE")) {
                return new DecimalType(
                        ((System.currentTimeMillis() / 1000L) - hotWaterChannels.get(0).getOverrideTimeoutUnixTime())
                                / 60);
            }
        }

        return new DecimalType(0);
    }
}
