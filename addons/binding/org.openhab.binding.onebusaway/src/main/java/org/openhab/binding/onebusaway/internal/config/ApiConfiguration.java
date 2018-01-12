/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onebusaway.internal.config;

import static org.openhab.binding.onebusaway.OneBusAwayBindingConstants.*;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The {@link ApiConfiguration} defines the model for a API bridge configuration.
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
        return new ToStringBuilder(this).append(API_CONFIG_API_KEY, this.getApiKey())
                .append(API_CONFIG_API_SERVER, this.getApiServer()).toString();
    }

}
