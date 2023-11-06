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
package org.openhab.binding.sonyaudio.internal.handler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletionException;

import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SrsZr5Handler} is responsible for handling commands for SRS-ZR5, which are
 * sent to one of the channels.
 *
 * @author David Åberg - Initial contribution
 */
public class SrsZr5Handler extends SonyAudioHandler {

    private final Logger logger = LoggerFactory.getLogger(SrsZr5Handler.class);

    public SrsZr5Handler(Thing thing, WebSocketClient webSocketClient) {
        super(thing, webSocketClient);
    }

    @Override
    public String setInputCommand(Command command) {
        switch (command.toString().toLowerCase()) {
            case "btaudio":
                return "extInput:btAudio";
            case "usb":
                return "storage:usb1";
            case "analog":
                return "extInput:line?port=1";
            case "hdmi":
                return "extInput:hdmi";
            case "network":
                return "dlna:music";
            case "cast":
                return "cast:audio";
        }
        return command.toString();
    }

    @Override
    public StringType inputSource(String input) {
        String in = input.toLowerCase();
        if (in.contains("extinput:btaudio".toLowerCase())) {
            return new StringType("btaudio");
        }
        if (in.contains("storage:usb1".toLowerCase())) {
            return new StringType("usb");
        }
        if (in.contains("extinput:line?port=1".toLowerCase())) {
            return new StringType("analog");
        }
        if (in.contains("extinput:hdmi".toLowerCase())) {
            return new StringType("hdmi1");
        }
        if (in.contains("dlna:music".toLowerCase())) {
            return new StringType("network");
        }
        if (in.contains("cast:audio".toLowerCase())) {
            return new StringType("cast");
        }
        return new StringType(input);
    }

    @Override
    public void handleSoundSettings(Command command, ChannelUID channelUID) throws IOException {
        if (command instanceof RefreshType) {
            try {
                logger.debug("SrsZr5Handler handleSoundSettings RefreshType");
                Map<String, String> result = soundSettingsCache.getValue();
                if (result != null) {
                    logger.debug("SrsZr5Handler Updating sound field to {} {}", result.get("clearAudio"),
                            result.get("soundField"));
                    if ("on".equalsIgnoreCase(result.get("clearAudio"))) {
                        updateState(channelUID, new StringType("clearAudio"));
                    } else {
                        updateState(channelUID, new StringType(result.get("soundField")));
                    }
                }
            } catch (CompletionException ex) {
                throw new IOException(ex.getCause());
            }
        }
        if (command instanceof StringType stringCommand) {
            if ("clearAudio".equalsIgnoreCase(stringCommand.toString())) {
                connection.setSoundSettings("clearAudio", "on");
            } else {
                connection.setSoundSettings("soundField", stringCommand.toString());
            }
        }
    }
}
