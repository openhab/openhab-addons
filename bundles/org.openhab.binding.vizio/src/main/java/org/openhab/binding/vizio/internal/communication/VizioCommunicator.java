/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.vizio.internal.communication;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.vizio.internal.VizioException;
import org.openhab.binding.vizio.internal.dto.PutResponse;
import org.openhab.binding.vizio.internal.dto.app.CurrentApp;
import org.openhab.binding.vizio.internal.dto.applist.VizioAppConfig;
import org.openhab.binding.vizio.internal.dto.audio.Audio;
import org.openhab.binding.vizio.internal.dto.input.CurrentInput;
import org.openhab.binding.vizio.internal.dto.inputlist.InputList;
import org.openhab.binding.vizio.internal.dto.pairing.PairingComplete;
import org.openhab.binding.vizio.internal.dto.pairing.PairingStart;
import org.openhab.binding.vizio.internal.dto.power.PowerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link VizioCommunicator} class contains methods for accessing the HTTP interface of Vizio TVs
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class VizioCommunicator {
    private final Logger logger = LoggerFactory.getLogger(VizioCommunicator.class);

    private static final String AUTH_HEADER = "AUTH";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String JSON_VALUE = "{\"VALUE\": %s}";

    private final HttpClient httpClient;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private final String authToken;
    private final String urlPowerMode;
    private final String urlCurrentAudio;
    private final String urlCurrentInput;
    private final String urlInputList;
    private final String urlChangeVolume;
    private final String urlCurrentApp;
    private final String urlLaunchApp;
    private final String urlKeyPress;
    private final String urlStartPairing;
    private final String urlSubmitPairingCode;

    public VizioCommunicator(HttpClient httpClient, String host, int port, String authToken) {
        this.httpClient = httpClient;
        this.authToken = authToken;

        final String baseUrl = "https://" + host + ":" + port;
        urlPowerMode = baseUrl + "/state/device/power_mode";
        urlCurrentAudio = baseUrl + "/menu_native/dynamic/tv_settings/audio";
        urlChangeVolume = baseUrl + "/menu_native/dynamic/tv_settings/audio/volume";
        urlCurrentInput = baseUrl + "/menu_native/dynamic/tv_settings/devices/current_input";
        urlInputList = baseUrl + "/menu_native/dynamic/tv_settings/devices/name_input";
        urlCurrentApp = baseUrl + "/app/current";
        urlLaunchApp = baseUrl + "/app/launch";
        urlKeyPress = baseUrl + "/key_command/";
        urlStartPairing = baseUrl + "/pairing/start";
        urlSubmitPairingCode = baseUrl + "/pairing/pair";
    }

    /**
     * Get the current power state of the Vizio TV
     *
     * @return A PowerMode response object
     * @throws VizioException
     *
     */
    public PowerMode getPowerMode() throws VizioException {
        return fromJson(getCommand(urlPowerMode), PowerMode.class);
    }

    /**
     * Get the current audio settings of the Vizio TV
     *
     * @return An Audio response object
     * @throws VizioException
     *
     */
    public Audio getCurrentAudioSettings() throws VizioException {
        return fromJson(getCommand(urlCurrentAudio), Audio.class);
    }

    /**
     * Change the volume of the Vizio TV
     *
     * @param the command JSON for the desired volue
     * @return A PutResponse response object
     * @throws VizioException
     *
     */
    public PutResponse changeVolume(String commandJSON) throws VizioException {
        return fromJson(putCommand(urlChangeVolume, commandJSON), PutResponse.class);
    }

    /**
     * Get the currently selected input of the Vizio TV
     *
     * @return A CurrentInput response object
     * @throws VizioException
     *
     */
    public CurrentInput getCurrentInput() throws VizioException {
        return fromJson(getCommand(urlCurrentInput), CurrentInput.class);
    }

    /**
     * Change the currently selected input of the Vizio TV
     *
     * @param the command JSON for the selected input
     * @return A PutResponse response object
     * @throws VizioException
     *
     */
    public PutResponse changeInput(String commandJSON) throws VizioException {
        return fromJson(putCommand(urlCurrentInput, commandJSON), PutResponse.class);
    }

    /**
     * Get the list of available source inputs from the Vizio TV
     *
     * @return An InputList response object
     * @throws VizioException
     *
     */
    public InputList getSourceInputList() throws VizioException {
        return fromJson(getCommand(urlInputList), InputList.class);
    }

    /**
     * Get the id of the app currently running on the Vizio TV
     *
     * @return A CurrentApp response object
     * @throws VizioException
     *
     */
    public CurrentApp getCurrentApp() throws VizioException {
        return fromJson(getCommand(urlCurrentApp), CurrentApp.class);
    }

    /**
     * Launch a given streaming app on the Vizio TV
     *
     * @param the VizioAppConfig data for the app to launch
     * @return A PutResponse response object
     * @throws VizioException
     *
     */
    public PutResponse launchApp(VizioAppConfig appConfig) throws VizioException {
        return fromJson(putCommand(urlLaunchApp, String.format(JSON_VALUE, gson.toJson(appConfig))), PutResponse.class);
    }

    /**
     * Send a key press command to the Vizio TV
     *
     * @param the command JSON for the key press
     * @return A PutResponse response object
     * @throws VizioException
     *
     */
    public PutResponse sendKeyPress(String commandJSON) throws VizioException {
        return fromJson(putCommand(urlKeyPress, commandJSON), PutResponse.class);
    }

    /**
     * Start the pairing process to obtain an auth token from the TV
     *
     * @param the deviceName that is displayed in the TV settings after the device is registered
     * @param the deviceId a unique number that identifies this pairing request
     * @return A PairingStart response object
     * @throws VizioException
     *
     */
    public PairingStart starPairing(String deviceName, int deviceId) throws VizioException {
        return fromJson(
                putCommand(urlStartPairing,
                        String.format("{ \"DEVICE_NAME\": \"%s\", \"DEVICE_ID\": \"%d\" }", deviceName, deviceId)),
                PairingStart.class);
    }

    /**
     * Finish the pairing process by submitting the code that was displayed on the TV to obtain the auth token
     *
     * @param the same deviceId that was used by startPairing()
     * @param the pairingCode that was displayed on the TV
     * @param the pairingToken returned by startPairing()
     * @return A PairingComplete response object
     * @throws VizioException
     *
     */
    public PairingComplete submitPairingCode(int deviceId, String pairingCode, int pairingToken) throws VizioException {
        return fromJson(putCommand(urlSubmitPairingCode, String.format(
                "{\"DEVICE_ID\": \"%d\",\"CHALLENGE_TYPE\": 1,\"RESPONSE_VALUE\": \"%s\",\"PAIRING_REQ_TOKEN\": %d}",
                deviceId, pairingCode, pairingToken)), PairingComplete.class);
    }

    /**
     * Sends a GET request to the Vizio TV
     *
     * @param url The url used to retrieve status information from the Vizio TV
     * @return The response content of the http request
     * @throws VizioException
     *
     */
    private String getCommand(String url) throws VizioException {
        try {
            final Request request = httpClient.newRequest(url).method(HttpMethod.GET);
            request.header(AUTH_HEADER, authToken);
            request.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);

            final ContentResponse response = request.send();

            logger.trace("GET url: {}, response: {}", url, response.getContentAsString());
            return response.getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new VizioException("Error executing vizio GET command, URL: " + url + " " + e.getMessage());
        }
    }

    /**
     * Sends a PUT request to the Vizio TV
     *
     * @param url The url used to send a command to the Vizio TV
     * @param commandJSON The JSON data needed to execute the command
     * @return The response content of the http request
     * @throws VizioException
     *
     */
    private String putCommand(String url, String commandJSON) throws VizioException {
        try {
            final Request request = httpClient.newRequest(url).method(HttpMethod.PUT);
            if (!url.contains("pairing")) {
                request.header(AUTH_HEADER, authToken);
            }
            request.content(new StringContentProvider(commandJSON), JSON_CONTENT_TYPE);

            final ContentResponse response = request.send();

            logger.trace("PUT url: {}, response: {}", url, response.getContentAsString());
            return response.getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new VizioException("Error executing vizio PUT command, URL: " + url + e.getMessage());
        }
    }

    /**
     * Wrapper for the Gson fromJson() method that encapsulates exception handling
     *
     * @param json The JSON string to be deserialized
     * @param classOfT The type of class to be returned
     * @return the deserialized object
     * @throws VizioException
     *
     */
    private <T> T fromJson(String json, Class<T> classOfT) throws VizioException {
        Object obj = null;
        try {
            obj = gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            throw new VizioException("Error Parsing JSON string: " + json + ", Exception: " + e.getMessage());
        }
        if (obj != null) {
            return classOfT.cast(obj);
        } else {
            throw new VizioException("Error creating " + classOfT.getSimpleName() + " object for JSON string: " + json);
        }
    }
}
