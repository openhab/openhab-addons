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
package org.openhab.binding.lgthinq.internal;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LGThinqConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqConfiguration {
    /**
     * Sample configuration parameters. Replace with your own.
     */
    public String username = "";
    public String password = "";
    public String country = "";
    public String language = "";
    public Integer poolingIntervalSec = 0;

    public LGThinqConfiguration() {
    }

    public LGThinqConfiguration(String username, String password, String country, String language,
            Integer poolingIntervalSec) {
        this.username = username;
        this.password = password;
        this.country = country;
        this.language = language;
        this.poolingIntervalSec = poolingIntervalSec;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCountry() {
        return country;
    }

    public String getLanguage() {
        return language;
    }

    public Integer getPoolingIntervalSec() {
        return poolingIntervalSec;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setPoolingIntervalSec(Integer poolingIntervalSec) {
        this.poolingIntervalSec = poolingIntervalSec;
    }
}
