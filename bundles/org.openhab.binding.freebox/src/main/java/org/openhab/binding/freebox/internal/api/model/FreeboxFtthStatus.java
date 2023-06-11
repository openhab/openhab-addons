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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link FreeboxFtthStatus} is the Java class used to map the "FtthStatus"
 * structure used by the response of the connection Ftth status API
 * https://dev.freebox.fr/sdk/os/connection/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class FreeboxFtthStatus {
    private boolean sfp_present;
    private boolean sfp_alim_ok;
    private boolean sfp_has_power_report;
    private boolean sfp_has_signal;
    private boolean link;
    private String sfp_serial;
    private String sfp_model;
    private String sfp_vendor;
    private long sfp_pwr_tx;
    private long sfp_pwr_rx;

    public boolean getSfpPresent() {
        return sfp_present;
    }

    public boolean getSfpAlimOk() {
        return sfp_alim_ok;
    }

    public boolean getSfpHasPowerReport() {
        return sfp_has_power_report;
    }

    public boolean getSfpHasSignal() {
        return sfp_has_signal;
    }

    public boolean getLink() {
        return link;
    }

    public String getSfpSerial() {
        return sfp_serial;
    }

    public String getSfpModel() {
        return sfp_model;
    }

    public String getSfpVendor() {
        return sfp_vendor;
    }

    public long getSfpPwrTx() {
        return sfp_pwr_tx;
    }

    public long getSfpPwrRx() {
        return sfp_pwr_rx;
    }
}
