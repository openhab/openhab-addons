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
package org.openhab.binding.luxom.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * luxom command
 * 
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public class LuxomCommand {
    private final LuxomAction action;
    private final @Nullable String address; // must for data byte commands be set after construction

    private @Nullable String data;

    public LuxomCommand(String command) {
        if (command.length() == 0) {
            action = LuxomAction.INVALID_ACTION;
            data = command;
            address = null;
            return;
        }
        String[] parts = command.split(",");

        if (parts.length == 1) {
            if (command.startsWith(LuxomAction.MODULE_INFORMATION.getCommand())) {
                action = LuxomAction.MODULE_INFORMATION;
                data = command.substring(2);
            } else if (command.equals(LuxomAction.PASSWORD_REQUEST.getCommand())) {
                action = LuxomAction.PASSWORD_REQUEST;
                data = null;
            } else if (command.equals(LuxomAction.ACKNOWLEDGE.getCommand())) {
                action = LuxomAction.ACKNOWLEDGE;
                data = null;
            } else {
                action = LuxomAction.INVALID_ACTION;
                data = command;
            }
            address = null;
        } else {
            action = LuxomAction.of(parts[0]);
            StringBuilder stringBuilder = new StringBuilder();
            if (action.isHasAddress()) {
                // first 0 not needed ?
                for (int i = 2; i < parts.length; i++) {
                    stringBuilder.append(parts[i]);
                    if (i != (parts.length - 1)) {
                        stringBuilder.append(",");
                    }
                }
                address = stringBuilder.toString();
                data = null;
            } else {
                for (int i = 1; i < parts.length; i++) {
                    stringBuilder.append(parts[i]);
                    if (i != (parts.length - 1)) {
                        stringBuilder.append(",");
                    }
                }
                address = null;
                data = stringBuilder.toString();
            }
        }
    }

    @Override
    public String toString() {
        return "LuxomCommand{" + "action=" + action + ", address='" + address + '\'' + ", data='" + data + '\'' + '}';
    }

    public LuxomAction getAction() {
        return action;
    }

    @Nullable
    public String getData() {
        return data;
    }

    @Nullable
    public String getAddress() {
        return address;
    }

    public void setData(@Nullable String data) {
        this.data = data;
    }
}
