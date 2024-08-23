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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

/**
 * Template for XML commands
 *
 * @author Tomasz Maruszak - Initial contribution
 */
class CommandTemplate {

    private final String command;
    private final String path;

    public CommandTemplate(String command, String path) {
        this.command = command;
        this.path = path;
    }

    public CommandTemplate(String command) {
        this(command, "");
    }

    public CommandTemplate replace(String oldToken, String newToken) {
        return new CommandTemplate(command.replace(oldToken, newToken), path.replace(oldToken, newToken));
    }

    public String apply(Object... args) {
        return String.format(command, args);
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return command;
    }
}
