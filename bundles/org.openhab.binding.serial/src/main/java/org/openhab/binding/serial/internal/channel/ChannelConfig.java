/*
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
package org.openhab.binding.serial.internal.channel;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class describing the channel user configuration
 *
 * @author Mike Major - Initial contribution
 * @author Roland Tapken - Added refreshValue and refreshInterval
 */
@NonNullByDefault
public class ChannelConfig {
    /**
     * Transform for received data
     */
    public @Nullable List<String> stateTransformation;

    /**
     * Transform for command
     */
    public @Nullable List<String> commandTransformation;

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

    /**
     * Command for refesh command
     */
    public @Nullable String refreshValue;

    /**
     * Automatic refresh interval.
     *
     * This value is only required if the peer has to send the “Refresh Value”
     * command regularly in order to return the current properties. It is not
     * required if the peer automatically forwards changed values to its
     * clients.
     */
    public int refreshInterval = 0;
}
