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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NhcSystemInfo1} class represents the systeminfo Niko Home Control communication object. It contains all
 * Niko Home Control system data received from the Niko Home Control controller when initializing the connection.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public final class NhcSystemInfo1 {

    private String swVersion = "";
    private String api = "";
    private String time = "";
    private String language = "";
    private String currency = "";
    private String units = "";
    private String dst = "";
    private String tz = "";
    private String lastEnergyErase = "";
    private String lastConfig = "";

    public String getSwVersion() {
        return swVersion;
    }

    void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getApi() {
        return api;
    }

    void setApi(String api) {
        this.api = api;
    }

    public String getTime() {
        return time;
    }

    void setTime(String time) {
        this.time = time;
    }

    public String getLanguage() {
        return language;
    }

    void setLanguage(String language) {
        this.language = language;
    }

    public String getCurrency() {
        return currency;
    }

    void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUnits() {
        return units;
    }

    void setUnits(String units) {
        this.units = units;
    }

    public String getDst() {
        return dst;
    }

    void setDst(String dst) {
        this.dst = dst;
    }

    public String getTz() {
        return tz;
    }

    void setTz(String tz) {
        this.tz = tz;
    }

    public String getLastEnergyErase() {
        return lastEnergyErase;
    }

    void setLastEnergyErase(String lastEnergyErase) {
        this.lastEnergyErase = lastEnergyErase;
    }

    public String getLastConfig() {
        return lastConfig;
    }

    void setLastConfig(String lastConfig) {
        this.lastConfig = lastConfig;
    }
}
