/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

import java.util.ArrayList;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class MowerData {
    private System system;
    private Battery battery;
    private MowerApp mower;
    private Calendar calendar;
    private Planner planner;
    private Metadata metadata;
    private ArrayList<Position> positions = new ArrayList<Position>();

    public System getSystem() {
        return system;
    }

    public void setSystem(System system) {
        this.system = system;
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    public MowerApp getMower() {
        return mower;
    }

    public void setMower(MowerApp mower) {
        this.mower = mower;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public Planner getPlanner() {
        return planner;
    }

    public void setPlanner(Planner planner) {
        this.planner = planner;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void addPosition(Position position) {
        this.positions.add(position);
    }

    public ArrayList<Position> getPositions() {
        return this.positions;
    }

    public Position getLastPosition() {
        return !this.positions.isEmpty() ? this.positions.get(0) : null;
    }
}
