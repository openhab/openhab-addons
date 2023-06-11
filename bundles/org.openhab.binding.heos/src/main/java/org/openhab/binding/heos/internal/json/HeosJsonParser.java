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
package org.openhab.binding.heos.internal.json;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.json.dto.HeosCommandTuple;
import org.openhab.binding.heos.internal.json.dto.HeosEvent;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Parser used for parsing the responses of JSON cli
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosJsonParser {
    private final Logger logger = LoggerFactory.getLogger(HeosJsonParser.class);

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public HeosEventObject parseEvent(String jsonBody) {
        HeosJsonWrapper wrapper = gson.fromJson(jsonBody, HeosJsonWrapper.class);

        return postProcess(wrapper.heos);
    }

    private HeosEventObject postProcess(HeosJsonObject heos) {
        return new HeosEventObject(HeosEvent.valueOfString(heos.command), heos.command, splitQuery(heos.message));
    }

    public <T> HeosResponseObject<T> parseResponse(String jsonBody, Class<T> clazz) {
        HeosJsonWrapper wrapper = gson.fromJson(jsonBody, HeosJsonWrapper.class);
        return postProcess(Objects.requireNonNull(wrapper), clazz);
    }

    private <T> HeosResponseObject<T> postProcess(HeosJsonWrapper wrapper, Class<T> clazz) {
        T payload = gson.fromJson(wrapper.payload, clazz);

        return new HeosResponseObject<>(HeosCommandTuple.valueOf(wrapper.heos.command), wrapper.heos.command,
                wrapper.heos.result, splitQuery(wrapper.heos.message), payload, wrapper.options);
    }

    private Map<String, String> splitQuery(@Nullable String url) {
        if (url == null) {
            return Collections.emptyMap();
        }

        return Arrays.stream(url.split("&")).map(p -> p.split("=", 2))
                .collect(Collectors.toMap(v -> decode(v[0]), v -> v.length == 1 ? "" : decode(v[1]), this::merge));
    }

    /**
     * for duplicates we ignore the first one
     *
     * @param v1 first occurrence
     * @param v2 second occurrence
     * @return second occurrence
     */
    private String merge(String v1, String v2) {
        logger.debug("Ignoring first occurrence '{}' in favor of '{}'", v1, v2);
        return v2;
    }

    private static String decode(String encoded) {
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }
}
