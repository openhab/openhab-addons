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
package org.openhab.binding.avmfritz.internal.hardware;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.avmfritz.internal.config.AVMFritzBoxConfiguration;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzBaseBridgeHandler;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzPowerMeterDeviceHandler;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaApplyTemplateCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaGetEnergyStatsCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetBlindTargetCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetBlindTargetCallback.BlindCommand;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetColorCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetColorTemperatureCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetHeatingModeCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetHeatingTemperatureCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetLevelPercentageCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetSwitchCallback;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles requests to a FRITZ!OS web interface for interfacing with AVM home automation devices. It manages
 * authentication and wraps commands.
 *
 * @author Robert Bausdorf, Christian Brauers - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 * @author Christoph Weitkamp - Added support for groups
 * @author Ulrich Mertin - Added support for HAN-FUN blinds
 * @author Christoph Sommer - Added support for color temperature
 * @author Leo Siepel - Made authentication non-blocking
 */
@NonNullByDefault
public class FritzAhaWebInterface {

    private static final String WEBSERVICE_PATH = "login_sid.lua";
    /**
     * RegEx Pattern to grab the session ID from a login XML response
     */
    private static final Pattern SID_PATTERN = Pattern.compile("<SID>([a-fA-F0-9]*)</SID>");
    /**
     * RegEx Pattern to grab the challenge from a login XML response
     */
    private static final Pattern CHALLENGE_PATTERN = Pattern.compile("<Challenge>(\\w*)</Challenge>");
    /**
     * RegEx Pattern to grab the access privilege for home automation functions from a login XML response
     */
    private static final Pattern ACCESS_PATTERN = Pattern.compile("<Name>HomeAuto</Name>\\s*?<Access>([0-9])</Access>");

    private final Logger logger = LoggerFactory.getLogger(FritzAhaWebInterface.class);
    /**
     * Configuration of the bridge from {@link AVMFritzBaseBridgeHandler}
     */
    private final AVMFritzBoxConfiguration config;
    /**
     * Bridge thing handler for updating thing status
     */
    private final AVMFritzBaseBridgeHandler handler;
    /**
     * Shared instance of HTTP client for asynchronous calls
     */
    private final HttpClient httpClient;
    private final Object authenticationLock = new Object();
    /**
     * Current session ID
     */
    private volatile @Nullable String sid;
    private volatile boolean disposed;
    private @Nullable CompletableFuture<Boolean> authentication;

