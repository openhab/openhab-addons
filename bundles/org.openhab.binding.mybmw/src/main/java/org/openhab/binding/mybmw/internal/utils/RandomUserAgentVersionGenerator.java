/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mybmw.internal.utils;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RandomUserAgentVersionGenerator} generates random version numbers for the X-User-Agent in the format
 * xxxxxx.xxx
 *
 * @author Martin Grassl - Initial contribution
 */
@NonNullByDefault
public interface RandomUserAgentVersionGenerator {

    /**
     * this method generates the random UserAgent version
     * 
     * @return formatted String xxxxxx.xxx
     */
    static String getRandomUserAgentVersion() {
        int decimalPlaces = 3;
        int min = 250000;
        int max = 999999;

        double random = min + (max - min) * Math.random();

        // with the US locale we ensure that the decimal separator is a dot
        return String.format(Locale.US, "%." + decimalPlaces + "f", random);
    }
}
