/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VenstarRuntime} represents one Runtime from the RuntimeData returned from the REST API
 *
 * @author Matthew Davies - Initial contribution
 */
@NonNullByDefault
public class VenstarRuntime {
    private long ts;
    private int heat1;
    private int heat2;
    private int cool1;
    private int cool2;
    private int aux1;
    private int aux2;
    private int fc;

    public long getTimeStamp() {
        return ts;
    }

    public void setTimeStamp(long ts) {
        this.ts = ts;
    }

    public int getHeat1Runtime() {
        return heat1;
    }

    public void setHeat1Runtime(int heat1) {
        this.heat1 = heat1;
    }

    public int getHeat2Runtime() {
        return heat2;
    }

    public void setHeat2Runtime(int heat2) {
        this.heat2 = heat2;
    }

    public int getCool1Runtime() {
        return cool1;
    }

    public void setCool1Runtime(int cool1) {
        this.cool1 = cool1;
    }

    public int getCool2Runtime() {
        return cool2;
    }

    public void setCool2Runtime(int cool2) {
        this.cool2 = cool2;
    }

    public int getAux1Runtime() {
        return aux1;
    }

    public void setAux1Runtime(int aux1) {
        this.aux1 = aux1;
    }

    public int getAux2Runtime() {
        return aux2;
    }

    public void setAux2Runtime(int aux2) {
        this.aux2 = aux2;
    }

    public int getFreeCoolRuntime() {
        return fc;
    }

    public void setFreeCoolRuntime(int fc) {
        this.fc = fc;
    }
}
