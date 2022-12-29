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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.config.VehicleConfiguration;

/**
 * The {@link ImageTest} Test Image conversions
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ImageTest {

    @Test
    public void testConfig() {
        Optional<VehicleConfiguration> config = Optional.of(new VehicleConfiguration());
        MultiMap<String> parameterMap = new MultiMap<String>();
        parameterMap.add("background", Boolean.toString(config.get().background));
        parameterMap.add("night", Boolean.toString(config.get().night));
        parameterMap.add("cropped", Boolean.toString(config.get().cropped));
        parameterMap.add("roofOpen", Boolean.toString(config.get().roofOpen));
        parameterMap.add("format", config.get().format);
        String params = UrlEncoded.encode(parameterMap, null, false);
        assertEquals("background=false&night=false&cropped=false&roofOpen=false&format=webp", params);

        config.get().background = true;
        config.get().format = "png";
        config.get().cropped = true;
        parameterMap = new MultiMap<String>();
        parameterMap.add("background", Boolean.toString(config.get().background));
        parameterMap.add("night", Boolean.toString(config.get().night));
        parameterMap.add("cropped", Boolean.toString(config.get().cropped));
        parameterMap.add("roofOpen", Boolean.toString(config.get().roofOpen));
        parameterMap.add("format", config.get().format);
        params = UrlEncoded.encode(parameterMap, null, false);
        assertEquals("background=true&night=false&cropped=true&roofOpen=false&format=png", params);
    }
}
