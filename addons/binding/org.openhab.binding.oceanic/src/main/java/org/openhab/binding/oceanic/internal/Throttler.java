/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.oceanic.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Throttler} is helper class that regulates the frequency at which messages/packets are sent to
 * the serial port.
 *
 * @author Karel Goderis - Initial Contribution
 */
public class Throttler {

    private static Logger logger = LoggerFactory.getLogger(Throttler.class);

    public static final long INTERVAL = 1000;

    private static ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Long> timestamps = new ConcurrentHashMap<>();

    public static void lock(String key) {
        if (!locks.containsKey(key)) {
            locks.put(key, new ReentrantLock());
        }

        locks.get(key).lock();

        if (timestamps.get(key) != null) {
            long lastStamp = timestamps.get(key);
            long timeToWait = Math.max(INTERVAL - (System.currentTimeMillis() - lastStamp), 0);
            if (timeToWait > 0) {
                try {
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    logger.error("An exception occurred while putting the thread to sleep : '{}'", e.getMessage());
                }
            }
        }
    }

    public static void unlock(String key) {
        if (locks.containsKey(key)) {
            timestamps.put(key, System.currentTimeMillis());
            locks.get(key).unlock();
        }
    }

    public static void lock() {
        for (ReentrantLock aLock : locks.values()) {
            aLock.lock();
        }

        long lastStamp = 0;

        for (Long aStamp : timestamps.values()) {
            if (aStamp > lastStamp) {
                lastStamp = aStamp;
            }
        }

        long timeToWait = Math.max(INTERVAL - (System.currentTimeMillis() - lastStamp), 0);
        if (timeToWait > 0) {
            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException e) {
                logger.error("An exception occurred while putting the thread to sleep : '{}'", e.getMessage());
            }
        }
    }

    public static void unlock() {
        for (String key : locks.keySet()) {
            if (locks.get(key).isHeldByCurrentThread()) {
                timestamps.put(key, System.currentTimeMillis());
                locks.get(key).unlock();
            }
        }
    }
}
