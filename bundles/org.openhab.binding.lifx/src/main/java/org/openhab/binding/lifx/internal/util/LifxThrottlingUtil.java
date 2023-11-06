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
package org.openhab.binding.lifx.internal.util;

import static org.openhab.binding.lifx.internal.LifxBindingConstants.PACKET_INTERVAL;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.fields.MACAddress;

/**
 * The {@link LifxThrottlingUtil} is a utility class that regulates the frequency at which messages/packets are
 * sent to LIFX lights. The LIFX LAN Protocol Specification states that lights can process up to 20 messages per second,
 * not more.
 *
 * @author Karel Goderis - Initial contribution
 * @author Wouter Born - Deadlock fix
 */
@NonNullByDefault
public final class LifxThrottlingUtil {

    private LifxThrottlingUtil() {
        // hidden utility class constructor
    }

    /**
     * Tracks when the last packet was sent to a LIFX light. The packet is sent after obtaining the lock and before
     * releasing the lock.
     */
    private static class LifxLightCommunicationTracker {

        private long timestamp;

        private ReentrantLock lock = new ReentrantLock();

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            // When iterating over all trackers another thread may have inserted this object so this thread may not
            // have a lock on it. When the thread does not have the lock, it also did not send a packet.
            if (lock.isHeldByCurrentThread()) {
                timestamp = System.currentTimeMillis();
                lock.unlock();
            }
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * A separate list of trackers is maintained when locking all lights in case of a broadcast. Iterators of
     * {@link ConcurrentHashMap}s may behave non-linear when inserts take place to obtain more concurrency. When the
     * iterator of {@code values()} of {@link #macTrackerMapping} is used for locking all lights, it could sometimes
     * cause deadlock.
     */
    private static List<LifxLightCommunicationTracker> trackers = new CopyOnWriteArrayList<>();

    private static Map<MACAddress, LifxLightCommunicationTracker> macTrackerMapping = new ConcurrentHashMap<>();

    public static void lock(@Nullable MACAddress mac) throws InterruptedException {
        if (mac != null) {
            LifxLightCommunicationTracker tracker = getOrCreateTracker(mac);
            tracker.lock();
            waitForNextPacketInterval(tracker.getTimestamp());
        } else {
            lock();
        }
    }

    private static LifxLightCommunicationTracker getOrCreateTracker(MACAddress mac) {
        LifxLightCommunicationTracker tracker = macTrackerMapping.get(mac);
        if (tracker == null) {
            // for better performance only synchronize when necessary
            synchronized (trackers) {
                // another thread may just have added a tracker in this synchronized block, so reevaluate
                tracker = macTrackerMapping.get(mac);
                if (tracker == null) {
                    tracker = new LifxLightCommunicationTracker();
                    trackers.add(tracker);
                    macTrackerMapping.put(mac, tracker);
                }
            }
        }
        return tracker;
    }

    private static void waitForNextPacketInterval(long timestamp) throws InterruptedException {
        long timeToWait = Math.max(PACKET_INTERVAL - (System.currentTimeMillis() - timestamp), 0);
        if (timeToWait > 0) {
            Thread.sleep(timeToWait);
        }
    }

    public static void unlock(@Nullable MACAddress mac) {
        if (mac != null) {
            LifxLightCommunicationTracker tracker = macTrackerMapping.get(mac);
            if (tracker != null) {
                tracker.unlock();
            }
        } else {
            unlock();
        }
    }

    public static void lock() throws InterruptedException {
        long lastStamp = 0;
        for (LifxLightCommunicationTracker tracker : trackers) {
            tracker.lock();
            lastStamp = Math.max(lastStamp, tracker.getTimestamp());
        }
        waitForNextPacketInterval(lastStamp);
    }

    public static void unlock() {
        for (LifxLightCommunicationTracker tracker : trackers) {
            tracker.unlock();
        }
    }
}
