/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kostalpikoiqplenticore.internal;

import static org.openhab.binding.kostalpikoiqplenticore.internal.KostalPikoIqPlenticoreBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KostalPikoIqPlenticoreHandlerBase} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author René Stakemeier - Initial contribution
 */
@NonNullByDefault
public abstract class KostalPikoIqPlenticoreHandlerBase extends BaseThingHandler {

    // used for logging
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    // base URL of the web api
    private static final String WEB_API = "/api/v1";

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
     * List of available channels for the concrete device
     */

    /*
     * After the authentication the result (the session id) is stored here and used to "sign" future requests
     */
    @Nullable
    protected String sessionId = null;
    /*
     * The configuration file containing the host, the password and the refresh interval
     */
    @Nullable
    private KostalPikoIqPlenticoreConfiguration config;

    @Nullable
    protected ScheduledFuture<?> refreshScheduler;

    /**
     * Constructor of this class
     *
     * @param thing the thing
     */
    public KostalPikoIqPlenticoreHandlerBase(Thing thing) {
        super(thing);

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /*
         * All channels are readonly and updated by the scheduler
         */
    }

    @Override
    public void dispose() {
        if (refreshScheduler != null) {
            refreshScheduler.cancel(true);
        }
        super.dispose();
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(KostalPikoIqPlenticoreConfiguration.class);
        /*
         * temporary value while initializing
         */
        updateStatus(ThingStatus.UNKNOWN);

        /*
         * Start the initialization
         */
        authenticate();

        /*
         * Start the update scheduler as configured
         */
        refreshScheduler = scheduler.scheduleWithFixedDelay(() -> {
            updateChannelValues();
        }, 10, config.refreshInternalInSeconds, TimeUnit.SECONDS);
    }

