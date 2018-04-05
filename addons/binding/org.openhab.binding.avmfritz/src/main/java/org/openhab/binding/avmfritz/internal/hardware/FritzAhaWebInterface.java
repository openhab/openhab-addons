/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.hardware;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.avmfritz.handler.AVMFritzBaseBridgeHandler;
import org.openhab.binding.avmfritz.internal.config.AVMFritzConfiguration;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetHeatingTemperatureCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetSwitchCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles requests to a FRITZ!OS web interface for interfacing with
 * AVM home automation devices. It manages authentication and wraps commands.
 *
 * @author Robert Bausdorf, Christian Brauers
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 * @author Christoph Weitkamp - Added support for groups
 */
public class FritzAhaWebInterface {

    /**
     * Configuration of the bridge from
     * {@link org.openhab.BoxHandler.fritzaha.handler.FritzAhaBridgeHandler}
     */
    protected AVMFritzConfiguration config;
    /**
     * Current session ID
     */
    protected String sid;
    /**
     * shared instance of HTTP client for asynchronous calls
     */
    protected HttpClient httpClient;
    /**
     * Bridge thing handler for updating thing status
     */
    protected AVMFritzBaseBridgeHandler handler;

    private final Logger logger = LoggerFactory.getLogger(FritzAhaWebInterface.class);
    // Uses RegEx to handle bad FRITZ!Box XML
    /**
     * RegEx Pattern to grab the session ID from a login XML response
     */
    protected static final Pattern SID_PATTERN = Pattern.compile("<SID>([a-fA-F0-9]*)</SID>");
    /**
     * RegEx Pattern to grab the challenge from a login XML response
     */
    protected static final Pattern CHALLENGE_PATTERN = Pattern.compile("<Challenge>(\\w*)</Challenge>");
    /**
     * RegEx Pattern to grab the access privilege for home automation functions
     * from a login XML response
     */
    protected static final Pattern ACCESS_PATTERN = Pattern
            .compile("<Name>HomeAuto</Name>\\s*?<Access>([0-9])</Access>");

