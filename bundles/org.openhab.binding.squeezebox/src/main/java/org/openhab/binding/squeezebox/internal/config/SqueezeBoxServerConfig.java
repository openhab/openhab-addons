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
package org.openhab.binding.squeezebox.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration of a server.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Mark Hilbush - Added user ID and password
 *
 */
@NonNullByDefault
public class SqueezeBoxServerConfig {
    /**
     * Server ip address
     */
    public String ipAddress = "";
    /**
     * Server web port for REST calls
     */
    public int webport;
    /**
     * Server cli port
     */
    public int cliport;
    /**
     * Language for TTS
     */
    public String language = "";
    /*
     * User ID (when authentication enabled in LMS)
     */
    public String userId = "";
    /*
     * User ID (when authentication enabled in LMS)
     */
    public String password = "";
}
