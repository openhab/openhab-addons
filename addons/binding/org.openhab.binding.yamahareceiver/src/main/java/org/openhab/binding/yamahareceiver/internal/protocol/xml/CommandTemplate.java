/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        return new CommandTemplate(
                command.replace(oldToken, newToken),
                path.replace(oldToken, newToken));
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
