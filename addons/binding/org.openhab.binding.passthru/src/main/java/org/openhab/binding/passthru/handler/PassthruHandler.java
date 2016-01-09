/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.passthru.handler;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.passthru.config.PassthruConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PassthruHandlerr} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author J. Geyer - Initial contribution
 */
public class PassthruHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(PassthruHandler.class);

    /**
     * Default timeout for http connections to a remote system
     */
    static final int TIMEOUT = 5000;

    /**
     * The Passthru interface saves the identifier and the command within the state map.
     * While polling the requested status gets compared with the actual and in case there is
     * a difference the remote system wins. The local status gets updated. The state map
     * will stay alive as long the interface is connected.
     */
    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * Constructed URL consisting of host and port
     */
    private String baseURL;

    /**
     * Target openhab version
     */
    private Integer version;

    /**
     * Our poll rate
     */
    int refresh;

    /**
     * The http client used for polling requests
     */
    HttpClient client;

    /**
     * is out config correct
     */
    private boolean properlyConfigured;

    /**
     * Future to poll for updated
     */
    private ScheduledFuture<?> pollFuture;

    public PassthruHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        configure();
    }

    @Override
    public void dispose() {
        logger.debug("Passthru handler disposed.");
        stopPolling();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (channelUID.getThingId().equals("remote")) {
            /*
             * The paasthru command just takes the event and passes it to the receiver.
             * The format of the channel description is
             * binding : ThingTypeId : ThingId : Id
             */
            String key = channelUID.getId();
            if (version == 1) {
                getUrl(baseURL + "/CMD?" + key + "=" + command.toString(), TIMEOUT);
            } else {
                getUrl(baseURL + "/classicui/CMD?" + key + "=" + command.toString(), TIMEOUT);
            }
            stateMap.put(key, command.toString());
        } else {
            String[] args = channelUID.getThingId().split("-");
            if (args.length < 2) {
                logger.warn("Unkown channel {} for command {}", channelUID, command.toString());
                return;
            }
        }

        String response = getUrl(baseURL + "/rest/items/" + channelUID.getId() + "/state", TIMEOUT);
        if (response.equals(command.toString())) {
            return;
        } else {
            logger.warn("Unkown status {} for command {}", channelUID, command.toString());
        }
    }

    /**
     * Configures this thing
     */
    private void configure() {

        properlyConfigured = false;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR);
        PassthruConfiguration configuration = getConfig().as(PassthruConfiguration.class);

        try {
            String _monitor = configuration.monitor;
            Integer _refresh = configuration.refresh;
            Integer _version = configuration.version;
            Integer _port = configuration.port;
            String host = configuration.host;

            // Process host
            if (StringUtils.isBlank(host)) {
                throw new RuntimeException("Remote hostname must not be empty");
            }

            // Process port
            int port = 80;
            if (_port != null) {
                port = _port.intValue();
            }

            client = new HttpClient();

            baseURL = "http://" + host + ":" + port;

            // Process version or auto detect
            version = 2;
            if (_version != null) {
                version = _version.intValue();
                if ((version != 1) && (version != 2)) {
                    throw new RuntimeException("Target version out of range");
                }
            } else {
                String response = getUrl(baseURL + "/rest", TIMEOUT);
                if (response != null) {
                    version = 1;
                }
            }

            // Process refresh rate
            refresh = 5;
            if (_refresh != null) {
                refresh = _refresh.intValue();
            }
            if (refresh == 0) {
                throw new RuntimeException("Refresh value zero");
            }

            // Process monitoring targets
            stateMap.clear();
            if (_monitor != null) {
                String delim = "[,]";
                String[] tokens = _monitor.split(delim);
                for (int i = 0; i < tokens.length; i++) {
                    stateMap.put(tokens[i], null);
                }
                logger.debug("Passthru monitors {}", _monitor);
            } else {
                throw new RuntimeException("No monitoring targets defined");
            }

            properlyConfigured = true;

            logger.debug("Passthru binding configured with base url {} version {} and refresh period of {}", baseURL,
                    version, refresh);

            initPolling();

        } catch (Exception e) {
            logger.error("Could not configure passthru interface", e);
        }
    }

    /**
     * starts this things polling future
     */
    private void initPolling() {
        stopPolling();
        pollFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (properlyConfigured) {
                        execute();
                    }
                } catch (Exception e) {
                    logger.debug("Exception during passthru status poll : {}", e);
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
     * The polling future executes this every iteration and synchronizes the status
     */
    protected void execute() {
        logger.trace("Passthru polling status {}" + baseURL);

        String hostIP;

        updateStatus(ThingStatus.ONLINE);

        Set<Entry<String, String>> stateEntry = stateMap.entrySet();
        Iterator<Entry<String, String>> iteratorMap = stateEntry.iterator();

        // Run thru remote state elements
        while (iteratorMap.hasNext()) {
            Entry<String, String> stateString = iteratorMap.next();
            String key = stateString.getKey();
            String command = stateString.getValue();

            // Get remote state
            String response = getUrl(baseURL + "/rest/items/" + key + "/state", TIMEOUT);
            if (response != null) {
                stateMap.put(key, response);
                try {
                    InetAddress localHost = InetAddress.getLocalHost();
                    hostIP = "http://" + localHost.getHostAddress() + ":" + "8080";
                } catch (Exception e) {
                    hostIP = "http://localhost" + ":" + "8080";
                }

                // Compare with local state and update if required
                String local = getUrl(hostIP + "/rest/items/" + key + "/state", TIMEOUT);
                if (response.equals(local)) {
                    return;
                } else {
                    getUrl(hostIP + "/classicui/CMD?" + key + "=" + response, TIMEOUT);
                    logger.debug("Synchronize {} state from {} to {} on {} ", key, command, response, hostIP);
                }
            }
        }
    }

    /**
     * Simple logic to perform a authenticated GET request
     */
    private String getUrl(String url, int timeout) {
        GetMethod method = new GetMethod(url);
        method.getParams().setSoTimeout(timeout);
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                logger.debug("Method failed: {}", method.getStatusLine());
                return null;
            }
            return IOUtils.toString(method.getResponseBodyAsStream());
        } catch (Exception e) {
            logger.debug("Could not make passthru http connection", e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }

}
