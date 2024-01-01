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
package org.openhab.binding.onebusaway.internal.config;

import static org.openhab.binding.onebusaway.internal.OneBusAwayBindingConstants.*;

/**
 * The {@link ApiConfiguration} defines the model for an API bridge configuration.
 *
 * @author Shawn Wilsher - Initial contribution
 */
public class ApiConfiguration {
    private String apiKey;
    private String apiServer;

    /**
     * @return the API Key to access the OneBusAway server.
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API Key for the OneBusAway server.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return the OneBusAway API server to use.
     */
    public String getApiServer() {
        return apiServer;
    }

    /**
     * Sets the OneBusAway API server.
     */
    public void setApiServer(String apiServer) {
        this.apiServer = apiServer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ " + API_CONFIG_API_KEY + "=" + this.getApiKey() + ", "
                + API_CONFIG_API_SERVER + "=" + this.getApiServer() + "}";
    }
}
