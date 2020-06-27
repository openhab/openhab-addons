/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApi;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;
import org.openhab.binding.draytonwiser.internal.model.RoomDTO;
import org.openhab.binding.draytonwiser.internal.model.RoomStatDTO;

/**
 * The {@link RoomHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 * @author Hilbrand Bouwkamp - Simplified handler to handle null data
 */
@NonNullByDefault
public class RoomHandler extends DraytonWiserThingHandler<RoomDTO> {

    private String name = "";

    public RoomHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        name = (String) getConfig().get("name");
    }

    @Override
    protected void handleCommand(final String channelId, final Command command) {
        switch (channelId) {
            case CHANNEL_CURRENT_SETPOINT:
                if (command instanceof QuantityType) {
                    setSetPoint((QuantityType<Temperature>) command);
                }
                break;
            case CHANNEL_MANUAL_MODE_STATE:
                if (command instanceof OnOffType) {
                    setManualMode(OnOffType.ON.equals(command));
                }
                break;
            case CHANNEL_ROOM_BOOST_DURATION:
                if (command instanceof DecimalType) {
                    setBoostDuration(Math.round((((DecimalType) command).floatValue() * 60)));
                }
                break;
            case CHANNEL_ROOM_WINDOW_STATE_DETECTION:
                if (command instanceof OnOffType) {
                    setWindowStateDetection(OnOffType.ON.equals(command));
                }
                break;
        }
    }

    @Override
    protected void refresh() {
        updateState(CHANNEL_CURRENT_TEMPERATURE, this::getTemperature);
        updateState(CHANNEL_CURRENT_HUMIDITY, this::getHumidity);
        updateState(CHANNEL_CURRENT_SETPOINT, this::getSetPoint);
        updateState(CHANNEL_CURRENT_DEMAND, this::getDemand);
        updateState(CHANNEL_HEAT_REQUEST, this::getHeatRequest);
        updateState(CHANNEL_MANUAL_MODE_STATE, this::getManualModeState);
        updateState(CHANNEL_ROOM_BOOSTED, this::getBoostedState);
        updateState(CHANNEL_ROOM_BOOST_REMAINING, this::getBoostRemainingState);
        updateState(CHANNEL_ROOM_WINDOW_STATE_DETECTION, this::getWindowDetectionState);
        updateState(CHANNEL_ROOM_WINDOW_STATE, this::getWindowState);
    }

    @Override
    protected @Nullable RoomDTO collectData(final DraytonWiserDTO domainDTOProxy) {
        return domainDTOProxy.getRoomByName(name);
    }

    private State getSetPoint() {
        return new QuantityType<>(getData().getCurrentSetPoint() / 10.0, SIUnits.CELSIUS);
    }

    private void setSetPoint(final QuantityType<Temperature> command) {
        if (getData().getId() != null) {
            final QuantityType<Temperature> value = command.toUnit(SIUnits.CELSIUS);

            if (value != null) {
                final int newSetPoint = (int) Math.round(value.doubleValue() * 10);

                getApi().setRoomSetPoint(getData().getId(), newSetPoint);
            }
        }
    }

    private State getHumidity() {
        if (getData().getId() != null && getData().getRoomStatId() != null) {
            final RoomStatDTO roomStat = getDraytonWiseDTO().getRoomStat(getData().getRoomStatId());

            if (roomStat != null) {
                final Integer humidity = roomStat.getMeasuredHumidity();

                return humidity == null ? UnDefType.UNDEF : new QuantityType<>(humidity, SmartHomeUnits.PERCENT);
            }
        }
        return UnDefType.UNDEF;
    }

    private State getTemperature() {
        final int fullScaleTemp = getData().getCalculatedTemperature();

        return OFFLINE_TEMPERATURE == fullScaleTemp ? UnDefType.UNDEF
                : new QuantityType<>(fullScaleTemp / 10.0, SIUnits.CELSIUS);
    }

    private State getDemand() {
        return new QuantityType<>(getData().getPercentageDemand(), SmartHomeUnits.PERCENT);
    }

    private State getHeatRequest() {
        return OnOffType.from(getData().getControlOutputState());
    }

    private State getManualModeState() {
        return OnOffType.from("MANUAL".equalsIgnoreCase(getData().getMode()));
    }

    private void setManualMode(final boolean manualMode) {
        getApi().setRoomManualMode(getData().getId(), manualMode);
    }

    private void setWindowStateDetection(final boolean stateDetection) {
        getApi().setRoomWindowStateDetection(getData().getId(), stateDetection);
    }

    private State getBoostedState() {
        if (getData().getOverrideTimeoutUnixTime() != null && !"NONE".equalsIgnoreCase(getData().getOverrideType())) {
            return OnOffType.ON;
        }
        updateState(CHANNEL_ROOM_BOOST_DURATION, DecimalType.ZERO);
        return OnOffType.OFF;
    }

    private State getBoostRemainingState() {
        if (getData().getOverrideTimeoutUnixTime() != null
                && !"NONE".equalsIgnoreCase(getData().getOverrideType())) {
            return new DecimalType(
                    (getData().getOverrideTimeoutUnixTime() - (System.currentTimeMillis() / 1000L)) / 60);
        }
        return DecimalType.ZERO;
    }

    private void setBoostDuration(final int durationMinutes) {
        if (durationMinutes > 0) {
            getApi().setRoomBoostActive(getData().getId(), getData().getCalculatedTemperature() + 20, durationMinutes);
        } else {
            getApi().setRoomBoostInactive(getData().getId());
        }
    }

    private State getWindowDetectionState() {
        return OnOffType.from(getData().getWindowDetectionActive());
    }

    private State getWindowState() {
        if (getData().getWindowState() != null && "OPEN".equalsIgnoreCase(getData().getWindowState())) {
            return OpenClosedType.OPEN;
        }
        return OpenClosedType.CLOSED;
    }
}
