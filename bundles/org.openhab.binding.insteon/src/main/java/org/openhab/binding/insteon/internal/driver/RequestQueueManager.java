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
package org.openhab.binding.insteon.internal.driver;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.device.DeviceFeature;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.message.Msg;
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
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class RequestQueueManager {
    private final Logger logger = LoggerFactory.getLogger(RequestQueueManager.class);
    private String name;
    private @Nullable Thread queueThread;
    private Queue<RequestQueue> requestQueues = new PriorityQueue<>();
    private Map<InsteonDevice, RequestQueue> requestQueueHash = new HashMap<>();
    private AtomicBoolean paused = new AtomicBoolean(false);

    /**
     * Constructor
     */
    public RequestQueueManager(String name) {
        this.name = name;
    }

    /**
     * Returns if request queue manager is running
     *
     * @return true if queue thread is defined
     */
    private boolean isRunning() {
        return queueThread != null;
    }

    /**
     * Adds device to global request queue.
     *
     * @param device the device to add
     * @param time (in milliseconds) to delay queue processing
     */
    public void addQueue(InsteonDevice device, long delay) {
        synchronized (requestQueues) {
            long time = System.currentTimeMillis() + delay;
            RequestQueue queue = requestQueueHash.get(device);
            if (queue == null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("scheduling request for device {} in {} msec", device.getAddress(), delay);
                }
                queue = new RequestQueue(device, time);
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("queue for device {} is already scheduled in {} msec", device.getAddress(),
                            queue.getExpirationTime() - System.currentTimeMillis());
                }
                requestQueues.remove(queue);
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
     * Pauses request manager thread
     */
    public void pause() {
        if (logger.isDebugEnabled()) {
            logger.debug("pausing request queue thread");
        }
        paused.set(true);
        synchronized (requestQueues) {
            requestQueues.notify();
        }
    }

    /**
     * Resumes request queue thread
     */
    public void resume() {
        if (logger.isDebugEnabled()) {
            logger.debug("resuming request queue thread");
        }
        paused.set(false);
        synchronized (paused) {
            paused.notify();
        }
    }

    /**
     * Starts request queue thread
     */
    public void start() {
        if (isRunning()) {
            logger.debug("request queue reader {} already running, not started again", name);
            return;
        }
        Thread queueThread = new Thread(new RequestQueueReader());
        queueThread.setName("OH-binding-" + InsteonBindingConstants.BINDING_ID + "-requestQueueReader:" + name);
        queueThread.setDaemon(true);
        queueThread.start();
        this.queueThread = queueThread;
    }

    /**
     * Stops request queue thread
     */
    public void stop() {
        Thread queueThread = this.queueThread;
        if (queueThread != null) {
            try {
                queueThread.interrupt();
                queueThread.join();
            } catch (InterruptedException e) {
                logger.debug("got interrupted waiting for request queue thread to exit.");
            }
            this.queueThread = null;
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
                            if (logger.isTraceEnabled()) {
                                logger.trace("waiting for request queue thread to resume");
                            }
                            paused.wait();
                            continue;
                        }
                    }
                    synchronized (requestQueues) {
                        if (requestQueues.isEmpty()) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("waiting for request queues to fill");
                            }
                            requestQueues.wait();
                            continue;
                        }
                        RequestQueue queue = requestQueues.peek();
                        if (queue != null) {
                            long now = System.currentTimeMillis();
                            long expTime = queue.getExpirationTime();
                            InsteonDevice device = queue.getDevice();
                            if (expTime > now) {
                                // The head of the queue is not up for processing yet, wait().
                                if (logger.isTraceEnabled()) {
                                    logger.trace("request queue head: {} must wait for {} msec", device.getAddress(),
                                            expTime - now);
                                }
                                requestQueues.wait(expTime - now);
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
                InsteonDevice device = queue.getDevice();
                requestQueueHash.remove(device); // and remove from hash map
                long nextExp = device.processRequestQueue(now);
                if (nextExp > 0) {
                    queue = new RequestQueue(device, nextExp);
                    requestQueues.add(queue);
                    requestQueueHash.put(device, queue);
                    if (logger.isTraceEnabled()) {
                        logger.trace("device queue for {} rescheduled in {} msec", device.getAddress(), nextExp - now);
                    }
                } else {
                    // remove from hash since queue is no longer scheduled
                    if (logger.isDebugEnabled()) {
                        logger.debug("device queue for {} is empty!", device.getAddress());
                    }
                }
            }
        }
    }

    /**
     * Request queue class
     */
    public static class RequestQueue implements Comparable<RequestQueue> {
        private InsteonDevice device;
        private long expirationTime;

        RequestQueue(InsteonDevice device, long expirationTime) {
            this.device = device;
            this.expirationTime = expirationTime;
        }

        public InsteonDevice getDevice() {
            return device;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
        }

        @Override
        public int compareTo(RequestQueue a) {
            return (int) (expirationTime - a.expirationTime);
        }
    }

    /**
     * Request queue entry class
     */
    public static class RequestQueueEntry implements Comparable<RequestQueueEntry> {
        private DeviceFeature feature;
        private Msg msg;
        private long expirationTime;

        public RequestQueueEntry(DeviceFeature feature, Msg msg, long delay) {
            this.feature = feature;
            this.msg = msg;
            setExpirationTime(delay);
        }

        public DeviceFeature getFeature() {
            return feature;
        }

        public Msg getMessage() {
            return msg;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long delay) {
            this.expirationTime = System.currentTimeMillis() + delay;
        }

        @Override
        public int compareTo(RequestQueueEntry a) {
            return (int) (expirationTime - a.expirationTime);
        }
    }
}
