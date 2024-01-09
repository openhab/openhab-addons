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
package org.openhab.binding.opensprinkler.internal.api;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.opensprinkler.internal.OpenSprinklerState;
import org.openhab.binding.opensprinkler.internal.OpenSprinklerState.JcResponse;
import org.openhab.binding.opensprinkler.internal.OpenSprinklerState.JnResponse;
import org.openhab.binding.opensprinkler.internal.OpenSprinklerState.JoResponse;
import org.openhab.binding.opensprinkler.internal.OpenSprinklerState.JsResponse;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.UnauthorizedApiException;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerHttpInterfaceConfig;
import org.openhab.binding.opensprinkler.internal.model.StationProgram;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link OpenSprinklerHttpApiV100} class is used for communicating with the
 * OpenSprinkler API for firmware versions less than 2.1.0
 *
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Allow https URLs and basic auth
 */
@NonNullByDefault
class OpenSprinklerHttpApiV100 implements OpenSprinklerApi {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final String hostname;
    protected final OpenSprinklerHttpInterfaceConfig config;
    protected String password;
    protected OpenSprinklerState state = new OpenSprinklerState();
    protected int numberOfStations = DEFAULT_STATION_COUNT;
    protected boolean isInManualMode = false;
    protected final Gson gson = new Gson();
    protected HttpRequestSender http;

    /**
     * Constructor for the OpenSprinkler API class to create a connection to the
     * OpenSprinkler device for control and obtaining status info.
     *
     * @param hostname Hostname or IP address as a String of the OpenSprinkler
     *            device.
     * @param port The port number the OpenSprinkler API is listening on.
     * @param password Admin password for the OpenSprinkler device.
     * @param basicUsername only needed if basic auth is required
     * @param basicPassword only needed if basic auth is required
     * @throws Exception
     */
    OpenSprinklerHttpApiV100(final HttpClient httpClient, final OpenSprinklerHttpInterfaceConfig config)
            throws CommunicationApiException, UnauthorizedApiException {
        if (config.hostname.startsWith(HTTP_REQUEST_URL_PREFIX)
                || config.hostname.startsWith(HTTPS_REQUEST_URL_PREFIX)) {
            this.hostname = config.hostname;
        } else {
            this.hostname = HTTP_REQUEST_URL_PREFIX + config.hostname;
        }
        this.config = config;
        this.password = config.password;
        this.http = new HttpRequestSender(httpClient);
    }

    @Override
    public boolean isManualModeEnabled() {
        return isInManualMode;
    }

    @Override
    public List<StateOption> getPrograms() {
        return state.programs;
    }

    @Override
    public List<StateOption> getStations() {
        return state.stations;
    }

    @Override
    public void refresh() throws CommunicationApiException, UnauthorizedApiException {
        state.joReply = getOptions();
        state.jsReply = getStationStatus();
        state.jcReply = statusInfo();
        state.jnReply = getStationNames();
    }

    @Override
    public void enterManualMode() throws CommunicationApiException, UnauthorizedApiException {
        http.sendHttpGet(getBaseUrl(), getRequestRequiredOptions() + "&" + CMD_ENABLE_MANUAL_MODE);
        numberOfStations = getNumberOfStations();
        isInManualMode = true;
    }

    @Override
    public void leaveManualMode() throws CommunicationApiException, UnauthorizedApiException {
        http.sendHttpGet(getBaseUrl(), getRequestRequiredOptions() + "&" + CMD_DISABLE_MANUAL_MODE);
        isInManualMode = false;
    }

    @Override
    public void openStation(int station, BigDecimal duration) throws CommunicationApiException, GeneralApiException {
        http.sendHttpGet(getBaseUrl() + "sn" + station + "=1&t=" + duration, null);
    }

    @Override
    public void closeStation(int station) throws CommunicationApiException, GeneralApiException {
        http.sendHttpGet(getBaseUrl() + "sn" + station + "=0", null);
    }

    @Override
    public boolean isStationOpen(int station) throws CommunicationApiException, GeneralApiException {
        String returnContent = http.sendHttpGet(getBaseUrl() + "sn" + station, null);
        return "1".equals(returnContent);
    }

    @Override
    public void ignoreRain(int station, boolean command) throws CommunicationApiException, UnauthorizedApiException {
    }

    @Override
    public boolean isIgnoringRain(int station) {
        return false;
    }

