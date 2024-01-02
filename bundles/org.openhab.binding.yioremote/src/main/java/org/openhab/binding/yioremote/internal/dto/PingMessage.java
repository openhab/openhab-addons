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
package org.openhab.binding.yioremote.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link PingMessage} the AuthenticationMessage DTO
 *
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class PingMessage {
    private String type = "dock";
    private String command = "ping";

    public String getType() {
        return type;
    }

    public String getcommand() {
        return command;
    }

    public void setToken(String command) {
        this.command = command;
    }

    public JsonObject getPingMessageJsonObject() {
        JsonObject pingMessage = new JsonObject();
        pingMessage.addProperty("type", type);
        pingMessage.addProperty("command", command);
        return pingMessage;
    }

    public String getPingMessageString() {
        return getPingMessageJsonObject().toString();
    }
}
