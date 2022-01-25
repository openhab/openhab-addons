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
package org.openhab.binding.wemo.internal.handler;

import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;
import static org.openhab.binding.wemo.internal.WemoUtil.*;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The {@link WemoCoffeeHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 */
@NonNullByDefault
public class WemoCoffeeHandler extends WemoBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WemoCoffeeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_COFFEE);

    public Collection<String> SERVICE_SUBSCRIPTIONS = Arrays.asList(DEVICEEVENT);

    public WemoCoffeeHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing, upnpIOService, wemoHttpCaller);

        logger.debug("Creating a WemoCoffeeHandler for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String localHost = getHost();
        if (localHost.isEmpty()) {
            logger.error("Failed to send command '{}' for device '{}': IP address missing", command,
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/config-status.error.missing-ip");
            return;
        }
        String wemoURL = getWemoURL(localHost, BASICACTION);
        if (wemoURL == null) {
            logger.error("Failed to send command '{}' for device '{}': URL cannot be created", command,
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/config-status.error.missing-url");
            return;
        }
        if (command instanceof RefreshType) {
            try {
                updateWemoState();
            } catch (Exception e) {
                logger.debug("Exception during poll", e);
            }
        } else if (channelUID.getId().equals(CHANNEL_STATE)) {
            if (command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    try {
                        String soapHeader = "\"urn:Belkin:service:deviceevent:1#SetAttributes\"";

                        String content = "<?xml version=\"1.0\"?>"
                                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                                + "<s:Body>" + "<u:SetAttributes xmlns:u=\"urn:Belkin:service:deviceevent:1\">"
                                + "<attributeList>&lt;attribute&gt;&lt;name&gt;Brewed&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;"
                                + "&lt;attribute&gt;&lt;name&gt;LastCleaned&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;&lt;attribute&gt;"
                                + "&lt;name&gt;ModeTime&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;&lt;attribute&gt;&lt;name&gt;Brewing&lt;/name&gt;"
                                + "&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;&lt;attribute&gt;&lt;name&gt;TimeRemaining&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;"
                                + "&lt;/attribute&gt;&lt;attribute&gt;&lt;name&gt;WaterLevelReached&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;&lt;"
                                + "attribute&gt;&lt;name&gt;Mode&lt;/name&gt;&lt;value&gt;4&lt;/value&gt;&lt;/attribute&gt;&lt;attribute&gt;&lt;name&gt;CleanAdvise&lt;/name&gt;"
                                + "&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;&lt;attribute&gt;&lt;name&gt;FilterAdvise&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;"
                                + "&lt;attribute&gt;&lt;name&gt;Cleaning&lt;/name&gt;&lt;value&gt;NULL&lt;/value&gt;&lt;/attribute&gt;</attributeList>"
                                + "</u:SetAttributes>" + "</s:Body>" + "</s:Envelope>";

                        String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                        if (wemoCallResponse != null) {
                            updateState(CHANNEL_STATE, OnOffType.ON);
                            State newMode = new StringType("Brewing");
                            updateState(CHANNEL_COFFEEMODE, newMode);
                            if (logger.isTraceEnabled()) {
                                logger.trace("wemoCall to URL '{}' for device '{}'", wemoURL, getThing().getUID());
                                logger.trace("wemoCall with soapHeader '{}' for device '{}'", soapHeader,
                                        getThing().getUID());
                                logger.trace("wemoCall with content '{}' for device '{}'", content,
                                        getThing().getUID());
                                logger.trace("wemoCall with response '{}' for device '{}'", wemoCallResponse,
                                        getThing().getUID());
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to send command '{}' for device '{}': {}", command, getThing().getUID(),
                                e.getMessage());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    }
                }
                // if command.equals(OnOffType.OFF) we do nothing because WeMo Coffee Maker cannot be switched
                // off
                // remotely
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        // We can subscribe to GENA events, but there is no usefull response right now.
    }

    /**
     * The {@link updateWemoState} polls the actual state of a WeMo CoffeeMaker.
     */
    @Override
    protected void updateWemoState() {
        String localHost = getHost();
        if (localHost.isEmpty()) {
            logger.error("Failed to get actual state for device '{}': IP address missing", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/config-status.error.missing-ip");
            return;
        }
        String actionService = DEVICEACTION;
        String wemoURL = getWemoURL(host, actionService);
        if (wemoURL == null) {
            logger.error("Failed to get actual state for device '{}': URL cannot be created", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/config-status.error.missing-url");
            return;
        }
        try {
            String action = "GetAttributes";
            String soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
            String content = createStateRequestContent(action, actionService);
            String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
            if (wemoCallResponse != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("wemoCall to URL '{}' for device '{}'", wemoURL, getThing().getUID());
                    logger.trace("wemoCall with soapHeader '{}' for device '{}'", soapHeader, getThing().getUID());
                    logger.trace("wemoCall with content '{}' for device '{}'", content, getThing().getUID());
                    logger.trace("wemoCall with response '{}' for device '{}'", wemoCallResponse, getThing().getUID());
                }
                try {
                    String stringParser = substringBetween(wemoCallResponse, "<attributeList>", "</attributeList>");

                    // Due to Belkins bad response formatting, we need to run this twice.
                    stringParser = unescapeXml(stringParser);
                    stringParser = unescapeXml(stringParser);

                    logger.trace("CoffeeMaker response '{}' for device '{}' received", stringParser,
                            getThing().getUID());

                    stringParser = "<data>" + stringParser + "</data>";

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    // see
                    // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
                    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    dbf.setXIncludeAware(false);
                    dbf.setExpandEntityReferences(false);
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
                            case "Mode":
                                State newMode = new StringType("Brewing");
                                State newAttributeValue;

                                switch (attributeValue) {
                                    case "0":
                                        updateState(CHANNEL_STATE, OnOffType.ON);
                                        newMode = new StringType("Refill");
                                        updateState(CHANNEL_COFFEEMODE, newMode);
                                        break;
                                    case "1":
                                        updateState(CHANNEL_STATE, OnOffType.OFF);
                                        newMode = new StringType("PlaceCarafe");
                                        updateState(CHANNEL_COFFEEMODE, newMode);
                                        break;
                                    case "2":
                                        updateState(CHANNEL_STATE, OnOffType.OFF);
                                        newMode = new StringType("RefillWater");
                                        updateState(CHANNEL_COFFEEMODE, newMode);
                                        break;
                                    case "3":
                                        updateState(CHANNEL_STATE, OnOffType.OFF);
                                        newMode = new StringType("Ready");
                                        updateState(CHANNEL_COFFEEMODE, newMode);
                                        break;
                                    case "4":
                                        updateState(CHANNEL_STATE, OnOffType.ON);
                                        newMode = new StringType("Brewing");
                                        updateState(CHANNEL_COFFEEMODE, newMode);
                                        break;
                                    case "5":
                                        updateState(CHANNEL_STATE, OnOffType.OFF);
                                        newMode = new StringType("Brewed");
                                        updateState(CHANNEL_COFFEEMODE, newMode);
                                        break;
                                    case "6":
                                        updateState(CHANNEL_STATE, OnOffType.OFF);
                                        newMode = new StringType("CleaningBrewing");
                                        updateState(CHANNEL_COFFEEMODE, newMode);
                                        break;
                                    case "7":
                                        updateState(CHANNEL_STATE, OnOffType.OFF);
                                        newMode = new StringType("CleaningSoaking");
                                        updateState(CHANNEL_COFFEEMODE, newMode);
                                        break;
                                    case "8":
                                        updateState(CHANNEL_STATE, OnOffType.OFF);
                                        newMode = new StringType("BrewFailCarafeRemoved");
                                        updateState(CHANNEL_COFFEEMODE, newMode);
                                        break;
                                }
                                break;
                            case "ModeTime":
                                newAttributeValue = new DecimalType(attributeValue);
                                updateState(CHANNEL_MODETIME, newAttributeValue);
                                break;
                            case "TimeRemaining":
                                newAttributeValue = new DecimalType(attributeValue);
                                updateState(CHANNEL_TIMEREMAINING, newAttributeValue);
                                break;
                            case "WaterLevelReached":
                                newAttributeValue = new DecimalType(attributeValue);
                                updateState(CHANNEL_WATERLEVELREACHED, newAttributeValue);
                                break;
                            case "CleanAdvise":
                                newAttributeValue = "0".equals(attributeValue) ? OnOffType.OFF : OnOffType.ON;
                                updateState(CHANNEL_CLEANADVISE, newAttributeValue);
                                break;
                            case "FilterAdvise":
                                newAttributeValue = "0".equals(attributeValue) ? OnOffType.OFF : OnOffType.ON;
                                updateState(CHANNEL_FILTERADVISE, newAttributeValue);
                                break;
                            case "Brewed":
                                newAttributeValue = getDateTimeState(attributeValue);
                                if (newAttributeValue != null) {
                                    updateState(CHANNEL_BREWED, newAttributeValue);
                                }
                                break;
                            case "LastCleaned":
                                newAttributeValue = getDateTimeState(attributeValue);
                                if (newAttributeValue != null) {
                                    updateState(CHANNEL_LASTCLEANED, newAttributeValue);
                                }
                                break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse attributeList for WeMo CoffeMaker '{}'", this.getThing().getUID(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get attributes for device '{}'", getThing().getUID(), e);
        }
    }
}
