/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.blinds.action.internal.util;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class IntStatCounter {
    private int min;
    private int max;
    private int sum;
    private int count;
    private int first;
    private int last;

    public synchronized void add(int value) {
        if (count == 0) {
            first = value;
        }

        last = value;
        sum += value;

        if (count == 0) {
            min = value;
            max = value;
        } else {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        count++;
    }

    public synchronized boolean hasValue() {
        return count > 0;
    }

    public synchronized int getMin() {
        return min;
    }

    public synchronized int getMax() {
        return max;
    }

    public synchronized int getSum() {
        return sum;
    }

    public synchronized int getCount() {
        return count;
    }

    public synchronized int getFirst() {
        return first;
    }

    public synchronized int getLast() {
        return last;
    }

    public synchronized double getAverage() {
        return (double) sum / count;
    }

}
