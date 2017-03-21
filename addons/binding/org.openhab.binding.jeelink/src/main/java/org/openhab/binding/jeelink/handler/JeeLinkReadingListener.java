/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler;

/**
 * Reading listener that only keeps track of the current reading.
 *
 * @author Volker Bier - Initial contribution
 */
public class JeeLinkReadingListener<R extends Reading> {
    public static final String ALL = "All";

    protected R lastReading = null;
    private long lastReadingTime = -1;
    private Average<R> readingAverage;

    /**
     * Constructs a reading listener that only keeps track of the current reading.
     */
    public JeeLinkReadingListener() {
    }

    /**
     * Constructs a reading listener that uses a rolling average to compute current reading.
     */
    public JeeLinkReadingListener(Average<R> average) {
        readingAverage = average;
    }

    public synchronized void handleReading(R reading) {
        lastReading = reading;
        lastReadingTime = System.currentTimeMillis();

        if (readingAverage != null) {
            readingAverage.add(reading);
        }
    }

    public long getLastReadingTime() {
        return lastReadingTime;
    }

    public R getCurrentReading() {
        if (readingAverage != null) {
            return readingAverage.getAverage();
        }

        return lastReading;
    }
}
