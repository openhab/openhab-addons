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
package org.openhab.binding.boschshc.internal.services.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JsonRestExceptionResponse}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class JsonRestExceptionResponseTest {

    private @NonNullByDefault({}) JsonRestExceptionResponse fixture;

    @BeforeEach
    public void setUp() throws Exception {
        fixture = new JsonRestExceptionResponse();
    }

    @Test
    public void testIsValid() {
        assertFalse(JsonRestExceptionResponse.isValid(null));
        assertTrue(JsonRestExceptionResponse.isValid(fixture));
        fixture.errorCode = null;
        assertFalse(JsonRestExceptionResponse.isValid(fixture));
        fixture.statusCode = null;
        assertFalse(JsonRestExceptionResponse.isValid(fixture));
        fixture.errorCode = "";
        assertFalse(JsonRestExceptionResponse.isValid(fixture));
    }
}