    /**
     * The API supports the resolution of multiple values at a time
     *
     * Therefore this methods builds one request to gather all information for the current inverter.
     * The list contains all channels as defined in {@link KostalPikoIqPlenticoreMappingInverterToChannel} for the
     * current inverter
     *
     */
    void updateChannelValues() {
        Map<String, List<KostalPikoIqPlenticoreChannelMappingToWebApi>> channelList = KostalPikoIqPlenticoreMappingInverterToChannel
                .getModuleToChannelsMappingForInverter(getInverterType());
        JSONArray updateMessageJSONArray = new JSONArray();
        /*
         * Build the message to send to the inverter
         */
        for (String moduleId : channelList.keySet()) {
            JSONObject moduleJSONObject = new JSONObject();
            try {
                moduleJSONObject.put("moduleid", moduleId);
            } catch (JSONException e1) {
                // Only thrown if key is already in the JSONObject
                // Cannot happen here => ignore
            }
            List<String> processdataNames = new ArrayList<String>();
            for (KostalPikoIqPlenticoreChannelMappingToWebApi processdata : channelList.get(moduleId)) {
                processdataNames.add(processdata.processdataId);
            }
            try {
                moduleJSONObject.put("processdataids", processdataNames);
            } catch (JSONException e) {
                // Only thrown if key is already in the JSONObject
                // Cannot happen here => ignore
            }
            updateMessageJSONArray.put(moduleJSONObject);
        }
        /*
         * Send the API request to get values for all channels
         * Note: If one does not exist, all fail
         */
        HttpResponse updateMessageHTTPResponse;
        try {
            updateMessageHTTPResponse = executeHttpPost(PROCESSDATA, updateMessageJSONArray, sessionId);
            if (updateMessageHTTPResponse.getStatusLine().getStatusCode() == 404) {
                /*
                 * Module not found
                 */
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        COMMUNICATION_ERROR_INCOMPATIBLE_DEVICE);
                return;
            }
            if (updateMessageHTTPResponse.getStatusLine().getStatusCode() == 503) {
                /*
                 * Communication error (e.g. during initial boot of the SCB)
                 */
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        COMMUNICATION_ERROR_HTTP);
                return;
            }
            if (updateMessageHTTPResponse.getStatusLine().getStatusCode() == 401) {
                /*
                 * session not valid (timed out?)
                 */
                logger.info("Session expired");
                try {
                    authenticate();
                } catch (Exception e) {
                }
                /*
                 * Retry
                 */
                updateMessageHTTPResponse = executeHttpPost(PROCESSDATA, updateMessageJSONArray, sessionId);
            }
        } catch (UnsupportedOperationException | IOException e) {
            /*
             * Communication problem
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        }
        JSONArray updateMessageResultsJSONArray;
        try {
            updateMessageResultsJSONArray = getJSONArrayFromResponse(updateMessageHTTPResponse);
        } catch (IOException e) {
            /*
             * Communication problem
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        } catch (JSONException | UnsupportedOperationException e) {
            /*
             * Answer did not contain a JSONArray
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return;
        }

        /*
         * Map the returned values back to the channels and update them
         */
        for (int i = 0; i < updateMessageResultsJSONArray.length(); i++) {
            JSONObject moduleAnswer;
            String moduleName;
            JSONArray processdata;
            try {
                moduleAnswer = updateMessageResultsJSONArray.getJSONObject(i);
                moduleName = moduleAnswer.getString("moduleid");
                processdata = moduleAnswer.getJSONArray("processdata");
            } catch (JSONException e) {
                /*
                 * The response did not contains a JSONArray
                 */
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        COMMUNICATION_ERROR_JSON);
                return;
            }

            for (int j = 0; j < processdata.length(); j++) {
                /*
                 * Update the channels with their new value
                 */
                JSONObject newValueObject;
                String valueId;
                double valueAsDouble;
                try {
                    newValueObject = processdata.getJSONObject(j);
                    valueId = newValueObject.getString("id");
                    valueAsDouble = newValueObject.getDouble("value");
                } catch (JSONException e) {
                    /*
                     * Response did not contain the data
                     */
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            COMMUNICATION_ERROR_JSON);
                    return;
                }
                KostalPikoIqPlenticoreChannelMappingToWebApi channel = channelList.get(moduleName).stream()
                        .filter(c -> c.moduleId.equals(moduleName) && c.processdataId.equals(valueId)).findFirst()
                        .get();
                updateChannelValue(channel.channelUID, channel.dataType, valueAsDouble);
            }
        }
    }

    /**
     * Update the channel to the given value.
     * The value is set to the matching data (SITypes etc)
     *
     * @param channeluid Channel to update
     * @param dataType   target data type
     * @param value      value
     */
    private void updateChannelValue(String channeluid, KostalPikoIqPlenticoreChannelDatatypes dataType, Double value) {
        switch (dataType) {
            case Integer: {
                updateState(channeluid, new DecimalType(value.longValue()));
                break;
            }
            case Percent: {
                updateState(channeluid,
                        new QuantityType<javax.measure.quantity.Dimensionless>(value, SmartHomeUnits.PERCENT));
                break;
            }
            case KiloGram: {
                updateState(channeluid, new QuantityType<javax.measure.quantity.Mass>(value / 1000, SIUnits.KILOGRAM));
                break;
            }
            case Seconds: {
                updateState(channeluid, new QuantityType<javax.measure.quantity.Time>(value, SmartHomeUnits.SECOND));
                break;
            }
            case KiloWattHour: {
                updateState(channeluid,
                        new QuantityType<javax.measure.quantity.Energy>(value / 1000, SmartHomeUnits.KILOWATT_HOUR));
                break;
            }
            case Watt: {
                updateState(channeluid, new QuantityType<javax.measure.quantity.Power>(value, SmartHomeUnits.WATT));
                break;
            }
            case Ampere: {
                updateState(channeluid,
                        new QuantityType<javax.measure.quantity.ElectricCurrent>(value, SmartHomeUnits.AMPERE));
                break;
            }
            case AmpereHour: {
                /*
                 * Ampere hours are not supported by ESH, but 1 AH is equal tp 3600 coulomb...
                 */
                updateState(channeluid,
                        new QuantityType<javax.measure.quantity.ElectricCharge>(value * 3600, SmartHomeUnits.COULOMB));
                break;
            }
            case Volt: {
                updateState(channeluid,
                        new QuantityType<javax.measure.quantity.ElectricPotential>(value, SmartHomeUnits.VOLT));
                break;
            }
            default: {
                /*
                 * unknown datatype
                 */
                logger.error(dataType + " not known!");
            }
        }
    }

    /**
     * Get the inverter type of the current handler
     *
     * @return inverter type
     */
    protected abstract KostalPikoIqPlenticoreInverterTypes getInverterType();

    /**
     * This function is used to authenticate against the SCB.
     * SCB uses PBKDF2 and AES256 GCM mode with a slightly modified authentication message.
     * The authentication will fail on JRE < 8u162. since the security policy is set to "limited" by default (see readme
     * for fix)
     */
    @SuppressWarnings("null")
    private final void authenticate() {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        /*
         * Create random numbers
         */
        String clientNonce = createClientNonce();
        /*
         * Perform first step of authentication
         */
        JSONObject authMeJsonObject = new JSONObject();
        try {
            authMeJsonObject.put("username", USER_TYPE);
            authMeJsonObject.put("nonce", clientNonce);
        } catch (JSONException e) {
            /*
             * This exception only occurs, if the key is already presented in the object
             * => ignore
             */
        }
        HttpResponse authStartResponseHttpResponse;
        try {
            authStartResponseHttpResponse = executeHttpPost(AUTH_START, authMeJsonObject);

            /*
             * 200 is the desired status code
             */
            int statusCode = authStartResponseHttpResponse.getStatusLine().getStatusCode();

            if (statusCode == 400) {
                /*
                 * Invalid user (which is hard coded and therefore can not be wrong until the api is changed by the
                 * manufacturer
                 *
                 */
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        COMMUNICATION_ERROR_API_CHANGED);
                return;
            }
            if (statusCode == 403) {
                /*
                 * User is logged
                 * This can happen, if the user had to many bad attempts of entering the password in the web
                 * front end
                 */
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        COMMUNICATION_ERROR_USER_ACCOUNT_LOCKED);
                return;
            }

            if (statusCode == 503) {
                /*
                 * internal communication error
                 * This can happen if the device is not ready yet for communication
                 */
                updateStatus(ThingStatus.UNINITIALIZED);
                return;
            }

        } catch (

        IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        }
        JSONObject authMeResponseJsonObject;
        try {
            authMeResponseJsonObject = getJSONObjectFromResponse(authStartResponseHttpResponse);
        } catch (UnsupportedOperationException | JSONException | IOException ex) {
            /*
             * No JSON answer received
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return;
        }
        /*
         * Extract information from the response
         */
        String salt;
        String serverNonce;
        int rounds;
        String transactionId;
        try {
            salt = authMeResponseJsonObject.getString("salt");
            serverNonce = authMeResponseJsonObject.getString("nonce");
            rounds = authMeResponseJsonObject.getInt("rounds");
            transactionId = authMeResponseJsonObject.getString("transactionId");
        } catch (JSONException e2) {
            /*
             * the answer did not contain the expected values
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }

        /*
         * Do the cryptography stuff (magic happens here)
         */
        byte[] saltedPasswort;
        byte[] clientKey;
        byte[] serverKey;
        byte[] storedKey;
        byte[] clientSignature;
        byte[] serverSignature;
        String authMessage;
        try {
            saltedPasswort = getPBKDF2Hash(config.userPassword, Base64.getDecoder().decode(salt), rounds);
            clientKey = getHMACSha256(saltedPasswort, "Client Key");
            serverKey = getHMACSha256(saltedPasswort, "Server Key");
            storedKey = getSha256Hash(clientKey);
            authMessage = String.format("n=%s,r=%s,r=%s,s=%s,i=%d,c=biws,r=%s", USER_TYPE, clientNonce, serverNonce,
                    salt, rounds, serverNonce);
            clientSignature = getHMACSha256(storedKey, authMessage);
            serverSignature = getHMACSha256(serverKey, authMessage);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IllegalStateException e2) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }
        String clientProof = createClientProof(clientSignature, clientKey);
        /*
         * Perform step 2 of the authentication
         */
        JSONObject authFinishJsonObject = new JSONObject();
        try {
            authFinishJsonObject.put("transactionId", transactionId);
            authFinishJsonObject.put("proof", clientProof);
        } catch (JSONException e) {
            /*
             * This exception only occurs, if the key is already presented in the object
             * => ignore
             */
        }
        HttpResponse authFinishResponseHttpResponse;
        try {
            authFinishResponseHttpResponse = executeHttpPost(AUTH_FINISH, authFinishJsonObject);
            /*
             * 200 is the desired status code
             */
            if (authFinishResponseHttpResponse.getStatusLine().getStatusCode() == 400) {
                /*
                 * Authentication failed
                 *
                 */
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        CONFIGURATION_ERROR_PASSWORD);
                return;
            }
        } catch (IOException e3) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        }
        JSONObject authFinishResponseJsonObject;
        try {
            authFinishResponseJsonObject = getJSONObjectFromResponse(authFinishResponseHttpResponse);
        } catch (UnsupportedOperationException | JSONException | IOException e2) {
            /*
             * the answer did not contain the expected values
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }

        /*
         * Extract information from the response
         */
        byte[] signature;
        String token;
        try {
            signature = Base64.getDecoder().decode(authFinishResponseJsonObject.getString("signature"));
            token = authFinishResponseJsonObject.getString("token");
        } catch (JSONException e2) {
            /*
             * the answer did not contain the expected values
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }

        /*
         * Validate provided signature against calculated signature
         */
        if (!java.util.Arrays.equals(serverSignature, signature)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }

        /*
         * Calculate protocol key
         */
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
            /*
             * Since the necessary libraries are provided, this should not happen
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }

        byte[] data;
        byte[] iv;

        /*
         * AES GCM stuff
         */
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
            /*
             * The java installation does not support AES encryption in GCM mode
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }
        try {
            data = cipher.doFinal(token.getBytes("UTF-8"));
        } catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e1) {
            /*
             * No JSON answer received
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_JSON);
            return;
        }

        byte[] ciphertext = new byte[data.length - AES_GCM_TAG_LENGTH / 8];
        byte[] gcmTag = new byte[AES_GCM_TAG_LENGTH / 8];
        System.arraycopy(data, 0, ciphertext, 0, data.length - AES_GCM_TAG_LENGTH / 8);
        System.arraycopy(data, data.length - AES_GCM_TAG_LENGTH / 8, gcmTag, 0, AES_GCM_TAG_LENGTH / 8);

        JSONObject createSessionJsonObject = new JSONObject();
        try {
            createSessionJsonObject.put("transactionId", transactionId);
            createSessionJsonObject.put("iv", Base64.getEncoder().encodeToString(iv));
            createSessionJsonObject.put("tag", Base64.getEncoder().encodeToString(gcmTag));
            createSessionJsonObject.put("payload", Base64.getEncoder().encodeToString(ciphertext));
        } catch (JSONException e) {
            /*
             * This exception only occurs, if the key is already presented in the object
             * => ignore
             */
        }
        /*
         * finally create the session for further communication
         */
        HttpResponse createSessionResponseHttpResponse;
        try {
            createSessionResponseHttpResponse = executeHttpPost(AUTH_CREATE_SESSION, createSessionJsonObject);
            /*
             * 200 is the desired status code
             */
            if (createSessionResponseHttpResponse.getStatusLine().getStatusCode() == 400) {
                /*
                 * Authentication failed
                 */
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        CONFIGURATION_ERROR_PASSWORD);
                return;
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, COMMUNICATION_ERROR_HTTP);
            return;
        }
        JSONObject createSessionResponseJsonObject;
        try {
            createSessionResponseJsonObject = getJSONObjectFromResponse(createSessionResponseHttpResponse);
        } catch (UnsupportedOperationException | JSONException | IOException e) {
            /*
             * no valid json object
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }
        try {
            sessionId = createSessionResponseJsonObject.getString("sessionId");
        } catch (JSONException e) {
            /*
             * the answer did not contain the expected values
             */
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    COMMUNICATION_ERROR_AUTHENTICATION);
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Helper function to execute a HTTP post request
     *
     * @param resource   web API resource to post to
     * @param parameters the JSON content to post
     * @return the HTTP response for the created post request
     * @throws ClientProtocolException thrown if the web service did not respond correctly
     * @throws IOException             thrown if there are communication problems
     */
    private HttpResponse executeHttpPost(String resource, JSONObject parameters)
            throws ClientProtocolException, IOException {
        return executeHttpPost(resource, parameters, null);
    }

    /**
     * Helper function to execute a HTTP post request
     *
     * @param resource   web API resource to post to
     * @param sessionId  optional session ID
     * @param parameters the JSON content to post
     * @return the HTTP response for the created post request
     * @throws ClientProtocolException thrown if the web service did not respond correctly
     * @throws IOException             thrown if there are communication problems
     */
    private HttpResponse executeHttpPost(String resource, JSONArray parameters, @Nullable String sessionId)
            throws ClientProtocolException, IOException {
        @SuppressWarnings("null")
        HttpPost postObject = new HttpPost(String.format("http://%s/%s%s", config.url, WEB_API, resource));
        postObject.addHeader("Accept", "application/json");
        postObject.addHeader("Content-Type", "application/json");
        if (sessionId != null) {
            postObject.addHeader("Authorization", String.format("Session %s", sessionId));
        }
        try {
            postObject.setEntity(new StringEntity(parameters.toString()));
        } catch (UnsupportedEncodingException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    COMMUNICATION_ERROR_UNSUPPORTED_ENCODING);
        }
        return HttpClientBuilder.create().build().execute(postObject);
    }

    /**
     * Helper function to execute a HTTP post request
     *
     * @param resource   web API resource to post to
     * @param sessionId  optional session ID
     * @param parameters the JSON content to post
     * @return the HTTP response for the created post request
     * @throws ClientProtocolException thrown if the web service did not respond correctly
     * @throws IOException             thrown if there are communication problems
     */
    private HttpResponse executeHttpPost(String resource, JSONObject parameters, @Nullable String sessionId)
            throws ClientProtocolException, IOException {
        @SuppressWarnings("null")
        HttpPost postObject = new HttpPost(String.format("http://%s/%s%s", config.url, WEB_API, resource));
        postObject.addHeader("Accept", "application/json");
        postObject.addHeader("Content-Type", "application/json");
        if (sessionId != null) {
            postObject.addHeader("Authorization", String.format("Session %s", sessionId));
        }
        try {
            postObject.setEntity(new StringEntity(parameters.toString()));
        } catch (UnsupportedEncodingException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    COMMUNICATION_ERROR_UNSUPPORTED_ENCODING);
        }
        return HttpClientBuilder.create().build().execute(postObject);
    }

    /**
     * Helper to extract the JSONArray from a HTTP response.
     * Use only, if you expect a JSONArray and no other types (e.g. JSON array)!
     *
     * @param reponse the HTTP response
     * @return the JSON object
     * @throws UnsupportedOperationException
     * @throws JSONException                 thrown if the response does not contain a JSON object
     * @throws IOException                   in case of communication problems
     */
    private JSONArray getJSONArrayFromResponse(HttpResponse reponse)
            throws UnsupportedOperationException, JSONException, IOException {
        return new JSONArray(new BufferedReader(new InputStreamReader(reponse.getEntity().getContent())).readLine());
    }

    /**
     * Helper to extract the JSON object from a HTTP response.
     * Use only, if you expect a JSON object and no other types (e.g. JSON array)!
     *
     * @param reponse the HTTP response
     * @return the JSON object
     * @throws UnsupportedOperationException
     * @throws JSONException                 thrown if the response does not contain a JSON object
     * @throws IOException                   in case of communication problems
     */
    private JSONObject getJSONObjectFromResponse(HttpResponse reponse)
            throws UnsupportedOperationException, JSONException, IOException {
        return new JSONObject(new BufferedReader(new InputStreamReader(reponse.getEntity().getContent())).readLine());
    }

    /**
     * Helper function to execute a HTTP get request
     *
     * @param resource web API resource to get
     * @return the HTTP response for the created get request
     * @throws ClientProtocolException thrown if the web service did not respond correctly
     * @throws IOException             thrown if there are communication problems
     */
    @SuppressWarnings("unused")
    private HttpResponse executeHttpGet(String resource) throws ClientProtocolException, IOException {
        return executeHttpGet(resource, null);
    }

    /**
     * Helper function to execute a HTTP get request
     *
     * @param resource  web API resource to get
     * @param sessionId optional session ID
     * @return the HTTP response for the created get request
     * @throws ClientProtocolException thrown if the web service did not respond correctly
     * @throws IOException             thrown if there are communication problems
     */
    private HttpResponse executeHttpGet(String resource, @Nullable String sessionId)
            throws ClientProtocolException, IOException {
        @SuppressWarnings("null")
        HttpGet getObject = new HttpGet(String.format("http://%s/%s%s", config.url, WEB_API, resource));
        getObject.addHeader("Accept", "application/json");
        getObject.addHeader("Content-Type", "application/json");
        if (sessionId != null) {
            getObject.addHeader("Authorization", String.format("Session %s", sessionId));
        }
        return HttpClientBuilder.create().build().execute(getObject);
    }

    /**
     * This method generates the HMACSha256 encrypted value of the given value
     *
     * @param password       Password used for encryption
     * @param valueToEncrypt value to encrypt
     * @return encrypted value
     * @throws InvalidKeyException      thrown if the key generated from the password is invalid
     * @throws NoSuchAlgorithmException thrown if HMAC SHA 256 is not supported
     */
    private static byte[] getHMACSha256(byte[] password, String valueToEncrypt)
            throws InvalidKeyException, NoSuchAlgorithmException {
        SecretKeySpec signingKey = new SecretKeySpec(password, HMAC_SHA256_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(signingKey);
        mac.update(valueToEncrypt.getBytes());
        return mac.doFinal();
    }

    /**
     * This methods generates the client proof.
     * It is calculated as XOR between the {@link clientSignature} and the {@link serverSignature}
     *
     * @param clientSignature client signature
     * @param serverSignature server signature
     * @return client proof
     */
    private static String createClientProof(byte[] clientSignature, byte[] serverSignature) {
        byte[] result = new byte[clientSignature.length];
        for (int i = 0; i < clientSignature.length; i++) {
            result[i] = (byte) (0xff & (clientSignature[i] ^ serverSignature[i]));
        }
        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * Create the PBKDF2 hash
     *
     * @param password password
     * @param salt     salt
     * @param rounds   rounds
     * @return hash
     * @throws NoSuchAlgorithmException if PBKDF2WithHmacSHA256 is not supported
     * @throws InvalidKeySpecException  if the key specification is not supported
     */
    private static byte[] getPBKDF2Hash(String password, byte[] salt, int rounds)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Create the SHA256 hash value for the given byte array
     *
     * @param valueToHash byte array to get the hash value for
     * @return the hash value
     * @throws NoSuchAlgorithmException if SHA256 is not supported
     */
    private static byte[] getSha256Hash(byte[] valueToHash) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(SHA_256_HASH).digest(valueToHash);
    }

    /**
     * Create the nonce (numbers used once) for the client for communication
     *
     * @return nonce
     */
    private static String createClientNonce() {
        Random generator = new Random();

        // Randomize the random generator
        byte[] randomizeArray = new byte[1024];
        generator.nextBytes(randomizeArray);

        // 3 words of 4 bytes are required for the handshake
        byte[] nonceArray = new byte[12];
        generator.nextBytes(nonceArray);

        // return the base64 encoded value of the random words
        return Base64.getMimeEncoder().encodeToString(nonceArray);
    }
}
