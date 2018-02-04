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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.internal.config.Device;
import org.openhab.binding.draytonwiser.internal.config.HeatingChannel;
import org.openhab.binding.draytonwiser.internal.config.HotWater;
import org.openhab.binding.draytonwiser.internal.config.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ControllerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class ControllerHandler extends DraytonWiserThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);

    @Nullable
    Device device;

    org.openhab.binding.draytonwiser.internal.config.@Nullable System system;

    @Nullable
    Station station;

    @Nullable
    List<HeatingChannel> heatingChannels;

    @Nullable
    List<HotWater> hotWaterChannels;

    public ControllerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
            return;
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_AWAY_MODE_STATE)) {
            boolean awayMode = command.toString().toUpperCase().equals("ON");
            setAwayMode(awayMode);
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_ECO_MODE_STATE)) {
            boolean ecoMode = command.toString().toUpperCase().equals("ON");
            setEcoMode(ecoMode);
        }
    }

    @Override
    protected void refresh() {
        try {
            boolean updated = updateControllerData();
            if (updated) {
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HEATING_OVERRIDE),
                        getHeatingOverride());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_SIGNAL_RSSI),
                        getRSSI());
                updateState(new ChannelUID(getThing().getUID(),
                        DraytonWiserBindingConstants.CHANNEL_CURRENT_SIGNAL_STRENGTH), getSignalStrength());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HEATCHANNEL_1_DEMAND),
                        getHeatChannel1Demand());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_HEATCHANNEL_2_DEMAND),
                        getHeatChannel2Demand());
                updateState(
                        new ChannelUID(getThing().getUID(),
                                DraytonWiserBindingConstants.CHANNEL_HEATCHANNEL_1_DEMAND_STATE),
                        getHeatChannel1DemandState());
                updateState(
                        new ChannelUID(getThing().getUID(),
                                DraytonWiserBindingConstants.CHANNEL_HEATCHANNEL_2_DEMAND_STATE),
                        getHeatChannel2DemandState());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_AWAY_MODE_STATE),
                        getAwayModeState());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_AWAY_MODE_SETPOINT),
                        getAwayModeSetPoint());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ECO_MODE_STATE),
                        getEcoModeState());
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
        station = bridgeHandler.getStation();
        heatingChannels = bridgeHandler.getHeatingChannels();
        hotWaterChannels = bridgeHandler.getHotWater();

        return device != null && system != null && station != null;
    }

    private State getHeatingOverride() {
        if (system != null) {
            if (system.getHeatingButtonOverrideState().toUpperCase().equals("ON")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private State getRSSI() {
        if (station != null) {
            return new DecimalType(station.getRSSI().getCurrent());
        }

        return UnDefType.UNDEF;
    }

    private State getSignalStrength() {
        if (device != null) {
            return new StringType(device.getDisplayedSignalStrength());
        }

        return UnDefType.UNDEF;
    }

    @SuppressWarnings("null")
    private State getHeatChannel1Demand() {
        if (heatingChannels != null && heatingChannels.size() >= 1) {
            return new DecimalType(heatingChannels.get(0).getPercentageDemand());
        }

        return UnDefType.UNDEF;
    }

    @SuppressWarnings("null")
    private State getHeatChannel2Demand() {
        if (heatingChannels != null && heatingChannels.size() >= 2) {
            return new DecimalType(heatingChannels.get(1).getPercentageDemand());
        }

        return UnDefType.UNDEF;
    }

    @SuppressWarnings("null")
    private State getHeatChannel1DemandState() {
        if (heatingChannels != null && heatingChannels.size() >= 1) {
            if (heatingChannels.get(0).getHeatingRelayState().toUpperCase().equals("ON")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    @SuppressWarnings("null")
    private State getHeatChannel2DemandState() {
        if (heatingChannels != null && heatingChannels.size() >= 2) {
            if (heatingChannels.get(1).getHeatingRelayState().toUpperCase().equals("ON")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private State getAwayModeState() {
        if (system != null && system.getOverrideType() != null) {
            if (system.getOverrideType().toUpperCase().equals("AWAY")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private State getAwayModeSetPoint() {
        if (system != null && system.getOverrideSetpoint() != null) {
            return new DecimalType((float) system.getOverrideSetpoint() / 10);
        }

        return UnDefType.UNDEF;
    }

    private State getEcoModeState() {
        if (system != null && system.getEcoModeEnabled() != null) {
            if (system.getEcoModeEnabled()) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private void setAwayMode(Boolean awayMode) {
        getBridgeHandler().setAwayMode(awayMode);
        updateControllerData();
    }

    private void setEcoMode(Boolean ecoMode) {
        getBridgeHandler().setEcoMode(ecoMode);
        updateControllerData();
    }
}
