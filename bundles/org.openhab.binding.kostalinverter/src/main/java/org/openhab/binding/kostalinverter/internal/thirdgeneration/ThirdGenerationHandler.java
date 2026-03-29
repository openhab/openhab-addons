/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.kostalinverter.internal.thirdgeneration;

import static org.openhab.binding.kostalinverter.internal.thirdgeneration.ThirdGenerationBindingConstants.*;
import static org.openhab.core.thing.ThingStatusDetail.OFFLINE;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ThirdGenerationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author René Stakemeier - Initial contribution
 */
@NonNullByDefault
public class ThirdGenerationHandler extends BaseThingHandler {

    /*
     * operations used for authentication
     */
    private static final String AUTH_START = "/auth/start";
    private static final String AUTH_FINISH = "/auth/finish";
    private static final String AUTH_CREATE_SESSION = "/auth/create_session";

    /*
     * operations used for gathering process data from the device
     */
    private static final String PROCESSDATA = "/processdata";

    /*
     * After the authentication the result (the session id) is stored here and used to "sign" future requests
     */
    private @Nullable String sessionId;
    /*
     * The configuration file containing the host, the password and the refresh interval
     */
    private @NonNullByDefault({}) ThirdGenerationConfiguration config;

    private @Nullable ScheduledFuture<?> refreshScheduler;

    private final HttpClient httpClient;
    private final ThirdGenerationInverterTypes inverterType;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Constructor of this class
     *
     * @param thing the thing
     * @param httpClient the httpClient used for communication
     * @param inverterType the type of the device
     */
    public ThirdGenerationHandler(Thing thing, HttpClient httpClient, ThirdGenerationInverterTypes inverterType) {
        super(thing);
        this.inverterType = inverterType;
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // All channels are readonly and updated by the scheduler
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> refreshScheduler = this.refreshScheduler;
        if (refreshScheduler != null) {
            refreshScheduler.cancel(true);
            this.refreshScheduler = null;
        }
        super.dispose();
    }

    @Override
    public void initialize() {
        config = getConfigAs(ThirdGenerationConfiguration.class);
        // temporary value while initializing
        updateStatus(ThingStatus.UNKNOWN);

        // Start the authentication
        scheduler.schedule(this::authenticate, 1, TimeUnit.SECONDS);

        // Start the update scheduler as configured
        refreshScheduler = scheduler.scheduleWithFixedDelay(this::updateChannelValues, 10,
                config.refreshInternalInSeconds, TimeUnit.SECONDS);
    }

