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
package org.openhab.binding.shelly.internal.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ShellyApiHelper} define various constants used by the Shelly API and also little helper functions (e.g.
 * building an URL).
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyApiHelper {
    public static final String SHELLY_API_MIN_FWVERSION = "v1.5.0";

    public static final String SHELLY_NULL_URL = "null";
    public static final String SHELLY_URL_DEVINFO = "/shelly";
    public static final String SHELLY_URL_STATUS = "/status";
    public static final String SHELLY_URL_SETTINGS = "/settings";
    public static final String SHELLY_URL_SETTINGS_AP = "/settings/ap";
    public static final String SHELLY_URL_SETTINGS_STA = "/settings/sta";
    public static final String SHELLY_URL_SETTINGS_LOGIN = "/settings/sta";
    public static final String SHELLY_URL_SETTINGS_CLOUD = "/settings/cloud";
    public static final String SHELLY_URL_LIST_IR = "/ir/list";
    public static final String SHELLY_URL_SEND_IR = "/ir/emit";

    public static final String SHELLY_URL_SETTINGS_RELAY = "/settings/relay";
    public static final String SHELLY_URL_STATUS_RELEAY = "/status/relay";
    public static final String SHELLY_URL_CONTROL_RELEAY = "/relay";

    public static final String SHELLY_URL_SETTINGS_EMETER = "/settings/emeter";
    public static final String SHELLY_URL_STATUS_EMETER = "/emeter";
    public static final String SHELLY_URL_DATA_EMETER = "/emeter/{0}/em_data.csv";

    public static final String SHELLY_URL_CONTROL_ROLLER = "/roller";
    public static final String SHELLY_URL_SETTINGS_ROLLER = "/settings/roller";

    public static final String SHELLY_URL_SETTINGS_LIGHT = "/settings/light";
    public static final String SHELLY_URL_STATUS_LIGHT = "/light";
    public static final String SHELLY_URL_CONTROL_LIGHT = "/light";

    public static final String SHELLY_URL_SETTINGS_DIMMER = "/settings/light";

    public static final String SHELLY_CALLBACK_URI = "/shelly/event";
    public static final String EVENT_TYPE_RELAY = "relay";
    public static final String EVENT_TYPE_ROLLER = "roller";
    public static final String EVENT_TYPE_SENSORDATA = "sensordata";
    public static final String EVENT_TYPE_LIGHT = "light";

    public static final String SHELLY_IR_CODET_STORED = "stored";
    public static final String SHELLY_IR_CODET_PRONTO = "pronto";
    public static final String SHELLY_IR_CODET_PRONTO_HEX = "pronto_hex";

    public static int SHELLY_API_TIMEOUT = 2500;

    public static final String OPENHAB_HTTP_PORT = "OPENHAB_HTTP_PORT";
    public static final String OPENHAB_DEF_PORT = "8080";

    public static final String HTTP_GET = "GET";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_HEADER_AUTH = "Authorization";
    public static final String HTTP_AUTH_TYPE_BASIC = "Basic";
    public static final String HTTP_401_UNAUTHORIZED = "401 Unauthorized";
    public static final String CONTENT_TYPE_XML = "text/xml; charset=UTF-8";
    public static final String CHARSET_UTF8 = "utf-8";

    protected static String buildSetEventUrl(String localIp, String localPort, String deviceName, Integer index,
            String deviceType, String urlParm) throws IOException {
        return SHELLY_URL_SETTINGS + "/" + deviceType + "/" + index + "?" + urlParm + "="
                + buildCallbackUrl(localIp, localPort, deviceName, index, deviceType, urlParm);
    }

    protected static String buildCallbackUrl(String localIp, String localPort, String deviceName, Integer index,
            String type, String parameter) throws IOException {
        String url = "http://" + localIp + ":" + localPort + SHELLY_CALLBACK_URI + "/" + deviceName + "/" + type + "/"
                + index + "?type=" + StringUtils.substringBefore(parameter, "_url");
        return urlEncode(url);
    }

    protected static String urlEncode(String input) throws IOException {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IOException(
                    "Unsupported encoding format: " + StandardCharsets.UTF_8.toString() + ", input=" + input);
        }
    }
}
