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

import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.DataFormatErrorApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.DataMissingApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.MismatchApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.NotPermittedApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.OutOfRangeApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.PageNotFoundApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.UnauthorizedApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.UnknownApiException;
import org.openhab.binding.opensprinkler.internal.util.Http;
import org.openhab.binding.opensprinkler.internal.util.Parse;

/**
 * The {@link OpenSprinklerHttpApiV210} class is used for communicating with
 * the OpenSprinkler API for firmware versions 2.1.0, 2.1.1 and 2.1.1
 *
 * @author Chris Graham - Initial contribution
 */
public class OpenSprinklerHttpApiV210 extends OpenSprinklerHttpApiV100 {
    /**
     * Constructor for the OpenSprinkler API class to create a connection to the OpenSprinkler
     * device for control and obtaining status info.
     *
     * @param hostname Hostname or IP address as a String of the OpenSprinkler device.
     * @param port The port number the OpenSprinkler API is listening on.
     * @param password Admin password for the OpenSprinkler device.
     * @throws Exception
     */
    public OpenSprinklerHttpApiV210(final String hostname, final int port, final String password) throws Exception {
        super(hostname, port, password);
    }

    @Override
    public boolean isStationOpen(int station) throws Exception {
        String returnContent;
        int stationStatus = -1;

        if (station < 0 || station >= numberOfStations) {
            throw new GeneralApiException("This OpenSprinkler device only has " + this.numberOfStations
                    + " but station " + station + " was requested for a status update.");
        }
        try {
            returnContent = Http.sendHttpGet(getBaseUrl() + CMD_STATION_INFO, getRequestRequiredOptions());
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }

        try {
            stationStatus = Parse.jsonIntAtArrayIndex(returnContent, JSON_OPTION_STATION, station);
        } catch (Exception exp) {
            throw new GeneralApiException("There was a problem parsing the station status for station " + station
                    + ". Got the error: " + exp.getMessage());
        }

        if (stationStatus == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void openStation(int station) throws Exception {
        String returnContent;

        if (station < 0 || station >= numberOfStations) {
            throw new GeneralApiException("This OpenSprinkler device only has " + this.numberOfStations
                    + " but station " + station + " was requested to be opened.");
        }

        try {
            returnContent = Http.sendHttpGet(getBaseUrl() + CMD_STATION_CONTROL, getRequestRequiredOptions() + "&"
                    + CMD_STATION + station + "&" + CMD_STATION_ENABLE + "&" + CMD_STATION_ENABLE_TIME);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }

        resultParser(returnContent);
    }

    @Override
    public void closeStation(int station) throws Exception {
        String returnContent;

        if (station < 0 || station > numberOfStations) {
            throw new GeneralApiException("This OpenSprinkler device only has " + this.numberOfStations
                    + " but station " + station + " was requested to be closed.");
        }

        try {
            returnContent = Http.sendHttpGet(getBaseUrl() + CMD_STATION_CONTROL,
                    getRequestRequiredOptions() + "&" + CMD_STATION + station + "&" + CMD_STATION_DISABLE);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }

        resultParser(returnContent);
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception
     */
    @Override
    public void openConnection() throws Exception {
        this.firmwareVersion = getFirmwareVersion();
        this.numberOfStations = getNumberOfStations();

        connectionOpen = true;
    }

    @Override
    public void closeConnection() {
        connectionOpen = false;
    }

    /**
     * Creates a custom exception based on a result code from the OpenSprinkler device. This is a
     * formatted response from the API as {"result: : ##}.
     *
     * @param returnContent String value of the return content from the OpenSprinkler device when
     *            an action result is returned from the API.
     * @throws Exception Returns a custom exception based on the result key.
     */
    protected void resultParser(String returnContent) throws Exception {
        int returnCode;

        try {
            returnCode = Parse.jsonInt(returnContent, JSON_OPTION_RESULT);
        } catch (Exception exp) {
            returnCode = -1;
        }

        switch (returnCode) {
            case -1:
                throw new UnknownApiException(
                        "The OpenSprinkler API returnd an result that was not parseable: " + returnContent);
            case 1:
                return;
            case 2:
                throw new UnauthorizedApiException("The OpenSprinkler API returned Unauthorized response code.");
            case 3:
                throw new MismatchApiException("The OpenSprinkler API returned Mismatch response code.");
            case 16:
                throw new DataMissingApiException("The OpenSprinkler API returned Data Missing response code.");
            case 17:
                throw new OutOfRangeApiException("The OpenSprinkler API returned Out of Range response code.");
            case 18:
                throw new DataFormatErrorApiException(
                        "The OpenSprinkler API returned Data Format Error response code.");
            case 32:
                throw new PageNotFoundApiException("The OpenSprinkler API returned Page Not Found response code.");
            case 48:
                throw new NotPermittedApiException("The OpenSprinkler API returned Not Permitted response code.");
            default:
                throw new UnknownApiException("Unknown response code from OpenSprinkler API: " + returnCode);
        }
    }
}
