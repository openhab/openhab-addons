/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.yioremote.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link IRCodeSendMessage} the IRCodeSendMessage DTO
 *
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class IRCodeSendMessage {
    private String type = "dock";
    private String command = "ir_send";

    private IRCode ircode = new IRCode();

    public IRCodeSendMessage(IRCode ircode) {
        this.ircode = ircode;
    }

    public String getType() {
        return type;
    }

    public String getCommand() {
        return command;
    }

    public JsonObject getircodesendmessagejsonobject() {
        JsonObject ircodesendmessagejsonobject = new JsonObject();
        ircodesendmessagejsonobject.addProperty("type", type);
        ircodesendmessagejsonobject.addProperty("command", command);
        ircodesendmessagejsonobject.addProperty("code", ircode.getcode());
        ircodesendmessagejsonobject.addProperty("format", ircode.getformat());
        return ircodesendmessagejsonobject;
    }

    public String getircodesendmessagestring() {
        JsonObject ircodesendmessagejsonobject = new JsonObject();
        ircodesendmessagejsonobject.addProperty("type", type);
        ircodesendmessagejsonobject.addProperty("command", command);
        ircodesendmessagejsonobject.addProperty("code", ircode.getcode());
        ircodesendmessagejsonobject.addProperty("format", ircode.getformat());
        return ircodesendmessagejsonobject.toString();
    }
}