    /**
     * The API supports the resolution of multiple values at a time
     *
     * Therefore this methods builds one request to gather all information for the current inverter.
     * The list contains all channels as defined in {@link ThirdGenerationMappingInverterToChannel} for the
     * current inverter
     *
     */
    private void updateChannelValues() {
        Map<String, List<ThirdGenerationChannelMappingToWebApi>> channelList = ThirdGenerationMappingInverterToChannel
                .getModuleToChannelsMappingForInverter(inverterType);
        Map<String, ThirdGenerationChannelMappingToWebApi> channelLookup = buildChannelLookup(channelList);
        JsonArray updateMessageJsonArray = getUpdateChannelMessage(channelList);

        // Send the API request to get values for all channels
        ContentResponse updateMessageContentResponse;
        try {
            updateMessageContentResponse = ThirdGenerationHttpHelper.executeHttpPost(httpClient, config.url,
                    PROCESSDATA, updateMessageJsonArray, sessionId);
            int statusCode = updateMessageContentResponse.getStatus();
            if (statusCode == HttpStatus.UNAUTHORIZED_401) {
                // session not valid (timed out? device rebooted?)
                logger.info("Session expired - performing retry");
                try {
                    authenticate();
                } catch (RuntimeException e) {
                    logger.debug("Re-authentication failed", e);
                    setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_AUTHENTICATION);
                    return;
                }
                // Retry
                updateMessageContentResponse = ThirdGenerationHttpHelper.executeHttpPost(httpClient, config.url,
                        PROCESSDATA, updateMessageJsonArray, sessionId);
                statusCode = updateMessageContentResponse.getStatus();
            }

            if (statusCode == HttpStatus.NOT_FOUND_404) {
                // Module not found
                setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_INCOMPATIBLE_DEVICE);
                return;
            }
            if (statusCode == HttpStatus.SERVICE_UNAVAILABLE_503) {
                // Communication error (e.g. during initial boot of the SCB)
                setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
                return;
            }
            if (statusCode != HttpStatus.OK_200) {
                logger.debug("Could not update values. Device returned status {}", statusCode);
                setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP_UNEXPECTED);
                return;
            }
        } catch (TimeoutException | ExecutionException e) {
            // Communication problem
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        }

        try {
            JsonArray updateMessageResultsJsonArray = ThirdGenerationHttpHelper
                    .getJsonArrayFromResponse(updateMessageContentResponse);

            if (updateMessageResultsJsonArray == null) {
                setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
                return;
            }

            // Map the returned values back to the channels and update them
            for (int i = 0; i < updateMessageResultsJsonArray.size(); i++) {
                JsonObject moduleAnswer = updateMessageResultsJsonArray.get(i).getAsJsonObject();
                String moduleName = moduleAnswer.get("moduleid").getAsString();
                JsonArray processdata = moduleAnswer.get("processdata").getAsJsonArray();
                for (int j = 0; j < processdata.size(); j++) {
                    // Update the channels with their new value
                    JsonObject newValueObject = processdata.get(j).getAsJsonObject();
                    String valueId = Objects.requireNonNull(newValueObject.get("id")).getAsString();
                    double valueAsDouble = newValueObject.get("value").getAsDouble();
                    ThirdGenerationChannelMappingToWebApi channel = Objects
                            .requireNonNull(channelLookup.get(getChannelLookupKey(moduleName, valueId)));
                    updateChannelValue(channel.channelUID, channel.dataType, valueAsDouble);
                }
            }
        } catch (RuntimeException e) {
            logger.debug("Could not parse or map update response", e);
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    private Map<String, ThirdGenerationChannelMappingToWebApi> buildChannelLookup(
            Map<String, List<ThirdGenerationChannelMappingToWebApi>> channelList) {
        Map<String, ThirdGenerationChannelMappingToWebApi> channelLookup = new HashMap<>();
        for (List<ThirdGenerationChannelMappingToWebApi> mappings : channelList.values()) {
            for (ThirdGenerationChannelMappingToWebApi mapping : mappings) {
                channelLookup.put(getChannelLookupKey(mapping.moduleId, mapping.processdataId), mapping);
            }
        }
        return channelLookup;
    }

    private String getChannelLookupKey(String moduleId, String processdataId) {
        return moduleId + "#" + processdataId;
    }

    /**
     * Update the channel to the given value.
     * The value is set to the matching data (SITypes etc)
     *
     * @param channeluid Channel to update
     * @param dataType target data type
     * @param value value
     */
    private void updateChannelValue(String channeluid, ThirdGenerationChannelDatatypes dataType, Double value) {
        switch (dataType) {
            case INTEGER: {
                updateState(channeluid, new DecimalType(value.longValue()));
                break;
            }
            case PERCEMTAGE: {
                updateState(channeluid, new QuantityType<>(value, Units.PERCENT));
                break;
            }
            case KILOGRAM: {
                updateState(channeluid, new QuantityType<>(value / 1000, SIUnits.KILOGRAM));
                break;
            }
            case SECONDS: {
                updateState(channeluid, new QuantityType<>(value, Units.SECOND));
                break;
            }
            case KILOWATT_HOUR: {
                updateState(channeluid, new QuantityType<>(value / 1000, Units.KILOWATT_HOUR));
                break;
            }
            case WATT: {
                updateState(channeluid, new QuantityType<>(value, Units.WATT));
                break;
            }
            case AMPERE: {
                updateState(channeluid, new QuantityType<>(value, Units.AMPERE));
                break;
            }
            case AMPERE_HOUR: {
                // Ampere hours is not a supported unit, but 1 AH is equal tp 3600 coulomb...
                updateState(channeluid, new QuantityType<>(value * 3600, Units.COULOMB));
                break;
            }
            case VOLT: {
                updateState(channeluid, new QuantityType<>(value, Units.VOLT));
                break;
            }
            case HERTZ: {
                updateState(channeluid, new QuantityType<>(value, Units.HERTZ));
                break;
            }
            default: {
                // unknown datatype
                logger.debug("{} not known!", dataType);
            }
        }
    }

    /**
     * Creates the message which has to be send to the inverter to gather the current informations for all channels
     *
     * @param channelList channels of this thing
     * @return the JSON array to send to the device
     */
    private JsonArray getUpdateChannelMessage(Map<String, List<ThirdGenerationChannelMappingToWebApi>> channelList) {
        // Build the message to send to the inverter
        JsonArray updateMessageJsonArray = new JsonArray();
        for (Entry<String, List<ThirdGenerationChannelMappingToWebApi>> moduleId : channelList.entrySet()) {
            JsonObject moduleJsonObject = new JsonObject();
            moduleJsonObject.addProperty("moduleid", moduleId.getKey());

            JsonArray processdataNames = new JsonArray();
            for (ThirdGenerationChannelMappingToWebApi processdata : Objects
                    .requireNonNull(channelList.get(moduleId.getKey()))) {
                processdataNames.add(processdata.processdataId);
            }
            moduleJsonObject.add("processdataids", processdataNames);
            updateMessageJsonArray.add(moduleJsonObject);
        }
        return updateMessageJsonArray;
    }

    private void setOffline(ThingStatusDetail detail, String message) {
        updateStatus(ThingStatus.OFFLINE, detail, message);
    }

    private void setOffline(ThingStatusDetail detail) {
        updateStatus(ThingStatus.OFFLINE, detail);
    }

    private @Nullable JsonObject getJsonObjectOrOffline(ContentResponse response) {
        JsonObject jsonObject;
        try {
            jsonObject = ThirdGenerationHttpHelper.getJsonObjectFromResponse(response);
        } catch (JsonSyntaxException e) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return null;
        }
        if (jsonObject == null) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return null;
        }
        return jsonObject;
    }

    private @Nullable String getRequiredStringOrOffline(JsonObject jsonObject, String fieldName) {
        try {
            return jsonObject.get(fieldName).getAsString();
        } catch (RuntimeException e) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return null;
        }
    }

    private @Nullable Integer getRequiredIntOrOffline(JsonObject jsonObject, String fieldName) {
        try {
            return jsonObject.get(fieldName).getAsInt();
        } catch (RuntimeException e) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return null;
        }
    }

    private byte[] getRequiredBase64OrOffline(JsonObject jsonObject, String fieldName) {
        try {
            return Base64.getDecoder().decode(jsonObject.get(fieldName).getAsString());
        } catch (RuntimeException e) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return new byte[0];
        }
    }

    /**
     * This function is used to authenticate against the SCB.
     * SCB uses PBKDF2 and AES256 GCM mode with a slightly modified authentication message.
     * The authentication will fail on JRE < 8u162. since the security policy is set to "limited" by default (see readme
     * for fix)
     */
    private final void authenticate() {
        String clientNonce = ThirdGenerationEncryptionHelper.createClientNonce();

        AuthenticationStartResult startResult = startAuthentication(clientNonce);
        if (startResult == null) {
            return;
        }

        AuthenticationFinishResult finishResult = finishAuthentication(startResult);
        if (finishResult == null) {
            return;
        }

        String createdSessionId = createSession(startResult.transactionId, finishResult.protocolKeyHmac,
                finishResult.token);
        if (createdSessionId == null) {
            return;
        }

        sessionId = createdSessionId;
        updateStatus(ThingStatus.ONLINE);
    }

    private @Nullable AuthenticationStartResult startAuthentication(String clientNonce) {
        JsonObject authMeJsonObject = new JsonObject();
        authMeJsonObject.addProperty("username", USER_TYPE);
        authMeJsonObject.addProperty("nonce", clientNonce);

        ContentResponse authStartResponseContentResponse;
        try {
            authStartResponseContentResponse = ThirdGenerationHttpHelper.executeHttpPost(httpClient, config.url,
                    AUTH_START, authMeJsonObject);

            int statusCode = authStartResponseContentResponse.getStatus();
            if (statusCode == HttpStatus.BAD_REQUEST_400) {
                setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_API_CHANGED);
                return null;
            }
            if (statusCode == HttpStatus.FORBIDDEN_403) {
                setOffline(OFFLINE.CONFIGURATION_ERROR, COMMUNICATION_ERROR_USER_ACCOUNT_LOCKED);
                return null;
            }
            if (statusCode == HttpStatus.SERVICE_UNAVAILABLE_503) {
                setOffline(OFFLINE.COMMUNICATION_ERROR);
                return null;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return null;
        }

        JsonObject authMeResponseJsonObject = getJsonObjectOrOffline(authStartResponseContentResponse);
        if (authMeResponseJsonObject == null) {
            return null;
        }

        String salt = getRequiredStringOrOffline(authMeResponseJsonObject, "salt");
        String serverNonce = getRequiredStringOrOffline(authMeResponseJsonObject, "nonce");
        Integer roundsObject = getRequiredIntOrOffline(authMeResponseJsonObject, "rounds");
        String transactionId = getRequiredStringOrOffline(authMeResponseJsonObject, "transactionId");
        if (salt == null || serverNonce == null || roundsObject == null || transactionId == null) {
            return null;
        }
        int rounds = roundsObject;

        byte[] saltedPasswort;
        byte[] clientKey;
        byte[] serverKey;
        byte[] storedKey;
        byte[] clientSignature;
        byte[] serverSignature;
        String authMessage;
        try {
            saltedPasswort = ThirdGenerationEncryptionHelper.getPBKDF2Hash(config.userPassword,
                    Base64.getDecoder().decode(salt), rounds);
            clientKey = ThirdGenerationEncryptionHelper.getHMACSha256(saltedPasswort, "Client Key");
            serverKey = ThirdGenerationEncryptionHelper.getHMACSha256(saltedPasswort, "Server Key");
            storedKey = ThirdGenerationEncryptionHelper.getSha256Hash(clientKey);
            authMessage = String.format("n=%s,r=%s,r=%s,s=%s,i=%d,c=biws,r=%s", USER_TYPE, clientNonce, serverNonce,
                    salt, rounds, serverNonce);
            clientSignature = ThirdGenerationEncryptionHelper.getHMACSha256(storedKey, authMessage);
            serverSignature = ThirdGenerationEncryptionHelper.getHMACSha256(serverKey, authMessage);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IllegalStateException
                | IllegalArgumentException e) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_AUTHENTICATION);
            return null;
        }

        String clientProof = ThirdGenerationEncryptionHelper.createClientProof(clientSignature, clientKey);
        return new AuthenticationStartResult(transactionId, clientProof, serverSignature, storedKey, clientKey,
                authMessage);
    }

    private @Nullable AuthenticationFinishResult finishAuthentication(AuthenticationStartResult startResult) {
        JsonObject authFinishJsonObject = new JsonObject();
        authFinishJsonObject.addProperty("transactionId", startResult.transactionId);
        authFinishJsonObject.addProperty("proof", startResult.clientProof);

        ContentResponse authFinishResponseContentResponse;
        try {
            authFinishResponseContentResponse = ThirdGenerationHttpHelper.executeHttpPost(httpClient, config.url,
                    AUTH_FINISH, authFinishJsonObject);
            if (authFinishResponseContentResponse.getStatus() == HttpStatus.BAD_REQUEST_400) {
                setOffline(OFFLINE.COMMUNICATION_ERROR, CONFIGURATION_ERROR_PASSWORD);
                return null;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e3) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return null;
        }

        JsonObject authFinishResponseJsonObject = getJsonObjectOrOffline(authFinishResponseContentResponse);
        if (authFinishResponseJsonObject == null) {
            return null;
        }

        byte[] signature = getRequiredBase64OrOffline(authFinishResponseJsonObject, "signature");
        String token = getRequiredStringOrOffline(authFinishResponseJsonObject, "token");
        if (signature.length == 0 || token == null) {
            return null;
        }

        if (!java.util.Arrays.equals(startResult.serverSignature, signature)) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_AUTHENTICATION);
            return null;
        }

        SecretKeySpec signingKey = new SecretKeySpec(startResult.storedKey, HMAC_SHA256_ALGORITHM);
        Mac mac;
        byte[] protocolKeyHmac;
        try {
            mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update("Session Key".getBytes());
            mac.update(startResult.authMessage.getBytes());
            mac.update(startResult.clientKey);
            protocolKeyHmac = mac.doFinal();
        } catch (NoSuchAlgorithmException | InvalidKeyException e1) {
            setOffline(OFFLINE.CONFIGURATION_ERROR, COMMUNICATION_ERROR_AUTHENTICATION);
            return null;
        }

        return new AuthenticationFinishResult(token, protocolKeyHmac);
    }

    private @Nullable String createSession(String transactionId, byte[] protocolKeyHmac, String token) {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        byte[] data;
        SecretKeySpec skeySpec = new SecretKeySpec(protocolKeyHmac, "AES");
        GCMParameterSpec param = new GCMParameterSpec(protocolKeyHmac.length * 8 - AES_GCM_TAG_LENGTH, iv);

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES_256/GCM/NOPADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, param);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException e1) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_AUTHENTICATION);
            return null;
        }
        try {
            data = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalBlockSizeException | BadPaddingException e1) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return null;
        }

        byte[] ciphertext = new byte[data.length - AES_GCM_TAG_LENGTH / 8];
        byte[] gcmTag = new byte[AES_GCM_TAG_LENGTH / 8];
        System.arraycopy(data, 0, ciphertext, 0, data.length - AES_GCM_TAG_LENGTH / 8);
        System.arraycopy(data, data.length - AES_GCM_TAG_LENGTH / 8, gcmTag, 0, AES_GCM_TAG_LENGTH / 8);

        JsonObject createSessionJsonObject = new JsonObject();
        createSessionJsonObject.addProperty("transactionId", transactionId);
        createSessionJsonObject.addProperty("iv", Base64.getEncoder().encodeToString(iv));
        createSessionJsonObject.addProperty("tag", Base64.getEncoder().encodeToString(gcmTag));
        createSessionJsonObject.addProperty("payload", Base64.getEncoder().encodeToString(ciphertext));

        ContentResponse createSessionResponseContentResponse;
        try {
            createSessionResponseContentResponse = ThirdGenerationHttpHelper.executeHttpPost(httpClient, config.url,
                    AUTH_CREATE_SESSION, createSessionJsonObject);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_AUTHENTICATION);
            return null;
        }

        if (createSessionResponseContentResponse.getStatus() == HttpStatus.BAD_REQUEST_400) {
            setOffline(OFFLINE.COMMUNICATION_ERROR, CONFIGURATION_ERROR_PASSWORD);
            return null;
        }

        JsonObject createSessionResponseJsonObject = getJsonObjectOrOffline(createSessionResponseContentResponse);
        if (createSessionResponseJsonObject == null) {
            return null;
        }

        return getRequiredStringOrOffline(createSessionResponseJsonObject, "sessionId");
    }

    private static final class AuthenticationStartResult {
        private final String transactionId;
        private final String clientProof;
        private final byte[] serverSignature;
        private final byte[] storedKey;
        private final byte[] clientKey;
        private final String authMessage;

        private AuthenticationStartResult(String transactionId, String clientProof, byte[] serverSignature,
                byte[] storedKey, byte[] clientKey, String authMessage) {
            this.transactionId = transactionId;
            this.clientProof = clientProof;
            this.serverSignature = serverSignature;
            this.storedKey = storedKey;
            this.clientKey = clientKey;
            this.authMessage = authMessage;
        }
    }

    private static final class AuthenticationFinishResult {
        private final String token;
        private final byte[] protocolKeyHmac;

        private AuthenticationFinishResult(String token, byte[] protocolKeyHmac) {
            this.token = token;
            this.protocolKeyHmac = protocolKeyHmac;
        }
    }
}
