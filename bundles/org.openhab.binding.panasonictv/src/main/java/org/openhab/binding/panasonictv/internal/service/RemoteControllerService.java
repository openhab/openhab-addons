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
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.panasonictv.internal.api.PanasonicEventListener;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteControllerService} is responsible for handling remote
 * controller commands.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
public class RemoteControllerService extends AbstractPanasonicTvService {
    public static final String SERVICE_NAME = "p00RemoteController";
    private static final String SERVICE_ID = "p00NetworkControl";
    private static final Set<String> SUPPORTED_COMMANDS = Set.of(KEY_CODE);
    private static final Map<String, List<ChannelConverter>> CONVERTERS = Map.of("sourceName", List
            .of(new ChannelConverter(SOURCE_NAME, StringType::new), new ChannelConverter(SOURCE_ID, StringType::new)));
    private static final Set<String> TV_INPUT_KEY_CODES = Set.of("NRC_HDMI1-ONOFF", "NRC_HDMI2-ONOFF",
            "NRC_HDMI3-ONOFF", "NRC_HDMI4-ONOFF", "NRC_TV-ONOFF", "NRC_VIDEO1-ONOFF", "NRC_VIDEO2-ONOFF");

    private final Logger logger = LoggerFactory.getLogger(RemoteControllerService.class);

    public RemoteControllerService(ScheduledExecutorService scheduler, UpnpIOService service, String udn,
            int refreshInterval, PanasonicEventListener listener) {
        super(udn, service, listener, scheduler, refreshInterval, SERVICE_NAME, SERVICE_ID, SUPPORTED_COMMANDS,
                CONVERTERS);

        logger.debug("Create a Panasonic TV RemoteController service");
    }

    @Override
    public void handleCommand(String channel, Command command) {
        logger.debug("Received channel: {}, command: {}", channel, command);

        switch (channel) {
            case KEY_CODE:
                if (command instanceof StringType) {
                    sendKeyCode(command.toString().toUpperCase());
                } else {
                    logger.warn("Command '{}' not supported for channel '{}'", command, channel);
                }
                break;
        }
    }

    /**
     * Sends a key code to Panasonic TV device.
     *
     * @param key Button code to send
     */
    private void sendKeyCode(final String key) {
        updateResourceState(SERVICE_ID, "X_SendKey", Map.of("X_KeyEvent", key));

        if (TV_INPUT_KEY_CODES.contains(key)) {
            onValueReceived("sourceName", key.substring(4, key.length() - 6), "p00NetworkControl");
        }
    }

    @Override
    protected void polling() {
        // nothing to do here
    }
}
