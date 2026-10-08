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
package org.openhab.binding.netatmo.internal.handler;

import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Martin Littkovsky - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class CommonInterfaceTest {

    private @Mock(answer = Answers.CALLS_REAL_METHODS) @NonNullByDefault({}) CommonInterface handler;
    private @Mock @NonNullByDefault({}) ScheduledExecutorService scheduler;

    @Test
    void scheduleKeepsTheFractionOfASecond() {
        Runnable job = () -> {
        };
        when(handler.getScheduler()).thenReturn(scheduler);

        handler.schedule(job, Duration.ofMillis(44_500));

        verify(scheduler).schedule(job, 44_500, TimeUnit.MILLISECONDS);
    }
}
