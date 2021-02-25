/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
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
import org.xml.sax.SAXException;

/**
 * The {@link WemoHolmesHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution;
 */

public class WemoHolmesHandler extends AbstractWemoHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(WemoHolmesHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_PURIFIER);

    /**
     * The default refresh interval in Seconds.
     */
    private static final int DEFAULT_REFRESH_INTERVAL_SECONDS = 120;
    private static final int FILTER_LIFE_DAYS = 330;
    private static final int FILTER_LIFE_MINS = FILTER_LIFE_DAYS * 24 * 60;
    private final Map<String, Boolean> subscriptionState = new HashMap<>();
    private final Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<>());

    private UpnpIOService service;

    private ScheduledFuture<?> refreshJob;

    private final Runnable refreshRunnable = () -> {
        if (!isUpnpDeviceRegistered()) {
            logger.debug("WeMo UPnP device {} not yet registered", getUDN());
        } else {
            updateWemoState();
            onSubscription();
        }
    };

    public WemoHolmesHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemohttpCaller) {
        super(thing);

        this.wemoHttpCaller = wemohttpCaller;

        logger.debug("Creating a WemoHolmesHandler for thing '{}'", getThing().getUID());

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
            logger.debug("Initializing WemoHolmesHandler for UDN '{}'", configuration.get("udn"));
            service.registerParticipant(this);
            onSubscription();
            onUpdate();
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Cannot initalize WemoHolmesHandler. UDN not set.");
        }
    }

    @Override
    public void dispose() {
        logger.debug("WemoHolmesHandler disposed.");

        removeSubscription();

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        String attribute = null;
        String value = null;

        if (command instanceof RefreshType) {
            updateWemoState();
        } else if (CHANNEL_PURIFIERMODE.equals(channelUID.getId())) {
            attribute = "Mode";
            String commandString = command.toString();
            switch (commandString) {
                case "OFF":
                    value = "0";
                    break;
                case "LOW":
                    value = "1";
                    break;
                case "MED":
                    value = "2";
                    break;
                case "HIGH":
                    value = "3";
                    break;
                case "AUTO":
                    value = "4";
                    break;
            }
        } else if (CHANNEL_IONIZER.equals(channelUID.getId())) {
            attribute = "Ionizer";
            if (OnOffType.ON.equals(command)) {
                value = "1";
            } else if (OnOffType.OFF.equals(command)) {
                value = "0";
            }
        } else if (CHANNEL_HUMIDIFIERMODE.equals(channelUID.getId())) {
            attribute = "FanMode";
            String commandString = command.toString();
            switch (commandString) {
                case "OFF":
                    value = "0";
                    break;
                case "MIN":
                    value = "1";
                    break;
                case "LOW":
                    value = "2";
                    break;
                case "MED":
                    value = "3";
                    break;
                case "HIGH":
                    value = "4";
                    break;
                case "MAX":
                    value = "5";
                    break;
            }
        } else if (CHANNEL_DESIREDHUMIDITY.equals(channelUID.getId())) {
            attribute = "DesiredHumidity";
            String commandString = command.toString();
            switch (commandString) {
                case "45":
                    value = "0";
                    break;
                case "50":
                    value = "1";
                    break;
                case "55":
                    value = "2";
                    break;
                case "60":
                    value = "3";
                    break;
                case "100":
                    value = "4";
                    break;
            }
        } else if (CHANNEL_HEATERMODE.equals(channelUID.getId())) {
            attribute = "Mode";
            String commandString = command.toString();
            switch (commandString) {
                case "OFF":
                    value = "0";
                    break;
                case "FROSTPROTECT":
                    value = "1";
                    break;
                case "HIGH":
                    value = "2";
                    break;
                case "LOW":
                    value = "3";
                    break;
                case "ECO":
                    value = "4";
                    break;
            }
        } else if (CHANNEL_TARGETTEMP.equals(channelUID.getId())) {
            attribute = "SetTemperature";
            value = command.toString();
        }
        try {
            String soapHeader = "\"urn:Belkin:service:deviceevent:1#SetAttributes\"";
            String content = "<?xml version=\"1.0\"?>"
                    + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                    + "<s:Body>" + "<u:SetAttributes xmlns:u=\"urn:Belkin:service:deviceevent:1\">"
                    + "<attributeList>&lt;attribute&gt;&lt;name&gt;" + attribute + "&lt;/name&gt;&lt;value&gt;" + value
                    + "&lt;/value&gt;&lt;/attribute&gt;</attributeList>" + "</u:SetAttributes>" + "</s:Body>"
                    + "</s:Envelope>";
            String wemoURL = getWemoURL("deviceevent");

            if (wemoURL != null) {
                wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
            }
        } catch (RuntimeException e) {
            logger.debug("Failed to send command '{}' for device '{}':", command, getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
        logger.debug("WeMo {}: Subscription to service {} {}", getUDN(), service, succeeded ? "succeeded" : "failed");
        subscriptionState.put(service, succeeded);
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {
        logger.debug("Received pair '{}':'{}' (service '{}') for thing '{}'", variable, value, service,
                this.getThing().getUID());

        updateStatus(ThingStatus.ONLINE);
        this.stateMap.put(variable, value);
    }

    private synchronized void onSubscription() {
        if (service.isRegistered(this)) {
            logger.debug("Checking WeMo GENA subscription for '{}'", this);

            String subscription = "basicevent1";

            if ((subscriptionState.get(subscription) == null) || !subscriptionState.get(subscription).booleanValue()) {
                logger.debug("Setting up GENA subscription {}: Subscribing to service {}...", getUDN(), subscription);
                service.addSubscription(this, subscription, SUBSCRIPTION_DURATION);
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
            String subscription = "basicevent1";

            if ((subscriptionState.get(subscription) != null) && subscriptionState.get(subscription).booleanValue()) {
                logger.debug("WeMo {}: Unsubscribing from service {}...", getUDN(), subscription);
                service.removeSubscription(this, subscription);
            }

            subscriptionState.remove(subscription);
            service.unregisterParticipant(this);
        }
    }

    private synchronized void onUpdate() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Configuration config = getThing().getConfiguration();
            int refreshInterval = DEFAULT_REFRESH_INTERVAL_SECONDS;
            Object refreshConfig = config.get("refresh");
            refreshInterval = refreshConfig == null ? DEFAULT_REFRESH_INTERVAL_SECONDS
                    : ((BigDecimal) refreshConfig).intValue();
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
     * The {@link updateWemoState} polls the actual state of a WeMo device and
     * calls {@link onValueReceived} to update the statemap and channels..
     *
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
                    logger.trace("State response '{}' for device '{}' received", wemoCallResponse, getThing().getUID());

                    String stringParser = StringUtils.substringBetween(wemoCallResponse, "<attributeList>",
                            "</attributeList>");

                    // Due to Belkins bad response formatting, we need to run this twice.
                    stringParser = StringEscapeUtils.unescapeXml(stringParser);
                    stringParser = StringEscapeUtils.unescapeXml(stringParser);

                    logger.trace("AirPurifier response '{}' for device '{}' received", stringParser,
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

                        State newMode = new StringType();
                        switch (attributeName) {
                            case "Mode":
                                if ("purifier".equals(getThing().getThingTypeUID().getId())) {
                                    switch (attributeValue) {
                                        case "0":
                                            newMode = new StringType("OFF");
                                            break;
                                        case "1":
                                            newMode = new StringType("LOW");
                                            break;
                                        case "2":
                                            newMode = new StringType("MED");
                                            break;
                                        case "3":
                                            newMode = new StringType("HIGH");
                                            break;
                                        case "4":
                                            newMode = new StringType("AUTO");
                                            break;
                                    }
                                    updateState(CHANNEL_PURIFIERMODE, newMode);
                                } else {
                                    switch (attributeValue) {
                                        case "0":
                                            newMode = new StringType("OFF");
                                            break;
                                        case "1":
                                            newMode = new StringType("FROSTPROTECT");
                                            break;
                                        case "2":
                                            newMode = new StringType("HIGH");
                                            break;
                                        case "3":
                                            newMode = new StringType("LOW");
                                            break;
                                        case "4":
                                            newMode = new StringType("ECO");
                                            break;
                                    }
                                    updateState(CHANNEL_HEATERMODE, newMode);
                                }
                                break;
                            case "Ionizer":
                                switch (attributeValue) {
                                    case "0":
                                        newMode = OnOffType.OFF;
                                        break;
                                    case "1":
                                        newMode = OnOffType.ON;
                                        break;
                                }
                                updateState(CHANNEL_IONIZER, newMode);
                                break;
                            case "AirQuality":
                                switch (attributeValue) {
                                    case "0":
                                        newMode = new StringType("POOR");
                                        break;
                                    case "1":
                                        newMode = new StringType("MODERATE");
                                        break;
                                    case "2":
                                        newMode = new StringType("GOOD");
                                        break;
                                }
                                updateState(CHANNEL_AIRQUALITY, newMode);
                                break;
                            case "FilterLife":
                                int filterLife = Integer.valueOf(attributeValue);
                                if ("purifier".equals(getThing().getThingTypeUID().getId())) {
                                    filterLife = Math.round((filterLife / FILTER_LIFE_MINS) * 100);
                                } else {
                                    filterLife = Math.round((filterLife / 60480) * 100);
                                }
                                updateState(CHANNEL_FILTERLIFE, new PercentType(String.valueOf(filterLife)));
                                break;
                            case "ExpiredFilterTime":
                                switch (attributeValue) {
                                    case "0":
                                        newMode = OnOffType.OFF;
                                        break;
                                    case "1":
                                        newMode = OnOffType.ON;
                                        break;
                                }
                                updateState(CHANNEL_EXPIREDFILTERTIME, newMode);
                                break;
                            case "FilterPresent":
                                switch (attributeValue) {
                                    case "0":
                                        newMode = OnOffType.OFF;
                                        break;
                                    case "1":
                                        newMode = OnOffType.ON;
                                        break;
                                }
                                updateState(CHANNEL_FILTERPRESENT, newMode);
                                break;
                            case "FANMode":
                                switch (attributeValue) {
                                    case "0":
                                        newMode = new StringType("OFF");
                                        break;
                                    case "1":
                                        newMode = new StringType("LOW");
                                        break;
                                    case "2":
                                        newMode = new StringType("MED");
                                        break;
                                    case "3":
                                        newMode = new StringType("HIGH");
                                        break;
                                    case "4":
                                        newMode = new StringType("AUTO");
                                        break;
                                }
                                updateState(CHANNEL_PURIFIERMODE, newMode);
                                break;
                            case "DesiredHumidity":
                                switch (attributeValue) {
                                    case "0":
                                        newMode = new PercentType("45");
                                        break;
                                    case "1":
                                        newMode = new PercentType("50");
                                        break;
                                    case "2":
                                        newMode = new PercentType("55");
                                        break;
                                    case "3":
                                        newMode = new PercentType("60");
                                        break;
                                    case "4":
                                        newMode = new PercentType("100");
                                        break;
                                }
                                updateState(CHANNEL_DESIREDHUMIDITY, newMode);
                                break;
                            case "CurrentHumidity":
                                newMode = new StringType(attributeValue);
                                updateState(CHANNEL_CURRENTHUMIDITY, newMode);
                                break;
                            case "Temperature":
                                newMode = new StringType(attributeValue);
                                updateState(CHANNEL_CURRENTTEMP, newMode);
                                break;
                            case "SetTemperature":
                                newMode = new StringType(attributeValue);
                                updateState(CHANNEL_TARGETTEMP, newMode);
                                break;
                            case "AutoOffTime":
                                newMode = new StringType(attributeValue);
                                updateState(CHANNEL_AUTOOFFTIME, newMode);
                                break;
                            case "TimeRemaining":
                                newMode = new StringType(attributeValue);
                                updateState(CHANNEL_HEATINGREMAINING, newMode);
                                break;
                        }
                    }
                }
            }
        } catch (RuntimeException | ParserConfigurationException | SAXException | IOException e) {
            logger.debug("Failed to get actual state for device '{}':", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        updateStatus(ThingStatus.ONLINE);
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
}
