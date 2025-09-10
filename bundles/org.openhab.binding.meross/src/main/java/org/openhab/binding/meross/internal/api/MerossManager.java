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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MerossEnum.Namespace;
import org.openhab.binding.meross.internal.command.Command;
import org.openhab.binding.meross.internal.dto.MqttMessageBuilder;
import org.openhab.binding.meross.internal.factory.ModeFactory;
import org.openhab.binding.meross.internal.factory.TypeFactory;
import org.openhab.binding.meross.internal.handler.MerossDeviceHandler;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MerossManager} class is responsible for implementing general functionalities to interact with
 * appliances
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Use mqtt transport
 */
@NonNullByDefault
public class MerossManager implements MqttMessageSubscriber {
    private final Logger logger = LoggerFactory.getLogger(MerossManager.class);

    private MerossMqttConnector mqttConnector;
    private String deviceUUID;
    private MerossDeviceHandler callback;
    private @Nullable InetAddress ipAddress;

    private String deviceRequestTopic;

    private Set<String> abilities = Set.of();

    public MerossManager(MerossMqttConnector mqttConnector, String deviceUUID, MerossDeviceHandler callback) {
        this.deviceUUID = deviceUUID;
        this.deviceRequestTopic = MqttMessageBuilder.buildDeviceRequestTopic(deviceUUID);
        this.callback = callback;
        this.mqttConnector = mqttConnector;
        mqttConnector.addDeviceRequestTopicSubscriber(this, deviceUUID);
    }

    public void dispose() {
        mqttConnector.removeDeviceRequestTopicSubscriber(this, deviceUUID);
    }

    public void initialize() throws MqttException {
        getSystemAll();
        getAbilities();
    }

    private void getSystemAll() throws MqttException {
        byte[] systemAllMessage = MqttMessageBuilder.buildMqttMessage("GET", MerossEnum.Namespace.SYSTEM_ALL.value(),
                deviceUUID, Collections.emptyMap());
        mqttConnector.publishMqttMessage(deviceRequestTopic, systemAllMessage);
    }

    private void getAbilities() throws MqttException {
        byte[] systemAbilityMessage = MqttMessageBuilder.buildMqttMessage("GET",
                MerossEnum.Namespace.SYSTEM_ABILITY.value(), deviceUUID, Collections.emptyMap());
        mqttConnector.publishMqttMessage(deviceRequestTopic, systemAbilityMessage);
    }

    /**
     * @param deviceChannel The device channel
     * @param commandNamespace The command type
     * @param commandMode The command Mode
     * @throws MqttException
     */

    public void sendCommand(int deviceChannel, Namespace commandNamespace, String commandMode) throws MqttException {
        ModeFactory modeFactory = TypeFactory.getFactory(commandNamespace);

        if (!abilities.isEmpty()) {
            if (!abilities.contains(commandNamespace.value())) {
                logger.debug("Ability {} not supported", commandNamespace.value());
                return;
            }
        }

        Command command = modeFactory.commandMode(commandMode, deviceChannel);
        byte[] commandMessage = command.command(deviceUUID);

        mqttConnector.publishMqttMessage(deviceRequestTopic, commandMessage);
    }

    @Override
    public void processMessage(String topic, byte[] mqttPayload) {
        String mqttPayloadString = new String(mqttPayload, StandardCharsets.UTF_8);
        JsonObject jsonObject = JsonParser.parseString(mqttPayloadString).getAsJsonObject();

        String method = null;
        Namespace namespace = null;
        if (jsonObject.has("header") && !jsonObject.get("header").isJsonNull()) {
            JsonObject header = jsonObject.getAsJsonObject("header");
            if (header.has("method") && !header.get("method").isJsonNull() && header.has("namespace")
                    && !header.get("namespace").isJsonNull()) {
                method = header.getAsJsonObject("method").getAsString();
                String ability = header.getAsJsonObject("namespace").getAsString();
                namespace = Namespace.getNamespaceByAbilityValue(ability);
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
                            if (firmware.has("innerIp") && !firmware.get("innerIp").isJsonNull()) {
                                String ipString = firmware.get("innerIp").getAsString();
                                try {
                                    ipAddress = InetAddress.getByName(ipString);
                                } catch (UnknownHostException e) {
                                    ipAddress = null;
                                }
                            }
                        }
                        if (system.has("online") && !system.get("online").isJsonNull()) {
                            JsonObject online = system.getAsJsonObject("online");
                            if (online.has("status") && !system.get("status").isJsonNull()) {
                                int status = system.get("status").getAsInt();
                                callback.setTingStatusFromMerossStatus(status);
                            }
                        }
                    }

                    if (all.has("digest") && !all.get("digest").isJsonNull()) {
                        JsonObject digest = all.getAsJsonObject("digest");
                        if (digest.has("togglex") && !digest.get("togglex").isJsonNull()) {
                            JsonArray entries = digest.getAsJsonArray("togglex");
                            entries.forEach(entry -> {
                                processUpdateMessage(Namespace.CONTROL_TOGGLEX, entry.getAsJsonObject());
                            });
                        }
                        if (digest.has("garageDoor") && !digest.get("garageDoor").isJsonNull()) {
                            JsonArray entries = digest.getAsJsonArray("garageDoor");
                            entries.forEach(entry -> {
                                processUpdateMessage(Namespace.GARAGE_DOOR_STATE, entry.getAsJsonObject());
                            });
                        }
                    }
                    break;
                case Namespace.SYSTEM_ABILITY:
                    if (payload.has("ability") && !payload.get("ability").isJsonNull()) {
                        JsonElement ability = payload.get("ability");
                        TypeToken<Map<String, Map<String, String>>> type = new TypeToken<>() {
                        };
                        Map<String, Map<String, String>> abilities = new Gson().fromJson(ability, type);
                        this.abilities = abilities.keySet();
                    }
                    break;
                default:
                    logger.debug("Ability {} not implemented", namespace.value());
                    break;
            }
        } else if ("SETACK".equals(method)) {
            JsonObject update = switch (namespace) {
                case Namespace.CONTROL_TOGGLEX -> payload.get("togglex").getAsJsonObject();
                case Namespace.GARAGE_DOOR_STATE -> payload.get("state").getAsJsonObject();
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
    }

    private void processUpdateMessage(Namespace namespace, JsonObject update) {
        ModeFactory modeFactory = TypeFactory.getFactory(namespace);
        int merossState;
        int deviceChannel = 0;
        switch (namespace) {
            case Namespace.CONTROL_TOGGLEX:
                if (update.has("onoff") && !update.get("onoff").isJsonNull()) {
                    merossState = update.get("onoff").getAsInt();
                    if (update.has("channel") && !update.get("channel").isJsonNull()) {
                        deviceChannel = update.get("channel").getAsInt();
                    }
                } else {
                    return;
                }
                break;
            case Namespace.GARAGE_DOOR_STATE:
                if (update.has("open") && !update.get("open").isJsonNull()) {
                    merossState = update.get("open").getAsInt();
                    if (update.has("channel") && !update.get("channel").isJsonNull()) {
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
        callback.updateState(deviceChannel, modeFactory.state(merossState));
    }
}
