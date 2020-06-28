/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.elgatokeylight.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.io.net.http.HttpUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Simple tool for talking to the Elgato Key Light API as documented <a href=
 * "https://github.com/bitfocus/companion-module-requests/issues/198">here</a>.
 *
 * @author Gunnar Wagenknecht - Initial contribution
 */
public class ElgatoKeyLight {

    public static class LightStatus {

        public boolean on;
        public int brightness;
        public int temperature;
    }

    private static final String TEMPERATURE = "temperature";
    private static final String BRIGHTNESS = "brightness";
    private static final String ON = "on";

    private static final String DISPLAY_NAME = "displayName";
    private static final String SERIAL_NUMBER = "serialNumber";
    private static final String PRODUCT_NAME = "productName";

    private final String ip;
    private final JsonParser parser;
    private final Gson gson;
    private volatile JsonObject accessoryInfo;
    private final int port;

    public ElgatoKeyLight(final String ip, final int port) {
        this.ip = ip;
        this.port = port;
        parser = new JsonParser();
        gson = new GsonBuilder().create();
    }

    private JsonObject createLightStatus(final Boolean on, final Integer brightness, final Integer temperature) {
        JsonObject desiredLightState = new JsonObject();
        desiredLightState.add("numberOfLights", new JsonPrimitive(1));
        JsonArray lights = new JsonArray(1);
        desiredLightState.add("lights", lights);
        JsonObject light = new JsonObject();
        lights.add(light);
        if (on != null) {
            light.addProperty(ON, on ? 1 : 0);
        }
        if (brightness != null) {
            light.addProperty(BRIGHTNESS, brightness);
        }
        if (temperature != null) {
            light.addProperty(TEMPERATURE, temperature);
        }
        return desiredLightState;
    }

    private JsonObject fetchAccessoryInfo() throws IOException {
        String accessoryInfoResponse = HttpUtil.executeUrl(HttpMethod.GET.asString(), getUrl("/elgato/accessory-info"),
                30);
        if (accessoryInfoResponse == null) {
            return null;
        }

        JsonElement accessoryInfo = parser.parse(accessoryInfoResponse);
        if (!accessoryInfo.isJsonObject()) {
            return null;
        }

        return (JsonObject) accessoryInfo;
    }

    private JsonObject fetchLightStatus() throws IOException {
        String lightStatusResponse = HttpUtil.executeUrl(HttpMethod.GET.asString(), getUrl("/elgato/lights"), 30);
        if (lightStatusResponse == null) {
            return null;
        }

        JsonElement lightStatus = parser.parse(lightStatusResponse);
        if (!lightStatus.isJsonObject()) {
            return null;
        }

        return (JsonObject) lightStatus;
    }

    private JsonObject getAccessoryInfo() throws IOException {
        JsonObject accessoryInfo = this.accessoryInfo;
        while (accessoryInfo == null) {
            accessoryInfo = this.accessoryInfo = fetchAccessoryInfo();
        }
        return accessoryInfo;
    }

    public String getIp() {
        return ip;
    }

    private String getUrl(final String path) {
        return "http://" + ip + ":" + port + path;
    }

    private LightStatus paresLightStatus(final JsonObject lightStatus) {
        if ((lightStatus != null) && lightStatus.has("lights")) {
            JsonArray lights = lightStatus.get("lights").getAsJsonArray();
            if (lights.size() > 0) {
                JsonObject light = lights.get(0).getAsJsonObject();
                if (light.has(ON)) {
                    LightStatus status = new LightStatus();
                    status.on = light.getAsJsonObject(ON).getAsInt() == 1 ? true : false;
                    status.brightness = light.has(BRIGHTNESS) ? light.getAsJsonObject(BRIGHTNESS).getAsInt() : 0;
                    status.temperature = light.has(TEMPERATURE) ? light.getAsJsonObject(TEMPERATURE).getAsInt() : 0;
                    return status;
                }
            }
        }
        return null;
    }

    private JsonObject putLightStatus(final JsonObject lightStatus) throws IOException {
        String lightStatusResponse = HttpUtil.executeUrl(HttpMethod.PUT.asString(), getUrl("/elgato/lights"),
                new ByteArrayInputStream(gson.toJson(lightStatus).getBytes(StandardCharsets.UTF_8)), "application/json",
                30);
        if (lightStatusResponse == null) {
            return null;
        }

        JsonElement newLightStatus = parser.parse(lightStatusResponse);
        if (!newLightStatus.isJsonObject()) {
            return null;
        }

        return (JsonObject) newLightStatus;
    }

    public String readDisplayName() throws IOException {
        JsonObject accessoryInfo = getAccessoryInfo();

        if ((accessoryInfo != null) && accessoryInfo.has(DISPLAY_NAME)) {
            return accessoryInfo.get(DISPLAY_NAME).getAsString();
        }

        return null;
    }

    public String readFirmwareVersion() throws IOException {
        JsonObject accessoryInfo = getAccessoryInfo();

        if ((accessoryInfo != null) && accessoryInfo.has("firmwareVersion")) {
            return accessoryInfo.get("firmwareVersion").getAsString();
        }

        return null;
    }

    public String readHardwareBoardType() throws IOException {
        JsonObject accessoryInfo = getAccessoryInfo();

        if ((accessoryInfo != null) && accessoryInfo.has("hardwareBoardType")) {
            return accessoryInfo.get("hardwareBoardType").getAsString();
        }

        return null;
    }

    public LightStatus readLightStatus() throws IOException {
        JsonObject lightStatus = fetchLightStatus(); // fetch (don't cache)

        return paresLightStatus(lightStatus);
    }

    public String readProductName() throws IOException {
        JsonObject accessoryInfo = getAccessoryInfo();

        if ((accessoryInfo != null) && accessoryInfo.has(PRODUCT_NAME)) {
            return accessoryInfo.get(PRODUCT_NAME).getAsString();
        }

        return null;
    }

    public String readSerialNumber() throws IOException {
        JsonObject accessoryInfo = getAccessoryInfo();

        if ((accessoryInfo != null) && accessoryInfo.has(SERIAL_NUMBER)) {
            return accessoryInfo.get(SERIAL_NUMBER).getAsString();
        }

        return null;
    }

    public LightStatus switchTo(final boolean on, final int brightness) throws IOException {
        JsonObject desiredLightState = createLightStatus(on, brightness, null /* temperature */);
        JsonObject newLightStatus = putLightStatus(desiredLightState);
        return paresLightStatus(newLightStatus);
    }

    public LightStatus writeTemperature(final int temperature) throws IOException {
        JsonObject desiredLightState = createLightStatus(null /* power */, null /* brightness */, temperature);
        JsonObject newLightStatus = putLightStatus(desiredLightState);
        return paresLightStatus(newLightStatus);
    }
}
