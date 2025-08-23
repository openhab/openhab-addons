/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that manages device poll scheduling
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class PollManager {
    private static final long MIN_MSEC_BETWEEN_POLLS = 2000L;

    private final Logger logger = LoggerFactory.getLogger(PollManager.class);

    private final long pollInterval;
    private final ScheduledExecutorService scheduler;
    private final Map<Device, ScheduledFuture<?>> polls = new ConcurrentHashMap<>();
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private volatile int deviceCount = 0;

    public PollManager(long pollInterval, ScheduledExecutorService scheduler) {
        this.pollInterval = pollInterval;
        this.scheduler = scheduler;
    }

    public int getPollCount() {
        return polls.size();
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    /**
     * Pauses poll manager
     */
    public void pause() {
        if (!paused.getAndSet(true)) {
            logger.debug("pausing poll manager");
        }
    }

    /**
     * Resumes poll manager
     */
    public void resume() {
        if (paused.getAndSet(false)) {
            logger.debug("resuming poll manager");
        }
    }

    /**
     * Stops poll manager
     */
    public void stop() {
        logger.debug("stopping poll manager");
        paused.set(false);
        polls.values().forEach(job -> job.cancel(false));
        polls.clear();
    }

    /**
     * Starts polling a given device
     *
     * @param device the device to start polling
     * @return true if polling is scheduled, otherwise false
     */
    public boolean startPolling(Device device) {
        if (polls.containsKey(device)) {
            logger.trace("device {} already being polled", device.getAddress());
            return true;
        }

        long pollDelay = getNextPollDelay();
        if (pollDelay > pollInterval) {
            logger.warn("device {} cannot be polled, increase the poll interval to allow for more devices to be polled",
                    device.getAddress());
            return false;
        }

        logger.debug("start polling device {} in {} msec", device.getAddress(), pollDelay);

        ScheduledFuture<?> job = scheduler.scheduleWithFixedDelay(() -> pollDevice(device), pollDelay, pollInterval,
                TimeUnit.MILLISECONDS);

        polls.put(device, job);

        return true;
    }

    /**
     * Stops polling a given device
     *
     * @param device the device to stop polling
     */
    public void stopPolling(Device device) {
        ScheduledFuture<?> job = polls.remove(device);
        if (job != null) {
            job.cancel(false);
            logger.debug("stopped polling device {}", device.getAddress());
        }
    }

    /**
     * Returns the next poll delay
     *
     * @return the next poll delay
     */
    private long getNextPollDelay() {
        long desiredDelay = polls.size() * pollInterval / (deviceCount + 1);
        Iterator<Long> iterator = polls.values().stream().map(job -> job.getDelay(TimeUnit.MILLISECONDS))
                .filter(delay -> delay >= desiredDelay - MIN_MSEC_BETWEEN_POLLS).sorted().iterator();

        long pollDelay = desiredDelay;
        while (iterator.hasNext()) {
            long delay = iterator.next();
            if (delay >= pollDelay + MIN_MSEC_BETWEEN_POLLS) {
                break;
            }
            pollDelay = delay + MIN_MSEC_BETWEEN_POLLS;
        }
        return pollDelay;
    }

    /**
     * Polls a device
     *
     * @param device the device to poll
     */
    private void pollDevice(Device device) {
        if (paused.get()) {
            logger.trace("poll manager paused, skipping poll for {}", device.getAddress());
        } else {
            logger.debug("polling device {}", device.getAddress());
            device.poll(0L);
            logger.trace("next poll for {} scheduled in {} msec", device.getAddress(), pollInterval);
        }
    }
}
