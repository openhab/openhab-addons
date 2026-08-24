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
package org.openhab.binding.atagone.internal.api;

import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.binding.atagone.internal.dto.PairReplyDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * HTTP client for the ATAG ONE local API (port 10000).
 * <p>
 * Three endpoints: {@code /pair}, {@code /retrieve}, {@code /update}. All use HTTP POST with JSON.
 * The device requires at least 1 second between consecutive requests; this client enforces that
 * with a synchronized rate limiter. Transient transport errors (EOF, timeout) are retried up to
 * {@value #MAX_RETRIES} times.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOneApiClient {

    /**
     * Bitmask for retrieve: control(1)+schedules(2)+configuration(4)+report(8)+status(16)+details(64) = 95.
     * wifi_scan(32) is deliberately excluded — it scans nearby APs and delays the response by several
     * seconds. The wifi-signal channel is unaffected: rssi is reported in the report(8) section.
     */
    private static final int INFO_BITMASK = 95;
    private static final int REQUEST_TIMEOUT_S = 5;
    private static final long MIN_INTERVAL_MS = 2_000L;
    private static final int MAX_RETRIES = 5;

    private final Logger logger = LoggerFactory.getLogger(AtagOneApiClient.class);

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String clientId;
    private final Gson gson = new GsonBuilder().create();

    private long lastRequestMs = 0;

    public AtagOneApiClient(HttpClient httpClient, String hostname, int port, String clientId) {
        this.httpClient = httpClient;
        this.baseUrl = "http://" + hostname + ":" + port;
        this.clientId = clientId;
    }

    /**
     * Sends a pairing request and returns the device's {@code acc_status}.
     *
     * @return 1 if the user must press Accept on the thermostat, 2 if granted, 3 if denied
     * @throws AtagOneCommunicationException on transport or protocol failure
     */
    public int pair() throws AtagOneCommunicationException {
        JsonObject accounts = new JsonObject();
        accounts.addProperty("user_account", "");
        accounts.addProperty("mac_address", clientId);
        accounts.addProperty("device_name", "openHAB");
        accounts.addProperty("account_type", 0);

        JsonArray accountsArray = new JsonArray();
        accountsArray.add(accounts);

        JsonObject pairMsg = new JsonObject();
        pairMsg.addProperty("seqnr", 0);
        pairMsg.add("accounts", accountsArray);

        JsonObject root = new JsonObject();
        root.add("pair_message", pairMsg);

        String responseJson = sendRequest("/pair", gson.toJson(root));
        logger.trace("pair raw response: {}", responseJson);
        JsonObject reply = JsonParser.parseString(responseJson).getAsJsonObject().getAsJsonObject("pair_reply");
        if (reply == null) {
            throw new AtagOneCommunicationException("Missing pair_reply in response: " + responseJson);
        }
        PairReplyDTO dto = gson.fromJson(reply, PairReplyDTO.class);
        if (dto == null) {
            throw new AtagOneCommunicationException("Failed to parse pair_reply");
        }
        logger.debug("pair() → acc_status={}", dto.acc_status);
        return dto.acc_status;
    }

    /**
     * Retrieves the full device state.
     *
     * @return parsed {@link RetrieveReplyDTO}
     * @throws AtagOneCommunicationException on transport or protocol failure
     */
    public RetrieveReplyDTO retrieve() throws AtagOneCommunicationException {
        JsonObject auth = new JsonObject();
        auth.addProperty("user_account", "");
        auth.addProperty("mac_address", clientId);

        JsonObject retrieveMsg = new JsonObject();
        retrieveMsg.addProperty("seqnr", 0);
        retrieveMsg.add("account_auth", auth);
        retrieveMsg.addProperty("info", INFO_BITMASK);

        JsonObject root = new JsonObject();
        root.add("retrieve_message", retrieveMsg);

        String responseJson = sendRequest("/retrieve", gson.toJson(root));
        logger.trace("retrieve raw response: {}", responseJson);
        JsonObject reply = JsonParser.parseString(responseJson).getAsJsonObject().getAsJsonObject("retrieve_reply");
        if (reply == null) {
            throw new AtagOneCommunicationException("Missing retrieve_reply in response: " + responseJson);
        }
        int accStatus = reply.has("acc_status") ? reply.get("acc_status").getAsInt() : 0;
        if (accStatus != 2) {
            throw new AtagOneCommunicationException("retrieve denied: acc_status=" + accStatus);
        }
        RetrieveReplyDTO result = gson.fromJson(reply, RetrieveReplyDTO.class);
        if (result == null) {
            throw new AtagOneCommunicationException("Failed to parse retrieve_reply");
        }
        validateComplete(result);
        return result;
    }

    /**
     * Verifies that every section {@link org.openhab.binding.atagone.internal.AtagOneHandler#updateChannels}
     * unconditionally dereferences is present. A firmware reply missing a section (e.g. during the boiler's
     * post-write API reinitialization window) would otherwise reach the handler as a DTO with null fields and
     * crash it with an NPE — which, thrown from a {@code scheduleWithFixedDelay} task, would silently and
     * permanently stop all future polls.
     */
    static void validateComplete(RetrieveReplyDTO result) throws AtagOneCommunicationException {
        if (result.report == null || result.control == null || result.configuration == null) {
            throw new AtagOneCommunicationException("retrieve_reply missing required section(s)");
        }
        if (result.report.details == null) {
            throw new AtagOneCommunicationException("retrieve_reply.report missing details section");
        }
    }

    /**
     * Sends a control update. Mode and its duration parameters must be sent together so the
     * device applies both atomically — sending {@code ch_mode} alone leaves the duration at
     * its previous (often hardcoded) value.
     *
     * @param controlUpdate fields to change; null fields are omitted from the JSON body
     * @throws AtagOneCommunicationException on transport or protocol failure
     */
    public void updateControl(ControlUpdateDTO controlUpdate) throws AtagOneCommunicationException {
        updateControl(controlUpdate, null);
    }

    /**
     * Sends a control and optional configuration update in a single request.
     *
     * @param controlUpdate control fields to change (null fields omitted)
     * @param configUpdate configuration fields to change, or null to omit the configuration block
     * @throws AtagOneCommunicationException on transport or protocol failure
     */
    public void updateControl(ControlUpdateDTO controlUpdate, @Nullable DeviceConfigUpdateDTO configUpdate)
            throws AtagOneCommunicationException {
        JsonObject auth = new JsonObject();
        auth.addProperty("user_account", "");
        auth.addProperty("mac_address", clientId);

        JsonObject updateMsg = new JsonObject();
        updateMsg.addProperty("seqnr", 0);
        updateMsg.add("account_auth", auth);
        updateMsg.add("control", gson.toJsonTree(controlUpdate));
        if (configUpdate != null) {
            updateMsg.add("configuration", gson.toJsonTree(configUpdate));
        }

        JsonObject root = new JsonObject();
        root.add("update_message", updateMsg);

        String responseJson = sendRequest("/update", gson.toJson(root));
        JsonObject reply = JsonParser.parseString(responseJson).getAsJsonObject().getAsJsonObject("update_reply");
        if (reply == null) {
            throw new AtagOneCommunicationException("Missing update_reply in response: " + responseJson);
        }
        int accStatus = reply.has("acc_status") ? reply.get("acc_status").getAsInt() : 0;
        if (accStatus != 2) {
            throw new AtagOneCommunicationException("update denied: acc_status=" + accStatus);
        }
        logger.debug("updateControl() succeeded");
    }

    /**
     * Sends an HTTP POST with rate-limiting and retry on transient failures.
     * Synchronized so only one request is in-flight at a time and the 1-second
     * inter-request gap is respected across concurrent callers.
     */
    private synchronized String sendRequest(String path, String body) throws AtagOneCommunicationException {
        // Enforce minimum inter-request interval required by the device firmware.
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestMs;
        if (elapsed < MIN_INTERVAL_MS) {
            try {
                Thread.sleep(MIN_INTERVAL_MS - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AtagOneCommunicationException("Interrupted while waiting for rate limit", e);
            }
        }

        // The device is an HTTP/1.0 server that closes every connection after responding.
        // Jetty's pool may hand us a stale half-closed connection on the first attempt,
        // producing an EOFException that is not a real failure. We retry that one time for
        // free; any subsequent EOF within the same call counts toward MAX_RETRIES normally.
        boolean staleCorrectionUsed = false;
        Exception lastException = new AtagOneCommunicationException("Unreachable");
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                lastRequestMs = System.currentTimeMillis();
                String url = baseUrl + path;
                byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                ContentResponse response = httpClient.newRequest(url).method(HttpMethod.POST)
                        .version(HttpVersion.HTTP_1_0).header(HttpHeader.CONTENT_TYPE, "application/json")
                        .header(HttpHeader.CONNECTION, "close").content(new BytesContentProvider(bodyBytes))
                        .timeout(REQUEST_TIMEOUT_S, TimeUnit.SECONDS).send();
                logger.trace("POST {} → HTTP {}", path, response.getStatus());
                return response.getContentAsString();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AtagOneCommunicationException("Interrupted during request to " + path, e);
            } catch (TimeoutException e) {
                lastException = e;
                logger.debug("Timeout on {} (attempt {}): {}", path, attempt + 1, e.getMessage());
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof EOFException && !staleCorrectionUsed) {
                    staleCorrectionUsed = true;
                    logger.trace("Stale pooled connection on {} — retrying on fresh connection", path);
                    attempt--; // don't count this against MAX_RETRIES
                } else if (cause instanceof EOFException || cause instanceof SocketTimeoutException) {
                    lastException = e;
                    logger.debug("Transient error on {} (attempt {}): {}", path, attempt + 1, e.getMessage());
                } else {
                    throw new AtagOneCommunicationException("Request to " + path + " failed", e);
                }
            }
        }
        throw new AtagOneCommunicationException("Request to " + path + " failed after " + MAX_RETRIES + " retries",
                lastException);
    }
}
