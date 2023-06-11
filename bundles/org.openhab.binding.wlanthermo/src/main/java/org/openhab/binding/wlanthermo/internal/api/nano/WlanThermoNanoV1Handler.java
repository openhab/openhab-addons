/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.wlanthermo.internal.api.nano;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.TRIGGER_NONE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wlanthermo.internal.WlanThermoException;
import org.openhab.binding.wlanthermo.internal.WlanThermoHandler;
import org.openhab.binding.wlanthermo.internal.WlanThermoInputException;
import org.openhab.binding.wlanthermo.internal.WlanThermoUnknownChannelException;
import org.openhab.binding.wlanthermo.internal.api.nano.dto.data.Data;
import org.openhab.binding.wlanthermo.internal.api.nano.dto.settings.Settings;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link WlanThermoNanoV1Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoNanoV1Handler extends WlanThermoHandler {

    private Data data = new Data();
    private Settings settings = new Settings();

    public WlanThermoNanoV1Handler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient, true);
    }

    @Override
    protected State getState(ChannelUID channelUID) throws WlanThermoInputException, WlanThermoUnknownChannelException {
        return WlanThermoNanoV1CommandHandler.getState(channelUID, data, settings);
    }

    @Override
    protected boolean setState(ChannelUID channelUID, Command command) {
        return WlanThermoNanoV1CommandHandler.setState(channelUID, command, data);
    }

    @Override
    protected void push() {
        // push update for sensor channels
        for (org.openhab.binding.wlanthermo.internal.api.nano.dto.data.Channel c : data.getChannel()) {
            try {
                String json = gson.toJson(c);
                if (!doPost("/setchannels", json)) {
                    break;
                }
            } catch (InterruptedException e) {
                logger.debug("Push interrupted. {}", e.getMessage());
                return;
            }
        }

        // push update for pitmaster channels
        try {
            String json = gson.toJson(data.getPitmaster().getPm());
            doPost("/setpitmaster", json);
        } catch (InterruptedException e) {
            logger.debug("Push interrupted. {}", e.getMessage());
        }
    }

    @Override
    protected void pull() {
        try {
            // Update objects with data from device
            data = doGet("/data", Data.class);
            settings = doGet("/settings", Settings.class);

            // Update channels
            for (Channel channel : thing.getChannels()) {
                try {
                    State state = WlanThermoNanoV1CommandHandler.getState(channel.getUID(), data, settings);
                    updateState(channel.getUID(), state);
                } catch (WlanThermoUnknownChannelException e) {
                    // if we could not obtain a state, try trigger instead
                    try {
                        String trigger = WlanThermoNanoV1CommandHandler.getTrigger(channel.getUID(), data);
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
