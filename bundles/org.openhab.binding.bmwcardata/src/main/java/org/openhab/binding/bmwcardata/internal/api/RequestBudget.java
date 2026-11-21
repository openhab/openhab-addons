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
package org.openhab.binding.bmwcardata.internal.api;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Tracks REST requests over a rolling 24 hour window, so the binding stays within BMW's limit of 50 requests a day.
 * <p>
 * The request times are persisted, so restarting openHAB, or the account thing, does not reset the count while BMW
 * still counts the earlier requests.
 *
 * @author Mark Herwege - Initial contribution
 * @author Michael Weger - Adaptation to the BMW CarData binding
 */
@NonNullByDefault
public class RequestBudget {

    /** BMW's limit of REST requests per account in 24 hours */
    public static final int BMW_DAILY_LIMIT = 50;

    static final Duration WINDOW = Duration.ofHours(24);

    private final Gson gson = new Gson();
    private final Storage<String> storage;
    private final String key;
    private final Clock clock;
    private final int budget;
    private final Deque<Instant> requests = new ArrayDeque<>();

    /**
     * @param storage the binding's storage
     * @param key unique storage key of this account's budget
     * @param budget maximum number of requests in 24 hours; kept between 1 and {@value #BMW_DAILY_LIMIT}
     * @param clock clock to use
     */
    public RequestBudget(Storage<String> storage, String key, int budget, Clock clock) {
        this.storage = storage;
        this.key = key;
        this.budget = Math.max(1, Math.min(BMW_DAILY_LIMIT, budget));
        this.clock = clock;
        load();
    }

    /**
     * Claims one request from the budget.
     *
     * @return whether a request may be sent; when {@code true} the request is counted
     */
    public synchronized boolean tryAcquire() {
        Instant now = clock.instant();
        expire(now);
        if (requests.size() >= budget) {
            return false;
        }
        requests.addLast(now);
        persist();
        return true;
    }

    /**
     * @return the number of requests counted in the last 24 hours
     */
    public synchronized int used() {
        expire(clock.instant());
        return requests.size();
    }

    /**
     * @return the effective budget, the configured one limited to BMW's daily limit
     */
    public int budget() {
        return budget;
    }

    /**
     * @param needed number of requests about to be made
     * @return whether that many requests fit in the budget right now, without claiming them
     */
    public synchronized boolean hasRoomFor(int needed) {
        expire(clock.instant());
        return requests.size() + needed <= budget;
    }

    /**
     * BMW reported its limit as reached: treat the whole budget as used until the oldest counted request ages out.
     */
    public synchronized void markExhausted() {
        Instant now = clock.instant();
        expire(now);
        while (requests.size() < budget) {
            requests.addLast(now);
        }
        persist();
    }

    private void expire(Instant now) {
        Instant cutoff = now.minus(WINDOW);
        while (!requests.isEmpty() && !requests.peekFirst().isAfter(cutoff)) {
            requests.removeFirst();
        }
    }

    private void load() {
        String json = storage.get(key);
        if (json == null) {
            return;
        }
        try {
            long @Nullable [] stored = gson.fromJson(json, long[].class);
            if (stored != null) {
                for (long epochMilli : stored) {
                    requests.addLast(Instant.ofEpochMilli(epochMilli));
                }
            }
        } catch (JsonParseException e) {
            storage.remove(key);
        }
        expire(clock.instant());
    }

    private void persist() {
        storage.put(key, gson.toJson(requests.stream().mapToLong(Instant::toEpochMilli).toArray()));
    }
}
