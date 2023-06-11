/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nuki.internal.constants;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

/**
 * The {@link NukiLinkBuilder} class helps with constructing links to various Nuki APIs.
 * Links to secured APIs will be created with all necessary authentication parameters.
 *
 * @author Jan Vybíral - Initial contribution
 */
@NonNullByDefault
public class NukiLinkBuilder {
    public static final URI URI_BRIDGE_DISCOVERY = URI.create("https://api.nuki.io/discover/bridges");
    public static final String CALLBACK_ENDPOINT = "/nuki/bcb";

    private static final String PATH_INFO = "/info";
    private static final String PATH_AUTH = "/auth";
    private static final String PATH_LOCKSTATE = "/lockState";
    private static final String PATH_LOCKACTION = "/lockAction";
    public static final String PATH_CBADD = "/callback/add";
    public static final String PATH_CBLIST = "/callback/list";
    public static final String PATH_CBREMOVE = "/callback/remove";
    public static final String PATH_LIST = "/list";

    private final String host;
    private final int port;
    private final String token;
    private final boolean secureToken;
    private final SecureRandom random = new SecureRandom();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

    /**
     * Create new instance of link builder
     * 
     * @param host Hostname/ip address of Nuki bridge
     * @param port Port of Nuki bridge
     * @param token Token for authenticating API calls
     */
    public NukiLinkBuilder(String host, int port, String token, boolean secureToken) {
        this.host = host;
        this.port = port;
        this.token = token;
        this.secureToken = secureToken;
    }

    public static URI getAuthUri(String host, int port) {
        return UriBuilder.fromPath(PATH_AUTH).host(host).port(port).scheme("http").build();
    }

    private UriBuilder builder(String path) {
        return UriBuilder.fromPath(path).scheme("http").host(host).port(port);
    }

    public URI info() {
        return buildWithAuth(builder(PATH_INFO));
    }

    public URI lockState(String nukiId, int deviceType) {
        return buildWithAuth(builder(PATH_LOCKSTATE).queryParam("nukiId", nukiId).queryParam("deviceType", deviceType));
    }

    public URI lockAction(String nukiId, int deviceType, int action) {
        return buildWithAuth(builder(PATH_LOCKACTION).queryParam("deviceType", deviceType).queryParam("action", action)
                .queryParam("nukiId", nukiId));
    }

    public URI callbackList() {
        return buildWithAuth(builder(PATH_CBLIST));
    }

    public URI callbackAdd(String callback) {
        String callbackEncoded = URLEncoder.encode(callback, StandardCharsets.UTF_8);
        return buildWithAuth(builder(PATH_CBADD).queryParam("url", callbackEncoded));
    }

    public URI callbackRemove(int id) {
        return buildWithAuth(builder(PATH_CBREMOVE).queryParam("id", id));
    }

    public URI list() {
        return buildWithAuth(builder(PATH_LIST));
    }

    public static UriBuilder callbackPath(String callbackId) {
        return UriBuilder.fromPath(CALLBACK_ENDPOINT).queryParam("callbackId", callbackId);
    }

    public static URI callbackUri(String host, int port, String callbackId) {
        return callbackPath(callbackId).host(host).port(port).scheme("http").build();
    }

    private URI buildWithAuth(UriBuilder builder) {
        if (secureToken) {
            return buildWithHashedToken(builder);
        } else {
            return buildWithPlainToken(builder);
        }
    }

    private URI buildWithHashedToken(UriBuilder builder) {
        String ts = formatter.format(OffsetDateTime.now(ZoneOffset.UTC));
        Integer rnr = random.nextInt(65536);
        String hashedToken = sha256(ts + "," + rnr + "," + token);

        return builder.queryParam("ts", ts).queryParam("rnr", rnr).queryParam("hash", hashedToken).build();
    }

    private URI buildWithPlainToken(UriBuilder builder) {
        return builder.queryParam("token", token).build();
    }

    public static String sha256(String data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 Algorithm not supported", e);
        }
        byte[] rawHash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return HexUtils.bytesToHex(rawHash).toLowerCase(Locale.ROOT);
    }
}
