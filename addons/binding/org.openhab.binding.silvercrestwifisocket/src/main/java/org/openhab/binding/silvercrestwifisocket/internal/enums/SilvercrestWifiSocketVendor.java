/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.silvercrestwifisocket.internal.enums;

/**
 * This enum represents the available Wifi Socket vendors.
 *
 * @author Christian Heimerl - Initial contribution
 *
 */
public enum SilvercrestWifiSocketVendor {
    LIDL_SILVERCREST("C1", "7150"),
    ALDI_EASYHOME("C2", "92DD");

    private final String companyCode;
    private String authenticationCode;

    SilvercrestWifiSocketVendor(String companyCode, String authenticationCode) {
        this.companyCode = companyCode;
        this.authenticationCode = authenticationCode;
    }

    /**
     * Gets the hexadecimal company code included in a request message.
     *
     * @return the hexadecimal company code
     */
    public String getCompanyCode() {
        return this.companyCode;
    }

    /**
     * Gets the hexadecimal authentication code included in a request message
     *
     * @return the hexadecimal authentication code
     */
    public String getAuthenticationCode() {
        return this.authenticationCode;
    }

    /**
     * Returns the SilvercrestWifiSocketVendor matching the given two digit company code.
     *
     * @param companyCode The two digit company code that should be searched for, e.g. C1
     * @return A SilvercrestWifiSocketVendor or null if no vendor matches the company code.
     */
    public static SilvercrestWifiSocketVendor fromCode(String companyCode) {
        for (SilvercrestWifiSocketVendor v : SilvercrestWifiSocketVendor.values()) {
            if (v.getCompanyCode().equals(companyCode)) {
                return v;
            }
        }
        return null;
    }
}
