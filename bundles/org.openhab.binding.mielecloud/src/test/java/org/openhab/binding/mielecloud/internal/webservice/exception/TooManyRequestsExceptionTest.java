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
package org.openhab.binding.mielecloud.internal.webservice.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class TooManyRequestsExceptionTest {
    @Test
    public void testHasRetryAfterHintReturnsFalseWhenNoRetryAfterWasPassedToConstructor() {
        // given:
        TooManyRequestsException exception = new TooManyRequestsException("", null);

        // when:
        boolean result = exception.hasRetryAfterHint();

        // then:
        assertFalse(result);
    }

    @Test
    public void testHasRetryAfterHintReturnsTrueWhenRetryAfterWasPassedToConstructor() {
        // given:
        TooManyRequestsException exception = new TooManyRequestsException("", "25");

        // when:
        boolean result = exception.hasRetryAfterHint();

        // then:
        assertTrue(result);
    }

    @Test
    public void testGetSecondsUntilRetryReturnsMinusOneWhenNoRetryAfterHintIsPresent() {
        // given:
        TooManyRequestsException exception = new TooManyRequestsException("", null);

        // when:
        long result = exception.getSecondsUntilRetry();

        // then:
        assertEquals(-1L, result);
    }

    @Test
    public void testGetSecondsUntilRetryParsesNumber() {
        // given:
        TooManyRequestsException exception = new TooManyRequestsException("", "30");

        // when:
        long result = exception.getSecondsUntilRetry();

        // then:
        assertEquals(30L, result);
    }

    @Test
    public void testGetSecondsUntilRetryParsesDate() {
        // given:
        TooManyRequestsException exception = new TooManyRequestsException("", "Thu, 12 Jan 5015 15:02:30 GMT");

        // when:
        long result = exception.getSecondsUntilRetry();

        // then:
        assertNotEquals(0L, result);
    }

    @Test
    public void testGetSecondsUntilRetryParsesDateFromThePast() {
        // given:
        TooManyRequestsException exception = new TooManyRequestsException("", "Wed, 21 Oct 2015 07:28:00 GMT");

        // when:
        long result = exception.getSecondsUntilRetry();

        // then:
        assertEquals(0L, result);
    }

    @Test
    public void testGetSecondsUntilRetryReturnsMinusOneWhenDateCannotBeParsed() {
        // given:
        TooManyRequestsException exception = new TooManyRequestsException("", "50 Minutes");

        // when:
        long result = exception.getSecondsUntilRetry();

        // then:
        assertEquals(-1L, result);
    }
}
