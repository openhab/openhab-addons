/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.persistence.dynamodb.internal;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

/**
 * This is a Java bean used to return historic items from Dynamodb.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class DynamoDBHistoricItem implements HistoricItem {
    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern(DynamoDBItem.DATE_FORMAT)
            .withZone(UTC);

    private final String name;
    private final State state;
    private final ZonedDateTime timestamp;

    public DynamoDBHistoricItem(String name, State state, ZonedDateTime timestamp) {
        this.name = name;
        this.state = state;
        this.timestamp = timestamp;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return name + ": " + DATEFORMATTER.format(timestamp) + ": " + state.toString();
    }
}
