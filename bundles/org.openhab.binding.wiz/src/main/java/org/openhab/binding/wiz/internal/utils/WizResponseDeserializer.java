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
package org.openhab.binding.wiz.internal.utils;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wiz.internal.entities.ErrorResponseResult;
import org.openhab.binding.wiz.internal.entities.ModelConfigResult;
import org.openhab.binding.wiz.internal.entities.SystemConfigResult;
import org.openhab.binding.wiz.internal.entities.WizResponse;
import org.openhab.binding.wiz.internal.entities.WizSyncState;
import org.openhab.binding.wiz.internal.enums.WizMethodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializes incoming json
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public class WizResponseDeserializer implements JsonDeserializer<WizResponse> {
    private final Logger logger = LoggerFactory.getLogger(WizResponseDeserializer.class);

    @Override
    @Nullable
    public WizResponse deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        // The outgoing response
        WizResponse deserializedResponse = new WizResponse();

        // The incoming JSON
        JsonObject jobject;
        if (json == null) {
            logger.trace("No json provided to parse.");
        } else if (context == null) {
            logger.trace("No context available for parsing sub-objects.");
        } else {
            jobject = json.getAsJsonObject();

            // Parse the ID
            if (jobject.has("id")) {
                deserializedResponse.setId(jobject.get("id").getAsInt());
            }
            // Parse the environment - I think this is always sent, but I'm checking anyway
            if (jobject.has("env")) {
                deserializedResponse.setEnv(jobject.get("env").getAsString());
            }

            // Check if the response contains an error
            // Return without completing parsing if there's an error
            if (jobject.has("error")) {
                ErrorResponseResult error = context.deserialize(jobject.getAsJsonObject("error"),
                        ErrorResponseResult.class);
                deserializedResponse.setError(error);
                if (jobject.has("method")) {
                    logger.debug("Bulb returned an error on method {}:  {}, {}", jobject.get("method"), error.code,
                            error.message);
                } else {
                    logger.debug("Bulb returned an error:  {}", error.code);
                }
                return deserializedResponse;
            }

            // Parse the method. We will use the method to decide how to continue to parse
            // Bail out of everything if we cannot understand the method.
            WizMethodType method;
            if (jobject.has("method")) {
                try {
                    String inMethod = jobject.get("method").getAsString();
                    String properCaseMethod = inMethod.substring(0, 1).toUpperCase() + inMethod.substring(1);
                    method = WizMethodType.valueOf(properCaseMethod);
                    deserializedResponse.setMethod(method);
                } catch (IllegalArgumentException e) {
                    logger.debug("Bulb returned an invalid method: {}", jobject.get("method"));
                    return deserializedResponse;
                }
            } else {
                throw new JsonParseException("Incoming message did not contain a method and cannot be parsed!");
            }

            switch (method) {
                case Registration:
                    // {"method": "registration", "id": 1, "env": "pro", "result": {"mac":
                    // "macOfopenHAB", "success": true}}
                    if (!jobject.has("result")) {
                        throw new JsonParseException("registration received, but no result object present");
                    }
                    JsonObject registrationResult = jobject.getAsJsonObject("result");
                    if (!registrationResult.has("mac")) {
                        throw new JsonParseException("registration received, but no MAC address present");
                    }
                    String mac = registrationResult.get("mac").getAsString();
                    deserializedResponse.setWizResponseMacAddress(mac);
                    deserializedResponse.setResultSucess(registrationResult.get("success").getAsBoolean());
                    logger.trace("Registration result deserialized with mac {} and success {}", mac,
                            registrationResult.get("success").getAsBoolean());
                    break;

                case Pulse:
                    // {"method":"pulse","id":22,"env":"pro","result":{"success":true}}
                case SetPilot:
                    // {"method":"setPilot","id":24,"env":"pro","result":{"success":true}}
                    if (!jobject.has("result")) {
                        throw new JsonParseException("pulse or setPilot method received, but no result object present");
                    }
                    JsonObject setResult = jobject.getAsJsonObject("result");
                    deserializedResponse.setResultSucess(setResult.get("success").getAsBoolean());
                    logger.trace("Result deserialized - command success {}", setResult.get("success").getAsBoolean());
                    break;

                case FirstBeat:
                    // {"method": "firstBeat", "id": 0, "env": "pro", "params": {"mac": "theBulbMacAddress",
                    // "homeId": xxxxxx, "fwVersion": "1.15.2"}}
                    if (!jobject.has("params")) {
                        throw new JsonParseException("firstBeat received, but no params object present");
                    }
                    SystemConfigResult parsedFBParams = context.deserialize(jobject.getAsJsonObject("params"),
                            SystemConfigResult.class);
                    if (parsedFBParams.mac.isEmpty()) {
                        throw new JsonParseException("firstBeat received, but no MAC address present");
                    }
                    deserializedResponse.setWizResponseMacAddress(parsedFBParams.mac);
                    deserializedResponse.setResultSucess(true);
                    deserializedResponse.setSystemConfigResult(parsedFBParams);
                    logger.trace("firstBeat result deserialized with mac {}", parsedFBParams.mac);
                    break;

                case GetModelConfig:
                    if (!jobject.has("result")) {
                        throw new JsonParseException("getModelConfig received, but no result object present");
                    }
                    ModelConfigResult parsedMResult = context.deserialize(jobject.getAsJsonObject("result"),
                            ModelConfigResult.class);
                    deserializedResponse.setResultSucess(true);
                    deserializedResponse.setModelConfigResult(parsedMResult);
                    break;

                case GetSystemConfig:
                    // {"method": "getSystemConfig", "id": 22, "env": "pro",
                    // "result": {"mac": "theBulbMacAddress", "homeId": xxxxxx, "roomId": xxxxxx,
                    // "homeLock": false, "pairingLock": false, "typeId": 0, "moduleName":
                    // "ESP01_SHRGB1C_31", "fwVersion": "1.15.2", "groupId": 0, "drvConf":[33,1]}}
                    if (!jobject.has("result")) {
                        throw new JsonParseException("getSystemConfig received, but no result object present");
                    }
                    SystemConfigResult parsedCResult = context.deserialize(jobject.getAsJsonObject("result"),
                            SystemConfigResult.class);
                    if (parsedCResult.mac.isEmpty()) {
                        throw new JsonParseException("getSystemConfig received, but no MAC address present");
                    }
                    deserializedResponse.setWizResponseMacAddress(parsedCResult.mac);
                    deserializedResponse.setResultSucess(true);
                    deserializedResponse.setSystemConfigResult(parsedCResult);
                    logger.trace("systemConfig result deserialized with mac {}", parsedCResult.mac);
                    break;

                case GetPilot:
                    // {"method": "getPilot", "id": 22, "env": "pro", "result": {"mac":
                    // "theBulbMacAddress", "rssi":-76, "state": true, "sceneId": 0, "temp": 2700,
                    // "dimming": 42, "schdPsetId": 5}}
                    if (!jobject.has("result")) {
                        throw new JsonParseException("getPilot received, but no result object present");
                    }
                    WizSyncState parsedPResult = context.deserialize(jobject.getAsJsonObject("result"),
                            WizSyncState.class);
                    if (parsedPResult.mac.isEmpty()) {
                        throw new JsonParseException("getPilot received, but no MAC address present");
                    }
                    deserializedResponse.setWizResponseMacAddress(parsedPResult.mac);
                    deserializedResponse.setResultSucess(true);
                    deserializedResponse.setSyncParams(parsedPResult);
                    logger.trace("getPilot result deserialized with mac {}", parsedPResult.mac);
                    break;

                case SyncPilot:
                    // {"method": "syncPilot", "id": 219, "env": "pro", "params": { "mac":
                    // "theBulbMacAddress", "rssi": -72, "src": "hb", "mqttCd": 0, "state": true, "sceneId":
                    // 0, "temp": 3362, "dimming": 69, "schdPsetId": 5}}
                    if (!jobject.has("params")) {
                        throw new JsonParseException("syncPilot received, but no params object present");
                    }
                    WizSyncState parsedPParam = context.deserialize(jobject.getAsJsonObject("params"),
                            WizSyncState.class);
                    if (parsedPParam.mac.isEmpty()) {
                        throw new JsonParseException("syncPilot received, but no MAC address present");
                    }
                    deserializedResponse.setWizResponseMacAddress(parsedPParam.mac);
                    deserializedResponse.setResultSucess(true);
                    deserializedResponse.setSyncParams(parsedPParam);
                    logger.trace("syncPilot result deserialized with mac {}", parsedPParam.mac);
                    break;

                case SetSystemConfig:
                    // ?? I'm not trying this at home!
                case SetWifiConfig:
                    // ?? I'm not trying this at home!
                case GetWifiConfig:
                    // The returns an encrypted string and I'm not using it so I'm not bothering to parse it
                    // {"method":"getWifiConfig","id":22,"env":"pro","result":{:["longStringInEncryptedUnicode"]}}
                case UnknownMethod:
                    // This should just never happen
                    break;
            }
        }

        return deserializedResponse;
    }
}
