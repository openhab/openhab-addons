/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.channelhandler;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ChannelHandlerAnnouncement} is responsible for the announcement channel
 *
 * @author Michael Geramb - Initial contribution
 */
public class ChannelHandlerAnnouncement extends ChannelHandler {
    public static final String CHANNEL_NAME = "announcement";

    public ChannelHandlerAnnouncement(IAmazonThingHandler thingHandler, Gson gson) {
        super(thingHandler, gson);
    }

    @Override
    public boolean tryHandleCommand(Device device, Connection connection, String channelId, Command command)
            throws IOException, URISyntaxException {
        if (channelId.equals(CHANNEL_NAME)) {
            if (command instanceof StringType) {
                String commandValue = ((StringType) command).toFullString();
                String body = commandValue;
                String title = null;
                String speak = " "; // blank generates a beep
                if (commandValue.startsWith("{") && commandValue.endsWith("}")) {
                    try {
                        AnnouncementRequestJson request = parseJson(commandValue, AnnouncementRequestJson.class);
                        if (request != null) {
                            title = request.title;
                            body = request.body;
                            if (body == null) {
                                body = "";
                            }
                            if (request.sound == false) {
                                speak = "<speak></speak>";
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        body = e.getLocalizedMessage();
                    }
                }
                connection.sendAnnouncement(device, speak, body, title, 0, 0);
            }
            RefreshChannel();
        }
        return false;
    }

    void RefreshChannel() {
        thingHandler.updateChannelState(CHANNEL_NAME, new StringType(""));
    }

    class AnnouncementRequestJson {
        public @Nullable Boolean sound;
        public @Nullable String title;
        public @Nullable String body;
    }
}
