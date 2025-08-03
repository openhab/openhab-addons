/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.ring.internal.api.ProfileTO;
import org.openhab.binding.ring.internal.api.RingDevicesTO;
import org.openhab.binding.ring.internal.api.RingEventTO;
import org.openhab.binding.ring.internal.api.SessionTO;
import org.openhab.binding.ring.internal.api.TokenTO;
import org.openhab.binding.ring.internal.data.ParamBuilder;
import org.openhab.binding.ring.internal.data.Tokens;
import org.openhab.binding.ring.internal.errors.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * @author Wim Vissers - Initial contribution
 * @author Pete Mietlowski - Updated authentication routines
 * @author Chris Milbert - Stickupcam contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 * @author Jan N. Klug - Refactored to use Jetty client
 */

@NonNullByDefault
public class RestClient {
    public static final Type RING_EVENT_LIST_TYPE = new TypeToken<List<RingEventTO>>() {
    }.getType();
    private static final int CONNECTION_TIMEOUT = 12000;

    private final Logger logger = LoggerFactory.getLogger(RestClient.class);
    private final Gson gson = new Gson();

    private final HttpClient httpClient;

    public RestClient(HttpClient httpClient) {
        logger.debug("Creating Ring client for API version {} on endPoint {}", ApiConstants.API_VERSION,
                ApiConstants.API_BASE);
        this.httpClient = httpClient;
    }

    /**
     * Post data to given url
     *
     * @param endpoint the endpoint
     * @param content the body content of this request
     * @param additionalHeaders a {@link Map} containing additional headers for this request
     * @param tokens the tokens for this session
     * @return the servers response
     * @throws AuthenticationException in case of an error
     */
    private String postRequest(String endpoint, String content, Map<String, String> additionalHeaders,
            @Nullable Tokens tokens) throws AuthenticationException {
        String result = "";
        try {
            Request request = httpClient.newRequest(endpoint);
            request.method(HttpMethod.POST);
            request.content(new StringContentProvider(content, StandardCharsets.UTF_8));
            request.timeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            request.agent(ApiConstants.API_USER_AGENT);
            request.header("X-API-LANG", "en");
            request.header(HttpHeader.CONTENT_TYPE.asString(), "application/x-www-form-urlencoded");
            if (tokens != null) {
                request.header(HttpHeader.AUTHORIZATION.asString(), "Bearer " + tokens.accessToken());
            }
            additionalHeaders.forEach(request::header);

            ContentResponse response = request.send();
            int responseCode = response.getStatus();
            switch (responseCode) {
                case HttpStatus.OK_200, HttpStatus.CREATED_201:
                    break;
                case HttpStatus.BAD_REQUEST_400:
                    throw new AuthenticationException("Bad request");
                case HttpStatus.UNAUTHORIZED_401:
                    throw new AuthenticationException("Invalid username or password");
                case HttpStatus.PRECONDITION_FAILED_412:
                    if (response.getReason().startsWith("Precondition")
                            || response.getReason().startsWith("Verification Code")) {
                        throw new AuthenticationException("Two factor authentication enabled, enter code");
                    } else {
                        throw new AuthenticationException("Invalid username or password");
                    }
                case HttpStatus.TOO_MANY_REQUESTS_429:
                    throw new AuthenticationException("Account rate-limited");
                default:
                    throw new AuthenticationException(
                            "Unhandled HTTP error: " + responseCode + " - " + response.getReason());
            }

            result = response.getContentAsString();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.warn("RestApi error in postRequest!", e);
            Thread.currentThread().interrupt();
        }
        return result;
    }

