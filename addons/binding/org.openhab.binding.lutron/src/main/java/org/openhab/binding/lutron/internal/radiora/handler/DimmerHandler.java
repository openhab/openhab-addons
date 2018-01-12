/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.handler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.lutron.LutronBindingConstants;
import org.openhab.binding.lutron.internal.radiora.config.DimmerConfig;
import org.openhab.binding.lutron.internal.radiora.protocol.LocalZoneChangeFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.SetDimmerLevelCommand;
import org.openhab.binding.lutron.internal.radiora.protocol.SetSwitchLevelCommand;
import org.openhab.binding.lutron.internal.radiora.protocol.ZoneMapFeedback;

/**
 * Handler for RadioRA dimmers
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class DimmerHandler extends LutronHandler {

    /**
     * Used to internally keep track of dimmer level. This helps us better respond
     * to external dimmer changes since RadioRA protocol does not send dimmer
     * levels in their messages.
     */
    private AtomicInteger lastKnownIntensity = new AtomicInteger(100);

    private AtomicBoolean switchEnabled = new AtomicBoolean(false);

    public DimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        DimmerConfig config = getConfigAs(DimmerConfig.class);

        if (LutronBindingConstants.CHANNEL_LIGHTLEVEL.equals(channelUID.getId())) {
            if (command instanceof PercentType) {
                int intensity = ((PercentType) command).intValue();

                SetDimmerLevelCommand cmd = new SetDimmerLevelCommand(config.getZoneNumber(), intensity);
                getRS232Handler().sendCommand(cmd);

                updateInternalState(intensity);
            }

            if (command instanceof OnOffType) {
                OnOffType onOffCmd = (OnOffType) command;

                SetSwitchLevelCommand cmd = new SetSwitchLevelCommand(config.getZoneNumber(), onOffCmd);
                getRS232Handler().sendCommand(cmd);

                updateInternalState(onOffCmd);

            }
        }
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        if (LutronBindingConstants.CHANNEL_LIGHTLEVEL.equals(channelUID.getId())) {
            PercentType percent = (PercentType) newState.as(PercentType.class);
            updateInternalState(percent.intValue());
        }
    }

    @Override
    public void handleFeedback(RadioRAFeedback feedback) {
        if (feedback instanceof LocalZoneChangeFeedback) {
            handleLocalZoneChangeFeedback((LocalZoneChangeFeedback) feedback);
        } else if (feedback instanceof ZoneMapFeedback) {
            handleZoneMapFeedback((ZoneMapFeedback) feedback);
        }
    }

    private void handleZoneMapFeedback(ZoneMapFeedback feedback) {
        char value = feedback.getZoneValue(getConfigAs(DimmerConfig.class).getZoneNumber());
        if (value == '1') {
            turnDimmerOnToLastKnownIntensity();
        } else if (value == '0') {
            turnDimmerOff();
        }
    }

    private void handleLocalZoneChangeFeedback(LocalZoneChangeFeedback feedback) {
        if (feedback.getZoneNumber() == getConfigAs(DimmerConfig.class).getZoneNumber()) {
            if (LocalZoneChangeFeedback.State.ON.equals(feedback.getState())) {
                turnDimmerOnToLastKnownIntensity();
            } else if (LocalZoneChangeFeedback.State.OFF.equals(feedback.getState())) {
                turnDimmerOff();
            }

        }
    }

    private void turnDimmerOnToLastKnownIntensity() {
        if (!switchEnabled.get()) {
            updateState(LutronBindingConstants.CHANNEL_LIGHTLEVEL, new PercentType(lastKnownIntensity.get()));
        }
        switchEnabled.set(true);
    }

    private void turnDimmerOff() {
        updateState(LutronBindingConstants.CHANNEL_LIGHTLEVEL, PercentType.ZERO);
        switchEnabled.set(false);
    }

    private void updateInternalState(int intensity) {
        if (intensity > 0) {
            lastKnownIntensity.set(intensity);
        }
        switchEnabled.set(intensity > 0);
    }

    private void updateInternalState(OnOffType type) {
        switchEnabled.set(OnOffType.ON.equals(type));
    }
}
