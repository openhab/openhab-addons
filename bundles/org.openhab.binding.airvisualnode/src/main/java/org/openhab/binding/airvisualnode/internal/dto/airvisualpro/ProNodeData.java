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
package org.openhab.binding.airvisualnode.internal.dto.airvisualpro;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.airvisualnode.internal.dto.DateAndTime;
import org.openhab.binding.airvisualnode.internal.dto.MeasurementsInterface;
import org.openhab.binding.airvisualnode.internal.dto.NodeDataInterface;
import org.openhab.binding.airvisualnode.internal.dto.airvisual.Settings;
import org.openhab.binding.airvisualnode.internal.dto.airvisual.Status;

/**
 * Top level object for AirVisual Node JSON data.
 *
 * @author Victor Antonovich - Initial contribution
 */
@NonNullByDefault
public class ProNodeData implements NodeDataInterface {

    private DateAndTime dateAndTime;
    private List<Measurements> measurements;
    private String serialNumber;
    private Settings settings;
    private Status status;

    public ProNodeData(DateAndTime dateAndTime, List<Measurements> measurements, String serialNumber, Settings settings,
            Status status) {
        this.dateAndTime = dateAndTime;
        this.measurements = measurements;
        this.serialNumber = serialNumber;
        this.settings = settings;
        this.status = status;
    }

    @Override
    public DateAndTime getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(DateAndTime dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    @Override
    public MeasurementsInterface getMeasurements() {
        return measurements.get(0);
    }

    public void setMeasurements(List<Measurements> measurements) {
        this.measurements = measurements;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
