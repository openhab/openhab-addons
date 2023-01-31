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
package org.openhab.persistence.influxdb.internal;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Row data returned from database query
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class InfluxRow {
    private final String itemName;
    private final Instant time;
    private final Object value;

    public InfluxRow(Instant time, String itemName, Object value) {
        this.time = time;
        this.itemName = itemName;
        this.value = value;
    }

    public Instant getTime() {
        return time;
    }

    public String getItemName() {
        return itemName;
    }

    public Object getValue() {
        return value;
    }
}
