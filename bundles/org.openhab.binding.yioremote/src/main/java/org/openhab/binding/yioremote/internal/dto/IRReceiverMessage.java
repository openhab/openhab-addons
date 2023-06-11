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
package org.openhab.binding.yioremote.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * The {@link IRReceiverMessage} the IRReceiverMessage DTO
 *
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class IRReceiverMessage {
    private String type = "dock";
    private String command = "ir_receive_off";

    public void setOn() {
        command = "ir_receive_on";
    }

    public void setOff() {
        command = "ir_receive_off";
    }

    public JsonObject getIRreceiverMessageJsonObject() {
        JsonObject irReceiverMessage = new JsonObject();
        irReceiverMessage.addProperty("type", type);
        irReceiverMessage.addProperty("command", command);
        return irReceiverMessage;
    }

    public String getIRreceiverMessageString() {
        return getIRreceiverMessageJsonObject().toString();
    }
}
