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
package org.openhab.binding.bsblan.internal.api;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiResponse;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterQueryResponse;
import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


/**
 * Utility class to call the BSB-LAN REST API.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanApiCaller {

    private static final int API_TIMEOUT = 10000;
    private final Logger logger = LoggerFactory.getLogger(BsbLanApiCaller.class);
    private final BsbLanBridgeConfiguration bridgeConfig;
    private final Gson gson;

    public BsbLanApiCaller(BsbLanBridgeConfiguration config) {
        bridgeConfig = config;
        gson = new Gson();
    }

    public BsbLanApiParameterQueryResponse queryParameters(Set<Integer> parameterIds) {
        String apiPath = String.format("/JQ={}", StringUtils.join(parameterIds, ","));
        return makeRestCall(BsbLanApiParameterQueryResponse.class, "GET", apiPath);
    }

    /**
     * @param type response class type
     * @param httpMethod to execute
     * @param url to request
     * @return the object representation of the json response
     */
    @Nullable
    private <T extends BsbLanApiResponse> T makeRestCall(Class<T> type, String httpMethod, String apiPath) {
        try {
            // build url
            StringBuilder url = new StringBuilder();
            url.append("http://");
            if (StringUtils.trimToNull(bridgeConfig.username) != null && StringUtils.trimToNull(bridgeConfig.password) != null) {
                url.append(bridgeConfig.username + ":" + bridgeConfig.password + "@");
            }
            url.append(bridgeConfig.hostname);
            url.append(apiPath);

            logger.debug("URL = {}", url.toString());
            String response = HttpUtil.executeUrl(httpMethod, url.toString(), API_TIMEOUT);

            if (response == null) {
                logger.debug("no response returned");
                return null;
            }

            logger.debug("apiResponse = {}", response);

            T result = gson.fromJson(response, type);
            if (result == null) {
                logger.debug("result null after json parsing (response = {})", response);
                return null;
            }

            return result;
        } catch (JsonSyntaxException | IOException | IllegalStateException e) {
            logger.debug("Error running bsb-lan request: {}", e.getMessage());
            return null;
        }
    }
}
