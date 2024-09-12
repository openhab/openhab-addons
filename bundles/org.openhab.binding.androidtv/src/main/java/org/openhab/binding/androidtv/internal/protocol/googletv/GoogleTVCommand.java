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
package org.openhab.binding.androidtv.internal.protocol.googletv;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * GoogleTVCommand represents a GoogleTV protocol command
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class GoogleTVCommand {
    private String command;

    public GoogleTVCommand(String command) {
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
