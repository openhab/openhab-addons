/**
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
package org.openhab.binding.mybmw.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.utils.Constants;

/**
 * The {@link MyBMWBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - renamed and added hcaptchastring
 */
@NonNullByDefault
public class MyBMWBridgeConfiguration {

    /**
     * Depending on the location the correct server needs to be called
     */
    private String region = Constants.EMPTY;

    /**
     * MyBMW App Username
     */
    private String userName = Constants.EMPTY;

    /**
     * MyBMW App Password
     */
    private String password = Constants.EMPTY;

    /**
     * Preferred Locale language
     */
    private String language = Constants.LANGUAGE_AUTODETECT;

    /**
     * the hCaptcha string
     */
    private String hcaptchatoken = Constants.EMPTY;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getHcaptchatoken() {
        return hcaptchatoken;
    }

    public void setHcaptchatoken(String hcaptchatoken) {
        this.hcaptchatoken = hcaptchatoken;
    }

    @Override
    public String toString() {
        return "MyBMWBridgeConfiguration [region=" + region + ", userName=" + userName + ", password=" + password
                + ", language=" + language + ", hcaptchatoken=" + hcaptchatoken + "]";
    }
}