    /**
     * This method authenticates with the FRITZ!OS Web Interface and updates the
     * session ID accordingly
     *
     * @return New session ID
     */
    public String authenticate() {
        if (this.config.getPassword() == null) {
            this.handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Please configure password first");
            return null;
        }
        String loginXml = null;
        try {
            loginXml = HttpUtil.executeUrl("GET", getURL("login_sid.lua", addSID("")),
                    10 * this.config.getSyncTimeout());
        } catch (IOException e) {
            logger.debug("Failed to get loginXML {}", e.getLocalizedMessage(), e);
        }
        if (loginXml == null) {
            this.handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond");
            return null;
        } else {
            logger.debug("response complete: {}", loginXml);
        }
        Matcher sidmatch = SID_PATTERN.matcher(loginXml);
        if (!sidmatch.find()) {
            this.handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond with SID");
            return null;
        }
        sid = sidmatch.group(1);
        Matcher accmatch = ACCESS_PATTERN.matcher(loginXml);
        if (accmatch.find()) {
            if ("2".equals(accmatch.group(1))) {
                this.handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                        "Resuming FRITZ!Box connection with SID " + sid);
                return sid;
            }
        }
        Matcher challengematch = CHALLENGE_PATTERN.matcher(loginXml);
        if (!challengematch.find()) {
            this.handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond with challenge for authentication");
            return null;
        }
        String challenge = challengematch.group(1);
        String response = createResponse(challenge);
        loginXml = null;
        try {
            loginXml = HttpUtil.executeUrl("GET",
                    getURL("login_sid.lua",
                            (this.config.getUser() != null && !"".equals(this.config.getUser())
                                    ? ("username=" + this.config.getUser() + "&")
                                    : "") + "response=" + response),
                    10 * this.config.getSyncTimeout());
        } catch (IOException e) {
            logger.debug("Failed to get loginXML {}", e.getLocalizedMessage(), e);
        }
        if (loginXml == null) {
            this.handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond");
            return null;
        } else {
            logger.debug("response complete: {}", loginXml);
        }
        sidmatch = SID_PATTERN.matcher(loginXml);
        if (!sidmatch.find()) {
            this.handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "FRITZ!Box does not respond with SID");
            return null;
        }
        sid = sidmatch.group(1);
        accmatch = ACCESS_PATTERN.matcher(loginXml);
        if (accmatch.find()) {
            if ("2".equals(accmatch.group(1))) {
                this.handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                        "Established FRITZ!Box connection with SID " + sid);
                return sid;
            }
        }
        this.handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "User " + this.config.getUser() + " has no access to FRITZ!Box home automation functions");
        return null;
    }

    /**
     * Checks the authentication status of the web interface
     *
     * @return
     */
    public boolean isAuthenticated() {
        return !(sid == null);
    }

    public AVMFritzConfiguration getConfig() {
        return config;
    }

    public void setConfig(AVMFritzConfiguration config) {
        this.config = config;
    }

    /**
     * Creates the proper response to a given challenge based on the password
     * stored
     *
     * @param challenge Challenge string as returned by the FRITZ!OS login
     *            script
     * @return Response to the challenge
     */
    protected String createResponse(String challenge) {
        String handshake = challenge.concat("-").concat(config.getPassword());
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error("This version of Java does not support MD5 hashing");
            return "";
        }
        byte[] handshakeHash;
        try {
            handshakeHash = md5.digest(handshake.getBytes("UTF-16LE"));
        } catch (UnsupportedEncodingException e) {
            logger.error("This version of Java does not understand UTF-16LE encoding");
            return "";
        }
        String response = challenge.concat("-");
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
    public FritzAhaWebInterface(AVMFritzConfiguration config, AVMFritzBaseBridgeHandler handler,
            HttpClient httpClient) {
        this.config = config;
        this.handler = handler;
        this.httpClient = httpClient;
        sid = null;
        authenticate();
        logger.debug("Starting with SID {}", sid);
    }

    /**
     * Constructs a URL from the stored information and a specified path
     *
     * @param path Path to include in URL
     * @return URL
     */
    public String getURL(String path) {
        return config.getProtocol() + "://" + config.getIpAddress()
                + (config.getPort() != null ? ":" + config.getPort() : "") + "/" + path;
    }

    /**
     * Constructs a URL from the stored information, a specified path and a
     * specified argument string
     *
     * @param path Path to include in URL
     * @param args String of arguments, in standard HTTP format
     *            (arg1=value1&arg2=value2&...)
     * @return URL
     */
    public String getURL(String path, String args) {
        return getURL(path + "?" + args);
    }

    public String addSID(String args) {
        if (sid == null) {
            return args;
        } else {
            return ("".equals(args) ? ("sid=") : (args + "&sid=")) + sid;
        }
    }

    /**
     * Sends an HTTP GET request using the asynchronous client
     *
     * @param Path Path of the requested resource
     * @param Args Arguments for the request
     * @param Callback Callback to handle the response with
     */
    public FritzAhaContentExchange asyncGet(String path, String args, FritzAhaCallback callback) {
        if (!isAuthenticated()) {
            authenticate();
        }
        FritzAhaContentExchange getExchange = new FritzAhaContentExchange(callback);
        httpClient.newRequest(getURL(path, addSID(args))).method(HttpMethod.GET).onResponseSuccess(getExchange)
                .onResponseFailure(getExchange) // .onComplete(getExchange)
                .send(getExchange);
        logger.debug("GETting URL {}", getURL(path, addSID(args)));
        return getExchange;
    }

    public FritzAhaContentExchange asyncGet(FritzAhaCallback callback) {
        return asyncGet(callback.getPath(), callback.getArgs(), callback);
    }

    /**
     * Sends an HTTP POST request using the asynchronous client
     *
     * @param Path Path of the requested resource
     * @param Args Arguments for the request
     * @param Callback Callback to handle the response with
     */
    public FritzAhaContentExchange asyncPost(String path, String args, FritzAhaCallback callback) {
        if (!isAuthenticated()) {
            authenticate();
        }
        FritzAhaContentExchange postExchange = new FritzAhaContentExchange(callback);
        httpClient.newRequest(getURL(path)).timeout(config.getAsyncTimeout(), TimeUnit.SECONDS).method(HttpMethod.POST)
                .onResponseSuccess(postExchange).onResponseFailure(postExchange) // .onComplete(postExchange)
                .content(new StringContentProvider(addSID(args), "UTF-8")).send(postExchange);
        return postExchange;
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
}
