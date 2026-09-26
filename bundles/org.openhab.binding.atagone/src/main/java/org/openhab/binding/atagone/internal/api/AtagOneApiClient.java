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
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * HTTP client for the ATAG ONE local API (port 10000, three endpoints: /pair, /retrieve, /update).
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOneApiClient {

    // wifi_scan(32) excluded: triggers an AP scan, delaying the device's response by several seconds
    private static final int INFO_BITMASK = 95;
    private static final int REQUEST_TIMEOUT_S = 15;
    private static final long MIN_INTERVAL_MS = 1_000L;
    private static final int MAX_RETRIES = 7;

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
        JsonObject reply = parseReplyObject(responseJson, "pair_reply");
        PairReplyDTO dto = gson.fromJson(reply, PairReplyDTO.class);
        if (dto == null) {
            throw new AtagOneCommunicationException("Failed to parse pair_reply");
        }
        logger.debug("pair() → acc_status={}", dto.acc_status);
        return dto.acc_status;
    }

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
        JsonObject reply = parseReplyObject(responseJson, "retrieve_reply");
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

    JsonObject parseReplyObject(String responseJson, String replyKey) throws AtagOneCommunicationException {
        JsonObject reply;
        try {
            reply = JsonParser.parseString(responseJson).getAsJsonObject().getAsJsonObject(replyKey);
        } catch (JsonParseException | IllegalStateException e) {
            throw new AtagOneCommunicationException("Malformed response for " + replyKey + ": " + responseJson, e);
        }
        if (reply == null) {
            throw new AtagOneCommunicationException("Missing " + replyKey + " in response: " + responseJson);
        }
        return reply;
    }

    static void validateComplete(RetrieveReplyDTO result) throws AtagOneCommunicationException {
        if (result.report == null || result.control == null || result.schedules == null
                || result.configuration == null) {
            throw new AtagOneCommunicationException("retrieve_reply missing required section(s)");
        }
        if (result.report.details == null) {
            throw new AtagOneCommunicationException("retrieve_reply.report missing details section");
        }
        if (result.schedules.ch_schedule == null || result.schedules.dhw_schedule == null) {
            throw new AtagOneCommunicationException("retrieve_reply.schedules missing ch_schedule/dhw_schedule");
        }
    }

    public void updateControl(ControlUpdateDTO controlUpdate) throws AtagOneCommunicationException {
        updateControl(controlUpdate, null);
    }

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
        JsonObject reply = parseReplyObject(responseJson, "update_reply");
        int accStatus = reply.has("acc_status") ? reply.get("acc_status").getAsInt() : 0;
        if (accStatus != 2) {
            throw new AtagOneCommunicationException("update denied: acc_status=" + accStatus);
        }
        logger.debug("updateControl() succeeded");
    }

    public void updateChSchedule(ScheduleDTO chSchedule) throws AtagOneCommunicationException {
        updateSchedule("ch_schedule", chSchedule);
    }

    public void updateDhwSchedule(ScheduleDTO dhwSchedule) throws AtagOneCommunicationException {
        updateSchedule("dhw_schedule", dhwSchedule);
    }

    private void updateSchedule(String key, ScheduleDTO schedule) throws AtagOneCommunicationException {
        JsonObject auth = new JsonObject();
        auth.addProperty("user_account", "");
        auth.addProperty("mac_address", clientId);

        JsonObject schedules = new JsonObject();
        schedules.add(key, scheduleToJson(schedule));

        JsonObject updateMsg = new JsonObject();
        updateMsg.addProperty("seqnr", 0);
        updateMsg.add("account_auth", auth);
        updateMsg.add("schedules", schedules);

        JsonObject root = new JsonObject();
        root.add("update_message", updateMsg);

        String responseJson = sendRequest("/update", gson.toJson(root));
        JsonObject reply = parseReplyObject(responseJson, "update_reply");
        int accStatus = reply.has("acc_status") ? reply.get("acc_status").getAsInt() : 0;
        if (accStatus != 2) {
            throw new AtagOneCommunicationException("schedule update denied: acc_status=" + accStatus);
        }
        logger.debug("updateSchedule({}) succeeded", key);
    }

    // Device requires integer start/end in entries ([0,240,20.5]); Gson's float serialization silently wipes the
    // schedule.
    static JsonObject scheduleToJson(ScheduleDTO schedule) {
        JsonObject obj = new JsonObject();
        obj.addProperty("base_temp", schedule.base_temp);
        JsonArray days = new JsonArray();
        for (double[][] day : schedule.entries) {
            JsonArray periods = new JsonArray();
            if (day != null) {
                for (double[] period : day) {
                    JsonArray p = new JsonArray();
                    p.add((long) period[0]);
                    p.add((long) period[1]);
                    p.add(period[2]);
                    periods.add(p);
                }
            }
            days.add(periods);
        }
        obj.add("entries", days);
        return obj;
    }

    private synchronized String sendRequest(String path, String body) throws AtagOneCommunicationException {
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

        // HTTP/1.0 device: Jetty may reuse a stale pooled connection, producing an EOFException on first attempt.
        boolean staleCorrectionUsed = false;
        Exception lastException = new AtagOneCommunicationException("Unreachable");
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                lastRequestMs = System.currentTimeMillis();
                String url = baseUrl + path;
                logger.trace("POST {} request (attempt {}): {}", path, attempt + 1, body);
                byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                ContentResponse response = httpClient.newRequest(url).method(HttpMethod.POST)
                        .version(HttpVersion.HTTP_1_0).header(HttpHeader.CONTENT_TYPE, "application/json")
                        .header(HttpHeader.CONNECTION, "close").content(new BytesContentProvider(bodyBytes))
                        .timeout(REQUEST_TIMEOUT_S, TimeUnit.SECONDS).send();
                String responseContent = response.getContentAsString();
                logger.trace("POST {} → HTTP {}, response: {}", path, response.getStatus(), responseContent);
                return responseContent;
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
