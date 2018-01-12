/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.message;

import com.google.gson.JsonElement;

/**
 * Message used for communicating with Minecraft server.
 * Used both for sending and receiving messages.
 *
 * @author Mattias Markehed
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
