/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lgthinq.internal;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.openhab.binding.lgthinq.internal.LGThinqBindingConstants.*;

/**
 * The {@link LGThinqConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nemer Daud - Initial contribution
 */
public class LGThinqConfiguration {
    /**
     * Sample configuration parameters. Replace with your own.
     */
    public String username;
    public String password;
    public String country;
    public String language;

    public Map commonHeader;
    /**
     *     The `access_token` and `session_id` are required for most normal,
     *     authenticated requests. They are not required, for example, to load
     *     the gateway server data or to start a session.
     */

    public LGThinqConfiguration() {
    }

    public LGThinqConfiguration(String username, String password, String country, String language) {
        this.username = username;
        this.password = password;
        this.country = country;
        this.language = language;
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
}
