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
package org.openhab.binding.intellicenter2.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public enum Command {

    // get values
    GET_PARAM_LIST("GetParamList"),
    // set values
    SET_PARAM_LIST("SetParamList"),
    GET_QUERY("GetQuery"),
    // responses for subscriptions
    NOTIFY_LIST("NotifyList"),
    // request a subscription
    REQUEST_PARAM_LIST("RequestParamList"),
    // unsubscribe
    RELEASE_PARAM_LIST("ReleaseParamList");

    private final String command;

    Command(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }
}
