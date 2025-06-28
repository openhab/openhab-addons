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
package org.openhab.binding.ecoflow.internal.api;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.ecoflow.internal.api.dto.response.DataWrapper;
import org.openhab.binding.ecoflow.internal.api.dto.response.DeviceListResponseEntry;
import org.openhab.binding.ecoflow.internal.api.dto.response.MqttConnectionData;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class EcoflowApi {
    private final HttpClient httpClient;
    private final String accessKey;
    private final String secretKey;
    private final Random random = new Random();
    private final Gson gson = new Gson();

    public EcoflowApi(HttpClient httpClient, String accessKey, String secretKey) {
        this.httpClient = httpClient;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public List<DeviceListResponseEntry> getDeviceList() throws EcoflowApiException, InterruptedException {
        final String url = "https://api.ecoflow.com/iot-open/sign/device/list";
        ContentResponse response = executeRequest(createHttpRequest(HttpMethod.GET, url, null));
        Type responseType = new TypeToken<DataWrapper<List<DeviceListResponseEntry>>>() {
        }.getType();
        return handleResponseWrapper(gson.fromJson(response.getContentAsString(), responseType));
    }

    public JsonObject getDeviceData(String serialNumber) throws EcoflowApiException, InterruptedException {
        final String url = "https://api.ecoflow.com/iot-open/sign/device/quota/all?sn=" + serialNumber;
        final JsonObject payload = new JsonObject();
        payload.addProperty("sn", serialNumber);
        ContentResponse response = executeRequest(createHttpRequest(HttpMethod.GET, url, payload));
        Type responseType = new TypeToken<DataWrapper<JsonObject>>() {
        }.getType();
        return handleResponseWrapper(gson.fromJson(response.getContentAsString(), responseType));
    }

    public JsonObject getPartialDeviceData(String serialNumber, List<String> keys)
            throws EcoflowApiException, InterruptedException {
        final String url = "https://api.ecoflow.com/iot-open/sign/device/quota";
        final JsonArray quotas = new JsonArray();
        for (String key : keys) {
            quotas.add(key);
        }
        final JsonObject params = new JsonObject();
        params.add("quotas", quotas);
        final JsonObject payload = new JsonObject();
        payload.addProperty("sn", serialNumber);
        payload.add("params", params);
        ContentResponse response = executeRequest(createHttpRequest(HttpMethod.POST, url, payload));
        Type responseType = new TypeToken<DataWrapper<JsonObject>>() {
        }.getType();
        return handleResponseWrapper(gson.fromJson(response.getContentAsString(), responseType));
    }

    public void sendSetRequest(String serialNumber, JsonObject payload)
            throws EcoflowApiException, InterruptedException {
        final String url = "https://api.ecoflow.com/iot-open/sign/device/quota";
        payload.addProperty("sn", serialNumber);
        Request request = createHttpRequest(HttpMethod.PUT, url, payload)
                .header(HttpHeader.CONTENT_TYPE, "application/json") //
                .content(new StringContentProvider(gson.toJson(payload)));
        executeRequest(request);
    }

    public MqttConnectionData createMqttLogin() throws EcoflowApiException, InterruptedException {
        final String url = "https://api.ecoflow.com/iot-open/sign/certification";
        ContentResponse response = executeRequest(createHttpRequest(HttpMethod.GET, url, null));
        Type responseType = new TypeToken<DataWrapper<MqttConnectionData>>() {
        }.getType();
        return handleResponseWrapper(gson.fromJson(response.getContentAsString(), responseType));
    }

    private Request createHttpRequest(HttpMethod method, String url, @Nullable JsonObject payload)
            throws EcoflowApiException {
        int nonce = random.nextInt(900000) + 100000; // 100000..999999
        long timestamp = System.currentTimeMillis();

        Map<String, String> headerParams = new TreeMap<String, String>();
        headerParams.put("accessKey", accessKey);
        headerParams.put("nonce", String.valueOf(nonce));
        headerParams.put("timestamp", String.valueOf(timestamp));

        final String signingData;
        if (payload != null && !payload.isEmpty()) {
            Map<String, String> payloadParams = new TreeMap<String, String>();
            appendToMapRecursive(payload, payloadParams, new Stack<String>());
            signingData = concatenateParams(payloadParams) + "&" + concatenateParams(headerParams);
        } else {
            signingData = concatenateParams(headerParams);
        }
        return httpClient.newRequest(url).method(method) //
                .header("accessKey", accessKey) //
                .header("timestamp", String.valueOf(timestamp)) //
                .header("nonce", String.valueOf(nonce)) //
                .header("sign", hmacSha256(signingData, secretKey));
    }

    private void appendToMapRecursive(JsonElement json, Map<String, String> map, Stack<String> keyStack) {
        if (json instanceof JsonObject obj) {
            for (String key : obj.keySet()) {
                keyStack.push(key);
                appendToMapRecursive(obj.get(key), map, keyStack);
                keyStack.pop();
            }
        } else if (json instanceof JsonArray arr) {
            for (int i = 0; i < arr.size(); i++) {
                keyStack.push("[" + i + "]");
                appendToMapRecursive(arr.get(i), map, keyStack);
                keyStack.pop();
            }
        } else {
            map.put(String.join(".", keyStack), json.getAsString());
        }
    }

    private String concatenateParams(Map<String, String> params) {
        return params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
    }

    private String hmacSha256(String data, String key) throws EcoflowApiException {
        final byte[] bytes;
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            hmac.init(keyspec);

            bytes = hmac.doFinal(data.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            throw new EcoflowApiException(e);
        }

        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private ContentResponse executeRequest(Request request) throws EcoflowApiException, InterruptedException {
        request.timeout(10, TimeUnit.SECONDS);
        try {
            ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new EcoflowApiException(response);
            }
            return response;
        } catch (TimeoutException | ExecutionException e) {
            throw new EcoflowApiException(e);
        }
    }

    private <T> T handleResponseWrapper(@Nullable DataWrapper<T> response) throws EcoflowApiException {
        if (response == null) {
            // should not happen in practice
            throw new EcoflowApiException("No response received");
        }
        if (response.code != 0) {
            throw new EcoflowApiException("API call failed: " + response.message + ", code " + response.code);
        }
        return response.data;
    }
}
