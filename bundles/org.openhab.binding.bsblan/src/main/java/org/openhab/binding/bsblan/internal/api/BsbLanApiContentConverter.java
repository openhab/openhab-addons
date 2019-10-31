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

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Utility class to create JSON content.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanApiContentConverter {
    private final static Logger logger = LoggerFactory.getLogger(BsbLanApiContentConverter.class);

    public static String toJson(BsbLanApiContent request) {
        Gson gson = new Gson();
        return gson.toJson(request);
    }

    @Nullable
    public static <T> T fromJson(String content, Class<T> resultType) {
        try {
            Gson gson = new Gson();
            T result = gson.fromJson(content, resultType);
            if (result == null) {
                logger.debug("result null after json parsing (response = {})", content);
            }
            return result;
        } catch (JsonSyntaxException e) {
            logger.debug("Parsing JSON API response failed: {}", e.getMessage());
            return null;
        }
    }
}
