/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.internal.throttler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

/**
 * The {@link Rate} defines a rate limiter that accepts a number of calls to be
 * executed in a given time length. If the quota of calls is used, then calls
 * are scheduled for the next block of time
 *
 * @author Karel Goderis - Initial contribution
 */
public final class Rate {

    private final int numberCalls;
    private final int timeLength;
    private final TimeUnit timeUnit;
    private final LinkedList<Long> callHistory = new LinkedList<Long>();

    public Rate(int numberCalls, int timeLength, TimeUnit timeUnit) {
        this.numberCalls = numberCalls;
        this.timeLength = timeLength;
        this.timeUnit = timeUnit;
    }

    public long timeInMillis() {
        return timeUnit.toMillis(timeLength);
    }

    void addCall(long callTime) {
        callHistory.addLast(callTime);
    }

    private void cleanOld(long now) {
        ListIterator<Long> i = callHistory.listIterator();
        long threshold = now - timeInMillis();
        while (i.hasNext()) {
            if (i.next() <= threshold) {
                i.remove();
            } else {
                break;
            }
        }
    }

    long callTime(long now) {
        cleanOld(now);
        if (callHistory.size() < numberCalls) {
            return now;
        }
        long lastStart = callHistory.getLast() - timeInMillis();
        long firstPeriodCall = lastStart, call;
        int count = 0;
        Iterator<Long> i = callHistory.descendingIterator();
        while (i.hasNext()) {
            call = i.next();
            if (call < lastStart) {
                break;
            } else {
                count++;
                firstPeriodCall = call;
            }
        }
        if (count < numberCalls) {
            return firstPeriodCall + 1;
        } else {
            return firstPeriodCall + timeInMillis() + 1;
        }
    }
}
