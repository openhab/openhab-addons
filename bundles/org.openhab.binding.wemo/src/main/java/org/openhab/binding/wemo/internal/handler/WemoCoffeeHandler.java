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
import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
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
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
public class WemoCoffeeHandler extends AbstractWemoHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(WemoCoffeeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_COFFEE);

    private Map<String, Boolean> subscriptionState = new HashMap<>();

    private UpnpIOService service;

    private WemoHttpCall wemoCall;

    private @Nullable ScheduledFuture<?> refreshJob;

    private final Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (!isUpnpDeviceRegistered()) {
                    logger.debug("WeMo UPnP device {} not yet registered", getUDN());
                }

                updateWemoState();
                onSubscription();
            } catch (Exception e) {
                logger.debug("Exception during poll", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    };

    public WemoCoffeeHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing, wemoHttpCaller);

        this.wemoCall = wemoHttpCaller;
        this.service = upnpIOService;

        logger.debug("Creating a WemoCoffeeHandler V0.4 for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        Configuration configuration = getConfig();

        if (configuration.get("udn") != null) {
            logger.debug("Initializing WemoCoffeeHandler for UDN '{}'", configuration.get("udn"));
            onSubscription();
            onUpdate();
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Cannot initalize WemoCoffeeHandler. UDN not set.");
        }
    }

    @Override
    public void dispose() {
        logger.debug("WeMoCoffeeHandler disposed.");

        ScheduledFuture<?> job = refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        refreshJob = null;
        removeSubscription();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);

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

                        URL descriptorURL = service.getDescriptorURL(this);
                        String wemoURL = getWemoURL(descriptorURL, "basicevent");

                        if (wemoURL != null) {
                            String wemoCallResponse = wemoCall.executeCall(wemoURL, soapHeader, content);
                            if (wemoCallResponse != null) {
                                updateState(CHANNEL_STATE, OnOffType.ON);
                                State newMode = new StringType("Brewing");
                                updateState(CHANNEL_COFFEEMODE, newMode);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to send command '{}' for device '{}': {}", command, getThing().getUID(),
                                e.getMessage());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    }
                }
                // if command.equals(OnOffType.OFF) we do nothing because WeMo Coffee Maker cannot be switched off
                // remotely
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        if (service != null) {
            logger.debug("WeMo {}: Subscription to service {} {}", getUDN(), service,
                    succeeded ? "succeeded" : "failed");
            subscriptionState.put(service, succeeded);
        }
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        // We can subscribe to GENA events, but there is no usefull response right now.
    }

    private synchronized void onSubscription() {
        if (service.isRegistered(this)) {
            logger.debug("Checking WeMo GENA subscription for '{}'", this);

            String subscription = "deviceevent1";
            if (subscriptionState.get(subscription) == null) {
                logger.debug("Setting up GENA subscription {}: Subscribing to service {}...", getUDN(), subscription);
                service.addSubscription(this, subscription, SUBSCRIPTION_DURATION_SECONDS);
                subscriptionState.put(subscription, true);
            }
        } else {
            logger.debug("Setting up WeMo GENA subscription for '{}' FAILED - service.isRegistered(this) is FALSE",
                    this);
        }
    }

    private synchronized void removeSubscription() {
        logger.debug("Removing WeMo GENA subscription for '{}'", this);

        if (service.isRegistered(this)) {
            String subscription = "deviceevent1";
            if (subscriptionState.get(subscription) != null) {
                logger.debug("WeMo {}: Unsubscribing from service {}...", getUDN(), subscription);
                service.removeSubscription(this, subscription);
            }

            subscriptionState = new HashMap<>();
            service.unregisterParticipant(this);
        }
    }

    private synchronized void onUpdate() {
        ScheduledFuture<?> job = refreshJob;
        if (job == null || job.isCancelled()) {
            Configuration config = getThing().getConfiguration();
            int refreshInterval = DEFAULT_REFRESH_INTERVALL_SECONDS;
            Object refreshConfig = config.get("pollingInterval");
            if (refreshConfig != null) {
                refreshInterval = ((BigDecimal) refreshConfig).intValue();
                logger.debug("Setting WemoCoffeeHandler refreshInterval to '{}' seconds", refreshInterval);
            }
            refreshJob = scheduler.scheduleWithFixedDelay(refreshRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private boolean isUpnpDeviceRegistered() {
        return service.isRegistered(this);
    }

    @Override
    public String getUDN() {
        return (String) this.getThing().getConfiguration().get(UDN);
    }

    /**
     * The {@link updateWemoState} polls the actual state of a WeMo CoffeeMaker.
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
            URL descriptorURL = service.getDescriptorURL(this);
            String wemoURL = getWemoURL(descriptorURL, actionService);

            if (wemoURL != null) {
                String wemoCallResponse = wemoCall.executeCall(wemoURL, soapHeader, content);
                if (wemoCallResponse != null) {
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
                                    newAttributeValue = attributeValue.equals("0") ? OnOffType.OFF : OnOffType.ON;
                                    updateState(CHANNEL_CLEANADVISE, newAttributeValue);
                                    break;
                                case "FilterAdvise":
                                    newAttributeValue = attributeValue.equals("0") ? OnOffType.OFF : OnOffType.ON;
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
                        logger.error("Failed to parse attributeList for WeMo CoffeMaker '{}'", this.getThing().getUID(),
                                e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get attributes for device '{}'", getThing().getUID(), e);
        }
    }

    public @Nullable State getDateTimeState(String attributeValue) {
        long value = 0;
        try {
            value = Long.parseLong(attributeValue);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse attributeValue '{}' for device '{}'; expected long", attributeValue,
                    getThing().getUID());
            return null;
        }
        ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochSecond(value), TimeZone.getDefault().toZoneId());
        State dateTimeState = new DateTimeType(zoned);
        logger.trace("New attribute brewed '{}' received", dateTimeState);
        return dateTimeState;
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
}