    @Override
    public boolean isRainDetected() {
        return state.jcReply.rs == 1;
    }

    @Override
    public int getSensor2State() {
        return state.jcReply.sn2;
    }

    @Override
    public int currentDraw() {
        return state.jcReply.curr;
    }

    @Override
    public int flowSensorCount() {
        return state.jcReply.flcrt;
    }

    @Override
    public int signalStrength() {
        return state.jcReply.rssi;
    }

    @Override
    public boolean getIsEnabled() {
        return state.jcReply.en == 1;
    }

    @Override
    public int waterLevel() {
        return state.joReply.wl;
    }

    @Override
    public int getNumberOfStations() {
        numberOfStations = state.jsReply.nstations;
        return numberOfStations;
    }

    @Override
    public int getFirmwareVersion() throws CommunicationApiException, UnauthorizedApiException {
        state.joReply = getOptions();
        return state.joReply.fwv;
    }

    @Override
    public void runProgram(Command command) throws CommunicationApiException, UnauthorizedApiException {
        logger.warn("OpenSprinkler requires at least firmware 217 for the runProgram feature to work");
    }

    @Override
    public void enablePrograms(Command command) throws UnauthorizedApiException, CommunicationApiException {
        if (command == OnOffType.ON) {
            http.sendHttpGet(getBaseUrl() + "cv", getRequestRequiredOptions() + "&en=1");
        } else {
            http.sendHttpGet(getBaseUrl() + "cv", getRequestRequiredOptions() + "&en=0");
        }
    }

    @Override
    public void resetStations() throws UnauthorizedApiException, CommunicationApiException {
        http.sendHttpGet(getBaseUrl() + "cv", getRequestRequiredOptions() + "&rsn=1");
    }

    @Override
    public void setRainDelay(int hours) throws UnauthorizedApiException, CommunicationApiException {
        http.sendHttpGet(getBaseUrl() + "cv", getRequestRequiredOptions() + "&rd=" + hours);
    }

    @Override
    public QuantityType<Time> getRainDelay() {
        if (state.jcReply.rdst == 0) {
            return new QuantityType<>(0, Units.SECOND);
        }
        long remainingTime = state.jcReply.rdst - state.jcReply.devt;
        return new QuantityType<>(remainingTime, Units.SECOND);
    }

    /**
     * Returns the hostname and port formatted URL as a String.
     *
     * @return String representation of the OpenSprinkler API URL.
     */
    protected String getBaseUrl() {
        return hostname + ":" + config.port + "/";
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
        if (state.jcReply.ps != null) {
            return state.jcReply.ps.stream().map(values -> new StationProgram(values.get(1)))
                    .collect(Collectors.toList()).get(station);
        }
        return new StationProgram(0);
    }

    private JcResponse statusInfo() throws CommunicationApiException, UnauthorizedApiException {
        String returnContent;
        JcResponse resp;
        try {
            returnContent = http.sendHttpGet(getBaseUrl() + CMD_STATUS_INFO, getRequestRequiredOptions());
            resp = gson.fromJson(returnContent, JcResponse.class);
            if (resp == null) {
                throw new CommunicationApiException(
                        "There was a problem in the HTTP communication: jcReply was empty.");
            }
        } catch (JsonSyntaxException exp) {
            throw new CommunicationApiException(
                    "There was a JSON syntax problem in the HTTP communication with the OpenSprinkler API: "
                            + exp.getMessage());
        }
        return resp;
    }

    private JoResponse getOptions() throws CommunicationApiException, UnauthorizedApiException {
        String returnContent;
        JoResponse resp;
        try {
            returnContent = http.sendHttpGet(getBaseUrl() + CMD_OPTIONS_INFO, getRequestRequiredOptions());
            resp = gson.fromJson(returnContent, JoResponse.class);
            if (resp == null) {
                throw new CommunicationApiException(
                        "There was a problem in the HTTP communication: joReply was empty.");
            }
        } catch (JsonSyntaxException exp) {
            throw new CommunicationApiException(
                    "There was a JSON syntax problem in the HTTP communication with the OpenSprinkler API: "
                            + exp.getMessage());
        }
        return resp;
    }

