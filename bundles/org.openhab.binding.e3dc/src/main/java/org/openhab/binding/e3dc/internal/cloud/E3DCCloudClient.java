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
package org.openhab.binding.e3dc.internal.cloud;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The E3/DC cloud API, which holds the AI 360 settings and the price based charging of the battery.
 *
 * <p>
 * These values exist in the cloud only: RSCP does not carry them and the device does not know them, so the
 * binding reads and writes them over HTTPS. The API has no login of its own; a token pair is handed out by
 * the SAML login of the portal, so the user seeds one token and the client keeps the pair alive by
 * exchanging it, which also returns a new token that is stored next to the openHAB user data.
 *
 * <p>
 * Every call is a request and a response: the client keeps no state besides the tokens, and returns
 * {@code null} or {@code false} when the cloud does not answer as expected. Callers decide what to publish.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCCloudClient {

    /** Where the installation, storage and tariff data lives. */
    public static final String API_BASE = "https://installations.e3dc.com";

    /** Exchanges a token for a new pair; there is no login endpoint. */
    public static final URI AUTH_ENDPOINT = URI.create("https://e3dc.e3dc.com/auth-saml/re-auth");

    /** The cloud is polled less often than the device, its values change on a schedule. */
    public static final int MIN_POLL_INTERVAL_SECONDS = 60;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final int TOKEN_MARGIN_SECONDS = 60;

    private final Logger logger = LoggerFactory.getLogger(E3DCCloudClient.class);
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private final Gson gson = new Gson();

    private final Path tokenFile;
    private final String installationId;
    private final String storageId;

    private volatile String authToken = "";
    private volatile String refreshToken = "";

    /**
     * @param tokenFile where the current token pair is kept between restarts
     * @param installationId the installation that owns the battery
     * @param storageId the storage whose AI 360 settings are controlled
     * @param seedToken the token from the configuration, used until the client has stored one
     */
    public E3DCCloudClient(Path tokenFile, String installationId, String storageId, String seedToken) {
        this.tokenFile = tokenFile;
        this.installationId = installationId;
        this.storageId = storageId;
        this.refreshToken = seedToken.trim();
        loadStoredTokens();
    }

    /** Whether the client has everything it needs to talk to the cloud. */
    public boolean isUsable() {
        return !installationId.isBlank() && !storageId.isBlank() && !refreshToken.isBlank();
    }

    /** The AI 360 settings of the installation, or {@code null} when they could not be read. */
    public @Nullable JsonObject readEms() {
        return get(emsPath(), "AI 360 settings");
    }

    /** The price based charging settings of the storage, or {@code null} when they could not be read. */
    public @Nullable JsonObject readPrices() {
        return get(pricesPath(), "price settings");
    }

    /** Writes the installation settings; the cloud answers an accepted write without a body. */
    public boolean writeEms(JsonObject body) {
        return put(emsPath(), body, "AI 360 settings");
    }

    /** Writes the storage settings; the cloud answers an accepted write without a body. */
    public boolean writePrices(JsonObject body) {
        return put(pricesPath(), body, "price settings");
    }

    private String emsPath() {
        return "installations/" + installationId + "/dynamic-ems";
    }

    private String pricesPath() {
        return "storages/" + storageId + "/dynamic-power-price";
    }

    private @Nullable JsonObject get(String path, String what) {
        HttpResponse<String> response = send("GET", path, null);
        if (response == null) {
            return null;
        }
        if (response.statusCode() == 204) {
            // the cloud answers with no body when nothing has been configured yet
            return null;
        }
        if (response.statusCode() != 200) {
            logger.debug("Reading the {} failed with HTTP {}", what, response.statusCode());
            return null;
        }
        return parse(response.body());
    }

    private boolean put(String path, JsonObject body, String what) {
        HttpResponse<String> response = send("PUT", path, body);
        if (response == null) {
            return false;
        }
        // an accepted write is answered with 204 and no body
        if (response.statusCode() == 204 || response.statusCode() == 200) {
            return true;
        }
        String detail = response.body() == null ? "" : response.body().strip();
        logger.warn("Writing the {} was rejected with HTTP {}: {}", what, response.statusCode(),
                detail.length() > 200 ? detail.substring(0, 200) : detail);
        return false;
    }

    private @Nullable HttpResponse<String> send(String method, String path, @Nullable JsonObject body) {
        if (!isTokenUsable()) {
            logger.debug("No valid cloud token, call skipped");
            return null;
        }
        HttpResponse<String> response = execute(method, path, body);
        if (response != null && response.statusCode() == 401) {
            // the token expired between the check and the call, or was revoked
            if (reAuth()) {
                response = execute(method, path, body);
            }
        }
        return response;
    }

    private @Nullable HttpResponse<String> execute(String method, String path, @Nullable JsonObject body) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(API_BASE + "/" + path)).timeout(TIMEOUT)
                    .header("authorization", "Bearer " + authToken).header("accept", "application/json");
            if ("GET".equals(method)) {
                request.GET();
            } else if (body == null) {
                request.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                request.header("content-type", "application/json").method(method,
                        HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8));
            }
            return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            logger.debug("Cloud request {} {} failed: {}", method, path, e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("Cloud request {} {} was interrupted", method, path);
            return null;
        }
    }

    private boolean isTokenUsable() {
        if (refreshToken.isBlank()) {
            return false;
        }
        Instant expiry = expiry(authToken);
        if (!authToken.isBlank() && expiry != null && expiry.isAfter(Instant.now().plusSeconds(TOKEN_MARGIN_SECONDS))) {
            return true;
        }
        return reAuth();
    }

    /**
     * Exchanges the current token for a new pair and stores it.
     *
     * <p>
     * The cloud hands out the successor of a token, so the stored pair replaces the configured one. A
     * configured token therefore only has to be renewed when the chain is broken, for example by logging
     * out of the portal.
     */
    synchronized boolean reAuth() {
        if (refreshToken.isBlank()) {
            return false;
        }
        String form = "reAuthToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
        try {
            HttpRequest request = HttpRequest.newBuilder(AUTH_ENDPOINT).timeout(TIMEOUT)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.warn("The E3/DC cloud token could not be renewed (HTTP {}), please copy a fresh token from"
                        + " https://my.e3dc.com into the bridge configuration", response.statusCode());
                return false;
            }
            JsonObject tokens = parse(response.body());
            if (tokens == null) {
                return false;
            }
            String token = string(tokens, "token");
            String refresh = string(tokens, "reAuthToken");
            if (token.isBlank() || refresh.isBlank()) {
                logger.warn("The E3/DC cloud answered the token renewal without a token pair");
                return false;
            }
            authToken = token;
            refreshToken = refresh;
            storeTokens();
            return true;
        } catch (IOException e) {
            logger.debug("Renewing the cloud token failed: {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void loadStoredTokens() {
        try {
            if (!Files.isReadable(tokenFile)) {
                return;
            }
            JsonObject stored = parse(Files.readString(tokenFile));
            if (stored == null) {
                return;
            }
            String token = string(stored, "authToken");
            String refresh = string(stored, "reAuthToken");
            if (!token.isBlank() && !refresh.isBlank()) {
                authToken = token;
                refreshToken = refresh;
            }
        } catch (IOException e) {
            logger.debug("Stored cloud tokens could not be read: {}", e.getMessage());
        }
    }

    private void storeTokens() {
        JsonObject tokens = new JsonObject();
        tokens.addProperty("authToken", authToken);
        tokens.addProperty("reAuthToken", refreshToken);
        try {
            Path folder = tokenFile.getParent();
            if (folder != null) {
                Files.createDirectories(folder);
            }
            Files.writeString(tokenFile, tokens.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // the tokens stay valid in memory, only a restart needs the configured token again
            logger.debug("Cloud tokens could not be stored: {}", e.getMessage());
        }
    }

    /** The expiry of a JWT, or {@code null} when it cannot be read. */
    static @Nullable Instant expiry(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonObject claims = parse(payload);
            if (claims == null) {
                return null;
            }
            if (!claims.has("exp") || !claims.get("exp").isJsonPrimitive()) {
                return null;
            }
            return Instant.ofEpochSecond(claims.get("exp").getAsLong());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static @Nullable JsonObject parse(@Nullable String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            return null;
        }
    }

    private static String string(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }
}
