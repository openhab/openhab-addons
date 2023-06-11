/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiContentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Utility class to create JSON content.
 *
 * @author Peter Schraffl - Initial contribution
 */
@NonNullByDefault
public class BsbLanApiContentConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BsbLanApiContentConverter.class);
    private static final Gson GSON = new Gson();

    public static String toJson(BsbLanApiContentDTO request) {
        return GSON.toJson(request);
    }

    public static <T> @Nullable T fromJson(String content, Class<T> resultType) {
        try {
            T result = GSON.fromJson(content, resultType);
            if (result == null) {
                LOGGER.debug("result null after json parsing (response = {})", content);
            }
            return result;
        } catch (JsonSyntaxException e) {
            LOGGER.debug("Parsing JSON API response failed: {}", e.getMessage());
            return null;
        }
    }
}
