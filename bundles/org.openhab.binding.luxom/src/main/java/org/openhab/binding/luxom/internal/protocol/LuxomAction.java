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
package org.openhab.binding.luxom.internal.protocol;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * luxom action
 * 
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public enum LuxomAction {
    HEARTBEAT("*U", false),
    ACKNOWLEDGE("@1*V", false),
    TOGGLE("*T", true),
    PING("*P", true),
    MODULE_INFORMATION("*!", false),
    PASSWORD_REQUEST("@1*PW-", false),
    CLEAR_RESPONSE("@1*C", true),
    SET_RESPONSE("@1*S", true),
    DATA_RESPONSE("@1*A", true, true),
    DATA_BYTE_RESPONSE("@1*Z", false),
    DATA("*A", true, true),
    DATA_BYTE("*Z", false),
    SET("*S", true),
    CLEAR("*C", true),
    REQUEST_FOR_INFORMATION("*?", false),
    INVALID_ACTION("-INVALID-", false); // this is not part of the luxom api, it's for internal use.;

    private final String command;
    private final boolean hasAddress;
    private final boolean needsData;

    LuxomAction(String command, boolean hasAddress) {
        this(command, hasAddress, false);
    }

    LuxomAction(String command, boolean hasAddress, boolean needsData) {
        this.command = command;
        this.hasAddress = hasAddress;
        this.needsData = needsData;
    }

    public static LuxomAction of(String command) {
        return Objects.requireNonNull(Arrays.stream(LuxomAction.values()).filter(a -> a.getCommand().equals(command))
                .findFirst().orElse(INVALID_ACTION));
    }

    public String getCommand() {
        return command;
    }

    public boolean isHasAddress() {
        return hasAddress;
    }

    public boolean isNeedsData() {
        return needsData;
    }
}
