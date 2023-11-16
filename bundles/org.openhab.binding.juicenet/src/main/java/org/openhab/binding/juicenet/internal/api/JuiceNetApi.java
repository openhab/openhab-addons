/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.juicenet.internal.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.juicenet.internal.api.dto.JuiceNetApiDevice;
import org.openhab.binding.juicenet.internal.api.dto.JuiceNetApiDeviceStatus;
import org.openhab.binding.juicenet.internal.api.dto.JuiceNetApiInfo;
import org.openhab.binding.juicenet.internal.api.dto.JuiceNetApiTouSchedule;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link JuiceNetApi} is responsible for implementing the api interface to the JuiceNet cloud server
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApi {
    private final Logger logger = LoggerFactory.getLogger(JuiceNetApi.class);

    private static final String API_HOST = "https://jbv1-api.emotorwerks.com/";
    private static final String API_ACCOUNT = API_HOST + "box_pin";
    private static final String API_DEVICE = API_HOST + "box_api_secure";
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    private String apiToken = "";
    private HttpClient httpClient;
    private ThingUID bridgeUID;

    public enum ApiCommand {
        GET_ACCOUNT_UNITS("get_account_units", API_ACCOUNT),
        GET_STATE("get_state", API_DEVICE),
        SET_CHARGING_LIMIT("set_limit", API_DEVICE),
        GET_SCHEDULE("get_schedule", API_DEVICE),
        SET_SCHEDULE("set_schedule", API_DEVICE),
        GET_INFO("get_info", API_DEVICE),
        SET_OVERRIDE("set_override", API_DEVICE);

        final String command;
        final String uri;

        ApiCommand(String command, String uri) {
            this.command = command;
            this.uri = uri;
        }
    }

    public JuiceNetApi(HttpClient httpClient, ThingUID bridgeUID) {
        this.bridgeUID = bridgeUID;
        this.httpClient = httpClient;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public List<JuiceNetApiDevice> queryDeviceList() throws JuiceNetApiException, InterruptedException {
        JuiceNetApiDevice[] listDevices;
        try {
            JsonObject jsonResponse = postApiCommand(ApiCommand.GET_ACCOUNT_UNITS, null);

            JsonElement unitsElement = jsonResponse.get("units");
            if (unitsElement == null) {
                throw new JuiceNetApiException("getDevices from Juicenet API failed, no 'units' element in response.");
            }

            listDevices = new Gson().fromJson(unitsElement.getAsJsonArray(), JuiceNetApiDevice[].class);
        } catch (JsonSyntaxException e) {
            throw new JuiceNetApiException("getDevices from JuiceNet API failed, invalid JSON list.");
        } catch (IllegalStateException e) {
            throw new JuiceNetApiException("getDevices from JuiceNet API failed - did not return valid array.");
        }

        return Arrays.asList(listDevices);
    }

    public JuiceNetApiDeviceStatus queryDeviceStatus(String token) throws JuiceNetApiException, InterruptedException {
        JuiceNetApiDeviceStatus deviceStatus;
        try {
            JsonObject jsonResponse = postApiCommand(ApiCommand.GET_STATE, token);

            deviceStatus = new Gson().fromJson(jsonResponse, JuiceNetApiDeviceStatus.class);
        } catch (JsonSyntaxException e) {
            throw new JuiceNetApiException("queryDeviceStatus from JuiceNet API failed, invalid JSON list.");
        } catch (IllegalStateException e) {
            throw new JuiceNetApiException("queryDeviceStatus from JuiceNet API failed - did not return valid array.");
        }

        return Objects.requireNonNull(deviceStatus);
    }

    public JuiceNetApiInfo queryInfo(String token) throws InterruptedException, JuiceNetApiException {
        JuiceNetApiInfo info;
        try {
            JsonObject jsonResponse = postApiCommand(ApiCommand.GET_INFO, token);

            info = new Gson().fromJson(jsonResponse, JuiceNetApiInfo.class);
        } catch (JsonSyntaxException e) {
            throw new JuiceNetApiException("queryInfo from JuiceNet API failed, invalid JSON list.");
        } catch (IllegalStateException e) {
            throw new JuiceNetApiException("queryInfo from JuiceNet API failed - did not return valid array.");
        }

        return Objects.requireNonNull(info);
    }

    public JuiceNetApiTouSchedule queryTOUSchedule(String token) throws InterruptedException, JuiceNetApiException {
        JuiceNetApiTouSchedule deviceTouSchedule;
        try {
            JsonObject jsonResponse = postApiCommand(ApiCommand.GET_SCHEDULE, token);

            deviceTouSchedule = new Gson().fromJson(jsonResponse, JuiceNetApiTouSchedule.class);
        } catch (JsonSyntaxException e) {
            throw new JuiceNetApiException("queryTOUSchedule from JuiceNet API failed, invalid JSON list.");
        } catch (IllegalStateException e) {
            throw new JuiceNetApiException("queryTOUSchedule from JuiceNet API failed - did not return valid array.");
        }

        return Objects.requireNonNull(deviceTouSchedule);
    }

    public void setOverride(String token, int energy_at_plugin, Long override_time, int energy_to_add)
            throws InterruptedException, JuiceNetApiException {
        Map<String, Object> params = new HashMap<>();

        params.put("energy_at_plugin", Integer.toString(energy_at_plugin));
        params.put("override_time", Long.toString(override_time));
        params.put("energy_to_add", Integer.toString(energy_to_add));

        postApiCommand(ApiCommand.SET_OVERRIDE, token, params);
    }

    public void setCurrentLimit(String token, int limit) throws InterruptedException, JuiceNetApiException {
        Map<String, Object> params = new HashMap<>();

        params.put("amperage", Integer.toString(limit));

        postApiCommand(ApiCommand.SET_OVERRIDE, token, params);
    }

    public JsonObject postApiCommand(ApiCommand cmd, @Nullable String token)
            throws InterruptedException, JuiceNetApiException {
        Map<String, Object> params = new HashMap<>();

        return postApiCommand(cmd, token, params);
    }

    public JsonObject postApiCommand(ApiCommand cmd, @Nullable String token, Map<String, Object> params)
            throws InterruptedException, JuiceNetApiException {
        Request request = httpClient.POST(cmd.uri);
        request.timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");

        // Add required params
        params.put("cmd", cmd.command);
        params.put("device_id", bridgeUID.getAsString());
        params.put("account_token", apiToken);

        if (token != null) {
            params.put("token", token);
        }

        JsonObject jsonResponse;
        try {
            request.content(new StringContentProvider(new Gson().toJson(params)), "application/json");
            ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new JuiceNetApiException(
                        cmd.command + "from JuiceNet API unsuccessful, please check configuation. (HTTP code :"
                                + response.getStatus() + ").");
            }

            String responseString = response.getContentAsString();
            logger.trace("{}", responseString);

            jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
            JsonElement successElement = jsonResponse.get("success");
            if (successElement == null) {
                throw new JuiceNetApiException(
                        cmd.command + " from JuiceNet API failed, 'success' element missing from response.");
            }
            boolean success = successElement.getAsBoolean();

            if (!success) {
                throw new JuiceNetApiException(cmd.command + " from JuiceNet API failed, please check configuration.");
            }
        } catch (IllegalStateException e) {
            throw new JuiceNetApiException(cmd.command + " from JuiceNet API failed, invalid JSON.");
        } catch (TimeoutException e) {
            throw new JuiceNetApiException(cmd.command + " from JuiceNet API timeout.");
        } catch (ExecutionException e) {
            throw new JuiceNetApiException(cmd.command + " from JuiceNet API execution issue.");
        }

        return jsonResponse;
    }
}
