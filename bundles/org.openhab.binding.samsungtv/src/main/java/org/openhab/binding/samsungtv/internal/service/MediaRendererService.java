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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The {@link MediaRendererService} is responsible for handling MediaRenderer
 * commands.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Nick Waterton - added checkConnection(), getServiceName, refactored
 */
@NonNullByDefault
public class MediaRendererService implements UpnpIOParticipant, SamsungTvService {

    private final Logger logger = LoggerFactory.getLogger(MediaRendererService.class);
    public static final String SERVICE_NAME = "MediaRenderer";
    private static final String SERVICE_RENDERING_CONTROL = "RenderingControl";
    private static final List<String> SUPPORTED_CHANNELS = List.of(VOLUME, MUTE, BRIGHTNESS, CONTRAST, SHARPNESS,
            COLOR_TEMPERATURE);
    protected static final int SUBSCRIPTION_DURATION = 1800;
    private static final List<String> ON_VALUE = List.of("true", "1");

    private final UpnpIOService service;

    private final String udn;
    private String host = "";

    private final SamsungTvHandler handler;

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<>());

    private boolean started;
    private boolean subscription;

    public MediaRendererService(UpnpIOService upnpIOService, String udn, String host, SamsungTvHandler handler) {
        this.service = upnpIOService;
        this.udn = udn;
        this.handler = handler;
        this.host = host;
        logger.debug("{}: Creating a Samsung TV MediaRenderer service: subscription={}", host, getSubscription());
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
                // Have to do this because old TV's don't update subscriptions properly
                if (handler.configuration.isWebsocketProtocol()) {
                    return List.of();
                }
            }
            return SUPPORTED_CHANNELS;
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
                    case VOLUME:
                        updateResourceState("GetVolume");
                        break;
                    case MUTE:
                        updateResourceState("GetMute");
                        break;
                    case BRIGHTNESS:
                        updateResourceState("GetBrightness");
                        break;
                    case CONTRAST:
                        updateResourceState("GetContrast");
                        break;
                    case SHARPNESS:
                        updateResourceState("GetSharpness");
                        break;
                    case COLOR_TEMPERATURE:
                        updateResourceState("GetColorTemperature");
                        break;
                    default:
                        break;
                }
            }
            return true;
        }

        switch (channel) {
            case VOLUME:
                if (command instanceof DecimalType) {
                    result = sendCommand("SetVolume", cmdToString(command));
                }
                break;
            case MUTE:
                if (command instanceof OnOffType) {
                    result = sendCommand("SetMute", cmdToString(command));
                }
                break;
            case BRIGHTNESS:
                if (command instanceof DecimalType) {
                    result = sendCommand("SetBrightness", cmdToString(command));
                }
                break;
            case CONTRAST:
                if (command instanceof DecimalType) {
                    result = sendCommand("SetContrast", cmdToString(command));
                }
                break;
            case SHARPNESS:
                if (command instanceof DecimalType) {
                    result = sendCommand("SetSharpness", cmdToString(command));
                }
                break;
            case COLOR_TEMPERATURE:
                if (command instanceof DecimalType commandAsDecimalType) {
                    int newValue = Math.max(0, Math.min(commandAsDecimalType.intValue(), 4));
                    result = sendCommand("SetColorTemperature", Integer.toString(newValue));
                }
                break;
            default:
                logger.warn("{}: Samsung TV doesn't support transmitting for channel '{}'", host, channel);
                return false;
        }
        if (!result) {
            logger.warn("{}: media renderer: wrong command type {} channel {}", host, command, channel);
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
            logger.debug("{}: Subscribing to service {}...", host, SERVICE_RENDERING_CONTROL);
            service.addSubscription(this, SERVICE_RENDERING_CONTROL, SUBSCRIPTION_DURATION);
        }
    }

    private void removeSubscription() {
        // Remove GENA Subscriptions
        if (isRegistered() && subscription) {
            logger.debug("{}: Unsubscribing from service {}...", host, SERVICE_RENDERING_CONTROL);
            service.removeSubscription(this, SERVICE_RENDERING_CONTROL);
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
            case "LastChange":
                stateMap.remove("InstanceID");
                parseEventValues(value);
                break;
            case "Volume":
                handler.valueReceived(VOLUME, new PercentType(value));
                break;
            case "Mute":
                handler.valueReceived(MUTE,
                        ON_VALUE.stream().anyMatch(value::equalsIgnoreCase) ? OnOffType.ON : OnOffType.OFF);
                break;
            case "Brightness":
                handler.valueReceived(BRIGHTNESS, new PercentType(value));
                break;
            case "Contrast":
                handler.valueReceived(CONTRAST, new PercentType(value));
                break;
            case "Sharpness":
                handler.valueReceived(SHARPNESS, new PercentType(value));
                break;
            case "ColorTemperature":
                handler.valueReceived(COLOR_TEMPERATURE, new DecimalType(value));
                break;
        }
    }

    protected Map<String, String> updateResourceState(String actionId) {
        return updateResourceState(actionId, Map.of());
    }

    protected synchronized Map<String, String> updateResourceState(String actionId, Map<String, String> inputs) {
        Map<String, String> inputsMap = new LinkedHashMap<String, String>(Map.of("InstanceID", "0"));
        if (Utils.isSoundChannel(actionId)) {
            inputsMap.put("Channel", "Master");
        }
        inputsMap.putAll(inputs);
        Map<String, String> result = service.invokeAction(this, SERVICE_RENDERING_CONTROL, actionId, inputsMap);
        if (!subscription) {
            result.keySet().stream().forEach(a -> onValueReceived(a, result.get(a), SERVICE_RENDERING_CONTROL));
        }
        return result;
    }

    private boolean sendCommand(String command, String value) {
        updateResourceState(command, Map.of(command.replace("Set", "Desired"), value));
        if (!subscription) {
            updateResourceState(command.replace("Set", "Get"));
        }
        return true;
    }

    private String cmdToString(Command command) {
        if (command instanceof DecimalType commandAsDecimalType) {
            return Integer.toString(commandAsDecimalType.intValue());
        }
        if (command instanceof OnOffType) {
            return Boolean.toString(command.equals(OnOffType.ON));
        }
        return command.toString();
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
            if ("InstanceID".equals(el.getNodeName())) {
                stateMap.put(el.getNodeName(), el.getAttribute("val"));
            }
            if (SUPPORTED_CHANNELS.stream().filter(a -> "0".equals(stateMap.get("InstanceID")))
                    .anyMatch(el.getNodeName()::equalsIgnoreCase)) {
                if (Utils.isSoundChannel(el.getNodeName()) && !"Master".equals(el.getAttribute("channel"))) {
                    return;
                }
                logger.trace("{}: Processing {}:{}", host, el.getNodeName(), el.getAttribute("val"));
                onValueReceived(el.getNodeName(), el.getAttribute("val"), SERVICE_RENDERING_CONTROL);
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
