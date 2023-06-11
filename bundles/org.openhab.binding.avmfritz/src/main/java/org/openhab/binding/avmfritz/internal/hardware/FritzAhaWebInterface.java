/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.avmfritz.internal.config.AVMFritzBoxConfiguration;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzBaseBridgeHandler;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaApplyTemplateCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetBlindTargetCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetBlindTargetCallback.BlindCommand;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetColorCallback;
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
    /**
     * Current session ID
     */
    private @Nullable String sid;

    /**
     * This method authenticates with the FRITZ!OS Web Interface and updates the session ID accordingly
     */
    public void authenticate() {
        sid = null;
        String localPassword = config.password;
        if (localPassword == null || localPassword.trim().isEmpty()) {
            handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Please configure the password.");
            return;
        }
        String loginXml = syncGet(getURL(WEBSERVICE_PATH, addSID("")));
        if (loginXml == null) {
            handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond.");
            return;
        }
        Matcher sidmatch = SID_PATTERN.matcher(loginXml);
        if (!sidmatch.find()) {
            handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond with SID.");
            return;
        }
        String localSid = sidmatch.group(1);
        Matcher accmatch = ACCESS_PATTERN.matcher(loginXml);
        if (accmatch.find()) {
            if ("2".equals(accmatch.group(1))) {
                sid = localSid;
                handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                return;
            }
        }
        Matcher challengematch = CHALLENGE_PATTERN.matcher(loginXml);
        if (!challengematch.find()) {
            handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond with challenge for authentication.");
            return;
        }
        String challenge = challengematch.group(1);
        String response = createResponse(challenge);
        String localUser = config.user;
        loginXml = syncGet(getURL(WEBSERVICE_PATH,
                (localUser == null || localUser.isEmpty() ? "" : ("username=" + localUser + "&")) + "response="
                        + response));
        if (loginXml == null) {
            handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond.");
            return;
        }
        sidmatch = SID_PATTERN.matcher(loginXml);
        if (!sidmatch.find()) {
            handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond with SID.");
            return;
        }
        localSid = sidmatch.group(1);
        accmatch = ACCESS_PATTERN.matcher(loginXml);
        if (accmatch.find()) {
            if ("2".equals(accmatch.group(1))) {
                sid = localSid;
                handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                return;
            }
        }
        handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "User "
                + (localUser == null ? "" : localUser) + " has no access to FRITZ!Box home automation functions.");
        return;
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
        authenticate();
        logger.debug("Starting with SID {}", sid);
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
     * @param args String of arguments, in standard HTTP format (arg1=value1&arg2=value2&...)
     * @return URL
     */
    public String getURL(String path, String args) {
        return getURL(args.isEmpty() ? path : path + "?" + args);
    }

    public String addSID(String path) {
        if (sid == null) {
            return path;
        } else {
            return (path.isEmpty() ? "" : path + "&") + "sid=" + sid;
        }
    }

    /**
     * Sends a HTTP GET request using the synchronous client
     *
     * @param path Path of the requested resource
     * @return response
     */
    public @Nullable String syncGet(String path) {
        try {
            ContentResponse contentResponse = httpClient.newRequest(path)
                    .timeout(config.syncTimeout, TimeUnit.MILLISECONDS).method(HttpMethod.GET).send();
            String content = contentResponse.getContentAsString();
            logger.debug("GET response complete: {}", content);
            return content;
        } catch (ExecutionException | TimeoutException e) {
            logger.debug("GET response failed: {}", e.getLocalizedMessage(), e);
            return null;
        } catch (InterruptedException e) {
            logger.debug("GET response interrupted: {}", e.getLocalizedMessage(), e);
            Thread.currentThread().interrupt();
            return null;
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
        if (!isAuthenticated()) {
            authenticate();
        }
        FritzAhaContentExchange getExchange = new FritzAhaContentExchange(callback);
        httpClient.newRequest(getURL(path, addSID(args))).method(HttpMethod.GET).onResponseSuccess(getExchange)
                .onResponseFailure(getExchange).send(getExchange);
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
        if (!isAuthenticated()) {
            authenticate();
        }
        FritzAhaContentExchange postExchange = new FritzAhaContentExchange(callback);
        httpClient.newRequest(getURL(path)).timeout(config.asyncTimeout, TimeUnit.MILLISECONDS).method(HttpMethod.POST)
                .onResponseSuccess(postExchange).onResponseFailure(postExchange)
                .content(new StringContentProvider(addSID(args), StandardCharsets.UTF_8)).send(postExchange);
        return postExchange;
    }

    public FritzAhaContentExchange applyTemplate(String ain) {
        FritzAhaApplyTemplateCallback callback = new FritzAhaApplyTemplateCallback(this, ain);
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

    public FritzAhaContentExchange setBlind(String ain, BlindCommand command) {
        FritzAhaSetBlindTargetCallback callback = new FritzAhaSetBlindTargetCallback(this, ain, command);
        return asyncGet(callback);
    }
}
