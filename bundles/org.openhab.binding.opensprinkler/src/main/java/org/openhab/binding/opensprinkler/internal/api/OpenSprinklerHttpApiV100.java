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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.model.StationProgram;
import org.openhab.binding.opensprinkler.internal.util.Http;
import org.openhab.binding.opensprinkler.internal.util.Parse;

import com.google.gson.Gson;

/**
 * The {@link OpenSprinklerHttpApiV100} class is used for communicating with
 * the OpenSprinkler API for firmware versions less than 2.1.0
 *
 * @author Chris Graham - Initial contribution
 */
public class OpenSprinklerHttpApiV100 implements OpenSprinklerApi {
    protected final String hostname;
    protected final int port;
    protected final String password;

    protected int firmwareVersion = -1;
    protected int numberOfStations = DEFAULT_STATION_COUNT;

    protected boolean isInManualMode = false;

    private final Gson gson = new Gson();

    /**
     * Constructor for the OpenSprinkler API class to create a connection to the OpenSprinkler
     * device for control and obtaining status info.
     *
     * @param hostname Hostname or IP address as a String of the OpenSprinkler device.
     * @param port The port number the OpenSprinkler API is listening on.
     * @param password Admin password for the OpenSprinkler device.
     * @throws Exception
     */
    public OpenSprinklerHttpApiV100(final String hostname, final int port, final String password)
            throws GeneralApiException {
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
            throw new GeneralApiException("The given hostname does not need to start with " + HTTP_REQUEST_URL_PREFIX
                    + " or " + HTTP_REQUEST_URL_PREFIX);
        }

        this.hostname = hostname;
        this.port = port;
        this.password = password;
    }

    @Override
    public boolean isManualModeEnabled() {
        return isInManualMode;
    }

    @Override
    public void enterManualMode() throws CommunicationApiException {
        try {
            Http.sendHttpGet(getBaseUrl(), getRequestRequiredOptions() + "&" + CMD_ENABLE_MANUAL_MODE);
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
            Http.sendHttpGet(getBaseUrl(), getRequestRequiredOptions() + "&" + CMD_DISABLE_MANUAL_MODE);
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
            Http.sendHttpGet(getBaseUrl() + "sn" + station + "=1", null);
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
            Http.sendHttpGet(getBaseUrl() + "sn" + station + "=0", null);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }
    }

    @Override
    public boolean isStationOpen(int station) throws Exception {
        String returnContent;

        if (station < 0 || station >= numberOfStations) {
            throw new GeneralApiException("This OpenSprinkler device only has " + this.numberOfStations
                    + " but station " + station + " was requested for a status update.");
        }

        try {
            returnContent = Http.sendHttpGet(getBaseUrl() + "sn" + station, null);
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
            returnContent = Http.sendHttpGet(getBaseUrl() + CMD_STATION_INFO, getRequestRequiredOptions());
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
            returnContent = Http.sendHttpGet(getBaseUrl() + CMD_OPTIONS_INFO, null);
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
        return HTTP_REQUEST_URL_PREFIX + hostname + ":" + port + "/";
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
            returnContent = Http.sendHttpGet(getBaseUrl() + CMD_STATUS_INFO, getRequestRequiredOptions());
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
}