    protected JsResponse getStationStatus() throws CommunicationApiException, UnauthorizedApiException {
        String returnContent;
        JsResponse resp;
        try {
            returnContent = http.sendHttpGet(getBaseUrl() + CMD_STATION_INFO, getRequestRequiredOptions());
            resp = gson.fromJson(returnContent, JsResponse.class);
            if (resp == null) {
                throw new CommunicationApiException(
                        "There was a problem in the HTTP communication: jsReply was empty.");
            }
        } catch (JsonSyntaxException exp) {
            throw new CommunicationApiException(
                    "There was a JSON syntax problem in the HTTP communication with the OpenSprinkler API: "
                            + exp.getMessage());
        }
        return resp;
    }

    @Override
    public void getProgramData() throws CommunicationApiException, UnauthorizedApiException {
    }

    @Override
    public JnResponse getStationNames() throws CommunicationApiException, UnauthorizedApiException {
        String returnContent;
        JnResponse resp;
        try {
            returnContent = http.sendHttpGet(getBaseUrl() + "jn", getRequestRequiredOptions());
            resp = gson.fromJson(returnContent, JnResponse.class);
            if (resp == null) {
                throw new CommunicationApiException(
                        "There was a problem in the HTTP communication: jnReply was empty.");
            }
        } catch (JsonSyntaxException exp) {
            throw new CommunicationApiException(
                    "There was a JSON syntax problem in the HTTP communication with the OpenSprinkler API: "
                            + exp.getMessage());
        }
        state.jnReply = resp;
        return resp;
    }

    /**
     * This class contains helper methods for communicating HTTP GET and HTTP POST
     * requests.
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
         * Given a URL and a set parameters, send a HTTP GET request to the URL location
         * created by the URL and parameters.
         *
         * @param url The URL to send a GET request to.
         * @param urlParameters List of parameters to use in the URL for the GET
         *            request. Null if no parameters.
         * @return String contents of the response for the GET request.
         * @throws Exception
         */
        public String sendHttpGet(String url, @Nullable String urlParameters)
                throws CommunicationApiException, UnauthorizedApiException {
            String location = null;
            if (urlParameters != null) {
                location = url + "?" + urlParameters;
            } else {
                location = url;
            }
            ContentResponse response = null;
            int retriesLeft = Math.max(1, config.retry);
            boolean connectionSuccess = false;
            while (connectionSuccess == false && retriesLeft > 0) {
                retriesLeft--;
                try {
                    response = withGeneralProperties(httpClient.newRequest(location))
                            .timeout(config.timeout, TimeUnit.SECONDS).method(HttpMethod.GET).send();
                    connectionSuccess = true;
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.debug("Request to OpenSprinkler device failed (retries left: {}): {}", retriesLeft,
                            e.getMessage());
                }
            }
            if (connectionSuccess == false) {
                throw new CommunicationApiException("Request to OpenSprinkler device failed");
            }
            if (response != null && response.getStatus() != HTTP_OK_CODE) {
                throw new CommunicationApiException(
                        "Error sending HTTP GET request to " + url + ". Got response code: " + response.getStatus());
            } else if (response != null) {
                String content = response.getContentAsString();
                if ("{\"result\":2}".equals(content)) {
                    throw new UnauthorizedApiException("Unauthorized, check your password is correct");
                }
                return content;
            }
            return "";
        }

        private Request withGeneralProperties(Request request) {
            request.header(HttpHeader.USER_AGENT, USER_AGENT);
            if (!config.basicUsername.isEmpty() && !config.basicPassword.isEmpty()) {
                String encoded = Base64.getEncoder().encodeToString(
                        (config.basicUsername + ":" + config.basicPassword).getBytes(StandardCharsets.UTF_8));
                request.header(HttpHeader.AUTHORIZATION, "Basic " + encoded);
            }
            return request;
        }

        /**
         * Given a URL and a set parameters, send a HTTP POST request to the URL
         * location created by the URL and parameters.
         *
         * @param url The URL to send a POST request to.
         * @param urlParameters List of parameters to use in the URL for the POST
         *            request. Null if no parameters.
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

    @Override
    public int getQueuedZones() {
        return state.jcReply.nq;
    }

    @Override
    public int getCloudConnected() {
        return state.jcReply.otcs;
    }

    @Override
    public int getPausedState() {
        return state.jcReply.pt;
    }

    @Override
    public void setPausePrograms(int seconds) throws UnauthorizedApiException, CommunicationApiException {
    }
}
