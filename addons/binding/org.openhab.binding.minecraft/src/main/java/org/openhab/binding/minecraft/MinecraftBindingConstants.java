/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MinecraftBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mattias Markehed
 */
public class MinecraftBindingConstants {

    public static final String BINDING_ID = "minecraft";

    public final static int SERVER_RECONNECT = 30; // Seconds to wait before reconnect

    public final static String PARAMETER_HOSTNAME = "hostname";
    public final static String PARAMETER_PORT = "port";

    public final static String TYPE_ID_SERVER = "server";
    public final static String TYPE_ID_PLAYER = "player";
    public final static String TYPE_ID_SIGN = "redstoneSign";

    public final static String CHANNEL_PLAYERS = "players";
    public final static String CHANNEL_MAX_PLAYERS = "maxPlayers";
    public final static String CHANNEL_ONLINE = "online";

    public final static String CHANNEL_PLAYER_ONLINE = "playerOnline";
    public final static String CHANNEL_PLAYER_LEVEL = "playerLevel";
    public final static String CHANNEL_PLAYER_LEVEL_PERCENTAGE = "playerExperiencePercentage";
    public final static String CHANNEL_PLAYER_TOTAL_EXPERIENCE = "playerTotalExperience";
    public final static String CHANNEL_PLAYER_HEALTH = "playerHealth";
    public final static String CHANNEL_PLAYER_WALK_SPEED = "playerWalkSpeed";
    public final static String CHANNEL_PLAYER_LOCATION = "playerLocation";
    public final static String CHANNEL_PLAYER_GAME_MODE = "playerGameMode";

    public final static String CHANNEL_SIGN_ACTIVE = "signActive";

    public final static String PARAMETER_PLAYER_NAME = "playerName";

    public final static String PARAMETER_SIGN_NAME = "signName";

    public final static ThingTypeUID THING_TYPE_SERVER = new ThingTypeUID(BINDING_ID, TYPE_ID_SERVER);
    public final static ThingTypeUID THING_TYPE_PLAYER = new ThingTypeUID(BINDING_ID, TYPE_ID_PLAYER);
    public final static ThingTypeUID THING_TYPE_SIGN = new ThingTypeUID(BINDING_ID, TYPE_ID_SIGN);
}
