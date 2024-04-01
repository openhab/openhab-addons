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
package org.openhab.persistence.mongodb.internal;

import java.text.DateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

/**
 * This is the implementation of the MongoDB historic item.
 *
 * @author Thorsten Hoeger - Initial contribution
 */
@NonNullByDefault
public class MongoDBItem implements HistoricItem {

    private final String name;
    private final State state;
    private final ZonedDateTime timestamp;

    public MongoDBItem(String name, State state, ZonedDateTime timestamp) {
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
        Date date = Date.from(timestamp.toInstant());
        return DateFormat.getDateTimeInstance().format(date) + ": " + name + " -> " + state.toString();
    }
}
