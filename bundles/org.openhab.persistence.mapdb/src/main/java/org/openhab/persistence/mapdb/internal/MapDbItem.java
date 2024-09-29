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
package org.openhab.persistence.mapdb.internal;

import java.text.DateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * This is a Java bean used to persist item states with timestamps in the database.
 *
 * @author Jens Viebig - Initial contribution
 *
 */
@NonNullByDefault
public class MapDbItem implements HistoricItem, PersistenceItemInfo {

    private String name = "";
    private State state = UnDefType.NULL;
    private Date timestamp = new Date(0);

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return DateFormat.getDateTimeInstance().format(timestamp) + ": " + name + " -> " + state.toString();
    }

    @Override
    public @Nullable Integer getCount() {
        return Integer.valueOf(1);
    }

    @Override
    public @Nullable Date getEarliest() {
        return null;
    }

    @Override
    public @Nullable Date getLatest() {
        return null;
    }

    public boolean isValid() {
        return name != null && state != null && timestamp != null;
    }
}
