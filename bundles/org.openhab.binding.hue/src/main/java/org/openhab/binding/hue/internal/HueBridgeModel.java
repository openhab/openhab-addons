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
package org.openhab.binding.hue.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for extracting the generation of a Hue Bridge from its model ID.
 * The generation is determined from the bridge model ID, which follows the pattern {@code BSBxxx}.
 * 
 * Currently the following generations are known:
 *
 * <ul>
 * <li>Generation 1: Original Hue Bridge (round white, model ID "BSB001")</li>
 * <li>Generation 2: Hue Bridge (square white, model ID "BSB002")</li>
 * <li>Generation 3: Hue Bridge Pro (square black, model ID "BSB003")</li>
 * </ul>
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public final class HueBridgeModel {

    /**
     * Pattern to extract the generation number from the model ID of the Hue Bridge.
     * The model ID follows the format {@code BSBxxx} where {@code xxx} is a zero-padded generation number,
     * for example {@code BSB002} for generation 2 and {@code BSB003} for generation 3 (Pro).
     */
    private static final Pattern BSB_MODEL_ID_PATTERN = Pattern.compile("^BSB(\\d{3})$");

    private HueBridgeModel() {
        // utility class
    }

    /**
     * Parses the generation of the Hue Bridge from its model ID.
     *
     * @param modelId the model ID of the Hue Bridge, e.g., "BSB001", "BSB002", "BSB003"
     * @return the generation number of the Hue Bridge, or 0 if the model ID is unknown or does not match the expected
     *         pattern
     */
    public static int getGeneration(String modelId) {
        Matcher matcher = BSB_MODEL_ID_PATTERN.matcher(modelId);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : 0;
    }
}
