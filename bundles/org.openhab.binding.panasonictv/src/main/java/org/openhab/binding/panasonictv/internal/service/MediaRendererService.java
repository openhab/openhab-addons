/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.panasonictv.internal.service;

import static org.openhab.binding.panasonictv.internal.PanasonicTvBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.panasonictv.internal.api.PanasonicEventListener;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MediaRendererService} is responsible for handling MediaRenderer
 * commands.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
public class MediaRendererService extends AbstractPanasonicTvService {
    public static final String SERVICE_NAME = "MediaRenderer";
    private static final String SERVICE_ID = "RenderingControl";
    private static final Set<String> SUPPORTED_COMMANDS = Set.of(VOLUME, MUTE);
    private static final Map<String, List<ChannelConverter>> CONVERTERS = Map.of("CurrentVolume",
            List.of(new ChannelConverter(VOLUME, PercentType::new)), "CurrentMute",
            List.of(new ChannelConverter(MUTE, v -> OnOffType.from(v.equals("true")))));

    private final Logger logger = LoggerFactory.getLogger(MediaRendererService.class);

    public MediaRendererService(ScheduledExecutorService scheduler, UpnpIOService upnpIOService, String udn,
            int refreshInterval, PanasonicEventListener eventListener) {
        super(udn, upnpIOService, eventListener, scheduler, refreshInterval, SERVICE_NAME, SERVICE_ID,
                SUPPORTED_COMMANDS, CONVERTERS);
        logger.debug("Create a Panasonic TV MediaRenderer service");
    }

    @Override
    protected void polling() {
        try {
            updateResourceState(SERVICE_ID, "GetVolume", Map.of("InstanceID", "0", "Channel", "Master"));
            updateResourceState(SERVICE_ID, "GetMute", Map.of("InstanceID", "0", "Channel", "Master"));
        } catch (Exception e) {
            reportError("Error occurred during poll", e);
        }
    }

    @Override
    public void handleCommand(String channel, Command command) {
        logger.debug("Received channel: {}, command: {}", channel, command);

        switch (channel) {
            case VOLUME:
                setVolume(command);
                break;
            case MUTE:
                setMute(command);
                break;
            default:
                logger.warn("Panasonic TV doesn't support transmitting for channel '{}'", channel);
        }
    }

    private void setVolume(Command command) {
        int newValue;

        try {
            String oldValue = stateMap.get("CurrentVolume");
            if (oldValue == null) {
                logger.debug("Old value for 'CurrentVolume' not found, assuming 0");
                oldValue = "0";
            }
            newValue = DataConverters.convertCommandToIntValue(command, 0, 100, Integer.parseInt(oldValue));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState(SERVICE_ID, "SetVolume",
                Map.of("InstanceID", "0", "Channel", "Master", "DesiredVolume", Integer.toString(newValue)));
        // updateResourceState(SERVICE_ID, "GetVolume", Map.of("InstanceID", "0", "Channel", "Master"));
    }

    private void setMute(Command command) {
        boolean newValue;

        try {
            newValue = DataConverters.convertCommandToBooleanValue(command);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState(SERVICE_ID, "SetMute",
                Map.of("InstanceID", "0", "Channel", "Master", "DesiredMute", Boolean.toString(newValue)));
        // updateResourceState(SERVICE_ID, "GetMute", Map.of("InstanceID", "0", "Channel", "Master"));
    }
}
