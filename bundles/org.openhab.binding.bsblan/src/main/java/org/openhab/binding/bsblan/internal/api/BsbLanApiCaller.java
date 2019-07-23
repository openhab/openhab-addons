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
import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.LongSerializationPolicy;

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
        if (parameterIds.size() == 0) {
            return null;
        }
        String apiPath = String.format("/JQ=%s", StringUtils.join(parameterIds, ","));

        return makeRestCall(BsbLanApiParameterQueryResponse.class, "GET", apiPath, "");
    }

    public BsbLanApiParameterSetResponse setParameter(Integer parameterId, String value, BsbLanApiParameterSetRequest.Type type) {
        BsbLanApiParameterSetRequest request = new BsbLanApiParameterSetRequest();
        request.setParameter(parameterId);
        request.setValue(value);
        request.setType(type);

        Gson gson = new GsonBuilder()
                .setLongSerializationPolicy(LongSerializationPolicy.STRING)
                .create();

        String content = gson.toJson(request);
        return makeRestCall(BsbLanApiParameterSetResponse.class, "POST", "/JS", content);
    }

    private String createApiBaseUrl() {
        StringBuilder url = new StringBuilder();
        url.append("http://");
        if (StringUtils.trimToNull(bridgeConfig.username) != null && StringUtils.trimToNull(bridgeConfig.password) != null) {
            url.append(bridgeConfig.username + ":" + bridgeConfig.password + "@");
        }
        url.append(bridgeConfig.hostname);
        if (StringUtils.trimToNull(bridgeConfig.passkey) != null) {
            url.append("/" + bridgeConfig.passkey);
        }
        return url.toString();
    }

    /**
     * @param responseType response class type
     * @param httpMethod to execute
     * @param apiPath to request
     * @return the object representation of the json response
     */
    @Nullable
    private <T> T makeRestCall(Class<T> responseType, String httpMethod, String apiPath, String content) {
        try {
            String url = createApiBaseUrl() + apiPath;
            logger.debug("api request url = '{}''", url);

            InputStream contentStream = null;
            String contentType = null;
            if (StringUtils.trimToNull(content) != null) {
                contentStream = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
                contentType = "application/json";
            }

            String response = HttpUtil.executeUrl(httpMethod, url, contentStream, contentType, API_TIMEOUT);
            if (response == null) {
                logger.debug("no response returned");
                return null;
            }

            logger.debug("apiResponse = {}", response);

            Gson gson = new Gson();
            T result = gson.fromJson(response, responseType);
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