    private String getRequest(String endpoint, Map<String, String> additionalHeaders, Tokens tokens)
            throws AuthenticationException {
        String result = "";
        try {
            Request request = httpClient.newRequest(endpoint);
            request.method(HttpMethod.GET);
            request.timeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            request.agent(ApiConstants.API_USER_AGENT);
            request.header("X-API-LANG", "en");
            request.header(HttpHeader.CONTENT_TYPE.asString(), "application/x-www-form-urlencoded");
            request.header(HttpHeader.AUTHORIZATION.asString(), "Bearer " + tokens.accessToken());
            additionalHeaders.forEach(request::header);

            ContentResponse response = request.send();
            int responseCode = response.getStatus();
            switch (responseCode) {
                case HttpStatus.OK_200, HttpStatus.CREATED_201:
                    break;
                case HttpStatus.BAD_REQUEST_400:
                    throw new AuthenticationException("Bad request");
                case HttpStatus.UNAUTHORIZED_401:
                    throw new AuthenticationException("Invalid username or password");
                case HttpStatus.TOO_MANY_REQUESTS_429:
                    throw new AuthenticationException("Account rate-limited");
                default:
                    throw new AuthenticationException(
                            "Unhandled HTTP error: " + responseCode + " - " + response.getReason());
            }

            result = response.getContentAsString();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.warn("RestApi error in getRequest!", e);
            Thread.currentThread().interrupt();
        }
        return result;
    }

    /**
     * Get the required authentication tokens.
     *
     * @param username the username of the Ring account.
     * @param password the password for the Ring account.
     * @param hardwareId a hardware ID (must be unique for every piece of hardware used).
     * @return the tokens
     * @throws AuthenticationException
     * @throws JsonParseException
     */
    public Tokens getTokens(String username, String password, String refreshToken, String twofactorCode,
            String hardwareId) throws AuthenticationException, JsonParseException {
        Map<String, String> additionalHeaders = new HashMap<>();
        ParamBuilder pb = new ParamBuilder(true);
        pb.add("client_id", "ring_official_android");
        pb.add("scope", "client");
        if (refreshToken.isBlank()) {
            logger.debug("getOauthToken - refreshToken not set - trying username/password");
            pb.add("grant_type", "password");
            pb.add("username", username);
            pb.add("password", password);
            if (!twofactorCode.isBlank()) {
                additionalHeaders.put("2fa-support", "true");
                additionalHeaders.put("2fa-code", twofactorCode);
            }
            additionalHeaders.put("hardware_id", hardwareId);
        } else {
            logger.debug("getOauthToken - refreshToken available");
            pb.add("grant_type", "refresh_token");
            pb.add("refresh_token", refreshToken);
        }

        String response = postRequest(ApiConstants.API_OAUTH_ENDPOINT, pb.toString(), additionalHeaders, null);
        TokenTO token = Objects.requireNonNull(new Gson().fromJson(response, TokenTO.class));

        return new Tokens(token.refreshToken, token.accessToken);
    }

    public ProfileTO getProfile(String hardwareId, Tokens tokens) throws AuthenticationException, JsonParseException {
        ParamBuilder pb = new ParamBuilder(false);
        pb.add("device[os]", "android");
        pb.add("device[hardware_id]", hardwareId);
        pb.add("device[app_brand]", "ring");
        pb.add("device[metadata][device_model]", "VirtualBox");
        pb.add("device[metadata][resolution]", "600x800");
        pb.add("device[metadata][app_version]", "1.7.29");
        pb.add("device[metadata][app_installation_date]", "");
        pb.add("device[metadata][os_version]", "4.4.4");
        pb.add("device[metadata][manufacturer]", "innotek GmbH");
        pb.add("device[metadata][is_tablet]", "true");
        pb.add("device[metadata][linphone_initialized]", "true");
        pb.add("device[metadata][language]", "en");
        pb.add("api_version", "" + ApiConstants.API_VERSION);
        String jsonResult = postRequest(ApiConstants.URL_SESSION, pb.toString(), Map.of(), tokens);
        SessionTO session = Objects.requireNonNull(gson.fromJson(jsonResult, SessionTO.class));
        return session.profile;
    }

    /**
     * Get get the Ring devices
     *
     * @param tokens the tokens previously retrieved when authenticating.
     * @return the RingDevices instance filled with all available data.
     * @throws AuthenticationException when request is invalid.
     * @throws JsonParseException when response is invalid JSON.
     */
    public RingDevicesTO getRingDevices(Tokens tokens) throws JsonParseException, AuthenticationException {
        logger.debug("RestClient - getRingDevices");
        String jsonResult = getRequest(ApiConstants.URL_DEVICES, Map.of(), tokens);
        return Objects.requireNonNull(gson.fromJson(jsonResult, RingDevicesTO.class));
    }

