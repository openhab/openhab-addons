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
package org.openhab.binding.minecraft.internal.message;

import com.google.gson.JsonElement;

/**
 * Message used for communicating with Minecraft server.
 * Used both for sending and receiving messages.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class OHMessage {

    public static final int MESSAGE_TYPE_PLAYERS = 1;
    public static final int MESSAGE_TYPE_SERVERS = 2;
    public static final int MESSAGE_TYPE_SIGNS = 4;
    public static final int MESSAGE_TYPE_PLAYER_COMMANDS = 3;
    public static final int MESSAGE_TYPE_SIGN_COMMANDS = 5;

    private int messageType;
    private JsonElement message;

    /**
     * Creates a message of type.
     *
     * @param messageType the message type.
     * @param message message data.
     */
    public OHMessage(int messageType, JsonElement message) {
        this.messageType = messageType;
        this.message = message;
    }

    /**
     * Get the type of message
     *
     * @return type of message
     */
    public int getMessageType() {
        return messageType;
    }

    /**
     * Set message type.
     *
     * @param messageType the type of message
     */
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    /**
     * Get messsage data.
     *
     * @return
     */
    public JsonElement getMessage() {
        return message;
    }

    /**
     * Set the message to send.
     *
     * @param message
     */
    public void setMessage(JsonElement message) {
        this.message = message;
    }
}
