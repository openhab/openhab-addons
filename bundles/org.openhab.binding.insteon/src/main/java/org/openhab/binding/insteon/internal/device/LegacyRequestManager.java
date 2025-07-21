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

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that manages all the per-device request queues using a single thread.
 *
 * - Each device has its own request queue, and the RequestQueueManager keeps a
 * queue of queues.
 * - Each entry in m_requestQueues corresponds to a single device's request queue.
 * A device should never be more than once in m_requestQueues.
 * - A hash map (m_requestQueueHash) is kept in sync with m_requestQueues for
 * faster lookup in case a request queue is modified and needs to be
 * rescheduled.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class LegacyRequestManager {
    private final Logger logger = LoggerFactory.getLogger(LegacyRequestManager.class);

    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private Queue<RequestQueue> requestQueues = new PriorityQueue<>();
    private Map<LegacyDevice, RequestQueue> requestQueueHash = new HashMap<>();

    /**
     * Constructor
     */
    public LegacyRequestManager(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Add device to global request queue.
     *
     * @param device the device to add
     * @param time the time when the queue should be processed
     */
    public void addQueue(LegacyDevice device, long time) {
        synchronized (requestQueues) {
            RequestQueue queue = requestQueueHash.get(device);
            if (queue == null) {
                logger.trace("scheduling request for device {} in {} msec", device.getAddress(),
                        time - System.currentTimeMillis());
                queue = new RequestQueue(device, time);
            } else {
                logger.trace("queue for device {} is already scheduled in {} msec", device.getAddress(),
                        queue.getExpirationTime() - System.currentTimeMillis());
                if (!requestQueues.remove(queue)) {
                    logger.warn("queue for {} should be there, report as bug!", device);
                }
                requestQueueHash.remove(device);
            }
            long expTime = queue.getExpirationTime();
            if (expTime > time) {
                queue.setExpirationTime(time);
            }
            // add the queue back in after (maybe) having modified
            // the expiration time
            requestQueues.add(queue);
            requestQueueHash.put(device, queue);
            requestQueues.notify();
        }
    }

    /**
     * Starts request queue thread
     */
    public void start() {
        if (job == null) {
            job = scheduler.schedule(new RequestQueueReader(), 0, TimeUnit.SECONDS);
        }
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

    class RequestQueueReader implements Runnable {
        @Override
        public void run() {
            logger.debug("starting request queue thread");
            try {
                while (!Thread.interrupted()) {
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
                            LegacyDevice device = queue.getDevice();
                            if (expTime > now) {
                                // The head of the queue is not up for processing yet, wait().
                                logger.trace("request queue head: {} must wait for {} msec", device.getAddress(),
                                        expTime - now);
                                requestQueues.wait(expTime - now);
                                // note that the wait() can also return because of changes to
                                // the queue, not just because the time expired!
                                continue;
                            }
                            // The head of the queue has expired and can be processed!
                            queue = requestQueues.poll(); // remove front element
                            requestQueueHash.remove(device); // and remove from hash map
                            long nextExp = device.processRequestQueue(now);
                            if (nextExp > 0) {
                                queue = new RequestQueue(device, nextExp);
                                requestQueues.add(queue);
                                requestQueueHash.put(device, queue);
                                logger.trace("device queue for {} rescheduled in {} msec", device.getAddress(),
                                        nextExp - now);
                            } else {
                                // remove from hash since queue is no longer scheduled
                                logger.debug("device queue for {} is empty!", device.getAddress());
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                logger.trace("request queue thread interrupted!");
            }
            logger.debug("exiting request queue thread!");
        }
    }

    public static class RequestQueue implements Comparable<RequestQueue> {
        private LegacyDevice device;
        private long expirationTime;

        RequestQueue(LegacyDevice device, long expirationTime) {
            this.device = device;
            this.expirationTime = expirationTime;
        }

        public LegacyDevice getDevice() {
            return device;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long t) {
            expirationTime = t;
        }

        @Override
        public int compareTo(RequestQueue queue) {
            return Long.compare(expirationTime, queue.expirationTime);
        }
    }
}
