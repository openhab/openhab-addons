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
package org.openhab.binding.hyperion.internal.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HyperionCommand} is an abstract class for sending commands
 * to the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public abstract class HyperionCommand {

    @SerializedName("command")
    private String command;

    public HyperionCommand(String command) {
        setCommand(command);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
