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

package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TouchWandRestClient} is responsible for handling low level commands units Touchwand WonderFull hub
 * REST API interface
 *
 * @author Roie Geron - Initial contribution
 */

public class TouchWandRestClient {

    private final Logger logger = LoggerFactory.getLogger(TouchWandRestClient.class);

    static CookieManager cookieManager = new CookieManager();

    private String touchWandIpAddr;
    private String touchWandPort;
    private boolean isConnected = false;

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";

    public static final String CMD_LOGIN = "login";
    public static final String CMD_LIST_UNITS = "listunits";
    public static final String CMD_LIST_SCENARIOS = "listsencarios";
    public static final String CMD_UNIT_ACTION_ON = "uniton";
    public static final String CMD_UNIT_ACTION_OFF = "unitoff";
    public static final String CMD_GET_UNIT_BY_ID = "getunitbyid";

    public static final String CMD_OFF = "{\"id\":%s ,\"value\":" + SWITCH_STATUS_OFF + "}";
    public static final String CMD_ON = "{\"id\":%s ,\"value\":" + SWITCH_STATUS_ON + "}";

    private static final int timeout = 10000; // 10 seconds
    private Map<String, String> commandmap = new HashMap<String, String>();

    public TouchWandRestClient() {

        commandmap.put(CMD_LOGIN, "/auth/login?");
        commandmap.put(CMD_LIST_UNITS, "/units/listUnits");
        commandmap.put(CMD_LIST_SCENARIOS, "/scenarios/listScenarios");
        commandmap.put(CMD_UNIT_ACTION_ON, "/units/action");
        commandmap.put(CMD_UNIT_ACTION_OFF, "/units/action");
        commandmap.put(CMD_GET_UNIT_BY_ID, "/units/getUnitByID?");

        CookieHandler.setDefault(cookieManager);

    }

    public final boolean connect(String user, String pass, String ipAddr, String port) {

        touchWandIpAddr = ipAddr;
        touchWandPort = port;
        isConnected = cmdLogin(user, pass, ipAddr);

        return isConnected;

    }

    private final boolean cmdLogin(String user, String pass, String ipAddr) {

        String command = buildUrl(CMD_LOGIN) + "user=" + user + "&" + "psw=" + pass;
        String response = sendCommand(command, METHOD_GET, null);

        if (response != null) {
            return true;
        }

        return false;
    }

    public String cmdListUnits() {

        String command = buildUrl(CMD_LIST_UNITS);
        String response = sendCommand(command, METHOD_GET, null);

        return response;
    }

    public String cmdGetUnitById(String id) {

        String command = buildUrl(CMD_GET_UNIT_BY_ID) + "id=" + id;
        String response = sendCommand(command, METHOD_GET, null);

        return response;

    }

    public String cmdUnitAction(String id, String action) {

        String command = buildUrl(CMD_UNIT_ACTION_ON);
        String unitAction = null;
        if (action.equals(OnOffType.OFF.toString())) {
            unitAction = String.format(CMD_OFF, id);
        } else if (action.equals(OnOffType.ON.toString())) {
            unitAction = String.format(CMD_ON, id);
        }

        String response = sendCommand(command, METHOD_POST, unitAction);

        return response;
    }

    private String buildUrl(String commad) {

        String url = "http://" + touchWandIpAddr + ":" + touchWandPort + commandmap.get(commad);

        return url;

    }

    private String sendCommand(String command, String Method, String content) {

        HttpURLConnection connection;
        String response = null;

        try {
            URL url = new URL(command);

            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(timeout);
            if (Method.equals("POST")) {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
            } else if (Method.equals("GET")) {
                connection.setRequestMethod("GET");
            }

            connection.connect();

            if (Method.equals("POST") && (content != null)) {
                byte[] postDataBytes = content.toString().getBytes("UTF-8");
                connection.getOutputStream().write(postDataBytes);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            response = stringBuilder.toString();
            logger.debug("Return string {}", response);

        } catch (IOException e) {
            logger.warn("Error open connecton to {} : {} ", touchWandIpAddr, e.getMessage());
        }

        return response;

    }

}
