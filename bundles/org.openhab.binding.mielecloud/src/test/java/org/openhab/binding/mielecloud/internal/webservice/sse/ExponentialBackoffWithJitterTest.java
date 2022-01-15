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
package org.openhab.binding.mielecloud.internal.webservice.sse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ExponentialBackoffWithJitterTest {
    private static final long RETRY_INTERVAL = 2;
    private static final long ALTERNATIVE_RETRY_INTERVAL = 50;
    private static final long MINIMUM_WAIT_TIME = 1;
    private static final long ALTERNATIVE_MINIMUM_WAIT_TIME = 2;
    private static final long MAXIMUM_WAIT_TIME = 100;
    private static final long ALTERNATIVE_MAXIMUM_WAIT_TIME = 150;

    @Test
    public void whenMinimumWaitTimeIsSmallerThanZeroThenAnIllegalArgumentExceptionIsThrown() {
        // when:
        assertThrows(IllegalArgumentException.class, () -> {
            new ExponentialBackoffWithJitter(-MINIMUM_WAIT_TIME, MAXIMUM_WAIT_TIME, RETRY_INTERVAL);
        });
    }

    @Test
    public void whenMaximumWaitTimeIsSmallerThanZeroThenAnIllegalArgumentExceptionIsThrown() {
        // when:
        assertThrows(IllegalArgumentException.class, () -> {
            new ExponentialBackoffWithJitter(MINIMUM_WAIT_TIME, -MAXIMUM_WAIT_TIME, RETRY_INTERVAL);
        });
    }

    @Test
    public void whenRetryIntervalIsSmallerThanZeroThenAnIllegalArgumentExceptionIsThrown() {
        // when:
        assertThrows(IllegalArgumentException.class, () -> {
            new ExponentialBackoffWithJitter(MINIMUM_WAIT_TIME, MAXIMUM_WAIT_TIME, -RETRY_INTERVAL);
        });
    }

    @Test
    public void whenMinimumWaitTimeIsLargerThanMaximumWaitTimeThenAnIllegalArgumentExceptionIsThrown() {
        // when:
        assertThrows(IllegalArgumentException.class, () -> {
            new ExponentialBackoffWithJitter(MAXIMUM_WAIT_TIME, MINIMUM_WAIT_TIME, RETRY_INTERVAL);
        });
    }

    @Test
    public void whenRetryIntervalIsLargerThanMaximumWaitTimeThenAnIllegalArgumentExceptionIsThrown() {
        // when:
        assertThrows(IllegalArgumentException.class, () -> {
            new ExponentialBackoffWithJitter(MINIMUM_WAIT_TIME, RETRY_INTERVAL, MAXIMUM_WAIT_TIME);
        });
    }

    @Test
    public void whenTheNumberOfFailedAttemptsIsNegativeThenZeroIsAssumedInstead() {
        // given:
        Random random = mock(Random.class);
        when(random.nextLong()).thenReturn(RETRY_INTERVAL);

        ExponentialBackoffWithJitter backoffStrategy = new ExponentialBackoffWithJitter(MINIMUM_WAIT_TIME,
                MAXIMUM_WAIT_TIME, RETRY_INTERVAL, random);

        // when:
        long result = backoffStrategy.getSecondsUntilRetry(-10);

        // then:
        assertEquals(MINIMUM_WAIT_TIME + RETRY_INTERVAL, result);
    }

    @Test
    public void whenThereIsNoFailedAttemptThenTheMaximalResultIsMinimumWaitTimePlusRetryInterval() {
        // given:
        Random random = mock(Random.class);
        when(random.nextLong()).thenReturn(RETRY_INTERVAL);

        ExponentialBackoffWithJitter backoffStrategy = new ExponentialBackoffWithJitter(MINIMUM_WAIT_TIME,
                MAXIMUM_WAIT_TIME, RETRY_INTERVAL, random);

        // when:
        long result = backoffStrategy.getSecondsUntilRetry(0);

        // then:
        assertEquals(MINIMUM_WAIT_TIME + RETRY_INTERVAL, result);
    }

    @Test
    public void whenThereIsOneFailedAttemptThenTheMaximalResultIsMinimumWaitTimePlusTwiceTheRetryInterval() {
        // given:
        Random random = mock(Random.class);
        when(random.nextLong()).thenReturn(RETRY_INTERVAL * 2);

        ExponentialBackoffWithJitter backoffStrategy = new ExponentialBackoffWithJitter(MINIMUM_WAIT_TIME,
                MAXIMUM_WAIT_TIME, RETRY_INTERVAL, random);

        // when:
        long result = backoffStrategy.getSecondsUntilRetry(1);

        // then:
        assertEquals(MINIMUM_WAIT_TIME + RETRY_INTERVAL * 2, result);
    }

    @Test
    public void whenThereAreTwoFailedAttemptsThenTheMaximalResultIsMinimumWaitTimePlusFourTimesTheRetryInterval() {
        // given:
        Random random = mock(Random.class);
        when(random.nextLong()).thenReturn(RETRY_INTERVAL * 4);

        ExponentialBackoffWithJitter backoffStrategy = new ExponentialBackoffWithJitter(MINIMUM_WAIT_TIME,
                MAXIMUM_WAIT_TIME, RETRY_INTERVAL, random);

        // when:
        long result = backoffStrategy.getSecondsUntilRetry(2);

        // then:
        assertEquals(MINIMUM_WAIT_TIME + RETRY_INTERVAL * 4, result);
    }

    @Test
    public void whenThereAreTwoFailedAttemptsThenTheMinimalResultIsTheMinimumWaitTime() {
        // given:
        Random random = mock(Random.class);
        when(random.nextLong()).thenReturn(0L);

        ExponentialBackoffWithJitter backoffStrategy = new ExponentialBackoffWithJitter(MINIMUM_WAIT_TIME,
                MAXIMUM_WAIT_TIME, RETRY_INTERVAL, random);

        // when:
        long result = backoffStrategy.getSecondsUntilRetry(2);

        // then:
        assertEquals(MINIMUM_WAIT_TIME, result);
    }

    @Test
    public void whenTheDrawnRandomValueIsNegativeThenItIsProjectedToAPositiveValue() {
        // given:
        Random random = mock(Random.class);
        when(random.nextLong()).thenReturn(-RETRY_INTERVAL * 4 - 1);

        ExponentialBackoffWithJitter backoffStrategy = new ExponentialBackoffWithJitter(MINIMUM_WAIT_TIME,
                MAXIMUM_WAIT_TIME, RETRY_INTERVAL, random);

        // when:
        long result = backoffStrategy.getSecondsUntilRetry(2);

        // then:
        assertEquals(MINIMUM_WAIT_TIME, result);
    }

    @Test
    public void whenTheResultWouldBeLargerThanTheMaximumThenItIsCappedToTheMaximum() {
        // given:
        Random random = mock(Random.class);
        when(random.nextLong()).thenReturn(MAXIMUM_WAIT_TIME - ALTERNATIVE_MINIMUM_WAIT_TIME);

        ExponentialBackoffWithJitter backoffStrategy = new ExponentialBackoffWithJitter(ALTERNATIVE_MINIMUM_WAIT_TIME,
                MAXIMUM_WAIT_TIME, ALTERNATIVE_RETRY_INTERVAL, random);

        // when:
        long result = backoffStrategy.getSecondsUntilRetry(2);

        // then:
        assertEquals(MAXIMUM_WAIT_TIME, result);
    }

    @Test
    public void whenTheResultWouldBeLargerThanTheAlternativeMaximumThenItIsCappedToTheAlternativeMaximum() {
        // given:
        Random random = mock(Random.class);
        when(random.nextLong()).thenReturn(ALTERNATIVE_MAXIMUM_WAIT_TIME - ALTERNATIVE_MINIMUM_WAIT_TIME);

        ExponentialBackoffWithJitter backoffStrategy = new ExponentialBackoffWithJitter(ALTERNATIVE_MINIMUM_WAIT_TIME,
                ALTERNATIVE_MAXIMUM_WAIT_TIME, ALTERNATIVE_RETRY_INTERVAL, random);

        // when:
        long result = backoffStrategy.getSecondsUntilRetry(2);

        // then:
        assertEquals(ALTERNATIVE_MAXIMUM_WAIT_TIME, result);
    }
}
