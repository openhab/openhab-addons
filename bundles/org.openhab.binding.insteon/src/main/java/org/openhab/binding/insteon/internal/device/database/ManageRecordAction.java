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
package org.openhab.binding.insteon.internal.device.database;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ManageRecordAction} represents an Insteon manage all-link record action
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum ManageRecordAction {
    FIND_FIRST(0x00),
    FIND_NEXT(0x01),
    MODIFY_OR_ADD(0x20),
    MODIFY_CONTROLLER_OR_ADD(0x40),
    MODIFY_RESPONDER_OR_ADD(0x41),
    DELETE(0x80),
    UNKNOWN(0xFF);

    private static final Map<Integer, ManageRecordAction> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(action -> action.code, Function.identity()));

    private final int code;

    private ManageRecordAction(int code) {
        this.code = code;
    }

    public int getControlCode() {
        return code;
    }

    /**
     * Factory method for getting a ManageRecordAction from a control code
     *
     * @param code the control code
     * @return the manage record action
     */
    public static ManageRecordAction valueOf(int code) {
        return CODE_MAP.getOrDefault(code, ManageRecordAction.UNKNOWN);
    }
}
