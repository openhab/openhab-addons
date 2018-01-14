/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.config;

/**
 * The {@link SatelThingConfig} contains common configuration values for Satel devices.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
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

}
