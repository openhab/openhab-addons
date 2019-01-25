/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.wemo.internal.handler;

import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;

import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The {@link WemoMakerHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */

public class WemoMakerHandler extends AbstractWemoHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(WemoMakerHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_MAKER);

    private UpnpIOService service;

    /**
     * The default refresh interval in Seconds.
     */
    private final int DEFAULT_REFRESH_INTERVAL = 15;

    private ScheduledFuture<?> refreshJob;

    private final Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                updateWemoState();
            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    };

    public WemoMakerHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpcaller) {
        super(thing);

        this.wemoHttpCaller = wemoHttpcaller;

        logger.debug("Creating a WemoMakerHandler for thing '{}'", getThing().getUID());

        if (upnpIOService != null) {
            this.service = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }
    }

    @Override
    public void initialize() {
        Configuration configuration = getConfig();

        if (configuration.get("udn") != null) {
            logger.debug("Initializing WemoMakerHandler for UDN '{}'", configuration.get("udn"));
            onUpdate();
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Cannot initalize WemoMakerHandler. UDN not set.");
        }
    }

    @Override
    public void dispose() {
        logger.debug("WeMoMakerHandler disposed.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        if (command instanceof RefreshType) {
            try {
                updateWemoState();
            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        } else if (channelUID.getId().equals(CHANNEL_RELAY)) {
            if (command instanceof OnOffType) {
                try {
                    String binaryState = null;

                    if (command.equals(OnOffType.ON)) {
                        binaryState = "1";
                    } else if (command.equals(OnOffType.OFF)) {
                        binaryState = "0";
                    }

                    String soapHeader = "\"urn:Belkin:service:basicevent:1#SetBinaryState\"";

                    String content = "<?xml version=\"1.0\"?>"
                            + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                            + "<s:Body>" + "<u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">"
                            + "<BinaryState>" + binaryState + "</BinaryState>" + "</u:SetBinaryState>" + "</s:Body>"
                            + "</s:Envelope>";

                    String wemoURL = getWemoURL("basicevent");

                    if (wemoURL != null) {
                        @SuppressWarnings("unused")
                        String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                    }
                } catch (Exception e) {
                    logger.error("Failed to send command '{}' for device '{}' ", command, getThing().getUID(), e);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private synchronized void onSubscription() {
    }

    @SuppressWarnings("unused")
    private synchronized void removeSubscription() {
    }

    private synchronized void onUpdate() {
        if (service.isRegistered(this)) {
            if (refreshJob == null || refreshJob.isCancelled()) {
                Configuration config = getThing().getConfiguration();
                int refreshInterval = DEFAULT_REFRESH_INTERVAL;
                Object refreshConfig = config.get("refresh");
                if (refreshConfig != null) {
                    refreshInterval = ((BigDecimal) refreshConfig).intValue();
                }
                refreshJob = scheduler.scheduleWithFixedDelay(refreshRunnable, 0, refreshInterval, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public String getUDN() {
        return (String) this.getThing().getConfiguration().get(UDN);
    }

    /**
     * The {@link updateWemoState} polls the actual state of a WeMo Maker.
     */
    protected void updateWemoState() {
        String action = "GetAttributes";
        String actionService = "deviceevent";

        String soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
        String content = "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>" + "<u:" + action + " xmlns:u=\"urn:Belkin:service:" + actionService + ":1\">" + "</u:"
                + action + ">" + "</s:Body>" + "</s:Envelope>";

        try {
            String wemoURL = getWemoURL(actionService);
            if (wemoURL != null) {
                String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                if (wemoCallResponse != null) {
                    try {
                        String stringParser = StringUtils.substringBetween(wemoCallResponse, "<attributeList>",
                                "</attributeList>");

                        // Due to Belkins bad response formatting, we need to run this twice.
                        stringParser = StringEscapeUtils.unescapeXml(stringParser);
                        stringParser = StringEscapeUtils.unescapeXml(stringParser);

                        logger.trace("Maker response '{}' for device '{}' received", stringParser, getThing().getUID());

                        stringParser = "<data>" + stringParser + "</data>";

                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(stringParser));

                        Document doc = db.parse(is);
                        NodeList nodes = doc.getElementsByTagName("attribute");

                        // iterate the attributes
                        for (int i = 0; i < nodes.getLength(); i++) {
                            Element element = (Element) nodes.item(i);

                            NodeList deviceIndex = element.getElementsByTagName("name");
                            Element line = (Element) deviceIndex.item(0);
                            String attributeName = getCharacterDataFromElement(line);
                            logger.trace("attributeName: {}", attributeName);

                            NodeList deviceID = element.getElementsByTagName("value");
                            line = (Element) deviceID.item(0);
                            String attributeValue = getCharacterDataFromElement(line);
                            logger.trace("attributeValue: {}", attributeValue);

                            switch (attributeName) {
                                case "Switch":
                                    State relayState = attributeValue.equals("0") ? OnOffType.OFF : OnOffType.ON;
                                    if (relayState != null) {
                                        logger.debug("New relayState '{}' for device '{}' received", relayState,
                                                getThing().getUID());
                                        updateState(CHANNEL_RELAY, relayState);
                                    }
                                    break;
                                case "Sensor":
                                    State sensorState = attributeValue.equals("1") ? OnOffType.OFF : OnOffType.ON;
                                    if (sensorState != null) {
                                        logger.debug("New sensorState '{}' for device '{}' received", sensorState,
                                                getThing().getUID());
                                        updateState(CHANNEL_SENSOR, sensorState);
                                    }
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to parse attributeList for WeMo Maker '{}'", this.getThing().getUID(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get attributes for device '{}'", getThing().getUID(), e);
        }
    }

    public String getWemoURL(String actionService) {
        URL descriptorURL = service.getDescriptorURL(this);
        String wemoURL = null;
        if (descriptorURL != null) {
            String deviceURL = StringUtils.substringBefore(descriptorURL.toString(), "/setup.xml");
            wemoURL = deviceURL + "/upnp/control/" + actionService + "1";
            return wemoURL;
        }
        return null;
    }

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "?";
    }

    @Override
    public void onStatusChanged(boolean status) {
    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {
    }

}
