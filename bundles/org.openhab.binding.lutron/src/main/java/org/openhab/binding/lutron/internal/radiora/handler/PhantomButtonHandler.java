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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.LutronBindingConstants;
import org.openhab.binding.lutron.internal.radiora.config.PhantomButtonConfig;
import org.openhab.binding.lutron.internal.radiora.protocol.ButtonPressCommand;
import org.openhab.binding.lutron.internal.radiora.protocol.LEDMapFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for RadioRA Phantom buttons
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class PhantomButtonHandler extends LutronHandler {

    private @NonNullByDefault({}) PhantomButtonConfig config;

    public PhantomButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(PhantomButtonConfig.class);
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        RS232Handler bridgeHandler = getRS232Handler();
        if (channelUID.getId().equals(LutronBindingConstants.CHANNEL_SWITCH)) {
            if (command instanceof OnOffType) {
                ButtonPressCommand cmd = new ButtonPressCommand(config.getButtonNumber(),
                        ButtonPressCommand.ButtonState.valueOf(command.toString()), config.system);
                if (bridgeHandler != null) {
                    bridgeHandler.sendCommand(cmd);
                }
            }
        }
    }

    @Override
    public void handleFeedback(RadioRAFeedback feedback) {
        if (feedback instanceof LEDMapFeedback ledMapFeedback) {
            handleLEDMapFeedback(ledMapFeedback);
        }
    }

    private void handleLEDMapFeedback(LEDMapFeedback feedback) {
        boolean zoneEnabled = feedback.getZoneValue(getConfigAs(PhantomButtonConfig.class).getButtonNumber()) == '1';

        updateState(LutronBindingConstants.CHANNEL_SWITCH, zoneEnabled ? OnOffType.ON : OnOffType.OFF);
    }
}