    /**
     * Get a List with the last recorded events, newest on top.
     *
     * @param tokens the tokens previously retrieved when authenticating.
     * @param limit the maximum number of events.
     * @return
     * @throws AuthenticationException
     * @throws JsonParseException
     */
    public synchronized List<RingEventTO> getHistory(Tokens tokens, int limit)
            throws AuthenticationException, JsonParseException {
        String jsonResult = getRequest(ApiConstants.URL_HISTORY + "?limit=" + limit, Map.of(), tokens);
        if (!jsonResult.isBlank()) {
            return Objects.requireNonNull(gson.fromJson(jsonResult, RING_EVENT_LIST_TYPE));
        } else {
            return List.of();
        }
    }

    public String downloadEventVideo(RingEventTO event, Tokens tokens, String filePath, int retentionCount) {
        try {
            Path path = Paths.get(filePath);

            try {
                Files.createDirectories(path.toAbsolutePath());
            } catch (IOException e) {
                logger.error("RingVideo: Unable to create folder {}, cannot download.: {}", filePath, e.getMessage());
                return "";
            }
            if (retentionCount > 0 && Files.exists(path)) {
                // get FileSystem object
                FileSystem fs = path.getFileSystem();
                String sep = fs.getSeparator();
                String filename = event.doorbot.description.replace(" ", "") + "-" + event.kind + "-"
                        + event.getCreatedAt().toString().replace(":", "-") + ".mp4";
                String fullfilepath = filePath + (filePath.endsWith(sep) ? "" : sep) + filename;
                logger.debug("fullfilepath = {}", fullfilepath);
                path = Paths.get(fullfilepath);
                boolean urlFound = false;
                if (Files.notExists(path)) {
                    long eventId = event.id;
                    StringBuilder vidUrl = new StringBuilder();
                    vidUrl.append(ApiConstants.URL_RECORDING_START).append(eventId)
                            .append(ApiConstants.URL_RECORDING_END);
                    for (int i = 0; i < 10; i++) {
                        try {
                            String jsonResult = getRequest(vidUrl.toString(), Map.of(), tokens);
                            JsonObject obj = JsonParser.parseString(jsonResult).getAsJsonObject();
                            if (obj.get("url").getAsString().startsWith("http")) {
                                URL url = new URI(obj.get("url").getAsString()).toURL();
                                InputStream in = url.openStream();
                                Files.copy(in, Paths.get(fullfilepath), StandardCopyOption.REPLACE_EXISTING);
                                in.close();
                                logger.info("fullfilepath.length() = {}", fullfilepath.length());
                                if (!fullfilepath.isEmpty()) {
                                    urlFound = true;
                                    break;
                                }
                            }
                        } catch (AuthenticationException | URISyntaxException e) {
                            logger.debug("RingVideo: Error downloading file: {}", e.getMessage());
                        } finally {
                            Thread.sleep(15000);
                        }
                    }
                }
                if (urlFound) {
                    File directory = new File(filePath);
                    File[] logFiles = directory.listFiles();
                    long oldestDate = Long.MAX_VALUE;
                    File oldestFile = null;
                    if (logFiles != null && logFiles.length > retentionCount) {
                        // delete oldest files after there's more than the specified number of files
                        for (File f : logFiles) {
                            if (f.lastModified() < oldestDate) {
                                oldestDate = f.lastModified();
                                oldestFile = f;
                            }
                        }

                        if (oldestFile != null) {
                            oldestFile.delete();
                        }
                    }
                    return filename;
                } else {
                    return "Video not available on ring.com";
                }
            } else if (retentionCount == 0) {
                return "videoRetentionCount = 0, Auto downloading disabled";
            } else {
                return "";
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("RingVideo: Unable to process request: {}", e.getMessage());
            return "";
        }
    }
}
