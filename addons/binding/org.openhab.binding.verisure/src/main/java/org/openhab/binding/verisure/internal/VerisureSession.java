/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal;

import static org.openhab.binding.verisure.VerisureBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class performs the communication with the Verisure My Pages.
 *
 * @author Jarle Hjortland
 *
 */
public class VerisureSession {
    private HashMap<String, VerisureObjectJSON> verisureObjects = new HashMap<String, VerisureObjectJSON>();

    private Logger logger = LoggerFactory.getLogger(VerisureSession.class);
    private String authstring;
    private String csrf;
    private String pinCode;
    private VerisureAlarmJSON alarmData = null;
    private Gson gson = new GsonBuilder().create();

    private VerisureAlarmJSON doorData;

    private List<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArrayList<>();
    private HttpClient httpClient;

    public void initialize(String _authstring, String pinCode) throws Exception {
        logger.debug("VerisureSession:initialize");

        this.authstring = _authstring.substring(0);
        this.pinCode = pinCode;
        // Instantiate and configure the SslContextFactory
        SslContextFactory sslContextFactory = new SslContextFactory();
        this.httpClient = new HttpClient(sslContextFactory);
        this.httpClient.start();
        logIn();
        updateStatus();
    }

    private void updateStatus() {
        logger.debug("VerisureSession:updateStatus");
        try {
            updateAlarmStatus();
            updateVerisureObjects(CLIMATEDATA_PATH, VerisureSensorJSON[].class);
            updateVerisureObjects(DOORWINDOW_PATH, VerisureDoorWindowsJSON[].class);
            updateVerisureObjects(USERTRACKING_PATH, VerisureUserTrackingJSON[].class);
            updateVerisureObjects(SMARTPLUGDATA_PATH, VerisureSmartPlugJSON[].class);
        } catch (RuntimeException e) {
            logger.error("Failed in updatestatus", e);
        }
    }

    public synchronized void updateVerisureObjects(String urlString, Class<? extends VerisureObjectJSON[]> jsonClass) {

        try {
            VerisureObjectJSON[] objects = callJSONRest(urlString, jsonClass);
            logger.debug("REST Response ({})", (Object[]) objects);
            if (objects != null) {
                for (VerisureObjectJSON cse : objects) {
                    cse.setId(cse.getId().replaceAll("[^a-zA-Z0-9_]", "_"));
                    VerisureObjectJSON oldObj = verisureObjects.get(cse.getId());
                    if (oldObj == null || !oldObj.equals(cse)) {
                        verisureObjects.put(cse.getId(), cse);
                        notifyListeners(cse);
                    }
                }
            }
        } catch (Exception e) {
            logger.info("Failed to get all {}", urlString, e);
        }
    }

    private void notifyListeners(VerisureObjectJSON cse) {
        for (DeviceStatusListener listener : deviceStatusListeners) {
            listener.onDeviceStateChanged(cse);
        }
    }

    public VerisureObjectJSON getVerisureObject(String serialNumber) {
        return verisureObjects.get(serialNumber);
    }

    public HashMap<String, VerisureObjectJSON> getVerisureObjects() {
        return verisureObjects;
    }

    public State getAlarmStatus() {
        if (alarmData != null) {

            String status = alarmData.getStatus();
            if (status != null) {
                return new StringType(status);
            }
        }
        return UnDefType.UNDEF;
    }

    public State getAlarmStatusNumeric() {

        if (alarmData != null) {

            String status = alarmData.getStatus();
            DecimalType val;

            if (status != null) {
                if (status.equals("unarmed")) {
                    val = new DecimalType(0);
                    return val;
                } else if (status.equals("armedhome")) {
                    val = new DecimalType(1);
                    return val;
                } else if (status.equals("armedaway")) {
                    val = new DecimalType(2);
                    return val;
                }

            }

        }
        return UnDefType.UNDEF;
    }

    public State getAlarmChangerName() {

        if (alarmData != null) {
            String status = alarmData.getName();
            if (status != null) {
                return new StringType(status);
            }
        }
        return UnDefType.UNDEF;
    }

