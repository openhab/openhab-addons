/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal;

/**
 * Computes a rolling average of readings.
 *
 * @author Volker Bier - Initial contribution
 */
public abstract class RollingReadingAverage<R extends Reading> {
    private int size = 0;
    private int maxSize;
    private R total = null;
    private int index = 0;
    private R[] samples;

    public RollingReadingAverage(R[] array) {
        maxSize = array.length;
        samples = array;
    }

    public void add(R reading) {
        if (size < maxSize) {
            size++;
        }

        if (total == null) {
            total = reading;
        } else {
            total = add(total, reading);
            total = substract(total, samples[index]);
        }

        samples[index] = reading;
        if (++index == maxSize) {
            index = 0;
        }
    }

    public R getAverage() {
        if (total == null) {
            return null;
        }
        return divide(total, size);
    }

    protected abstract R add(R value1, R value2);

    protected abstract R substract(R from, R value);

    protected abstract R divide(R value, int count);
}
