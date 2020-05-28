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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class RingBuffer<E> {
    private ArrayList<E> buffer = null;
    private long minIndex = 0;
    private long maxIndex = 0;
    private int maxSize;
    private int shift = 0;

    public RingBuffer(int capacity) {
        buffer = new ArrayList<E>(capacity);
        this.maxSize = capacity;
    }

    public int getCapacity() {
        return maxSize;
    }

    /*
     * void debugout() {
     * System.out.println("maxSize: " + maxSize +
     * " shift: " + shift + " minIndex: " + minIndex + " maxIndex: " + maxIndex);
     * }
     */

    public E get(long index) {
        if ((index < minIndex) || (index >= maxIndex)) {
            return null;
        }

        return buffer.get((int) ((index + shift) % maxSize));
    }

    /**
     * @param e the element to insert (may override last element if ringbuffer is full)
     * @return the element previously at the specified position
     */
    public E add(E e) {
        E old = null;
        if (!isFull()) {
            // extend ringbuffer
            buffer.add(e);
            maxIndex++;
        } else {
            // overwrite oldest element
            old = buffer.set((int) ((minIndex + shift) % maxSize), e);
            minIndex++;
            maxIndex++;
        }
        return old;
    }

    public int size() {
        return (int) (maxIndex - minIndex);
    }

    public long getMinIndex() {
        return minIndex;
    }

    /**
     *
     * @return the first and oldest entry in the ringbuffer. null, if no item is in the ringbuffer
     */
    public E getFirst() {
        return get(minIndex);
    }

    /**
     * the last and newest entry in the ringbuffer
     *
     * @return null, if no item is in the ringbuffer
     */
    public E getLast() {
        return get(maxIndex - 1);
    }

    public Iterator<E> iterator() {
        return new RingBufferIterator();
    }

    public Iterator<E> backwardIterator() {
        return new BackwardRingBufferIterator();
    }

    public void clear() {
        buffer.clear();
        minIndex = 0;
        maxIndex = 0;
        shift = 0;

    }

    class RingBufferIterator implements Iterator<E> {
        long index = minIndex;

        // Returns true if the iteration has more elements.
        @Override
        public boolean hasNext() {
            return index < maxIndex;
        }

        @Override
        public E next() {
            if (hasNext()) {
                return get(index++); // Returns the next element in the iteration.
            }
            throw new NoSuchElementException();
        }
    }

    class BackwardRingBufferIterator implements Iterator<E> {
        long index = maxIndex;

        // Returns true if the iteration has more elements.
        @Override
        public boolean hasNext() {
            return index > minIndex;
        }

        @Override
        public E next() {
            if (hasNext()) {
                return get(--index); // Returns the next element in the iteration.
            }
            throw new NoSuchElementException();
        }
    }

    public boolean isFull() {
        return buffer.size() >= maxSize;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
