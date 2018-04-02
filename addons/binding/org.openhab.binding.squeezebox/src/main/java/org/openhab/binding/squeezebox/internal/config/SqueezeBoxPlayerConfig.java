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
 * Configuration for a player
 *
 * @author Dan Cunningham
 * @author Mark Hilbush - Convert sound notification volume from channel to config parameter
 *
 */
public class SqueezeBoxPlayerConfig {
    /**
     * MAC address of player
     */
    public String mac;

    /**
     * Number of seconds to wait to time out a notification
     */
    public int notificationTimeout;

    /**
     * Volume used for playing notifications
     */
    public Integer notificationVolume;
}
