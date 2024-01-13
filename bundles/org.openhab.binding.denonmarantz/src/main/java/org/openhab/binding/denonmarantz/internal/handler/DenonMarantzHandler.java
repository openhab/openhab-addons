/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.denonmarantz.internal.handler;

import static org.openhab.binding.denonmarantz.internal.DenonMarantzBindingConstants.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.denonmarantz.internal.DenonMarantzState;
import org.openhab.binding.denonmarantz.internal.DenonMarantzStateChangedListener;
import org.openhab.binding.denonmarantz.internal.UnsupportedCommandTypeException;
import org.openhab.binding.denonmarantz.internal.config.DenonMarantzConfiguration;
import org.openhab.binding.denonmarantz.internal.connector.DenonMarantzConnector;
import org.openhab.binding.denonmarantz.internal.connector.DenonMarantzConnectorFactory;
import org.openhab.binding.denonmarantz.internal.connector.http.DenonMarantzHttpConnector;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link DenonMarantzHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 */
public class DenonMarantzHandler extends BaseThingHandler implements DenonMarantzStateChangedListener {

    private final Logger logger = LoggerFactory.getLogger(DenonMarantzHandler.class);
    private static final int RETRY_TIME_SECONDS = 30;
    private HttpClient httpClient;
    private DenonMarantzConnector connector;
    private DenonMarantzConfiguration config;
    private DenonMarantzConnectorFactory connectorFactory = new DenonMarantzConnectorFactory();
    private DenonMarantzState denonMarantzState;
    private ScheduledFuture<?> retryJob;

    public DenonMarantzHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (connector == null) {
            return;
        }

        if (connector instanceof DenonMarantzHttpConnector && command instanceof RefreshType) {
            // Refreshing individual channels isn't supported by the Http connector.
            // The connector refreshes all channels together at the configured polling interval.
            return;
        }

