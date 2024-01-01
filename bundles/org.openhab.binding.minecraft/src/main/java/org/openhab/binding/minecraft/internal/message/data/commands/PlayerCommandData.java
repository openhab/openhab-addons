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
package org.openhab.binding.minecraft.internal.message.data.commands;

/**
 * Object representing Minecraft server.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class PlayerCommandData {

    public static final String COMMAND_PLAYER_HEALTH = "PLAYER_HEALTH";
    public static final String COMMAND_PLAYER_LEVEL = "PLAYER_LEVEL";
    public static final String COMMAND_PLAYER_WALK_SPEED = "PLAYER_WALK_SPEED";
    public static final String COMMAND_PLAYER_GAME_MODE = "PLAYER_GAME_MODE";
    public static final String COMMAND_PLAYER_LOCATION = "PLAYER_LOCATION";

    private String type;
    private String playerName;
    private String value;

    public PlayerCommandData() {
    }

    public PlayerCommandData(String type, String playerName, String value) {
        this.type = type;
        this.playerName = playerName;
        this.value = value;
    }

    /**
     * Get the type of command.
     *
     * @return the type of command.
     */
    public String getType() {
        return type;
    }

    /**
     * The name of the player that the command targets.
     *
     * @return name of player
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * The command value sent.
     *
     * @return command value.
     */
    public String getValue() {
        return value;
    }
}
