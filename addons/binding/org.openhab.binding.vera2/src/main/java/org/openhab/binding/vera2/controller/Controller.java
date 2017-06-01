/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera2.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.vera2.controller.json.Device;
import org.openhab.binding.vera2.controller.json.Room;
import org.openhab.binding.vera2.controller.json.Scene;
import org.openhab.binding.vera2.controller.json.Sdata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link Controller} class used to connect to the Vera
 *
 * @author Dmitriy Ponomarev
 */
public class Controller {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final String veraHost;
    private final String veraPort;
    private final Gson gson;

    private Sdata sdata;

    public Controller(String veraHost, String veraPort) {
        this.veraHost = veraHost;
        this.veraPort = veraPort;
        this.gson = new Gson();

        updateSdata();
    }

    public Sdata getSdata() {
        if (sdata == null) {
            updateSdata();
        }
        return sdata;
    }

    private void denormalizeSdata(Sdata data) {
        Map<String, Room> roomMap = new HashMap<>();
        for (Room r : data.getRooms()) {
            roomMap.put(r.getId(), r);
        }
        for (Device d : data.getDevices()) {
            d.setName(replaceTrash(d.getName()));
            if (d.getRoom() != null && roomMap.get(d.getRoom()) != null) {
                d.setRoomName(roomMap.get(d.getRoom()).getName());
            } else {
                d.setRoomName("no room");
            }
            try {
                d.setCategoryType(CategoryType.values()[Integer.parseInt(d.getCategory())]);
            } catch (IndexOutOfBoundsException e) {
                logger.warn("Unknown category type: {}", d.getCategory());
            }
        }
        for (Scene s : data.getScenes()) {
            if (s.getRoom() != null && roomMap.get(s.getRoom()) != null) {
                s.setRoomName(roomMap.get(s.getRoom()).getName());
            } else {
                s.setRoomName("no room");
            }
        }
    }

    private String request(String request) throws IOException {
        String result;
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connection.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            buffer.append(line).append("\n");
        }
        br.close();
        result = buffer.toString();
        return result;
    }

    private boolean sendCommand(String request) {
        try {
            URL url = new URL(request);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.getResponseMessage();
        } catch (IOException e) {
            logger.warn("Error while get getJson: {}, {}", request, e);
            return false;
        }
        return true;
    }

    private String replaceTrash(String name) {
        String sanitizedName = name.replaceAll("[0-9:/-]", "");
        sanitizedName = name.replaceAll("\\s+", " ");
        return sanitizedName.trim();
    }

    private String getUrl() {
        return "http://" + veraHost + ":" + veraPort + "/data_request";
    }

    private void setStatus(Device d, String status) {
        d.setStatus(status);
        String service = "urn:upnp-org:serviceId:SwitchPower1";
        if ("7".equals(d.getCategory())) {
            service = "urn:micasaverde-com:serviceId:DoorLock1";
        }
        sendCommand(getUrl() + "?id=action&DeviceNum=" + d.getId() + "&serviceId=" + service
                + "&action=SetTarget&newTargetValue=" + status);
    }

    public void turnDeviceOn(Device d) {
        setStatus(d, "1");
    }

    public void turnDeviceOff(Device d) {
        setStatus(d, "0");
    }

    public void setDimLevel(Device d, String level) {
        d.setLevel(level);
        sendCommand(getUrl() + "?id=action&DeviceNum=" + d.getId()
                + "&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=" + level);
    }

    public void runScene(String id) {
        sendCommand(getUrl() + "?id=action&SceneNum=" + id
                + "&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene");
    }

    public void updateSdata() {
        try {
            String result = request(getUrl() + "?id=sdata&output_format=json");
            Sdata data = gson.fromJson(result, Sdata.class);
            denormalizeSdata(data);
            sdata = data;
        } catch (IOException e) {
            logger.warn("Failed to update sdata: {}", e.getMessage());
        }
    }

    public boolean isConnected() {
        return sdata != null;
    }

    public Device getDevice(String deviceId) {
        for (Device d : sdata.getDevices()) {
            if (deviceId.equals(d.getId())) {
                return d;
            }
        }
        return null;
    }

    public Scene getScene(String sceneId) {
        for (Scene s : sdata.getScenes()) {
            if (sceneId.equals(s.getId())) {
                return s;
            }
        }
        return null;
    }
}
