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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VictoriaMetricsStringStateLimiterTest {
    private final Map<String, @Nullable Set<String>> storedStates = new HashMap<>();
    private final List<String> loadedItems = new ArrayList<>();

    @Test
    public void newStatesAreDroppedAboveLimit() {
        VictoriaMetricsStringStateLimiter limiter = createLimiter(2);

        assertThat(states(limiter.filter(List.of(point("Mode", "A"), point("Mode", "B"), point("Mode", "C"),
                point("Mode", "A"), number("Mode")))), contains("A", "B", "A", null));
        // the limit applies per item
        assertThat(states(limiter.filter(List.of(point("Other", "C")))), contains("C"));
        // stored states are only read once per item
        assertThat(loadedItems, contains("Mode", "Other"));
    }

    @Test
    public void storedStatesCountTowardsLimit() {
        storedStates.put("Mode", Set.of("A", "B"));
        VictoriaMetricsStringStateLimiter limiter = createLimiter(3);

        assertThat(states(limiter.filter(List.of(point("Mode", "B"), point("Mode", "C"), point("Mode", "D")))),
                contains("B", "C"));
    }

    @Test
    public void pointsAreRetriedIfStoredStatesCannotBeRead() {
        storedStates.put("Mode", null);
        VictoriaMetricsStringStateLimiter limiter = createLimiter(3);

        assertThat(limiter.filter(List.of(number("Mode"), point("Mode", "A"))), nullValue());

        storedStates.put("Mode", Set.of());
        assertThat(states(limiter.filter(List.of(number("Mode"), point("Mode", "A")))), contains(null, "A"));
    }

    @Test
    public void pointsWithoutStringStatesAreNotLimited() {
        VictoriaMetricsStringStateLimiter limiter = createLimiter(1);

        assertThat(states(limiter.filter(List.of(number("Mode"), number("Mode")))), contains(null, null));
        assertThat(loadedItems, empty());
    }

    @Test
    public void emptyStateIsNotLimited() {
        VictoriaMetricsStringStateLimiter limiter = createLimiter(1);

        assertThat(states(limiter.filter(List.of(point("Mode", ""), point("Mode", "A"), point("Mode", "")))),
                contains("", "A", ""));
    }

    private VictoriaMetricsStringStateLimiter createLimiter(int maxStates) {
        return new VictoriaMetricsStringStateLimiter(maxStates, (metricName, itemName) -> {
            loadedItems.add(itemName);
            return storedStates.containsKey(itemName) ? storedStates.get(itemName) : Set.of();
        });
    }

    private static VictoriaMetricsPoint point(String itemName, String state) {
        return VictoriaMetricsPoint.newBuilder(itemName).withTime(Instant.EPOCH).withStringState(state)
                .withTag("item", itemName).build();
    }

    private static VictoriaMetricsPoint number(String itemName) {
        return VictoriaMetricsPoint.newBuilder(itemName).withTime(Instant.EPOCH).withValue(1).withTag("item", itemName)
                .build();
    }

    private static List<@Nullable String> states(@Nullable List<VictoriaMetricsPoint> points) {
        return Objects.requireNonNull(points).stream().map(VictoriaMetricsPoint::getStringState).toList();
    }
}
