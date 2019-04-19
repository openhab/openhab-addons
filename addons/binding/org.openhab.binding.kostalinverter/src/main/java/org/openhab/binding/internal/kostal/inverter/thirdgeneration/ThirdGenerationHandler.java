/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.internal.kostal.inverter.thirdgeneration;

import static org.openhab.binding.internal.kostal.inverter.thirdgeneration.ThirdGenerationBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCharge;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

    private @Nullable HttpClient httpClient;

    private ThirdGenerationInverterTypes inverterType;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Constructor of this class
     *
     * @param thing        the thing
     * @param httpClient   the httpClient used for communication
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
        if (refreshScheduler != null) {
            refreshScheduler.cancel(true);
            refreshScheduler = null;
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
        JsonArray updateMessageJsonArray = getUpdateChannelMessage(channelList);

        // Send the API request to get values for all channels
        ContentResponse updateMessageContentResponse;
        try {
            updateMessageContentResponse = ThirdGenerationHttpHelper.executeHttpPost(httpClient, config.url,
                    PROCESSDATA, updateMessageJsonArray, sessionId);
            if (updateMessageContentResponse.getStatus() == 404) {
                // Module not found
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        COMMUNICATION_ERROR_INCOMPATIBLE_DEVICE);
                return;
            }
            if (updateMessageContentResponse.getStatus() == 503) {
                // Communication error (e.g. during initial boot of the SCB)
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        COMMUNICATION_ERROR_HTTP);
                return;
            }
            if (updateMessageContentResponse.getStatus() == 401) {
                // session not valid (timed out? device rebooted?)
                logger.info("Session expired - performing retry");
                authenticate();
                // Retry
                updateMessageContentResponse = ThirdGenerationHttpHelper.executeHttpPost(httpClient, config.url,
                        PROCESSDATA, updateMessageJsonArray, sessionId);
            }
        } catch (TimeoutException | ExecutionException e) {
            // Communication problem
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        }
        JsonArray updateMessageResultsJsonArray = ThirdGenerationHttpHelper
                .getJsonArrayFromResponse(updateMessageContentResponse);

        // Map the returned values back to the channels and update them
        for (int i = 0; i < updateMessageResultsJsonArray.size(); i++) {
            JsonObject moduleAnswer = updateMessageResultsJsonArray.get(i).getAsJsonObject();
            String moduleName = moduleAnswer.get("moduleid").getAsString();
            JsonArray processdata = moduleAnswer.get("processdata").getAsJsonArray();
            for (int j = 0; j < processdata.size(); j++) {
                // Update the channels with their new value
                JsonObject newValueObject = processdata.get(j).getAsJsonObject();
                String valueId = newValueObject.get("id").getAsString();
                double valueAsDouble = newValueObject.get("value").getAsDouble();
                ThirdGenerationChannelMappingToWebApi channel = channelList.get(moduleName).stream()
                        .filter(c -> c.moduleId.equals(moduleName) && c.processdataId.equals(valueId)).findFirst()
                        .get();
                updateChannelValue(channel.channelUID, channel.dataType, valueAsDouble);
            }
        }
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Update the channel to the given value.
     * The value is set to the matching data (SITypes etc)
     *
     * @param channeluid Channel to update
     * @param dataType   target data type
     * @param value      value
     */
    private void updateChannelValue(String channeluid, ThirdGenerationChannelDatatypes dataType, Double value) {
        switch (dataType) {
            case INTEGER: {
                updateState(channeluid, new DecimalType(value.longValue()));
                break;
            }
            case PERCEMTAGE: {
                updateState(channeluid, new QuantityType<Dimensionless>(value, SmartHomeUnits.PERCENT));
                break;
            }
            case KILOGRAM: {
                updateState(channeluid, new QuantityType<Mass>(value / 1000, SIUnits.KILOGRAM));
                break;
            }
            case SECONDS: {
                updateState(channeluid, new QuantityType<Time>(value, SmartHomeUnits.SECOND));
                break;
            }
            case KILOWATT_HOUR: {
                updateState(channeluid, new QuantityType<Energy>(value / 1000, SmartHomeUnits.KILOWATT_HOUR));
                break;
            }
            case WATT: {
                updateState(channeluid, new QuantityType<Power>(value, SmartHomeUnits.WATT));
                break;
            }
            case AMPERE: {
                updateState(channeluid, new QuantityType<ElectricCurrent>(value, SmartHomeUnits.AMPERE));
                break;
            }
            case AMPERE_HOUR: {
                // Ampere hours are not supported by ESH, but 1 AH is equal tp 3600 coulomb...
                updateState(channeluid, new QuantityType<ElectricCharge>(value * 3600, SmartHomeUnits.COULOMB));
                break;
            }
            case VOLT: {
                updateState(channeluid, new QuantityType<ElectricPotential>(value, SmartHomeUnits.VOLT));
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
            for (ThirdGenerationChannelMappingToWebApi processdata : channelList.get(moduleId.getKey())) {
                processdataNames.add(processdata.processdataId);
            }
            moduleJsonObject.add("processdataids", processdataNames);
            updateMessageJsonArray.add(moduleJsonObject);
        }
        return updateMessageJsonArray;
    }

    /**
     * This function is used to authenticate against the SCB.
     * SCB uses PBKDF2 and AES256 GCM mode with a slightly modified authentication message.
     * The authentication will fail on JRE < 8u162. since the security policy is set to "limited" by default (see readme
     * for fix)
     */
    private final void authenticate() {
        // Create random numbers
        String clientNonce = ThirdGenerationEncryptionHelper.createClientNonce();
        // Perform first step of authentication
        JsonObject authMeJsonObject = new JsonObject();
        authMeJsonObject.addProperty("username", USER_TYPE);
        authMeJsonObject.addProperty("nonce", clientNonce);

        ContentResponse authStartResponseContentResponse;
        try {
            authStartResponseContentResponse = ThirdGenerationHttpHelper.executeHttpPost(httpClient, config.url,
                    AUTH_START, authMeJsonObject);

            // 200 is the desired status code
            int statusCode = authStartResponseContentResponse.getStatus();

            if (statusCode == 400) {
                // Invalid user (which is hard coded and therefore can not be wrong until the api is changed by the
                // manufacturer
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        COMMUNICATION_ERROR_API_CHANGED);
                return;
            }
            if (statusCode == 403) {
                // User is logged
                // This can happen, if the user had to many bad attempts of entering the password in the web
                // front end
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        COMMUNICATION_ERROR_USER_ACCOUNT_LOCKED);
                return;
            }

            if (statusCode == 503) {
                // internal communication error
                // This can happen if the device is not ready yet for communication
                updateStatus(ThingStatus.UNINITIALIZED);
                return;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        }
        JsonObject authMeResponseJsonObject = ThirdGenerationHttpHelper
                .getJsonObjectFromResponse(authStartResponseContentResponse);

        // Extract information from the response
        String salt = authMeResponseJsonObject.get("salt").getAsString();
        String serverNonce = authMeResponseJsonObject.get("nonce").getAsString();
        int rounds = authMeResponseJsonObject.get("rounds").getAsInt();
        String transactionId = authMeResponseJsonObject.get("transactionId").getAsString();

        // Do the cryptography stuff (magic happens here)
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
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IllegalStateException e2) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }
        String clientProof = ThirdGenerationEncryptionHelper.createClientProof(clientSignature, clientKey);
        // Perform step 2 of the authentication
        JsonObject authFinishJsonObject = new JsonObject();
        authFinishJsonObject.addProperty("transactionId", transactionId);
        authFinishJsonObject.addProperty("proof", clientProof);

        ContentResponse authFinishResponseContentResponse;
        JsonObject authFinishResponseJsonObject;
        try {
            authFinishResponseContentResponse = ThirdGenerationHttpHelper.executeHttpPost(httpClient, config.url,
                    AUTH_FINISH, authFinishJsonObject);
            authFinishResponseJsonObject = ThirdGenerationHttpHelper
                    .getJsonObjectFromResponse(authFinishResponseContentResponse);
            // 200 is the desired status code
            if (authFinishResponseContentResponse.getStatus() == 400) {
                // Authentication failed
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        CONFIGURATION_ERROR_PASSWORD);
                return;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e3) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        }

        // Extract information from the response
        byte[] signature = Base64.getDecoder().decode(authFinishResponseJsonObject.get("signature").getAsString());
        String token = authFinishResponseJsonObject.get("token").getAsString();

        // Validate provided signature against calculated signature
        if (!java.util.Arrays.equals(serverSignature, signature)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }

        // Calculate protocol key
        SecretKeySpec signingKey = new SecretKeySpec(storedKey, HMAC_SHA256_ALGORITHM);
        Mac mac;
        byte[] protocolKeyHMAC;
        try {
            mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update("Session Key".getBytes());
            mac.update(authMessage.getBytes());
            mac.update(clientKey);
            protocolKeyHMAC = mac.doFinal();
        } catch (NoSuchAlgorithmException | InvalidKeyException e1) {
            // Since the necessary libraries are provided, this should not happen
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }

        byte[] data;
        byte[] iv;

        // AES GCM stuff
        iv = new byte[16];

        new SecureRandom().nextBytes(iv);

        SecretKeySpec skeySpec = new SecretKeySpec(protocolKeyHMAC, "AES");
        GCMParameterSpec param = new GCMParameterSpec(protocolKeyHMAC.length * 8 - AES_GCM_TAG_LENGTH, iv);

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES_256/GCM/NOPADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, param);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException e1) {
            // The java installation does not support AES encryption in GCM mode
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }
        try {
            data = cipher.doFinal(token.getBytes("UTF-8"));
        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e1) {
            // No JSON answer received
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return;
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

        // finally create the session for further communication
        ContentResponse createSessionResponseContentResponse;
        JsonObject createSessionResponseJsonObject;
        try {
            createSessionResponseContentResponse = ThirdGenerationHttpHelper.executeHttpPost(httpClient, config.url,
                    AUTH_CREATE_SESSION, createSessionJsonObject);
            createSessionResponseJsonObject = ThirdGenerationHttpHelper
                    .getJsonObjectFromResponse(createSessionResponseContentResponse);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }
        // 200 is the desired status code
        if (createSessionResponseContentResponse.getStatus() == 400) {
            // Authentication failed
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    CONFIGURATION_ERROR_PASSWORD);
            return;
        }

        sessionId = createSessionResponseJsonObject.get("sessionId").getAsString();

        updateStatus(ThingStatus.ONLINE);
    }

}
