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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsPointQueue.OverflowStrategy;

/**
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VictoriaMetricsPointQueueTest {
    private final VictoriaMetricsPoint p1 = point(1);
    private final VictoriaMetricsPoint p2 = point(2);
    private final VictoriaMetricsPoint p3 = point(3);
    private final VictoriaMetricsPoint p4 = point(4);

    @Test
    public void pollReturnsOldestPointsFirst() {
        VictoriaMetricsPointQueue queue = new VictoriaMetricsPointQueue(10, OverflowStrategy.DROP_OLDEST);
        queue.add(p1);
        queue.add(p2);
        queue.add(p3);

        assertThat(queue.poll(2), contains(p1, p2));
        assertThat(queue.poll(2), contains(p3));
        assertThat(queue.isEmpty(), is(true));
    }

    @Test
    public void fullQueueDropsOldestPoints() {
        VictoriaMetricsPointQueue queue = new VictoriaMetricsPointQueue(2, OverflowStrategy.DROP_OLDEST);
        queue.add(p1);
        queue.add(p2);
        queue.add(p3);

        assertThat(queue.takeDroppedCount(), is(1L));
        assertThat(queue.takeDroppedCount(), is(0L));
        assertThat(queue.poll(10), contains(p2, p3));
    }

    @Test
    public void fullQueueDropsNewestPoints() {
        VictoriaMetricsPointQueue queue = new VictoriaMetricsPointQueue(2, OverflowStrategy.DROP_NEWEST);
        queue.add(p1);
        queue.add(p2);
        queue.add(p3);

        assertThat(queue.takeDroppedCount(), is(1L));
        assertThat(queue.poll(10), contains(p1, p2));
    }

    @Test
    public void requeuedPointsAreOlderThanQueuedPoints() {
        VictoriaMetricsPointQueue queue = new VictoriaMetricsPointQueue(10, OverflowStrategy.DROP_OLDEST);
        queue.add(p1);
        queue.add(p2);
        List<VictoriaMetricsPoint> failed = queue.poll(2);
        queue.add(p3);

        queue.requeue(failed);

        assertThat(queue.poll(10), contains(p1, p2, p3));
    }

    @Test
    public void requeueIntoFullQueueDropsOldestPoints() {
        VictoriaMetricsPointQueue queue = new VictoriaMetricsPointQueue(3, OverflowStrategy.DROP_OLDEST);
        queue.add(p1);
        queue.add(p2);
        List<VictoriaMetricsPoint> failed = queue.poll(2);
        queue.add(p3);
        queue.add(p4);

        queue.requeue(failed);

        assertThat(queue.takeDroppedCount(), is(1L));
        assertThat(queue.poll(10), contains(p2, p3, p4));
    }

    @Test
    public void requeueIntoFullQueueDropsNewestPoints() {
        VictoriaMetricsPointQueue queue = new VictoriaMetricsPointQueue(3, OverflowStrategy.DROP_NEWEST);
        queue.add(p1);
        queue.add(p2);
        List<VictoriaMetricsPoint> failed = queue.poll(2);
        queue.add(p3);
        queue.add(p4);

        queue.requeue(failed);

        assertThat(queue.takeDroppedCount(), is(1L));
        assertThat(queue.poll(10), contains(p1, p2, p3));
    }

    private static VictoriaMetricsPoint point(int value) {
        return VictoriaMetricsPoint.newBuilder("MyItem").withTime(Instant.ofEpochSecond(value)).withValue(value)
                .withTag("item", "MyItem").build();
    }
}
