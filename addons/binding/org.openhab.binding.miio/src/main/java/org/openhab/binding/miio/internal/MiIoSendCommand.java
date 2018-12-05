/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Commands to be send
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoSendCommand {

    private final int id;
    private final MiIoCommand command;
    private final String commandString;
    private JsonObject response;

    public void setResponse(JsonObject response) {
        this.response = response;
    }

    public MiIoSendCommand(int id, MiIoCommand command, String commandString) {
        this.id = id;
        this.command = command;
        this.commandString = commandString;
    }

    public int getId() {
        return id;
    }

    public MiIoCommand getCommand() {
        return command;
    }

    public String getCommandString() {
        return commandString;
    }

    public JsonObject getResponse() {
        return response;
    }

    public boolean isError() {
        return response.get("error") != null;
    }

    public JsonElement getResult() {
        return response.get("result");
    }
}
