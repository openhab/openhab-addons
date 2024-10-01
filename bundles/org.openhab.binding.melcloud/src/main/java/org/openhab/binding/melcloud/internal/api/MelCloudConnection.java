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
package org.openhab.binding.melcloud.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.melcloud.internal.api.dto.Device;
import org.openhab.binding.melcloud.internal.api.dto.DeviceStatus;
import org.openhab.binding.melcloud.internal.api.dto.HeatpumpDeviceStatus;
import org.openhab.binding.melcloud.internal.api.dto.ListDevicesResponse;
import org.openhab.binding.melcloud.internal.api.dto.LoginClientResponse;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudCommException;
import org.openhab.binding.melcloud.internal.exceptions.MelCloudLoginException;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MelCloudConnection} Manage connection to Mitsubishi Cloud (MelCloud).
 *
 * @author Luca Calcaterra - Initial Contribution
 * @author Pauli Anttila - Refactoring
 * @author Wietse van Buitenen - Return all devices, added heatpump device
 */
@NonNullByDefault
public class MelCloudConnection {

    private static final String LOGIN_URL = "https://app.melcloud.com/Mitsubishi.Wifi.Client/Login/ClientLogin";
    private static final String DEVICE_LIST_URL = "https://app.melcloud.com/Mitsubishi.Wifi.Client/User/ListDevices";
    private static final String DEVICE_URL = "https://app.melcloud.com/Mitsubishi.Wifi.Client/Device";

    private static final int TIMEOUT_MILLISECONDS = 10000;

    // Gson objects are safe to share across threads and are somewhat expensive to construct. Use a single instance.
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    private final Logger logger = LoggerFactory.getLogger(MelCloudConnection.class);

    private boolean isConnected = false;
    private String sessionKey = "";

    public void login(String username, String password, int languageId)
            throws MelCloudCommException, MelCloudLoginException {
        setConnected(false);
        sessionKey = "";
        JsonObject jsonReq = new JsonObject();
        jsonReq.addProperty("Email", username);
        jsonReq.addProperty("Password", password);
        jsonReq.addProperty("Language", languageId);
        jsonReq.addProperty("AppVersion", "1.17.5.0");
        jsonReq.addProperty("Persist", false);
        jsonReq.addProperty("CaptchaResponse", (String) null);
        InputStream data = new ByteArrayInputStream(jsonReq.toString().getBytes(StandardCharsets.UTF_8));

        try {
            String loginResponse = HttpUtil.executeUrl("POST", LOGIN_URL, null, data, "application/json",
                    TIMEOUT_MILLISECONDS);
            logger.debug("Login response: {}", loginResponse);
            LoginClientResponse resp = Objects.requireNonNull(GSON.fromJson(loginResponse, LoginClientResponse.class));
            if (resp.getErrorId() != null) {
                String errorMsg = String.format("Login failed, error code: %s", resp.getErrorId());
                if (resp.getErrorMessage() != null) {
                    errorMsg = String.format("%s (%s)", errorMsg, resp.getErrorMessage());
                }
                throw new MelCloudLoginException(errorMsg);
            }
            sessionKey = resp.getLoginData().getContextKey();
            setConnected(true);
        } catch (IOException | JsonSyntaxException e) {
            throw new MelCloudCommException(String.format("Login error, reason: %s", e.getMessage()), e);
        }
    }

    public List<Device> fetchDeviceList() throws MelCloudCommException {
        assertConnected();
        try {
            String response = HttpUtil.executeUrl("GET", DEVICE_LIST_URL, getHeaderProperties(), null, null,
                    TIMEOUT_MILLISECONDS);
            logger.debug("Device list response: {}", response);
            List<Device> devices = new ArrayList<>();
            ListDevicesResponse[] buildings = GSON.fromJson(response, ListDevicesResponse[].class);
            Arrays.asList(buildings).forEach(building -> {
                if (building.getStructure().getDevices() != null) {
                    devices.addAll(building.getStructure().getDevices());
                }
                building.getStructure().getAreas().forEach(area -> {
                    if (area.getDevices() != null) {
                        devices.addAll(area.getDevices());
                    }
                });
                building.getStructure().getFloors().forEach(floor -> {
                    if (floor.getDevices() != null) {
                        devices.addAll(floor.getDevices());
                    }
                    floor.getAreas().forEach(area -> {
                        if (area.getDevices() != null) {
                            devices.addAll(area.getDevices());
                        }
                    });
                });
            });
            logger.debug("Found {} devices", devices.size());

            return devices;
        } catch (IOException | JsonSyntaxException e) {
            setConnected(false);
            throw new MelCloudCommException("Error occurred during device list poll", e);
        }
    }

