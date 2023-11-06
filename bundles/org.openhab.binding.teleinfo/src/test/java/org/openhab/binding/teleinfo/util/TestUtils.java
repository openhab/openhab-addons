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
package org.openhab.binding.teleinfo.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public final class TestUtils {

    private TestUtils() {
        // private constructor
    }

    public static File getTestFile(String testResourceName) {
        URL url = TestUtils.class.getClassLoader().getResource(testResourceName);
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
