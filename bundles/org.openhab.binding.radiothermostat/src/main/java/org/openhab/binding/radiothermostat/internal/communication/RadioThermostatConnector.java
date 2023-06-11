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
package org.openhab.binding.radiothermostat.internal.communication;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.openhab.binding.radiothermostat.internal.RadioThermostatBindingConstants.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.radiothermostat.internal.RadioThermostatHttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with the RadioThermostat web interface
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatConnector {
    private final Logger logger = LoggerFactory.getLogger(RadioThermostatConnector.class);

    private static final String URL = "http://%hostName%/%resource%";

    private final HttpClient httpClient;
    private final List<RadioThermostatEventListener> listeners = new CopyOnWriteArrayList<>();

    private @Nullable String hostName;

    public RadioThermostatConnector(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setThermostatHostName(@Nullable String hostName) {
        this.hostName = hostName;
    }

    /**
     * Add a listener to the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void addEventListener(RadioThermostatEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void removeEventListener(RadioThermostatEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Send an asynchronous http call to the thermostat, the response will be send to the
     * event listeners as a RadioThermostat event when it is finally received
     *
     * @param resouce the url of the json resource on the thermostat
     */
    public void getAsyncThermostatData(String resource) {
        String urlStr = buildRequestURL(resource);

        httpClient.newRequest(urlStr).method(GET).timeout(30, TimeUnit.SECONDS).send(new BufferingResponseListener() {
            @Override
            public void onComplete(@Nullable Result result) {
                if (result != null && !result.isFailed()) {
                    String response = getContentAsString();
                    logger.debug("thermostatResponse = {}", response);
                    dispatchKeyValue(resource, response);
                } else {
                    dispatchKeyValue(KEY_ERROR, "");
                }
            }
        });
    }

    /**
     * Sends a command to the thermostat
     *
     * @param the JSON attribute key for the value to be updated
     * @param the value to be updated in the thermostat
     * @param the end point URI to use for the command
     * @return the JSON response string from the thermostat
     */
    public String sendCommand(String cmdKey, @Nullable String cmdVal, String resource) {
        return sendCommand(cmdKey, cmdVal, null, resource);
    }

    /**
     * Sends a command to the thermostat
     *
     * @param the JSON attribute key for the value to be updated
     * @param the value to be updated in the thermostat
     * @param JSON string to send directly to the thermostat instead of a key/value pair
     * @param the end point URI to use for the command
     * @return the JSON response string from the thermostat
     */
    public String sendCommand(@Nullable String cmdKey, @Nullable String cmdVal, @Nullable String cmdJson,
            String resource) {
        // if we got a cmdJson string send that, otherwise build the json from the key and val params
        String postJson = cmdJson != null ? cmdJson : "{\"" + cmdKey + "\":" + cmdVal + "}";
        String urlStr = buildRequestURL(resource);

        String output = "";

        try {
            Request request = httpClient.POST(urlStr);
            request.header(HttpHeader.ACCEPT, "text/plain");
            request.header(HttpHeader.CONTENT_TYPE, "text/plain");
            request.content(new StringContentProvider(postJson), "application/json");

            ContentResponse contentResponse = request.send();
            int httpStatus = contentResponse.getStatus();

            if (httpStatus != OK_200) {
                throw new RadioThermostatHttpException("Thermostat HTTP response code was: " + httpStatus);
            }
            output = contentResponse.getContentAsString();
        } catch (RadioThermostatHttpException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error executing thermostat command: {}, {}", postJson, e.getMessage());
        }

        return output;
    }

    /**
     * Build request URL from configuration data
     *
     * @return a valid URL for the thermostat's JSON interface
     */
    private String buildRequestURL(String resource) {
        String hostName = this.hostName;
        if (hostName == null) {
            throw new IllegalStateException("hostname must not be null");
        }
        String urlStr = URL.replace("%hostName%", hostName);
        urlStr = urlStr.replace("%resource%", resource);

        return urlStr;
    }

    /**
     * Dispatch an event (key, value) to the event listeners
     *
     * @param key the key
     * @param value the value
     */
    private void dispatchKeyValue(String key, String value) {
        RadioThermostatEvent event = new RadioThermostatEvent(this, key, value);
        for (RadioThermostatEventListener listener : listeners) {
            listener.onNewMessageEvent(event);
        }
    }
}
