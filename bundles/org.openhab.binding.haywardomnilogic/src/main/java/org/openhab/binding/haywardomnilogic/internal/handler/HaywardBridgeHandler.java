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
package org.openhab.binding.haywardomnilogic.internal.handler;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
import org.openhab.binding.haywardomnilogic.internal.HaywardAccount;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.HaywardDynamicStateDescriptionProvider;
import org.openhab.binding.haywardomnilogic.internal.HaywardException;
import org.openhab.binding.haywardomnilogic.internal.HaywardThingHandler;
import org.openhab.binding.haywardomnilogic.internal.HaywardTypeToRequest;
import org.openhab.binding.haywardomnilogic.internal.config.HaywardConfig;
import org.openhab.binding.haywardomnilogic.internal.discovery.HaywardDiscoveryService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragment;
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
public class HaywardBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardBridgeHandler.class);
    private final HaywardDynamicStateDescriptionProvider stateDescriptionProvider;
    private final HttpClient httpClient;
    private @Nullable ScheduledFuture<?> initializeFuture;
    private @Nullable ScheduledFuture<?> pollTelemetryFuture;
    private @Nullable ScheduledFuture<?> pollAlarmsFuture;
    private int commFailureCount;
    public HaywardConfig config = getConfig().as(HaywardConfig.class);
    public HaywardAccount account = getConfig().as(HaywardAccount.class);

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(HaywardDiscoveryService.class);
    }

    public HaywardBridgeHandler(HaywardDynamicStateDescriptionProvider stateDescriptionProvider, Bridge bridge,
            HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
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

            if (!(mspConfigUnits())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to getMSPConfigUnits from Hayward's server");
                clearPolling(pollTelemetryFuture);
                clearPolling(pollAlarmsFuture);
                commFailureCount = 50;
                initPolling(60);
                return;
            }

            if (logger.isTraceEnabled()) {
                if (!(getApiDef())) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unable to getApiDef from Hayward's server");
                    clearPolling(pollTelemetryFuture);
                    clearPolling(pollAlarmsFuture);
                    commFailureCount = 50;
                    initPolling(60);
                    return;
                }
            }

            if (this.thing.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

            logger.debug("Succesfully opened connection to Hayward's server: {} Username:{}", config.endpointUrl,
                    config.username);

            initPolling(0);
            logger.trace("Hayward Telemetry polling scheduled");

            if (config.alarmPollTime > 0) {
                initAlarmPolling(1);
            }
        } catch (HaywardException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "scheduledInitialize exception: " + e.getMessage());
            clearPolling(pollTelemetryFuture);
            clearPolling(pollAlarmsFuture);
            commFailureCount = 50;
            initPolling(60);
            return;
        } catch (InterruptedException e) {
            return;
        }
    }

    public synchronized boolean login() throws HaywardException, InterruptedException {
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

        if (!("0".equals(status))) {
            logger.debug("Hayward Connection thing: Login XML response: {}", xmlResponse);
            return false;
        }

        account.token = evaluateXPath("/Response/Parameters//Parameter[@name='Token']/text()", xmlResponse).get(0);
        account.userID = evaluateXPath("/Response/Parameters//Parameter[@name='UserID']/text()", xmlResponse).get(0);
        return true;
    }

    public synchronized boolean getApiDef() throws HaywardException, InterruptedException {
        String xmlResponse;

        // *****getApiDef from Hayward server
        String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetAPIDef</Name><Parameters>"
                + "<Parameter name=\"Token\" dataType=\"String\">" + account.token + "</Parameter>"
                + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + account.mspSystemID + "</Parameter>;"
                + "<Parameter name=\"Version\" dataType=\"string\">0.4</Parameter >\r\n"
                + "<Parameter name=\"Language\" dataType=\"string\">en</Parameter >\r\n" + "</Parameters></Request>";

        xmlResponse = httpXmlResponse(urlParameters);

        if (xmlResponse.isEmpty()) {
            logger.debug("Hayward Connection thing: Login XML response was null");
            return false;
        }
        return true;
    }

    public synchronized boolean getSiteList() throws HaywardException, InterruptedException {
        String xmlResponse;
        String status;

        // *****Get MSP
        String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetSiteList</Name><Parameters>"
                + "<Parameter name=\"Token\" dataType=\"String\">" + account.token
                + "</Parameter><Parameter name=\"UserID\" dataType=\"String\">" + account.userID
                + "</Parameter></Parameters></Request>";

        xmlResponse = httpXmlResponse(urlParameters);

        if (xmlResponse.isEmpty()) {
            logger.debug("Hayward Connection thing: getSiteList XML response was null");
            return false;
        }

        status = evaluateXPath("/Response/Parameters//Parameter[@name='Status']/text()", xmlResponse).get(0);

        if (!("0".equals(status))) {
            logger.debug("Hayward Connection thing: getSiteList XML response: {}", xmlResponse);
            return false;
        }

        account.mspSystemID = evaluateXPath("/Response/Parameters/Parameter/Item//Property[@name='MspSystemID']/text()",
                xmlResponse).get(0);
        account.backyardName = evaluateXPath(
                "/Response/Parameters/Parameter/Item//Property[@name='BackyardName']/text()", xmlResponse).get(0);
        account.address = evaluateXPath("/Response/Parameters/Parameter/Item//Property[@name='Address']/text()",
                xmlResponse).get(0);
        return true;
    }

    public synchronized String getMspConfig() throws HaywardException, InterruptedException {
        // *****getMspConfig from Hayward server
        String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetMspConfigFile</Name><Parameters>"
                + "<Parameter name=\"Token\" dataType=\"String\">" + account.token + "</Parameter>"
                + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + account.mspSystemID
                + "</Parameter><Parameter name=\"Version\" dataType=\"string\">0</Parameter>\r\n"
                + "</Parameters></Request>";

        String xmlResponse = httpXmlResponse(urlParameters);

        if (xmlResponse.isEmpty()) {
            logger.debug("Hayward Connection thing: requestConfig XML response was null");
            return "Fail";
        }

        if (evaluateXPath("//Backyard/Name/text()", xmlResponse).isEmpty()) {
            logger.debug("Hayward Connection thing: requestConfiguration XML response: {}", xmlResponse);
            return "Fail";
        }
        return xmlResponse;
    }

    public synchronized boolean mspConfigUnits() throws HaywardException, InterruptedException {
        List<String> property1 = new ArrayList<>();
        List<String> property2 = new ArrayList<>();

        String xmlResponse = getMspConfig();

        // Get Units (Standard, Metric)
        property1 = evaluateXPath("//System/Units/text()", xmlResponse);
        account.units = property1.get(0);

        // Get Variable Speed Pump Units (percent, RPM)
        property2 = evaluateXPath("//System/Msp-Vsp-Speed-Format/text()", xmlResponse);
        account.vspSpeedFormat = property2.get(0);

        return true;
    }

    public synchronized boolean getTelemetryData() throws HaywardException, InterruptedException {
        // *****getTelemetry from Hayward server
        String urlParameters = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Request><Name>GetTelemetryData</Name><Parameters>"
                + "<Parameter name=\"Token\" dataType=\"String\">" + account.token + "</Parameter>"
                + "<Parameter name=\"MspSystemID\" dataType=\"int\">" + account.mspSystemID
                + "</Parameter></Parameters></Request>";

        String xmlResponse = httpXmlResponse(urlParameters);

        if (xmlResponse.isEmpty()) {
            logger.debug("Hayward Connection thing: getTelemetry XML response was null");
            return false;
        }

        if (!evaluateXPath("/Response/Parameters//Parameter[@name='StatusMessage']/text()", xmlResponse).isEmpty()) {
            logger.debug("Hayward Connection thing: getTelemetry XML response: {}", xmlResponse);
            return false;
        }

        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof HaywardThingHandler) {
                HaywardThingHandler handler = (HaywardThingHandler) thing.getHandler();
                if (handler != null) {
                    handler.getTelemetry(xmlResponse);
                }
            }
        }
        return true;
    }

    public synchronized boolean getAlarmList() throws HaywardException {
        for (Thing thing : getThing().getThings()) {
            Map<String, String> properties = thing.getProperties();
            if ("BACKYARD".equals(properties.get(HaywardBindingConstants.PROPERTY_TYPE))) {
                HaywardBackyardHandler handler = (HaywardBackyardHandler) thing.getHandler();
                if (handler != null) {
                    String systemID = properties.get(HaywardBindingConstants.PROPERTY_SYSTEM_ID);
                    if (systemID != null) {
                        return handler.getAlarmList(systemID);
                    }
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
                updateStatus(ThingStatus.ONLINE);
            } catch (HaywardException e) {
                logger.debug("Hayward Connection thing: Exception during poll: {}", e.getMessage());
            } catch (InterruptedException e) {
                return;
            }
        }, initalDelay, config.telemetryPollTime, TimeUnit.SECONDS);
        return;
    }

    private synchronized void initAlarmPolling(int initalDelay) {
        pollAlarmsFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                getAlarmList();
            } catch (HaywardException e) {
                logger.debug("Hayward Connection thing: Exception during poll: {}", e.getMessage());
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
            if (Integer.toString(num).equals(properties.get(HaywardBindingConstants.PROPERTY_SYSTEM_ID))) {
                if (type.toString().equals(properties.get(HaywardBindingConstants.PROPERTY_TYPE))) {
                    return thing;
                }
            }
        }
        return null;
    }

    public List<String> evaluateXPath(String xpathExp, String xmlResponse) {
        List<String> values = new ArrayList<>();
        try {
            InputSource inputXML = new InputSource(new StringReader(xmlResponse));
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xPath.evaluate(xpathExp, inputXML, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                values.add(nodes.item(i).getNodeValue());
            }
        } catch (XPathExpressionException e) {
            logger.warn("XPathExpression exception: {}", e.getMessage());
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

    public synchronized String httpXmlResponse(String urlParameters) throws HaywardException, InterruptedException {
        String urlParameterslength = Integer.toString(urlParameters.length());
        String statusMessage;

        try {
            ContentResponse httpResponse = sendRequestBuilder(config.endpointUrl, HttpMethod.POST)
                    .content(new StringContentProvider(urlParameters), "text/xml; charset=utf-8")
                    .header(HttpHeader.CONTENT_LENGTH, urlParameterslength).send();

            int status = httpResponse.getStatus();
            String xmlResponse = httpResponse.getContentAsString();

            if (status == 200) {
                List<String> statusMessages = evaluateXPath(
                        "/Response/Parameters//Parameter[@name='StatusMessage']/text()", xmlResponse);
                if (!(statusMessages.isEmpty())) {
                    statusMessage = statusMessages.get(0);
                } else {
                    statusMessage = httpResponse.getReason();
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("Hayward Connection thing:  {} Hayward http command: {}", getCallingMethod(),
                            urlParameters);
                    logger.trace("Hayward Connection thing:  {} Hayward http response: {} {}", getCallingMethod(),
                            statusMessage, xmlResponse);
                }
                return xmlResponse;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Hayward Connection thing:  {} Hayward http command: {}", getCallingMethod(),
                            urlParameters);
                    logger.debug("Hayward Connection thing:  {} Hayward http response: {} {}", getCallingMethod(),
                            status, xmlResponse);
                }
                return "";
            }
        } catch (ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to resolve host.  Check Hayward hostname and your internet connection. " + e);
            return "";
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connection Timeout.  Check Hayward hostname and your internet connection. " + e);
            return "";
        }
    }

    private String getCallingMethod() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[3];
        return e.getMethodName();
    }

    void updateChannelStateDescriptionFragment(Channel channel, StateDescriptionFragment descriptionFragment) {
        ChannelUID channelId = channel.getUID();
        stateDescriptionProvider.setStateDescriptionFragment(channelId, descriptionFragment);
    }

    public int convertCommand(Command command) {
        if (command == OnOffType.ON) {
            return 1;
        } else {
            return 0;
        }
    }
}
