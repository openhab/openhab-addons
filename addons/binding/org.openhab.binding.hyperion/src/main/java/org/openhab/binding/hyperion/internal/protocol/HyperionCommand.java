/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HyperionCommand} is a abstract class for sending commands
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
