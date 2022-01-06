/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
public class RequestQueueManager {
    private static @Nullable RequestQueueManager instance = null;
    private final Logger logger = LoggerFactory.getLogger(RequestQueueManager.class);
    private @Nullable Thread queueThread = null;
    private Queue<RequestQueue> requestQueues = new PriorityQueue<>();
    private Map<InsteonDevice, RequestQueue> requestQueueHash = new HashMap<>();
    private boolean keepRunning = true;

    private RequestQueueManager() {
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
     * @param dev the device to add
     * @param time the time when the queue should be processed
     */
    public void addQueue(InsteonDevice dev, long time) {
        synchronized (requestQueues) {
            RequestQueue q = requestQueueHash.get(dev);
            if (q == null) {
                logger.trace("scheduling request for device {} in {} msec", dev.getAddress(),
                        time - System.currentTimeMillis());
                q = new RequestQueue(dev, time);
            } else {
                logger.trace("queue for dev {} is already scheduled in {} msec", dev.getAddress(),
                        q.getExpirationTime() - System.currentTimeMillis());
                if (!requestQueues.remove(q)) {
                    logger.warn("queue for {} should be there, report as bug!", dev);
                }
                requestQueueHash.remove(dev);
            }
            long expTime = q.getExpirationTime();
            if (expTime > time) {
                q.setExpirationTime(time);
            }
            // add the queue back in after (maybe) having modified
            // the expiration time
            requestQueues.add(q);
            requestQueueHash.put(dev, q);
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
                        RequestQueue q;
                        while (keepRunning && (q = requestQueues.peek()) != null) {
                            long now = System.currentTimeMillis();
                            long expTime = q.getExpirationTime();
                            InsteonDevice dev = q.getDevice();
                            if (expTime > now) {
                                //
                                // The head of the queue is not up for processing yet, wait().
                                //
                                logger.trace("request queue head: {} must wait for {} msec", dev.getAddress(),
                                        expTime - now);
                                requestQueues.wait(expTime - now);
                                //
                                // note that the wait() can also return because of changes to
                                // the queue, not just because the time expired!
                                //
                                continue;
                            }
                            //
                            // The head of the queue has expired and can be processed!
                            //
                            q = requestQueues.poll(); // remove front element
                            requestQueueHash.remove(dev); // and remove from hash map
                            long nextExp = dev.processRequestQueue(now);
                            if (nextExp > 0) {
                                q = new RequestQueue(dev, nextExp);
                                requestQueues.add(q);
                                requestQueueHash.put(dev, q);
                                logger.trace("device queue for {} rescheduled in {} msec", dev.getAddress(),
                                        nextExp - now);
                            } else {
                                // remove from hash since queue is no longer scheduled
                                logger.debug("device queue for {} is empty!", dev.getAddress());
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
        private InsteonDevice device;
        private long expirationTime;

        RequestQueue(InsteonDevice dev, long expirationTime) {
            this.device = dev;
            this.expirationTime = expirationTime;
        }

        public InsteonDevice getDevice() {
            return device;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long t) {
            expirationTime = t;
        }

        @Override
        public int compareTo(RequestQueue a) {
            return (int) (expirationTime - a.expirationTime);
        }
    }

    public static synchronized @Nullable RequestQueueManager instance() {
        if (instance == null) {
            instance = new RequestQueueManager();
        }
        return instance;
    }

    public static synchronized void destroyInstance() {
        RequestQueueManager instance = RequestQueueManager.instance;
        if (instance != null) {
            instance.stopThread();
            RequestQueueManager.instance = null;
        }
    }
}
