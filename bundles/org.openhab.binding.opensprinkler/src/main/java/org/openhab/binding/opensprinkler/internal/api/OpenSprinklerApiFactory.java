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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerHttpInterfaceConfig;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenSprinklerApiFactory} class is used for creating instances of
 * the OpenSprinkler API classes to interact with the OpenSprinklers HTTP or
 * GPIO API's.
 *
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Refactoring
 */
@Component(service = OpenSprinklerApiFactory.class)
@NonNullByDefault
public class OpenSprinklerApiFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private HttpClient httpClient;

    @Activate
    public OpenSprinklerApiFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    /**
     * Factory method used to determine what version of the API is in use at the
     * OpenSprinkler API and return the proper class for control of the device.
     *
     * @param config Interface settings
     * @return OpenSprinkler HTTP API class for control of the device.
     * @throws CommunicationApiException
     * @throws GeneralApiException
     */
    public OpenSprinklerApi getHttpApi(OpenSprinklerHttpInterfaceConfig config)
            throws CommunicationApiException, GeneralApiException {
        int version = -1;

        OpenSprinklerApi lowestSupportedApi = new OpenSprinklerHttpApiV100(this.httpClient, config);
        try {
            version = lowestSupportedApi.getFirmwareVersion();
        } catch (CommunicationApiException exp) {
            throw new CommunicationApiException(
                    "Problem fetching the firmware version from the OpenSprinkler: " + exp.getMessage());
        }
        logger.debug("Firmware was reported as {}", version);
        if (version >= 210 && version < 213) {
            return new OpenSprinklerHttpApiV210(this.httpClient, config);
        } else if (version >= 213 && version < 217) {
            return new OpenSprinklerHttpApiV213(this.httpClient, config);
        } else if (version >= 217 && version < 219) {
            return new OpenSprinklerHttpApiV217(this.httpClient, config);
        } else if (version >= 219 && version < 220) {
            return new OpenSprinklerHttpApiV219(this.httpClient, config);
        } else if (version >= 220) {
            return new OpenSprinklerHttpApiV220(this.httpClient, config);
        } else {
            /* Need to make sure we have an older OpenSprinkler device by checking the first station. */
            try {
                lowestSupportedApi.isStationOpen(0);
            } catch (GeneralApiException | CommunicationApiException exp) {
                throw new CommunicationApiException(
                        "There was a problem in the HTTP communication with the OpenSprinkler API: "
                                + exp.getMessage());
            }
            return lowestSupportedApi;
        }
    }
}
