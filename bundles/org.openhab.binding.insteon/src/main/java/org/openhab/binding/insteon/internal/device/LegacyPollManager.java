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
 */
@NonNullByDefault
public class LegacyPollManager {
    private static final long MIN_MSEC_BETWEEN_POLLS = 2000L;

    private final Logger logger = LoggerFactory.getLogger(LegacyPollManager.class);

    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private TreeSet<PQEntry> pollQueue = new TreeSet<>();

    /**
     * Constructor
     */
    public LegacyPollManager(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
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
     * @param numDev approximate number of total devices
     */
    public void startPolling(LegacyDevice device, int numDev) {
        logger.debug("start polling device {}", device);
        synchronized (pollQueue) {
            // try to spread out the scheduling when
            // starting up
            int n = pollQueue.size();
            long pollDelay = n * device.getPollInterval() / (numDev > 0 ? numDev : 1);
            addToPollQueue(device, System.currentTimeMillis() + pollDelay);
            pollQueue.notify();
        }
    }

    /**
     * Start polling a given device
     *
     * @param device reference to the device to be polled
     */
    public void stopPolling(LegacyDevice device) {
        synchronized (pollQueue) {
            for (Iterator<PQEntry> i = pollQueue.iterator(); i.hasNext();) {
                if (i.next().getDevice().getAddress().equals(device.getAddress())) {
                    i.remove();
                    logger.debug("stopped polling device {}", device);
                }
            }
        }
    }

    /**
     * Starts the poller thread
     */
    public void start() {
        if (job == null) {
            job = scheduler.schedule(new PollQueueReader(), 0, TimeUnit.SECONDS);
        }
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
     * @param time the target time for the next poll to happen. Note that this time is merely
     *            a suggestion, and may be adjusted, because there must be at least a minimum gap in polling.
     */

    private void addToPollQueue(LegacyDevice device, long time) {
        long texp = findNextExpirationTime(device, time);
        PQEntry ne = new PQEntry(device, texp);
        logger.trace("added entry {} originally aimed at time {}", ne, String.format("%tc", new Date(time)));
        pollQueue.add(ne);
    }

    /**
     * Finds the best expiration time for a poll queue, i.e. a time slot that is after the
     * desired expiration time, but does not collide with any of the already scheduled
     * polls.
     *
     * @param device device to poll (for logging)
     * @param time desired time after which the device should be polled
     * @return the suggested time to poll
     */

    private long findNextExpirationTime(LegacyDevice device, long time) {
        long expTime = time;
        // tailSet finds all those that expire after time - buffer
        SortedSet<PQEntry> expired = pollQueue.tailSet(new PQEntry(device, time - MIN_MSEC_BETWEEN_POLLS));
        if (expired.isEmpty()) {
            // all entries in the poll queue are ahead of the new element,
            // go ahead and simply add it to the end
            expTime = time;
        } else {
            Iterator<PQEntry> pqi = expired.iterator();
            PQEntry prev = pqi.next();
            if (prev.getExpirationTime() > time + MIN_MSEC_BETWEEN_POLLS) {
                // there is a time slot free before the head of the tail set
                expTime = time;
            } else {
                // look for a gap where we can squeeze in
                // a new poll while maintaining MIN_MSEC_BETWEEN_POLLS
                while (pqi.hasNext()) {
                    PQEntry pqe = pqi.next();
                    long currTime = pqe.getExpirationTime();
                    long prevTime = prev.getExpirationTime();
                    if (currTime - prevTime >= 2 * MIN_MSEC_BETWEEN_POLLS) {
                        // found gap
                        logger.trace("device {} time {} found slot between {} and {}", device, time, prevTime,
                                currTime);
                        break;
                    }
                    prev = pqe;
                }
                expTime = prev.getExpirationTime() + MIN_MSEC_BETWEEN_POLLS;
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
                        readPollQueue();
                    }
                }
            } catch (InterruptedException e) {
                logger.trace("poll queue thread interrupted!");
            }
            logger.debug("exiting poll queue thread!");
        }

        /**
         * Waits for first element of poll queue to become current,
         * then process it.
         *
         * @throws InterruptedException
         */
        private void readPollQueue() throws InterruptedException {
            if (pollQueue.isEmpty()) {
                logger.trace("waiting for poll queue to fill");
                pollQueue.wait();
                return;
            }
            // something is in the queue
            long now = System.currentTimeMillis();
            PQEntry pqe = pollQueue.first();
            long expTime = pqe.getExpirationTime();
            long delta = expTime - now;
            if (delta > 0) { // must wait for this item to expire
                logger.trace("waiting for {} msec until {} comes due", delta, pqe);
                pollQueue.wait(delta);
            } else { // queue entry has expired, process it!
                logger.trace("entry {} expired at time {}", pqe, now);
                processQueue(now);
            }
        }

        /**
         * Takes first element off the poll queue, polls the corresponding device,
         * and puts the device back into the poll queue to be polled again later.
         *
         * @param now the current time
         */
        private void processQueue(long now) {
            processQueue(now, pollQueue.pollFirst());
        }

        private void processQueue(long now, @Nullable PQEntry pqe) {
            if (pqe != null) {
                pqe.getDevice().doPoll(0);
                addToPollQueue(pqe.getDevice(), now + pqe.getDevice().getPollInterval());
            }
        }
    }

    /**
     * A poll queue entry corresponds to a single device that needs
     * to be polled.
     *
     * @author Bernd Pfrommer - Initial contribution
     *
     */
    private static class PQEntry implements Comparable<PQEntry> {
        private LegacyDevice device;
        private long expirationTime;

        PQEntry(LegacyDevice device, long expirationTime) {
            this.device = device;
            this.expirationTime = expirationTime;
        }

        long getExpirationTime() {
            return expirationTime;
        }

        LegacyDevice getDevice() {
            return device;
        }

        @Override
        public int compareTo(PQEntry pqe) {
            return Long.compare(expirationTime, pqe.expirationTime);
        }

        @Override
        public String toString() {
            return device.getAddress().toString() + "/" + String.format("%tc", new Date(expirationTime));
        }
    }
}
