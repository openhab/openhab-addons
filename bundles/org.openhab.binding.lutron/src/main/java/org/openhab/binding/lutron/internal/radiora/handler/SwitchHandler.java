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
import org.openhab.binding.lutron.internal.radiora.config.SwitchConfig;
import org.openhab.binding.lutron.internal.radiora.protocol.LocalZoneChangeFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.openhab.binding.lutron.internal.radiora.protocol.SetSwitchLevelCommand;
import org.openhab.binding.lutron.internal.radiora.protocol.ZoneMapFeedback;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for RadioRA switches
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class SwitchHandler extends LutronHandler {

    private final Logger logger = LoggerFactory.getLogger(SwitchHandler.class);
    private @NonNullByDefault({}) SwitchConfig config;

    public SwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(SwitchConfig.class);
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        RS232Handler bridgeHandler = getRS232Handler();
        if (LutronBindingConstants.CHANNEL_SWITCH.equals(channelUID.getId())) {
            if (command instanceof OnOffType onOffCommand) {
                SetSwitchLevelCommand cmd = new SetSwitchLevelCommand(config.getZoneNumber(), onOffCommand,
                        config.system);

                if (bridgeHandler != null) {
                    bridgeHandler.sendCommand(cmd);
                }
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
            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.ON);
        } else if (value == '0') {
            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.OFF);
        }
    }

    private void handleLocalZoneChangeFeedback(LocalZoneChangeFeedback feedback) {
        if (systemsMatch(feedback.getSystem(), config.system) && feedback.getZoneNumber() == config.getZoneNumber()) {
            if (LocalZoneChangeFeedback.State.CHG.equals(feedback.getState())) {
                logger.debug("Not Implemented Yet - CHG state received from Local Zone Change Feedback.");
            }

            updateState(LutronBindingConstants.CHANNEL_SWITCH, OnOffType.valueOf(feedback.getState().toString()));
        }
    }
}
