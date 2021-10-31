/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.evnotify.api;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the {@link ApiVersion} class
 *
 * @author Michael Schmidt - Initial contribution
 */
class ApiVersionTest {

    @Test
    void shouldGetVersions() {
        // given

        // when
        ApiVersion v2 = ApiVersion.getApiVersion("V2");
        ApiVersion v3 = ApiVersion.getApiVersion("V3");

        // then
        assertEquals(ApiVersion.V2, v2);
        assertEquals(ApiVersion.V3, v3);
    }

    @Test
    void shouldHandleNullVersions() {
        // given
        String versionToBeTested = null;

        // when
        assertThrows(IllegalArgumentException.class, () -> ApiVersion.getApiVersion(versionToBeTested));

        // then
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidVersion() {
        // given
        String versionToBeTested = "V999";

        // when
        assertThrows(IllegalArgumentException.class, () -> ApiVersion.getApiVersion(versionToBeTested));

        // then
    }

}