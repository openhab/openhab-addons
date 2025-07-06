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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that manages per-device request scheduling
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class RequestManager {
    private final Logger logger = LoggerFactory.getLogger(RequestManager.class);

    private final ScheduledExecutorService scheduler;
    private final Map<Device, RequestEntry> requests = new ConcurrentHashMap<>();
    private final AtomicBoolean paused = new AtomicBoolean(false);

    public RequestManager(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Pauses request manager
     */
    public void pause() {
        if (!paused.getAndSet(true)) {
            logger.debug("pausing request manager");
            requests.values().forEach(this::cancelRequest);
        }
    }

    /**
     * Resumes request manager
     */
    public void resume() {
        if (paused.getAndSet(false)) {
            logger.debug("resuming request manager");
            requests.values().forEach(this::scheduleRequest);
        }
    }

    /**
     * Stops request manager
     */
    public void stop() {
        logger.debug("stopping request manager");
        paused.set(false);
        requests.values().forEach(this::cancelRequest);
        requests.clear();
    }

    /**
     * Adds a request for a device
     *
     * @param device the device to add
     * @param time (in milliseconds) to delay handling
     */
    public void addRequest(Device device, long delay) {
        requests.compute(device, (key, request) -> {
            if (request == null) {
                logger.trace("scheduling request for {} in {} msec", device.getAddress(), delay);
                request = new RequestEntry(device, delay);
                scheduleRequest(request);
            } else if (request.getScheduledDelay() > delay) {
                logger.trace("rescheduling request for {} from {} to {} msec", device.getAddress(),
                        request.getScheduledDelay(), delay);
                request.setScheduledDelay(delay);
                cancelRequest(request);
                scheduleRequest(request);
            }
            return request;
        });
    }

    /**
     * Cancels a request
     *
     * @param request the request to cancel
     */
    private void cancelRequest(RequestEntry request) {
        ScheduledFuture<?> job = request.job;
        if (job != null && !job.isDone()) {
            job.cancel(false);
            request.job = null;
        }
    }

    /**
     * Schedules a request
     *
     * @param request the request to schedule
     */
    private void scheduleRequest(RequestEntry request) {
        if (paused.get()) {
            logger.trace("request manager paused, request for {} to be scheduled later", request.device.getAddress());
            return;
        }

        long delay = request.getScheduledDelay();
        request.job = scheduler.schedule(() -> handleRequest(request.device), delay, TimeUnit.MILLISECONDS);

        logger.trace("request for {} scheduled in {} msec", request.device.getAddress(), delay);
    }

    /**
     * Handles a request for a device
     *
     * @param device the device to handle the request for
     */
    private void handleRequest(Device device) {
        RequestEntry request = requests.remove(device);
        if (request == null) {
            logger.trace("no request to handle for {}", device.getAddress());
            return;
        }

        logger.trace("handling request for {}", device.getAddress());

        long delay = device.handleNextRequest();
        if (delay > 0) {
            addRequest(device, delay);
        } else {
            logger.trace("no more pending requests for {}", device.getAddress());
        }
    }

    /**
     * Class that represents a request entry
     */
    private static class RequestEntry {
        private final Device device;
        private volatile long scheduledTime;
        private volatile @Nullable ScheduledFuture<?> job;

        public RequestEntry(Device device, long delay) {
            this.device = device;
            setScheduledDelay(delay);
        }

        public long getScheduledDelay() {
            return Math.max(0, scheduledTime - System.currentTimeMillis());
        }

        public void setScheduledDelay(long delay) {
            this.scheduledTime = System.currentTimeMillis() + delay;
        }
    }
}
