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
package org.openhab.binding.wlanthermo.internal.api.esp32;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.TRIGGER_NONE;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants;
import org.openhab.binding.wlanthermo.internal.WlanThermoException;
import org.openhab.binding.wlanthermo.internal.WlanThermoHandler;
import org.openhab.binding.wlanthermo.internal.WlanThermoInputException;
import org.openhab.binding.wlanthermo.internal.WlanThermoUnknownChannelException;
import org.openhab.binding.wlanthermo.internal.api.esp32.dto.data.Data;
import org.openhab.binding.wlanthermo.internal.api.esp32.dto.settings.Settings;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link WlanThermoEsp32Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoEsp32Handler extends WlanThermoHandler {

    private Data data = new Data();
    private Settings settings = new Settings();

    public WlanThermoEsp32Handler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient, true);
    }

    @Override
    protected State getState(ChannelUID channelUID) throws WlanThermoInputException, WlanThermoUnknownChannelException {
        return WlanThermoEsp32CommandHandler.getState(channelUID, data, settings);
    }

    @Override
    protected boolean setState(ChannelUID channelUID, Command command) {
        return WlanThermoEsp32CommandHandler.setState(channelUID, command, data, settings);
    }

    @Override
    protected void push() {
        // Push update for sensor channels
        for (org.openhab.binding.wlanthermo.internal.api.esp32.dto.data.Channel c : data.getChannel()) {
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

            // Update Channels if required
            Map<String, String> properties = editProperties();
            Boolean pmEnabled = settings.getFeatures().getBluetooth();
            int pmChannels = pmEnabled ? data.getPitmaster().getPm().size() : 0;
            int tempChannels = data.getChannel().size();

            // Update properties
            properties.putIfAbsent(WlanThermoBindingConstants.PROPERTY_MODEL, settings.getDevice().getDevice());
            properties.putIfAbsent(WlanThermoBindingConstants.PROPERTY_SERIAL, settings.getDevice().getSerial());
            properties.putIfAbsent(WlanThermoBindingConstants.PROPERTY_ESP32_BT_ENABLED,
                    settings.getFeatures().getBluetooth().toString());
            properties.putIfAbsent(WlanThermoBindingConstants.PROPERTY_ESP32_PM_ENABLED, pmEnabled.toString());
            properties.put(WlanThermoBindingConstants.PROPERTY_ESP32_TEMP_CHANNELS, String.valueOf(tempChannels));
            properties.put(WlanThermoBindingConstants.PROPERTY_ESP32_PM_CHANNELS, String.valueOf(pmChannels));
            updateProperties(properties);

            // Update channel state
            for (Channel channel : thing.getChannels()) {
                try {
                    State state = WlanThermoEsp32CommandHandler.getState(channel.getUID(), data, settings);
                    updateState(channel.getUID(), state);
                } catch (WlanThermoUnknownChannelException e) {
                    // if we could not obtain a state, try trigger instead
                    try {
                        String trigger = WlanThermoEsp32CommandHandler.getTrigger(channel.getUID(), data);
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
