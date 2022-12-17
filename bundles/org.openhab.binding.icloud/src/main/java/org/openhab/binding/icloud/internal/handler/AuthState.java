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
package org.openhab.binding.icloud.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enum to mark state during iCloud authentication.
 *
 * @author Simon Spielmann - Initial contribution
 *
 */
@NonNullByDefault
public enum AuthState {

    /**
     * Authentication was not tried yet.
     */
    INITIAL,

    /**
     * Entered credentials (apple id / password) are invalid.
     */
    USER_PW_INVALID,

    /**
     * Waiting for user to provide 2-FA code in thing configuration.
     */
    WAIT_FOR_CODE,

    /**
     * Sucessfully authenticated.
     */
    AUTHENTICATED

}
