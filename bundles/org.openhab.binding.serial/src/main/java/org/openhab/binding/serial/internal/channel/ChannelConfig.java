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
package org.openhab.binding.serial.internal.channel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class describing the channel user configuration
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class ChannelConfig {
    /**
     * Transform for received data
     */
    public @Nullable String stateTransformation;

    /**
     * Transform for command
     */
    public @Nullable String commandTransformation;

    /**
     * Format string for command
     */
    public @Nullable String commandFormat;

    /**
     * On value
     */
    public @Nullable String onValue;

    /**
     * Off value
     */
    public @Nullable String offValue;

    /**
     * Up value
     */
    public @Nullable String upValue;

    /**
     * Down value
     */
    public @Nullable String downValue;

    /**
     * Stop value
     */
    public @Nullable String stopValue;

    /**
     * Increase value
     */
    public @Nullable String increaseValue;

    /**
     * Decrease value
     */
    public @Nullable String decreaseValue;
}
