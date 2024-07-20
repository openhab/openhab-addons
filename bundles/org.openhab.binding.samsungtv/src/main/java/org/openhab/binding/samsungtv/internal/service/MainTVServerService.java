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
package org.openhab.binding.samsungtv.internal.service;

import static org.openhab.binding.samsungtv.internal.SamsungTvBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.samsungtv.internal.Utils;
import org.openhab.binding.samsungtv.internal.handler.SamsungTvHandler;
import org.openhab.binding.samsungtv.internal.service.api.SamsungTvService;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The {@link MainTVServerService} is responsible for handling MainTVServer
 * commands.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Nick Waterton - add checkConnection(), getServiceName, some refactoring
 */
@NonNullByDefault
public class MainTVServerService implements UpnpIOParticipant, SamsungTvService {

    public static final String SERVICE_NAME = "MainTVServer2";
    private static final String SERVICE_MAIN_AGENT = "MainTVAgent2";
    private static final List<String> SUPPORTED_CHANNELS = List.of(SOURCE_NAME, SOURCE_ID, BROWSER_URL, STOP_BROWSER);
    private static final List<String> REFRESH_CHANNELS = List.of(CHANNEL, SOURCE_NAME, SOURCE_ID, PROGRAM_TITLE,
            CHANNEL_NAME, BROWSER_URL);
    private static final List<String> SUBSCRIPTION_REFRESH_CHANNELS = List.of(SOURCE_NAME);
    protected static final int SUBSCRIPTION_DURATION = 1800;
    private final Logger logger = LoggerFactory.getLogger(MainTVServerService.class);

    private final UpnpIOService service;

    private final String udn;
    private String host = "";

    private final SamsungTvHandler handler;

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, String> sources = Collections.synchronizedMap(new HashMap<>());

    private boolean started;
    private boolean subscription;

    public MainTVServerService(UpnpIOService upnpIOService, String udn, String host, SamsungTvHandler handler) {
        this.service = upnpIOService;
        this.udn = udn;
        this.handler = handler;
        this.host = host;
        logger.debug("{}: Creating a Samsung TV MainTVServer service: subscription={}", host, getSubscription());
    }

