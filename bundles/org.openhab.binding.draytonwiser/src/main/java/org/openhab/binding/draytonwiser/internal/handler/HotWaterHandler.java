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
package org.openhab.binding.draytonwiser.internal.handler;

import static org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.*;

import java.util.List;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApiException;
import org.openhab.binding.draytonwiser.internal.handler.HotWaterHandler.HotWaterData;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;
import org.openhab.binding.draytonwiser.internal.model.HotWaterDTO;
import org.openhab.binding.draytonwiser.internal.model.SystemDTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link HotWaterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 * @author Hilbrand Bouwkamp - Simplified handler to handle null data
 */
@NonNullByDefault
public class HotWaterHandler extends DraytonWiserThingHandler<HotWaterData> {

    public HotWaterHandler(final Thing thing) {
        super(thing);
    }

    @Override
    protected void handleCommand(final String channelId, final Command command) throws DraytonWiserApiException {
        if (command instanceof OnOffType && CHANNEL_MANUAL_MODE_STATE.equals(channelId)) {
            setManualMode(OnOffType.ON.equals(command));
        } else if (command instanceof OnOffType && CHANNEL_HOT_WATER_SETPOINT.equals(channelId)) {
            setSetPoint(OnOffType.ON.equals(command));
        } else if (command instanceof DecimalType && CHANNEL_HOT_WATER_BOOST_DURATION.equals(channelId)) {
            setBoostDuration(Math.round((Float.parseFloat(command.toString()) * 60)));
        }
    }

    @Override
    protected void refresh() {
        updateState(CHANNEL_HOT_WATER_OVERRIDE, this::getHotWaterOverride);
        updateState(CHANNEL_HOTWATER_DEMAND_STATE, this::getHotWaterDemandState);
        updateState(CHANNEL_MANUAL_MODE_STATE, this::getManualModeState);
        updateState(CHANNEL_HOT_WATER_SETPOINT, this::getSetPointState);
        updateState(CHANNEL_HOT_WATER_BOOSTED, this::getBoostedState);
        updateState(CHANNEL_HOT_WATER_BOOST_REMAINING, this::getBoostRemainingState);
    }

    @Override
    protected @Nullable HotWaterData collectData(final DraytonWiserDTO domainDTOProxy) {
        final SystemDTO system = domainDTOProxy.getSystem();
        final List<HotWaterDTO> hotWater = domainDTOProxy.getHotWater();

        return system == null ? null : new HotWaterData(system, hotWater);
    }

    private State getHotWaterOverride() {
        return OnOffType.from("ON".equalsIgnoreCase(getData().system.getHotWaterButtonOverrideState()));
    }

    private State getHotWaterDemandState() {
        final List<HotWaterDTO> hotWater = getData().hotWater;
        return OnOffType.from(!hotWater.isEmpty() && "ON".equalsIgnoreCase(hotWater.get(0).getHotWaterRelayState()));
    }

    private State getManualModeState() {
        final List<HotWaterDTO> hotWater = getData().hotWater;
        return OnOffType.from(!hotWater.isEmpty() && "MANUAL".equalsIgnoreCase(hotWater.get(0).getMode()));
    }

    private State getSetPointState() {
        final List<HotWaterDTO> hotWater = getData().hotWater;
        return OnOffType.from(!hotWater.isEmpty() && "ON".equalsIgnoreCase(hotWater.get(0).getWaterHeatingState()));
    }

    private void setManualMode(final boolean manualMode) throws DraytonWiserApiException {
        getApi().setHotWaterManualMode(manualMode);
    }

    private void setSetPoint(final boolean setPointMode) throws DraytonWiserApiException {
        getApi().setHotWaterSetPoint(setPointMode ? 1100 : -200);
    }

    private void setBoostDuration(final int durationMinutes) throws DraytonWiserApiException {
        if (durationMinutes > 0) {
            getApi().setHotWaterBoostActive(durationMinutes);
        } else {
            getApi().setHotWaterBoostInactive();
        }
    }

    private State getBoostedState() {
        if (getData().hotWater.size() >= 1) {
            final HotWaterDTO firstChannel = getData().hotWater.get(0);

            if (firstChannel.getOverrideTimeoutUnixTime() != null
                    && !"NONE".equalsIgnoreCase(firstChannel.getOverrideType())) {
                return OnOffType.ON;
            }
        }

        updateState(CHANNEL_HOT_WATER_BOOST_DURATION, DecimalType.ZERO);

        return OnOffType.OFF;
    }

    private State getBoostRemainingState() {
        if (getData().hotWater.size() >= 1) {
            final HotWaterDTO firstChannel = getData().hotWater.get(0);
            final Integer overrideTimeout = firstChannel.getOverrideTimeoutUnixTime();

            if (overrideTimeout != null && !"NONE".equalsIgnoreCase(firstChannel.getOverrideType())) {
                return new QuantityType<Time>(overrideTimeout - (System.currentTimeMillis() / 1000L), Units.SECOND);
            }
        }
        return new QuantityType<Time>(0, Units.SECOND);
    }

    static class HotWaterData {
        public final SystemDTO system;
        public final List<HotWaterDTO> hotWater;

        public HotWaterData(final SystemDTO system, final List<HotWaterDTO> hotWater) {
            this.system = system;
            this.hotWater = hotWater;
        }
    }
}
