/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.autelis.handler;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.autelis.internal.config.AutelisConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 *
 * Autelis Pool Control Binding
 *
 * Autelis controllers allow remote access to many common pool systems. This
 * binding allows openHAB to both monitor and control a pool system through
 * these controllers.
 *
 * @see <a href="http://Autelis.com">http://autelis.com</a>
 *
 *      The {@link AutelisHandler} is responsible for handling commands, which
 *      are sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Svilen Valkanov - Replaced Apache HttpClient with Jetty
 */
public class AutelisHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(AutelisHandler.class);

    /**
     * Default timeout for http connections to a Autelis controller
     */
    static final int TIMEOUT = 5000;

    /**
     * Autelis controllers will not update their XML immediately after we change
     * a value. To compensate we cache previous values for a {@link Channel}
     * using the item name as a key. After a polling run has been executed we
     * only update an channel if the value is different then what's in the
     * cache. This cache is cleared after a fixed time period when commands are
     * sent.
     */
    private Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<String, State>());

    /**
     * Clear our state every hour
     */
    private static int NORMAL_CLEARTIME = 60 * 60; // one hour

    /**
     * Clear state after an command is sent
     */
    private static int UPDATE_CLEARTIME = 60 * 2; // two minutes

    /**
     * Holds the next clear time in millis
     */
    private long clearTime;

    /**
     * Constructed URL consisting of host and port
     */
    private String baseURL;

    /**
     * Our poll rate
     */
    int refresh;

    /**
     * The http client used for polling requests
     */
    private HttpClient client = new HttpClient();

    /**
     * Regex expression to match XML responses from the Autelis, this is used to
     * combine similar XML docs into a single document, {@link XPath} is still
     * used for XML querying
     */
    private Pattern responsePattern = Pattern.compile("<response>(.+?)</response>", Pattern.DOTALL);

    /**
     * is our config correct
     */
    private boolean properlyConfigured;

    /**
     * Future to poll for updated
     */
    private ScheduledFuture<?> pollFuture;

    public AutelisHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configure();
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopPolling();
        stopHttpClient(client);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // clear our cached values so the new channel gets updated
        clearState(true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (channelUID.getId().equals("lightscmd")) {
            /*
             * lighting command possible values, but we will let anything
             * through. alloff, allon, csync, cset, cswim, party, romance,
             * caribbean, american, sunset, royalty, blue, green, red, white,
             * magenta, hold, recall
             */
            getUrl(baseURL + "/lights.cgi?val=" + command.toString(), TIMEOUT);
        } else if (channelUID.getId().equals("reboot") && command == OnOffType.ON) {
            getUrl(baseURL + "/userreboot.cgi?do=true" + command.toString(), TIMEOUT);
            updateState(channelUID, OnOffType.OFF);
        } else {
            String[] args = channelUID.getId().split("-");
            if (args.length < 2) {
                logger.warn("Unown channel {} for command {}", channelUID, command);
                return;
            }
            String type = args[0];
            String name = args[1];

            if (type.equals("equipment")) {
                String cmd = "value";
                int value;
                if (command == OnOffType.OFF) {
                    value = 0;
                } else if (command == OnOffType.ON) {
                    value = 1;
                } else if (command instanceof DecimalType) {
                    value = ((DecimalType) command).intValue();
                    if (value >= 3) {
                        // this is a dim type. not sure what 2 does
                        cmd = "dim";
                    }
                } else {
                    logger.error("command type {} is not supported", command);
                    return;
                }
                String response = getUrl(baseURL + "/set.cgi?name=" + name + "&" + cmd + "=" + value, TIMEOUT);
                logger.debug("equipment set {} {} {} : result {}", name, cmd, value, response);
            } else if (type.equals("temp")) {
                String value;
                if (command == IncreaseDecreaseType.INCREASE) {
                    value = "up";
                } else if (command == IncreaseDecreaseType.DECREASE) {
                    value = "down";
                } else {
                    value = command.toString();
                }

                String cmd;
                // name ending in sp are setpoints, ht are heat types?
                if (name.endsWith("sp")) {
                    cmd = "temp";
                } else if (name.endsWith("ht")) {
                    cmd = "hval";
                } else {
                    logger.error("Unknown temp type {}", name);
                    return;
                }

                String response = getUrl(baseURL + "/set.cgi?wait=1&name=" + name + "&" + cmd + "=" + value, TIMEOUT);
                logger.debug("temp set {} {} : result {}", cmd, value, response);
            } else {
                logger.error("Unsupported type {}", type);
            }
        }
        scheduleClearTime(UPDATE_CLEARTIME);
    }

    /**
     * Configures this thing
     */
    private void configure() {

        properlyConfigured = false;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR);
        AutelisConfiguration configuration = getConfig().as(AutelisConfiguration.class);
        try {
            Integer _refresh = configuration.refresh;
            Integer _port = configuration.port;
            String host = configuration.host;
            String username = configuration.user;
            String password = configuration.password;

            if (StringUtils.isBlank(username)) {
                throw new RuntimeException("username must not be empty");
            }

            if (StringUtils.isBlank(password)) {
                throw new RuntimeException("password must not be empty");
            }

            if (StringUtils.isBlank(host)) {
                throw new RuntimeException("hostname must not be empty");
            }

            refresh = 5;
            if (_refresh != null) {
                refresh = _refresh.intValue();
            }

            int port = 80;
            if (_port != null) {
                port = _port.intValue();
            }

            baseURL = "http://" + host + ":" + port;

            properlyConfigured = true;

            logger.debug("Autelius binding configured with base url {} and refresh period of {}", baseURL, refresh);

            initPolling();

        } catch (Exception e) {
            logger.error("Could not configure autelis instance", e);
        }
    }

    /**
     * starts this things polling future
     */
    private void initPolling() {
        stopPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    if (properlyConfigured) {
                        execute();
                    }
                } catch (Exception e) {
                    logger.debug("Exception during poll : {}", e);
                }
            }
        }, 0, refresh, TimeUnit.SECONDS);

    }

    /**
     * Stops this thing's polling future
     */
    private void stopPolling() {
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(true);
            pollFuture = null;
        }
    }

    /**
     * The polling future executes this every iteration
     */
    protected void execute() {
        logger.trace("Connecting to {}", baseURL);

        clearState(false);

        // we will reconstruct the document with all the responses combined for
        // XPATH
        StringBuilder sb = new StringBuilder("<response>");

        // pull down the three xml documents
        String[] statuses = { "status", "chem", "pumps" };
        for (String status : statuses) {
            String response = getUrl(baseURL + "/" + status + ".xml", TIMEOUT);
            logger.trace("{}/{}.xml \n {}", baseURL, status, response);
            if (response == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
                return;
            }
            // get the xml data between the response tags and append to our main
            // doc
            Matcher m = responsePattern.matcher(response);
            if (m.find()) {
                sb.append(m.group(1));
            }
        }
        // finish our "new" XML Document
        sb.append("</response>");

        if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);
        }

        /*
         * This xmlDoc will now contain the three XML documents we retrieved
         * wrapped in response tags for easier querying in XPath.
         */
        String xmlDoc = sb.toString();
        for (Channel channel : getThing().getChannels()) {
            String key = channel.getUID().getId().replace('-', '/');
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            try {
                InputSource is = new InputSource(new StringReader(xmlDoc));
                String value = xpath.evaluate("response/" + key, is);

                if (StringUtils.isEmpty((value))) {
                    continue;
                }
                State state = toState(channel.getAcceptedItemType(), value);
                State oldState = stateMap.put(channel.getUID().getAsString(), state);
                if (!state.equals(oldState)) {
                    logger.trace("updating channel {} with state {}", channel, state);
                    updateState(channel.getUID(), state);
                }
            } catch (XPathExpressionException e) {
                logger.error("could not parse xml", e);
            }
        }
    }

    /**
     * Simple logic to perform a authenticated GET request
     *
     * @param url
     * @param timeout
     * @return
     */
    private String getUrl(String url, int timeout) {
        url += (url.contains("?") ? "&" : "?") + "timestamp=" + System.currentTimeMillis();
        startHttpClient(client);

        Request request = client.newRequest(url).timeout(TIMEOUT, TimeUnit.MILLISECONDS);

        AutelisConfiguration configuration = getConfig().as(AutelisConfiguration.class);
        String user = configuration.user;
        String password = configuration.password;

        String basicAuthentication = "Basic " + B64Code.encode(user + ":" + password, StringUtil.__ISO_8859_1);

        request.header(HttpHeader.AUTHORIZATION, basicAuthentication);

        try {
            ContentResponse response = request.send();
            int statusCode = response.getStatus();
            if (statusCode != HttpStatus.OK_200) {
                logger.debug("Method failed: {}", response.getStatus() + " " + response.getReason());
                return null;
            }
            return response.getContentAsString();
        } catch (Exception e) {
            logger.debug("Could not make http connection", e);
        }
        return null;
    }

    /**
     * Converts a {@link String} value to a {@link State} for a given
     * {@link String} accepted type
     *
     * @param itemType
     * @param value
     * @return {@link State}
     */
    private State toState(String type, String value) throws NumberFormatException {
        if (type.equals("Number")) {
            return new DecimalType(value);
        } else if (type.equals("Switch")) {
            return Integer.parseInt(value) > 0 ? OnOffType.ON : OnOffType.OFF;
        } else {
            return StringType.valueOf(value);
        }
    }

    /**
     * Clears our state if it is time
     */
    private void clearState(boolean force) {
        if (force || System.currentTimeMillis() >= clearTime) {
            stateMap.clear();
            scheduleClearTime(NORMAL_CLEARTIME);
        }
    }

    /**
     * Schedule when our next clear cycle will be
     *
     * @param secs
     */
    private void scheduleClearTime(int secs) {
        clearTime = System.currentTimeMillis() + (secs * 1000);
    }

    private void startHttpClient(HttpClient client) {
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                logger.error("Could not stop HttpClient", e);
            }
        }
    }

    private void stopHttpClient(HttpClient client) {
        client.getAuthenticationStore().clearAuthentications();
        client.getAuthenticationStore().clearAuthenticationResults();
        if (client.isStarted()) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.error("Could not stop HttpClient", e);
            }
        }
    }
}