    private boolean getSubscription() {
        return handler.configuration.getSubscription();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public List<String> getSupportedChannelNames(boolean refresh) {
        if (refresh) {
            if (subscription) {
                return SUBSCRIPTION_REFRESH_CHANNELS;
            }
            return REFRESH_CHANNELS;
        }
        logger.trace("{}: getSupportedChannelNames: {}", host, SUPPORTED_CHANNELS);
        return SUPPORTED_CHANNELS;
    }

    @Override
    public void start() {
        service.registerParticipant(this);
        addSubscription();
        started = true;
    }

    @Override
    public void stop() {
        removeSubscription();
        service.unregisterParticipant(this);
        started = false;
    }

    @Override
    public void clearCache() {
        stateMap.clear();
        sources.clear();
    }

    @Override
    public boolean isUpnp() {
        return true;
    }

    @Override
    public boolean checkConnection() {
        return started;
    }

    @Override
    public boolean handleCommand(String channel, Command command) {
        logger.trace("{}: Received channel: {}, command: {}", host, channel, command);
        boolean result = false;

        if (!checkConnection()) {
            return false;
        }

        if (command == RefreshType.REFRESH) {
            if (isRegistered()) {
                switch (channel) {
                    case CHANNEL:
                        updateResourceState("GetCurrentMainTVChannel");
                        break;
                    case SOURCE_NAME:
                    case SOURCE_ID:
                        updateResourceState("GetCurrentExternalSource");
                        break;
                    case PROGRAM_TITLE:
                    case CHANNEL_NAME:
                        updateResourceState("GetCurrentContentRecognition");
                        break;
                    case BROWSER_URL:
                        updateResourceState("GetCurrentBrowserURL");
                        break;
                    default:
                        break;
                }
            }
            return true;
        }

        switch (channel) {
            case SOURCE_ID:
                if (command instanceof DecimalType) {
                    command = new StringType(command.toString());
                }
            case SOURCE_NAME:
                if (command instanceof StringType) {
                    result = setSourceName(command);
                    updateResourceState("GetCurrentExternalSource");
                }
                break;
            case BROWSER_URL:
                if (command instanceof StringType) {
                    result = setBrowserUrl(command);
                }
                break;
            case STOP_BROWSER:
                if (command instanceof OnOffType) {
                    // stop browser if command is On or Off
                    result = stopBrowser();
                    if (result) {
                        onValueReceived("BrowserURL", "", SERVICE_MAIN_AGENT);
                    }
                }
                break;
            default:
                logger.warn("{}: Samsung TV doesn't support send for channel '{}'", host, channel);
                return false;
        }
        if (!result) {
            logger.warn("{}: main tvservice: command error {} channel {}", host, command, channel);
        }
        return result;
    }

    private boolean isRegistered() {
        return service.isRegistered(this);
    }

    @Override
    public String getUDN() {
        return udn;
    }

    private void addSubscription() {
        // Set up GENA Subscriptions
        if (isRegistered() && getSubscription()) {
            logger.debug("{}: Subscribing to service {}...", host, SERVICE_MAIN_AGENT);
            service.addSubscription(this, SERVICE_MAIN_AGENT, SUBSCRIPTION_DURATION);
        }
    }

    private void removeSubscription() {
        // Remove GENA Subscriptions
        if (isRegistered() && subscription) {
            logger.debug("{}: Unsubscribing from service {}...", host, SERVICE_MAIN_AGENT);
            service.removeSubscription(this, SERVICE_MAIN_AGENT);
        }
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        if (service == null) {
            return;
        }
        subscription = succeeded;
        logger.debug("{}: Subscription to service {} {}", host, service, succeeded ? "succeeded" : "failed");
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        if (variable == null || value == null || service == null || variable.isBlank()) {
            return;
        }

        variable = variable.replace("Current", "");
        String oldValue = stateMap.getOrDefault(variable, "None");
        if (value.equals(oldValue)) {
            logger.trace("{}: Value '{}' for {} hasn't changed, ignoring update", host, value, variable);
            return;
        }

        stateMap.put(variable, value);

        switch (variable) {
            case "A_ARG_TYPE_LastChange":
                parseEventValues(value);
                break;
            case "ProgramTitle":
                handler.valueReceived(PROGRAM_TITLE, new StringType(value));
                break;
            case "ChannelName":
                handler.valueReceived(CHANNEL_NAME, new StringType(value));
                break;
            case "ExternalSource":
                handler.valueReceived(SOURCE_NAME, new StringType(value));
                break;
            case "MajorCh":
                handler.valueReceived(CHANNEL, new DecimalType(value));
                break;
            case "ID":
                handler.valueReceived(SOURCE_ID, new DecimalType(value));
                break;
            case "BrowserURL":
                handler.valueReceived(BROWSER_URL, new StringType(value));
                break;
        }
    }

    protected Map<String, String> updateResourceState(String actionId) {
        return updateResourceState(actionId, Map.of());
    }

    protected synchronized Map<String, String> updateResourceState(String actionId, Map<String, String> inputs) {
        Map<String, String> result = Objects.requireNonNull(
                Optional.of(service).map(a -> a.invokeAction(this, SERVICE_MAIN_AGENT, actionId, inputs))
                        .filter(a -> !a.isEmpty()).orElse(Map.of("Result", "Command Failed")));
        if (isOk(result)) {
            result.keySet().stream().filter(a -> !"Result".equals(a)).forEach(a -> {
                String val = result.getOrDefault(a, "");
                if ("CurrentChannel".equals(a)) {
                    val = parseCurrentChannel(val);
                    a = "MajorCh";
                }
                onValueReceived(a, val, SERVICE_MAIN_AGENT);
            });
        }
        return result;
    }

    public boolean isOk(Map<String, String> result) {
        return result.getOrDefault("Result", "Error").equals("OK");
    }

    /**
     * Searches sources for source, or ID, and sets TV input to that value
     */
    private boolean setSourceName(Command command) {
        String tmpSource = command.toString();
        if (sources.isEmpty()) {
            getSourceMap();
        }
        String source = Objects.requireNonNull(sources.entrySet().stream().filter(a -> a.getValue().equals(tmpSource))
                .map(a -> a.getKey()).findFirst().orElse(tmpSource));
        Map<String, String> result = updateResourceState("SetMainTVSource",
                Map.of("Source", source, "ID", sources.getOrDefault(source, "0"), "UiID", "0"));
        logResult(result.getOrDefault("Result", "Unable to Set Source Name: " + source));
        return isOk(result);
    }

    private boolean setBrowserUrl(Command command) {
        Map<String, String> result = updateResourceState("RunBrowser", Map.of("BrowserURL", command.toString()));
        logResult(result.getOrDefault("Result", "Unable to Set browser URL: " + command.toString()));
        return isOk(result);
    }

    private boolean stopBrowser() {
        Map<String, String> result = updateResourceState("StopBrowser");
        logResult(result.getOrDefault("Result", "Unable to Stop Browser"));
        return isOk(result);
    }

    private void logResult(String ok) {
        if ("OK".equals(ok)) {
            logger.debug("{}: Command successfully executed", host);
        } else {
            logger.warn("{}: Command execution failed, result='{}'", host, ok);
        }
    }

    private String parseCurrentChannel(String xml) {
        return Objects.requireNonNull(Utils.loadXMLFromString(xml, host).map(a -> a.getDocumentElement())
                .map(a -> getFirstNodeValue(a, "MajorCh", "-1")).orElse("-1"));
    }

    private void getSourceMap() {
        // NodeList doesn't have a stream, so do this
        sources = Objects.requireNonNull(
                Optional.of(updateResourceState("GetSourceList")).filter(a -> "OK".equals(a.get("Result")))
                        .map(a -> a.get("SourceList")).flatMap(xml -> Utils.loadXMLFromString(xml, host))
                        .map(a -> a.getDocumentElement()).map(a -> a.getElementsByTagName("Source")).map(
                                nList -> IntStream.range(0, nList.getLength()).boxed().map(i -> (Element) nList.item(i))
                                        .collect(Collectors.toMap(a -> getFirstNodeValue(a, "SourceType", ""),
                                                a -> getFirstNodeValue(a, "ID", ""), (key1, key2) -> key2)))
                        .orElse(Map.of()));
    }

    private String getFirstNodeValue(Element nodeList, String node, String ifNone) {
        return Objects.requireNonNull(Optional.ofNullable(nodeList).map(a -> a.getElementsByTagName(node))
                .filter(a -> a.getLength() > 0).map(a -> a.item(0)).map(a -> a.getTextContent()).orElse(ifNone));
    }

    /**
     * Parse Subscription Event from {@link String} which contains XML content.
     * Parses all child Nodes recursively.
     * If valid channel update is found, call onValueReceived()
     *
     * @param xml{@link String} which contains XML content.
     */
    public void parseEventValues(String xml) {
        Utils.loadXMLFromString(xml, host).ifPresent(a -> visitRecursively(a));
    }

    public void visitRecursively(Node node) {
        // get all child nodes, NodeList doesn't have a stream, so do this
        Optional.ofNullable(node.getChildNodes()).ifPresent(nList -> IntStream.range(0, nList.getLength())
                .mapToObj(i -> (Node) nList.item(i)).forEach(childNode -> parseNode(childNode)));
    }

    public void parseNode(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element el = (Element) node;
            switch (el.getNodeName()) {
                case "BrowserChanged":
                    if ("Disable".equals(el.getTextContent())) {
                        onValueReceived("BrowserURL", "", SERVICE_MAIN_AGENT);
                    } else {
                        updateResourceState("GetCurrentBrowserURL");
                    }
                    break;
                case "PowerOFF":
                    logger.debug("{}: TV has Powered Off", host);
                    handler.setOffline();
                    break;
                case "MajorCh":
                case "ChannelName":
                case "ProgramTitle":
                case "ExternalSource":
                case "ID":
                case "BrowserURL":
                    logger.trace("{}: Processing {}:{}", host, el.getNodeName(), el.getTextContent());
                    onValueReceived(el.getNodeName(), el.getTextContent(), SERVICE_MAIN_AGENT);
                    break;
            }
        }
        // visit child node
        visitRecursively(node);
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.trace("{}: onStatusChanged: status={}", host, status);
        if (!status) {
            handler.setOffline();
        }
    }
}
