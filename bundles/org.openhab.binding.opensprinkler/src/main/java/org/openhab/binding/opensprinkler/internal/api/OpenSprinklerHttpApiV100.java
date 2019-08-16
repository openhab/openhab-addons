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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
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
public class OpenSprinklerHttpApiV100 implements OpenSprinklerApi {
    protected final String hostname;
    protected final int port;
    protected final String password;
    protected final String basicUsername;
    protected final String basicPassword;

    protected int firmwareVersion = -1;
    protected int numberOfStations = DEFAULT_STATION_COUNT;

    protected boolean isInManualMode = false;

    private final Gson gson = new Gson();
    protected Http http = new Http();

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
    public OpenSprinklerHttpApiV100(final String hostname, final int port, final String password,
            final String basicUsername, final String basicPassword) throws GeneralApiException {
        if (hostname == null) {
            throw new GeneralApiException("The given url is null.");
        }
        if (port < 1 || port > 65535) {
            throw new GeneralApiException("The given port is invalid.");
        }
        if (password == null) {
            throw new GeneralApiException("The given password is null.");
        }

        if (hostname.startsWith(HTTP_REQUEST_URL_PREFIX) || hostname.startsWith(HTTPS_REQUEST_URL_PREFIX)) {
            this.hostname = hostname;
        } else {
            this.hostname = HTTP_REQUEST_URL_PREFIX + hostname;
        }
        this.port = port;
        this.password = password;
        this.basicUsername = basicUsername;
        this.basicPassword = basicPassword;
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
        } catch (IOException | CommunicationApiException exp) {
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
     * This class contains static methods for communicating HTTP GET
     * and HTTP POST requests.
     *
     * @author Chris Graham - Initial contribution
     * @author Florian Schmidt - Reduce visibility of Http communication to Api
     */
    protected class Http {
        private static final String HTTP_GET = "GET";
        private static final String HTTP_POST = "POST";
        private static final int HTTP_OK_CODE = 200;
        private static final String USER_AGENT = "Mozilla/5.0";

        /**
         * Given a URL and a set parameters, send a HTTP GET request to the URL location created by the URL and
         * parameters.
         *
         * @param url The URL to send a GET request to.
         * @param urlParameters List of parameters to use in the URL for the GET request. Null if no parameters.
         * @return String contents of the response for the GET request.
         * @throws Exception
         */
        public String sendHttpGet(String url, String urlParameters) throws IOException, CommunicationApiException {
            URL location = null;

            if (urlParameters != null) {
                location = new URL(url + "?" + urlParameters);
            } else {
                location = new URL(url);
            }

            HttpURLConnection connection = (HttpURLConnection) location.openConnection();

            connection.setRequestMethod(HTTP_GET);
            generalRequestProperties(connection);

            int responseCode = connection.getResponseCode();

            if (responseCode != HTTP_OK_CODE) {
                throw new CommunicationApiException(
                        "Error sending HTTP GET request to " + url + ". Got response code: " + responseCode);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            return response.toString();
        }

        private void generalRequestProperties(HttpURLConnection connection) {
            connection.setRequestProperty("User-Agent", USER_AGENT);
            basicAuthIfNeeded(connection);
        }

        private void basicAuthIfNeeded(HttpURLConnection connection) {
            String encoded = Base64.getEncoder()
                    .encodeToString((basicUsername + ":" + basicPassword).getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encoded);
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
        public String sendHttpPost(String url, String urlParameters) throws Exception {
            URL location = new URL(url);
            HttpURLConnection connection = (HttpsURLConnection) location.openConnection();

            connection.setRequestMethod(HTTP_POST);
            generalRequestProperties(connection);

            // Send post request
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();

            if (responseCode != HTTP_OK_CODE) {
                throw new Exception(
                        "Error sending HTTP POST request to " + url + ". Got responce code: " + responseCode);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            return response.toString();
        }
    }
}
