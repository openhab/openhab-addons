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
package org.openhab.binding.iaqualink.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration properties for connecting to an iAqualink Account
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
public class IAqualinkConfiguration {

    /**
     * user to us when connecting to the account
     */
    public String userName = "";

    /**
     * password to us when connecting to the account
     */
    public String password = "";

    /**
     * Option serialId of the pool controller to connect to, only useful if you have more then one controller
     */
    public String serialId = "";

    /**
     * fixed API key provided by iAqualink clients (Android, IOS) , unknown if this will change in the future.
     */
    public String apiKey = "";

    /**
     * Rate we poll for new data
     */
    public int refresh = 30;
}
