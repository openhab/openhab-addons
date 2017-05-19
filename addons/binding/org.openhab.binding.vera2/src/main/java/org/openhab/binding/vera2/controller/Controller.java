package org.openhab.binding.vera2.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import org.openhab.binding.vera2.controller.Vera.json.Categorie;
import org.openhab.binding.vera2.controller.Vera.json.Device;
import org.openhab.binding.vera2.controller.Vera.json.Room;
import org.openhab.binding.vera2.controller.Vera.json.Scene;
import org.openhab.binding.vera2.controller.Vera.json.Sdata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Controller {
    private Logger log = LoggerFactory.getLogger(getClass());

    private String veraHost;
    private String veraPort;
    private Sdata sdata;

    public Controller(String veraHost, String veraPort) {
        this.veraHost = veraHost;
        this.veraPort = veraPort;

        updateSdata();
    }

    public Sdata getSdata() {
        if (sdata == null) {
            updateSdata();
        }
        return sdata;
    }

    private void denormalizeSdata(Sdata theSdata) {
        Map<String, Room> roomMap = new HashMap<>();
        for (Room i : theSdata.rooms) {
            roomMap.put(i.id, i);
        }
        Map<String, Categorie> categoryMap = new HashMap<>();
        for (Categorie i : theSdata.categories) {
            categoryMap.put(i.id, i);
        }
        Categorie controllerCat = new Categorie();
        controllerCat.name = "Controller";
        controllerCat.id = "0";
        categoryMap.put(controllerCat.id, controllerCat);
        ListIterator<Device> theIterator = theSdata.devices.listIterator();
        Device d;
        while (theIterator.hasNext()) {
            d = theIterator.next();
            d.uName = replaceTrash(d.name);
            if (d.room != null && roomMap.get(d.room) != null) {
                d.room = roomMap.get(d.room).name;
            } else {
                d.room = "no room";
            }
            if (d.category != null && categoryMap.get(d.category) != null) {
                d.categoryName = categoryMap.get(d.category).name;
            } else {
                d.categoryName = "<unknown>";
            }
        }
        ListIterator<Scene> theSecneIter = theSdata.scenes.listIterator();
        Scene theScene;
        while (theSecneIter.hasNext()) {
            theScene = theSecneIter.next();
            if (theScene.room != null && roomMap.get(theScene.room) != null) {
                theScene.room = roomMap.get(theScene.room).name;
            } else {
                theScene.room = "no room";
            }
        }
    }

    private String request(String request, String auth) {
        URL url;
        HttpURLConnection connection;
        String result;
        try {
            // log.info("Sending command: " + request);
            url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (auth != null) {
                connection.setRequestProperty("Authorization", "Basic " + auth);
            }
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            connection.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            br.close();
            result = buffer.toString();
        } catch (Exception e) {
            log.error("Error while get getJson: {}", request);
            e.printStackTrace();
            return null;
        }
        return result;
    }

    private boolean sendCommand(String request, String auth) {
        URL url;
        HttpURLConnection connection;
        String response = null;
        try {
            // log.info("Sending command: " + request);
            url = new URL(request);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (auth != null) {
                connection.setRequestProperty("Authorization", "Basic " + auth);
            }
            response = connection.getResponseMessage();
            log.info("Received response: {}", response);
        } catch (Exception e) {
            log.error("Error while sending command: {}", request);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String replaceTrash(String name) {
        name = name.replaceAll("1", "");
        name = name.replaceAll("2", "");
        name = name.replaceAll("3", "");
        name = name.replaceAll("4", "");
        name = name.replaceAll("5", "");
        name = name.replaceAll("6", "");
        name = name.replaceAll("7", "");
        name = name.replaceAll("8", "");
        name = name.replaceAll("9", "");
        name = name.replaceAll("0", "");
        name = name.replaceAll(":", "");
        name = name.replaceAll("/", "");
        name = name.replaceAll("-", "");
        name = name.replaceAll("/", "");
        name = name.replaceAll("  ", " ");
        name = name.trim();
        return name;
    }

    private String getUrl() {
        return "http://" + veraHost + ":" + veraPort + "/data_request";
    }

    private void setStatus(Device d, String status, String service) {
        sendCommand("http://" + veraHost + ":" + veraPort + "/data_request?id=action&DeviceNum=" + d.id + "&serviceId="
                + service + "&action=SetTarget&newTargetValue=" + status, null);
    }

    public Sdata updateSdata() {
        String result = request(getUrl() + "?id=sdata&output_format=json", null);
        Sdata theSdata = result == null ? null : new Gson().fromJson(result, Sdata.class);
        if (theSdata != null) {
            denormalizeSdata(theSdata);
        }
        sdata = theSdata;
        return theSdata;
    }

    public void turnDeviceOn(Device d) {
        d.status = "1";
        if (d.category.equals("7")) {
            setStatus(d, "1", "urn:micasaverde-com:serviceId:DoorLock1");
        } else {
            setStatus(d, "1", "urn:upnp-org:serviceId:SwitchPower1");
        }
    }

    public void turnDeviceOff(Device d) {
        d.status = "0";
        if (d.category.equals("7")) {
            setStatus(d, "0", "urn:micasaverde-com:serviceId:DoorLock1");
        } else {
            setStatus(d, "0", "urn:upnp-org:serviceId:SwitchPower1");
        }
    }

    public void setDimLevel(Device d, String level) {
        d.level = level;
        sendCommand(getUrl() + "?id=action&DeviceNum=" + d.id
                + "&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=" + level,
                null);
    }

    public void runScene(String id) {
        sendCommand(getUrl() + "?id=action&SceneNum=" + id
                + "&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene", null);
    }

    public boolean isConnected() {
        return sdata != null;
    }

    public Device getDevice(String deviceId) {
        for (Device d : sdata.devices) {
            if (d.id.equals(deviceId)) {
                return d;
            }
        }
        return null;
    }

    public Scene getScene(String sceneId) {
        for (Scene s : sdata.scenes) {
            if (s.id.equals(sceneId)) {
                return s;
            }
        }
        return null;
    }
}
