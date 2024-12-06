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
package org.openhab.binding.insteon.internal.device;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that manages all the per-device request queues using a single thread.
 *
 * - Each device has its own request queue, and the RequestQueueManager keeps a
 * queue of queues.
 * - Each entry in requestQueues corresponds to a single device's request queue.
 * A device should never be more than once in requestQueues.
 * - A hash map (requestQueueHash) is kept in sync with requestQueues for
 * faster lookup in case a request queue is modified and needs to be
 * rescheduled.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class RequestManager {
    private final Logger logger = LoggerFactory.getLogger(RequestManager.class);

    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private Queue<RequestQueue> requestQueues = new PriorityQueue<>();
    private Map<Device, RequestQueue> requestQueueHash = new HashMap<>();
    private AtomicBoolean paused = new AtomicBoolean(false);

    /**
     * Constructor
     */
    public RequestManager(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Returns if request manager is running
     *
     * @return true if request queue reader job is defined
     */
    private boolean isRunning() {
        return job != null;
    }

    /**
     * Adds device to global request queue.
     *
     * @param device the device to add
     * @param time (in milliseconds) to delay queue processing
     */
    public void addQueue(Device device, long delay) {
        synchronized (requestQueues) {
            long now = System.currentTimeMillis();
            long time = now + delay;
            RequestQueue queue = requestQueueHash.get(device);
            if (queue == null) {
                logger.trace("scheduling request for device {} in {} msec", device.getAddress(), delay);
                queue = new RequestQueue(device, time);
                requestQueues.add(queue);
                requestQueueHash.put(device, queue);
                requestQueues.notify();
            } else if (queue.getExpirationTime() > time) {
                logger.trace("rescheduling request for device {} from {} to {} msec", device.getAddress(),
                        queue.getExpirationTime() - now, delay);
                queue.setExpirationTime(time);
            }
        }
    }

    /**
     * Pauses request manager thread
     */
    public void pause() {
        if (isRunning() && !paused.getAndSet(true)) {
            logger.debug("pausing request queue thread");

            synchronized (requestQueues) {
                requestQueues.notify();
            }
        }
    }

    /**
     * Resumes request queue thread
     */
    public void resume() {
        if (isRunning() && paused.getAndSet(false)) {
            logger.debug("resuming request queue thread");

            synchronized (paused) {
                paused.notify();
            }
        }
    }

    /**
     * Starts request queue thread
     */
    public void start() {
        if (isRunning()) {
            logger.debug("request manager already running, not started again");
            return;
        }
        job = scheduler.schedule(new RequestQueueReader(), 0, TimeUnit.SECONDS);
    }

    /**
     * Stops request queue thread
     */
    public void stop() {
        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }
    }

    /**
     * Request queue reader class
     */
    private class RequestQueueReader implements Runnable {
        @Override
        public void run() {
            logger.debug("starting request queue thread");
            try {
                while (!Thread.interrupted()) {
                    synchronized (paused) {
                        if (paused.get()) {
                            logger.trace("waiting for request queue thread to resume");
                            paused.wait();
                            continue;
                        }
                    }
                    synchronized (requestQueues) {
                        if (requestQueues.isEmpty()) {
                            logger.trace("waiting for request queues to fill");
                            requestQueues.wait();
                            continue;
                        }
                        RequestQueue queue = requestQueues.peek();
                        if (queue != null) {
                            long now = System.currentTimeMillis();
                            long expTime = queue.getExpirationTime();
                            long delay = expTime - now;
                            Device device = queue.getDevice();
                            if (delay > 0) {
                                // The head of the queue is not up for processing yet, wait().
                                logger.trace("request queue head: {} must wait for {} msec", device.getAddress(),
                                        delay);
                                requestQueues.wait(delay);
                            } else {
                                // The head of the queue has expired and can be processed!
                                processRequestQueue(now);
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                logger.debug("request queue thread interrupted!");
            }
            logger.debug("exiting request queue thread!");
        }

        /**
         * Processes the head of the queue
         *
         * @param now the current time
         */
        private void processRequestQueue(long now) {
            RequestQueue queue = requestQueues.poll(); // remove front element
            if (queue != null) {
                Device device = queue.getDevice();
                requestQueueHash.remove(device); // and remove from hash map
                long nextExp = device.handleNextRequest();
                if (nextExp > 0) {
                    queue = new RequestQueue(device, nextExp);
                    requestQueues.add(queue);
                    requestQueueHash.put(device, queue);
                    logger.trace("device queue for {} rescheduled in {} msec", device.getAddress(), nextExp - now);
                } else {
                    // remove from hash since queue is no longer scheduled
                    logger.trace("device queue for {} is empty!", device.getAddress());
                }
            }
        }
    }

    /**
     * Class that represents a request queue
     */
    private static class RequestQueue implements Comparable<RequestQueue> {
        private Device device;
        private long expirationTime;

        RequestQueue(Device device, long expirationTime) {
            this.device = device;
            this.expirationTime = expirationTime;
        }

        public Device getDevice() {
            return device;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
        }

        @Override
        public int compareTo(RequestQueue other) {
            return (int) (expirationTime - other.expirationTime);
        }
    }
}
