/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
