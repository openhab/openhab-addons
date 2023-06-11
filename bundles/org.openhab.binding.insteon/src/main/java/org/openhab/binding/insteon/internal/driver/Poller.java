/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.sql.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
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
public class Poller {
    private static final long MIN_MSEC_BETWEEN_POLLS = 2000L;

    private final Logger logger = LoggerFactory.getLogger(Poller.class);
    private static Poller poller = new Poller(); // for singleton

    private @Nullable Thread pollThread = null;
    private TreeSet<PQEntry> pollQueue = new TreeSet<>();
    private boolean keepRunning = true;

    /**
     * Constructor
     */
    private Poller() {
    }

    /**
     * Get size of poll queue
     *
     * @return number of devices being polled
     */
    public int getSizeOfQueue() {
        return (pollQueue.size());
    }

    /**
     * Register a device for polling.
     *
     * @param d device to register for polling
     * @param aNumDev approximate number of total devices
     */
    public void startPolling(InsteonDevice d, int aNumDev) {
        logger.debug("start polling device {}", d);
        synchronized (pollQueue) {
            // try to spread out the scheduling when
            // starting up
            int n = pollQueue.size();
            long pollDelay = n * d.getPollInterval() / (aNumDev > 0 ? aNumDev : 1);
            addToPollQueue(d, System.currentTimeMillis() + pollDelay);
            pollQueue.notify();
        }
    }

    /**
     * Start polling a given device
     *
     * @param d reference to the device to be polled
     */
    public void stopPolling(InsteonDevice d) {
        synchronized (pollQueue) {
            for (Iterator<PQEntry> i = pollQueue.iterator(); i.hasNext();) {
                if (i.next().getDevice().getAddress().equals(d.getAddress())) {
                    i.remove();
                    logger.debug("stopped polling device {}", d);
                }
            }
        }
    }

    /**
     * Starts the poller thread
     */
    public void start() {
        if (pollThread == null) {
            pollThread = new Thread(new PollQueueReader());
            setParamsAndStart(pollThread);
        }
    }

    private void setParamsAndStart(@Nullable Thread thread) {
        if (thread != null) {
            thread.setName("OH-binding-" + InsteonBindingConstants.BINDING_ID + "-pollQueueReader");
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Stops the poller thread
     */
    public void stop() {
        logger.debug("stopping poller!");
        synchronized (pollQueue) {
            pollQueue.clear();
            keepRunning = false;
            pollQueue.notify();
        }
        try {
            Thread pollThread = this.pollThread;
            if (pollThread != null) {
                pollThread.join();
                this.pollThread = null;
            }
            keepRunning = true;
        } catch (InterruptedException e) {
            logger.debug("got interrupted on exit: {}", e.getMessage());
        }
    }

    /**
     * Adds a device to the poll queue. After this call, the device's doPoll() method
     * will be called according to the polling frequency set.
     *
     * @param d the device to poll periodically
     * @param time the target time for the next poll to happen. Note that this time is merely
     *            a suggestion, and may be adjusted, because there must be at least a minimum gap in polling.
     */

    private void addToPollQueue(InsteonDevice d, long time) {
        long texp = findNextExpirationTime(d, time);
        PQEntry ne = new PQEntry(d, texp);
        logger.trace("added entry {} originally aimed at time {}", ne, String.format("%tc", new Date(time)));
        pollQueue.add(ne);
    }

    /**
     * Finds the best expiration time for a poll queue, i.e. a time slot that is after the
     * desired expiration time, but does not collide with any of the already scheduled
     * polls.
     *
     * @param d device to poll (for logging)
     * @param aTime desired time after which the device should be polled
     * @return the suggested time to poll
     */

    private long findNextExpirationTime(InsteonDevice d, long aTime) {
        long expTime = aTime;
        // tailSet finds all those that expire after aTime - buffer
        SortedSet<PQEntry> ts = pollQueue.tailSet(new PQEntry(d, aTime - MIN_MSEC_BETWEEN_POLLS));
        if (ts.isEmpty()) {
            // all entries in the poll queue are ahead of the new element,
            // go ahead and simply add it to the end
            expTime = aTime;
        } else {
            Iterator<PQEntry> pqi = ts.iterator();
            PQEntry prev = pqi.next();
            if (prev.getExpirationTime() > aTime + MIN_MSEC_BETWEEN_POLLS) {
                // there is a time slot free before the head of the tail set
                expTime = aTime;
            } else {
                // look for a gap where we can squeeze in
                // a new poll while maintaining MIN_MSEC_BETWEEN_POLLS
                while (pqi.hasNext()) {
                    PQEntry pqe = pqi.next();
                    long tcurr = pqe.getExpirationTime();
                    long tprev = prev.getExpirationTime();
                    if (tcurr - tprev >= 2 * MIN_MSEC_BETWEEN_POLLS) {
                        // found gap
                        logger.trace("dev {} time {} found slot between {} and {}", d, aTime, tprev, tcurr);
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
            logger.debug("starting poll thread.");
            synchronized (pollQueue) {
                while (keepRunning) {
                    try {
                        readPollQueue();
                    } catch (InterruptedException e) {
                        logger.warn("poll queue reader thread interrupted!");
                        break;
                    }
                }
            }
            logger.debug("poll thread exiting");
        }

        /**
         * Waits for first element of poll queue to become current,
         * then process it.
         *
         * @throws InterruptedException
         */
        private void readPollQueue() throws InterruptedException {
            while (pollQueue.isEmpty() && keepRunning) {
                pollQueue.wait();
            }
            if (!keepRunning) {
                return;
            }
            // something is in the queue
            long now = System.currentTimeMillis();
            PQEntry pqe = pollQueue.first();
            long tfirst = pqe.getExpirationTime();
            long dt = tfirst - now;
            if (dt > 0) { // must wait for this item to expire
                logger.trace("waiting for {} msec until {} comes due", dt, pqe);
                pollQueue.wait(dt);
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
        private InsteonDevice dev;
        private long expirationTime;

        PQEntry(InsteonDevice dev, long time) {
            this.dev = dev;
            this.expirationTime = time;
        }

        long getExpirationTime() {
            return expirationTime;
        }

        InsteonDevice getDevice() {
            return dev;
        }

        @Override
        public int compareTo(PQEntry b) {
            return (int) (expirationTime - b.expirationTime);
        }

        @Override
        public String toString() {
            return dev.getAddress().toString() + "/" + String.format("%tc", new Date(expirationTime));
        }
    }

    /**
     * Singleton pattern instance() method
     *
     * @return the poller instance
     */
    public static synchronized Poller instance() {
        poller.start();
        return (poller);
    }
}
