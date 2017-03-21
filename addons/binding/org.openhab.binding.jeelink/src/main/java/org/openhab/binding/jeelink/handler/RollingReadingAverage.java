/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler;

import java.lang.reflect.Array;

/**
 * Computes a rolling average of readings.
 *
 * @author Volker Bier - Initial contribution
 */
public class RollingReadingAverage<R extends Reading> implements Average<R> {
    private int size = 0;
    private int maxSize;
    private R total = null;
    private int index = 0;
    private R[] samples;

    public RollingReadingAverage(Class<R> clazz, int size) {
        maxSize = size;
        samples = (R[]) Array.newInstance(clazz, maxSize);
    }

    @Override
    public void add(R reading) {
        if (size < maxSize) {
            size++;
        }

        if (total == null) {
            total = reading;
        } else {
            total = (R) total.add(reading).substract(samples[index]);
        }

        samples[index] = reading;
        if (++index == maxSize) {
            index = 0;
        }
    }

    @Override
    public R getAverage() {
        if (total == null) {
            return null;
        }
        return (R) total.divide(size);
    }
}
