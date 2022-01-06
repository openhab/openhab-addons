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
package org.openhab.binding.sonyaudio.internal.handler;

import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link HtMt500Handler} is responsible for handling commands for HT-ST500, which are
 * sent to one of the channels.
 *
 * @author David Åberg - Initial contribution
 */
public class HtMt500Handler extends SonyAudioHandler {

    public HtMt500Handler(Thing thing, WebSocketClient webSocketClient) {
        super(thing, webSocketClient);
    }

    @Override
    public String setInputCommand(Command command) {
        switch (command.toString().toLowerCase()) {
            case "btaudio":
                return "extInput:btAudio";
            case "tv":
                return "extInput:tv";
            case "analog":
                return "extInput:line";
            case "usb":
                return "storage:usb1";
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
        if (in.contains("extinput:tv".toLowerCase())) {
            return new StringType("tv");
        }
        if (in.contains("extinput:line".toLowerCase())) {
            return new StringType("analog");
        }
        if (in.contains("storage:usb1".toLowerCase())) {
            return new StringType("usb");
        }
        if (in.contains("dlna:music".toLowerCase())) {
            return new StringType("network");
        }
        if (in.contains("cast:audio".toLowerCase())) {
            return new StringType("cast");
        }
        return new StringType(input);
    }
}
