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
package org.openhab.binding.homeconnectdirect.internal.common;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A list implementation with a fixed maximum size that discards oldest entries.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class LimitedSizeList<E> {
    private final LinkedList<E> list = new LinkedList<>();
    private final int capacity;

    public LimitedSizeList(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0!");
        }
        this.capacity = capacity;
    }

    public synchronized void add(E element) {
        if (list.size() >= capacity) {
            list.removeFirst();
        }
        list.addLast(element);
    }

    public synchronized List<E> getAllElements() {
        return Collections.unmodifiableList(new LinkedList<>(list));
    }

    public synchronized int size() {
        return list.size();
    }

    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public synchronized String toString() {
        return list.toString();
    }
}
