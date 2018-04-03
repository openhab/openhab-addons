/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler.BSTKeys;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler.KeyState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WSHelper} class communicates with the REST API of the speaker.
 *
 * @author syracom - Initial contribution
 */
public class WSHelper implements WSHelperInterface {
    private static final String KEY_PATH = "/key";
    private Logger logger = LoggerFactory.getLogger(WSHelper.class);
    private String deviceUrl;

    public enum MESSAGE_TYPE {
        POST,
        GET
    }

    public WSHelper(String url) {
        deviceUrl = url;
    }

    @Override
    public synchronized String pressAndReleaseButtonOnSpeaker(BSTKeys keyIdentifier) {
        return actionButtonOnSpeaker(keyIdentifier, KeyState.PRESS)
                + actionButtonOnSpeaker(keyIdentifier, KeyState.RELEASE);
    }

    private String actionButtonOnSpeaker(BSTKeys keyIdentifier, KeyState keyState) {
        String message = "<key state=\"" + keyState.getValue() + "\" sender=\"Gabbo\">" + keyIdentifier + "</key>";
        String response = sendMessage(message, KEY_PATH, MESSAGE_TYPE.POST);
        return checkForErrorFromSpeaker(response);
    }

    private String checkForErrorFromSpeaker(String response) {
        if (response.startsWith("<error")) {
            return "error";
        }
        return response;
    }

    @Override
    public synchronized String setVolume(PercentType num) {
        String message = "<volume>" + num + "</volume>";
        return checkForErrorFromSpeaker(sendMessage(message, "/volume", MESSAGE_TYPE.POST));
    }

    private String sendMessage(String message, String key, MESSAGE_TYPE type) {
        String url = deviceUrl + key;

        BufferedReader in = null;
        StringBuffer response = new StringBuffer();
        try {
            URL urlObject = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            if (type == MESSAGE_TYPE.POST) {
                configurePostMessage(connection);
                sendPostMessage(connection, message);
            } else {
                configureGetMessage(connection);
            }
            int responseCode = connection.getResponseCode();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            logger.debug("IOException on sending message to webservice: ", message);
            response.append("<error");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                logger.debug("Exception on closing BufferedReader ");
                return "error";
            }
        }
        return checkForErrorFromSpeaker(response.toString());
    }

    private void configurePostMessage(HttpURLConnection connection) {
        connection.setDoInput(true);
        connection.setDoOutput(true);
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            logger.debug("Error in GET protocol");
        }
        connection.setRequestProperty("Content-Type", "text/xml");
    }

    private void sendPostMessage(HttpURLConnection connection, String message) {
        try {
            PrintWriter pr = new PrintWriter(connection.getOutputStream());
            pr.write(message);
            pr.flush();
            pr.close();
        } catch (IOException e) {
            logger.debug("Error on writing POST Message: ", message);
        }

    }

    private void configureGetMessage(HttpURLConnection con) {
        try {
            con.setRequestMethod("GET");
        } catch (ProtocolException e) {
            logger.debug("Error in GET protocol");
        }
    }

    @Override
    public synchronized String setBass(DecimalType num) {
        String message = "<bass>" + num + "</bass>";
        return checkForErrorFromSpeaker(sendMessage(message, "/bass", MESSAGE_TYPE.POST));
    }

    @Override
    public synchronized String get(String service) {
        String response = sendMessage("", service, MESSAGE_TYPE.GET);
        return checkForErrorFromSpeaker(response);
    }

    @Override
    public synchronized String selectAUX() {
        String message = "<ContentItem source=\"AUX\" sourceAccount=\"AUX\"></ContentItem>";
        return checkForErrorFromSpeaker(sendMessage(message, "/select", MESSAGE_TYPE.POST));
    }

    @Override
    public synchronized String selectBluetooth() {
        String message = "<ContentItem source=\"BLUETOOTH\"></ContentItem>";
        return checkForErrorFromSpeaker(sendMessage(message, "/select", MESSAGE_TYPE.POST));
    }

}
