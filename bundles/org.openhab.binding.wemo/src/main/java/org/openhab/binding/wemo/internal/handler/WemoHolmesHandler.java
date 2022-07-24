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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.config.core.Configuration;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link WemoHolmesHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution;
 */
@NonNullByDefault
public class WemoHolmesHandler extends WemoBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WemoHolmesHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_PURIFIER);

    private static final int FILTER_LIFE_DAYS = 330;
    private static final int FILTER_LIFE_MINS = FILTER_LIFE_DAYS * 24 * 60;

    private final Object jobLock = new Object();

    private final Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<>());

    private @Nullable ScheduledFuture<?> pollingJob;

    public WemoHolmesHandler(Thing thing, UpnpIOService upnpIOService, UpnpService upnpService,
            WemoHttpCall wemoHttpCaller) {
        super(thing, upnpIOService, upnpService, wemoHttpCaller);

        logger.debug("Creating a WemoHolmesHandler for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        super.initialize();
        Configuration configuration = getConfig();

        if (configuration.get(UDN) != null) {
            logger.debug("Initializing WemoHolmesHandler for UDN '{}'", configuration.get(UDN));
            addSubscription(BASICEVENT);
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
        logger.debug("WemoHolmesHandler disposed.");

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
        String wemoURL = getWemoURL(DEVICEACTION);
        if (wemoURL == null) {
            logger.debug("Failed to send command '{}' for device '{}': URL cannot be created", command,
                    getThing().getUID());
            return;
        }
        String attribute = null;
        String value = null;

        if (command instanceof RefreshType) {
            updateWemoState();
        } else if (CHANNEL_PURIFIER_MODE.equals(channelUID.getId())) {
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
        } else if (CHANNEL_HUMIDIFIER_MODE.equals(channelUID.getId())) {
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
        } else if (CHANNEL_DESIRED_HUMIDITY.equals(channelUID.getId())) {
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
        } else if (CHANNEL_HEATER_MODE.equals(channelUID.getId())) {
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
        } else if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())) {
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
            wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.debug("Failed to send command '{}' for device '{}':", command, getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.debug("Received pair '{}':'{}' (service '{}') for thing '{}'", variable, value, service,
                this.getThing().getUID());

        updateStatus(ThingStatus.ONLINE);
        if (variable != null && value != null) {
            this.stateMap.put(variable, value);
        }
    }

    /**
     * The {@link updateWemoState} polls the actual state of a WeMo device and
     * calls {@link onValueReceived} to update the statemap and channels..
     *
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
            String stringParser = substringBetween(wemoCallResponse, "<attributeList>", "</attributeList>");

            // Due to Belkins bad response formatting, we need to run this twice.
            stringParser = unescapeXml(stringParser);
            stringParser = unescapeXml(stringParser);

            logger.trace("AirPurifier response '{}' for device '{}' received", stringParser, getThing().getUID());

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
                            updateState(CHANNEL_PURIFIER_MODE, newMode);
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
                            updateState(CHANNEL_HEATER_MODE, newMode);
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
                        updateState(CHANNEL_AIR_QUALITY, newMode);
                        break;
                    case "FilterLife":
                        int filterLife = Integer.valueOf(attributeValue);
                        if ("purifier".equals(getThing().getThingTypeUID().getId())) {
                            filterLife = Math.round((filterLife / FILTER_LIFE_MINS) * 100);
                        } else {
                            filterLife = Math.round((filterLife / 60480) * 100);
                        }
                        updateState(CHANNEL_FILTER_LIFE, new PercentType(String.valueOf(filterLife)));
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
                        updateState(CHANNEL_EXPIRED_FILTER_TIME, newMode);
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
                        updateState(CHANNEL_FILTER_PRESENT, newMode);
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
                        updateState(CHANNEL_PURIFIER_MODE, newMode);
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
                        updateState(CHANNEL_DESIRED_HUMIDITY, newMode);
                        break;
                    case "CurrentHumidity":
                        newMode = new StringType(attributeValue);
                        updateState(CHANNEL_CURRENT_HUMIDITY, newMode);
                        break;
                    case "Temperature":
                        newMode = new StringType(attributeValue);
                        updateState(CHANNEL_CURRENT_TEMPERATURE, newMode);
                        break;
                    case "SetTemperature":
                        newMode = new StringType(attributeValue);
                        updateState(CHANNEL_TARGET_TEMPERATURE, newMode);
                        break;
                    case "AutoOffTime":
                        newMode = new StringType(attributeValue);
                        updateState(CHANNEL_AUTO_OFF_TIME, newMode);
                        break;
                    case "TimeRemaining":
                        newMode = new StringType(attributeValue);
                        updateState(CHANNEL_HEATING_REMAINING, newMode);
                        break;
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (RuntimeException | ParserConfigurationException | SAXException | IOException e) {
            logger.debug("Failed to get actual state for device '{}':", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