        try {
            switch (channelUID.getId()) {
                case CHANNEL_POWER:
                    connector.sendPowerCommand(command, 0);
                    break;
                case CHANNEL_MAIN_ZONE_POWER:
                    connector.sendPowerCommand(command, 1);
                    break;
                case CHANNEL_MUTE:
                    connector.sendMuteCommand(command, 1);
                    break;
                case CHANNEL_MAIN_VOLUME:
                    connector.sendVolumeCommand(command, 1);
                    break;
                case CHANNEL_MAIN_VOLUME_DB:
                    connector.sendVolumeDbCommand(command, 1);
                    break;
                case CHANNEL_INPUT:
                    connector.sendInputCommand(command, 1);
                    break;
                case CHANNEL_SURROUND_PROGRAM:
                    connector.sendSurroundProgramCommand(command);
                    break;
                case CHANNEL_COMMAND:
                    connector.sendCustomCommand(command);
                    break;

                case CHANNEL_ZONE2_POWER:
                    connector.sendPowerCommand(command, 2);
                    break;
                case CHANNEL_ZONE2_MUTE:
                    connector.sendMuteCommand(command, 2);
                    break;
                case CHANNEL_ZONE2_VOLUME:
                    connector.sendVolumeCommand(command, 2);
                    break;
                case CHANNEL_ZONE2_VOLUME_DB:
                    connector.sendVolumeDbCommand(command, 2);
                    break;
                case CHANNEL_ZONE2_INPUT:
                    connector.sendInputCommand(command, 2);
                    break;

                case CHANNEL_ZONE3_POWER:
                    connector.sendPowerCommand(command, 3);
                    break;
                case CHANNEL_ZONE3_MUTE:
                    connector.sendMuteCommand(command, 3);
                    break;
                case CHANNEL_ZONE3_VOLUME:
                    connector.sendVolumeCommand(command, 3);
                    break;
                case CHANNEL_ZONE3_VOLUME_DB:
                    connector.sendVolumeDbCommand(command, 3);
                    break;
                case CHANNEL_ZONE3_INPUT:
                    connector.sendInputCommand(command, 3);
                    break;

                case CHANNEL_ZONE4_POWER:
                    connector.sendPowerCommand(command, 4);
                    break;
                case CHANNEL_ZONE4_MUTE:
                    connector.sendMuteCommand(command, 4);
                    break;
                case CHANNEL_ZONE4_VOLUME:
                    connector.sendVolumeCommand(command, 4);
                    break;
                case CHANNEL_ZONE4_VOLUME_DB:
                    connector.sendVolumeDbCommand(command, 4);
                    break;
                case CHANNEL_ZONE4_INPUT:
                    connector.sendInputCommand(command, 4);
                    break;

                default:
                    throw new UnsupportedCommandTypeException();
            }
        } catch (UnsupportedCommandTypeException e) {
            logger.debug("Unsupported command {} for channel {}", command, channelUID.getId());
        }
    }

    public boolean checkConfiguration() {
        // prevent too low values for polling interval
        if (config.httpPollingInterval < 5) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The polling interval should be at least 5 seconds!");
            return false;
        }
        // Check zone count is within supported range
        if (config.getZoneCount() < 1 || config.getZoneCount() > 4) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "This binding supports 1 to 4 zones. Please update the zone count.");
            return false;
        }
        return true;
    }

    /**
     * Try to auto configure the connection type (Telnet or HTTP) for unmanaged Things.
     */
    private void autoConfigure() throws InterruptedException {
        /*
         * The isTelnet parameter has no default.
         * When not set we will try to auto-detect the correct values
         * for isTelnet and zoneCount and update the Thing accordingly.
         */
        if (config.isTelnet() == null) {
            logger.debug("Trying to auto-detect the connection.");
            ContentResponse response;
            boolean telnetEnable = true;
            int httpPort = 80;
            boolean httpApiUsable = false;

            // try to reach the HTTP API at port 80 (most models, except Denon ...H should respond.
            String host = config.getHost();
            try {
                response = httpClient.newRequest("http://" + host + "/goform/Deviceinfo.xml")
                        .timeout(3, TimeUnit.SECONDS).send();
                if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                    logger.debug("We can access the HTTP API, disabling the Telnet mode by default.");
                    telnetEnable = false;
                    httpApiUsable = true;
                }
            } catch (TimeoutException | ExecutionException e) {
                logger.debug("Error when trying to access AVR using HTTP on port 80, reverting to Telnet mode.", e);
            }

            if (telnetEnable) {
                // the above attempt failed. Let's try on port 8080, as for some models a subset of the HTTP API is
                // available
                try {
                    response = httpClient.newRequest("http://" + host + ":8080/goform/Deviceinfo.xml")
                            .timeout(3, TimeUnit.SECONDS).send();
                    if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                        logger.debug(
                                "This model responds to HTTP port 8080, we use this port to retrieve the number of zones.");
                        httpPort = 8080;
                        httpApiUsable = true;
                    }
                } catch (TimeoutException | ExecutionException e) {
                    logger.debug("Additionally tried to connect to port 8080, this also failed", e);
                }
            }

            // default zone count
            int zoneCount = 2;

            // try to determine the zone count by checking the Deviceinfo.xml file
            if (httpApiUsable) {
                int status = 0;
                response = null;
                try {
                    response = httpClient.newRequest("http://" + host + ":" + httpPort + "/goform/Deviceinfo.xml")
                            .timeout(3, TimeUnit.SECONDS).send();
                    status = response.getStatus();
                } catch (TimeoutException | ExecutionException e) {
                    logger.debug("Failed in fetching the Deviceinfo.xml to determine zone count", e);
                }

                if (status == HttpURLConnection.HTTP_OK && response != null) {
                    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                    try {
                        // see
                        // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
                        domFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                        domFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                        domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                        domFactory.setXIncludeAware(false);
                        domFactory.setExpandEntityReferences(false);
                        DocumentBuilder builder;
                        builder = domFactory.newDocumentBuilder();
                        Document dDoc = builder.parse(new InputSource(new StringReader(response.getContentAsString())));
                        XPath xPath = XPathFactory.newInstance().newXPath();
                        Node node = (Node) xPath.evaluate("/Device_Info/DeviceZones/text()", dDoc, XPathConstants.NODE);
                        if (node != null) {
                            String nodeValue = node.getNodeValue();
                            logger.trace("/Device_Info/DeviceZones/text() = {}", nodeValue);
                            zoneCount = Integer.parseInt(nodeValue);
                            logger.debug("Discovered number of zones: {}", zoneCount);
                        }
                    } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException
                            | NumberFormatException e) {
                        logger.debug("Something went wrong with looking up the zone count in Deviceinfo.xml: {}",
                                e.getMessage());
                    }
                }
            }
            config.setTelnet(telnetEnable);
            config.setZoneCount(zoneCount);
            Configuration configuration = editConfiguration();
            configuration.put(PARAMETER_TELNET_ENABLED, telnetEnable);
            configuration.put(PARAMETER_ZONE_COUNT, zoneCount);
            updateConfiguration(configuration);
        }
    }

    @Override
    public void initialize() {
        cancelRetry();
        config = getConfigAs(DenonMarantzConfiguration.class);

        // Configure Connection type (Telnet/HTTP) and number of zones
        // Note: this only happens for discovered Things
        try {
            autoConfigure();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!checkConfiguration()) {
            return;
        }

        denonMarantzState = new DenonMarantzState(this);
        configureZoneChannels();
        updateStatus(ThingStatus.UNKNOWN);
        // create connection (either Telnet or HTTP)
        // ThingStatus ONLINE/OFFLINE is set when AVR status is known.
        createConnection();
    }

    private void createConnection() {
        if (connector != null) {
            connector.dispose();
        }
        connector = connectorFactory.getConnector(config, denonMarantzState, scheduler, httpClient,
                this.getThing().getUID().getAsString());
        connector.connect();
    }

    private void cancelRetry() {
        ScheduledFuture<?> localRetryJob = retryJob;
        if (localRetryJob != null && !localRetryJob.isDone()) {
            localRetryJob.cancel(false);
        }
    }

    private void configureZoneChannels() {
        logger.debug("Configuring zone channels");
        Integer zoneCount = config.getZoneCount();
        List<Channel> channels = new ArrayList<>(this.getThing().getChannels());
        boolean channelsUpdated = false;

        // construct a set with the existing channel type UIDs, to quickly check
        Set<String> currentChannels = new HashSet<>();
        channels.forEach(channel -> currentChannels.add(channel.getUID().getId()));

        Set<Entry<String, ChannelTypeUID>> channelsToRemove = new HashSet<>();

        if (zoneCount > 1) {
            List<Entry<String, ChannelTypeUID>> channelsToAdd = new ArrayList<>(ZONE2_CHANNEL_TYPES.entrySet());

            if (zoneCount > 2) {
                // add channels for zone 3
                channelsToAdd.addAll(ZONE3_CHANNEL_TYPES.entrySet());
                if (zoneCount > 3) {
                    // add channels for zone 4 (more zones currently not supported)
                    channelsToAdd.addAll(ZONE4_CHANNEL_TYPES.entrySet());
                } else {
                    channelsToRemove.addAll(ZONE4_CHANNEL_TYPES.entrySet());
                }
            } else {
                channelsToRemove.addAll(ZONE3_CHANNEL_TYPES.entrySet());
                channelsToRemove.addAll(ZONE4_CHANNEL_TYPES.entrySet());
            }

            // filter out the already existing channels
            channelsToAdd.removeIf(c -> currentChannels.contains(c.getKey()));

            // add the channels that were not yet added
            if (!channelsToAdd.isEmpty()) {
                for (Entry<String, ChannelTypeUID> entry : channelsToAdd) {
                    String itemType = CHANNEL_ITEM_TYPES.get(entry.getKey());
                    Channel channel = ChannelBuilder
                            .create(new ChannelUID(this.getThing().getUID(), entry.getKey()), itemType)
                            .withType(entry.getValue()).build();
                    channels.add(channel);
                }
                channelsUpdated = true;
            } else {
                logger.debug("No zone channels have been added");
            }
        } else {
            channelsToRemove.addAll(ZONE2_CHANNEL_TYPES.entrySet());
            channelsToRemove.addAll(ZONE3_CHANNEL_TYPES.entrySet());
            channelsToRemove.addAll(ZONE4_CHANNEL_TYPES.entrySet());
        }

        // filter out the non-existing channels
        channelsToRemove.removeIf(c -> !currentChannels.contains(c.getKey()));

        // remove the channels that were not yet added
        if (!channelsToRemove.isEmpty()) {
            for (Entry<String, ChannelTypeUID> entry : channelsToRemove) {
                if (channels.removeIf(c -> (entry.getKey()).equals(c.getUID().getId()))) {
                    logger.trace("Removed channel {}", entry.getKey());
                } else {
                    logger.trace("Could NOT remove channel {}", entry.getKey());
                }
            }
            channelsUpdated = true;
        } else {
            logger.debug("No zone channels have been removed");
        }

        // update Thing if channels changed
        if (channelsUpdated) {
            updateThing(editThing().withChannels(channels).build());
        }
    }

    @Override
    public void dispose() {
        if (connector != null) {
            connector.dispose();
            connector = null;
        }
        cancelRetry();
        super.dispose();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        String channelID = channelUID.getId();
        if (isLinked(channelID)) {
            State state = denonMarantzState.getStateForChannelID(channelID);
            if (state != null) {
                updateState(channelID, state);
            }
        }
    }

    @Override
    public void stateChanged(String channelID, State state) {
        logger.debug("Received state {} for channelID {}", state, channelID);

        // Don't flood the log with thing 'updated: ONLINE' each time a single channel changed
        if (this.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        updateState(channelID, state);
    }

    @Override
    public void connectionError(String errorMessage) {
        if (this.getThing().getStatus() != ThingStatus.OFFLINE) {
            // Don't flood the log with thing 'updated: OFFLINE' when already offline
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
        connector.dispose();
        retryJob = scheduler.schedule(this::createConnection, RETRY_TIME_SECONDS, TimeUnit.SECONDS);
    }
}
