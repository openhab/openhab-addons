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
package org.openhab.binding.knx.internal.monitor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Which rows of the monitor to hand out. Values inside one filter are alternatives, the filters must all hold; then
 * every row within the context span of a match is added, and of the result the newest rows up to the limit are kept.
 * After a row number only the rows above it are looked at, also for the context, and the oldest rows up to the limit
 * are kept, so that a caller can read on without a hole. An empty filter matches everything. The console and REST
 * turn text into one of these with {@link TelegramFilterText}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class TelegramFilter {
    public static final TelegramFilter ALL = builder().build();

    private final Set<IndividualAddress> sources;
    private final Set<GroupAddress> destinations;
    private final Set<TelegramType> types;
    private final Set<Direction> directions;
    private final Set<String> dataTypes;
    private final @Nullable Instant from;
    private final @Nullable Instant to;
    private final Duration context;
    private final int limit;
    private final long afterNumber;

    private TelegramFilter(Builder builder) {
        sources = Set.copyOf(builder.sources);
        destinations = Set.copyOf(builder.destinations);
        types = Set.copyOf(builder.types);
        directions = Set.copyOf(builder.directions);
        dataTypes = Set.copyOf(builder.dataTypes);
        from = builder.from;
        to = builder.to;
        context = builder.context;
        limit = builder.limit;
        afterNumber = builder.afterNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** {@return true if the row passes every filter; the context span and the limit are not looked at} */
    public boolean matches(TelegramRecord row) {
        Instant start = from;
        Instant end = to;
        return isAfterNumber(row) && (sources.isEmpty() || sources.contains(row.source()))
                && (destinations.isEmpty() || destinations.contains(row.destination()))
                && (types.isEmpty() || types.contains(row.type()))
                && (directions.isEmpty() || directions.contains(row.direction()))
                && (dataTypes.isEmpty() || matchesDataType(row.dataType()))
                && (start == null || !row.time().isBefore(start)) && (end == null || !row.time().isAfter(end));
    }

    private boolean isAfterNumber(TelegramRecord row) {
        return afterNumber < 0 || row.number() > afterNumber;
    }

    /** {@return the row number only rows above which are looked at, -1 for none} */
    long afterNumber() {
        return afterNumber;
    }

    int limit() {
        return limit;
    }

    /** {@return true if nothing but the row number and the limit chooses the rows} */
    boolean choosesByNumberOnly() {
        return sources.isEmpty() && destinations.isEmpty() && types.isEmpty() && directions.isEmpty()
                && dataTypes.isEmpty() && from == null && to == null && context.isZero();
    }

    /** A main number such as {@code 9} stands for all its sub types. */
    private boolean matchesDataType(@Nullable String dataType) {
        if (dataType == null) {
            return false;
        }
        if (dataTypes.contains(dataType)) {
            return true;
        }
        int dot = dataType.indexOf('.');
        return dot > 0 && dataTypes.contains(dataType.substring(0, dot));
    }

    /**
     * @param rows the rows to choose from, oldest first
     * @return the chosen rows, oldest first
     */
    public List<TelegramRecord> apply(List<TelegramRecord> rows) {
        List<TelegramRecord> matched = new ArrayList<>();
        List<Instant> matchTimes = new ArrayList<>();
        for (TelegramRecord row : rows) {
            if (matches(row)) {
                matched.add(row);
                matchTimes.add(row.time());
            }
        }
        List<TelegramRecord> result = matched;
        if (!context.isZero() && !matchTimes.isEmpty()) {
            Collections.sort(matchTimes);
            result = new ArrayList<>();
            for (TelegramRecord row : rows) {
                if (isAfterNumber(row) && nearAMatch(row.time(), matchTimes)) {
                    result.add(row);
                }
            }
        }
        if (result.size() > limit) {
            result = afterNumber < 0 ? result.subList(result.size() - limit, result.size()) : result.subList(0, limit);
        }
        return List.copyOf(result);
    }

    private boolean nearAMatch(Instant time, List<Instant> sortedMatchTimes) {
        int index = Collections.binarySearch(sortedMatchTimes, time);
        if (index >= 0) {
            return true;
        }
        int after = -index - 1;
        return (after < sortedMatchTimes.size() && within(time, sortedMatchTimes.get(after)))
                || (after > 0 && within(time, sortedMatchTimes.get(after - 1)));
    }

    private boolean within(Instant time, Instant match) {
        return Duration.between(match, time).abs().compareTo(context) <= 0;
    }

    /** Collects the filters; every method adds to what was given before. */
    public static final class Builder {
        private final Set<IndividualAddress> sources = new HashSet<>();
        private final Set<GroupAddress> destinations = new HashSet<>();
        private final Set<TelegramType> types = new HashSet<>();
        private final Set<Direction> directions = new HashSet<>();
        private final Set<String> dataTypes = new HashSet<>();
        private @Nullable Instant from;
        private @Nullable Instant to;
        private Duration context = Duration.ZERO;
        private int limit = Integer.MAX_VALUE;
        private long afterNumber = -1;

        private Builder() {
        }

        public Builder source(IndividualAddress... values) {
            sources.addAll(Arrays.asList(values));
            return this;
        }

        public Builder destination(GroupAddress... values) {
            destinations.addAll(Arrays.asList(values));
            return this;
        }

        public Builder type(TelegramType... values) {
            types.addAll(Arrays.asList(values));
            return this;
        }

        public Builder direction(Direction... values) {
            directions.addAll(Arrays.asList(values));
            return this;
        }

        /** @param values datapoint types such as {@code 9.001}, or main numbers such as {@code 9} */
        public Builder dataType(String... values) {
            dataTypes.addAll(Arrays.asList(values));
            return this;
        }

        /** Rows at or after this time. */
        public Builder from(Instant time) {
            from = time;
            return this;
        }

        /** Rows at or before this time. */
        public Builder to(Instant time) {
            to = time;
            return this;
        }

        /** Adds every row up to this span before or after a match; negative counts as zero. */
        public Builder context(Duration span) {
            context = span.isNegative() ? Duration.ZERO : span;
            return this;
        }

        /**
         * Looks only at rows with a higher number, and makes the limit keep the oldest rows; negative counts as
         * zero.
         */
        public Builder afterNumber(long number) {
            afterNumber = Math.max(0, number);
            return this;
        }

        /** Keeps at most this many rows, the newest, or the oldest after a row number; negative counts as zero. */
        public Builder limit(int rows) {
            limit = Math.max(0, rows);
            return this;
        }

        public TelegramFilter build() {
            return new TelegramFilter(this);
        }
    }
}
