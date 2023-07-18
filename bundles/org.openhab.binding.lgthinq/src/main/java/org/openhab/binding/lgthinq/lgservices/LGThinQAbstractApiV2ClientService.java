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
package org.openhab.binding.lgthinq.lgservices;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.V2_CTRL_DEVICE_CONFIG_PATH;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.api.RestUtils;
import org.openhab.binding.lgthinq.internal.api.TokenResult;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.AbstractSnapshotDefinition;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.ResultCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The {@link LGThinQAbstractApiV2ClientService}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class LGThinQAbstractApiV2ClientService<C extends CapabilityDefinition, S extends AbstractSnapshotDefinition>
        extends LGThinQAbstractApiClientService<C, S> {
    private static final Logger logger = LoggerFactory.getLogger(LGThinQAbstractApiV2ClientService.class);

    protected LGThinQAbstractApiV2ClientService(Class<C> capabilityClass, Class<S> snapshotClass, HttpClient httpClient) {
        super(capabilityClass, snapshotClass, httpClient);
    }

    @Override	
    protected RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey,
            String command, String keyName, String value) throws Exception {
        return sendCommand(bridgeName, deviceId, controlPath, controlKey, command, keyName, value, null);
    }

    protected RestResult postCall(String bridgeName, String deviceId, String controlPath, String payload)
            throws LGThinqApiException, IOException {
        TokenResult token = tokenManager.getValidRegisteredToken(bridgeName);
        UriBuilder builder = UriBuilder.fromUri(token.getGatewayInfo().getApiRootV2())
                .path(String.format(V2_CTRL_DEVICE_CONFIG_PATH, deviceId, controlPath));
        Map<String, String> headers = getCommonV2Headers(token.getGatewayInfo().getLanguage(),
                token.getGatewayInfo().getCountry(), token.getAccessToken(), token.getUserInfo().getUserNumber());
        RestResult resp = RestUtils.postCall(httpClient, builder.build().toURL().toString(), headers, payload);
        if (resp == null) {
            logger.warn("Null result returned sending command to LG API V2: {}, {}, {}", deviceId, controlPath, payload);
            throw new LGThinqApiException("Null result returned sending command to LG API V2");
        }
        return resp;
    }

    @Override
    public RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey,
            String command, @Nullable String keyName, @Nullable String value, @Nullable ObjectNode extraNode)
            throws Exception {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("ctrlKey", controlKey).put("command", command).put("dataKey", keyName).put("dataValue", value);
        if (extraNode != null) {
            payload.setAll(extraNode);
        }
        return postCall(bridgeName, deviceId, controlPath, payload.toPrettyString());
    }

    protected RestResult sendBasicControlCommands(String bridgeName, String deviceId, String command, String keyName,
            int value) throws Exception {
        return sendCommand(bridgeName, deviceId, "control-sync", "basicCtrl", command, keyName, String.valueOf(value));
    }

    @Override
    protected Map<String, Object> handleGenericErrorResult(@Nullable RestResult resp) throws LGThinqApiException {
        Map<String, Object> metaResult;
        if (resp == null) {
            return Collections.emptyMap();
        }
        if (resp.getStatusCode() != 200) {
            if (resp.getStatusCode() == 400) {
                logger.warn("Error returned by LG Server API. HTTP Status: {}. The reason is: {}", resp.getStatusCode(), resp.getJsonResponse());
                return Collections.emptyMap();
            } else {
                logger.error("Error returned by LG Server API. HTTP Status: {}. The reason is: {}", resp.getStatusCode(), resp.getJsonResponse());
                throw new LGThinqApiException(
                        String.format("Error returned by LG Server API. HTTP Status: %s. The reason is: %s", resp.getStatusCode(), resp.getJsonResponse()));
            }
        } else {
            try {
                metaResult = objectMapper.readValue(resp.getJsonResponse(), new TypeReference<>() {
                });
                String code = (String) metaResult.get("resultCode");
                if (!ResultCodes.OK.containsResultCode(String.valueOf(metaResult.get("resultCode")))) {
                    logger.error("LG API report error processing the request -> resultCode=[{}], message=[{}]", code,
                            getErrorCodeMessage(code));
                    throw new LGThinqApiException(
                            String.format("Status error executing endpoint. resultCode must be 0000, but was:%s",
                                    metaResult.get("resultCode")));
                }
                return metaResult;
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unknown error occurred deserializing json stream", e);
            }
        }
    }
}
