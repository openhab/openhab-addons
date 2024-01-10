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
package org.openhab.binding.plugwise.internal.protocol.field;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The boundary switch action of a Sense when the value is below/above the boundary minimum/maximum.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public enum BoundaryAction {

    OFF_BELOW_ON_ABOVE(0, 1),
    ON_BELOW_OFF_ABOVE(1, 0);

    private final int lowerAction;
    private final int upperAction;

    BoundaryAction(int lowerAction, int upperAction) {
        this.lowerAction = lowerAction;
        this.upperAction = upperAction;
    }

    public int getLowerAction() {
        return lowerAction;
    }

    public int getUpperAction() {
        return upperAction;
    }
}
