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
package org.openhab.binding.gemini.internal;

import static org.openhab.binding.gemini.internal.GeminiBindingConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

/**
 * The {@link GeminiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class GeminiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(GeminiHandler.class);

    private @Nullable GeminiConfiguration config;

    private @Nullable GoogleCredentials credentials;
    private @Nullable String projectId;
    private @Nullable VertexAI vertexAI;
    private Map<ChannelUID, ChatSession> chatSessions = new HashMap<>();

    public GeminiHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("request, {}, {}, {}, {}", channelUID, command, CHANNEL_CHAT, channelUID.getId());
        // if (CHANNEL_CHAT.equals(channelUID.getId())) {
        if (command instanceof RefreshType) {
            // TODO: handle data refresh
            logger.warn("refresh for {}", channelUID.getId());
        }

        if (command instanceof StringType stringCommand) {
            // TODO: handle data refresh
            String cmd = stringCommand.toFullString();
            logger.warn("command for {}: {}", channelUID.getId(), cmd);
            try {

                ChatSession chatSession = chatSessions.get(channelUID);
                // wrong model spec will trigger the following exception:
                // io.grpc.StatusRuntimeException: NOT_FOUND: Publisher Model
                // `projects/this-is-my-smart-1538510448613/locations/europe-west3/publishers/google/models/gemini-pro-visio`
                // not found.
                GenerateContentResponse response = chatSession.sendMessage(cmd);
                logger.warn("Gemini: '{}'", ResponseHandler.getText(response));
                updateState(channelUID, new StringType(ResponseHandler.getText(response)));

                /*
                 * String imageUri = "gs://generativeai-downloads/images/scones.jpg";
                 * GenerateContentResponse response = model.generateContent(ContentMaker
                 * .fromMultiModalData(PartMaker.fromMimeTypeAndData("image/jpg", imageUri),
                 * "What's in this photo"));
                 * 
                 * logger.warn("XXX {}", response.toString());
                 */

            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.warn("XXX ioe", e);
            }

        }

        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information:
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
    }

    @Override
    public void initialize() {
        config = getConfigAs(GeminiConfiguration.class);

        if (config.keyFile.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error-key");
            return;
        }

        if (config.location.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error-location");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        boolean initComplete = false;
        projectId = null;
        credentials = null;
        vertexAI = null;
        String keyFileUri = OpenHAB.getConfigFolder() + File.separator + "misc" + File.separator + config.keyFile;
        try (FileInputStream fs = new FileInputStream(keyFileUri)) {
            ServiceAccountCredentials service = ServiceAccountCredentials.fromStream(fs);
            projectId = service.getProjectId();
            credentials = service.createScoped("https://www.googleapis.com/auth/cloud-platform");

            logger.debug("Using stored credentials, project id {}", projectId);

            vertexAI = new VertexAI.Builder().setCredentials(credentials).setProjectId(projectId)
                    .setLocation(config.location).build();

            logger.trace("VertexAI generated");
            initComplete = true;

            // scheduler.execute(() -> initializeLater());

        } catch (java.io.IOException e) {
            logger.warn("Error reading credentials from file '{}'", config.keyFile, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error-credentials");
        }
        if (initComplete) {
            for (Channel channel : getThing().getChannels()) {
                GeminiChannelConfiguration cc = channel.getConfiguration().as(GeminiChannelConfiguration.class);
                logger.debug("found channel {} ({}), model {}", channel.getLabel(), channel.getUID(), cc.model);

                GenerativeModel model = new GenerativeModel(cc.model, vertexAI);

                // Create a chat session to be used for interactive conversation.
                ChatSession chatSession = new ChatSession(model);
                logger.trace("session generated, saving as {}", channel.getUID());
                chatSessions.put(channel.getUID(), chatSession);
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }
}