    /**
     * This method authenticates with the FRITZ!OS Web Interface and updates the session ID accordingly
     */
    public CompletableFuture<Boolean> authenticate() {
        CompletableFuture<Boolean> currentAuthentication;
        synchronized (authenticationLock) {
            if (disposed) {
                return CompletableFuture.completedFuture(false);
            }
            if (sid != null) {
                return CompletableFuture.completedFuture(true);
            }
            CompletableFuture<Boolean> ongoingAuthentication = authentication;
            if (ongoingAuthentication != null) {
                return ongoingAuthentication;
            }
            currentAuthentication = new CompletableFuture<>();
            authentication = currentAuthentication;
        }
        String localPassword = config.password;
        if (localPassword == null || localPassword.trim().isEmpty()) {
            completeAuthentication(currentAuthentication, null, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Please configure the password.");
            return currentAuthentication;
        }
        requestLoginPage(currentAuthentication, getURL(WEBSERVICE_PATH),
                loginXml -> processInitialLoginResponse(currentAuthentication, loginXml));
        return currentAuthentication;
    }

    private void processInitialLoginResponse(CompletableFuture<Boolean> currentAuthentication, String loginXml) {
        Matcher sidmatch = SID_PATTERN.matcher(loginXml);
        if (!sidmatch.find()) {
            completeAuthentication(currentAuthentication, null, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond with SID.");
            return;
        }
        String localSid = sidmatch.group(1);
        Matcher accmatch = ACCESS_PATTERN.matcher(loginXml);
        if (accmatch.find() && "2".equals(accmatch.group(1))) {
            completeAuthentication(currentAuthentication, localSid, ThingStatusDetail.NONE, null);
            return;
        }
        Matcher challengematch = CHALLENGE_PATTERN.matcher(loginXml);
        if (!challengematch.find()) {
            completeAuthentication(currentAuthentication, null, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond with challenge for authentication.");
            return;
        }
        String challenge = challengematch.group(1);
        String response = createResponse(challenge);
        String localUser = config.user;
        requestLoginPage(currentAuthentication,
                getURL(WEBSERVICE_PATH,
                        (localUser == null || localUser.isEmpty() ? "" : ("username=" + localUser + "&")) + "response="
                                + response),
                authenticatedLoginXml -> processAuthenticatedLoginResponse(currentAuthentication, authenticatedLoginXml,
                        localUser));
    }

    private void processAuthenticatedLoginResponse(CompletableFuture<Boolean> currentAuthentication, String loginXml,
            @Nullable String localUser) {
        Matcher sidmatch = SID_PATTERN.matcher(loginXml);
        if (!sidmatch.find()) {
            completeAuthentication(currentAuthentication, null, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond with SID.");
            return;
        }
        String localSid = sidmatch.group(1);
        Matcher accmatch = ACCESS_PATTERN.matcher(loginXml);
        if (accmatch.find() && "2".equals(accmatch.group(1))) {
            completeAuthentication(currentAuthentication, localSid, ThingStatusDetail.NONE, null);
            return;
        }
        completeAuthentication(currentAuthentication, null, ThingStatusDetail.CONFIGURATION_ERROR, "User "
                + (localUser == null ? "" : localUser) + " has no access to FRITZ!Box home automation functions.");
    }

    private void requestLoginPage(CompletableFuture<Boolean> currentAuthentication, String url,
            Consumer<String> responseConsumer) {
        try {
            httpClient.newRequest(url).timeout(config.syncTimeout, TimeUnit.MILLISECONDS).method(HttpMethod.GET)
                    .send(new BufferingResponseListener() {
                        @Override
                        public void onComplete(@NonNullByDefault({}) Result result) {
                            String content = getContentAsString();
                            if (result.isSucceeded() && result.getResponse().getStatus() == 200 && content != null) {
                                logger.debug("Authentication GET response complete");
                                try {
                                    responseConsumer.accept(content);
                                } catch (RuntimeException e) {
                                    logger.debug("Failed to process authentication response", e);
                                    completeAuthentication(currentAuthentication, null,
                                            ThingStatusDetail.COMMUNICATION_ERROR,
                                            "FRITZ!Box returned an invalid authentication response.");
                                }
                            } else {
                                logger.debug("Authentication GET response failed", result.getFailure());
                                completeAuthentication(currentAuthentication, null,
                                        ThingStatusDetail.COMMUNICATION_ERROR, "FRITZ!Box does not respond.");
                            }
                        }
                    });
        } catch (RuntimeException e) {
            logger.debug("Authentication GET request failed", e);
            completeAuthentication(currentAuthentication, null, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond.");
        }
    }

    private void completeAuthentication(CompletableFuture<Boolean> currentAuthentication, @Nullable String newSid,
            ThingStatusDetail statusDetail, @Nullable String description) {
        synchronized (authenticationLock) {
            if (!currentAuthentication.equals(authentication) || disposed) {
                currentAuthentication.complete(false);
                return;
            }
            sid = newSid;
            boolean authenticated = newSid != null;
            handler.setStatusInfo(authenticated ? ThingStatus.ONLINE : ThingStatus.OFFLINE, statusDetail, description);
            currentAuthentication.complete(authenticated);
            authentication = null;
        }
    }

    /**
     * Checks the authentication status of the web interface
     *
     * @return
     */
    public boolean isAuthenticated() {
        return sid != null;
    }

    /**
     * Creates the proper response to a given challenge based on the password stored
     *
     * @param challenge Challenge string as returned by the FRITZ!OS login script
     * @return Response to the challenge
     */
    protected String createResponse(String challenge) {
        String response = challenge.concat("-");
        String handshake = response.concat(config.password);
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error("This version of Java does not support MD5 hashing");
            return "";
        }
        byte[] handshakeHash = md5.digest(handshake.getBytes(StandardCharsets.UTF_16LE));
        for (byte handshakeByte : handshakeHash) {
            response = response.concat(String.format("%02x", handshakeByte));
        }
        return response;
    }

    /**
     * Constructor to set up interface
     *
     * @param config Bridge configuration
     */
    public FritzAhaWebInterface(AVMFritzBoxConfiguration config, AVMFritzBaseBridgeHandler handler,
            HttpClient httpClient) {
        this.config = config;
        this.handler = handler;
        this.httpClient = httpClient;
    }

    public void dispose() {
        CompletableFuture<Boolean> currentAuthentication;
        synchronized (authenticationLock) {
            disposed = true;
            sid = null;
            currentAuthentication = authentication;
            authentication = null;
        }
        if (currentAuthentication != null) {
            currentAuthentication.complete(false);
        }
    }

    /**
     * Constructs an URL from the stored information and a specified path
     *
     * @param path Path to include in URL
     * @return URL
     */
    public String getURL(String path) {
        return config.protocol + "://" + config.ipAddress + (config.port == null ? "" : ":" + config.port) + "/" + path;
    }

    /**
     * Constructs an URL from the stored information, a specified path and a specified argument string
     *
     * @param path Path to include in URL
     * @param args String of arguments, in standard HTTP format ({@code arg1=value1&arg2=value2&...})
     * @return URL
     */
    public String getURL(String path, String args) {
        return getURL(args.isEmpty() ? path : path + "?" + args);
    }

    public String addSID(String path) {
        String currentSid = sid;
        if (currentSid == null) {
            return path;
        } else {
            return (path.isEmpty() ? "" : path + "&") + "sid=" + currentSid;
        }
    }

    /**
     * Sends a HTTP GET request using the asynchronous client
     *
     * @param path Path of the requested resource
     * @param args Arguments for the request
     * @param callback Callback to handle the response with
     */
    public FritzAhaContentExchange asyncGet(String path, String args, FritzAhaCallback callback) {
        FritzAhaContentExchange getExchange = new FritzAhaContentExchange(callback);
        authenticate().thenAccept(authenticated -> {
            if (authenticated && !disposed) {
                httpClient.newRequest(getURL(path, addSID(args))).timeout(config.asyncTimeout, TimeUnit.MILLISECONDS)
                        .method(HttpMethod.GET).onResponseSuccess(getExchange).onResponseFailure(getExchange)
                        .send(getExchange);
            }
        });
        return getExchange;
    }

    public FritzAhaContentExchange asyncGet(FritzAhaCallback callback) {
        return asyncGet(callback.getPath(), callback.getArgs(), callback);
    }

    /**
     * Sends a HTTP POST request using the asynchronous client
     *
     * @param path Path of the requested resource
     * @param args Arguments for the request
     * @param callback Callback to handle the response with
     */
    public FritzAhaContentExchange asyncPost(String path, String args, FritzAhaCallback callback) {
        FritzAhaContentExchange postExchange = new FritzAhaContentExchange(callback);
        authenticate().thenAccept(authenticated -> {
            if (authenticated && !disposed) {
                httpClient.newRequest(getURL(path)).timeout(config.asyncTimeout, TimeUnit.MILLISECONDS)
                        .method(HttpMethod.POST).onResponseSuccess(postExchange).onResponseFailure(postExchange)
                        .content(new StringContentProvider(addSID(args), StandardCharsets.UTF_8)).send(postExchange);
            }
        });
        return postExchange;
    }

    public void invalidateAuthentication() {
        synchronized (authenticationLock) {
            sid = null;
        }
    }

    public FritzAhaContentExchange applyTemplate(String ain) {
        FritzAhaApplyTemplateCallback callback = new FritzAhaApplyTemplateCallback(this, ain);
        return asyncGet(callback);
    }

    public FritzAhaContentExchange getEnergyStats(AVMFritzPowerMeterDeviceHandler handler, long deviceId) {
        FritzAhaGetEnergyStatsCallback callback = new FritzAhaGetEnergyStatsCallback(this, handler, deviceId);
        return asyncGet(callback);
    }

    public FritzAhaContentExchange setSwitch(String ain, boolean switchOn) {
        FritzAhaSetSwitchCallback callback = new FritzAhaSetSwitchCallback(this, ain, switchOn);
        return asyncGet(callback);
    }

    public FritzAhaContentExchange setSetTemp(String ain, BigDecimal temperature) {
        FritzAhaSetHeatingTemperatureCallback callback = new FritzAhaSetHeatingTemperatureCallback(this, ain,
                temperature);
        return asyncGet(callback);
    }

    public FritzAhaContentExchange setBoostMode(String ain, long endTime) {
        return setHeatingMode(ain, FritzAhaSetHeatingModeCallback.BOOST_COMMAND, endTime);
    }

    public FritzAhaContentExchange setWindowOpenMode(String ain, long endTime) {
        return setHeatingMode(ain, FritzAhaSetHeatingModeCallback.WINDOW_OPEN_COMMAND, endTime);
    }

    private FritzAhaContentExchange setHeatingMode(String ain, String command, long endTime) {
        FritzAhaSetHeatingModeCallback callback = new FritzAhaSetHeatingModeCallback(this, ain, command, endTime);
        return asyncGet(callback);
    }

    public FritzAhaContentExchange setLevelPercentage(String ain, BigDecimal levelPercentage) {
        FritzAhaSetLevelPercentageCallback callback = new FritzAhaSetLevelPercentageCallback(this, ain,
                levelPercentage);
        return asyncGet(callback);
    }

    public FritzAhaContentExchange setMappedHueAndSaturation(String ain, int hue, int saturation, int duration) {
        FritzAhaSetColorCallback callback = new FritzAhaSetColorCallback(this, ain, hue, saturation, duration);
        return asyncGet(callback);
    }

    public FritzAhaContentExchange setUnmappedHueAndSaturation(String ain, int hue, int saturation, int duration) {
        FritzAhaSetColorCallback callback = new FritzAhaSetColorCallback(this, ain, hue, saturation, duration, false);
        return asyncGet(callback);
    }

    public FritzAhaContentExchange setColorTemperature(String ain, int temperature, int duration) {
        FritzAhaSetColorTemperatureCallback callback = new FritzAhaSetColorTemperatureCallback(this, ain, temperature,
                duration);
        return asyncGet(callback);
    }

    public FritzAhaContentExchange setBlind(String ain, BlindCommand command) {
        FritzAhaSetBlindTargetCallback callback = new FritzAhaSetBlindTargetCallback(this, ain, command);
        return asyncGet(callback);
    }
}
