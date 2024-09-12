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

    public JsonObject getIRcodeSendMessageJsonObject() {
        JsonObject irCodeSendMessage = new JsonObject();
        irCodeSendMessage.addProperty("type", type);
        irCodeSendMessage.addProperty("command", command);
        irCodeSendMessage.addProperty("code", ircode.getCode());
        irCodeSendMessage.addProperty("format", ircode.getFormat());
        return irCodeSendMessage;
    }

    public String getIRcodeSendMessageString() {
        return getIRcodeSendMessageJsonObject().toString();
    }
}