    public State getAlarmTimestamp() {

        if (alarmData != null) {
            String status = alarmData.getDate();
            if (status != null) {
                return new StringType(status);
            }
        }
        return UnDefType.UNDEF;
    }

    @SuppressWarnings("null")
    public synchronized void updateAlarmStatus() {
        // Get JSON
        try {
            VerisureAlarmJSON[] jsonObjects = callJSONRest(ALARMSTATUS_PATH, VerisureAlarmJSON[].class);
            if (jsonObjects != null) {
                // Print in Console
                for (VerisureAlarmJSON object : jsonObjects) {
                    if (object.getType().equals("ARM_STATE")) {
                        setAlarmData(object);
                    } else if (object.getType().equals("DOOR_LOCK")) {
                        setDoorData(object);
                    }
                }
            }
        } catch (Exception e) {
            logger.info("Failed to update alarm state", e);
        }

    }

    private <T> T callJSONRest(String url, Class<T> jsonClass) throws Exception {
        T result = null;
        ContentResponse httpResult = httpClient.GET(BASEURL + url + System.currentTimeMillis());
        logger.debug("HTTP Response ({}) Body:{}", httpResult.getStatus(), httpResult.getContentAsString());
        if (httpResult.getStatus() == HttpStatus.OK_200) {
            if (httpResult.getMediaType() != null && httpResult.getMediaType().equalsIgnoreCase("application/json")) {
                result = gson.fromJson(httpResult.getContentAsString(), jsonClass);
            } else {
                logger.info("Wrong response type {} for call to {}", httpResult.getMediaType(),
                        httpResult.getRequest().getURI());
            }
        }
        return result;
    }

    private void setAlarmData(VerisureAlarmJSON object) {
        if (!object.equals(alarmData)) {
            this.alarmData = object;
            notifyListeners(alarmData);
        }
    }

    private void setDoorData(VerisureAlarmJSON object) {
        if (!object.equals(doorData)) {
            this.doorData = object;
            this.verisureObjects.put(doorData.getId(), this.doorData);
            notifyListeners(doorData);
        }
    }

    private synchronized void logIn() throws Exception {
        logger.debug("Attempting to log in to mypages.verisure.com");

        String url = BASEURL + LOGON_SUF;
        String source = sendHTTPpost(url, authstring);

        // logger.debug(source);

        url = BASEURL + START_SUF;
        ContentResponse resp = httpClient.GET(url);
        source = resp.getContentAsString();
        csrf = getCsrfToken2(source);

        logger.trace("Got CSRF: {}", csrf);
        return;
    }

    private String sendHTTPpost(String urlString, String data) {
        try {
            org.eclipse.jetty.client.api.Request request = httpClient.newRequest(urlString).method(HttpMethod.POST);
            request.header("x-csrf-token", csrf).header("Accept", "application/json");
            request.content(new BytesContentProvider(data.getBytes()),
                    "application/x-www-form-urlencoded; charset=UTF-8");
            ContentResponse response = request.send();
            String content = response.getContentAsString();
            logger.debug("HTTP Response ({}) Body:{}", response.getStatus(), content);
            return content;
        } catch (Exception e) {
            logger.warn("had an exception {}", e.toString(), e);
        }
        return null;
    }

    private boolean areWeLoggedIn() {
        logger.debug("areWeLoggedIn() - Checking if we are logged in");
        String urlString = BASEURL + ALARMSTATUS_PATH;

        try {

            ContentResponse response = httpClient.newRequest(urlString).followRedirects(false).method(HttpMethod.HEAD)
                    .send();
            logger.debug("areWeLoggedIn({}) - ", response.getStatus());
            switch (response.getStatus()) {
                case 200:
                    // Redirection
                    logger.debug("Status code 200. Probably logged in");
                    return true;

                case 302:
                    // Redirection
                    logger.debug("Status code 302. Redirected. Probably not logged in");
                    logger.debug("areWeLoggedIn(302) - {}", response.getContentAsString());
                    return false;

                case 404:
                    // not found
                    logger.debug("Status code 404. Probably logged on too");
                    break;

                default:
                    logger.info("Status code {} body {}", response.getStatus(), response.getContentAsString());
                    break;
            }

        } catch (Exception e) {
            logger.warn("Error:", e);
        }

        return false;

    }

