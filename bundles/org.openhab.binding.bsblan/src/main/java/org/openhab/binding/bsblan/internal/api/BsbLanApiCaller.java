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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.nio.charset.Charset;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterQueryResponse;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterSetRequest;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterSetResponse;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterSetResult;
import org.openhab.binding.bsblan.internal.api.BsbLanApiContentConverter;
import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.bsblan.internal.BsbLanBindingConstants.*;

/**
 * Utility class to call the BSB-LAN REST API.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanApiCaller {

    private final Logger logger = LoggerFactory.getLogger(BsbLanApiCaller.class);
    private final BsbLanBridgeConfiguration bridgeConfig;

    public BsbLanApiCaller(BsbLanBridgeConfiguration config) {
        bridgeConfig = config;
    }

    public BsbLanApiParameterQueryResponse queryParameter(Integer parameterId) {
        Set<Integer> parameters = new HashSet<Integer>();
        parameters.add(parameterId);
        return queryParameters(parameters);
    }

    public BsbLanApiParameterQueryResponse queryParameters(Set<Integer> parameterIds) {
        // make the request even if parameterIds is empty, thing OFFLINE/ONLINE detection relies on a response
        // if (parameterIds.size() == 0) {
        //     return null;
        // }
        String apiPath = String.format("/JQ=%s", StringUtils.join(parameterIds, ","));

        return makeRestCall(BsbLanApiParameterQueryResponse.class, "GET", apiPath, null);
    }

    public boolean setParameter(Integer parameterId, String value, BsbLanApiParameterSetRequest.Type type) {
        // prepare request content
        BsbLanApiParameterSetRequest request = new BsbLanApiParameterSetRequest();
        request.parameter = parameterId.toString();
        request.value = value;
        request.type = type;

        // make REST call and process response
        BsbLanApiParameterSetResponse setResponse = makeRestCall(BsbLanApiParameterSetResponse.class, "POST", "/JS", request);
        if (setResponse == null) {
            logger.warn("Failed to set parameter {} to '{}': no response received", parameterId, value);
            return false;
        }

        BsbLanApiParameterSetResult result = setResponse.getOrDefault(parameterId, null);
        if (result == null) {
            logger.warn("Failed to set parameter {} to '{}'': result is null", parameterId, value);
            return false;
        }
        if (result.status == null) {
            logger.warn("Failed to set parameter {} to '{}': status is null", parameterId, value);
            return false;
        }
        if (result.status != BsbLanApiParameterSetResult.Status.SUCCESS) {
            logger.info("Failed to set parameter {} to '{}': status = {}", parameterId, value, result.status);
            return false;
        }
        return true;
    }

    private String createApiBaseUrl() {
        StringBuilder url = new StringBuilder();
        url.append("http://");
        if (StringUtils.trimToNull(bridgeConfig.username) != null && StringUtils.trimToNull(bridgeConfig.password) != null) {
            url.append(bridgeConfig.username + ":" + bridgeConfig.password + "@");
        }
        url.append(bridgeConfig.host);
        if (bridgeConfig.port != 80) {
            url.append(":" + bridgeConfig.host.toString());
        }
        if (StringUtils.trimToNull(bridgeConfig.passkey) != null) {
            url.append("/" + bridgeConfig.passkey);
        }
        return url.toString();
    }

    /**
     * @param responseType response class type
     * @param httpMethod to execute
     * @param apiPath to request
     * @param content to add to request
     * @return the object representation of the json response
     */
    @Nullable
    private <T> T makeRestCall(Class<T> responseType, String httpMethod, String apiPath, BsbLanApiContent request) {
        try {
            String url = createApiBaseUrl() + apiPath;
            logger.debug("api request url = '{}'", url);

            InputStream contentStream = null;
            String contentType = null;
            if (request != null) {
                String content = BsbLanApiContentConverter.toJson(request);
                logger.debug("api request content: '{}'", content);
                if (StringUtils.trimToNull(content) != null) {
                    contentStream = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
                    contentType = "application/json";
                }
            }

            String response = HttpUtil.executeUrl(httpMethod, url, contentStream, contentType, API_TIMEOUT);
            if (response == null) {
                logger.debug("no response returned");
                return null;
            }

            logger.debug("api response content: '{}'", response);
            return BsbLanApiContentConverter.fromJson(response, responseType);
        } catch (IOException | IllegalStateException e) {
            logger.debug("Error executing bsb-lan api request: {}", e.getMessage());
            return null;
        }
    }
}
