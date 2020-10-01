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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.config.HaywardConfig;
import org.openhab.binding.haywardomnilogic.internal.hayward.HaywardThingHandler;
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

@NonNullByDefault
public class HaywardBridgeHandler extends BaseBridgeHandler implements HaywardListener {
    private final Logger logger = LoggerFactory.getLogger(HaywardBridgeHandler.class);

    private List<HaywardHandlerListener> listeners = new ArrayList<>();
    private final HttpClient httpClient;
    private @Nullable ScheduledFuture<?> initializeFuture;
    private @Nullable ScheduledFuture<?> pollTelemetryFuture;
    private @Nullable ScheduledFuture<?> pollAlarmsFuture;
    private int commFailureCount;

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
        updateStatus(ThingStatus.UNKNOWN);

        initializeFuture = scheduler.schedule(this::scheduledInitialize, 1, TimeUnit.SECONDS);
        return;
    }

    public void scheduledInitialize() {
        config = getConfigAs(HaywardConfig.class);

        try {
            clearPolling(pollTelemetryFuture);
            clearPolling(pollAlarmsFuture);

            if (!(login())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to Login to Hayward's server");
                clearPolling(pollTelemetryFuture);
                clearPolling(pollAlarmsFuture);
                commFailureCount = 50;
                initPolling(60);
                return;
            }

            if (!(getSiteList())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to getMSP from Hayward's server");
                clearPolling(pollTelemetryFuture);
                clearPolling(pollAlarmsFuture);
                commFailureCount = 50;
                initPolling(60);
                return;
            }

            if (!(getMspConfig())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to getConfig from Hayward's server");
                clearPolling(pollTelemetryFuture);
                clearPolling(pollAlarmsFuture);
                commFailureCount = 50;
                initPolling(60);
                return;
            }

            updateStatus(ThingStatus.ONLINE);
            logger.trace("Succesfully opened connection to Hayward's server: {} Username:{}", config.hostname,
                    config.username);

            initPolling(0);
            logger.trace("Hayward Telemetry polling scheduled");

            if (config.alarmPollTime > 0) {
                initAlarmPolling(1);
                logger.trace("Hayward Alarm polling scheduled");
            } else {
                logger.trace("Hayward Alarm polling disabled");
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "scheduledInitialize exception");
            logger.error("Unable to open connection to Hayward Server: {} Username: {}", config.hostname,
                    config.username, e);
            clearPolling(pollTelemetryFuture);
            clearPolling(pollAlarmsFuture);
            commFailureCount = 50;
            initPolling(60);
            return;
        }
    }

    public synchronized boolean login() throws Exception {
        String xmlResponse;
        String status;

        // *****Login to Hayward server
        String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request>" + "<Name>Login</Name><Parameters>"
                + "<Parameter name=\"UserName\" dataType=\"String\">" + config.username + "</Parameter>"
                + "<Parameter name=\"Password\" dataType=\"String\">" + config.password + "</Parameter>"
                + "</Parameters></Request>";

        xmlResponse = httpXmlResponse(urlParameters);

        if (xmlResponse.isEmpty()) {
            return false;
        }

        status = evaluateXPath("/Response/Parameters//Parameter[@name='Status']/text()", xmlResponse).get(0);

        if (!(status.equals("0"))) {
            logger.error("Hayward Login XML response: {}", xmlResponse);
            return false;
        }

        config.token = evaluateXPath("/Response/Parameters//Parameter[@name='Token']/text()", xmlResponse).get(0);
        config.userID = evaluateXPath("/Response/Parameters//Parameter[@name='UserID']/text()", xmlResponse).get(0);
        return true;
    }

    public synchronized boolean getApiDef() throws Exception {
        String xmlResponse;

        // *****getConfig from Hayward server
        String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetAPIDef</Name><Parameters>"
                + "<Parameter name=\"Token\" dataType=\"String\">" + config.token + "</Parameter>"
                + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + config.mspSystemID + "</Parameter>;"
                + "<Parameter name=\"Version\" dataType=\"string\">0.4</Parameter >\r\n"
                + "<Parameter name=\"Language\" dataType=\"string\">en</Parameter >\r\n" + "</Parameters></Request>";

        xmlResponse = httpXmlResponse(urlParameters);

        if (xmlResponse.isEmpty()) {
            logger.error("Hayward Login XML response was null");
            return false;
        }
        return true;
    }

    public synchronized boolean getSiteList() throws Exception {
        String xmlResponse;
        String status;

        // *****Get MSP
        String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetSiteList</Name><Parameters>"
                + "<Parameter name=\"Token\" dataType=\"String\">" + config.token
                + "</Parameter><Parameter name=\"UserID\" dataType=\"String\">" + config.userID
                + "</Parameter></Parameters></Request>";

        xmlResponse = httpXmlResponse(urlParameters);

        if (xmlResponse.isEmpty()) {
            logger.error("Hayward getSiteList XML response was null");
            return false;
        }

        status = evaluateXPath("/Response/Parameters//Parameter[@name='Status']/text()", xmlResponse).get(0);

        if (!(status.equals("0"))) {
            logger.error("Hayward getSiteList XML response: {}", xmlResponse);
            return false;
        }

        config.mspSystemID = evaluateXPath("/Response/Parameters/Parameter/Item//Property[@name='MspSystemID']/text()",
                xmlResponse).get(0);
        config.backyardName = evaluateXPath(
                "/Response/Parameters/Parameter/Item//Property[@name='BackyardName']/text()", xmlResponse).get(0);
        config.address = evaluateXPath("/Response/Parameters/Parameter/Item//Property[@name='Address']/text()",
                xmlResponse).get(0);
        return true;
    }

    public synchronized boolean getMspConfig() throws Exception {
        String xmlResponse;
        List<String> systemIDs = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> bowName = new ArrayList<>();
        List<String> bowID = new ArrayList<>();
        List<String> property1 = new ArrayList<>();
        List<String> property2 = new ArrayList<>();
        List<String> property3 = new ArrayList<>();
        List<String> property4 = new ArrayList<>();

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

        if (xmlResponse.isEmpty()) {
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
            onDeviceDiscovered(HaywardTypeToRequest.BACKYARD, Integer.parseInt(config.mspSystemID), name, "", "", "",
                    "", "", "");
        }

        // Find Bodies of Water
        systemIDs = evaluateXPath("//Body-of-water/System-Id/text()", xmlResponse);
        names = evaluateXPath("//Body-of-water/Name/text()", xmlResponse);

        for (int i = 0; i < systemIDs.size(); i++) {
            onDeviceDiscovered(HaywardTypeToRequest.BOW, Integer.parseInt(systemIDs.get(i)), names.get(i), "", "", "",
                    "", "", "");
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
            bowID = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()",
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
            bowID = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()",
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
            bowID = evaluateXPath("//*[System-Id=" + systemIDs.get(i) + "]/ancestor::Body-of-water/System-Id/text()",
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
                onDeviceDiscovered(HaywardTypeToRequest.RELAY, Integer.parseInt(systemIDs.get(i)), names.get(i), "", "",
                        "", "", "", "");
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
            onDeviceDiscovered(HaywardTypeToRequest.VIRTUALHEATER, Integer.parseInt(systemIDs.get(i)), "Virtual Heater",
                    bowID.get(0), bowName.get(0), "", "", "", "");
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
        return true;
    }

    public synchronized boolean getTelemetryData() throws Exception {
        // *****Request Telemetry from Hayward server
        String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetTelemetryData</Name><Parameters>"
                + "<Parameter name=\"Token\" dataType=\"String\">" + config.token + "</Parameter>"
                + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + config.mspSystemID
                + "</Parameter></Parameters></Request>";

        String xmlResponse = httpXmlResponse(urlParameters);

        if (xmlResponse.isEmpty()) {
            logger.error("Hayward getTelemetry XML response was null");
            return false;
        }

        if (!evaluateXPath("/Response/Parameters//Parameter[@name='StatusMessage']/text()", xmlResponse).isEmpty()) {
            logger.error("Hayward getTelemetry XML response: {}", xmlResponse);
            return false;
        }

        for (Thing thing : getThing().getThings()) {
            if (thing != null && thing.getHandler() instanceof HaywardThingHandler) {
                HaywardThingHandler handler = (HaywardThingHandler) thing.getHandler();
                if (handler != null) {
                    handler.getTelemetry(xmlResponse);
                }
            }
        }
        return true;
    }

    public synchronized boolean getAlarmList() throws Exception {
        for (Thing thing : getThing().getThings()) {
            Map<String, String> properties = thing.getProperties();
            if (properties.get(HaywardBindingConstants.PROPERTY_TYPE).equals("BACKYARD")) {
                HaywardBackyardHandler handler = (HaywardBackyardHandler) thing.getHandler();
                if (handler != null) {
                    return handler.getAlarmList(properties.get(HaywardBindingConstants.PROPERTY_SYSTEM_ID));
                }
            }
        }
        return false;
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

    private void clearPolling(@Nullable ScheduledFuture<?> pollJob) {
        if (pollJob != null) {
            pollJob.cancel(false);
        }
    }

    @Nullable
    Thing getThingForType(HaywardTypeToRequest type, int num) {
        for (Thing thing : getThing().getThings()) {
            Map<String, String> properties = thing.getProperties();
            if (properties.get(HaywardBindingConstants.PROPERTY_SYSTEM_ID).equals(Integer.toString(num))) {
                if (properties.get(HaywardBindingConstants.PROPERTY_TYPE).equals(type.toString())) {
                    return thing;
                }
            }
        }
        return null;
    }

    public List<String> evaluateXPath(String xpathExp, String xmlResponse) throws Exception {
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

    public synchronized String httpXmlResponse(String urlParameters) throws Exception {
        String statusMessage;
        String urlParameterslength = Integer.toString(urlParameters.length());

        try {
            ContentResponse httpResponse = sendRequestBuilder(config.hostname, HttpMethod.POST)
                    .content(new StringContentProvider(urlParameters), "text/xml; charset=utf-8")
                    .header(HttpHeader.CONTENT_LENGTH, urlParameterslength).send();

            int status = httpResponse.getStatus();
            String xmlResponse = httpResponse.getContentAsString();

            if (!(evaluateXPath("/Response/Parameters//Parameter[@name='StatusMessage']/text()", xmlResponse)
                    .isEmpty())) {
                statusMessage = evaluateXPath("/Response/Parameters//Parameter[@name='StatusMessage']/text()",
                        xmlResponse).get(0);

            } else {
                statusMessage = httpResponse.getReason();
            }

            if (status == 200) {
                if (logger.isTraceEnabled()) {
                    logger.trace("{} Hayward http command: {}", getCallingMethod(), urlParameters);
                    logger.trace("{} Hayward http response: {} {}", getCallingMethod(), statusMessage, xmlResponse);
                } else if (logger.isDebugEnabled()) {
                    logger.debug("{} Hayward http response: {}", getCallingMethod(), statusMessage);
                }
                return xmlResponse;
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("{} Hayward http command: {}", getCallingMethod(), urlParameters);
                    logger.error("{} Hayward http response: {}", getCallingMethod(), status);
                }
                return "";
            }
        } catch (java.net.UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to resolve host.  Check Hayward hostname and your internet connection.");
            return "";
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connection Timeout.  Check Hayward hostname and your internet connection.");
            return "";
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
