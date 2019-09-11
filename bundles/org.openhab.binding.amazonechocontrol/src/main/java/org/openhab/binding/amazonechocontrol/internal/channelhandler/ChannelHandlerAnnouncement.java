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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
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
                String speak = commandValue;
                if (commandValue.startsWith("{") && commandValue.endsWith("}")) {
                    try {
                        AnnouncementRequestJson request = parseJson(commandValue, AnnouncementRequestJson.class);
                        if (request != null) {
                            if (StringUtils.isEmpty(request.speak)) {
                                speak = " "; // blank generates a beep
                            } else {
                                speak = request.speak;
                            }
                            title = request.title;
                            body = request.body;
                            if (body == null) {
                                body = "";
                            }
                            Boolean sound = request.sound;
                            if (sound != null) {
                                if (sound == false && !speak.startsWith("<speak>")) {
                                    speak = "<speak>" + StringEscapeUtils.escapeXml(speak) + "</speak>";
                                }
                                if (sound == true && speak.startsWith("<speak>")) {
                                    body = "Error: The combination of sound and speak in SSML syntax is not allowed";
                                    title = "Error";
                                    speak = "<speak><lang xml:lang=\"en-UK\">Error: The combination of sound and speak in <prosody rate=\"x-slow\"><say-as interpret-as=\"characters\">SSML</say-as></prosody> syntax is not allowed</lang></speak>";
                                }
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        body = "Invalid Json." + e.getLocalizedMessage();
                        title = "Error";
                        speak = "<speak><lang xml:lang=\"en-US\">" + StringEscapeUtils.escapeXml(body)
                                + "</lang></speak>";
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

    static class AnnouncementRequestJson {
        public @Nullable Boolean sound;
        public @Nullable String title;
        public @Nullable String body;
        public @Nullable String speak;
    }
}
