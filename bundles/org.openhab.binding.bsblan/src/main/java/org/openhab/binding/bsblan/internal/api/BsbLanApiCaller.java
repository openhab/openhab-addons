/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.bsblan.internal.BsbLanBindingConstants.API_TIMEOUT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiContentDTO;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterQueryResponseDTO;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterSetRequestDTO;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterSetResponseDTO;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterSetResultDTO;
import org.openhab.binding.bsblan.internal.configuration.BsbLanBridgeConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
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
        return queryParameters(Set.of(parameterId));
    }

    public @Nullable BsbLanApiParameterQueryResponseDTO queryParameters(Set<Integer> parameterIds) {
        // note: make the request even if parameterIds is empty as
        // thing OFFLINE/ONLINE detection relies on a response
        String apiPath = String.format("/JQ=%s",
                parameterIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
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
        final String host = bridgeConfig.host.trim();
        final String username = bridgeConfig.username.trim();
        final String password = bridgeConfig.password.trim();
        final String passkey = bridgeConfig.passkey.trim();

        StringBuilder url = new StringBuilder();
        url.append("http://");
        if (!username.isBlank() && !password.isBlank()) {
            url.append(username).append(":").append(password).append("@");
        }
        url.append(host);
        if (bridgeConfig.port != 80) {
            url.append(":").append(bridgeConfig.port);
        }
        if (!passkey.isBlank()) {
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
                if (!content.isBlank()) {
                    contentStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
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
