/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.core.persistence.PersistedItem;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * This is a Java bean used to persist item states with timestamps in the database.
 *
 * @author Jens Viebig - Initial contribution
 * @author Mark Herwege - Add lastState and lastStateChange
 *
 */
@NonNullByDefault
class MapDbItem implements PersistedItem, PersistenceItemInfo {

    private String name = "";
    private State state = UnDefType.NULL;
    private Date timestamp = new Date(0);
    private @Nullable State lastState = null;
    private @Nullable Date lastStateChange = null;

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public State getState() {
        return state;
    }

    void setState(State state) {
        this.state = state;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    }

    void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public @Nullable State getLastState() {
        return lastState;
    }

    void setLastState(@Nullable State lastState) {
        this.lastState = lastState;
    }

    @Override
    public @Nullable ZonedDateTime getLastStateChange() {
        return lastStateChange != null ? ZonedDateTime.ofInstant(lastStateChange.toInstant(), ZoneId.systemDefault())
                : null;
    }

    void setLastStateChange(@Nullable Date lastStateChange) {
        this.lastStateChange = lastStateChange;
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

    boolean isValid() {
        return name != null && state != null && timestamp != null;
    }
}
