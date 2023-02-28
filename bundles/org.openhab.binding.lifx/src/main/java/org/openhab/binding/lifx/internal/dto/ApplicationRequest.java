/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal.dto;

/**
 * @author Wouter Born - Initial contribution
 */
public enum ApplicationRequest {

    /**
     * Don't apply the requested changes until a message with APPLY or APPLY_ONLY is sent.
     */
    NO_APPLY(0x00),

    /**
     * Apply the changes immediately and apply any pending changes.
     */
    APPLY(0x01),

    /**
     * Ignore the requested changes in this message and only apply pending changes.
     */
    APPLY_ONLY(0x02);

    private final int value;

    private ApplicationRequest(int value) {
        this.value = value;
    }

    /**
     * Gets the integer value of this application request.
     *
     * @return the integer value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the {@link ApplicationRequest} for the given integer value.
     *
     * @param value the integer value
     * @return the {@link ApplicationRequest} or <code>null</code>, if no {@link ApplicationRequest} exists for the
     *         given value
     */
    public static ApplicationRequest fromValue(int value) {
        for (ApplicationRequest ar : values()) {
            if (ar.getValue() == value) {
                return ar;
            }
        }

        return null;
    }
}
