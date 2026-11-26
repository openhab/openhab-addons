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
package org.openhab.binding.tibber.internal.pulse.stream;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tibber.internal.pulse.stream.StreamClient.BackoffWaiter;

/**
 * A {@link BackoffWaiter} that records the requested delays and returns at once. After the given number of waits it
 * reports "stopped", which ends the supervision loop without calling {@code stop()}.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
final class RecordingWaiter implements BackoffWaiter {

    private final List<Duration> delays = new CopyOnWriteArrayList<>();
    private final int stopAfter;
    private final CountDownLatch finished = new CountDownLatch(1);

    RecordingWaiter(int stopAfter) {
        this.stopAfter = stopAfter;
    }

    @Override
    public boolean awaitStop(Duration delay, CountDownLatch stopped) {
        delays.add(delay);
        if (delays.size() >= stopAfter) {
            finished.countDown();
            return true;
        }
        return stopped.getCount() == 0;
    }

    /** @return the requested delays in seconds */
    List<Long> seconds() {
        return delays.stream().map(Duration::toSeconds).toList();
    }

    int waits() {
        return delays.size();
    }

    /** Waits until the loop ended because the configured number of waits was reached. */
    boolean awaitFinished(long timeoutMillis) throws InterruptedException {
        return finished.await(timeoutMillis, TimeUnit.MILLISECONDS);
    }
}
