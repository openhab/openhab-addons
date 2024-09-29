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
package org.openhab.binding.easee.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this enum represents the charger operation states as documented by https://developer.easee.cloud/docs/enumerations
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public enum ChargerOpState {
    OFFLINE(0),
    DISCONNECTED(1),
    WAITING(2),
    CHARGING(3),
    COMPLETED(4),
    ERROR(5),
    READY_TO_CHARGE(6),
    NOT_AUTHENTICATED(7),
    DEAUTHENTICATING(8),
    UNKNOWN_STATE(-1);

    private static final Logger LOGGER = LoggerFactory.getLogger(ChargerOpState.class);
    private final int code;

    private ChargerOpState(int code) {
        this.code = code;
    }

    public boolean isAuthenticatedState() {
        switch (this) {
            case WAITING:
            case CHARGING:
            case COMPLETED:
            case READY_TO_CHARGE:
                return true;
            default:
                return false;
        }
    }

    public static ChargerOpState fromCode(String code) {
        try {
            return ChargerOpState.fromCode(Integer.parseInt(code));
        } catch (NumberFormatException ex) {
            LOGGER.warn("caught exception while parsing ChargerOpState code: '{}' - exception: {}", code,
                    ex.getMessage());
            return UNKNOWN_STATE;
        }
    }

    public static ChargerOpState fromCode(int code) {
        for (ChargerOpState state : ChargerOpState.values()) {
            if (state.code == code) {
                return state;
            }
        }
        LOGGER.info("unknown ChargerOpState code: '{}'", code);
        return UNKNOWN_STATE;
    }
}
