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
package org.openhab.binding.lgthinq.internal.api;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.openhab.binding.lgthinq.internal.api.model.GatewayResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link LGThinqCanonicalModelUtil} class
 *
 * @author Nemer Daud - Initial contribution
 */
public class LGThinqCanonicalModelUtil {
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final String LG_ROOT_TAG_V1 = "lgedmRoot";

    public static GatewayResult getGatewayResult(String rawJson) throws IOException {
        Map<String, Object> map = mapper.readValue(rawJson, new TypeReference<>() {
        });
        Map<String, String> content = (Map<String, String>) map.get("result");
        String resultCode = (String) map.get("resultCode");
        if (content == null) {
            throw new IllegalArgumentException("Enexpected result. Gateway Content Result is null");
        } else if (resultCode == null) {
            throw new IllegalArgumentException("Enexpected result. resultCode code is null");
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
