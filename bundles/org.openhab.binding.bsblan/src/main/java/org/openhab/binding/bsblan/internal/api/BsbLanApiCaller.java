/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.bsblan.internal.BsbLanBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiContentDTO;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterQueryResponseDTO;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterSetRequestDTO;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterSetResponseDTO;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterSetResultDTO;
import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to call the BSB-LAN REST API.
 *
 * @author Peter Schraffl - Initial contribution
 */
@NonNullByDefault
public class BsbLanApiCaller {

    private final Logger logger = LoggerFactory.getLogger(BsbLanApiCaller.class);
    private final BsbLanBridgeConfiguration bridgeConfig;

    public BsbLanApiCaller(BsbLanBridgeConfiguration config) {
        bridgeConfig = config;
    }

    public @Nullable BsbLanApiParameterQueryResponseDTO queryParameter(Integer parameterId) {
        Set<Integer> parameters = new HashSet<>();

        parameters.add(parameterId);
        return queryParameters(parameters);
    }

    public @Nullable BsbLanApiParameterQueryResponseDTO queryParameters(Set<Integer> parameterIds) {
        // note: make the request even if parameterIds is empty as
        // thing OFFLINE/ONLINE detection relies on a response

        String apiPath = String.format("/JQ=%s", StringUtils.join(parameterIds, ","));
        return makeRestCall(BsbLanApiParameterQueryResponseDTO.class, "GET", apiPath, null);
    }

    public boolean setParameter(Integer parameterId, String value, BsbLanApiParameterSetRequestDTO.Type type) {
        // prepare request content
        BsbLanApiParameterSetRequestDTO request = new BsbLanApiParameterSetRequestDTO();
        request.parameter = parameterId.toString();
        request.value = value;
        request.type = type;

        // make REST call and process response
        BsbLanApiParameterSetResponseDTO setResponse = makeRestCall(BsbLanApiParameterSetResponseDTO.class, "POST",
                "/JS", request);
        if (setResponse == null) {
            logger.debug("Failed to set parameter {} to '{}': no response received", parameterId, value);
            return false;
        }

        BsbLanApiParameterSetResultDTO result = setResponse.getOrDefault(parameterId, null);
        if (result == null) {
            logger.debug("Failed to set parameter {} to '{}'': result is null", parameterId, value);
            return false;
        }
        if (result.status == null) {
            logger.debug("Failed to set parameter {} to '{}': status is null", parameterId, value);
            return false;
        }
        if (result.status != BsbLanApiParameterSetResultDTO.Status.SUCCESS) {
            logger.debug("Failed to set parameter {} to '{}': status = {}", parameterId, value, result.status);
            return false;
        }
        return true;
    }

    private String createApiBaseUrl() {
        final String host = StringUtils.trimToEmpty(bridgeConfig.host);
        final String username = StringUtils.trimToEmpty(bridgeConfig.username);
        final String password = StringUtils.trimToEmpty(bridgeConfig.password);
        final String passkey = StringUtils.trimToEmpty(bridgeConfig.passkey);

        StringBuilder url = new StringBuilder();
        url.append("http://");
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            url.append(username).append(":").append(password).append("@");
        }
        url.append(host);
        if (bridgeConfig.port != 80) {
            url.append(":").append(bridgeConfig.port);
        }
        if (StringUtils.isNotBlank(passkey)) {
            url.append("/").append(passkey);
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
    private <T> @Nullable T makeRestCall(Class<T> responseType, String httpMethod, String apiPath,
            @Nullable BsbLanApiContentDTO request) {
        try {
            String url = createApiBaseUrl() + apiPath;
            logger.trace("api request url = '{}'", url);

            InputStream contentStream = null;
            String contentType = null;
            if (request != null) {
                String content = BsbLanApiContentConverter.toJson(request);
                logger.trace("api request content: '{}'", content);
                if (StringUtils.isNotBlank(content)) {
                    contentStream = new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
                    contentType = "application/json";
                }
            }

            String response = HttpUtil.executeUrl(httpMethod, url, contentStream, contentType, API_TIMEOUT);
            if (response == null) {
                logger.debug("no response returned");
                return null;
            }

            logger.trace("api response content: '{}'", response);
            return BsbLanApiContentConverter.fromJson(response, responseType);
        } catch (IOException | IllegalStateException e) {
            logger.debug("Error executing bsb-lan api request: {}", e.getMessage());
            return null;
        }
    }
}
