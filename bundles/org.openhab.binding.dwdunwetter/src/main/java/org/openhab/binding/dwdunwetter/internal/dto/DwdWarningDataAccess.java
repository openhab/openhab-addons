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
package org.openhab.binding.dwdunwetter.internal.dto;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the access to the API Endpoint
 *
 * @author Martin Koehler - Initial contribution
 */
public class DwdWarningDataAccess {

    private final Logger logger = LoggerFactory.getLogger(DwdWarningDataAccess.class);

    // URL of the Service
    private static final String DWD_URL = "https://maps.dwd.de/geoserver/dwd/ows?service=WFS&version=2.0.0&request=GetFeature&typeName=dwd:Warnungen_Gemeinden";

    /**
     * Returns the raw Data from the Endpoint.
     * In case of errors or empty cellId value, returns an empty String.
     *
     * @param cellId The warnCell-Id for which the warnings should be returned
     * @return The raw data received or an empty string.
     */
    public String getDataFromEndpoint(String cellId) {
        try {
            if (cellId == null || cellId.isBlank()) {
                logger.warn("No cellId provided");
                return "";
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(DWD_URL);
            stringBuilder.append("&CQL_FILTER=");
            stringBuilder.append(URLEncoder.encode("WARNCELLID LIKE '" + cellId + "'", StandardCharsets.UTF_8));
            logger.debug("Refreshing Data for cell {}", cellId);
            String rawData = getByURL(stringBuilder.toString());
            logger.trace("Raw request: {}", stringBuilder);
            logger.trace("Raw response: {}", rawData);

            if (rawData == null || !rawData.startsWith("<?xml") || !rawData.contains("FeatureCollection")) {
                logger.warn("Communication error occurred while getting data, response is not in expected XML-format");
                return "";
            }
            return rawData;
        } catch (IOException e) {
            logger.warn("Communication error occurred while getting data: {}", e.getMessage());
            logger.debug("Communication error trace", e);
        }

        return "";
    }

    public String getByURL(String url) throws IOException {
        return HttpUtil.executeUrl("GET", url, 5000);
    }
}
