/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opensprinkler.internal.api;

import static org.openhab.binding.opensprinkler.internal.api.OpenSprinklerApiConstants.*;

import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.util.Http;
import org.openhab.binding.opensprinkler.internal.util.Parse;

/**
 * The {@link OpenSprinklerApiFactory} class is used for creating instances of
 * the OpenSprinkler API classes to interact with the OpenSprinklers HTTP or
 * GPIO API's.
 *
 * @author Chris Graham - Initial contribution
 */
public class OpenSprinklerApiFactory {

    /**
     * Factory method used to determine what version of the API is in use at the
     * OpenSprinkler API and return the proper class for control of the device.
     *
     * @param hostname Hostname or IP address as a String of the OpenSprinkler device.
     * @param port The port number the OpenSprinkler API is listening on.
     * @param password Admin password for the OpenSprinkler device.
     * @return OpenSprinkler HTTP API class for control of the device.
     * @throws Exception
     */
    public static OpenSprinklerApi getHttpApi(String hostname, int port, String password) throws Exception {
        String returnContent;
        int version = -1;

        try {
            returnContent = Http.sendHttpGet(HTTP_REQUEST_URL_PREFIX + hostname + ":" + port + "/" + CMD_OPTIONS_INFO,
                    null);
        } catch (Exception exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }

        try {
            version = Parse.jsonInt(returnContent, JSON_OPTION_FIRMWARE_VERSION);
        } catch (Exception exp) {
            version = -1;
        }

        if (version >= 210 && version < 213) {
            return new OpenSprinklerHttpApiV210(hostname, port, password);
        } else if (version >= 213) {
            return new OpenSprinklerHttpApiV213(hostname, port, password);
        } else {
            /* Need to make sure we have an older OpenSprinkler device by checking the first station. */
            try {
                returnContent = Http.sendHttpGet(HTTP_REQUEST_URL_PREFIX + hostname + ":" + port + "/sn0", null);
            } catch (Exception exp) {
                throw new CommunicationApiException(
                        "There was a problem in the HTTP communication with the OpenSprinkler API: "
                                + exp.getMessage());
            }

            if (returnContent == null || (!returnContent.equals("0") && !returnContent.equals("1"))) {
                throw new CommunicationApiException(
                        "There was a problem in the HTTP communication with the OpenSprinkler API, Unexpected API response: "
                                + returnContent);
            }

            return new OpenSprinklerHttpApiV100(hostname, port, password);
        }
    }

    /**
     * Factory method returns an OpenSprnkler PI GPIO class for control.
     *
     * @param numberOfStations The number of stations to control on the OpenSprinkler PI device.
     * @return OpenSprinkler GPIO class for control of the device.
     */
    public static OpenSprinklerApi getGpioApi(int numberOfStations) {
        return new OpenSprinklerGpioApi(numberOfStations);
    }
}
