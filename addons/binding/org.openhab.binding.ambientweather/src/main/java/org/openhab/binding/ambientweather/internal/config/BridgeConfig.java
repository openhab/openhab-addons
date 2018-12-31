/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.config;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link BridgeConfig} is responsible for storing the
 * Ambient Weather bridge thing configuration.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class BridgeConfig {
    /**
     * API key
     */
    private String apiKey;

    /**
     * Application key
     */
    private String applicationKey;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public boolean isValid() {
        if (StringUtils.isBlank(apiKey)) {
            return false;
        }
        if (StringUtils.isBlank(applicationKey)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AmbientWeatherBridgeConfig{ apiKey=" + apiKey + ", applicationKey=" + applicationKey + " }";
    }
}
