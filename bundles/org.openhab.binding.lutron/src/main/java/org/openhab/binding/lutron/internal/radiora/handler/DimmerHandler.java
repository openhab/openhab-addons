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
package org.openhab.binding.lutron.internal.radiora.handler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.LutronBindingConstants;
import org.openhab.binding.lutron.internal.radiora.config.DimmerConfig;
import org.openhab.binding.lutron.internal.radiora.protocol.LocalZoneChangeFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.SetDimmerLevelCommand;
import org.openhab.binding.lutron.internal.radiora.protocol.SetSwitchLevelCommand;
import org.openhab.binding.lutron.internal.radiora.protocol.ZoneMapFeedback;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for RadioRA dimmers
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class DimmerHandler extends LutronHandler {

    /**
     * Used to internally keep track of dimmer level. This helps us better respond
     * to external dimmer changes since RadioRA protocol does not send dimmer
     * levels in their messages.
     */
    private @NonNullByDefault({}) DimmerConfig config;
    private AtomicInteger lastKnownIntensity = new AtomicInteger(100);
    private AtomicBoolean switchEnabled = new AtomicBoolean(false);

    public DimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(DimmerConfig.class);
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        RS232Handler bridgeHandler = getRS232Handler();
        if (bridgeHandler == null) {
            return;
        }

        if (LutronBindingConstants.CHANNEL_LIGHTLEVEL.equals(channelUID.getId())) {
            if (command instanceof PercentType percentCommand) {
                int intensity = percentCommand.intValue();

                SetDimmerLevelCommand cmd = new SetDimmerLevelCommand(config.getZoneNumber(), intensity, config.system);
                bridgeHandler.sendCommand(cmd);

                updateInternalState(intensity);
            }

            if (command instanceof OnOffType onOffCmd) {
                SetSwitchLevelCommand cmd = new SetSwitchLevelCommand(config.getZoneNumber(), onOffCmd, config.system);
                bridgeHandler.sendCommand(cmd);

                updateInternalState(onOffCmd);
            }
        }
    }

    @Override
    public void handleFeedback(RadioRAFeedback feedback) {
        if (feedback instanceof LocalZoneChangeFeedback localZoneChangeFeedback) {
            handleLocalZoneChangeFeedback(localZoneChangeFeedback);
        } else if (feedback instanceof ZoneMapFeedback zoneMapFeedback) {
            handleZoneMapFeedback(zoneMapFeedback);
        }
    }

    private void handleZoneMapFeedback(ZoneMapFeedback feedback) {
        if (!systemsMatch(feedback.getSystem(), config.system)) {
            return;
        }
        char value = feedback.getZoneValue(config.getZoneNumber());
        if (value == '1') {
            turnDimmerOnToLastKnownIntensity();
        } else if (value == '0') {
            turnDimmerOff();
        }
    }

    private void handleLocalZoneChangeFeedback(LocalZoneChangeFeedback feedback) {
        if (systemsMatch(feedback.getSystem(), config.system) && feedback.getZoneNumber() == config.getZoneNumber()) {
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
