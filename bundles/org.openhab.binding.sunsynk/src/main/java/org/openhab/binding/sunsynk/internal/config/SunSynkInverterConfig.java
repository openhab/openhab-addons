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

package org.openhab.binding.sunsynk.internal.config;

/**
 * The {@link SunSynkInverterConfig} Parameters used for Inverterconfiguration.
 *
 * @author Lee Charlton - Initial contribution
 */

public class SunSynkInverterConfig {

    private String access_token;
    private String gsn;
    private String modelId;
    private String alias;
    private String sn;
    private int refresh;

    public String getToken() {
        return this.access_token;
    }

    public String getgsn() {
        return this.gsn;
    }

    public String getsn() {
        return this.sn;
    }

    public String getalias() {
        return this.alias;
    }

    public int getRefresh() {
        return this.refresh;
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    @Override
    public String toString() {
        return "InverterConfig [access token =" + access_token + ", gsn=" + gsn + ", serial=" + sn + ", alias=" + alias
                + ", refresh=" + refresh + "]";
    }
}
