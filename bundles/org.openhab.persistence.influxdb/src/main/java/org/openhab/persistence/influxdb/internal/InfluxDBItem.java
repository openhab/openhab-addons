/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * This is a Java bean used to return historic items from InfluxDB.
 *
 * @author Theo Weiss - Initial Contribution
 * @author Dominik Vorreiter - Port to OH 2.0
 *
 */
@NonNullByDefault
public class InfluxDBItem implements HistoricItem, PersistenceItemInfo {

    private String name = "";
    private State state = UnDefType.NULL;
    private Date timestamp = new Date(0);

    public InfluxDBItem(String name) {
        this.name = name;
    }

    public InfluxDBItem(String name, State state, Date timestamp) {
        this.name = name;
        this.state = state;
        this.timestamp = timestamp;
    }

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
    public Date getTimestamp() {
        return timestamp;
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
        return null;
    }

    @Override
    public @Nullable Date getEarliest() {
        return null;
    }

    @Override
    public @Nullable Date getLatest() {
        return null;
    }
}
