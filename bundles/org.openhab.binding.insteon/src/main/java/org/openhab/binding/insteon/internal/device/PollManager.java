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

import java.sql.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the polling of all devices.
 * Between successive polls of any device there is a quiet time of
 * at least MIN_MSEC_BETWEEN_POLLS. This avoids bunching up of poll messages
 * and keeps the network bandwidth open for other messages.
 *
 * - An entry in the poll queue corresponds to a single device, i.e. each device should
 * have exactly one entry in the poll queue. That entry is created when startPolling()
 * is called, and then re-enqueued whenever it expires.
 * - When a device comes up for polling, its doPoll() method is called, which in turn
 * puts an entry into that devices request queue. So the Poller class actually never
 * sends out messages directly. That is done by the device itself via its request
 * queue. The poller just reminds the device to poll.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class PollManager {
    private static final long MIN_MSEC_BETWEEN_POLLS = 2000L;

    private final Logger logger = LoggerFactory.getLogger(PollManager.class);

    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private TreeSet<PQEntry> pollQueue = new TreeSet<>();

    /**
     * Constructor
     */
    public PollManager(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Returns if poller is running
     *
     * @return true if poll queue reader job is defined
     */
    private boolean isRunning() {
        return job != null;
    }

    /**
     * Get size of poll queue
     *
     * @return number of devices being polled
     */
    public int getSizeOfQueue() {
        return pollQueue.size();
    }

    /**
     * Register a device for polling.
     *
     * @param device device to register for polling
     * @param pollInterval device poll interval
     * @param numDev approximate number of total devices
     */
    public void startPolling(Device device, long pollInterval, int numDev) {
        logger.debug("start polling device {}", device.getAddress());

        synchronized (pollQueue) {
            // try to spread out the scheduling when starting up
            long pollDelay = pollQueue.size() * pollInterval / (numDev + 1);
            addToPollQueue(device, pollInterval, System.currentTimeMillis() + pollDelay);
            pollQueue.notify();
        }
    }

    /**
     * Stops polling a given device
     *
     * @param device reference to the device to be polled
     */
    public void stopPolling(Device device) {
        synchronized (pollQueue) {
            for (Iterator<PQEntry> it = pollQueue.iterator(); it.hasNext();) {
                if (it.next().getDevice().getAddress().equals(device.getAddress())) {
                    it.remove();
                    logger.debug("stopped polling device {}", device.getAddress());
                }
            }
        }
    }

    /**
     * Starts the poller thread
     */
    public void start() {
        if (isRunning()) {
            logger.debug("poll manager already running, not started again");
            return;
        }
        job = scheduler.schedule(new PollQueueReader(), 0, TimeUnit.SECONDS);
    }

    /**
     * Stops the poller thread
     */
    public void stop() {
        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }
    }

    /**
     * Adds a device to the poll queue. After this call, the device's doPoll() method
     * will be called according to the polling frequency set.
     *
     * @param device the device to poll periodically
     * @param pollInterval the device poll interval
     * @param time the target time for the next poll to happen. Note that this time is merely
     *            a suggestion, and may be adjusted, because there must be at least a minimum gap in polling.
     */

    private void addToPollQueue(Device device, long pollInterval, long time) {
        long expTime = findNextExpirationTime(device, pollInterval, time);
        PQEntry queue = new PQEntry(device, pollInterval, expTime);
        logger.trace("added entry {}", queue);
        pollQueue.add(queue);
    }

    /**
     * Finds the best expiration time for a poll queue, i.e. a time slot that is after the
     * desired expiration time, but does not collide with any of the already scheduled
     * polls.
     *
     * @param device device to poll (for logging)
     * @param pollInterval device poll interval
     * @param time desired time after which the device should be polled
     * @return the suggested time to poll
     */

    private long findNextExpirationTime(Device device, long pollInterval, long time) {
        long expTime;
        // tailSet finds all those that expire after time - buffer
        PQEntry queue = new PQEntry(device, pollInterval, time - MIN_MSEC_BETWEEN_POLLS);
        SortedSet<PQEntry> tailSet = pollQueue.tailSet(queue);
        if (tailSet.isEmpty()) {
            // all entries in the poll queue are ahead of the new element,
            // go ahead and simply add it to the end
            expTime = time;
        } else {
            Iterator<PQEntry> it = tailSet.iterator();
            PQEntry prevQueue = it.next();
            if (prevQueue.getExpirationTime() > time + MIN_MSEC_BETWEEN_POLLS) {
                // there is a time slot free before the head of the tail set
                expTime = time;
            } else {
                // look for a gap where we can squeeze in
                // a new poll while maintaining MIN_MSEC_BETWEEN_POLLS
                while (it.hasNext()) {
                    PQEntry currQueue = it.next();
                    long currTime = currQueue.getExpirationTime();
                    long prevTime = prevQueue.getExpirationTime();
                    if (currTime - prevTime >= 2 * MIN_MSEC_BETWEEN_POLLS) {
                        // found gap
                        logger.trace("device {} time {} found slot between {} and {}", device.getAddress(), time,
                                prevTime, currTime);
                        break;
                    }
                    prevQueue = currQueue;
                }
                expTime = prevQueue.getExpirationTime() + MIN_MSEC_BETWEEN_POLLS;
            }
        }
        return expTime;
    }

    private class PollQueueReader implements Runnable {
        @Override
        public void run() {
            logger.debug("starting poll queue thread");
            try {
                while (!Thread.interrupted()) {
                    synchronized (pollQueue) {
                        if (pollQueue.isEmpty()) {
                            logger.trace("waiting for poll queue to fill");
                            pollQueue.wait();
                            continue;
                        }
                        // something is in the queue
                        long now = System.currentTimeMillis();
                        PQEntry queue = pollQueue.first();
                        long delay = queue.getExpirationTime() - now;
                        if (delay > 0) { // must wait for this item to expire
                            logger.trace("waiting for {} msec until {} comes due", delay, queue);
                            pollQueue.wait(delay);
                        } else { // queue entry has expired, process it!
                            logger.trace("poll queue {} has expired", queue);
                            processQueueEntry(now);
                        }
                    }
                }
            } catch (InterruptedException e) {
                logger.debug("poll queue thread interrupted!");
            }
            logger.debug("exiting poll queue thread!");
        }

        /**
         * Takes first element off the poll queue, polls the corresponding device,
         * and puts the device back into the poll queue to be polled again later.
         *
         * @param now the current time
         */
        private void processQueueEntry(long now) {
            PQEntry queue = pollQueue.pollFirst();
            if (queue != null) {
                queue.getDevice().doPoll(0L);
                addToPollQueue(queue.getDevice(), queue.getPollInterval(), now + queue.getPollInterval());
            }
        }
    }

    /**
     * A poll queue entry corresponds to a single device that needs
     * to be polled.
     */
    private static class PQEntry implements Comparable<PQEntry> {
        private Device device;
        private long pollInterval;
        private long expirationTime;

        PQEntry(Device device, long pollInterval, long expirationTime) {
            this.device = device;
            this.pollInterval = pollInterval;
            this.expirationTime = expirationTime;
        }

        long getExpirationTime() {
            return expirationTime;
        }

        long getPollInterval() {
            return pollInterval;
        }

        Device getDevice() {
            return device;
        }

        @Override
        public int compareTo(PQEntry other) {
            return (int) (expirationTime - other.expirationTime);
        }

        @Override
        public String toString() {
            return String.format("%s/%tc", device.getAddress(), new Date(expirationTime));
        }
    }
}
