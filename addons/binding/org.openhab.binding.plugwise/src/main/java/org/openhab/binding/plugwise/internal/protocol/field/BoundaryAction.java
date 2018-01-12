/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.protocol.field;

/**
 * The boundary switch action of a Sense when the value is below/above the boundary minimum/maximum.
 *
 * @author Wouter Born - Initial contribution
 */
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
