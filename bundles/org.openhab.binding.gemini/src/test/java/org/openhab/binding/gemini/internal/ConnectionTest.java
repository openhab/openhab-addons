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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

/**
 *
 * @author Holger Friedrich - Initial contribution
 *
 */
@NonNullByDefault
class ConnectionTest {
    private final Logger logger = LoggerFactory.getLogger(GeminiHandler.class);

    @Test
    @EnabledIfEnvironmentVariable(named = "GOOGLE_APPLICATION_CREDENTIALS", matches = ".*")
    public void invalidFails() {
        String projectId = "";
        String location = "europe-west3";
        String modelName = "gemini-pro-vision";
        /*
         * import java.io.InputStream;
         * import java.util.Objects;
         * import com.google.auth.oauth2.GoogleCredentials;
         * import com.google.auth.oauth2.ServiceAccountCredentials;
         *
         * GoogleCredentials credentials = null;
         * try {
         * InputStream key = getClass().getClassLoader().getResourceAsStream("tmp.json");
         *
         * ServiceAccountCredentials service = ServiceAccountCredentials.fromStream(key);
         * projectId = service.getProjectId();
         * credentials = service.createScoped("https://www.googleapis.com/auth/cloud-platform");
         * logger.warn("{}", Objects.toString(credentials));
         * } catch (Exception e) {
         * logger.warn("credentials fail", e);
         * }
         */

        // Initialize client that will be used to send requests. This client only needs
        // to be created once, and can be reused for multiple requests.
        // .setCredentials(credentials)
        try (VertexAI vertexAI = new VertexAI.Builder().setProjectId(projectId).setLocation(location).build()) {

            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            // Create a chat session to be used for interactive conversation.
            ChatSession chatSession = new ChatSession(model);

            GenerateContentResponse response = chatSession
                    .sendMessage("You are Marvin, the depressed robot. Tell me about the weather today in Germany.");
            logger.warn("{}", ResponseHandler.getText(response));

            /*
             * String imageUri = "gs://generativeai-downloads/images/scones.jpg";
             * GenerateContentResponse response = model.generateContent(ContentMaker
             * .fromMultiModalData(PartMaker.fromMimeTypeAndData("image/jpg", imageUri), "What's in this photo"));
             * 
             * logger.warn("{}", response.toString());
             */
            // thingReachable = true; // <background task with long running initialization here>
        } catch (IOException e) {
            logger.warn("ioe", e);
        }
    }
}
