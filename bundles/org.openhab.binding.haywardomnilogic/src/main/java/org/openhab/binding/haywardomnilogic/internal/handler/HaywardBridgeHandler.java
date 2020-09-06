/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.haywardomnilogic.internal.handler;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.config.HaywardConfig;
import org.openhab.binding.haywardomnilogic.internal.hayward.HaywardTypeToRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The {@link HaywardBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matt Myers - Initial contribution
 */

public class HaywardBridgeHandler extends BaseBridgeHandler implements HaywardListener {
    private final Logger logger = LoggerFactory.getLogger(HaywardBridgeHandler.class);
    private List<HaywardHandlerListener> listeners = new ArrayList<HaywardHandlerListener>();
    private final HttpClient httpClient;
    private ScheduledFuture<?> initializeFuture;
    private ScheduledFuture<?> pollTelemetryFuture;
    private ScheduledFuture<?> pollAlarmsFuture;
    private int commFailureCount;
    public String chlorTimedPercent;
    public String chlorState;

    HaywardConfig config = getConfig().as(HaywardConfig.class);

    public HaywardBridgeHandler(Bridge thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void addListener(HaywardHandlerListener listener) {
        synchronized (listeners) {
            this.listeners.add(listener);
        }
    }

    public void removeListener(HaywardHandlerListener listener) {
        synchronized (listeners) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public void onDeviceDiscovered(HaywardTypeToRequest type, Integer systemID, String label, String bowID,
            String bowName, String property1, String property2, String property3, String property4) {
        // Once we have a description, see if the thing exists.
        Thing thing = getThingForType(type, systemID);
        if (thing == null) {
            for (HaywardHandlerListener listener : this.listeners) {
                if (type == HaywardTypeToRequest.BACKYARD) {
                    listener.onBackyardDiscovered(systemID, label);
                } else if (type == HaywardTypeToRequest.BOW) {
                    listener.onBOWDiscovered(systemID, label);
                } else if (type == HaywardTypeToRequest.FILTER) {
                    listener.onFilterDiscovered(systemID, label, bowID, bowName, property1, property2, property3,
                            property4);
                } else if (type == HaywardTypeToRequest.HEATER) {
                    listener.onHeaterDiscovered(systemID, label, bowID, bowName);
                } else if (type == HaywardTypeToRequest.CHLORINATOR) {
                    listener.onChlorinatorDiscovered(systemID, label, bowID, bowName);
                } else if (type == HaywardTypeToRequest.COLORLOGIC) {
                    listener.onColorLogicDiscovered(systemID, label, bowID, bowName);
                } else if (type == HaywardTypeToRequest.PUMP) {
                    listener.onPumpDiscovered(systemID, label, bowID, bowName, property1, property2, property3,
                            property4);
                } else if (type == HaywardTypeToRequest.RELAY) {
                    listener.onRelayDiscovered(systemID, label, bowID, bowName);
                } else if (type == HaywardTypeToRequest.SENSOR) {
                    listener.onSensorDiscovered(systemID, label, bowID, bowName);
                } else if (type == HaywardTypeToRequest.VIRTUALHEATER) {
                    listener.onVirtualHeaterDiscovered(systemID, label, bowID, bowName);
                }
            }
        }
    }

    @Override
    public void dispose() {
        clearPolling(initializeFuture);
        clearPolling(pollTelemetryFuture);
        clearPolling(pollAlarmsFuture);
        logger.trace("Hayward polling cancelled");
        super.dispose();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "Opening connection to Hayward's Server");

        initializeFuture = scheduler.schedule(() -> {
            scheduledInitialize();
        }, 1, TimeUnit.SECONDS);
        return;
    }

    public void scheduledInitialize() {
        config = getConfigAs(HaywardConfig.class);

        clearPolling(pollTelemetryFuture);
        clearPolling(pollAlarmsFuture);

        try {
            if (!(login())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to Login to Hayward's server");
                clearPolling(pollTelemetryFuture);
                clearPolling(pollAlarmsFuture);
                commFailureCount = 50;
                initPolling(30);
                return;
            }

            if (!(getSiteList())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to getMSP from Hayward's server");
                clearPolling(pollTelemetryFuture);
                clearPolling(pollAlarmsFuture);
                commFailureCount = 50;
                initPolling(30);
                return;
            }

            if (!(getMspConfig())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to getConfig from Hayward's server");
                clearPolling(pollTelemetryFuture);
                clearPolling(pollAlarmsFuture);
                commFailureCount = 50;
                initPolling(30);
                return;
            }

            updateStatus(ThingStatus.ONLINE);
            logger.trace("Succesfully opened connection to Hayward's server: {} Username:{}", config.hostname,
                    config.username);

            if (config.telemetryPollTime > 0) {
                initPolling(0);
                logger.trace("Hayward Telemetry polling scheduled");
            } else {
                logger.trace("Hayward Telemetry polling disabled");
            }

            if (config.alarmPollTime > 0) {
                initAlarmPolling(1);
                logger.trace("Hayward Alarm polling scheduled");
            } else {
                logger.trace("Hayward Alarm polling disabled");
            }
        } catch (Exception e) {
            logger.debug("Unable to open connection to Hayward's server: {} Username:{}", config.hostname,
                    config.username, e);
        }
    }

    public synchronized boolean login() {
        String xmlResponse;
        String status;
        try {
            // *****Login to Hayward server
            String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request>"
                    + "<Name>Login</Name><Parameters>" + "<Parameter name=\"UserName\" dataType=\"String\">"
                    + config.username + "</Parameter>" + "<Parameter name=\"Password\" dataType=\"String\">"
                    + config.password + "</Parameter>" + "</Parameters></Request>";

            xmlResponse = httpXmlResponse(urlParameters);

            if (xmlResponse == null) {
                logger.error("Hayward Login XML response was null");
                return false;
            }

            status = evaluateXPath("/Response/Parameters//Parameter[@name='Status']/text()", xmlResponse).get(0);

            if (!(status.equals("0"))) {
                logger.error("Hayward Login XML response: {}", xmlResponse);
                return false;
            }

            config.token = evaluateXPath("/Response/Parameters//Parameter[@name='Token']/text()", xmlResponse).get(0);
            config.userID = evaluateXPath("/Response/Parameters//Parameter[@name='UserID']/text()", xmlResponse).get(0);

        } catch (Exception e) {
            logger.debug("Unable to login to Hayward's server {}:{}", config.hostname, config.username, e);
            return false;
        }
        return true;
    }

    public synchronized boolean getApiDef() {
        String xmlResponse;
        try {
            // *****getConfig from Hayward server
            String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetAPIDef</Name><Parameters>"
                    + "<Parameter name=\"Token\" dataType=\"String\">" + config.token + "</Parameter>"
                    + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + config.mspSystemID + "</Parameter>;"
                    + "<Parameter name=\"Version\" dataType=\"string\">0.4</Parameter >\r\n"
                    + "<Parameter name=\"Language\" dataType=\"string\">en</Parameter >\r\n"
                    + "</Parameters></Request>";

            xmlResponse = httpXmlResponse(urlParameters);

            if (xmlResponse == null) {
                logger.error("Hayward Login XML response was null");
                return false;
            }
        } catch (Exception e) {
            logger.debug("Unable to getApiDef from Hayward's server {}:{}", config.hostname, config.username, e);
            return false;
        }
        return true;
    }

    public synchronized boolean getSiteList() {
        String xmlResponse;
        String status;
        try {
            // *****Get MSP
            String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetSiteList</Name><Parameters>"
                    + "<Parameter name=\"Token\" dataType=\"String\">" + config.token
                    + "</Parameter><Parameter name=\"UserID\" dataType=\"String\">" + config.userID
                    + "</Parameter></Parameters></Request>";

            xmlResponse = httpXmlResponse(urlParameters);

            if (xmlResponse == null) {
                logger.error("Hayward getSiteList XML response was null");
                return false;
            }

            status = evaluateXPath("/Response/Parameters//Parameter[@name='Status']/text()", xmlResponse).get(0);

            if (!(status.equals("0"))) {
                logger.error("Hayward getSiteList XML response: {}", xmlResponse);
                return false;
            }

            config.mspSystemID = evaluateXPath(
                    "/Response/Parameters/Parameter/Item//Property[@name='MspSystemID']/text()", xmlResponse).get(0);
            config.backyardName = evaluateXPath(
                    "/Response/Parameters/Parameter/Item//Property[@name='BackyardName']/text()", xmlResponse).get(0);
            config.address = evaluateXPath("/Response/Parameters/Parameter/Item//Property[@name='Address']/text()",
                    xmlResponse).get(0);
        } catch (Exception e) {
            logger.debug("Unable to getMSP from Hayward's server {}:{}", config.hostname, config.username, e);
            return false;
        }
        return true;
    }

    public synchronized boolean getMspConfig() {
        String xmlResponse;
        List<String> systemIDs = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> bowName = new ArrayList<>();
        List<String> bowID = new ArrayList<>();
        List<String> property1 = new ArrayList<>();
        List<String> property2 = new ArrayList<>();
        List<String> property3 = new ArrayList<>();
        List<String> property4 = new ArrayList<>();
        try {
            // *****getMspConfig from Hayward server
            String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetMspConfigFile</Name><Parameters>"
                    + "<Parameter name=\"Token\" dataType=\"String\">" + config.token + "</Parameter>"
                    + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + config.mspSystemID
                    + "</Parameter><Parameter name=\"Version\" dataType=\"string\">0</Parameter>\r\n"
                    + "</Parameters></Request>";

            xmlResponse = httpXmlResponse(urlParameters);

            // Debug: Inject xml file for testing
            // String path =
            // "C:/Users/Controls/openhab-2-5-x/git/openhab-addons/bundles/org.openhab.binding.haywardomnilogic/getConfig.xml";
            // xmlResponse = new String(Files.readAllBytes(Paths.get(path)));

            if (xmlResponse == null) {
                logger.error("Hayward requestConfig XML response was null");
                return false;
            }

            if (evaluateXPath("//Backyard/Name/text()", xmlResponse).isEmpty()) {
                logger.error("Hayward requestConfiguration XML response: {}", xmlResponse);
                return false;
            }

            // Find Backyard
            names = evaluateXPath("//Backyard/Name/text()", xmlResponse);

            for (String name : names) {
                onDeviceDiscovered(HaywardTypeToRequest.BACKYARD, Integer.parseInt(config.mspSystemID), name, "", "",
                        "", "", "", "");
            }

            // Find Bodies of Water
            systemIDs = evaluateXPath("//Body-of-water/System-Id/text()", xmlResponse);
            names = evaluateXPath("//Body-of-water/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                onDeviceDiscovered(HaywardTypeToRequest.BOW, Integer.parseInt(systemIDs.get(i)), names.get(i), "", "",
                        "", "", "", "");
            }

            // Find Chlorinators
            systemIDs = evaluateXPath("//Chlorinator/System-Id/text()", xmlResponse);
            names = evaluateXPath("//Chlorinator/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                        xmlResponse);
                onDeviceDiscovered(HaywardTypeToRequest.CHLORINATOR, Integer.parseInt(systemIDs.get(i)), names.get(i),
                        bowID.get(0), bowName.get(0), "", "", "", "");
            }

            // Find ColorLogic Lights
            systemIDs = evaluateXPath("//ColorLogic-Light/System-Id/text()", xmlResponse);
            names = evaluateXPath("//ColorLogic-Light/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                        xmlResponse);
                onDeviceDiscovered(HaywardTypeToRequest.COLORLOGIC, Integer.parseInt(systemIDs.get(i)), names.get(i),
                        bowID.get(0), bowName.get(0), "", "", "", "");
            }

            // Find CSAD's
            systemIDs = evaluateXPath("//CSAD/System-Id/text()", xmlResponse);
            names = evaluateXPath("//CSAD/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/Name/text()",
                        xmlResponse);
                onDeviceDiscovered(HaywardTypeToRequest.CSAD, Integer.parseInt(systemIDs.get(i)), names.get(i),
                        bowID.get(0), bowName.get(0), "", "", "", "");
            }

            // Find Filters
            systemIDs = evaluateXPath("//Filter/System-Id/text()", xmlResponse);
            names = evaluateXPath("//Filter/Name/text()", xmlResponse);
            property1 = evaluateXPath("//Filter/Min-Pump-Speed/text()", xmlResponse);
            property2 = evaluateXPath("//Filter/Max-Pump-Speed/text()", xmlResponse);
            property3 = evaluateXPath("//Filter/Min-Pump-RPM/text()", xmlResponse);
            property4 = evaluateXPath("//Filter/Max-Pump-RPM/text()", xmlResponse);
            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                        xmlResponse);
                onDeviceDiscovered(HaywardTypeToRequest.FILTER, Integer.parseInt(systemIDs.get(i)), names.get(i),
                        bowID.get(0), bowName.get(0), property1.get(i), property2.get(i), property3.get(i),
                        property4.get(i));
            }

            // Find Heaters
            systemIDs = evaluateXPath("//Heater-Equipment/System-Id/text()", xmlResponse);
            names = evaluateXPath("//Heater-Equipment/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/Name/text()",
                        xmlResponse);
                onDeviceDiscovered(HaywardTypeToRequest.HEATER, Integer.parseInt(systemIDs.get(i)), names.get(i),
                        bowID.get(0), bowName.get(0), "", "", "", "");
            }

            // Find Pumps
            systemIDs = evaluateXPath("//Pump/System-Id/text()", xmlResponse);
            names = evaluateXPath("//Pump/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = evaluateXPath(
                        "//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/Name/text()",
                        xmlResponse);
                onDeviceDiscovered(HaywardTypeToRequest.PUMP, Integer.parseInt(systemIDs.get(i)), names.get(i),
                        bowID.get(0), bowName.get(0), "", "", "", "");
            }

            // Find Relays
            systemIDs = evaluateXPath("//Relay/System-Id/text()", xmlResponse);
            names = evaluateXPath("//Relay/Name/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                        xmlResponse);
                if (!(bowID.isEmpty())) {
                    onDeviceDiscovered(HaywardTypeToRequest.RELAY, Integer.parseInt(systemIDs.get(i)), names.get(i),
                            bowID.get(0), bowName.get(0), "", "", "", "");
                } else {
                    onDeviceDiscovered(HaywardTypeToRequest.RELAY, Integer.parseInt(systemIDs.get(i)), names.get(i), "",
                            "", "", "", "", "");
                }

            }

            // Find Virtual Heaters
            systemIDs = evaluateXPath("//Heater/System-Id/text()", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                        xmlResponse);
                onDeviceDiscovered(HaywardTypeToRequest.VIRTUALHEATER, Integer.parseInt(systemIDs.get(i)),
                        "Virtual Heater", bowID.get(0), bowName.get(0), "", "", "", "");
            }

            // Find Sensors
            // Flow and water temp sensor aren't showing up in telemetry. Need example to determine how to differentiate
            // "system" sensors
            // that are reported in the BOW water temp, Filter flow switch, ORP, etc.
            systemIDs = evaluateXPath("//Sensor/System-Id/text()", xmlResponse);
            names = evaluateXPath("//Sensor/Name/text()", xmlResponse);
            for (int i = 0; i < systemIDs.size(); i++) {
                // get Body of Water for each item
                bowID = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/System-Id/text()",
                        xmlResponse);
                bowName = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/parent::Body-of-water/Name/text()",
                        xmlResponse);
                // Do not add backyard sensors that do not exist in the BOW thus bowID is null
                if (!(bowID.isEmpty())) {
                    onDeviceDiscovered(HaywardTypeToRequest.SENSOR, Integer.parseInt(systemIDs.get(i)), names.get(i),
                            bowID.get(0), bowName.get(0), "", "", "", "");
                }
            }
        } catch (Exception e) {
            logger.debug("Unable to getMspConfig from Hayward's server: {} with username: {}", config.hostname,
                    config.username, e);
            return false;
        }

        return true;
    }

    public synchronized boolean getTelemetryData() {
        String xmlResponse;
        List<String> data = new ArrayList<>();
        List<String> systemIDs = new ArrayList<>();

        try {
            // *****Request Telemetry from Hayward server
            String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetTelemetryData</Name><Parameters>"
                    + "<Parameter name=\"Token\" dataType=\"String\">" + config.token + "</Parameter>"
                    + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + config.mspSystemID
                    + "</Parameter></Parameters></Request>";

            xmlResponse = httpXmlResponse(urlParameters);

            if (xmlResponse == null) {
                logger.error("Hayward getTelemetry XML response was null");
                return false;
            }

            if (!evaluateXPath("/Response/Parameters//Parameter[@name='StatusMessage']/text()", xmlResponse)
                    .isEmpty()) {
                logger.error("Hayward getTelemetry XML response: {}", xmlResponse);
                return false;
            }

            // ******************
            // ***BACKYARD***
            // ******************
            systemIDs = evaluateXPath("//Backyard/@systemId", xmlResponse);

            // Air temp
            data = evaluateXPath("//Backyard/@airTemp", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.BACKYARD, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_BACKYARD_AIRTEMP, data.get(0));

            // Status
            data = evaluateXPath("//Backyard/@status", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.BACKYARD, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_BACKYARD_STATUS, data.get(0));

            // State
            data = evaluateXPath("//Backyard/@state", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.BACKYARD, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_BACKYARD_STATE, data.get(0));

            // ******************
            // ***BodyOfWater***
            // ******************
            systemIDs = evaluateXPath("//BodyOfWater/@systemId", xmlResponse);

            // Flow
            data = evaluateXPath("//BodyOfWater/@flow", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.BOW, systemIDs.get(0), HaywardBindingConstants.CHANNEL_BOW_FLOW,
                    data.get(0));

            // Water Temp
            data = evaluateXPath("//BodyOfWater/@waterTemp", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.BOW, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_BOW_WATERTEMP, data.get(0));

            // ******************
            // ***Chlorinator***
            // ******************
            systemIDs = evaluateXPath("//Chlorinator/@systemId", xmlResponse);

            // Operating Mode
            data = evaluateXPath("//Chlorinator/@operatingMode", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.CHLORINATOR, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_CHLORINATOR_OPERATINGMODE, data.get(0));
            this.chlorState = data.get(0);

            // Timed Percent
            data = evaluateXPath("//Chlorinator/@Timed-Percent", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.CHLORINATOR, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_CHLORINATOR_TIMEDPERCENT, data.get(0));
            this.chlorTimedPercent = data.get(0);

            // scMode
            data = evaluateXPath("//Chlorinator/@scMode", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.CHLORINATOR, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_CHLORINATOR_SCMODE, data.get(0));

            // Error
            data = evaluateXPath("//Chlorinator/@chlrError", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.CHLORINATOR, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_CHLORINATOR_ERROR, data.get(0));

            // Alert
            data = evaluateXPath("//Chlorinator/@chlrAlert", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.CHLORINATOR, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_CHLORINATOR_ALERT, data.get(0));

            // Average Salt Level
            data = evaluateXPath("//Chlorinator/@avgSaltLevel", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.CHLORINATOR, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_CHLORINATOR_AVGSALTLEVEL, data.get(0));

            // Instant Salt Level
            data = evaluateXPath("//Chlorinator/@instantSaltLevel", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.CHLORINATOR, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_CHLORINATOR_INSTANTSALTLEVEL, data.get(0));

            // Status
            data = evaluateXPath("//Chlorinator/@status", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.CHLORINATOR, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_CHLORINATOR_STATUS, data.get(0));

            if (data.get(0).equals("0")) {
                handleHaywardTelemetry(HaywardTypeToRequest.CHLORINATOR, systemIDs.get(0),
                        HaywardBindingConstants.CHANNEL_CHLORINATOR_ENABLE, "0");
                this.chlorState = "2";
            } else {
                handleHaywardTelemetry(HaywardTypeToRequest.CHLORINATOR, systemIDs.get(0),
                        HaywardBindingConstants.CHANNEL_CHLORINATOR_ENABLE, "1");
                this.chlorState = "3";
            }

            // ***********************
            // ***Color Logic Light***
            // ***********************
            systemIDs = evaluateXPath("//ColorLogic-Light/@systemId", xmlResponse);

            // Light State
            data = evaluateXPath("//ColorLogic-Light/@lightState", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.COLORLOGIC, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_COLORLOGIC_LIGHTSTATE, data.get(0));

            if (data.get(0).equals("0")) {
                handleHaywardTelemetry(HaywardTypeToRequest.COLORLOGIC, systemIDs.get(0),
                        HaywardBindingConstants.CHANNEL_COLORLOGIC_ENABLE, "0");
            } else {
                handleHaywardTelemetry(HaywardTypeToRequest.COLORLOGIC, systemIDs.get(0),
                        HaywardBindingConstants.CHANNEL_COLORLOGIC_ENABLE, "1");
            }

            // Current Show
            data = evaluateXPath("//ColorLogic-Light/@currentShow", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.COLORLOGIC, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_COLORLOGIC_CURRENTSHOW, data.get(0));

            // ******************
            // ***Filter***
            // ******************
            systemIDs = evaluateXPath("//Filter/@systemId", xmlResponse);

            // Valve Position
            data = evaluateXPath("//Filter/@valvePosition", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.FILTER, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_FILTER_VALVEPOSITION, data.get(0));

            // Speed
            data = evaluateXPath("//Filter/@filterSpeed", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.FILTER, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_FILTER_SPEED, data.get(0));

            if (data.get(0).equals("0")) {
                handleHaywardTelemetry(HaywardTypeToRequest.FILTER, systemIDs.get(0),
                        HaywardBindingConstants.CHANNEL_FILTER_ENABLE, "0");
            } else {
                handleHaywardTelemetry(HaywardTypeToRequest.FILTER, systemIDs.get(0),
                        HaywardBindingConstants.CHANNEL_FILTER_ENABLE, "1");
            }

            // State
            data = evaluateXPath("//Filter/@filterState", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.FILTER, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_FILTER_STATE, data.get(0));

            // lastSpeed
            data = evaluateXPath("//Filter/@lastSpeed", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.FILTER, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_FILTER_LASTSPEED, data.get(0));

            // ******************
            // ***Heater***
            // ******************
            systemIDs = evaluateXPath("//Heater/@systemId", xmlResponse);

            // State
            data = evaluateXPath("//Heater/@heaterState", xmlResponse);
            handleHaywardTelemetry(HaywardTypeToRequest.HEATER, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_HEATER_STATE, data.get(0));

            // Enable
            data = evaluateXPath("//Heater/@enable", xmlResponse);
            handleHaywardTelemetry(HaywardTypeToRequest.HEATER, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_HEATER_ENABLE, data.get(0));

            // ******************
            // ***Relays***
            // ******************
            systemIDs = evaluateXPath("//Relay/@systemId", xmlResponse);

            // State
            data = evaluateXPath("//Relay/@relayState", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                handleHaywardTelemetry(HaywardTypeToRequest.RELAY, systemIDs.get(i),
                        HaywardBindingConstants.CHANNEL_RELAY_STATE, data.get(i));

            }

            // ******************
            // ***Sensors***
            // ******************
            systemIDs = evaluateXPath("//Sensor/@systemId", xmlResponse);

            // State
            data = evaluateXPath("//Sensor/@relayState", xmlResponse);

            for (int i = 0; i < systemIDs.size(); i++) {
                handleHaywardTelemetry(HaywardTypeToRequest.RELAY, systemIDs.get(i),
                        HaywardBindingConstants.CHANNEL_RELAY_STATE, data.get(i));

            }

            // ******************
            // ***Virtual Heater***
            // ******************

            systemIDs = evaluateXPath("//VirtualHeater/@systemId", xmlResponse);

            // Current Setpoint
            data = evaluateXPath("//VirtualHeater/@Current-Set-Point", xmlResponse);

            handleHaywardTelemetry(HaywardTypeToRequest.VIRTUALHEATER, systemIDs.get(0),
                    HaywardBindingConstants.CHANNEL_VIRTUALHEATER_CURRENTSETPOINT, data.get(0));

            // Enable
            data = evaluateXPath("//VirtualHeater/@enable", xmlResponse);

            if (data.get(0).equals("yes")) {
                handleHaywardTelemetry(HaywardTypeToRequest.VIRTUALHEATER, systemIDs.get(0),
                        HaywardBindingConstants.CHANNEL_VIRTUALHEATER_ENABLE, "1");
            } else if (data.get(0).equals("no")) {
                handleHaywardTelemetry(HaywardTypeToRequest.VIRTUALHEATER, systemIDs.get(0),
                        HaywardBindingConstants.CHANNEL_VIRTUALHEATER_ENABLE, "0");
            }
        } catch (Exception e) {
            logger.error("Unable to getTelemetry from Hayward's server: {} with username: {}", config.hostname,
                    config.username, e);
            return false;
        }

        return true;
    }

    public synchronized boolean getAlarmList() {
        List<String> backyardID = new ArrayList<>();
        List<String> bowID = new ArrayList<>();
        List<String> parameter1 = new ArrayList<>();
        List<String> message = new ArrayList<>();
        String status;
        String xmlResponse;
        String alarmStr;

        try {
            // Get list backyard ID
            for (Thing thing : getThing().getThings()) {
                Map<String, String> properties = thing.getProperties();
                if (properties.get(HaywardBindingConstants.PROPERTY_TYPE).equals("Backyard")) {
                    backyardID.add(properties.get(HaywardBindingConstants.PROPERTY_SYSTEM_ID));
                }
            }
            if (backyardID.size() == 0) {
                // No backyard thing exists
                return true;
            }

            // *****Request Alarm List from Hayward server
            String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetAlarmList</Name><Parameters>"
                    + "<Parameter name=\"Token\" dataType=\"String\">" + config.token + "</Parameter>"
                    + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + config.mspSystemID + "</Parameter>"
                    + "<Parameter name=\"CultureInfoName\" dataType=\"String\">en-us</Parameter></Parameters></Request>";

            xmlResponse = httpXmlResponse(urlParameters);

            if (xmlResponse == null) {
                logger.error("Hayward getAlarmList XML response was null");
                return false;
            }

            status = evaluateXPath("/Response/Parameters//Parameter[@name='Status']/text()", xmlResponse).get(0);

            if (!(status.equals("0"))) {
                logger.error("Hayward getAlarm XML response: {}", xmlResponse);
                return false;
            }

            if (status.equals("0")) {
                bowID = evaluateXPath("//Property[@name='BowID']/text()", xmlResponse);
                parameter1 = evaluateXPath("//Property[@name='Parameter1']/text()", xmlResponse);
                message = evaluateXPath("//Property[@name='Message']/text()", xmlResponse);

                for (int i = 0; i < 5; i++) {
                    if (i < bowID.size()) {
                        alarmStr = parameter1.get(i) + ": " + message.get(i);
                    } else {
                        alarmStr = "";
                    }

                    handleHaywardTelemetry(HaywardTypeToRequest.BACKYARD, backyardID.get(0),
                            "backyardAlarm" + String.format("%01d", i + 1), alarmStr);
                }
            } else {
                logger.error("Hayward getAlarms XML response: {}", xmlResponse);
                return false;
            }
        } catch (Exception e) {
            logger.debug("Unable to getAlarms from Hayward's server: {} with username: {}", config.hostname,
                    config.username, e);
            return false;
        }

        return true;
    }

    @Override
    public void handleHaywardTelemetry(HaywardTypeToRequest type, String systemID, String channelID, String data) {
        // Once we have a description, see if the thing exists.
        Thing thing = getThingForType(type, Integer.parseInt(systemID));

        if (thing != null) {
            if (type == HaywardTypeToRequest.BACKYARD) {
                HaywardBackyardHandler handler = (HaywardBackyardHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateData(systemID, channelID, data);
                }
            } else if (type == HaywardTypeToRequest.BOW) {
                HaywardBowHandler handler = (HaywardBowHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateData(systemID, channelID, data);
                }
            } else if (type == HaywardTypeToRequest.FILTER) {
                HaywardFilterHandler handler = (HaywardFilterHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateData(systemID, channelID, data);
                }
            } else if (type == HaywardTypeToRequest.HEATER) {
                HaywardHeaterHandler handler = (HaywardHeaterHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateData(systemID, channelID, data);
                }
            } else if (type == HaywardTypeToRequest.COLORLOGIC) {
                HaywardColorLogicHandler handler = (HaywardColorLogicHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateData(systemID, channelID, data);
                }
            } else if (type == HaywardTypeToRequest.CHLORINATOR) {
                HaywardChlorinatorHandler handler = (HaywardChlorinatorHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateData(systemID, channelID, data);
                }
            } else if (type == HaywardTypeToRequest.RELAY) {
                HaywardRelayHandler handler = (HaywardRelayHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateData(systemID, channelID, data);
                }
            } else if (type == HaywardTypeToRequest.VIRTUALHEATER) {
                HaywardVirtualHeaterHandler handler = (HaywardVirtualHeaterHandler) thing.getHandler();
                if (handler != null) {
                    handler.updateData(systemID, channelID, data);
                }
            }
        }
    }

    private synchronized void initPolling(int initalDelay) {
        pollTelemetryFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (commFailureCount >= 5) {
                    commFailureCount = 0;
                    clearPolling(pollTelemetryFuture);
                    clearPolling(pollAlarmsFuture);
                    initialize();
                    return;
                }
                if (!(getTelemetryData())) {
                    commFailureCount++;
                    return;
                }
            } catch (Exception e) {
                logger.debug("Exception during poll", e);
            }
        }, initalDelay, config.telemetryPollTime, TimeUnit.SECONDS);
        return;
    }

    private synchronized void initAlarmPolling(int initalDelay) {
        pollAlarmsFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                getAlarmList();
            } catch (Exception e) {
                logger.debug("Exception during poll", e);
            }
        }, initalDelay, config.alarmPollTime, TimeUnit.SECONDS);
    }

    private void clearPolling(ScheduledFuture<?> pollJob) {
        if (pollJob != null) {
            pollJob.cancel(false);
        }
    }

    public void haywardCommand(ChannelUID channelUID, Command command, String systemID, String poolID) {
        String urlParameters = null;
        String urlTokenMspPoolID = null;
        String urlSchedule = null;
        String xmlResponse = null;
        String status = null;

        int cmdValue = 0;
        String cmdBool;
        String cmdString;
        if (command == OnOffType.OFF) {
            cmdValue = 0;
            cmdBool = "false";
            cmdString = "2";
        } else if (command == OnOffType.ON) {
            cmdValue = 1;
            cmdBool = "True";
            cmdString = "3";
        } else if (command instanceof DecimalType) {
            cmdValue = ((DecimalType) command).intValue();
            cmdBool = "null";
            cmdString = "null";
        } else if (command instanceof StringType) {
            cmdValue = 0;
            cmdString = ((StringType) command).toString();
            cmdBool = "null";
        } else {
            logger.error("command type {} is not supported", command);
            return;
        }

        try {
            if (!(command instanceof RefreshType)) {
                // Stop Polling
                clearPolling(pollTelemetryFuture);

                urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request>";

                // @formatter:off
                urlTokenMspPoolID = "<Parameter name=\"Token\" dataType=\"String\">" + config.token + "</Parameter>"
                        + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + config.mspSystemID + "</Parameter>"
                        + "<Parameter name=\"PoolID\" dataType=\"int\">" + poolID + "</Parameter>";

                urlSchedule = "<Parameter name=\"IsCountDownTimer\" dataType=\"bool\">false</Parameter>"
                        + "<Parameter name=\"StartTimeHours\" dataType=\"int\">0</Parameter>"
                        + "<Parameter name=\"StartTimeMinutes\" dataType=\"int\">0</Parameter>"
                        + "<Parameter name=\"EndTimeHours\" dataType=\"int\">0</Parameter>"
                        + "<Parameter name=\"EndTimeMinutes\" dataType=\"int\">0</Parameter>"
                        + "<Parameter name=\"DaysActive\" dataType=\"int\">0</Parameter>"
                        + "<Parameter name=\"Recurring\" dataType=\"bool\">false</Parameter>";
                // @formatter:on

                switch (channelUID.getId()) {
                    case HaywardBindingConstants.CHANNEL_FILTER_ENABLE:
                    case HaywardBindingConstants.CHANNEL_RELAY_STATE:
                    case HaywardBindingConstants.CHANNEL_FILTER_SPEED:
                    case HaywardBindingConstants.CHANNEL_COLORLOGIC_ENABLE:
                        if (channelUID.getId().equals(HaywardBindingConstants.CHANNEL_FILTER_ENABLE)) {
                            if (command == OnOffType.ON) {
                                cmdValue = 100;
                            } else {
                                cmdValue = 0;
                            }
                        }
                        // Append the command to the http command string
                        // @formatter:off
                        urlParameters = urlParameters + "<Name>SetUIEquipmentCmd</Name><Parameters>" + urlTokenMspPoolID
                                + "<Parameter name=\"EquipmentID\" dataType=\"int\">" + systemID + "</Parameter>"
                                + "<Parameter name=\"IsOn\" dataType=\"int\">" + cmdValue + "</Parameter>" + urlSchedule
                                + "</Parameters></Request>";
                        break;
                    case HaywardBindingConstants.CHANNEL_VIRTUALHEATER_CURRENTSETPOINT:
                        // Append the command to the http command string
                        urlParameters = urlParameters + "<Name>SetUIHeaterCmd</Name><Parameters>" + urlTokenMspPoolID
                                + "<Parameter name=\"HeaterID\" dataType=\"int\">" + systemID + "</Parameter>"
                                + "<Parameter name=\"Temp\" dataType=\"int\">" + cmdValue + "</Parameter>"
                                + "</Parameters></Request>";
                        break;
                    case HaywardBindingConstants.CHANNEL_VIRTUALHEATER_ENABLE:
                        // Append the command to the http command string
                        urlParameters = urlParameters + "<Name>SetHeaterEnable</Name><Parameters>" + urlTokenMspPoolID
                                + "<Parameter name=\"HeaterID\" dataType=\"int\">" + systemID + "</Parameter>"
                                + "<Parameter name=\"Enabled\" dataType=\"bool\">" + cmdBool + "</Parameter>"
                                + "</Parameters></Request>";
                        break;
                    case HaywardBindingConstants.CHANNEL_CHLORINATOR_ENABLE:
                        // Append the command to the http command string
                        urlParameters = urlParameters
                                + "<Name>SetCHLORParams</Name><Parameters>"
                                + urlTokenMspPoolID
                                + "<Parameter name=\"ChlorID\" dataType=\"int\" alias=\"EquipmentID\">" + systemID + "</Parameter>"
                                + "<Parameter name=\"CfgState\" dataType=\"byte\" alias=\"Data1\">" + cmdString + "</Parameter>"
                                + "<Parameter name=\"OpMode\" dataType=\"byte\" alias=\"Data2\">1</Parameter>"
                                + "<Parameter name=\"BOWType\" dataType=\"byte\" alias=\"Data3\">1</Parameter>"
                                + "<Parameter name=\"CellType\" dataType=\"byte\" alias=\"Data4\">4</Parameter>"
                                + "<Parameter name=\"TimedPercent\" dataType=\"byte\" alias=\"Data5\">" + this.chlorTimedPercent + "</Parameter>"
                                + "<Parameter name=\"SCTimeout\" dataType=\"byte\" unit=\"hour\" alias=\"Data6\">24</Parameter>"
                                + "<Parameter name=\"ORPTimout\" dataType=\"byte\" unit=\"hour\" alias=\"Data7\">24</Parameter>"
                                + "</Parameters></Request>";
                                this.chlorTimedPercent=Integer.toString(cmdValue);
                        break;
                    case HaywardBindingConstants.CHANNEL_CHLORINATOR_TIMEDPERCENT:
                        // Append the command to the http command string
                        urlParameters = urlParameters
                                + "<Name>SetCHLORParams</Name><Parameters>"
                                + urlTokenMspPoolID
                                + "<Parameter name=\"ChlorID\" dataType=\"int\" alias=\"EquipmentID\">" + systemID + "</Parameter>"
                                + "<Parameter name=\"CfgState\" dataType=\"byte\" alias=\"Data1\">" + this.chlorState + "</Parameter>"
                                + "<Parameter name=\"OpMode\" dataType=\"byte\" alias=\"Data2\">1</Parameter>"
                                + "<Parameter name=\"BOWType\" dataType=\"byte\" alias=\"Data3\">1</Parameter>"
                                + "<Parameter name=\"CellType\" dataType=\"byte\" alias=\"Data4\">4</Parameter>"
                                + "<Parameter name=\"TimedPercent\" dataType=\"byte\" alias=\"Data5\">" + cmdValue + "</Parameter>"
                                + "<Parameter name=\"SCTimeout\" dataType=\"byte\" unit=\"hour\" alias=\"Data6\">24</Parameter>"
                                + "<Parameter name=\"ORPTimout\" dataType=\"byte\" unit=\"hour\" alias=\"Data7\">24</Parameter>"
                                + "</Parameters></Request>";
                                this.chlorTimedPercent=Integer.toString(cmdValue);
                        break;
                    case HaywardBindingConstants.CHANNEL_COLORLOGIC_CURRENTSHOW:
                        // Append the command to the http command string
                        urlParameters = urlParameters
                                + "<Name>SetStandAloneLightShow</Name><Parameters>"
                                + urlTokenMspPoolID
                                + "<Parameter name=\"LightID\" dataType=\"int\">" + systemID + "</Parameter>"
                                + "<Parameter name=\"Show\" dataType=\"int\">" + cmdString + "</Parameter>"
                                + "<Parameter name=\"Speed\" dataType=\"byte\">4</Parameter>"
                                + "<Parameter name=\"Brightness\" dataType=\"byte\">4</Parameter>"
                                + "<Parameter name=\"Reserved\" dataType=\"byte\">0</Parameter>"
                                + urlSchedule
                                + "</Parameters></Request>";
                        // @formatter:on
                        break;
                    default:
                        logger.error("haywardCommand Unsupported type {}", channelUID);
                        initPolling(config.telemetryPollTime);
                        return;
                }

                // *****Send Command to Hayward server
                xmlResponse = httpXmlResponse(urlParameters);
                status = evaluateXPath("//Parameter[@name='Status']/text()", xmlResponse).get(0);

                if (!(status.equals("0"))) {
                    logger.error("haywardCommand XML response: {}", xmlResponse);
                    return;
                }
                // Restart Polling
                initPolling(config.telemetryPollTime);
            }
        } catch (Exception e) {
            logger.debug("Unable to send command to Hayward's server {}:{}", config.hostname, config.username);
            // Restart Polling
            initPolling(config.telemetryPollTime);
        }
    }

    Thing getThingForType(HaywardTypeToRequest type, int num) {
        for (Thing thing : getThing().getThings()) {
            Map<String, String> properties = thing.getProperties();
            if (properties.get(HaywardBindingConstants.PROPERTY_SYSTEM_ID).equals(Integer.toString(num))) {
                if (properties.get(HaywardBindingConstants.PROPERTY_TYPE).equals(type.toString())) {
                    {
                        return thing;
                    }
                }
            }
        }
        return null;
    }

    private List<String> evaluateXPath(String xpathExp, String xmlResponse) throws Exception {
        List<String> values = new ArrayList<>();
        try {
            InputSource inputXML = new InputSource(new StringReader(xmlResponse));
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xPath.evaluate(xpathExp, inputXML, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                values.add(nodes.item(i).getNodeValue());
            }

        } catch (XPathExpressionException e) {
            logger.error("XPathExpression exception:", e);
        }
        return values;
    }

    private Request sendRequestBuilder(String url, HttpMethod method) {
        return this.httpClient.newRequest(url).agent("NextGenForIPhone/16565 CFNetwork/887 Darwin/17.0.0")
                .method(method).header(HttpHeader.ACCEPT_LANGUAGE, "en-us").header(HttpHeader.ACCEPT, "*/*")
                .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate").version(HttpVersion.HTTP_1_1)
                .header(HttpHeader.CONNECTION, "keep-alive").header(HttpHeader.HOST, "www.haywardomnilogic.com:80")
                .timeout(10, TimeUnit.SECONDS);
    }

    private synchronized String httpXmlResponse(String urlParameters) {
        int status;
        String statusMessage;

        try {
            String urlParameterslength = Integer.toString(urlParameters.length());

            ContentResponse httpResponse = sendRequestBuilder(config.hostname, HttpMethod.POST)
                    .content(new StringContentProvider(urlParameters), "text/xml; charset=utf-8")
                    .header(HttpHeader.CONTENT_LENGTH, urlParameterslength).send();

            status = httpResponse.getStatus();
            String xmlResponse = httpResponse.getContentAsString();

            if (!(evaluateXPath("/Response/Parameters//Parameter[@name='StatusMessage']/text()", xmlResponse)
                    .isEmpty())) {
                statusMessage = evaluateXPath("/Response/Parameters//Parameter[@name='StatusMessage']/text()",
                        xmlResponse).get(0);

            } else {
                statusMessage = httpResponse.getReason();
            }

            if (status == 200) {
                if (logger.isDebugEnabled()) {
                    logger.trace("{} Hayward http command: {}", getCallingMethod(), urlParameters);
                    logger.trace("{} Hayward http response: {} {}", getCallingMethod(), statusMessage, xmlResponse);
                } else if (logger.isInfoEnabled()) {
                    logger.debug("{} Hayward http response: {}", getCallingMethod(), statusMessage);
                }
                return xmlResponse;
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("{} Hayward http command: {}", getCallingMethod(), urlParameters);
                    logger.error("{} Hayward http response: {}", getCallingMethod(), status);
                }
                return null;
            }

        } catch (TimeoutException e) {
            if (logger.isErrorEnabled()) {
                logger.error("{}  Connection timeout to Hayward's server {} with username {}", getCallingMethod(),
                        config.hostname, config.username);
            }
            return null;
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("{}  Unable to open connection to Hayward's server {} with username {}",
                        getCallingMethod(), config.hostname, config.username, e);
            }
            return null;
        }
    }

    private String getCallingMethod() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[3];
        return e.getMethodName();
    }

    public int convertCommand(Command command) {
        if (command == OnOffType.ON) {
            return 1;
        } else {
            return 0;
        }
    }
}
