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
package org.openhab.binding.asuswrt.internal.api;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.COOKIE_LIFETIME_S;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AsuswrtCookie} is used for storing cookie details.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtCookie {
    protected String cookie = "";
    protected String token = "";
    protected Long cookieTimeStamp = 0L;

    /*
     * Set and reset functions
     */

    /**
     * Sets a new cookie.
     */
    public void setCookie(String cookie) {
        this.cookie = cookie;
        cookieTimeStamp = System.currentTimeMillis();
    }

    /**
     * Resets a cookie.
     */
    public void resetCookie() {
        cookie = "";
        token = "";
        cookieTimeStamp = 0L;
    }

    /*
     * Cookie checks
     */

    /**
     * Checks if a cookie is set.
     */
    public boolean cookieIsSet() {
        return !cookie.isBlank();
    }

    /**
     * Checks if a cookie is expired.
     *
     * @return <code>true</code> if cookie is set and expired
     */
    public boolean cookieIsExpired() {
        return cookieTimeStamp > 0L && System.currentTimeMillis() > cookieTimeStamp + (COOKIE_LIFETIME_S * 1000);
    }

    /**
     * Checks if a cookie is set and not expired.
     */
    public boolean isValid() {
        return !cookieIsExpired() && cookieIsSet();
    }

    /**
     * Gets the cookie.
     */
    public String getCookie() {
        return cookie;
    }
}
