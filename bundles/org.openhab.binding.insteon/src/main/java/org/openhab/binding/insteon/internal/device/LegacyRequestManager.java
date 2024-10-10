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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
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
    private static @Nullable LegacyRequestManager instance = null;
    private final Logger logger = LoggerFactory.getLogger(LegacyRequestManager.class);
    private @Nullable Thread queueThread = null;
    private Queue<RequestQueue> requestQueues = new PriorityQueue<>();
    private Map<LegacyDevice, RequestQueue> requestQueueHash = new HashMap<>();
    private boolean keepRunning = true;

    private LegacyRequestManager() {
        queueThread = new Thread(new RequestQueueReader());
        setParamsAndStart(queueThread);
    }

    private void setParamsAndStart(@Nullable Thread thread) {
        if (thread != null) {
            thread.setName("OH-binding-" + InsteonBindingConstants.BINDING_ID + "-requestQueueReader");
            thread.setDaemon(true);
            thread.start();
        }
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
     * Stops request queue thread
     */
    private void stopThread() {
        logger.debug("stopping thread");
        Thread queueThread = this.queueThread;
        if (queueThread != null) {
            synchronized (requestQueues) {
                keepRunning = false;
                requestQueues.notifyAll();
            }
            try {
                logger.debug("waiting for thread to join");
                queueThread.join();
                logger.debug("request queue thread exited!");
            } catch (InterruptedException e) {
                logger.warn("got interrupted waiting for thread exit ", e);
            }
            this.queueThread = null;
        }
    }

    class RequestQueueReader implements Runnable {
        @Override
        public void run() {
            logger.debug("starting request queue thread");
            synchronized (requestQueues) {
                while (keepRunning) {
                    try {
                        RequestQueue queue;
                        while (keepRunning && (queue = requestQueues.peek()) != null) {
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
                        logger.trace("waiting for request queues to fill");
                        requestQueues.wait();
                    } catch (InterruptedException e) {
                        logger.warn("request queue thread got interrupted, breaking..", e);
                        break;
                    }
                }
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
            return (int) (expirationTime - queue.expirationTime);
        }
    }

    public static synchronized @Nullable LegacyRequestManager instance() {
        if (instance == null) {
            instance = new LegacyRequestManager();
        }
        return instance;
    }

    public static synchronized void destroyInstance() {
        LegacyRequestManager instance = LegacyRequestManager.instance;
        if (instance != null) {
            instance.stopThread();
            LegacyRequestManager.instance = null;
        }
    }
}
