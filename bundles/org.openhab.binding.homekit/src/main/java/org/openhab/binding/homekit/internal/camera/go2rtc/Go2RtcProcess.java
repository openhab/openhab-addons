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
package org.openhab.binding.homekit.internal.camera.go2rtc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Runs one go2rtc process for a camera, using a generated YAML configuration file.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Go2RtcProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(Go2RtcProcess.class);
    private static final long STOP_TIMEOUT_SECONDS = 5;

    private final Path binary;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private @Nullable Process process;
    private @Nullable Path configFile;

    public Go2RtcProcess(Path binary) {
        this.binary = binary;
    }

    /**
     * Writes the configuration and starts go2rtc. Any previously started process is stopped first.
     *
     * @param yaml the go2rtc configuration
     * @throws IOException if the configuration cannot be written or the process cannot be started
     */
    public synchronized void start(String yaml) throws IOException {
        stop();
        Path config = Files.createTempFile("go2rtc-", ".yaml");
        Files.writeString(config, yaml, StandardCharsets.UTF_8);
        configFile = config;

        Process p = new ProcessBuilder(binary.toString(), "-config", config.toString()).redirectErrorStream(true)
                .start();
        process = p;
        Thread reader = new Thread(() -> logOutput(p), "OH-binding-homekit-go2rtc-log");
        reader.setDaemon(true);
        reader.start();
    }

    /**
     * Returns true if go2rtc answers on its API port.
     *
     * @param host the API address from the configuration
     * @param port the API port from the configuration
     */
    public boolean isReady(String host, int port) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://" + host + ":" + port + "/api/config"))
                    .timeout(Duration.ofSeconds(2)).GET().build();
            return httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Returns true if any of the named go2rtc streams has at least one consumer.
     *
     * @param host the API address from the configuration
     * @param port the API port from the configuration
     * @param streams the stream names to check
     * @throws IOException if go2rtc cannot be queried or its response does not describe the streams
     */
    public boolean hasConsumers(String host, int port, String... streams) throws IOException {
        return hasConsumers(fetchStreams(host, port), streams);
    }

    /**
     * Returns true if the named stream has at least one producer, such as the browser publishing talk audio.
     *
     * @throws IOException if the streams cannot be read or the named stream is not configured
     */
    public boolean hasProducers(String host, int port, String stream) throws IOException {
        return hasProducers(fetchStreams(host, port), stream);
    }

    /**
     * Parses a go2rtc {@code /api/streams} response and returns true if the named stream has a producer.
     *
     * @throws IOException if the response is not a JSON object, or the named stream is not present
     */
    static boolean hasProducers(String json, String stream) throws IOException {
        JsonElement entry = parseStreams(json).get(stream);
        if (entry == null || !entry.isJsonObject()) {
            throw new IOException("go2rtc streams response does not contain the configured streams");
        }
        JsonElement producers = entry.getAsJsonObject().get("producers");
        return producers != null && producers.isJsonArray() && !producers.getAsJsonArray().isEmpty();
    }

    /**
     * Exchanges a WebRTC SDP offer for an answer with go2rtc, for the named stream.
     *
     * @param host the API address from the configuration
     * @param port the API port from the configuration
     * @param stream the stream name to view
     * @param offer the browser's SDP offer
     * @return the go2rtc SDP answer, or null if go2rtc rejects the offer, typically because the stream has no producer
     * @throws IOException if go2rtc cannot be reached
     */
    public @Nullable String answerWebRtc(String host, int port, String stream, String offer) throws IOException {
        String query = URLEncoder.encode(stream, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://" + host + ":" + port + "/api/webrtc?src=" + query))
                .timeout(Duration.ofSeconds(10)).header("Content-Type", "application/sdp")
                .POST(HttpRequest.BodyPublishers.ofString(offer)).build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while requesting WebRTC answer from go2rtc", e);
        }
        if (response.statusCode() / 100 != 2) {
            LOGGER.debug("go2rtc rejected WebRTC offer for stream {} with HTTP {}", stream, response.statusCode());
            return null;
        }
        return response.body();
    }

    private String fetchStreams(String host, int port) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://" + host + ":" + port + "/api/streams"))
                .timeout(Duration.ofSeconds(2)).GET().build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while querying go2rtc", e);
        }
        if (response.statusCode() != 200) {
            throw new IOException("go2rtc streams request failed with HTTP " + response.statusCode());
        }
        return response.body();
    }

    /**
     * Parses a go2rtc {@code /api/streams} response and returns true if any of the named streams has a
     * consumer. A {@code null} or empty consumer list means no consumers.
     *
     * @throws IOException if the response is not a JSON object, or none of the named streams is present
     */
    static boolean hasConsumers(String json, String... streams) throws IOException {
        JsonObject all = parseStreams(json);
        boolean found = false;
        for (String name : streams) {
            JsonElement stream = all.get(name);
            if (stream == null || !stream.isJsonObject()) {
                continue;
            }
            found = true;
            JsonElement consumers = stream.getAsJsonObject().get("consumers");
            if (consumers != null && consumers.isJsonArray() && !consumers.getAsJsonArray().isEmpty()) {
                return true;
            }
        }
        if (!found) {
            throw new IOException("go2rtc streams response does not contain the configured streams");
        }
        return false;
    }

    private static JsonObject parseStreams(String json) throws IOException {
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (JsonParseException e) {
            throw new IOException("Invalid go2rtc streams response", e);
        }
        if (!root.isJsonObject()) {
            throw new IOException("Unexpected go2rtc streams response");
        }
        return root.getAsJsonObject();
    }

    /**
     * Returns true if the process has been started and is still running.
     */
    public synchronized boolean isRunning() {
        Process p = process;
        return p != null && p.isAlive();
    }

    /**
     * Stops the process, forcing it to exit if it does not stop within a few seconds, and removes the configuration.
     */
    public synchronized void stop() {
        Process p = process;
        process = null;
        if (p != null) {
            p.destroy();
            try {
                if (!p.waitFor(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                }
            } catch (InterruptedException e) {
                p.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
        Path config = configFile;
        configFile = null;
        if (config != null) {
            try {
                Files.deleteIfExists(config);
            } catch (IOException e) {
                LOGGER.debug("Failed to delete go2rtc configuration {}", config, e);
            }
        }
    }

    /**
     * Logs the process output until the process exits or its output is closed.
     */
    private static void logOutput(Process p) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.debug("go2rtc: {}", line);
            }
        } catch (IOException e) {
            LOGGER.debug("go2rtc output closed", e);
        }
    }
}
