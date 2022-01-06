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
package org.openhab.binding.opengarage.internal;

import java.io.IOException;

import org.openhab.binding.opengarage.internal.api.ControllerVariables;
import org.openhab.binding.opengarage.internal.api.Enums.OpenGarageCommand;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles performing the actual HTTP requests for communicating with Opengarage units.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
public class OpenGarageWebTargets {
    private static final int TIMEOUT_MS = 30000;

    private String getControllerVariablesUri;
    private String changeControllerVariablesUri;
    private final Logger logger = LoggerFactory.getLogger(OpenGarageWebTargets.class);

    public OpenGarageWebTargets(String ipAddress, long port, String password) {
        String baseUri = "http://" + ipAddress + ":" + port + "/";
        getControllerVariablesUri = baseUri + "jc";
        changeControllerVariablesUri = baseUri + "cc?dkey=" + password;
    }

    public ControllerVariables getControllerVariables() throws OpenGarageCommunicationException {
        String response = invoke(getControllerVariablesUri);
        return ControllerVariables.parse(response);
    }

    public void setControllerVariables(OpenGarageCommand request) throws OpenGarageCommunicationException {
        logger.debug("Received request: {}", request);
        String queryParams = null;
        switch (request) {
            case OPEN:
                queryParams = "&open=1";
                break;
            case CLOSE:
                queryParams = "&close=1";
                break;
            case CLICK:
                queryParams = "&click=1";
                break;
        }
        if (queryParams != null) {
            invoke(changeControllerVariablesUri, queryParams);
        }
    }

    private String invoke(String uri) throws OpenGarageCommunicationException {
        return invoke(uri, "");
    }

    private String invoke(String uri, String params) throws OpenGarageCommunicationException {
        String uriWithParams = uri + params;
        logger.debug("Calling url: {}", uriWithParams);
        String response;
        synchronized (this) {
            try {
                response = HttpUtil.executeUrl("GET", uriWithParams, TIMEOUT_MS);
            } catch (IOException ex) {
                logger.debug("{}", ex.getLocalizedMessage(), ex);
                // Response will also be set to null if parsing in executeUrl fails so we use null here to make the
                // error check below consistent.
                response = null;
            }
        }

        if (response == null) {
            throw new OpenGarageCommunicationException(
                    String.format("OpenGaragecontroller returned error while invoking %s", uriWithParams));
        }
        return response;
    }
}
