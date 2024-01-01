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

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.CMD_PROGRAM_DATA;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.opensprinkler.internal.OpenSprinklerState.JpResponse;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.UnauthorizedApiException;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerHttpInterfaceConfig;
import org.openhab.core.types.StateOption;

import com.google.gson.JsonParseException;

/**
 * The {@link OpenSprinklerHttpApiV220} class is used for communicating with
 * the firmware versions 2.2.0 and up.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class OpenSprinklerHttpApiV220 extends OpenSprinklerHttpApiV219 {

    OpenSprinklerHttpApiV220(HttpClient httpClient, OpenSprinklerHttpInterfaceConfig config)
            throws GeneralApiException, CommunicationApiException {
        super(httpClient, config);
    }

    @Override
    public void getProgramData() throws CommunicationApiException, UnauthorizedApiException {
        String returnContent;
        try {
            returnContent = http.sendHttpGet(getBaseUrl() + CMD_PROGRAM_DATA, getRequestRequiredOptions());
        } catch (CommunicationApiException exp) {
            throw new CommunicationApiException(
                    "There was a problem in the HTTP communication with the OpenSprinkler API: " + exp.getMessage());
        }
        try {
            JpResponse resp = gson.fromJson(returnContent, JpResponse.class);
            if (resp != null && resp.pd.length > 0) {
                state.programs = new ArrayList<>();
                int counter = 0;
                for (Object x : resp.pd) {
                    String temp = x.toString();
                    logger.trace("Program Data:{}", temp);
                    int end = temp.lastIndexOf('[') - 2;
                    int start = temp.lastIndexOf((','), end - 1) + 2;
                    if (start > -1 && end > -1) {
                        temp = temp.substring(start, end);
                        state.programs.add(new StateOption(Integer.toString(counter++), temp));
                    }
                }
            }
        } catch (JsonParseException e) {
            logger.debug("Following json could not be parsed:{}", returnContent);
        }
    }

    @Override
    public void setPausePrograms(int seconds) throws UnauthorizedApiException, CommunicationApiException {
        http.sendHttpGet(getBaseUrl() + "pq", getRequestRequiredOptions() + "&dur=" + seconds);
    }
}
