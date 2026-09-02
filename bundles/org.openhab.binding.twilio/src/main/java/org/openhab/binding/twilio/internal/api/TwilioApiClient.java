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
package org.openhab.binding.twilio.internal.api;

import static org.openhab.binding.twilio.internal.TwilioBindingConstants.API_BASE_URL;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * REST API client for the Twilio API. Handles authentication and request/response
 * processing for sending SMS/MMS, making calls, and managing phone numbers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TwilioApiClient {

    private static final int TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(TwilioApiClient.class);

    private final HttpClient httpClient;
    private final String accountSid;
    private final String authToken;
    private final String baseUrl;
    private final String authHeader;

    public TwilioApiClient(HttpClient httpClient, String accountSid, String authToken) {
        this.httpClient = httpClient;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.baseUrl = API_BASE_URL + accountSid + "/";
        this.authHeader = "Basic "
                + Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates the account credentials by fetching the account resource.
     *
     * @return true if the account is active
     * @throws TwilioApiException if the API call fails
     */
    public boolean validateAccount() throws TwilioApiException {
        String response = get(baseUrl.substring(0, baseUrl.length() - 1) + ".json");
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonElement statusElement = json.get("status");
        return statusElement != null && "active".equals(statusElement.getAsString());
    }

    /**
     * Sends an SMS or MMS message.
     *
     * @param from the Twilio phone number to send from (E.164 format)
     * @param to the recipient phone number (E.164 format)
     * @param body the message body
     * @param mediaUrl optional media URL for MMS
     * @param statusCallback optional status callback URL
     * @return the message SID if successful
     * @throws TwilioApiException if the API call fails
     */
    public String sendMessage(String from, String to, @Nullable String body, @Nullable String mediaUrl,
            @Nullable String statusCallback) throws TwilioApiException {
        StringBuilder params = new StringBuilder();
        params.append("From=").append(encode(from));
        params.append("&To=").append(encode(to));
        if (body != null && !body.isBlank()) {
            params.append("&Body=").append(encode(body));
        }
        if (mediaUrl != null && !mediaUrl.isBlank()) {
            params.append("&MediaUrl=").append(encode(mediaUrl));
        }
        if (statusCallback != null && !statusCallback.isBlank()) {
            params.append("&StatusCallback=").append(encode(statusCallback));
        }

        String response = post(baseUrl + "Messages.json", params.toString());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonElement sidElement = json.get("sid");
        return sidElement != null ? sidElement.getAsString() : "";
    }

    /**
     * Initiates a voice call with inline TwiML.
     *
     * @param from the Twilio phone number to call from (E.164 format)
     * @param to the recipient phone number (E.164 format)
     * @param twiml the TwiML instructions for the call
     * @param statusCallback optional status callback URL
     * @return the call SID if successful
     * @throws TwilioApiException if the API call fails
     */
    public String makeCall(String from, String to, String twiml, @Nullable String statusCallback)
            throws TwilioApiException {
        StringBuilder params = new StringBuilder();
        params.append("From=").append(encode(from));
        params.append("&To=").append(encode(to));
        params.append("&Twiml=").append(encode(twiml));
        if (statusCallback != null && !statusCallback.isBlank()) {
            params.append("&StatusCallback=").append(encode(statusCallback));
        }

        String response = post(baseUrl + "Calls.json", params.toString());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonElement sidElement = json.get("sid");
        return sidElement != null ? sidElement.getAsString() : "";
    }

    /**
     * Lists all incoming phone numbers on the account.
     *
     * @return list of phone number info objects
     * @throws TwilioApiException if the API call fails
     */
    public List<TwilioPhoneNumberInfo> listPhoneNumbers() throws TwilioApiException {
        String response = get(baseUrl + "IncomingPhoneNumbers.json");
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        var numbers = json.getAsJsonArray("incoming_phone_numbers");
        List<TwilioPhoneNumberInfo> result = new ArrayList<>();
        if (numbers != null) {
            for (int i = 0; i < numbers.size(); i++) {
                JsonObject entry = numbers.get(i).getAsJsonObject();
                String sid = getJsonString(entry, "sid");
                String phoneNumber = getJsonString(entry, "phone_number");
                String friendlyName = getJsonString(entry, "friendly_name");
                result.add(new TwilioPhoneNumberInfo(sid, phoneNumber, friendlyName));
            }
        }
        return result;
    }

    /**
     * Looks up a phone number SID by phone number.
     *
     * @param phoneNumber the phone number in E.164 format
     * @return the phone number SID, or null if not found
     * @throws TwilioApiException if the API call fails
     */
    public @Nullable String lookupPhoneNumberSid(String phoneNumber) throws TwilioApiException {
        String url = baseUrl + "IncomingPhoneNumbers.json?PhoneNumber=" + encode(phoneNumber);
        String response = get(url);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        var numbers = json.getAsJsonArray("incoming_phone_numbers");
        if (numbers != null && numbers.size() > 0) {
            JsonObject first = numbers.get(0).getAsJsonObject();
            JsonElement sidElement = first.get("sid");
            return sidElement != null ? sidElement.getAsString() : null;
        }
        return null;
    }

    /**
     * Configures webhook URLs on a phone number.
     *
     * @param phoneNumberSid the phone number SID
     * @param smsUrl the SMS webhook URL
     * @param voiceUrl the voice webhook URL
     * @param statusCallback the status callback URL
     * @throws TwilioApiException if the API call fails
     */
    public void configureWebhooks(String phoneNumberSid, String smsUrl, String voiceUrl, String statusCallback)
            throws TwilioApiException {
        StringBuilder params = new StringBuilder();
        params.append("SmsUrl=").append(encode(smsUrl));
        params.append("&SmsMethod=POST");
        params.append("&VoiceUrl=").append(encode(voiceUrl));
        params.append("&VoiceMethod=POST");
        params.append("&StatusCallback=").append(encode(statusCallback));
        params.append("&StatusCallbackMethod=POST");

        post(baseUrl + "IncomingPhoneNumbers/" + phoneNumberSid + ".json", params.toString());
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getAccountSid() {
        return accountSid;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String get(String url) throws TwilioApiException {
        logger.trace("Twilio GET: {}", url);
        try {
            ContentResponse response = httpClient.newRequest(url) //
                    .method(HttpMethod.GET) //
                    .header(HttpHeader.AUTHORIZATION, authHeader) //
                    .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS) //
                    .send();
            return handleResponse(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TwilioApiException("Request interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new TwilioApiException("Request failed: " + e.getMessage(), e);
        }
    }

    private String post(String url, String formBody) throws TwilioApiException {
        logger.trace("Twilio POST: {} body: {}", url, formBody);
        try {
            ContentResponse response = httpClient.newRequest(url) //
                    .method(HttpMethod.POST) //
                    .header(HttpHeader.AUTHORIZATION, authHeader) //
                    .content(new StringContentProvider("application/x-www-form-urlencoded", formBody,
                            StandardCharsets.UTF_8)) //
                    .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS) //
                    .send();
            return handleResponse(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TwilioApiException("Request interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new TwilioApiException("Request failed: " + e.getMessage(), e);
        }
    }

    private String handleResponse(ContentResponse response) throws TwilioApiException {
        int status = response.getStatus();
        String content = response.getContentAsString();
        logger.trace("Twilio response: status={}, content={}", status, content);

        if (status == HttpStatus.OK_200 || status == HttpStatus.CREATED_201) {
            return content;
        } else if (status == HttpStatus.UNAUTHORIZED_401) {
            throw new TwilioApiException("Invalid Account SID or Auth Token", true);
        } else {
            String message = extractErrorMessage(content);
            throw new TwilioApiException("Twilio API error (" + status + "): " + message);
        }
    }

    private String extractErrorMessage(String content) {
        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            JsonElement messageElement = json.get("message");
            if (messageElement != null) {
                return messageElement.getAsString();
            }
        } catch (Exception e) {
            // ignore parse errors
        }
        return content;
    }

    private String getJsonString(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        return element != null && !element.isJsonNull() ? element.getAsString() : "";
    }
}
