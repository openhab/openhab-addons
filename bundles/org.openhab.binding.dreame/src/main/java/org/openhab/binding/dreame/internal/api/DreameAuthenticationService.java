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
package org.openhab.binding.dreame.internal.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Owns cloud authentication state and access-token refresh decisions.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameAuthenticationService {
    private static final String PASSWORD_SALT = "RAylYC%fmSKp7%Tq";
    private static final Set<String> CLOUD_REGIONS = Set.of("cn", "eu", "in", "ru", "sg", "us");
    private static final Set<String> EUROPEAN_COUNTRIES = Set.of("ad", "al", "at", "ba", "be", "bg", "by", "ch", "cy",
            "cz", "de", "dk", "ee", "es", "fi", "fr", "gb", "gr", "hr", "hu", "ie", "is", "it", "li", "lt", "lu", "lv",
            "mc", "md", "me", "mk", "mt", "nl", "no", "pl", "pt", "ro", "rs", "se", "si", "sk", "sm", "ua", "va");

    private final Logger logger = LoggerFactory.getLogger(DreameAuthenticationService.class);
    private final Clock clock;

    private String country = "";
    private DreameCloudService cloudService = DreameCloudService.DREAMEHOME;
    private String accessToken = "";
    private String refreshToken = "";
    private String tenantId = "000000";
    private String userId = "";
    private Instant tokenExpires = Instant.EPOCH;

    public DreameAuthenticationService() {
        this(Clock.systemUTC());
    }

    DreameAuthenticationService(Clock clock) {
        this.clock = clock;
    }

    public void login(String username, String password, String location, TokenRequester requester)
            throws DreameCloudException {
        login(username, password, location, DreameCloudService.DREAMEHOME, requester);
    }

    public void login(String username, String password, String location, DreameCloudService cloudService,
            TokenRequester requester) throws DreameCloudException {
        String normalized = normalizeCountry(location);
        String countryCode = CLOUD_REGIONS.contains(normalized) ? "" : normalized;
        this.cloudService = cloudService;
        tenantId = cloudService.tenantId();
        country = cloudRegion(normalized);
        logger.debug("Authenticating with {} region {}", cloudService.label(), country);
        authenticate(createPasswordRequestBody(username, password, countryCode), requester);
    }

    public void ensureAuthenticated(TokenRequester requester) throws DreameCloudException {
        if (accessToken.isBlank()) {
            throw new DreameCloudException("Not authenticated with " + cloudService.label());
        }
        if (!Instant.now(clock).isBefore(tokenExpires)) {
            if (refreshToken.isBlank()) {
                throw new DreameCloudException(cloudService.label() + " access token expired");
            }
            authenticate("platform=IOS&scope=all&grant_type=refresh_token&refresh_token=" + refreshToken, requester);
        }
    }

    private void authenticate(String body, TokenRequester requester) throws DreameCloudException {
        JsonObject response = requester.request(body);
        String token = stringValue(response, "access_token");
        if (token.isBlank()) {
            throw new DreameCloudException(cloudService.label() + " login failed");
        }
        accessToken = token;
        userId = defaultIfBlank(stringValue(response, "uid"), userId);
        refreshToken = stringValue(response, "refresh_token");
        tenantId = defaultIfBlank(stringValue(response, "tenant_id"), tenantId);
        country = cloudRegion(defaultIfBlank(stringValue(response, "region"), country));
        long expiresIn = response.has("expires_in") ? response.get("expires_in").getAsLong() : 3600;
        tokenExpires = Instant.now(clock).plusSeconds(Math.max(0, expiresIn - 120));
        logger.debug("{} authentication succeeded for region {}; token lifetime is {} seconds", cloudService.label(),
                country, expiresIn);
    }

    public void logout() {
        country = "";
        cloudService = DreameCloudService.DREAMEHOME;
        accessToken = "";
        refreshToken = "";
        tenantId = "000000";
        userId = "";
        tokenExpires = Instant.EPOCH;
    }

    public String country() {
        return country;
    }

    public DreameCloudService cloudService() {
        return cloudService;
    }

    public String accessToken() {
        return accessToken;
    }

    public String tenantId() {
        return tenantId;
    }

    public String userId() {
        return userId;
    }

    static String createPasswordRequestBody(String username, String password, String countryCode)
            throws DreameCloudException {
        String location = countryCode.isBlank() ? ""
                : "&country=" + countryCode.toUpperCase(Locale.ROOT) + "&lang=" + countryCode;
        return "platform=IOS&scope=all&grant_type=password&username=" + username + "&password="
                + md5(password + PASSWORD_SALT) + "&type=account" + location;
    }

    static String cloudRegion(String country) throws DreameCloudException {
        String normalized = normalizeCountry(country);
        if (CLOUD_REGIONS.contains(normalized)) {
            return normalized;
        }
        if (EUROPEAN_COUNTRIES.contains(normalized)) {
            return "eu";
        }
        throw new DreameCloudException("Unsupported cloud country or region: " + country);
    }

    private static String normalizeCountry(String value) throws DreameCloudException {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new DreameCloudException("Cloud country or region must not be empty");
        }
        return normalized;
    }

    private static String md5(String value) throws DreameCloudException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new DreameCloudException("MD5 is unavailable", e);
        }
    }

    private static String stringValue(JsonObject object, String name) {
        return object.has(name) && !object.get(name).isJsonNull() ? object.get(name).getAsString() : "";
    }

    private static String defaultIfBlank(String value, String fallback) {
        return value.isBlank() ? fallback : value;
    }

    @FunctionalInterface
    public interface TokenRequester {
        JsonObject request(String body) throws DreameCloudException;
    }
}
