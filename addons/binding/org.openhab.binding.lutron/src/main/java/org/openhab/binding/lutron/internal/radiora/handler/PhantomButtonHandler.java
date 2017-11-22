/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.LutronBindingConstants;
import org.openhab.binding.lutron.internal.radiora.config.PhantomButtonConfig;
import org.openhab.binding.lutron.internal.radiora.protocol.ButtonPressCommand;
import org.openhab.binding.lutron.internal.radiora.protocol.LEDMapFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;

/**
 * Handler for RadioRA Phantom buttons
 *
 * @author Jeff Lauterbach
 *
 */
public class PhantomButtonHandler extends LutronHandler {

    private PhantomButtonConfig config;

    public PhantomButtonHandler(Thing thing) {
        super(thing);
        this.config = getConfigAs(PhantomButtonConfig.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(LutronBindingConstants.CHANNEL_SWITCH)) {
            if (command instanceof OnOffType) {

                ButtonPressCommand cmd = new ButtonPressCommand(config.getButtonNumber(),
                        ButtonPressCommand.ButtonState.valueOf(command.toString()));
                getChronosHandler().sendCommand(cmd);
            }
        }
    }

    @Override
    public void handleFeedback(RadioRAFeedback feedback) {
        if (feedback instanceof LEDMapFeedback) {
            handleLEDMapFeedback((LEDMapFeedback) feedback);
        }

    }

    private void handleLEDMapFeedback(LEDMapFeedback feedback) {

        if (feedback.getZoneValue(config.getButtonNumber()) == '1') {
            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.ON);
        } else {
            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.OFF);
        }
    }

}
