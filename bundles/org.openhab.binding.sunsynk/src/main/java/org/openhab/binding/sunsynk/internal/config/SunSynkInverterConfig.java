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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SunSynkInverterConfig} Parameters used for Inverterconfiguration.
 *
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkInverterConfig {

    private String alias = "";
    private String sn = "";
    private int refresh;

    public String getsn() {
        return this.sn;
    }

    public int getRefresh() {
        return this.refresh;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    public void setalias(String alias) {
        this.alias = alias;
    }

    public void setsn(String sn) {
        this.sn = sn;
    }

    @Override
    public String toString() {
        return "InverterConfig [alias=" + alias + ", serial=" + sn + ", refresh=" + refresh + "]";
    }
}
