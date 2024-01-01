/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link BeWaterWise} class contains the logic to get data the
 * bewaterwise.org.nz website.
 * 
 * Northland Regional Council
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class BeWaterWise implements WaterWebService {
    private final Logger logger = LoggerFactory.getLogger(BeWaterWise.class);

    private static final String HOSTNAME = "https://bewaterwise.org.nz";
    private static final String REGION_FARNORTH = "/current-water-levels_far-north/";
    private static final String REGION_WHANGAREI = "/current-water-levels_whangarei/";
    private static final String REGION_KAIPARA = "/current-water-levels_kaipara/";

    private static final String PATTERN = "vc_text_separator.*?<span>(.*?)<\\/span>.*?water-level-([0-4]).*?";
    private static final Pattern REGEX = Pattern.compile(PATTERN,
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    @Override
    public String service() {
        return "bewaterwise";
    }

    @Override
    public String endpoint(final String region) {
        switch (region.toLowerCase()) {
            case "farnorth":
                return HOSTNAME + REGION_FARNORTH;

            case "whangarei":
                return HOSTNAME + REGION_WHANGAREI;

            case "kaipara":
                return HOSTNAME + REGION_KAIPARA;
        }
        return "";
    }

    @Override
    public int findWaterLevel(final String data, final String area) {
        final Matcher matches = REGEX.matcher(data);

        while (matches.find()) {
            final String dataArea = matches.group(1).replaceAll("\\W", "");
            final String level = matches.group(2);
            logger.debug("Data Area {} Level {}", dataArea, level);
            if (dataArea.equalsIgnoreCase(area)) {
                return Integer.valueOf(level);
            }
        }
        return -1;
    }
}
