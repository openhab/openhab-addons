/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
    private Boolean sfp_present;
    private Boolean sfp_alim_ok;
    private Boolean sfp_has_power_report;
    private Boolean sfp_has_signal;
    private Boolean link;
    private String sfp_serial;
    private String sfp_model;
    private String sfp_vendor;
    private long sfp_pwr_tx;
    private long sfp_pwr_rx;

    public Boolean getSfp_present() {
        return sfp_present;
    }

    public Boolean getSfp_alim_ok() {
        return sfp_alim_ok;
    }

    public Boolean getSfp_has_power_report() {
        return sfp_has_power_report;
    }

    public Boolean getSfp_has_signal() {
        return sfp_has_signal;
    }

    public Boolean getLink() {
        return link;
    }

    public String getSfp_serial() {
        return sfp_serial;
    }

    public String getSfp_model() {
        return sfp_model;
    }

    public String getSfp_vendor() {
        return sfp_vendor;
    }

    public long getSfp_pwr_tx() {
        return sfp_pwr_tx;
    }

    public long getSfp_pwr_rx() {
        return sfp_pwr_rx;
    }

}
