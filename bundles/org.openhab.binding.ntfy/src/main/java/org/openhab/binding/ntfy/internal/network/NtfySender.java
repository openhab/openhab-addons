/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal.network;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.ntfy.internal.NtfyConnectionConfiguration;
import org.openhab.binding.ntfy.internal.NtfyConnectionHandler;
import org.openhab.binding.ntfy.internal.models.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NtfySender} builds and sends a message to a topic
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class NtfySender {

    private @Nullable NtfyConnectionConfiguration configuration;
    private Supplier<NtfyConnectionHandler> getBridgeHandler;
    private HttpClient httpClient;
    private String topicName;
    private final Logger logger = LoggerFactory.getLogger(NtfySender.class);

    /**
     * Creates a new sender for the given topic.
     *
     * @param topicName the ntfy topic name to send messages to
     * @param httpClient the Jetty {@link HttpClient} used to perform requests
     * @param getBridgeHandler supplier returning the associated {@link NtfyConnectionHandler}
     */
    public NtfySender(String topicName, HttpClient httpClient, Supplier<NtfyConnectionHandler> getBridgeHandler) {
        this.topicName = topicName;
        this.getBridgeHandler = getBridgeHandler;
        this.httpClient = httpClient;
    }

    /**
     * Sends the given message to the configured topic and returns the created
     * {@link MessageEvent} on success.
     *
     * @param ntfyMessage the message to send
     * @return the created {@link MessageEvent} when successful, or {@code null} on failure
     * @throws URISyntaxException
     */
    public @Nullable MessageEvent sendMessage(NtfyMessage ntfyMessage) throws URISyntaxException {
        NtfyConnectionConfiguration connectionConfiguration = getConfigurarion();

        Request request = httpClient.newRequest(new URI(connectionConfiguration.hostname + "/" + topicName))
                .method(HttpMethod.POST).content(new StringContentProvider(ntfyMessage.getMessage()))
                .timeout(1, TimeUnit.MINUTES);

        if (connectionConfiguration.isAuthHeaderNeeded()) {
            String authHeader = connectionConfiguration.buildAuthHeader();
            request.header("Authorization", authHeader);
        }

        new NtfyMessageHeaderBuilder(ntfyMessage, request).build();

        ContentResponse response;
        try {
            response = request.send();
            if (HttpStatus.isSuccess(response.getStatus())) {
                return GsonDeserializer.deserialize(response.getContentAsString(), MessageEvent.class);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Failed to send message: {}", e.getMessage());
            getBridge().connectionError(e);
        }

        return null;
    }

    /**
     * Uploads the content of a local file to the topic.
     *
     * @param file the filesystem path to the file whose contents will be sent as the request body
     * @param filename optional filename that may be presented to recipients (may be null)
     * @param sequenceId optional sequence id to associate with the uploaded message
     * @return the created {@link MessageEvent} when successful, or {@code null} on failure
     * @throws URISyntaxException when the constructed request URI is invalid
     */
    public @Nullable MessageEvent sendFile(String file, @Nullable String filename, @Nullable String sequenceId)
            throws URISyntaxException {
        NtfyConnectionConfiguration connectionConfiguration = getConfigurarion();

        Request request = httpClient.newRequest(new URI(connectionConfiguration.hostname + "/" + topicName))
                .method(HttpMethod.PUT);

        NtfyMessage ntfyMessage = new NtfyMessage();
        ntfyMessage.setSequenceId(sequenceId);
        ntfyMessage.setFilename(filename);
        new NtfyMessageHeaderBuilder(ntfyMessage, request).build();

        if (connectionConfiguration.isAuthHeaderNeeded()) {
            String authHeader = connectionConfiguration.buildAuthHeader();
            request.header("Authorization", authHeader);
        }

        try {
            java.nio.file.Path path = java.nio.file.Path.of(file);
            request.content(new InputStreamContentProvider(java.nio.file.Files.newInputStream(path)));
        } catch (IOException e) {
            logger.error("Failed to read file for upload: {}", e.getMessage());
            return null;
        }

        ContentResponse response;
        try {
            response = request.send();
            if (HttpStatus.isSuccess(response.getStatus())) {
                return GsonDeserializer.deserialize(response.getContentAsString(), MessageEvent.class);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Failed to send file: {}", e.getMessage());
            getBridge().connectionError(e);
        }
        return null;
    }

    /**
     * Deletes a message identified by the provided sequence id from the topic.
     *
     * @param sequenceId the sequence id of the message to delete
     * @return {@code true} when deletion succeeded, {@code false} otherwise
     * @throws URISyntaxException when the request URI could not be constructed
     */
    public boolean deleteMessage(String sequenceId) throws URISyntaxException {
        NtfyConnectionConfiguration connectionConfiguration = getConfigurarion();

        Request request = httpClient
                .newRequest(new URI(connectionConfiguration.hostname + "/" + topicName + "/" + sequenceId))
                .method(HttpMethod.DELETE);

        if (connectionConfiguration.isAuthHeaderNeeded()) {
            String authHeader = connectionConfiguration.buildAuthHeader();
            request.header("Authorization", authHeader);
        }

        try {
            return HttpStatus.isSuccess(request.send().getStatus());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("Failed to delete message: {}", e.getMessage());
            getBridge().connectionError(e);
        }
        return false;
    }

    private NtfyConnectionConfiguration getConfigurarion() {
        NtfyConnectionConfiguration configuration = this.configuration;
        if (configuration != null) {
            return configuration;
        }

        configuration = getBridge().getThing().getConfiguration().as(NtfyConnectionConfiguration.class);
        return configuration;
    }

    private NtfyConnectionHandler getBridge() {
        return getBridgeHandler.get();
    }
}
