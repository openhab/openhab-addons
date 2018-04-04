/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.squeezebox.internal.config;

/**
 * Configuration of a server.
 *
 * @author Dan Cunningham
 * @author Mark Hilbush Added user ID and password
 *
 */
public class SqueezeBoxServerConfig {
    /**
     * Server ip address
     */
    public String ipAddress;
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
    public String language;
    /*
     * User ID (when authentication enabled in LMS)
     */
    public String userId;
    /*
     * User ID (when authentication enabled in LMS)
     */
    public String password;
}
