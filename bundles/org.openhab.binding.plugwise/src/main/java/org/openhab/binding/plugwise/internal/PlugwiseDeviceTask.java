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
package org.openhab.binding.plugwise.internal;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A recurring Plugwise device task that can for instance be extended for updating a channel or setting the clock.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public abstract class PlugwiseDeviceTask {

    private final Logger logger = LoggerFactory.getLogger(PlugwiseDeviceTask.class);

    private final ReentrantLock lock = new ReentrantLock();
    private final String name;
    private final ScheduledExecutorService scheduler;

    private @Nullable DeviceType deviceType;
    private @Nullable Duration interval;
    private @Nullable MACAddress macAddress;

    private @Nullable ScheduledFuture<?> future;

    private Runnable scheduledRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                lock.lock();
                logger.debug("Running '{}' Plugwise task for {} ({})", name, deviceType, macAddress);
                runTask();
            } catch (Exception e) {
                logger.warn("Error while running '{}' Plugwise task for {} ({})", name, deviceType, macAddress, e);
            } finally {
                lock.unlock();
            }
        }
    };

    public PlugwiseDeviceTask(String name, ScheduledExecutorService scheduler) {
        this.name = name;
        this.scheduler = scheduler;
    }

    public abstract Duration getConfiguredInterval();

    public @Nullable Duration getInterval() {
        return interval;
    }

    public String getName() {
        return name;
    }

    public boolean isScheduled() {
        return future != null && !future.isCancelled();
    }

    public abstract void runTask();

    public abstract boolean shouldBeScheduled();

    public void start() {
        try {
            lock.lock();
            if (!isScheduled()) {
                Duration configuredInterval = getConfiguredInterval();
                future = scheduler.scheduleWithFixedDelay(scheduledRunnable, 0, configuredInterval.getSeconds(),
                        TimeUnit.SECONDS);
                interval = configuredInterval;
                logger.debug("Scheduled '{}' Plugwise task for {} ({}) with {} seconds interval", name, deviceType,
                        macAddress, configuredInterval.getSeconds());
            }
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            if (isScheduled()) {
                ScheduledFuture<?> localFuture = future;
                if (localFuture != null) {
                    localFuture.cancel(true);
                }
                future = null;
                logger.debug("Stopped '{}' Plugwise task for {} ({})", name, deviceType, macAddress);
            }
        } finally {
            lock.unlock();
        }
    }

    public void update(DeviceType deviceType, @Nullable MACAddress macAddress) {
        this.deviceType = deviceType;
        this.macAddress = macAddress;
    }
}
