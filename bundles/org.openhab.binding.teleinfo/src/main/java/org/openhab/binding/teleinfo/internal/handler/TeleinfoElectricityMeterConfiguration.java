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
package org.openhab.binding.teleinfo.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TeleinfoElectricityMeterConfiguration} class stores electricity meter thing configuration
 *
 * @author Olivier MARCEAU - Initial contribution
 */
@NonNullByDefault
public class TeleinfoElectricityMeterConfiguration {

    private String adco = "";
    private String idd2l = "";
    private String appKey = "";
    private String ivKey = "";

    public String getAdco() {
        return adco;
    }

    public String getIdd2l() {
        return idd2l;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getIvKey() {
        return ivKey;
    }

    public void setAdco(String adco) {
        this.adco = adco;
    }

    public void setIdd2l(String idd2l) {
        this.idd2l = idd2l;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void setIvKey(String ivKey) {
        this.ivKey = ivKey;
    }
}
