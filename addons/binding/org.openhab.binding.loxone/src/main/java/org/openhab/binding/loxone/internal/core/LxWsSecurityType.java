/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

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
     * security type fo given index
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
