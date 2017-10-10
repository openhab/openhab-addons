/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for RadioRA dimmers
 *
 * @author Jeff Lauterbach
 *
 */
public class DimmerHandler extends LutronHandler {

    private Logger logger = LoggerFactory.getLogger(DimmerHandler.class);

    private DimmerConfig config;

    /**
     * Used to internally keep track of dimmer level. This helps us better respond
     * to external dimmer changes since RadioRA protocol does not send dimmer
     * levels in their messages.
     */
    private AtomicInteger lastKnownIntensity = new AtomicInteger(100);

    private AtomicBoolean switchEnabled = new AtomicBoolean(false);

    public DimmerHandler(Thing thing) {
        super(thing);
        this.config = getConfigAs(DimmerConfig.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(LutronBindingConstants.CHANNEL_LIGHTLEVEL)) {
            if (command instanceof PercentType) {
                int intensity = ((PercentType) command).intValue();

                SetDimmerLevelCommand cmd = new SetDimmerLevelCommand(config.getZoneNumber(), intensity);
                getChronosHandler().sendCommand(cmd);

                updateInternalState(intensity);
            }

            if (command instanceof OnOffType) {
                OnOffType onOffCmd = (OnOffType) command;

                SetSwitchLevelCommand cmd = new SetSwitchLevelCommand(config.getZoneNumber(), onOffCmd);
                getChronosHandler().sendCommand(cmd);

                updateInternalState(onOffCmd);

            }
        }
    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        if (channelUID.getId().equals(LutronBindingConstants.CHANNEL_LIGHTLEVEL)) {
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
        char value = feedback.getZoneValue(config.getZoneNumber());
        if (value == '1') {
            turnDimmerOnToLastKnownIntensity();
        } else if (value == '0') {
            turnDimmerOff();
        }
    }

    private void handleLocalZoneChangeFeedback(LocalZoneChangeFeedback feedback) {
        if (feedback.getZoneNumber() == config.getZoneNumber()) {
            if (feedback.getState().equals(LocalZoneChangeFeedback.State.CHG)) {
                logger.debug("Not Implemented Yet - CHG state received from Local Zone Change Feedback.");
            }

            if (feedback.getState().equals(LocalZoneChangeFeedback.State.ON)) {
                turnDimmerOnToLastKnownIntensity();
            } else if (feedback.getState().equals(LocalZoneChangeFeedback.State.OFF)) {
                turnDimmerOff();
            }

        }
    }

    private void turnDimmerOnToLastKnownIntensity() {
        if (switchEnabled.get() == false) {
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
        switchEnabled.set(type.equals(OnOffType.ON));
    }
}
