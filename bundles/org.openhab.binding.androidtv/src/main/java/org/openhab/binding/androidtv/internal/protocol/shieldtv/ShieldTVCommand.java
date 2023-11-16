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
package org.openhab.binding.androidtv.internal.protocol.shieldtv;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * ShieldTVCommand represents a ShieldTV protocol command
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class ShieldTVCommand {
    private String command;

    public ShieldTVCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }

    public boolean isEmpty() {
        return command.isEmpty();
    }
}
