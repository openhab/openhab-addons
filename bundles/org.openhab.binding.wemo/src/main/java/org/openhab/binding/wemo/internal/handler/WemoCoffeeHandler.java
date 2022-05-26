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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.DateTimeType;
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

    private final Object jobLock = new Object();

    private @Nullable ScheduledFuture<?> pollingJob;

    public WemoCoffeeHandler(Thing thing, UpnpIOService upnpIOService, UpnpService upnpService,
            WemoHttpCall wemoHttpCaller) {
        super(thing, upnpIOService, upnpService, wemoHttpCaller);

        logger.debug("Creating a WemoCoffeeHandler for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        super.initialize();
        Configuration configuration = getConfig();

        if (configuration.get(UDN) != null) {
            logger.debug("Initializing WemoCoffeeHandler for UDN '{}'", configuration.get(UDN));
            addSubscription(DEVICEEVENT);
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 0, DEFAULT_REFRESH_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/config-status.error.missing-udn");
        }
    }

    @Override
    public void dispose() {
        logger.debug("WemoCoffeeHandler disposed.");
        ScheduledFuture<?> job = this.pollingJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        this.pollingJob = null;
        super.dispose();
    }

    private void poll() {
        synchronized (jobLock) {
            if (pollingJob == null) {
                return;
            }
            try {
                logger.debug("Polling job");

                // Check if the Wemo device is set in the UPnP service registry
                if (!isUpnpDeviceRegistered()) {
                    logger.debug("UPnP device {} not yet registered", getUDN());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "@text/config-status.pending.device-not-registered [\"" + getUDN() + "\"]");
                    return;
                }
                updateWemoState();
            } catch (Exception e) {
                logger.debug("Exception during poll: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String wemoURL = getWemoURL(BASICACTION);
        if (wemoURL == null) {
            logger.debug("Failed to send command '{}' for device '{}': URL cannot be created", command,
                    getThing().getUID());
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

                        wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                        updateState(CHANNEL_STATE, OnOffType.ON);
                        State newMode = new StringType("Brewing");
                        updateState(CHANNEL_COFFEE_MODE, newMode);
                        updateStatus(ThingStatus.ONLINE);
                    } catch (Exception e) {
                        logger.warn("Failed to send command '{}' for device '{}': {}", command, getThing().getUID(),
                                e.getMessage());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    }
                }
                // if command.equals(OnOffType.OFF) we do nothing because WeMo Coffee Maker cannot be switched
                // off remotely
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
    protected void updateWemoState() {
        String actionService = DEVICEACTION;
        String wemoURL = getWemoURL(actionService);
        if (wemoURL == null) {
            logger.debug("Failed to get actual state for device '{}': URL cannot be created", getThing().getUID());
            return;
        }
        try {
            String action = "GetAttributes";
            String soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
            String content = createStateRequestContent(action, actionService);
            String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
            try {
                String stringParser = substringBetween(wemoCallResponse, "<attributeList>", "</attributeList>");

                // Due to Belkins bad response formatting, we need to run this twice.
                stringParser = unescapeXml(stringParser);
                stringParser = unescapeXml(stringParser);

                logger.trace("CoffeeMaker response '{}' for device '{}' received", stringParser, getThing().getUID());

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
                                    updateState(CHANNEL_COFFEE_MODE, newMode);
                                    break;
                                case "1":
                                    updateState(CHANNEL_STATE, OnOffType.OFF);
                                    newMode = new StringType("PlaceCarafe");
                                    updateState(CHANNEL_COFFEE_MODE, newMode);
                                    break;
                                case "2":
                                    updateState(CHANNEL_STATE, OnOffType.OFF);
                                    newMode = new StringType("RefillWater");
                                    updateState(CHANNEL_COFFEE_MODE, newMode);
                                    break;
                                case "3":
                                    updateState(CHANNEL_STATE, OnOffType.OFF);
                                    newMode = new StringType("Ready");
                                    updateState(CHANNEL_COFFEE_MODE, newMode);
                                    break;
                                case "4":
                                    updateState(CHANNEL_STATE, OnOffType.ON);
                                    newMode = new StringType("Brewing");
                                    updateState(CHANNEL_COFFEE_MODE, newMode);
                                    break;
                                case "5":
                                    updateState(CHANNEL_STATE, OnOffType.OFF);
                                    newMode = new StringType("Brewed");
                                    updateState(CHANNEL_COFFEE_MODE, newMode);
                                    break;
                                case "6":
                                    updateState(CHANNEL_STATE, OnOffType.OFF);
                                    newMode = new StringType("CleaningBrewing");
                                    updateState(CHANNEL_COFFEE_MODE, newMode);
                                    break;
                                case "7":
                                    updateState(CHANNEL_STATE, OnOffType.OFF);
                                    newMode = new StringType("CleaningSoaking");
                                    updateState(CHANNEL_COFFEE_MODE, newMode);
                                    break;
                                case "8":
                                    updateState(CHANNEL_STATE, OnOffType.OFF);
                                    newMode = new StringType("BrewFailCarafeRemoved");
                                    updateState(CHANNEL_COFFEE_MODE, newMode);
                                    break;
                            }
                            break;
                        case "ModeTime":
                            newAttributeValue = new DecimalType(attributeValue);
                            updateState(CHANNEL_MODE_TIME, newAttributeValue);
                            break;
                        case "TimeRemaining":
                            newAttributeValue = new DecimalType(attributeValue);
                            updateState(CHANNEL_TIME_REMAINING, newAttributeValue);
                            break;
                        case "WaterLevelReached":
                            newAttributeValue = new DecimalType(attributeValue);
                            updateState(CHANNEL_WATER_LEVEL_REACHED, newAttributeValue);
                            break;
                        case "CleanAdvise":
                            newAttributeValue = "0".equals(attributeValue) ? OnOffType.OFF : OnOffType.ON;
                            updateState(CHANNEL_CLEAN_ADVISE, newAttributeValue);
                            break;
                        case "FilterAdvise":
                            newAttributeValue = "0".equals(attributeValue) ? OnOffType.OFF : OnOffType.ON;
                            updateState(CHANNEL_FILTER_ADVISE, newAttributeValue);
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
                                updateState(CHANNEL_LAST_CLEANED, newAttributeValue);
                            }
                            break;
                    }
                }
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                logger.warn("Failed to parse attributeList for WeMo CoffeMaker '{}'", this.getThing().getUID(), e);
            }
        } catch (Exception e) {
            logger.warn("Failed to get attributes for device '{}'", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public @Nullable State getDateTimeState(String attributeValue) {
        long value = 0;
        try {
            value = Long.parseLong(attributeValue);
        } catch (NumberFormatException e) {
            logger.warn("Unable to parse attributeValue '{}' for device '{}'; expected long", attributeValue,
                    getThing().getUID());
            return null;
        }
        ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochSecond(value), TimeZone.getDefault().toZoneId());
        State dateTimeState = new DateTimeType(zoned);
        logger.trace("New attribute brewed '{}' received", dateTimeState);
        return dateTimeState;
    }
}
