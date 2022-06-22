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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;

import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.VehicleConfiguration;

/**
 * The {@link ImageTest} Test Image conversions
 *
 * @author Bernd Weymann - Initial contribution
 */
class ImageTest {

    void testImageBytes() {
        byte[] content = FileReader.readFileInBytes("src/test/resources/UIScreenshot.png");
        System.out.println(content.length);
        System.out.println(content);
        String base64 = Base64.getEncoder().encodeToString(content);
        System.out.println(base64.length());
        // System.out.println(base64);
        byte[] base64Decoded = Base64.getDecoder().decode(base64);
        File outputFile = new File("src/test/resources/UIScreenshot-rewrite.png");
        try {
            Files.write(outputFile.toPath(), base64Decoded);
        } catch (IOException e) {
        }
    }

    @Test
    public void testConfig() {
        Optional<VehicleConfiguration> config = Optional.of(new VehicleConfiguration());
        MultiMap<String> parameterMap = new MultiMap<String>();
        parameterMap.add("background", Boolean.toString(config.get().roofOpen));
        parameterMap.add("night", Boolean.toString(config.get().roofOpen));
        parameterMap.add("cropped", Boolean.toString(config.get().roofOpen));
        parameterMap.add("roofOpen", Boolean.toString(config.get().roofOpen));
        parameterMap.add("format", config.get().format);
        String params = UrlEncoded.encode(parameterMap, null, false);
        System.out.println(params);
    }
}
