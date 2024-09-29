/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.persistence.rrd4j.internal;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

/**
 * This is a Java bean used to return historic items from a rrd4j database.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
public class RRD4jItem implements HistoricItem {

    private final String name;
    private final State state;
    private final ZonedDateTime timestamp;

    public RRD4jItem(String name, State state, ZonedDateTime timestamp) {
        this.name = name;
        this.state = state;
        this.timestamp = timestamp;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return timestamp
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(Locale.getDefault())) + ": "
                + name + " -> " + state.toString();
    }
}
