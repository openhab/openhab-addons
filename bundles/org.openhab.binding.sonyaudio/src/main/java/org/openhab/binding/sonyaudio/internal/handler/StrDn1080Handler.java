/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link StrDn1080Handler} is responsible for handling commands for STR-DN1080, which are
 * sent to one of the channels.
 *
 * @author David Åberg - Initial contribution
 */
public class StrDn1080Handler extends SonyAudioHandler {

    private final Logger logger = LoggerFactory.getLogger(StrDn1080Handler.class);

    public StrDn1080Handler(Thing thing, WebSocketClient webSocketClient) {
        super(thing, webSocketClient);
    }

    @Override
    public String setInputCommand(Command command) {
        switch (command.toString().toLowerCase()) {
            case "btaudio":
                return "extInput:btAudio";
            case "fm":
                return "radio:fm";
            case "usb":
                return "storage:usb1";
            case "bd/dvd":
                return "extInput:bd-dvd";
            case "game":
                return "extInput:game";
            case "sat/catv":
                return "extInput:sat-catv";
            case "video1":
                return "extInput:video?port=1";
            case "video2":
                return "extInput:video?port=2";
            case "tv":
                return "extInput:tv";
            case "sa-cd/cd":
                return "extInput:sacd-cd";
            case "network":
                return "dlna:music";
            case "source":
                return "extInput:source";
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
        if (in.contains("radio:fm".toLowerCase())) {
            return new StringType("fm");
        }
        if (in.contains("storage:usb1".toLowerCase())) {
            return new StringType("usb");
        }
        if (in.contains("extInput:bd-dvd".toLowerCase())) {
            return new StringType("bd/dvd");
        }
        if (in.contains("extInput:game".toLowerCase())) {
            return new StringType("game");
        }
        if (in.contains("extInput:sat-catv".toLowerCase())) {
            return new StringType("sat/catv");
        }
        if (in.contains("extInput:video?port=1".toLowerCase())) {
            return new StringType("video1");
        }
        if (in.contains("extInput:video?port=2".toLowerCase())) {
            return new StringType("video2");
        }
        if (in.contains("extinput:tv".toLowerCase())) {
            return new StringType("tv");
        }
        if (in.contains("extInput:sacd-cd".toLowerCase())) {
            return new StringType("sa-cd/cd");
        }
        if (in.contains("dlna:music".toLowerCase())) {
            return new StringType("network");
        }
        if (in.contains("extInput:source".toLowerCase())) {
            return new StringType("source");
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
                logger.debug("StrDn1080Handler handleSoundSettings RefreshType");
                Map<String, String> result = soundSettingsCache.getValue();

                if (result != null) {
                    logger.debug("StrDn1080Handler Updateing sound field to {} {}", result.get("pureDirect"),
                            result.get("soundField"));
                    if ("on".equalsIgnoreCase(result.get("pureDirect"))) {
                        updateState(channelUID, new StringType("pureDirect"));
                    } else {
                        updateState(channelUID, new StringType(result.get("soundField")));
                    }
                }
            } catch (CompletionException ex) {
                throw new IOException(ex.getCause());
            }
        }
        if (command instanceof StringType stringCommand) {
            if ("pureDirect".equalsIgnoreCase(stringCommand.toString())) {
                connection.setSoundSettings("pureDirect", "on");
            } else {
                connection.setSoundSettings("soundField", stringCommand.toString());
            }
        }
    }
}
