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
package org.openhab.binding.caldav.internal.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.ConnectException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Structured transport diagnostics retain the operation and cause without copying sensitive text.
 *
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
class CalDavHttpExceptionTest {
    @Test
    void preservesStatusCodeAndOperationInMessage() {
        CalDavHttpException exception = new CalDavHttpException("PROPFIND", 403);

        assertEquals(403, exception.statusCode());
        assertEquals("PROPFIND", exception.operation());
        assertEquals("CalDAV PROPFIND failed with HTTP status 403", exception.getMessage());
    }

    @Test
    void preservesStructuredCauseWithoutIncludingItsSensitiveText() {
        ConnectException cause = new ConnectException("private-host password");
        CalDavHttpException exception = new CalDavHttpException("REPORT", 500, false, false, cause);
        assertEquals(500, exception.statusCode());
        assertEquals("REPORT", exception.operation());
        assertSame(cause, exception.getCause());
        assertEquals("CalDAV REPORT failed with HTTP status 500", exception.getMessage());
    }
}
