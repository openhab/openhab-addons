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
package org.openhab.binding.wlanthermo.internal.api.mini;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.TRIGGER_NONE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wlanthermo.internal.WlanThermoException;
import org.openhab.binding.wlanthermo.internal.WlanThermoHandler;
import org.openhab.binding.wlanthermo.internal.WlanThermoInputException;
import org.openhab.binding.wlanthermo.internal.WlanThermoUnknownChannelException;
import org.openhab.binding.wlanthermo.internal.api.mini.dto.builtin.App;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link WlanThermoMiniHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoMiniHandler extends WlanThermoHandler {

    private App app = new App();

    public WlanThermoMiniHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient, false);
    }

    @Override
    protected State getState(ChannelUID channelUID) throws WlanThermoInputException, WlanThermoUnknownChannelException {
        return WlanThermoMiniCommandHandler.getState(channelUID, app);
    }

    @Override
    protected boolean setState(ChannelUID channelUID, Command command) {
        // Mini is read-only!
        return false;
    }

    @Override
    protected void push() {
        // Unused, Mini is read-only!
    }

    @Override
    protected void pull() {
        try {
            // Update objects with data from device
            app = doGet("/app.php", App.class);

            // Update channels
            for (Channel channel : thing.getChannels()) {
                try {
                    State state = WlanThermoMiniCommandHandler.getState(channel.getUID(), app);
                    updateState(channel.getUID(), state);
                } catch (WlanThermoUnknownChannelException e) {
                    // if we could not obtain a state, try trigger instead
                    try {
                        String trigger = WlanThermoMiniCommandHandler.getTrigger(channel.getUID(), app);
                        if (!trigger.equals(TRIGGER_NONE)) {
                            triggerChannel(channel.getUID(), trigger);
                        }
                    } catch (WlanThermoUnknownChannelException e1) {
                        logger.debug("{}", e.getMessage());
                    }
                }
            }
        } catch (WlanThermoException ignore) {
            // Nothing more to do
        } catch (InterruptedException e) {
            logger.debug("Update interrupted. {}", e.getMessage());
        }
    }
}
