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
package org.openhab.binding.boschspexor.internal.api.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Representation of a status object
 *
 * @author Marc Fischer - Initial contribution
 *
 */
@NonNullByDefault
public class Status {

    private Energy energy = new Energy();
    private Connection connection = new Connection();
    private Firmware firmware = new Firmware();
    private List<ObservationStatus> observation = new ArrayList<>();

    public Energy getEnergy() {
        return energy;
    }

    public void setEnergy(Energy energy) {
        this.energy = energy;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Firmware getFirmware() {
        return firmware;
    }

    public void setFirmware(Firmware firmware) {
        this.firmware = firmware;
    }

    public List<ObservationStatus> getObservation() {
        return observation;
    }

    public void setObservation(List<ObservationStatus> observation) {
        this.observation = observation;
    }
}
