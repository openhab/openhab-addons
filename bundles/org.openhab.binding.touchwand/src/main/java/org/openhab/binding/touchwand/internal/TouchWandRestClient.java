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
package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TouchWandRestClient} is responsible for handling low level commands units TouchWand WonderFull hub
 * REST API interface
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandRestClient {

    private final Logger logger = LoggerFactory.getLogger(TouchWandRestClient.class);

    static CookieManager cookieManager = new CookieManager();

    private static final HttpMethod METHOD_GET = HttpMethod.GET;
    private static final HttpMethod METHOD_POST = HttpMethod.POST;

    private static final String CMD_LOGIN = "login";
    private static final String CMD_LIST_UNITS = "listunits";
    private static final String CMD_LIST_SCENARIOS = "listsencarios";
    private static final String CMD_UNIT_ACTION = "action";
    private static final String CMD_GET_UNIT_BY_ID = "getunitbyid";

    private static final String ACTION_SWITCH_OFF = "{\"id\":%s,\"value\":" + SWITCH_STATUS_OFF + "}";
    private static final String ACTION_SWITCH_ON = "{\"id\":%s,\"value\":" + SWITCH_STATUS_ON + "}";
    private static final String ACTION_SHUTTER_DOWN = "{\"id\":%s,\"value\":0,\"type\":\"height\"}";
    private static final String ACTION_SHUTTER_UP = "{\"id\":%s,\"value\":255,\"type\":\"height\"}";
    private static final String ACTION_SHUTTER_STOP = "{\"id\":%s,\"value\":0,\"type\":\"stop\"}";
    private static final String ACTION_SHUTTER_POSITION = "{\"id\":%s,\"value\":%s}";
    private static final String ACTION_DIMMER_POSITION = "{\"id\":%s,\"value\":%s}";
    private static final String ACTION_THERMOSTAT_ON = "{\"id\":%s,\"value\":" + THERMOSTAT_STATE_ON + "}";
    private static final String ACTION_THERMOSTAT_OFF = "{\"id\":%s,\"value\":" + THERMOSTAT_STATE_OFF + "}";
    private static final String ACTION_THERMOSTAT_MODE = "{\"id\":%s,\"ac-all\":\"mode\",\"fan\":\"%s\"}";
    private static final String ACTION_THERMOSTAT_FAN_LEVEL = "{\"id\":%s,\"ac-all\":\"fan\",\"fan\":\"%s\"}";
    private static final String ACTION_THERMOSTAT_TARGET_TEMPERATURE = "{\"id\":%s,\"ac-all\":\"temp\",\"temp_val\":%s}";

    private static final String CONTENT_TYPE_APPLICATION_JSON = MimeTypes.Type.APPLICATION_JSON.asString();

    private static final int REQUEST_TIMEOUT_SEC = 10;

    private static final Map<String, String> COMMAND_MAP = new HashMap<String, String>();
    static {
        COMMAND_MAP.put(CMD_LOGIN, "/auth/login?");
        COMMAND_MAP.put(CMD_LIST_UNITS, "/units/listUnits");
        COMMAND_MAP.put(CMD_LIST_SCENARIOS, "/scenarios/listScenarios");
        COMMAND_MAP.put(CMD_UNIT_ACTION, "/units/action");
        COMMAND_MAP.put(CMD_GET_UNIT_BY_ID, "/units/getUnitByID?");
    }

    private String touchWandIpAddr = "";
    private String touchWandPort = "";
    private boolean isConnected = false;
    private HttpClient httpClient;

    public TouchWandRestClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public final boolean connect(String user, String pass, String ipAddr, String port) {
        touchWandIpAddr = ipAddr;
        touchWandPort = port;
        isConnected = cmdLogin(user, pass, ipAddr);

        return isConnected;
    }

    private final boolean cmdLogin(String user, String pass, String ipAddr) {
        String encodedUser = URLEncoder.encode(user, StandardCharsets.UTF_8);
        String encodedPass = URLEncoder.encode(pass, StandardCharsets.UTF_8);
        String response = "";
        String command = buildUrl(CMD_LOGIN) + "user=" + encodedUser + "&" + "psw=" + encodedPass;
        response = sendCommand(command, METHOD_GET, "");

        return !"Unauthorized".equals(response);
    }

    public String cmdListUnits() {
        String response = "";
        if (isConnected) {
            String command = buildUrl(CMD_LIST_UNITS);
            response = sendCommand(command, METHOD_GET, "");
        }
        return response;
    }

    public String cmdGetUnitById(String id) {
        String response = "";

        if (isConnected) {
            String command = buildUrl(CMD_GET_UNIT_BY_ID) + "id=" + id;
            response = sendCommand(command, METHOD_GET, "");
        }
        return response;
    }

    public void cmdSwitchOnOff(String id, OnOffType onoff) {
        String action;

        if (OnOffType.OFF.equals(onoff)) {
            action = String.format(ACTION_SWITCH_OFF, id);
        } else {
            action = String.format(ACTION_SWITCH_ON, id);
        }
        cmdUnitAction(action);
    }

    public void cmdShutterUp(String id) {
        String action = String.format(ACTION_SHUTTER_UP, id);
        cmdUnitAction(action);
    }

    public void cmdShutterDown(String id) {
        String action = String.format(ACTION_SHUTTER_DOWN, id);
        cmdUnitAction(action);
    }

    public void cmdShutterPosition(String id, String position) {
        String action = String.format(ACTION_SHUTTER_POSITION, id, position);
        cmdUnitAction(action);
    }

    public void cmdShutterStop(String id) {
        String action = String.format(ACTION_SHUTTER_STOP, id);
        cmdUnitAction(action);
    }

    public void cmdDimmerPosition(String id, String position) {
        String action = String.format(ACTION_DIMMER_POSITION, id, position);
        cmdUnitAction(action);
    }

    public void cmdThermostatOnOff(String id, OnOffType onoff) {
        String action;

        if (OnOffType.OFF.equals(onoff)) {
            action = String.format(ACTION_THERMOSTAT_OFF, id);
        } else {
            action = String.format(ACTION_THERMOSTAT_ON, id);
        }
        cmdUnitAction(action);
    }

    public void cmdThermostatMode(String id, String mode) {
        String action = String.format(ACTION_THERMOSTAT_MODE, id, mode);
        cmdUnitAction(action);
    }

    public void cmdThermostatFanLevel(String id, String fanLevel) {
        String action = String.format(ACTION_THERMOSTAT_FAN_LEVEL, id, fanLevel);
        cmdUnitAction(action);
    }

    public void cmdThermostatTargetTemperature(String id, String targetTemperature) {
        String action = String.format(ACTION_THERMOSTAT_TARGET_TEMPERATURE, id, targetTemperature);
        cmdUnitAction(action);
    }

    private String cmdUnitAction(String action) {
        String response = "";
        if (isConnected) {
            String command = buildUrl(CMD_UNIT_ACTION);
            response = sendCommand(command, METHOD_POST, action);
        }
        return response;
    }

    private String buildUrl(String command) {
        return "http://" + touchWandIpAddr + ":" + touchWandPort + COMMAND_MAP.get(command);
    }

    private synchronized String sendCommand(String command, HttpMethod method, String content) {
        ContentResponse response;
        Request request;

        URL url = null;
        try {
            url = new URL(command);
        } catch (MalformedURLException e) {
            logger.warn("Error building URL {} : {}", command, e.getMessage());
            return "";
        }

        request = httpClient.newRequest(url.toString()).timeout(REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS).method(method);
        if (method.equals(METHOD_POST) && (!content.isEmpty())) {
            ContentProvider contentProvider = new StringContentProvider(CONTENT_TYPE_APPLICATION_JSON, content,
                    StandardCharsets.UTF_8);
            request = request.content(contentProvider);
        }

        try {
            response = request.send();
            return response.getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error opening connection to {} : {} ", touchWandIpAddr, e.getMessage());
        }
        return "";
    }
}
