/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.handler;

import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.supla.SuplaBindingConstants.SWITCH_1_CHANNEL;
import static org.openhab.binding.supla.SuplaBindingConstants.SWITCH_2_CHANNEL;

/**
 * The {@link TwoChannelRelayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class TwoChannelRelayHandler extends OneChannelRelayHandler {
    private final Logger logger = LoggerFactory.getLogger(TwoChannelRelayHandler.class);

    public TwoChannelRelayHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(SWITCH_2_CHANNEL)) {
            executeCommandForSwitchChannel(channelUID, command);
        } else {
            super.handleCommand(channelUID, command);
        }
    }
}
