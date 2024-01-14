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
package org.openhab.binding.mpd.internal.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class for encapsulating an MPD command
 *
 * @author Stefan Röllin - Initial contribution
 */

@NonNullByDefault
public class MPDCommand {

    private final String command;
    private final List<String> parameters = new ArrayList<>();

    /**
     * Create an MPD command without parameters
     *
     * @param command the command to send
     */
    public MPDCommand(String command) {
        this.command = command;
    }

    /**
     * Create an MPD command with one Integer parameter
     *
     * @param command the command to send
     * @param value parameter of the command
     */
    public MPDCommand(String command, Integer value) {
        this.command = command;
        parameters.add(Integer.toString(value));
    }

    /**
     * Create an MPD command with parameters
     *
     * @param command the command to send
     * @param parameters the parameters of the command to send
     */
    public MPDCommand(String command, String... parameters) {
        this.command = command;
        Collections.addAll(this.parameters, Arrays.copyOf(parameters, parameters.length));
    }

    /**
     * Returns the command.
     *
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the command as one line, including the parameters
     *
     * @return the command and parameters
     */
    public String asLine() {
        StringBuilder builder = new StringBuilder(command);

        for (String param : parameters) {
            builder.append(" ");
            builder.append("\"");
            builder.append(param.replace("\"", "\\\\\"").replace("'", "\\\\'"));
            builder.append("\"");
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(command);

        for (String param : parameters) {

            builder.append(" ");
            builder.append("\"");
            if ("password".equals(command)) {
                builder.append(param.replaceAll(".", "."));
            } else {
                builder.append(param.replace("\"", "\\\\\"").replace("'", "\\\\'"));
            }
            builder.append("\"");
        }

        return builder.toString();
    }
}
