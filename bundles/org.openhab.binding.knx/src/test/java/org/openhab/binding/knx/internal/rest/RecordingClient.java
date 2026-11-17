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
package org.openhab.binding.knx.internal.rest;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A stream client for the tests: notes every event as its name and its JSON, counts unfinished sends as the test
 * says, and can refuse further events.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class RecordingClient implements StreamClient {
    /** An event as a page would read it. */
    record Event(String name, JsonObject data) {
        long number(String field) {
            return data.get(field).getAsLong();
        }
    }

    final List<Event> events = new ArrayList<>();
    /** Whether each send stays unfinished until {@link #finishAll()}. */
    boolean slow;
    /** Whether a send fails, as when the reader has gone. */
    boolean gone;
    boolean closed;
    /** Unfinished sends it always has besides the counted ones, so that it takes only the rest each pass. */
    int busy;
    private int pending;

    @Override
    public boolean send(String event, Object data) {
        if (gone || closed) {
            return false;
        }
        events.add(new Event(event, JsonParser.parseString(RestFixtures.GSON.toJson(data)).getAsJsonObject()));
        if (slow) {
            pending++;
        }
        return true;
    }

    @Override
    public int pending() {
        return busy + pending;
    }

    @Override
    public boolean finished() {
        return gone || closed;
    }

    @Override
    public void close() {
        closed = true;
    }

    void finishAll() {
        pending = 0;
    }

    List<String> names() {
        return events.stream().map(Event::name).toList();
    }

    List<Event> named(String name) {
        return events.stream().filter(event -> event.name().equals(name)).toList();
    }

    /** {@return the numbers of the telegram events, in the order they came} */
    List<Long> telegramNumbers() {
        return named("telegram").stream().map(event -> event.number("number")).toList();
    }

    Event last() {
        return events.get(events.size() - 1);
    }
}
