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
package org.openhab.binding.hue.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 *
 *
 * @author Samuel Leisering - Initial contribution
 */
public class ApiVersionTest {

    @Test
    public void testKnownVersions() {
        String version = "1.0";
        assertTrue(ApiVersion.of(version).compare(new ApiVersion(1, 0, 0)) == 0);

        version = "1.0.0";
        assertTrue(ApiVersion.of(version).compare(new ApiVersion(1, 0, 0)) == 0);

        version = "1.10.0";
        assertTrue(ApiVersion.of(version).compare(new ApiVersion(1, 10, 0)) == 0);

        version = "1.2.1";
        assertTrue(ApiVersion.of(version).compare(new ApiVersion(1, 2, 1)) == 0);
    }

    @Test
    public void testCompare() {
        ApiVersion v = new ApiVersion(1, 2, 3);

        assertTrue(v.compare(new ApiVersion(1, 2, 3)) == 0);

        assertTrue(v.compare(new ApiVersion(1, 2, 2)) > 0);
        assertTrue(v.compare(new ApiVersion(1, 2, 4)) < 0);

        assertTrue(v.compare(new ApiVersion(1, 1, 3)) > 0);
        assertTrue(v.compare(new ApiVersion(1, 3, 3)) < 0);

        assertTrue(v.compare(new ApiVersion(0, 2, 3)) > 0);
        assertTrue(v.compare(new ApiVersion(3, 2, 3)) < 0);
    }
}
