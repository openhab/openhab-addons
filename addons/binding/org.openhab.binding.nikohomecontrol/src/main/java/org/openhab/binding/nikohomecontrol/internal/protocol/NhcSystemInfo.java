/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol;

/**
 * The {@link NhcSystemInfo} class represents the systeminfo Niko Home Control communication object. It contains all
 * Niko Home Control system data received from the Niko Home Control controller when initializing the connection.
 *
 * @author Mark Herwege
 */
public final class NhcSystemInfo {

    // Initialize with empty strings. If null, downstream methods may throw null pointer exceptions. These
    // exceptions cause threads to stop without warning, no way to catch the exception. This behavior happened when
    // trying to write null to properties of things. So this avoids having to check before the call for null
    // pointers each time as the null exceptions cannot be caught.
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
