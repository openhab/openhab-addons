/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SunSynkInverterConfig} Parameters used for Inverter configuration.
 *
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkInverterConfig {

    private String alias = "";
    private String serialnumber = "";
    private String plantId = "";
    private String plantName = "";
    private int refresh;

    public String getAlias() {
        return this.alias;
    }

    public String getSerialnumber() {
        return this.serialnumber;
    }

    public String getPlantId() {
        return this.plantId;
    }

    public String getPlantName() {
        return this.plantName;
    }

    public int getRefresh() {
        return this.refresh;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setSerialnumber(String sn) {
        this.serialnumber = sn;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    @Override
    public String toString() {
        return "InverterConfig [alias=" + alias + ", serial=" + serialnumber + ", plant ID =" + plantId
                + ", plant name =" + plantName + ", refresh=" + refresh + "]";
    }
}
