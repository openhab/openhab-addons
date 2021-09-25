/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.venstarthermostat.internal.dto;

/**
 * The {@link VenstarSensor} represents a sensor returned from the REST API.
 *
 * @author Matthew Davies - Class added to add more functionality to binding
 */
public class VenstarRuntime {
    int ts;
    int heat1;
    int heat2;
    int cool1;
    int cool2;
    int aux1;
    int aux2;
    int fc;

    public int getTimeStamp() {
        return ts;
    }

    public void setTimeStamp(int ts) {
        this.ts = ts;
    }

    public int getHeat1() {
        return heat1;
    }

    public void setHeat1(int heat1) {
        this.heat1 = heat1;
    }

    public int getHeat2() {
        return heat2;
    }

    public void setHeat2(int heat2) {
        this.heat2 = heat2;
    }

    public int getCool1() {
        return cool1;
    }

    public void setCool1(int cool1) {
        this.cool1 = cool1;
    }

    public int getCool2() {
        return cool2;
    }

    public void setCool2(int cool2) {
        this.cool2 = cool2;
    }

    public int getAux1() {
        return aux1;
    }

    public void setAux1(int aux1) {
        this.aux1 = aux1;
    }

    public int getAux2() {
        return aux2;
    }

    public void setAux2(int aux2) {
        this.aux2 = aux2;
    }

    public int getFC() {
        return fc;
    }

    public void setFC(int fc) {
        this.fc = fc;
    }

}
