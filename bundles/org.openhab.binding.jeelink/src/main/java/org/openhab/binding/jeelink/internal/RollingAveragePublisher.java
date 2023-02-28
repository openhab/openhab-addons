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
package org.openhab.binding.jeelink.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Computes a rolling average of readings that is passed on to the next publisher
 * after a given time frame.
 *
 * @author Volker Bier - Initial contribution
 */
public abstract class RollingAveragePublisher<R extends Reading> implements ReadingPublisher<R> {
    private final ReadingPublisher<R> publisher;

    private ScheduledFuture<?> valueUpdateJob;
    private RollingReadingAverage<R> rollingAvg;

    public RollingAveragePublisher(int bufferSize, int interval, ReadingPublisher<R> p,
            ScheduledExecutorService execService) {
        publisher = p;

        valueUpdateJob = createUpdateJob(execService, interval);
        rollingAvg = createRollingReadingAverage(bufferSize);
    }

    public abstract RollingReadingAverage<R> createRollingReadingAverage(int bufferSize);

    @Override
    public void publish(R reading) {
        rollingAvg.add(reading);
    }

    @Override
    public void dispose() {
        if (valueUpdateJob != null) {
            valueUpdateJob.cancel(true);
            valueUpdateJob = null;
        }

        publisher.dispose();
    }

    private ScheduledFuture<?> createUpdateJob(ScheduledExecutorService execService, final int updateInterval) {
        return execService.scheduleWithFixedDelay(() -> {
            publisher.publish(rollingAvg.getAverage());
        }, updateInterval, updateInterval, TimeUnit.SECONDS);
    }
}
