/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.components;

/**
 * Indicates the interim state of a velux command.
 *
 * @author MFK - Initial Contribution
 */
public enum VeluxState {

    /** The non executing. */
    NON_EXECUTING(0),

    /** The error executing. */
    ERROR_EXECUTING(1),

    /** The not used. */
    NOT_USED(2),

    /** The awaiting power. */
    AWAITING_POWER(3),

    /** The executing. */
    EXECUTING(4),

    /** The done. */
    DONE(5),

    /** The unknown. */
    UNKNOWN(255);

    /** The state code. */
    private int stateCode;

    /**
     * Instantiates a new velux state.
     *
     * @param code
     *                 the code
     */
    private VeluxState(int code) {
        this.stateCode = code;
    }

    /**
     * Gets the state code.
     *
     * @return the state code
     */
    public int getStateCode() {
        return this.stateCode;
    }

    /**
     * Creates the.
     *
     * @param code
     *                 the code
     * @return the velux state
     */
    public static VeluxState create(int code) {
        switch (code) {
            case 0:
                return NON_EXECUTING;
            case 1:
                return ERROR_EXECUTING;
            case 2:
                return NOT_USED;
            case 3:
                return AWAITING_POWER;
            case 4:
                return EXECUTING;
            case 5:
                return DONE;
            default:
                return UNKNOWN;
        }
    }
}
