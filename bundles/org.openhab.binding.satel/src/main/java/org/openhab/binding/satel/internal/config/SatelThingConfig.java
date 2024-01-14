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
package org.openhab.binding.satel.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SatelThingConfig} contains common configuration values for Satel devices.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class SatelThingConfig {

    public static final String ID = "id";
    public static final String UP_ID = "upId";
    public static final String DOWN_ID = "downId";

    private int id;
    private int upId;
    private int downId;
    private boolean invertState;
    private boolean forceArming;
    private boolean commandOnly;
    private boolean wireless;

    /**
     * @return device identifier
     */
    public int getId() {
        return id;
    }

    /**
     * @return for a shutter: output number to control "up" direction
     */
    public int getUpId() {
        return upId;
    }

    /**
     * @return for a shutter: output number to control "down" direction
     */
    public int getDownId() {
        return downId;
    }

    /**
     * @return if <code>true</code>, device's state should be inverted
     */
    public boolean isStateInverted() {
        return invertState;
    }

    /**
     * @return if <code>true</code>, forces arming a partition
     */
    public boolean isForceArmingEnabled() {
        return forceArming;
    }

    /**
     * @return if <code>true</code> the thing should accept only commands, it does not update its state
     */
    public boolean isCommandOnly() {
        return commandOnly;
    }

    /**
     * @return if <code>true</code> the thing is a wireless device
     */
    public boolean isWireless() {
        return wireless;
    }
}
