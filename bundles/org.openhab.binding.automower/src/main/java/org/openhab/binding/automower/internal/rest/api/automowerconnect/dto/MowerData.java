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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class MowerData {
    private System system;
    private Battery battery;
    private Capabilities capabilities;
    private MowerApp mower;
    private Calendar calendar;
    private Planner planner;
    private Metadata metadata;
    private List<Position> positions = new ArrayList<>();
    private Settings settings;
    private Statistics statistics;
    private StayOutZones stayOutZones;
    private List<WorkArea> workAreas = new ArrayList<>();

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

    public Capabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
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

    public List<Position> getPositions() {
        return this.positions;
    }

    public Position getLastPosition() {
        return !this.positions.isEmpty() ? this.positions.get(0) : null;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public StayOutZones getStayOutZones() {
        return stayOutZones;
    }

    public void setStayOutZones(StayOutZones stayOutZones) {
        this.stayOutZones = stayOutZones;
    }

    public List<WorkArea> getWorkAreas() {
        return workAreas;
    }

    public void setWorkAreas(List<WorkArea> workAreas) {
        this.workAreas = workAreas;
    }
}
