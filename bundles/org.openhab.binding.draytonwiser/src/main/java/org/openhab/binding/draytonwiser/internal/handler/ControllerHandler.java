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
package org.openhab.binding.draytonwiser.internal.handler;

import static org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.SignalStrength;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApiException;
import org.openhab.binding.draytonwiser.internal.handler.ControllerHandler.ControllerData;
import org.openhab.binding.draytonwiser.internal.model.DeviceDTO;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;
import org.openhab.binding.draytonwiser.internal.model.HeatingChannelDTO;
import org.openhab.binding.draytonwiser.internal.model.HotWaterDTO;
import org.openhab.binding.draytonwiser.internal.model.StationDTO;
import org.openhab.binding.draytonwiser.internal.model.SystemDTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link ControllerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 * @author Hilbrand Bouwkamp - Simplified handler to handle null data
 */
@NonNullByDefault
public class ControllerHandler extends DraytonWiserThingHandler<ControllerData> {

    public ControllerHandler(final Thing thing) {
        super(thing);
    }

    @Override
    protected void handleCommand(final String channelId, final Command command) throws DraytonWiserApiException {
        if (command instanceof OnOffType) {
            final boolean onOffState = OnOffType.ON.equals(command);

            if (CHANNEL_AWAY_MODE_STATE.equals(channelId)) {
                setAwayMode(onOffState);
            } else if (CHANNEL_ECO_MODE_STATE.equals(channelId)) {
                setEcoMode(onOffState);
            } else if (CHANNEL_COMFORT_MODE_STATE.equals(channelId)) {
                setComfortMode(onOffState);
            }
        }
    }

    @Override
    protected void refresh() {
        updateState(CHANNEL_HEATING_OVERRIDE, this::getHeatingOverride);
        updateState(CHANNEL_CURRENT_SIGNAL_RSSI, this::getRSSI);
        updateState(CHANNEL_CURRENT_WISER_SIGNAL_STRENGTH, this::getWiserSignalStrength);
        updateState(CHANNEL_CURRENT_SIGNAL_STRENGTH, this::getSignalStrength);
        updateState(CHANNEL_HEATCHANNEL_1_DEMAND, this::getHeatChannel1Demand);
        updateState(CHANNEL_HEATCHANNEL_2_DEMAND, this::getHeatChannel2Demand);
        updateState(CHANNEL_HEATCHANNEL_1_DEMAND_STATE, this::getHeatChannel1DemandState);
        updateState(CHANNEL_HEATCHANNEL_2_DEMAND_STATE, this::getHeatChannel2DemandState);
        updateState(CHANNEL_AWAY_MODE_STATE, this::getAwayModeState);
        updateState(CHANNEL_ECO_MODE_STATE, this::getEcoModeState);
        updateState(CHANNEL_COMFORT_MODE_STATE, this::getComfortModeState);
    }

    @Override
    protected @Nullable ControllerData collectData(final DraytonWiserDTO domainDTOProxy)
            throws DraytonWiserApiException {
        final StationDTO station = getApi().getStation();
        final DeviceDTO device = domainDTOProxy.getExtendedDeviceProperties(0);
        final SystemDTO system = domainDTOProxy.getSystem();
        final List<HeatingChannelDTO> heatingChannels = domainDTOProxy.getHeatingChannels();
        final List<HotWaterDTO> hotWaterChannels = domainDTOProxy.getHotWater();

        return station != null && device != null && system != null
                ? new ControllerData(device, system, station, heatingChannels, hotWaterChannels)
                : null;
    }

    private State getHeatingOverride() {
        return OnOffType.from("ON".equalsIgnoreCase(getData().system.getHeatingButtonOverrideState()));
    }

    private State getRSSI() {
        return new DecimalType(getData().station.getRSSI().getCurrent());
    }

    private State getWiserSignalStrength() {
        return new StringType(getData().device.getDisplayedSignalStrength());
    }

    private State getSignalStrength() {
        return SignalStrength.toSignalStrength(getData().device.getDisplayedSignalStrength());
    }

    private State getHeatChannel1Demand() {
        return !getData().heatingChannels.isEmpty()
                ? new QuantityType<>(getData().heatingChannels.get(0).getPercentageDemand(), Units.PERCENT)
                : UnDefType.UNDEF;
    }

    private State getHeatChannel2Demand() {
        return getData().heatingChannels.size() >= 2
                ? new QuantityType<>(getData().heatingChannels.get(1).getPercentageDemand(), Units.PERCENT)
                : UnDefType.UNDEF;
    }

    private State getHeatChannel1DemandState() {
        return OnOffType.from(!getData().heatingChannels.isEmpty()
                && "ON".equalsIgnoreCase(getData().heatingChannels.get(0).getHeatingRelayState()));
    }

    private State getHeatChannel2DemandState() {
        return OnOffType.from(getData().heatingChannels.size() >= 2
                && "ON".equalsIgnoreCase(getData().heatingChannels.get(1).getHeatingRelayState()));
    }

    private State getAwayModeState() {
        return OnOffType.from(getData().system.getOverrideType() != null
                && "AWAY".equalsIgnoreCase(getData().system.getOverrideType()));
    }

    private State getEcoModeState() {
        return OnOffType.from(getData().system.getEcoModeEnabled() != null && getData().system.getEcoModeEnabled());
    }

    private State getComfortModeState() {
        return OnOffType
                .from(getData().system.getComfortModeEnabled() != null && getData().system.getComfortModeEnabled());
    }

    private void setAwayMode(final Boolean awayMode) throws DraytonWiserApiException {
        getApi().setAwayMode(awayMode);
    }

    private void setEcoMode(final Boolean ecoMode) throws DraytonWiserApiException {
        getApi().setEcoMode(ecoMode);
    }

    private void setComfortMode(final Boolean comfortMode) throws DraytonWiserApiException {
        getApi().setComfortMode(comfortMode);
    }

    static class ControllerData {
        public final DeviceDTO device;
        public final SystemDTO system;
        public final StationDTO station;
        public final List<HeatingChannelDTO> heatingChannels;
        public final List<HotWaterDTO> hotWaterChannels;

        public ControllerData(final DeviceDTO device, final SystemDTO system, final StationDTO station,
                final List<HeatingChannelDTO> heatingChannels, final List<HotWaterDTO> hotWaterChannels) {
            this.device = device;
            this.system = system;
            this.station = station;
            this.heatingChannels = heatingChannels;
            this.hotWaterChannels = hotWaterChannels;
        }
    }
}
