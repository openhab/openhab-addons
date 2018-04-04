/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A recurring Plugwise device task that can for instance be extended for updating a channel or setting the clock.
 *
 * @author Wouter Born - Initial contribution
 */
public abstract class PlugwiseDeviceTask {

    private final Logger logger = LoggerFactory.getLogger(PlugwiseDeviceTask.class);

    private final ReentrantLock lock = new ReentrantLock();
    private final String name;
    private final ScheduledExecutorService scheduler;

    private DeviceType deviceType;
    private Duration interval;
    private MACAddress macAddress;

    private ScheduledFuture<?> future;

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

    public Duration getInterval() {
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
                interval = getConfiguredInterval();
                future = scheduler.scheduleWithFixedDelay(scheduledRunnable, 0, interval.getSeconds(),
                        TimeUnit.SECONDS);
                logger.debug("Scheduled '{}' Plugwise task for {} ({}) with {} seconds interval", name, deviceType,
                        macAddress, interval.getSeconds());
            }
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        try {
            lock.lock();
            if (isScheduled()) {
                future.cancel(true);
                future = null;
                logger.debug("Stopped '{}' Plugwise task for {} ({})", name, deviceType, macAddress);
            }
        } finally {
            lock.unlock();
        }
    }

    public void update(DeviceType deviceType, MACAddress macAddress) {
        this.deviceType = deviceType;
        this.macAddress = macAddress;
    }

}
