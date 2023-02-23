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
package org.openhab.binding.boschshc.internal.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LongPollingFailedException}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class LongPollingFailedExceptionTest {

    @Test
    public void testConstructor() {
        RuntimeException testException = new RuntimeException("test exception");
        LongPollingFailedException longPollingFailedException = new LongPollingFailedException("message",
                testException);
        assertEquals("message", longPollingFailedException.getMessage());
        assertSame(testException, longPollingFailedException.getCause());
    }
}
