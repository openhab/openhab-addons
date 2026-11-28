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
package org.openhab.persistence.victoriametrics.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Bounded queue of the points waiting to be written, e.g. while VictoriaMetrics is unavailable
 * <p>
 * The queue is ordered from the oldest to the newest point. When it is full, the oldest or the newest points are
 * dropped, depending on the {@link OverflowStrategy}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VictoriaMetricsPointQueue {
    public enum OverflowStrategy {
        DROP_OLDEST,
        DROP_NEWEST
    }

    private final int capacity;
    private final OverflowStrategy overflowStrategy;
    private final Deque<VictoriaMetricsPoint> points = new ArrayDeque<>();
    private long droppedCount;

    public VictoriaMetricsPointQueue(int capacity, OverflowStrategy overflowStrategy) {
        this.capacity = capacity;
        this.overflowStrategy = overflowStrategy;
    }

    /**
     * Add a new point, dropping a point if the queue is full
     *
     * @param point the point to add
     */
    public synchronized void add(VictoriaMetricsPoint point) {
        points.addLast(point);
        trimToCapacity();
    }

    /**
     * Remove and return the oldest points
     *
     * @param maxPoints the maximum number of points to return
     * @return the oldest points, at most maxPoints
     */
    public synchronized List<VictoriaMetricsPoint> poll(int maxPoints) {
        List<VictoriaMetricsPoint> result = new ArrayList<>(Math.min(maxPoints, points.size()));
        while (result.size() < maxPoints && !points.isEmpty()) {
            result.add(points.removeFirst());
        }
        return result;
    }

    /**
     * Return points that could not be written, which are older than all queued points, dropping points if the queue
     * is full
     *
     * @param failedPoints the points returned by {@link #poll(int)}, in the same order
     */
    public synchronized void requeue(List<VictoriaMetricsPoint> failedPoints) {
        for (int i = failedPoints.size() - 1; i >= 0; i--) {
            points.addFirst(failedPoints.get(i));
        }
        trimToCapacity();
    }

    private void trimToCapacity() {
        while (points.size() > capacity) {
            if (overflowStrategy == OverflowStrategy.DROP_OLDEST) {
                points.removeFirst();
            } else {
                points.removeLast();
            }
            droppedCount++;
        }
    }

    /**
     * Return the number of points dropped since the last call and reset it
     *
     * @return the number of dropped points
     */
    public synchronized long takeDroppedCount() {
        long result = droppedCount;
        droppedCount = 0;
        return result;
    }

    public synchronized int size() {
        return points.size();
    }

    public synchronized boolean isEmpty() {
        return points.isEmpty();
    }

    public int getCapacity() {
        return capacity;
    }

    public OverflowStrategy getOverflowStrategy() {
        return overflowStrategy;
    }
}
