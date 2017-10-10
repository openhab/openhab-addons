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
import org.openhab.binding.lutron.internal.radiora.config.SwitchConfig;
import org.openhab.binding.lutron.internal.radiora.protocol.LocalZoneChangeFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.SetSwitchLevelCommand;
import org.openhab.binding.lutron.internal.radiora.protocol.ZoneMapFeedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for RadioRA switches
 *
 * @author Jeff Lauterbach
 *
 */
public class SwitchHandler extends LutronHandler {

    private Logger logger = LoggerFactory.getLogger(SwitchHandler.class);

    private SwitchConfig config;

    public SwitchHandler(Thing thing) {
        super(thing);
        this.config = getConfigAs(SwitchConfig.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(LutronBindingConstants.CHANNEL_SWITCH)) {
            if (command instanceof OnOffType) {

                SetSwitchLevelCommand cmd = new SetSwitchLevelCommand(config.getZoneNumber(), (OnOffType) command);
                getChronosHandler().sendCommand(cmd);
            }
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
            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.ON);
        } else if (value == '0') {
            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.OFF);
        }
    }

    private void handleLocalZoneChangeFeedback(LocalZoneChangeFeedback feedback) {
        if (feedback.getZoneNumber() == config.getZoneNumber()) {
            if (feedback.getState().equals(LocalZoneChangeFeedback.State.CHG)) {
                logger.debug("Not Implemented Yet - CHG state received from Local Zone Change Feedback.");
            }

            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.valueOf(feedback.getState().toString()));
        }
    }

}
