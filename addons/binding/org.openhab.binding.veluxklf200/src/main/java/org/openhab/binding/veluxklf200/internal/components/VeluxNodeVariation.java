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
 * Indicates a node variation.
 *
 * @author MFK - Initial Contribution
 */
public enum VeluxNodeVariation {

    /** The not set. */
    NOT_SET(0),

    /** The tophung. */
    TOPHUNG(1),

    /** The kip. */
    KIP(2),

    /** The flat roof. */
    FLAT_ROOF(3),

    /** The sky light. */
    SKY_LIGHT(4),

    /** The unknown. */
    UNKNOWN(-1);

    /** The variation code. */
    private int variationCode;

    /**
     * Instantiates a new velux node variation.
     *
     * @param code
     *                 the code
     */
    private VeluxNodeVariation(int code) {
        this.variationCode = code;
    }

    /**
     * Gets the variation code.
     *
     * @return the variation code
     */
    public int getVariationCode() {
        return this.variationCode;
    }

    /**
     * Creates the.
     *
     * @param c
     *              the c
     * @return the velux node variation
     */
    public static VeluxNodeVariation create(int c) {
        switch (c) {
            case 0:
                return NOT_SET;
            case 1:
                return TOPHUNG;
            case 2:
                return KIP;
            case 3:
                return FLAT_ROOF;
            case 4:
                return SKY_LIGHT;
            default:
                return UNKNOWN;

        }
    }
}
