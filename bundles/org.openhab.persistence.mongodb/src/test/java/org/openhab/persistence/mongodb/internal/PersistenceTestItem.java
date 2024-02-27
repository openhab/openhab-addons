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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * This class provides helper methods to store generated data for persistence tests.
 * 
 * @author René Ulbricht - Initial contribution
 */
public class PersistenceTestItem {
    public final String itemName;
    public final ZonedDateTime date;
    public final double value;

    public PersistenceTestItem(String itemName, ZonedDateTime date, double value) {
        this.itemName = itemName;
        this.date = date.truncatedTo(ChronoUnit.MILLIS);
        this.value = value;
    }

    @Override
    public String toString() {
        return "PersistenceTestItem{" + "item='" + itemName + '\'' + ", date=" + date + ", value=" + value + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemName, date, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PersistenceTestItem other = (PersistenceTestItem) obj;
        return other.itemName.equals(itemName) && other.date.equals(date) && other.value == value;
    }
}
