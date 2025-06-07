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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.command.Command;
import org.openhab.binding.meross.internal.dto.MqttMessageBuilder;
import org.openhab.binding.meross.internal.exception.MerossMqttConnackException;
import org.openhab.binding.meross.internal.factory.ModeFactory;
import org.openhab.binding.meross.internal.factory.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MerossManager} class is responsible for implementing general functionalities to interact with
 * appliances
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossManager {
    private final Logger logger = LoggerFactory.getLogger(MerossManager.class);

    public static MerossManager newMerossManager(MerossHttpConnector merossHttpConnector) {
        return new MerossManager(merossHttpConnector);
    }

    final MerossHttpConnector merossHttpConnector;

    private MerossManager(MerossHttpConnector merossHttpConnector) {
        this.merossHttpConnector = merossHttpConnector;
    }

    /**
     * Initializes the mqtt connector with proper cloud credentials
     *
     */
    public void initializeMerossMqttConnector() {
        final var credentials = merossHttpConnector.readCredentials();
        String clientId = MqttMessageBuilder.buildClientId();
        MqttMessageBuilder.setClientId(clientId);
        if (credentials == null) {
            logger.debug("No credentials found");
        } else {
            String userId = credentials.userId();
            MqttMessageBuilder.setUserId(userId);
            String key = credentials.key();
            MqttMessageBuilder.setKey(key);
            String brokerAddress = credentials.mqttDomain();
            MqttMessageBuilder.setBrokerAddress(brokerAddress);
        }
    }

    /**
     * @param deviceName The device name
     * @param commandType The command type
     * @param commandMode The command Mode
     */

    public void sendCommand(String deviceName, String commandType, String commandMode)
            throws IOException, MerossMqttConnackException {
        String uuid = merossHttpConnector.getDevUUIDByDevName(deviceName);
        if (uuid.isEmpty()) {
            return;
        }
        initializeMerossMqttConnector();
        String deviceUUID = merossHttpConnector.getDevUUIDByDevName(deviceName);
        MqttMessageBuilder.setDestinationDeviceUUID(deviceUUID);
        String requestTopic = MqttMessageBuilder.buildDeviceRequestTopic(deviceUUID);
        ModeFactory modeFactory = TypeFactory.getFactory(commandType);
        Command command = modeFactory.commandMode(commandMode);
        byte[] commandMessage = command.commandType(commandType);
        var abilities = getAbilities(deviceUUID);
        if (abilities != null && abilities.isEmpty()) {
            if (!abilities.contains(MerossEnum.Namespace.getAbilityValueByName(commandType))) {
                logger.debug("Command {} not supported", commandType);
                return;
            }
        }
        MerossMqttConnector.publishMqttMessage(commandMessage, requestTopic);
    }

    public int onlineStatus(String deviceName) throws IOException, MerossMqttConnackException {
        String systemAll = getSystemAll(deviceName);
        if (!systemAll.isEmpty()) {
            JsonObject jsonObject = JsonParser.parseString(systemAll).getAsJsonObject();
            if (jsonObject.has("payload") && !jsonObject.get("payload").isJsonNull()) {
                JsonObject payloadObject = jsonObject.get("payload").getAsJsonObject();
                if (payloadObject.has("all") && !payloadObject.get("all").isJsonNull()) {
                    JsonObject allObject = payloadObject.get("all").getAsJsonObject();
                    if (allObject.has("system") && !allObject.get("system").isJsonNull()) {
                        JsonObject systemObject = allObject.get("system").getAsJsonObject();
                        if (systemObject.has("online") && !systemObject.get("online").isJsonNull()) {
                            JsonObject onlineObject = systemObject.get("online").getAsJsonObject();
                            if (!onlineObject.isEmpty()) {
                                Optional<JsonElement> jsonElement = Optional.of(onlineObject.get(("status")));
                                return jsonElement.get().getAsInt();
                            } else {
                                logger.debug("Online status missing or null");
                            }
                        }
                    }
                }
            }
        }
        return 4;
    }

    public String getSystemAll(String deviceName) throws IOException, MerossMqttConnackException {
        initializeMerossMqttConnector();
        String uuid = merossHttpConnector.getDevUUIDByDevName(deviceName);
        String requestTopic = MqttMessageBuilder.buildDeviceRequestTopic(uuid);
        byte[] systemAllMessage = MqttMessageBuilder.buildMqttMessage("GET", MerossEnum.Namespace.SYSTEM_ALL.value(),
                Collections.emptyMap());
        String mqttResponse = MerossMqttConnector.publishMqttMessage(systemAllMessage, requestTopic);
        if (!mqttResponse.isEmpty()) {
            return mqttResponse;
        }
        return "";
    }

    @Nullable
    public HashSet<String> getAbilities(String deviceName) throws IOException, MerossMqttConnackException {
        initializeMerossMqttConnector();
        String uuid = merossHttpConnector.getDevUUIDByDevName(deviceName);
        if (!uuid.isEmpty()) {
            String requestTopic = MqttMessageBuilder.buildDeviceRequestTopic(uuid);
            byte[] systemAbilityMessage = MqttMessageBuilder.buildMqttMessage("GET",
                    MerossEnum.Namespace.SYSTEM_ABILITY.value(), Collections.emptyMap());
            JsonObject jsonObject;
            String mqttResponse = MerossMqttConnector.publishMqttMessage(systemAbilityMessage, requestTopic);
            if (!mqttResponse.isEmpty()) {
                jsonObject = JsonParser.parseString(mqttResponse).getAsJsonObject();
                if (jsonObject.has("payload") && !jsonObject.get("payload").isJsonNull()) {
                    JsonObject payloadObject = jsonObject.getAsJsonObject("payload");
                    if (payloadObject.has("ability") && !payloadObject.get("ability").isJsonNull()) {
                        JsonElement abilityObject = payloadObject.get("ability");
                        if (!abilityObject.isJsonNull()) {
                            Optional<JsonElement> ability = Optional.of(abilityObject.getAsJsonObject());
                            TypeToken<HashMap<String, HashMap<String, String>>> type = new TypeToken<>() {
                            };
                            HashMap<String, HashMap<String, String>> abilities = new Gson().fromJson(ability.get(),
                                    type);
                            return new HashSet<>(abilities.keySet());
                        } else {
                            logger.debug("Abilities missing or null");
                        }
                    }
                }
            }
        }
        return null;
    }
}
