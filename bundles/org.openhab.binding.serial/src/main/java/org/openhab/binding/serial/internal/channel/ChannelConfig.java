/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.serial.internal.channel;

/**
 * Class describing the channel user configuration
 *
 * @author Mike Major - Initial contribution
 */
public class ChannelConfig {
    /**
     * Transform for received data
     */
    public String transform;

    /**
     * Transform for command
     */
    public String commandTransform;

    /**
     * Format string for command
     */
    public String commandFormat;

    /**
     * On value
     */
    public String on;

    /**
     * Off value
     */
    public String off;

    /**
     * Up value
     */
    public String up;

    /**
     * Down value
     */
    public String down;

    /**
     * Stop value
     */
    public String stop;

    /**
     * Increase value
     */
    public String increase;

    /**
     * Decrease value
     */
    public String decrease;
}
