/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.meross.internal.ContentAnonymizer;
import org.openhab.binding.meross.internal.api.MerossEnum.Namespace;
import org.openhab.binding.meross.internal.command.Command;
import org.openhab.binding.meross.internal.dto.MqttMessageBuilder;
import org.openhab.binding.meross.internal.factory.ModeFactory;
import org.openhab.binding.meross.internal.factory.TypeFactory;
import org.openhab.binding.meross.internal.handler.MerossDeviceHandlerCallback;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MerossManager} class is responsible for implementing general functionalities to interact with
 * appliances
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Single manager per device
 * @author Mark Herwege - Use mqtt transport, receive mqtt push messages
 * @author Mark Herwege - Response parsing
 * @author Mark Herwege - Local http connection
 */
@NonNullByDefault
public class MerossManager implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(MerossManager.class);

    private HttpClient httpClient;
    private MerossMqttConnector mqttConnector;
    private @Nullable MerossHttpConnector httpConnector;
    private String deviceUUID;
    private MerossDeviceHandlerCallback callback;

    private String deviceRequestTopic;

    private static final int FUTURE_TIMOUT_SEC = 5;
    private @Nullable CompletableFuture<Boolean> ipInitialized;
    private @Nullable CompletableFuture<String> responseFuture;
    private @Nullable CompletableFuture<String> abilitiesFuture;

    private Set<String> abilities = Set.of();

    private static final Gson GSON = new Gson();
    private static final Type ABILITIES_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    public MerossManager(HttpClient httpClient, MerossMqttConnector mqttConnector, String deviceUUID,
            MerossDeviceHandlerCallback callback) {
        this.deviceUUID = deviceUUID;
        this.deviceRequestTopic = MqttMessageBuilder.buildDeviceRequestTopic(deviceUUID);
        this.callback = callback;
        this.httpClient = httpClient;
        this.mqttConnector = mqttConnector;
        mqttConnector.addClientUserTopicSubscriber(this);
        mqttConnector.addClientResponseTopicSubscriber(this);
    }

    private void setHttpConnector() {
        String ipAddress = callback.getIpAddress();
        if (ipAddress != null) {
            // Check if a valid address, return if not
            try {
                InetAddress.getByName(ipAddress);
            } catch (UnknownHostException e) {
                httpConnector = null;
                return;
            }
            httpConnector = new MerossHttpConnector.Builder().httpClient(httpClient)
                    .setApiBaseUrl("http://" + ipAddress + "/config").build();
        }
    }

    public void dispose() {
        mqttConnector.removeDeviceRequestTopicSubscriber(this, deviceUUID);
    }

    public void initialize() throws MqttException, InterruptedException {
        // if there is an IP adress configured, we can build the httpConnector straight away
        setHttpConnector();

        try {
            getSystemAll().handle((result, ex) -> {
                if (ex != null) {
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof TimeoutException) {
                        try {
                            // We give it a chance to retrieve the IP address from the cloud MQTT broker if talking to
                            // the cloud. We can safely continue on timeout and only use cloud communication. The device
                            // IP will not have been set, so we will not be able to use local communication if it wasn't
                            // already set in the thing configuration.
                            getAbilities();
                        } catch (MqttException | InterruptedException me) {
                            throw new CompletionException(me);
                        }
                        return null;
                    } else {
                        throw new CompletionException(cause);
                    }
                } else {
                    try {
                        getAbilities();
                    } catch (MqttException | InterruptedException e) {
                        throw new CompletionException(e);
                    }
                    return null;
                }
            }).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof MqttException me) {
                throw me;
            } else if (e.getCause() instanceof InterruptedException ie) {
                throw ie;
            }
            throw new MqttException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedException();
        }
    }

    private synchronized void publishMessage(String topic, byte[] message) throws MqttException, InterruptedException {
        logger.trace("Publishing topic {}, message {}", ContentAnonymizer.anonymizeTopic(topic),
                ContentAnonymizer.anonymizeMessage(new String(message, StandardCharsets.UTF_8)));

        MerossHttpConnector httpConnector = this.httpConnector;
        if (httpConnector != null) {
            logger.trace("Publishing to local http...");
            try {
                String response = httpConnector.postResponse(message);
                processMessage(topic, response.getBytes());
                return;
            } catch (IOException e) {
                logger.debug("Error communicating to device with IP address {} in LAN, trying cloud",
                        callback.getIpAddress());
                logger.trace("Error: ", e);
            }
        }
        logger.trace("Publishing to mqtt...");
        mqttConnector.publishMqttMessage(topic, message);
    }

    private CompletableFuture<Boolean> getSystemAll() throws MqttException, InterruptedException {
        byte[] message = MqttMessageBuilder.buildMqttMessage("GET", MerossEnum.Namespace.SYSTEM_ALL.value(), deviceUUID,
                Collections.emptyMap());
        CompletableFuture<Boolean> ipInitialized = new CompletableFuture<Boolean>();
        publishMessage(deviceRequestTopic, message);
        ipInitialized.orTimeout(FUTURE_TIMOUT_SEC, TimeUnit.SECONDS);
        this.ipInitialized = ipInitialized;
        if (callback.getIpAddress() != null) {
            // If there is already an IP address configured, we don't have to wait to get it
            ipInitialized.complete(true);
        }
        return ipInitialized;
    }

    private void getAbilities() throws MqttException, InterruptedException {
        byte[] message = MqttMessageBuilder.buildMqttMessage("GET", MerossEnum.Namespace.SYSTEM_ABILITY.value(),
                deviceUUID, Collections.emptyMap());
        publishMessage(deviceRequestTopic, message);
    }

    /**
     * @param deviceChannel The device channel
     * @param commandNamespace The command type
     * @param commandMode The command Mode
     * @throws MqttException
     * @throws InterruptedException
     */
    public void sendCommand(int deviceChannel, Namespace commandNamespace, String commandMode)
            throws MqttException, InterruptedException {
        ModeFactory modeFactory = TypeFactory.getFactory(commandNamespace);

        if (!abilities.isEmpty()) {
            if (!abilities.contains(commandNamespace.value())) {
                logger.debug("Ability {} not supported", commandNamespace.value());
                return;
            }
        }

        Command command = modeFactory.commandMode(commandMode, deviceChannel);
        byte[] commandMessage = command.command(deviceUUID);

        publishMessage(deviceRequestTopic, commandMessage);
    }

    /**
     * @param namespace
     * @throws MqttException
     * @throws InterruptedException
     */
    public void refresh(Namespace namespace) throws MqttException, InterruptedException {
        byte[] message = MqttMessageBuilder.buildMqttMessage("GET", namespace.value(), deviceUUID,
                Collections.emptyMap());
        publishMessage(deviceRequestTopic, message);
    }

    @Override
    public synchronized void processMessage(String topic, byte[] message) {
        try {
            String mqttPayload = new String(message, StandardCharsets.UTF_8);
            logger.trace("Processing topic {}, message {}", ContentAnonymizer.anonymizeTopic(topic),
                    ContentAnonymizer.anonymizeMessage(mqttPayload));
            JsonObject jsonObject = JsonParser.parseString(mqttPayload).getAsJsonObject();

            String method = null;
            Namespace namespace = null;
            if (jsonObject.has("header") && !jsonObject.get("header").isJsonNull()) {
                JsonObject header = jsonObject.getAsJsonObject("header");
                if (header.has("method") && header.get("method").isJsonPrimitive() && header.has("namespace")
                        && header.get("namespace").isJsonPrimitive()) {
                    method = header.get("method").getAsString();
                    String namespaceString = header.get("namespace").getAsString();
                    namespace = Namespace.getNamespaceByAbilityValue(namespaceString);
                }
            }
            if (method == null || namespace == null) {
                return;
            }

            JsonObject payload;
            if (jsonObject.has("payload") && !jsonObject.get("payload").isJsonNull()) {
                payload = jsonObject.getAsJsonObject("payload");
            } else {
                return;
            }

            if ("GETACK".equals(method)) {
                switch (namespace) {
                    case Namespace.SYSTEM_ALL:
                        setResponse(mqttPayload, namespace);
                        JsonObject all;
                        if (payload.has("all") && !payload.get("all").isJsonNull()) {
                            all = payload.getAsJsonObject("all");
                        } else {
                            return;
                        }
                        if (all.has("system") && !all.get("system").isJsonNull()) {
                            JsonObject system = all.getAsJsonObject("system");

                            // Get the local ip address of the device if available, so we can use local http calls
                            if (system.has("firmware") && !system.get("firmware").isJsonNull()) {
                                JsonObject firmware = system.getAsJsonObject("firmware");
                                if (firmware.has("innerIp") && firmware.get("innerIp").isJsonPrimitive()) {
                                    String innerIp = firmware.get("innerIp").getAsString();
                                    callback.setIpAddress(innerIp);
                                    setHttpConnector();
                                    CompletableFuture<Boolean> initialized = this.ipInitialized;
                                    if (initialized != null) {
                                        initialized.complete(true);
                                    }
                                }
                            }
                            if (system.has("online") && !system.get("online").isJsonNull()) {
                                JsonObject online = system.getAsJsonObject("online");
                                if (online.has("status") && online.get("status").isJsonPrimitive()) {
                                    int status = online.get("status").getAsInt();
                                    callback.setThingStatusFromMerossStatus(status);
                                }
                            }
                        }

                        if (all.has("digest") && !all.get("digest").isJsonNull()) {
                            JsonObject digest = all.getAsJsonObject("digest");
                            if (digest.has("togglex") && !digest.get("togglex").isJsonNull()) {
                                processUpdateMessage(Namespace.CONTROL_TOGGLEX, digest.get("togglex"));
                            }
                            if (digest.has("garageDoor") && !digest.get("garageDoor").isJsonNull()) {
                                processUpdateMessage(Namespace.GARAGE_DOOR_STATE, digest.get("garageDoor"));
                            }
                        }
                        break;
                    case Namespace.SYSTEM_ABILITY:
                        setResponse(mqttPayload, namespace);
                        if (payload.has("ability") && !payload.get("ability").isJsonNull()) {
                            JsonElement ability = payload.get("ability").getAsJsonObject();
                            Map<String, Object> abilities = GSON.fromJson(ability, ABILITIES_TYPE);
                            this.abilities = abilities != null ? abilities.keySet() : Collections.emptySet();
                        }
                        break;
                    default:
                        logger.debug("Ability {} not implemented", namespace.value());
                        break;
                }
            } else if ("SETACK".equals(method) || "PUSH".equals(method)) {
                JsonElement update = switch (namespace) {
                    case Namespace.CONTROL_TOGGLEX -> payload.get("togglex");
                    case Namespace.GARAGE_DOOR_STATE -> payload.get("state");
                    default -> null;
                };
                if (update == null) {
                    logger.debug("Ability {} not implemented", namespace.value());
                    return;
                }
                processUpdateMessage(namespace, update);
            } else {
                logger.debug("Processing method {} not implemented", method);
            }
        } catch (JsonSyntaxException | IllegalStateException | UnsupportedOperationException | ClassCastException e) {
            logger.debug("Error parsing response: ", e);
        }
    }

    private void processUpdateMessage(Namespace namespace, JsonElement update) {
        if (update.isJsonObject()) {
            processUpdateMessageElement(namespace, update);
        } else if (update.isJsonArray()) {
            update.getAsJsonArray().forEach(entry -> {
                processUpdateMessageElement(namespace, entry);
            });
        }
    }

    private void processUpdateMessageElement(Namespace namespace, JsonElement updateElement) {
        ModeFactory modeFactory = TypeFactory.getFactory(namespace);
        int merossState;
        int deviceChannel = 0;
        JsonObject update = updateElement.getAsJsonObject();
        switch (namespace) {
            case Namespace.CONTROL_TOGGLEX:
                if (update.has("onoff") && update.get("onoff").isJsonPrimitive()) {
                    merossState = update.get("onoff").getAsInt();
                    if (update.has("channel") && update.get("channel").isJsonPrimitive()) {
                        deviceChannel = update.get("channel").getAsInt();
                    }
                } else {
                    return;
                }
                break;
            case Namespace.GARAGE_DOOR_STATE:
                if (update.has("open") && update.get("open").isJsonPrimitive()) {
                    merossState = update.get("open").getAsInt();
                    if (update.has("channel") && update.get("channel").isJsonPrimitive()) {
                        deviceChannel = update.get("channel").getAsInt();
                    }
                } else {
                    return;
                }
                break;
            default:
                logger.debug("Ability {} not implemented", namespace.value());
                return;
        }
        callback.updateState(namespace, deviceChannel, modeFactory.state(merossState));
    }

    /**
     * Method to be used in command extension to get device specifications.
     *
     * @return JSON with device specifications
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws MqttException
     */
    public String getDeviceSpecsCommand()
            throws InterruptedException, ExecutionException, TimeoutException, MqttException {
        setHttpConnector();
        String systemAll = getSystemAllCommand();
        String abilities = getAbilitiesCommand();
        JsonObject wrapper = new JsonObject();
        if (systemAll != null) {
            wrapper.add("systemAll", JsonParser.parseString(systemAll));
        }
        if (abilities != null) {
            wrapper.add("abilities", JsonParser.parseString(abilities));
        }
        return wrapper.toString();
    }

    private @Nullable String getSystemAllCommand()
            throws InterruptedException, ExecutionException, TimeoutException, MqttException {
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        if (httpConnector == null) {
            setHttpConnector();
        }
        this.responseFuture = responseFuture;
        getSystemAll();
        return responseFuture.get(FUTURE_TIMOUT_SEC, TimeUnit.SECONDS);
    }

    private @Nullable String getAbilitiesCommand()
            throws InterruptedException, ExecutionException, TimeoutException, MqttException {
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        if (httpConnector == null) {
            setHttpConnector();
        }
        this.responseFuture = responseFuture;
        getAbilities();
        return responseFuture.get(FUTURE_TIMOUT_SEC, TimeUnit.SECONDS);
    }

    private void setResponse(String message, Namespace namespace) {
        CompletableFuture<String> responseFuture = this.responseFuture;
        if (responseFuture != null && !responseFuture.isDone()) {
            responseFuture.complete(message);
        }
    }
}