    public DeviceStatus fetchDeviceStatus(int deviceId, int buildingId) throws MelCloudCommException {
        assertConnected();
        String url = DEVICE_URL + String.format("/Get?id=%d&buildingID=%d", deviceId, buildingId);
        try {
            String response = HttpUtil.executeUrl("GET", url, getHeaderProperties(), null, null, TIMEOUT_MILLISECONDS);
            logger.debug("Device status response: {}", response);
            return Objects.requireNonNull(GSON.fromJson(response, DeviceStatus.class));
        } catch (IOException | JsonSyntaxException e) {
            setConnected(false);
            throw new MelCloudCommException("Error occurred during device status fetch", e);
        }
    }

    public DeviceStatus sendDeviceStatus(DeviceStatus deviceStatus) throws MelCloudCommException {
        assertConnected();
        String content = GSON.toJson(deviceStatus, DeviceStatus.class);
        logger.debug("Sending device status: {}", content);
        InputStream data = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        try {
            String response = HttpUtil.executeUrl("POST", DEVICE_URL + "/SetAta", getHeaderProperties(), data,
                    "application/json", TIMEOUT_MILLISECONDS);
            logger.debug("Device status sending response: {}", response);
            return Objects.requireNonNull(GSON.fromJson(response, DeviceStatus.class));
        } catch (IOException | JsonSyntaxException e) {
            setConnected(false);
            throw new MelCloudCommException("Error occurred during device command sending", e);
        }
    }

    public HeatpumpDeviceStatus fetchHeatpumpDeviceStatus(int deviceId, int buildingId) throws MelCloudCommException {
        assertConnected();
        String url = DEVICE_URL + String.format("/Get?id=%d&buildingID=%d", deviceId, buildingId);
        try {
            String response = HttpUtil.executeUrl("GET", url, getHeaderProperties(), null, null, TIMEOUT_MILLISECONDS);
            logger.debug("Device heatpump status response: {}", response);
            return Objects.requireNonNull(GSON.fromJson(response, HeatpumpDeviceStatus.class));
        } catch (IOException | JsonSyntaxException e) {
            setConnected(false);
            throw new MelCloudCommException("Error occurred during heatpump device status fetch", e);
        }
    }

    public HeatpumpDeviceStatus sendHeatpumpDeviceStatus(HeatpumpDeviceStatus heatpumpDeviceStatus)
            throws MelCloudCommException {
        assertConnected();
        String content = GSON.toJson(heatpumpDeviceStatus, HeatpumpDeviceStatus.class);
        logger.debug("Sending heatpump device status: {}", content);
        InputStream data = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        try {
            String response = HttpUtil.executeUrl("POST", DEVICE_URL + "/SetAtw", getHeaderProperties(), data,
                    "application/json", TIMEOUT_MILLISECONDS);
            logger.debug("Device heatpump status sending response: {}", response);
            return Objects.requireNonNull(GSON.fromJson(response, HeatpumpDeviceStatus.class));
        } catch (IOException | JsonSyntaxException e) {
            setConnected(false);
            throw new MelCloudCommException("Error occurred during heatpump device command sending", e);
        }
    }

    public synchronized boolean isConnected() {
        return isConnected;
    }

    private synchronized void setConnected(boolean state) {
        isConnected = state;
    }

    private Properties getHeaderProperties() {
        Properties headers = new Properties();
        headers.put("X-MitsContextKey", sessionKey);
        return headers;
    }

    private void assertConnected() throws MelCloudCommException {
        if (!isConnected) {
            throw new MelCloudCommException("Not connected to MELCloud");
        }
    }
}
