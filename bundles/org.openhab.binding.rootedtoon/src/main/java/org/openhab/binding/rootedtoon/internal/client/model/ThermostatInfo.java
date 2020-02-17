/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.rootedtoon.internal.client.model;

import java.util.Date;

/**
 *
 * @author daanmeijer - Initial Contribution
 *
 */

public class ThermostatInfo {
    public double currentTemp;
    public double currentSetpoint;
    public double currentInternalBoilerSetpoint;
    public int programState;
    public int activeState;
    public int nextProgram;
    public int nextState;

    public Date getNextTime() {
        return new Date(this.nextTime * 1000L);
    }

    public long nextTime;
    public double nextSetpoint;
    public long randomConfigId;
    public long errorFound;
    public long connection;
    public int burnerInfo;
    public long otCommError;
    public long currentModulationLevel;

    @Override
    public String toString() {
        return String.format("currentTemp: %.1f, currentSetpoint: %.1f", this.currentTemp, this.currentSetpoint);
    }
}
