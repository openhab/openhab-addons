/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.energidataservice.internal.retry.strategy;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.energidataservice.internal.retry.RetryStrategy;

/**
 * Tests for {@link Linear}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class LinearTest {

    @Test
    void linear() {
        RetryStrategy retryPolicy = new Linear().withMinimum(Duration.ofMinutes(1)).withJitter(0.0);
        for (int i = 0; i <= 10; i++) {
            assertThat(retryPolicy.getDuration(), is(Duration.ofMinutes(1)));
        }
    }
}
