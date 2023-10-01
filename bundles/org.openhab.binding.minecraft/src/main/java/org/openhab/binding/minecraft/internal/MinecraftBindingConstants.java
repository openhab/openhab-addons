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
package org.openhab.binding.minecraft.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MinecraftBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mattias Markehed - Initial contribution
 */
@NonNullByDefault
public class MinecraftBindingConstants {

    public static final String BINDING_ID = "minecraft";

    public static final int SERVER_RECONNECT = 30; // Seconds to wait before reconnect

    public static final String PARAMETER_HOSTNAME = "hostname";
    public static final String PARAMETER_PORT = "port";

    public static final String TYPE_ID_SERVER = "server";
    public static final String TYPE_ID_PLAYER = "player";
    public static final String TYPE_ID_SIGN = "redstoneSign";

    public static final String CHANNEL_PLAYERS = "players";
    public static final String CHANNEL_MAX_PLAYERS = "maxPlayers";
    public static final String CHANNEL_ONLINE = "online";

    public static final String CHANNEL_PLAYER_ONLINE = "playerOnline";
    public static final String CHANNEL_PLAYER_LEVEL = "playerLevel";
    public static final String CHANNEL_PLAYER_LEVEL_PERCENTAGE = "playerExperiencePercentage";
    public static final String CHANNEL_PLAYER_TOTAL_EXPERIENCE = "playerTotalExperience";
    public static final String CHANNEL_PLAYER_HEALTH = "playerHealth";
    public static final String CHANNEL_PLAYER_WALK_SPEED = "playerWalkSpeed";
    public static final String CHANNEL_PLAYER_LOCATION = "playerLocation";
    public static final String CHANNEL_PLAYER_GAME_MODE = "playerGameMode";

    public static final String CHANNEL_SIGN_ACTIVE = "signActive";

    public static final String PARAMETER_PLAYER_NAME = "playerName";

    public static final String PARAMETER_SIGN_NAME = "signName";

    public static final ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, TYPE_ID_SERVER);
    public static final ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, TYPE_ID_PLAYER);
    public static final ThingTypeUID THING_TYPE_SIGN = new ThingTypeUID(BINDING_ID, TYPE_ID_SIGN);
}
