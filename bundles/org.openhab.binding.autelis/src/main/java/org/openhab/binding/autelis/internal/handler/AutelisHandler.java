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
package org.openhab.binding.autelis.internal.handler;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.autelis.internal.AutelisBindingConstants;
import org.openhab.binding.autelis.internal.config.AutelisConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
 * @see <a href="http://www.autelis.com/wiki/index.php?title=Pool_Control_HTTP_Command_Reference"</a> for Jandy API
 * @see <a href="http://www.autelis.com/wiki/index.php?title=Pool_Control_(PI)_HTTP_Command_Reference"</a> for Pentair
 *      API
 *
 *      The {@link AutelisHandler} is responsible for handling commands, which
 *      are sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Svilen Valkanov - Replaced Apache HttpClient with Jetty
 */
public class AutelisHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AutelisHandler.class);

    /**
     * Default timeout for http connections to an Autelis controller
     */
    static final int TIMEOUT_SECONDS = 5;

    /**
     * Autelis controllers will not update their XML immediately after we change
     * a value. To compensate we cache previous values for a {@link Channel}
     * using the item name as a key. After a polling run has been executed we
     * only update a channel if the value is different then what's in the
     * cache. This cache is cleared after a fixed time period when commands are
     * sent.
     */
    private Map<String, State> stateMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * Clear our state every hour
     */
    private static final int NORMAL_CLEARTIME_SECONDS = 60 * 60;

    /**
     * Default poll rate rate, this is derived from the Autelis web UI
     */
    private static final int DEFAULT_REFRESH_SECONDS = 3;

    /**
     * How long should we wait to poll after we send an update, derived from trial and error
     */
    private static final int COMMAND_UPDATE_TIME_SECONDS = 6;

    /**
     * The autelis unit will 'loose' commands if sent to fast
     */
    private static final int THROTTLE_TIME_MILLISECONDS = 500;

    /**
     * Autelis web port
     */
    private static final int WEB_PORT = 80;

    /**
     * Pentair values for pump response
     */
    private static final String[] PUMP_TYPES = { "watts", "rpm", "gpm", "filer", "error" };

    /**
     * Matcher for pump channel names for Pentair
     */
    private static final Pattern PUMPS_PATTERN = Pattern.compile("(pumps/pump\\d?)-(watts|rpm|gpm|filter|error)");

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
    private int refresh;

    /**
     * The http client used for polling requests
     */
    private HttpClient client = new HttpClient();

    /**
     * last time we finished a request
     */
    private long lastRequestTime = 0;

    /**
     * Authentication for login
     */
    private String basicAuthentication;

    /**
     * Regex expression to match XML responses from the Autelis, this is used to
     * combine similar XML docs into a single document, {@link XPath} is still
     * used for XML querying
     */
    private Pattern responsePattern = Pattern.compile("<response>(.+?)</response>", Pattern.DOTALL);

    /**
     * Future to poll for updated
     */
    private ScheduledFuture<?> pollFuture;

    public AutelisHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        startHttpClient(client);
        configure();
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        clearPolling();
        stopHttpClient(client);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // clear our cached values so the new channel gets updated
        clearState(true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            logger.debug("handleCommand channel: {} command: {}", channelUID.getId(), command);
            if (AutelisBindingConstants.CMD_LIGHTS.equals(channelUID.getId())) {
                /*
                 * lighting command possible values, but we will let anything
                 * through. alloff, allon, csync, cset, cswim, party, romance,
                 * caribbean, american, sunset, royalty, blue, green, red, white,
                 * magenta, hold, recall
                 */
                getUrl(baseURL + "/lights.cgi?val=" + command.toString(), TIMEOUT_SECONDS);
            } else if (AutelisBindingConstants.CMD_REBOOT.equals(channelUID.getId()) && command == OnOffType.ON) {
                getUrl(baseURL + "/userreboot.cgi?do=true" + command.toString(), TIMEOUT_SECONDS);
                updateState(channelUID, OnOffType.OFF);
            } else {
                String[] args = channelUID.getId().split("-");
                if (args.length < 2) {
                    logger.warn("Unown channel {} for command {}", channelUID, command);
                    return;
                }
                String type = args[0];
                String name = args[1];

                if (AutelisBindingConstants.CMD_EQUIPMENT.equals(type)) {
                    String cmd = "value";
                    int value;
                    if (command == OnOffType.OFF) {
                        value = 0;
                    } else if (command == OnOffType.ON) {
                        value = 1;
                    } else if (command instanceof DecimalType) {
                        value = ((DecimalType) command).intValue();
                        if (!isJandy() && value >= 3) {
                            // this is an autelis dim type. not sure what 2 does
                            cmd = "dim";
                        }
                    } else {
                        logger.error("command type {} is not supported", command);
                        return;
                    }
                    String response = getUrl(baseURL + "/set.cgi?name=" + name + "&" + cmd + "=" + value,
                            TIMEOUT_SECONDS);
                    logger.debug("equipment set {} {} {} : result {}", name, cmd, value, response);
                } else if (AutelisBindingConstants.CMD_TEMP.equals(type)) {
                    String value;
                    if (command == IncreaseDecreaseType.INCREASE) {
                        value = "up";
                    } else if (command == IncreaseDecreaseType.DECREASE) {
                        value = "down";
                    } else if (command == OnOffType.OFF) {
                        value = "0";
                    } else if (command == OnOffType.ON) {
                        value = "1";
                    } else {
                        value = command.toString();
                    }

                    String cmd;
                    // name ending in sp are setpoints, ht are heater?
                    if (name.endsWith("sp")) {
                        cmd = "temp";
                    } else if (name.endsWith("ht")) {
                        cmd = "hval";
                    } else {
                        logger.error("Unknown temp type {}", name);
                        return;
                    }
                    String response = getUrl(baseURL + "/set.cgi?wait=1&name=" + name + "&" + cmd + "=" + value,
                            TIMEOUT_SECONDS);
                    logger.debug("temp set name:{} cmd:{} value:{} : result {}", name, cmd, value, response);
                } else if (AutelisBindingConstants.CMD_CHEM.equals(type)) {
                    String response = getUrl(baseURL + "/set.cgi?name=" + name + "&chem=" + command.toString(),
                            TIMEOUT_SECONDS);
                    logger.debug("chlrp {} {}: result {}", name, command, response);
                } else if (AutelisBindingConstants.CMD_PUMPS.equals(type)) {
                    String response = getUrl(baseURL + "/set.cgi?name=" + name + "&speed=" + command.toString(),
                            TIMEOUT_SECONDS);
                    logger.debug("pumps {} {}: result {}", name, command, response);
                } else {
                    logger.error("Unsupported type {}", type);
                }
            }
            clearState(true);
            // reset the schedule for our next poll which at that time will reflect if our command was successful or
            // not.
            initPolling(COMMAND_UPDATE_TIME_SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Configures this thing
     */
    private void configure() {
        clearPolling();

        AutelisConfiguration configuration = getConfig().as(AutelisConfiguration.class);
        Integer refreshOrNull = configuration.refresh;
        Integer portOrNull = configuration.port;
        String host = configuration.host;
        String username = configuration.user;
        String password = configuration.password;

        if (username == null || username.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "username must not be empty");
            return;
        }

        if (password == null || password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "password must not be empty");
            return;
        }

        if (host == null || host.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "hostname must not be empty");
            return;
        }

        refresh = DEFAULT_REFRESH_SECONDS;
        if (refreshOrNull != null) {
            refresh = refreshOrNull.intValue();
        }

        int port = WEB_PORT;
        if (portOrNull != null) {
            port = portOrNull.intValue();
        }

        baseURL = "http://" + host + ":" + port;
        basicAuthentication = "Basic "
                + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.ISO_8859_1));
        logger.debug("Autelius binding configured with base url {} and refresh period of {}", baseURL, refresh);

        initPolling(0);
    }

    /**
     * Starts/Restarts polling with an initial delay. This allows changes in the poll cycle for when commands are sent
     * and we need to poll sooner then the next refresh cycle.
     */
    private synchronized void initPolling(int initalDelay) {
        clearPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                pollAutelisController();
            } catch (Exception e) {
                logger.debug("Exception during poll", e);
            }
        }, initalDelay, DEFAULT_REFRESH_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Stops/clears this thing's polling future
     */
    private void clearPolling() {
        if (pollFuture != null && !pollFuture.isCancelled()) {
            logger.trace("Canceling future");
            pollFuture.cancel(false);
        }
    }

    /**
     * Poll the Autelis controller for updates. This will retrieve various xml documents and update channel states from
     * its contents.
     */
    private void pollAutelisController() throws InterruptedException {
        logger.trace("Connecting to {}", baseURL);

        // clear our cached stated IF it is time.
        clearState(false);

        // we will reconstruct the document with all the responses combined for XPATH
        StringBuilder sb = new StringBuilder("<response>");

        // pull down the three xml documents
        String[] statuses = { "status", "chem", "pumps" };

        for (String status : statuses) {
            String response = getUrl(baseURL + "/" + status + ".xml", TIMEOUT_SECONDS);
            logger.trace("{}/{}.xml \n {}", baseURL, status, response);
            if (response == null) {
                // all models and versions have the status.xml endpoint
                if (status.equals("status")) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
                    return;
                } else {
                    // not all models have the other endpoints, so we ignore errors
                    continue;
                }
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

        if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
            updateStatus(ThingStatus.ONLINE);
        }

        /*
         * This xmlDoc will now contain the three XML documents we retrieved
         * wrapped in response tags for easier querying in XPath.
         */
        HashMap<String, String> pumps = new HashMap<>();
        String xmlDoc = sb.toString();
        for (Channel channel : getThing().getChannels()) {
            String key = channel.getUID().getId().replaceFirst("-", "/");
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            try {
                InputSource is = new InputSource(new StringReader(xmlDoc));
                String value = null;

                /**
                 * Work around for Pentair pumps. Rather then have child XML elements, the response rather uses commas
                 * on the pump response to separate the different values like so:
                 *
                 * watts,rpm,gpm,filter,error
                 *
                 * Also, some pools will only report the first 3 out of the 5 values.
                 */

                Matcher matcher = PUMPS_PATTERN.matcher(key);
                if (matcher.matches()) {
                    if (!pumps.containsKey(key)) {
                        String pumpValue = xpath.evaluate("response/" + matcher.group(1), is);
                        String[] values = pumpValue.split(",");
                        for (int i = 0; i < PUMP_TYPES.length; i++) {

                            // this will be something like pump/pump1-rpm
                            String newKey = matcher.group(1) + '-' + PUMP_TYPES[i];

                            // some Pentair models only have the first 3 values
                            if (i < values.length) {
                                pumps.put(newKey, values[i]);
                            } else {
                                pumps.put(newKey, "");
                            }
                        }
                    }
                    value = pumps.get(key);
                } else {
                    value = xpath.evaluate("response/" + key, is);

                    // Convert pentair salt levels to PPM.
                    if ("chlor/salt".equals(key)) {
                        try {
                            value = String.valueOf(Integer.parseInt(value) * 50);
                        } catch (NumberFormatException ignored) {
                            logger.debug("Failed to parse pentair salt level as integer");
                        }
                    }
                }

                if (value == null || value.isEmpty()) {
                    continue;
                }

                State state = toState(channel.getAcceptedItemType(), value);
                State oldState = stateMap.put(channel.getUID().getAsString(), state);
                if (!state.equals(oldState)) {
                    logger.trace("updating channel {} with state {} (old state {})", channel.getUID(), state, oldState);
                    updateState(channel.getUID(), state);
                }
            } catch (XPathExpressionException e) {
                logger.error("could not parse xml", e);
            }
        }
    }

    /**
     * Simple logic to perform an authenticated GET request
     *
     * @param url
     * @param timeout
     * @return
     */
    private synchronized String getUrl(String url, int timeout) throws InterruptedException {
        // throttle commands for a very short time to avoid 'loosing' them
        long now = System.currentTimeMillis();
        long nextReq = lastRequestTime + THROTTLE_TIME_MILLISECONDS;
        if (nextReq > now) {
            logger.trace("Throttling request for {} mills", nextReq - now);
            Thread.sleep(nextReq - now);
        }
        String getURL = url + (url.contains("?") ? "&" : "?") + "timestamp=" + System.currentTimeMillis();
        logger.trace("Getting URL {} ", getURL);
        Request request = client.newRequest(getURL).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        request.header(HttpHeader.AUTHORIZATION, basicAuthentication);
        try {
            ContentResponse response = request.send();
            int statusCode = response.getStatus();
            if (statusCode != HttpStatus.OK_200) {
                logger.trace("Method failed: {}", response.getStatus() + " " + response.getReason());
                return null;
            }
            lastRequestTime = System.currentTimeMillis();
            return response.getContentAsString();
        } catch (ExecutionException | TimeoutException e) {
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
        if ("Number".equals(type)) {
            return new DecimalType(value);
        } else if ("Switch".equals(type)) {
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
            clearTime = System.currentTimeMillis() + (NORMAL_CLEARTIME_SECONDS * 1000);
        }
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
        if (client != null) {
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

    private boolean isJandy() {
        return AutelisBindingConstants.JANDY_THING_TYPE_UID.equals(getThing().getThingTypeUID());
    }
}
