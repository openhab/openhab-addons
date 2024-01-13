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
package org.openhab.binding.loxone.internal.types;

/**
 * Types of security authentication/encryption methods.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public enum LxWsSecurityType {
    /**
     * Method will be determined base on Miniserver software version
     */
    AUTO,
    /**
     * Hash-based authentication with no command encryption
     */
    HASH,
    /**
     * Token-based authentication with AES-256 command encryption
     */
    TOKEN;

    /**
     * Encode security type based on index
     *
     * @param index
     *            0 for auto, 1 for hash, 2 for token
     * @return
     *         security type fo given index
     */
    public static LxWsSecurityType getType(int index) {
        switch (index) {
            case 0:
                return AUTO;
            case 1:
                return HASH;
            default:
            case 2:
                return TOKEN;
        }
    }
}
