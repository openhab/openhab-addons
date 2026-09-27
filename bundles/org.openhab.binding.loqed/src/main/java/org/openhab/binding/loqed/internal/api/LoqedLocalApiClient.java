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
package org.openhab.binding.loqed.internal.api;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.loqed.internal.LoqedLocalConfiguration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Client for the API exposed by a LOQED bridge on the local network.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedLocalApiClient {
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int TIMEOUT_SECONDS = 15;
    private static final int WEBHOOK_FLAGS = 0x18F;
    private static final Gson GSON = new Gson();

    private final HttpClient httpClient;
    private final String baseUrl;
    private final byte[] bridgeKey;

    public LoqedLocalApiClient(HttpClient httpClient, LoqedLocalConfiguration config)
            throws LoqedConfigurationException {
        this.httpClient = httpClient;
        baseUrl = normalizeHost(config.host);
        try {
            bridgeKey = Base64.getDecoder().decode(config.bridgeKey);
        } catch (IllegalArgumentException e) {
            throw new LoqedConfigurationException("The LOQED bridge key is not valid Base64", e);
        }
    }

    public LoqedLockData getStatus() throws LoqedApiException {
        return parseStatus(send(baseUrl + "/status", HttpMethod.GET, null).getContentAsString());
    }

    public void setBoltState(String keySecret, int localKeyId, BoltState boltState) throws LoqedApiException {
        if (boltState == BoltState.UNKNOWN) {
            throw new LoqedConfigurationException("Unsupported LOQED bolt state: " + boltState);
        }
        long timestamp = Instant.now().getEpochSecond();
        byte[] command = createSignedCommand(keySecret, localKeyId, boltState.localAction(), timestamp);
        String encoded = URLEncoder.encode(Base64.getEncoder().encodeToString(command), StandardCharsets.UTF_8);
        send(baseUrl + "/to_lock?command_signed_base64=" + encoded, HttpMethod.GET, null);
    }

    /**
     * Ensures that exactly one webhook exists for the callback route and returns its bridge-assigned identifier.
     *
     * @param callbackUrl complete callback URL
     * @return webhook identifier assigned by the bridge
     * @throws LoqedApiException if the webhook cannot be listed, created, or reconciled
     */
    public long ensureWebhook(String callbackUrl) throws LoqedApiException {
        List<Webhook> webhooks = listWebhooks();
        @Nullable
        Webhook currentWebhook = null;
        for (Webhook webhook : webhooks) {
            if (callbackUrl.equals(webhook.url)) {
                if (currentWebhook == null) {
                    currentWebhook = webhook;
                } else {
                    removeWebhook(webhook.id);
                }
            } else if (hasSameCallbackPath(webhook.url, callbackUrl)) {
                removeWebhook(webhook.id);
            }
        }
        if (currentWebhook != null) {
            return currentWebhook.id;
        }

        createWebhook(callbackUrl);
        return listWebhooks().stream().filter(webhook -> callbackUrl.equals(webhook.url)).mapToLong(Webhook::id)
                .findFirst()
                .orElseThrow(() -> new LoqedResponseException("The LOQED local bridge did not return the new webhook"));
    }

    /**
     * Removes a webhook from the local bridge.
     *
     * @param webhookId bridge-assigned webhook identifier
     * @throws LoqedApiException if the webhook cannot be removed
     */
    public void removeWebhook(long webhookId) throws LoqedApiException {
        long timestamp = Instant.now().getEpochSecond();
        String deleteHash = createWebhookDeleteHash(webhookId, timestamp);
        sendWebhookRequest(HttpMethod.DELETE, baseUrl + "/webhooks/" + webhookId, null, timestamp, deleteHash);
    }

    /** Returns whether another client communicates with the same local bridge endpoint. */
    public boolean connectsToSameBridge(LoqedLocalApiClient other) {
        return baseUrl.equals(other.baseUrl);
    }

    /**
     * Applies a local webhook payload to the latest known lock status.
     *
     * @param currentStatus latest known lock status
     * @param event webhook payload
     * @return {@code true} when the lock transitioned from offline to online and requires a full status refresh
     */
    public static boolean applyWebhook(LoqedLockData currentStatus, JsonObject event) {
        boolean wasOnline = currentStatus.online;
        String requestedState = getString(event, "requested_state", "");
        if (!requestedState.isEmpty()) {
            BoltState.fromApiValue(requestedState).ifPresent(state -> currentStatus.boltState = state);
        }

        JsonElement batteryPercentage = event.get("battery_percentage");
        if (batteryPercentage != null && batteryPercentage.isJsonPrimitive()) {
            currentStatus.batteryPercentage = batteryPercentage.getAsInt();
        }
        JsonElement batteryType = event.get("battery_type");
        if (batteryType != null) {
            currentStatus.batteryType = batteryType(batteryType);
        }
        JsonElement bleStrength = event.get("ble_strength");
        if (bleStrength != null && bleStrength.isJsonPrimitive()) {
            currentStatus.online = bleStrength.getAsInt() != -1;
        }
        boolean becameOnline = !wasOnline && currentStatus.online;
        if (becameOnline) {
            currentStatus.boltState = BoltState.UNKNOWN;
            currentStatus.batteryPercentage = -1;
        }
        return becameOnline;
    }

    private List<Webhook> listWebhooks() throws LoqedApiException {
        long timestamp = Instant.now().getEpochSecond();
        String listHash = sha256Hex(longBytes(timestamp), bridgeKey);
        ContentResponse response = sendWebhookRequest(HttpMethod.GET, baseUrl + "/webhooks", null, timestamp, listHash);
        try {
            JsonElement responseBody = GSON.fromJson(response.getContentAsString(), JsonElement.class);
            if (responseBody == null || !responseBody.isJsonArray()) {
                throw new LoqedResponseException("The LOQED local bridge returned an invalid webhook list");
            }
            return responseBody.getAsJsonArray().asList().stream().filter(JsonElement::isJsonObject)
                    .map(JsonElement::getAsJsonObject)
                    .map(webhook -> new Webhook(getLong(webhook, "id", -1), getString(webhook, "url", "")))
                    .filter(webhook -> webhook.id >= 0 && !webhook.url.isEmpty()).toList();
        } catch (JsonParseException | IllegalStateException | NumberFormatException e) {
            throw new LoqedResponseException("The LOQED local bridge returned an invalid webhook list", e);
        }
    }

    private void createWebhook(String callbackUrl) throws LoqedApiException {
        JsonObject body = new JsonObject();
        body.addProperty("url", callbackUrl);
        body.addProperty("trigger_state_changed_open", 1);
        body.addProperty("trigger_state_changed_latch", 1);
        body.addProperty("trigger_state_changed_night_lock", 1);
        body.addProperty("trigger_state_changed_unknown", 1);
        body.addProperty("trigger_state_goto_open", 0);
        body.addProperty("trigger_state_goto_latch", 0);
        body.addProperty("trigger_state_goto_night_lock", 0);
        body.addProperty("trigger_battery", 1);
        body.addProperty("trigger_online_status", 1);

        long timestamp = Instant.now().getEpochSecond();
        String createHash = sha256Hex(callbackUrl.getBytes(StandardCharsets.UTF_8), intBytes(WEBHOOK_FLAGS),
                longBytes(timestamp), bridgeKey);
        sendWebhookRequest(HttpMethod.POST, baseUrl + "/webhooks", GSON.toJson(body), timestamp, createHash);
    }

    public boolean verifyWebhook(byte[] body, String timestampHeader, String hashHeader) {
        try {
            long timestamp = Long.parseLong(timestampHeader);
            if (Math.abs(Instant.now().getEpochSecond() - timestamp) > 10) {
                return false;
            }
            byte[] expected = HexFormat.of().parseHex(sha256Hex(body, longBytes(timestamp), bridgeKey));
            byte[] supplied = HexFormat.of().parseHex(hashHeader);
            return MessageDigest.isEqual(expected, supplied);
        } catch (IllegalArgumentException | LoqedApiException e) {
            return false;
        }
    }

    byte[] createSignedCommand(String keySecret, int localKeyId, int action, long timestamp) throws LoqedApiException {
        byte[] payload = ByteBuffer.allocate(13).put((byte) 2).put((byte) 7).putLong(timestamp).put((byte) localKeyId)
                .put((byte) 1).put((byte) action).array();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(Base64.getDecoder().decode(keySecret), "HmacSHA256"));
            byte[] signature = mac.doFinal(payload);
            return ByteBuffer.allocate(53).putLong(0).put((byte) 2).put((byte) 7).putLong(timestamp).put(signature)
                    .put((byte) localKeyId).put((byte) 1).put((byte) action).array();
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException e) {
            throw new LoqedConfigurationException("Could not sign the LOQED local command", e);
        }
    }

    String createWebhookDeleteHash(long webhookId, long timestamp) throws LoqedApiException {
        return sha256Hex(longBytes(webhookId), longBytes(timestamp), bridgeKey);
    }

    private ContentResponse sendWebhookRequest(HttpMethod method, String url, @Nullable String body, long timestamp,
            String hash) throws LoqedApiException {
        try {
            var request = httpClient.newRequest(url).method(method).header("TIMESTAMP", Long.toString(timestamp))
                    .header("HASH", hash).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (body != null) {
                request.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .content(new StringContentProvider(body, StandardCharsets.UTF_8));
            }
            ContentResponse response = request.send();
            if (!HttpStatus.isSuccess(response.getStatus())) {
                throw responseException(response);
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoqedCommunicationException("Communication with the LOQED local bridge was interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new LoqedCommunicationException("Could not communicate with the LOQED local bridge", e);
        }
    }

    private ContentResponse send(String url, HttpMethod method, @Nullable String body) throws LoqedApiException {
        try {
            var request = httpClient.newRequest(url).method(method).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (body != null) {
                request.content(new StringContentProvider(body, StandardCharsets.UTF_8));
            }
            ContentResponse response = request.send();
            if (!HttpStatus.isSuccess(response.getStatus())) {
                throw responseException(response);
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoqedCommunicationException("Communication with the LOQED local bridge was interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new LoqedCommunicationException("Could not communicate with the LOQED local bridge", e);
        }
    }

    private static LoqedApiException responseException(ContentResponse response) {
        int statusCode = response.getStatus();
        if (statusCode == HttpStatus.UNAUTHORIZED_401 || statusCode == HttpStatus.FORBIDDEN_403) {
            return new LoqedAuthenticationException(statusCode);
        }
        return new LoqedResponseException(statusCode, response.getContentAsString());
    }

    static LoqedLockData parseStatus(String json) throws LoqedResponseException {
        try {
            @Nullable
            JsonElement element = GSON.fromJson(json, JsonElement.class);
            if (element == null || !element.isJsonObject()) {
                throw new LoqedResponseException("The LOQED local bridge returned an invalid response");
            }
            JsonObject status = element.getAsJsonObject();
            LoqedLockData lock = new LoqedLockData();
            lock.modelName = "LOQED Smart Lock";
            lock.batteryPercentage = getInt(status, "battery_percentage", -1);
            lock.batteryType = batteryType(status.get("battery_type"));
            lock.boltState = BoltState.fromApiValueOrUnknown(getString(status, "bolt_state", "unknown"));
            lock.online = getBoolean(status, "lock_online");
            lock.partyMode = getOptionalBoolean(status, "party_mode");
            lock.guestAccessMode = getOptionalBoolean(status, "guest_access_mode");
            lock.twistAssist = getOptionalBoolean(status, "twist_assist");
            lock.touchToConnect = getOptionalBoolean(status, "touch_to_connect");
            return lock;
        } catch (JsonParseException | IllegalStateException | NumberFormatException e) {
            throw new LoqedResponseException("The LOQED local bridge returned invalid JSON", e);
        }
    }

    private static int getInt(JsonObject object, String name, int defaultValue) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() ? value.getAsInt() : defaultValue;
    }

    private static long getLong(JsonObject object, String name, long defaultValue) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() ? value.getAsLong() : defaultValue;
    }

    private static boolean getBoolean(JsonObject object, String name) {
        return getInt(object, name, 0) != 0;
    }

    private static @Nullable Boolean getOptionalBoolean(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() ? value.getAsInt() != 0 : null;
    }

    private static String getString(JsonObject object, String name, String defaultValue) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : defaultValue;
    }

    private static String batteryType(@Nullable JsonElement value) {
        if (value == null || !value.isJsonPrimitive()) {
            return "unknown";
        }
        if (value.getAsJsonPrimitive().isNumber()) {
            return switch (value.getAsInt()) {
                case 0 -> "alkaline";
                case 1 -> "nimh";
                case 2 -> "lithium";
                default -> "unknown";
            };
        }
        return value.getAsString();
    }

    private static String normalizeHost(String host) {
        String result = host.strip();
        if (!result.startsWith("http://") && !result.startsWith("https://")) {
            result = "http://" + result;
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    static boolean hasSameCallbackPath(String registeredUrl, String callbackUrl) {
        try {
            return Objects.equals(URI.create(registeredUrl).getPath(), URI.create(callbackUrl).getPath());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] intBytes(int value) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
    }

    private static byte[] longBytes(long value) {
        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    }

    private static String sha256Hex(byte[]... parts) throws LoqedApiException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (byte[] part : parts) {
                digest.update(part);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new LoqedConfigurationException("Could not calculate the LOQED authentication hash", e);
        }
    }

    private record Webhook(long id, String url) {
    }
}
