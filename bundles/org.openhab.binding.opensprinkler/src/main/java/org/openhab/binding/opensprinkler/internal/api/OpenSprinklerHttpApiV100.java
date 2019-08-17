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
package org.openhab.binding.opensprinkler.internal.api;

import static org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerHttpInterfaceConfig;
import org.openhab.binding.opensprinkler.internal.model.StationProgram;
import org.openhab.binding.opensprinkler.internal.util.Parse;

import com.google.gson.Gson;

/**
 * The {@link OpenSprinklerHttpApiV100} class is used for communicating with
 * the OpenSprinkler API for firmware versions less than 2.1.0
 *
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Allow https URLs and basic auth
 */
class OpenSprinklerHttpApiV100 implements OpenSprinklerApi {
    protected final String hostname;
    protected final int port;
    protected final String password;
    protected final String basicUsername;
    protected final String basicPassword;

    protected int firmwareVersion = -1;
    protected int numberOfStations = DEFAULT_STATION_COUNT;

    protected boolean isInManualMode = false;

    private final Gson gson = new Gson();
    protected HttpRequestSender http;

    /**
     * Constructor for the OpenSprinkler API class to create a connection to the OpenSprinkler
     * device for control and obtaining status info.
     *
     * @param hostname Hostname or IP address as a String of the OpenSprinkler device.
     * @param port The port number the OpenSprinkler API is listening on.
     * @param password Admin password for the OpenSprinkler device.
     * @param basicUsername only needed if basic auth is required
     * @param basicPassword only needed if basic auth is required
     * @throws Exception
     */
    OpenSprinklerHttpApiV100(final HttpClient httpClient, final OpenSprinklerHttpInterfaceConfig config)
            throws GeneralApiException {
        if (config.hostname == null) {
            throw new GeneralApiException("The given url is null.");
        }
        if (config.port < 1 || config.port > 65535) {
            throw new GeneralApiException("The given port is invalid.");
        }
        if (config.password == null) {
            throw new GeneralApiException("The given password is null.");
        }

        if (config.hostname.startsWith(HTTP_REQUEST_URL_PREFIX)
                || config.hostname.startsWith(HTTPS_REQUEST_URL_PREFIX)) {
            this.hostname = config.hostname;
        } else {
            this.hostname = HTTP_REQUEST_URL_PREFIX + config.hostname;
        }
        this.port = config.port;
        this.password = config.password;
        this.basicUsername = config.basicUsername;
        this.basicPassword = config.basicPassword;
        this.http = new HttpRequestSender(httpClient);
    }

    @Override
    public boolean isManualModeEnabled() {
        return isInManualMode;
    }

    @Override
    public void enterManualMode() throws CommunicationApiException {
        try {
            http.sendHttpGet(getBaseUrl(), getRequestRequiredOptions() + "&" + CMD_ENABLE_MANUAL_MODE);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }

        this.firmwareVersion = getFirmwareVersion();
        this.numberOfStations = getNumberOfStations();

        isInManualMode = true;
    }

    @Override
    public void leaveManualMode() throws CommunicationApiException {
        isInManualMode = false;

        try {
            http.sendHttpGet(getBaseUrl(), getRequestRequiredOptions() + "&" + CMD_DISABLE_MANUAL_MODE);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }
    }

    @Override
    public void openStation(int station) throws Exception {
        if (station < 0 || station >= numberOfStations) {
            throw new GeneralApiException("This OpenSprinkler device only has " + this.numberOfStations
                    + " but station " + station + " was requested to be opened.");
        }

        try {
            http.sendHttpGet(getBaseUrl() + "sn" + station + "=1", null);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }
    }

    @Override
    public void closeStation(int station) throws Exception {
        if (station < 0 || station >= numberOfStations) {
            throw new GeneralApiException("This OpenSprinkler device only has " + this.numberOfStations
                    + " but station " + station + " was requested to be closed.");
        }
        try {
            http.sendHttpGet(getBaseUrl() + "sn" + station + "=0", null);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }
    }

    @Override
    public boolean isStationOpen(int station) throws GeneralApiException, CommunicationApiException {
        String returnContent;

        if (station < 0 || station >= numberOfStations) {
            throw new GeneralApiException("This OpenSprinkler device only has " + this.numberOfStations
                    + " but station " + station + " was requested for a status update.");
        }

        try {
            returnContent = http.sendHttpGet(getBaseUrl() + "sn" + station, null);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }

        return returnContent != null && returnContent.equals("1");
    }

