/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.api;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.api.model.GatewayResult;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@link LGThinqCanonicalModelUtil} class - Utilities to help communication with LG API
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqCanonicalModelUtil {
    public static ObjectMapper mapper = JsonMapper.builder().configureForJackson2().build();

    /**
     * Get structured result from the LG Authentication Gateway
     *
     * @param rawJson RAW Json to process
     * @return Structured Object returned from the API
     * @throws JacksonException If the JSON can not be parsed.
     */
    public static GatewayResult getGatewayResult(String rawJson) throws JacksonException {
        Map<String, Object> map = mapper.readValue(rawJson, new TypeReference<>() {
        });
        @SuppressWarnings("unchecked")
        Map<String, String> content = (Map<String, String>) map.get("result");
        String resultCode = (String) map.get("resultCode");
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Unexpected result. Gateway Content Result is null");
        } else if (resultCode == null) {
            throw new IllegalArgumentException("Unexpected result. resultCode code is null");
        }

        return new GatewayResult(Objects.requireNonNull(resultCode, "Expected resultCode field in json"), "",
                Objects.requireNonNull(content.get("rtiUri"), "Expected rtiUri field in json"),
                Objects.requireNonNull(content.get("thinq1Uri"), "Expected thinq1Uri field in json"),
                Objects.requireNonNull(content.get("thinq2Uri"), "Expected thinq2Uri field in json"),
                Objects.requireNonNull(content.get("empUri"), "Expected empUri field in json"),
                Objects.requireNonNull(content.get("empTermsUri"), "Expected empTermsUri field in json"), "",
                Objects.requireNonNull(content.get("empSpxUri"), "Expected empSpxUri field in json"));
    }
}
