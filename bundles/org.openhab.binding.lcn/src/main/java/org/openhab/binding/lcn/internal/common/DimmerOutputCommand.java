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
package org.openhab.binding.lcn.internal.common;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PercentType;

/**
 * Holds the information to control dimmer outputs of an LCN module. Used when the user configured an "output" profile.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class DimmerOutputCommand extends PercentType {
    private static final long serialVersionUID = 8147502412107723798L;
    private final boolean controlAllOutputs;
    private final boolean controlOutputs12;
    private final int rampMs;

    public DimmerOutputCommand(BigDecimal value, boolean controlAllOutputs, boolean controlOutputs12, int rampMs) {
        super(value);
        this.controlAllOutputs = controlAllOutputs;
        this.controlOutputs12 = controlOutputs12;
        this.rampMs = rampMs;
    }

    /**
     * Gets the ramp.
     *
     * @return ramp in milliseconds
     */
    public int getRampMs() {
        return rampMs;
    }

    /**
     * Returns if all dimmer outputs shall be controlled.
     *
     * @return true, if all dimmer outputs shall be controlled
     */
    public boolean isControlAllOutputs() {
        return controlAllOutputs;
    }

    /**
     * Returns if dimmer outputs 1+2 shall be controlled.
     *
     * @return true, if dimmer outputs 1+2 shall be controlled
     */
    public boolean isControlOutputs12() {
        return controlOutputs12;
    }
}