    @Override
    public boolean isRainDetected() throws CommunicationApiException {
        if (statusInfo().rs == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getNumberOfStations() throws CommunicationApiException {
        String returnContent;

        try {
            returnContent = http.sendHttpGet(getBaseUrl() + CMD_STATION_INFO, getRequestRequiredOptions());
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }

        this.numberOfStations = Parse.jsonInt(returnContent, JSON_OPTION_STATION_COUNT);

        return this.numberOfStations;
    }

    @Override
    public int getFirmwareVersion() throws CommunicationApiException {
        String returnContent;

        try {
            returnContent = http.sendHttpGet(getBaseUrl() + CMD_OPTIONS_INFO, null);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }

        try {
            this.firmwareVersion = Parse.jsonInt(returnContent, JSON_OPTION_FIRMWARE_VERSION);
        } catch (Exception exp) {
            this.firmwareVersion = -1;
        }

        return this.firmwareVersion;
    }

    /**
     * Returns the hostname and port formatted URL as a String.
     *
     * @return String representation of the OpenSprinkler API URL.
     */
    protected String getBaseUrl() {
        return hostname + ":" + port + "/";
    }

    /**
     * Returns the required URL parameters required for every API call.
     *
     * @return String representation of the parameters needed during an API call.
     */
    protected String getRequestRequiredOptions() {
        return CMD_PASSWORD + this.password;
    }

    @Override
    public StationProgram retrieveProgram(int station) throws CommunicationApiException {
        JcResponse resp = statusInfo();
        return resp.ps.stream().map(values -> new StationProgram(values.get(1))).collect(Collectors.toList())
                .get(station);
    }

    private JcResponse statusInfo() throws CommunicationApiException {
        String returnContent;

        try {
            returnContent = http.sendHttpGet(getBaseUrl() + CMD_STATUS_INFO, getRequestRequiredOptions());
        } catch (CommunicationApiException exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }

        JcResponse resp = gson.fromJson(returnContent, JcResponse.class);
        return resp;
    }

    private static class JcResponse {
        public List<List<Integer>> ps;
        public int rs;
    }

    /**
     * This class contains helper methods for communicating HTTP GET
     * and HTTP POST requests.
     *
     * @author Chris Graham - Initial contribution
     * @author Florian Schmidt - Reduce visibility of Http communication to Api
     */
    protected class HttpRequestSender {
        private static final int HTTP_OK_CODE = 200;
        private static final String USER_AGENT = "Mozilla/5.0";

        private final HttpClient httpClient;

        public HttpRequestSender(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        /**
         * Given a URL and a set parameters, send a HTTP GET request to the URL location created by the URL and
         * parameters.
         *
         * @param url The URL to send a GET request to.
         * @param urlParameters List of parameters to use in the URL for the GET request. Null if no parameters.
         * @return String contents of the response for the GET request.
         * @throws Exception
         */
        public String sendHttpGet(String url, String urlParameters) throws CommunicationApiException {
            String location = null;

            if (urlParameters != null) {
                location = url + "?" + urlParameters;
            } else {
                location = url;
            }

            ContentResponse response;
            try {
                response = withGeneralProperties(httpClient.newRequest(location)).method(HttpMethod.GET).send();
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                throw new CommunicationApiException("Request to OpenSprinkler device failed: " + e.getMessage());
            }

            if (response.getStatus() != HTTP_OK_CODE) {
                throw new CommunicationApiException(
                        "Error sending HTTP GET request to " + url + ". Got response code: " + response.getStatus());
            }

            return response.getContentAsString();
        }

        private Request withGeneralProperties(Request request) {
            request.header(HttpHeader.USER_AGENT, USER_AGENT);
            if (basicUsername != null && basicPassword != null) {
                String encoded = Base64.getEncoder()
                        .encodeToString((basicUsername + ":" + basicPassword).getBytes(StandardCharsets.UTF_8));
                request.header(HttpHeader.AUTHORIZATION, "Basic " + encoded);
            }
            return request;
        }

        /**
         * Given a URL and a set parameters, send a HTTP POST request to the URL location created by the URL and
         * parameters.
         *
         * @param url The URL to send a POST request to.
         * @param urlParameters List of parameters to use in the URL for the POST request. Null if no parameters.
         * @return String contents of the response for the POST request.
         * @throws Exception
         */
        public String sendHttpPost(String url, String urlParameters) throws CommunicationApiException {
            ContentResponse response;
            try {
                response = withGeneralProperties(httpClient.newRequest(url)).method(HttpMethod.POST)
                        .content(new StringContentProvider(urlParameters)).send();
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                throw new CommunicationApiException("Request to OpenSprinkler device failed: " + e.getMessage());
            }

            if (response.getStatus() != HTTP_OK_CODE) {
                throw new CommunicationApiException(
                        "Error sending HTTP POST request to " + url + ". Got response code: " + response.getStatus());
            }

            return response.getContentAsString();
        }
    }
}
