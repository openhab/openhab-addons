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
package org.openhab.binding.plclogo.internal.config;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.NOT_SUPPORTED;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plclogo.internal.PLCLogoBindingConstants;

/**
 * The {@link PLCLogoBridgeConfiguration} hold configuration of Siemens LOGO! PLCs.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCLogoBridgeConfiguration {

    private String address = "";
    private String family = NOT_SUPPORTED;
    private String localTSAP = "0x3000";
    private String remoteTSAP = "0x2000";
    private Integer refresh = 100;

    /**
     * Get configured Siemens LOGO! device IP address.
     *
     * @return Configured Siemens LOGO! IP address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set IP address for Siemens LOGO! device.
     *
     * @param address IP address of Siemens LOGO! device
     */
    public void setAddress(final String address) {
        this.address = address.trim();
    }

    /**
     * Get configured Siemens LOGO! device family.
     *
     * @see PLCLogoBindingConstants#LOGO_0BA7
     * @see PLCLogoBindingConstants#LOGO_0BA8
     * @return Configured Siemens LOGO! device family
     */
    public String getFamily() {
        return family;
    }

    /**
     * Set Siemens LOGO! device family.
     *
     * @param family Family of Siemens LOGO! device
     * @see PLCLogoBindingConstants#LOGO_0BA7
     * @see PLCLogoBindingConstants#LOGO_0BA8
     */
    public void setFamily(final String family) {
        this.family = family.trim();
    }

    /**
     * Get configured local TSAP of Siemens LOGO! device.
     *
     * @return Configured local TSAP of Siemens LOGO!
     */
    public @Nullable Integer getLocalTSAP() {
        Integer result = null;
        if (localTSAP.startsWith("0x")) {
            result = Integer.decode(localTSAP);
        }
        return result;
    }

    /**
     * Set local TSAP of Siemens LOGO! device.
     *
     * @param tsap Local TSAP of Siemens LOGO! device
     */
    public void setLocalTSAP(final String tsap) {
        this.localTSAP = tsap.trim();
    }

    /**
     * Get configured remote TSAP of Siemens LOGO! device.
     *
     * @return Configured local TSAP of Siemens LOGO!
     */
    public @Nullable Integer getRemoteTSAP() {
        Integer result = null;
        if (remoteTSAP.startsWith("0x")) {
            result = Integer.decode(remoteTSAP);
        }
        return result;
    }

    /**
     * Set remote TSAP of Siemens LOGO! device.
     *
     * @param tsap Remote TSAP of Siemens LOGO! device
     */
    public void setRemoteTSAP(final String tsap) {
        this.remoteTSAP = tsap.trim();
    }

    /**
     * Get configured refresh rate of Siemens LOGO! device blocks in milliseconds.
     *
     * @return Configured refresh rate of Siemens LOGO! device blocks
     */
    public Integer getRefreshRate() {
        return refresh;
    }

    /**
     * Set refresh rate of Siemens LOGO! device blocks in milliseconds.
     *
     * @param rate Refresh rate of Siemens LOGO! device blocks
     */
    public void setRefreshRate(final Integer rate) {
        this.refresh = rate;
    }
}
