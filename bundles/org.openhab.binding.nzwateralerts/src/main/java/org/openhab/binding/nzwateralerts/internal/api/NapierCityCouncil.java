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
package org.openhab.binding.nzwateralerts.internal.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NapierCityCouncil} class contains the logic to get data the
 * www.napier.govt.nz website.
 * 
 * Napier City Council
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class NapierCityCouncil implements WaterWebService {
    private final Logger logger = LoggerFactory.getLogger(NapierCityCouncil.class);

    private static final String HOSTNAME = "https://www.napier.govt.nz";
    private static final String REGION_NAPIER = "/services/water/water-restrictions/";

    private static final String PATTERN = "\"waterstat\".*?<p>.*?at (.*?) Restrictions.*?</div>";
    private static final Pattern REGEX = Pattern.compile(PATTERN,
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    @Override
    public String service() {
        return "napiercitycouncil";
    }

    @Override
    public String endpoint(final String region) {
        switch (region.toLowerCase()) {
            case "napier":
                return HOSTNAME + REGION_NAPIER;

        }
        return "";
    }

    @Override
    public int findWaterLevel(final String data, final String area) {
        final Matcher matches = REGEX.matcher(data);

        while (matches.find()) {
            final String level = matches.group(1);
            logger.debug("Data Level {}", level);

            switch (level.toLowerCase()) {
                case "no":
                    return 0;

                case "level one":
                    return 1;

                case "level two":
                    return 2;

                case "level three":
                    return 3;

                case "level four":
                    return 4;
            }

        }
        return -1;
    }
}
