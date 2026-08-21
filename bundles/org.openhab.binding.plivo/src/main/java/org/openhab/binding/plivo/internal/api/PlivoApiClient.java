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
package org.openhab.binding.plivo.internal.api;

import static org.openhab.binding.plivo.internal.PlivoBindingConstants.API_BASE_URL;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * REST API client for the Plivo API. Handles authentication and request/response
 * processing for sending SMS/MMS/WhatsApp messages, making calls, and managing
 * phone numbers and applications.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@NonNullByDefault
public class PlivoApiClient {

    private static final int TIMEOUT_SECONDS = 30;
    private static final int PAGE_LIMIT = 20;
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ANSWER_METHOD_POST = "POST";

    private final Logger logger = LoggerFactory.getLogger(PlivoApiClient.class);

    private final HttpClient httpClient;
    private final String authId;
    private final String authToken;
    private final String baseUrl;
    private final String authHeader;

    public PlivoApiClient(HttpClient httpClient, String authId, String authToken) {
        this.httpClient = httpClient;
        this.authId = authId;
        this.authToken = authToken;
        this.baseUrl = API_BASE_URL + authId + "/";
        this.authHeader = "Basic "
                + Base64.getEncoder().encodeToString((authId + ":" + authToken).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates the account credentials by fetching the account resource.
     *
     * @return true if the account is reachable and the Auth ID matches
     * @throws PlivoApiException if the API call fails
     */
    public boolean validateAccount() throws PlivoApiException {
        String response = get(baseUrl);
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonElement authIdElement = json.get("auth_id");
        return authIdElement != null && authId.equals(authIdElement.getAsString());
    }

    /**
     * Sends an SMS or MMS message.
     *
     * @param src the Plivo phone number to send from (E.164 format)
     * @param dst the recipient phone number (E.164 format)
     * @param text the message body
     * @param mediaUrl optional media URL for MMS
     * @param url optional delivery-report webhook URL
     * @return the message UUID if successful
     * @throws PlivoApiException if the API call fails
     */
    public String sendMessage(String src, String dst, @Nullable String text, @Nullable String mediaUrl,
            @Nullable String url) throws PlivoApiException {
        JsonObject body = new JsonObject();
        body.addProperty("src", src);
        body.addProperty("dst", dst);
        body.addProperty("type", mediaUrl != null && !mediaUrl.isBlank() ? "mms" : "sms");
        if (text != null && !text.isBlank()) {
            body.addProperty("text", text);
        }
        if (mediaUrl != null && !mediaUrl.isBlank()) {
            JsonArray mediaUrls = new JsonArray();
            mediaUrls.add(mediaUrl);
            body.add("media_urls", mediaUrls);
        }
        if (url != null && !url.isBlank()) {
            body.addProperty("url", url);
        }
        return firstMessageUuid(post(baseUrl + "Message/", body.toString()));
    }

    /**
     * Sends a WhatsApp message (freeform text and/or media).
     *
     * @param src the WhatsApp Business number to send from (E.164 format)
     * @param dst the recipient phone number (E.164 format)
     * @param text the message body
     * @param mediaUrl optional media URL
     * @param url optional delivery-report webhook URL
     * @return the message UUID if successful
     * @throws PlivoApiException if the API call fails
     */
    public String sendWhatsApp(String src, String dst, @Nullable String text, @Nullable String mediaUrl,
            @Nullable String url) throws PlivoApiException {
        JsonObject body = new JsonObject();
        body.addProperty("src", src);
        body.addProperty("dst", dst);
        body.addProperty("type", "whatsapp");
        if (text != null && !text.isBlank()) {
            body.addProperty("text", text);
        }
        if (mediaUrl != null && !mediaUrl.isBlank()) {
            JsonArray mediaUrls = new JsonArray();
            mediaUrls.add(mediaUrl);
            body.add("media_urls", mediaUrls);
        }
        if (url != null && !url.isBlank()) {
            body.addProperty("url", url);
        }
        return firstMessageUuid(post(baseUrl + "Message/", body.toString()));
    }

    /**
     * Initiates a voice call. Plivo fetches the call-flow XML from {@code answerUrl}
     * when the call is answered.
     *
     * @param from the Plivo phone number to call from (E.164 format)
     * @param to the recipient phone number (E.164 format)
     * @param answerUrl the URL that returns the answer XML
     * @param hangupUrl optional URL notified when the call ends
     * @return the request UUID if successful
     * @throws PlivoApiException if the API call fails
     */
    public String makeCall(String from, String to, String answerUrl, @Nullable String hangupUrl)
            throws PlivoApiException {
        JsonObject body = new JsonObject();
        body.addProperty("from", from);
        body.addProperty("to", to);
        body.addProperty("answer_url", answerUrl);
        body.addProperty("answer_method", ANSWER_METHOD_POST);
        if (hangupUrl != null && !hangupUrl.isBlank()) {
            body.addProperty("hangup_url", hangupUrl);
            body.addProperty("hangup_method", ANSWER_METHOD_POST);
        }
        String response = post(baseUrl + "Call/", body.toString());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonElement requestUuid = json.get("request_uuid");
        return requestUuid != null && !requestUuid.isJsonNull() ? requestUuid.getAsString() : "";
    }

    /**
     * Lists all phone numbers on the account.
     *
     * @return list of phone number info objects
     * @throws PlivoApiException if the API call fails
     */
    public List<PlivoPhoneNumberInfo> listPhoneNumbers() throws PlivoApiException {
        JsonArray objects = getAllObjects("Number/");
        List<PlivoPhoneNumberInfo> result = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            JsonObject entry = objects.get(i).getAsJsonObject();
            result.add(new PlivoPhoneNumberInfo(getJsonString(entry, "number"), getJsonString(entry, "alias")));
        }
        return result;
    }

    /**
     * Creates or updates a Plivo application with the given webhook URLs and returns its app ID.
     * An existing application with the same name is updated in place so repeated initializations
     * do not create duplicates.
     *
     * @param appName the application name
     * @param answerUrl the voice answer webhook URL
     * @param messageUrl the inbound message webhook URL
     * @param hangupUrl the call hangup webhook URL
     * @return the application ID
     * @throws PlivoApiException if the API call fails
     */
    public String createOrUpdateApplication(String appName, String answerUrl, String messageUrl, String hangupUrl)
            throws PlivoApiException {
        String existingAppId = findApplicationId(appName);

        JsonObject body = new JsonObject();
        body.addProperty("answer_url", answerUrl);
        body.addProperty("answer_method", ANSWER_METHOD_POST);
        body.addProperty("message_url", messageUrl);
        body.addProperty("message_method", ANSWER_METHOD_POST);
        body.addProperty("hangup_url", hangupUrl);
        body.addProperty("hangup_method", ANSWER_METHOD_POST);

        if (existingAppId != null) {
            post(baseUrl + "Application/" + existingAppId + "/", body.toString());
            return existingAppId;
        }
        body.addProperty("app_name", appName);
        String response = post(baseUrl + "Application/", body.toString());
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return getJsonString(json, "app_id");
    }

    /**
     * Assigns an application to a phone number.
     *
     * @param number the phone number in E.164 format
     * @param appId the application ID
     * @throws PlivoApiException if the API call fails
     */
    public void assignApplicationToNumber(String number, String appId) throws PlivoApiException {
        JsonObject body = new JsonObject();
        body.addProperty("app_id", appId);
        String pathNumber = number.startsWith("+") ? number.substring(1) : number;
        post(baseUrl + "Number/" + encode(pathNumber) + "/", body.toString());
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getAuthId() {
        return authId;
    }

    private @Nullable String findApplicationId(String appName) throws PlivoApiException {
        JsonArray objects = getAllObjects("Application/");
        for (int i = 0; i < objects.size(); i++) {
            JsonObject entry = objects.get(i).getAsJsonObject();
            if (appName.equals(getJsonString(entry, "app_name"))) {
                return getJsonString(entry, "app_id");
            }
        }
        return null;
    }

    /**
     * Fetches all pages of a Plivo list resource and returns the concatenated {@code objects} array.
     * Plivo paginates list responses, so following the {@code meta.next} link (and stopping when a
     * page returns fewer than {@code limit} entries) avoids missing existing numbers or applications,
     * which in turn prevents duplicate applications being created.
     *
     * @param resourcePath the list resource path relative to the account base (e.g. {@code Number/})
     * @return every object across all pages
     * @throws PlivoApiException if any page request fails
     */
    private JsonArray getAllObjects(String resourcePath) throws PlivoApiException {
        JsonArray all = new JsonArray();
        int offset = 0;
        while (true) {
            String url = baseUrl + resourcePath + "?limit=" + PAGE_LIMIT + "&offset=" + offset;
            JsonObject json = JsonParser.parseString(get(url)).getAsJsonObject();
            JsonArray objects = json.getAsJsonArray("objects");
            int fetched = objects != null ? objects.size() : 0;
            if (objects != null) {
                all.addAll(objects);
            }
            JsonObject meta = json.getAsJsonObject("meta");
            boolean hasNext = meta != null && meta.get("next") != null && !meta.get("next").isJsonNull();
            if (fetched == 0 || fetched < PAGE_LIMIT || !hasNext) {
                break;
            }
            offset += fetched;
        }
        return all;
    }

    private String firstMessageUuid(String response) {
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        JsonArray uuids = json.getAsJsonArray("message_uuid");
        if (uuids != null && uuids.size() > 0) {
            return uuids.get(0).getAsString();
        }
        return "";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String get(String url) throws PlivoApiException {
        logger.trace("Plivo GET: {}", url);
        try {
            ContentResponse response = httpClient.newRequest(url) //
                    .method(HttpMethod.GET) //
                    .header(HttpHeader.AUTHORIZATION, authHeader) //
                    .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS) //
                    .send();
            return handleResponse(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PlivoApiException("Request interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new PlivoApiException("Request failed: " + e.getMessage(), e);
        }
    }

    private String post(String url, String jsonBody) throws PlivoApiException {
        logger.trace("Plivo POST: {} body: {}", url, jsonBody);
        try {
            ContentResponse response = httpClient.newRequest(url) //
                    .method(HttpMethod.POST) //
                    .header(HttpHeader.AUTHORIZATION, authHeader) //
                    .content(new StringContentProvider(CONTENT_TYPE_JSON, jsonBody, StandardCharsets.UTF_8)) //
                    .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS) //
                    .send();
            return handleResponse(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PlivoApiException("Request interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new PlivoApiException("Request failed: " + e.getMessage(), e);
        }
    }

    private String handleResponse(ContentResponse response) throws PlivoApiException {
        int status = response.getStatus();
        String content = response.getContentAsString();
        logger.trace("Plivo response: status={}, content={}", status, content);

        if (status >= 200 && status < 300) {
            return content;
        } else if (status == HttpStatus.UNAUTHORIZED_401) {
            throw new PlivoApiException("Invalid Auth ID or Auth Token", true);
        } else {
            throw new PlivoApiException("Plivo API error (" + status + "): " + extractErrorMessage(content));
        }
    }

    private String extractErrorMessage(String content) {
        try {
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            JsonElement errorElement = json.get("error");
            if (errorElement != null && !errorElement.isJsonNull()) {
                return errorElement.getAsString();
            }
            JsonElement messageElement = json.get("message");
            if (messageElement != null && !messageElement.isJsonNull()) {
                return messageElement.getAsString();
            }
        } catch (RuntimeException e) {
            logger.trace("Could not parse Plivo error body: {}", e.getMessage());
        }
        return content;
    }

    private String getJsonString(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        return element != null && !element.isJsonNull() ? element.getAsString() : "";
    }
}
