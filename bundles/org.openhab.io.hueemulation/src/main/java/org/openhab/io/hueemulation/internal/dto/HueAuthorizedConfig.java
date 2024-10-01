/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.dto;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Hue API config object
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueAuthorizedConfig extends HueUnauthorizedConfig {
    public String uuid = ""; // Example: 5673dfa7-272c-4315-9955-252cdd86131c

    public String timeformat = "24h";
    public String timezone = ZonedDateTime.now().getOffset().getId().replace("Z", "+00:00");
    public String UTC = "2018-11-10T15:24:23";
    public String localtime = "2018-11-10T16:24:23";

    public String devicename = "openHAB";

    public String fwversion = "0x262e0500";

    public boolean rfconnected = true;
    public int zigbeechannel = 15;
    public boolean linkbutton = false;
    public transient boolean createNewUserOnEveryEndpoint = false;
    public int panid = 19367;

    public boolean dhcp = true;
    public String gateway = "192.168.0.1";
    public String ipaddress = ""; // Example: 192.168.0.46
    public String netmask = ""; // Example: 255.255.255.0
    public int networkopenduration = 60;

    public String proxyaddress = "none";
    public int proxyport = 0;

    public final Map<String, HueUserAuth> whitelist = new TreeMap<>();

    public void makeV1bridge() {
        apiversion = "1.16.0";
        datastoreversion = "60";
        modelid = "BSB001";
        swversion = "01041302";
        fwversion = "0x262e0500";
    }

    public void makeV2bridge() {
        apiversion = "1.22.0";
        datastoreversion = "60";
        modelid = "BSB002";
        swversion = "1901181309";
        fwversion = "0x262e0500";
    }

    /**
     * Return a json serializer that behaves like the default one, but updates the UTC and localtime fields
     * before each serialization.
     */
    @NonNullByDefault({})
    public static class Serializer implements JsonSerializer<HueAuthorizedConfig> {
        static class HueAuthorizedConfigHelper extends HueAuthorizedConfig {

        }

        @Override
        public JsonElement serialize(HueAuthorizedConfig src, Type typeOfSrc, JsonSerializationContext context) {
            src.UTC = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            src.localtime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return context.serialize(src, HueAuthorizedConfigHelper.class);
        }
    }
}
