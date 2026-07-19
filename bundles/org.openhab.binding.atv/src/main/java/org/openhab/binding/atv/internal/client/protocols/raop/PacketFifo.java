/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Simple FIFO for packets based on a map.
 *
 * <p>
 * The FIFO holds at most {@code upperLimit} elements. Each item maps a sequence number
 * to a packet, allowing fast look-up of a certain packet. The order is defined by
 * insertion order and <em>not</em> sequence number order. When the upper limit is
 * exceeded, the item that was inserted <em>first</em> is removed.
 *
 * <p>
 * Used as the retransmit ring: the sender keeps the last N transmitted audio packets
 * keyed by RTP sequence number so lost packets can be resent on request.
 *
 * <p>
 * Not thread-safe; confine to a single loop or synchronize externally.
 *
 * @param <T> packet type stored in the FIFO
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class PacketFifo<T> implements Iterable<Integer> {

    private final LinkedHashMap<Integer, T> items = new LinkedHashMap<>();
    private final int upperLimit;

    /**
     * Creates a new FIFO holding at most {@code upperLimit} items.
     */
    public PacketFifo(int upperLimit) {
        this.upperLimit = upperLimit;
    }

    /**
     * Removes all items in the FIFO.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Returns the number of items in the FIFO.
     */
    public int size() {
        return items.size();
    }

    /**
     * Returns {@code true} if the FIFO holds no items.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Adds an item to the FIFO.
     *
     * @throws IllegalArgumentException if an item with the same index is already present
     */
    public void put(int index, T value) {
        if (items.containsKey(index)) {
            throw new IllegalArgumentException(index + " already in FIFO");
        }
        if (items.size() + 1 > upperLimit) {
            Iterator<Integer> oldest = items.keySet().iterator();
            oldest.next();
            oldest.remove();
        }
        items.put(index, value);
    }

    /**
     * Returns the value of an item.
     *
     * @throws NoSuchElementException if no item with the index exists
     */
    public T get(int index) {
        T value = items.get(index);
        if (value == null && !items.containsKey(index)) {
            throw new NoSuchElementException(String.valueOf(index));
        }
        return value;
    }

    /**
     * Returns whether an element exists in the FIFO.
     */
    public boolean contains(int index) {
        return items.containsKey(index);
    }

    /**
     * Iterates over indices in the FIFO in insertion order.
     */
    @Override
    public Iterator<Integer> iterator() {
        return java.util.Collections.unmodifiableSet(items.keySet()).iterator();
    }

    /**
     * Returns a string representation with only index numbers, e.g. {@code [1, 2]}.
     */
    @Override
    public String toString() {
        return items.keySet().stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * Returns the internal representation, e.g. {@code {1: 2, 2: 3}}.
     */
    public String repr() {
        return items.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * Returns an unmodifiable view of the underlying map (test/debug helper).
     */
    public Map<Integer, T> asMap() {
        return java.util.Collections.unmodifiableMap(items);
    }
}
