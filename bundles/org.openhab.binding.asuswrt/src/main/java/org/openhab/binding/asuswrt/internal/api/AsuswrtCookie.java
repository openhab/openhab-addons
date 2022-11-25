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
package org.openhab.binding.asuswrt.internal.api;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * CLASS FOR COOKIE HANDLING
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtCookie {
    protected String cookie = "";
    protected String token = "";
    protected Long cookieTimeStamp = 0L;

    /***********************************
     *
     * SET AND RESET FUNCTIONS
     *
     ************************************/

    /**
     * Set new cookie
     * 
     * @param cookie
     */
    public void setCookie(String cookie) {
        this.cookie = cookie;
        this.cookieTimeStamp = System.currentTimeMillis();
    }

    /**
     * reset cookie
     */
    public void resetCookie() {
        this.cookie = "";
        this.token = "";
        this.cookieTimeStamp = 0L;
    }

    /***********************************
     *
     * CHECK COOKIE
     *
     ************************************/

    /**
     * check if cookie is set
     * 
     * @return
     */
    public Boolean cookieIsSet() {
        return !this.cookie.isBlank();
    }

    /**
     * check if cookie is expired
     * 
     * @return true if cookie is set and expired
     */
    public Boolean cookieIsExpired() {
        if (this.cookieTimeStamp > 0L
                && System.currentTimeMillis() > this.cookieTimeStamp + (COOKIE_LIFETIME_S * 1000)) {
            return true;
        }
        return false;
    }

    /**
     * Check if cookie is set and not expired
     * 
     * @return
     */
    public Boolean isValid() {
        return !cookieIsExpired() && cookieIsSet();
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    public String getCookie() {
        return this.cookie;
    }
}
