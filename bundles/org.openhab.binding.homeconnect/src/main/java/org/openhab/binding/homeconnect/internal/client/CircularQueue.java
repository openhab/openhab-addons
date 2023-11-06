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
package org.openhab.binding.homeconnect.internal.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * FIFO queue (ring buffer implementation).
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public class CircularQueue<E> {

    private final ArrayBlockingQueue<E> queue;

    public CircularQueue(final int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
    }

    public synchronized void add(E element) {
        ArrayBlockingQueue<E> myQueue = queue;
        if (myQueue.remainingCapacity() <= 0) {
            myQueue.poll();
        }
        myQueue.add(element);
    }

    public synchronized void addAll(Collection<? extends E> collection) {
        collection.forEach(this::add);
    }

    public synchronized Collection<E> getAll() {
        return new ArrayList<>(queue);
    }
}