    public boolean refresh() {

        for (int i = 0; i < 3; i++) {
            if (areWeLoggedIn()) {
                updateStatus();
                return true;
            } else {
                try {
                    Thread.sleep(2000);
                    logIn();
                } catch (InterruptedException e) {
                    logger.warn("Interupted waiting for new login ", e);
                } catch (Exception e) {
                    logger.warn("Failed waiting for new login ", e);
                }

            }
        }

        return false;
    }

    public void dispose() {
        logger.debug("Should dispose of objects here in session");
        if (httpClient != null) {
            httpClient.destroy();
            httpClient = null;
        }
        if (deviceStatusListeners != null) {
            deviceStatusListeners.clear();
        }
        gson = null;

    }

    private String getCsrfToken2(String htmlText) {
        // Method should be replaced by regex logix
        String subString = null;
        try {
            int labelIndex = htmlText.indexOf("_csrf\" value=");

            subString = htmlText.substring(labelIndex + 14, labelIndex + 78);
            // logger.debug("QA test", "csrf-token = " + subString);
        } catch (IndexOutOfBoundsException e) {
            logger.debug("QA test", "Parsing Error = {}", e.toString(), e);
        }
        // Assert.assertNotNull("Null csrf-token ", subString);
        return subString;
    }

    public boolean disarmAlarm() {
        logger.debug("Sending command to disarm the alarm!");

        String url = BASEURL + ALARM_COMMAND;
        String data = "code=" + pinCode + "&state=DISARMED";

        sendHTTPpost(url, data);
        return true;
    }

    public boolean lockDoor(String door) {
        logger.debug("Sending command to lock the door!");

        String url = BASEURL + LOCK_COMMAND;
        String data = "code=" + pinCode + "&state=LOCKED&deviceLabel=" + door + "&_csrf=" + csrf;

        sendHTTPpost(url, data);
        return true;
    }

    public boolean armHomeAlarm() {
        logger.debug("Sending command to arm_home the alarm!");

        String url = BASEURL + ALARM_COMMAND;
        String data = "code=" + pinCode + "&state=ARMED_HOME";

        sendHTTPpost(url, data);
        return true;
    }

    public boolean armAwayAlarm() {
        logger.debug("Sending command to arm_away the alarm!");

        String url = BASEURL + ALARM_COMMAND;
        String data = "code=" + pinCode + "&state=ARMED_AWAY";

        sendHTTPpost(url, data);
        return true;
    }

    public VerisureAlarmJSON getDoorStatus() {
        // TODO Auto-generated method stub
        return doorData;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.tellstick.handler.TelldusBridgeHandlerIntf#registerDeviceStatusListener(org.openhab.binding.
     * tellstick.handler.DeviceStatusListener)
     */
    public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        return deviceStatusListeners.add(deviceStatusListener);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.tellstick.handler.TelldusBridgeHandlerIntf#unregisterDeviceStatusListener(org.openhab.binding
     * .tellstick.handler.DeviceStatusListener)
     */
    public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        boolean result = deviceStatusListeners.remove(deviceStatusListener);
        if (result) {
            // onUpdate();
        }
        return result;
    }

    public boolean unLockDoor(String door) {
        logger.debug("Sending command to disarm the alarm!");

        String url = BASEURL + LOCK_COMMAND;
        String data = "code=" + pinCode + "&state=UNLOCKED&deviceLabel=" + door + "&_csrf=" + csrf;

        sendHTTPpost(url, data);
        return true;
    }

    public VerisureAlarmJSON getAlarmObject() {
        return alarmData;
    }

}
