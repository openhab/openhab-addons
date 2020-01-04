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
package org.openhab.binding.wizlighting.internal.utils;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wizlighting.internal.entities.ErrorResponseResult;
import org.openhab.binding.wizlighting.internal.entities.SyncResponseParam;
import org.openhab.binding.wizlighting.internal.entities.SystemConfigResult;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;
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
public class WizResponseDeserializer implements JsonDeserializer<WizLightingResponse> {
    // We can't do too much logging, can we?
    private final Logger logger = LoggerFactory.getLogger(WizResponseDeserializer.class);

    @Override
    public WizLightingResponse deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        // The outgoing response
        WizLightingResponse deserializedResponse = new WizLightingResponse();

        // The incoming JSON
        JsonObject jobject;
        if (json == null) {
            logger.trace("No json provided to parse.");
        } else if (context == null) {
            logger.trace("No context available for parsing sub-objects.");
        } else {
            jobject = json.getAsJsonObject();

            // Parse the ID
            deserializedResponse.setId(jobject.get("id").getAsInt());
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
                logger.warn("Bulb returned an error on method {}:  {}, {}", jobject.get("method"), error.code,
                        error.message);
                return deserializedResponse;
            }

            // Parse the method. We will use the method to decide how to continue to parse
            // Bail out of everything if we cannot understand the method.
            WizLightingMethodType method;
            if (jobject.has("method")) {
                try {
                    String inMethod = jobject.get("method").getAsString();
                    method = WizLightingMethodType.valueOf(inMethod);
                    deserializedResponse.setMethod(method);
                } catch (IllegalArgumentException e) {
                    logger.warn("Bulb returned an invalid method: {}", jobject.get("method"));
                    return deserializedResponse;
                }
            } else {
                throw new JsonParseException("Incoming message did not contain a method and cannot be parsed!");
            }

            switch (method) {
                case registration:
                    // {"method": "registration", "id": 1, "env": "pro", "result": {"mac":
                    // "macOfOpenHab", "success": true}}
                    JsonObject registrationResult = jobject.getAsJsonObject("result");
                    @Nullable
                    String mac = registrationResult.get("mac").getAsString();
                    deserializedResponse.setWizResponseMacAddress(mac);
                    deserializedResponse.setResultSucess(registrationResult.get("success").getAsBoolean());
                    break;

                case pulse:
                    // {"method":"pulse","id":22,"env":"pro","result":{"success":true}}
                case setPilot:
                    // {"method":"setPilot","id":24,"env":"pro","result":{"success":true}}
                    JsonObject setResult = jobject.getAsJsonObject("result");
                    deserializedResponse.setResultSucess(setResult.get("success").getAsBoolean());
                    break;

                case firstBeat:
                    // {"method": "firstBeat", "id": 0, "env": "pro", "params": {"mac": "bulbMac",
                    // "homeId": xxxxxx, "fwVersion": "1.15.2"}}
                case getSystemConfig:
                    // {"method": "getSystemConfig", "id": 22, "env": "pro",
                    // "result": {"mac": "bulbMac", "homeId": xxxxxx, "roomId": xxxxxx,
                    // "homeLock": false, "pairingLock": false, "typeId": 0, "moduleName":
                    // "ESP01_SHRGB1C_31", "fwVersion": "1.15.2", "groupId": 0, "drvConf":[33,1]}}
                    SystemConfigResult parsedCResult = context.deserialize(jobject.getAsJsonObject("result"),
                            SystemConfigResult.class);
                    deserializedResponse.setResultSucess(true);
                    deserializedResponse.setWizResponseMacAddress(parsedCResult.mac);
                    deserializedResponse.setSystemConfigResult(parsedCResult);
                    break;

                case getPilot:
                    // {"method": "getPilot", "id": 22, "env": "pro", "result": {"mac":
                    // "a8bb508f570a", "rssi":-76, "state": true, "sceneId": 0, "temp": 2700,
                    // "dimming": 42, "schdPsetId": 5}}
                    SyncResponseParam parsedPResult = context.deserialize(jobject.getAsJsonObject("result"),
                            SyncResponseParam.class);
                    deserializedResponse.setResultSucess(true);
                    deserializedResponse.setWizResponseMacAddress(parsedPResult.mac);
                    deserializedResponse.setSyncParams(parsedPResult);
                    break;

                case syncPilot:
                    // {"method": "syncPilot", "id": 219, "env": "pro", "params": { "mac":
                    // "bulbMac", "rssi": -72, "src": "hb", "mqttCd": 0, "state": true, "sceneId":
                    // 0, "temp": 3362, "dimming": 69, "schdPsetId": 5}}
                    SyncResponseParam parsedPParam = context.deserialize(jobject.getAsJsonObject("param"),
                            SyncResponseParam.class);
                    deserializedResponse.setResultSucess(true);
                    deserializedResponse.setWizResponseMacAddress(parsedPParam.mac);
                    deserializedResponse.setSyncParams(parsedPParam);
                    break;

                case setSystemConfig:
                    // ?? I'm not trying this at home!
                case setWifiConfig:
                    // ?? I'm not trying this at home!
                case getWifiConfig:
                    // The returns an encrypted string and I'm not using it so I'm not bothering to parse it
                    // {"method":"getWifiConfig","id":22,"env":"pro","result":{:["longStringInEncryptedUnicode"]}}
                case unknownMethod:
                    // This should just never happen
                    break;
            }
        }

        return deserializedResponse;
    }
}
