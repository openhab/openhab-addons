/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.autelis.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration properties for connecting to an Autelis Controller
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
public class AutelisConfiguration {

    /**
     * Host of the Autelis controller
     */
    public String host = "";

    /**
     * port of the Autelis controller
     */
    public int port = 80;

    /**
     * user to us when connecting to the Autelis controller
     */
    public String user = "";

    /**
     * password to us when connecting to the Autelis controller
     */
    public String password = "";

    /**
     * Rate we poll for new data
     */
    public int refresh = 5;
}
