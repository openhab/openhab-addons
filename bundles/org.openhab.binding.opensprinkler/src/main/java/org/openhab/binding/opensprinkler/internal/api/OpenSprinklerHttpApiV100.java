/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
import com.google.gson.annotations.SerializedName;

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
    protected final int port;
    protected String password;
    protected final String basicUsername;
    protected final String basicPassword;
    protected JcResponse jcReply = new JcResponse();
    protected JoResponse joReply = new JoResponse();
    protected JsResponse jsReply = new JsResponse();
    protected JpResponse jpReply = new JpResponse();
    protected JnResponse jnReply = new JnResponse();
    protected int numberOfStations = DEFAULT_STATION_COUNT;
    protected boolean isInManualMode = false;
    protected final Gson gson = new Gson();
    protected HttpRequestSender http;
    protected List<StateOption> programs = new ArrayList<>();
    protected List<StateOption> stations = new ArrayList<>();

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
    public List<StateOption> getPrograms() {
        return programs;
    }

    @Override
    public List<StateOption> getStations() {
        return stations;
    }

    @Override
    public void refresh() throws CommunicationApiException, UnauthorizedApiException {
        joReply = getOptions();
        jsReply = getStationStatus();
        jcReply = statusInfo();
        jnReply = getStationNames();
    }

    @Override
    public void enterManualMode() throws CommunicationApiException, UnauthorizedApiException {
        try {
            http.sendHttpGet(getBaseUrl(), getRequestRequiredOptions() + "&" + CMD_ENABLE_MANUAL_MODE);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }
        numberOfStations = getNumberOfStations();
        isInManualMode = true;
    }

    @Override
    public void leaveManualMode() throws CommunicationApiException {
        try {
            http.sendHttpGet(getBaseUrl(), getRequestRequiredOptions() + "&" + CMD_DISABLE_MANUAL_MODE);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }
        isInManualMode = false;
    }

    @Override
    public void openStation(int station, BigDecimal duration) throws CommunicationApiException, GeneralApiException {
        try {
            http.sendHttpGet(getBaseUrl() + "sn" + station + "=1&t=" + duration, null);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }
    }

    @Override
    public void closeStation(int station) throws CommunicationApiException, GeneralApiException {
        http.sendHttpGet(getBaseUrl() + "sn" + station + "=0", null);
    }

    @Override
    public boolean isStationOpen(int station) throws CommunicationApiException, GeneralApiException {
        String returnContent;
        try {
            returnContent = http.sendHttpGet(getBaseUrl() + "sn" + station, null);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }
        return returnContent.equals("1");
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
        return jcReply.rs == 1;
    }

    @Override
    public int getSensor2State() {
        return jcReply.sn2;
    }

    @Override
    public int currentDraw() {
        return jcReply.curr;
    }

    @Override
    public int flowSensorCount() {
        return jcReply.flcrt;
    }

    @Override
    public int signalStrength() {
        return jcReply.RSSI;
    }

    @Override
    public boolean getIsEnabled() {
        return jcReply.en == 1;
    }

    @Override
    public int waterLevel() {
        return joReply.wl;
    }

    @Override
    public int getNumberOfStations() {
        numberOfStations = jsReply.nstations;
        return numberOfStations;
    }

    @Override
    public int getFirmwareVersion() throws CommunicationApiException, UnauthorizedApiException {
        joReply = getOptions();
        return joReply.fwv;
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
        if (jcReply.rdst == 0) {
            return new QuantityType<Time>(0, Units.SECOND);
        }
        long remainingTime = jcReply.rdst - jcReply.devt;
        return new QuantityType<Time>(remainingTime, Units.SECOND);
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
        if (jcReply.ps != null) {
            return jcReply.ps.stream().map(values -> new StationProgram(values.get(1))).collect(Collectors.toList())
                    .get(station);
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
        } catch (CommunicationApiException | JsonSyntaxException exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
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
        } catch (CommunicationApiException | JsonSyntaxException exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
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
        } catch (CommunicationApiException | JsonSyntaxException exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
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
        } catch (CommunicationApiException | JsonSyntaxException exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }
        jnReply = resp;
        return resp;
    }

    protected static class JsResponse {
        public int sn[] = new int[8];
        public int nstations = 8;
    }

    protected static class JpResponse {
        public int nprogs = 0;
        public Object[] pd = {};
    }

    private static class JoResponse {
        public int wl;
        public int fwv = -1;
    }

    protected static class JnResponse {
        public List<String> snames = new ArrayList<>();
        public byte[] ignore_rain = { 0 };
    }

    protected static class JcResponse {
        public @Nullable List<List<Integer>> ps;
        @SerializedName(value = "sn1", alternate = "rs")
        public int rs;
        public long devt = 0;
        public long rdst = 0;
        public int en = 1;
        public int sn2 = -1;
        public int RSSI = 1; // json reply uses all uppercase
        public int flcrt = -1;
        public int curr = -1;
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
            String content = response.getContentAsString();
            if (content.equals("{\"result\":2}")) {
                throw new UnauthorizedApiException("Unauthorized, check your password is correct");
            }
            return content;
        }

        private Request withGeneralProperties(Request request) {
            request.header(HttpHeader.USER_AGENT, USER_AGENT);
            if (!basicUsername.isEmpty() && !basicPassword.isEmpty()) {
                String encoded = Base64.getEncoder()
                        .encodeToString((basicUsername + ":" + basicPassword).getBytes(StandardCharsets.UTF_8));
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
}
