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
package org.openhab.binding.minecraft.internal.config;

/**
 * Configuration settings for a {@link org.openhab.binding.minecraft.internal.handler.MinecraftPlayerHandler}.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class PlayerConfig {
    private String playerName = "";

    /**
     * Get name of player.
     *
     * @return player name
     */
    public String getName() {
        return playerName;
    }

    /**
     * Set the player name.
     *
     * @param player name
     */
    public void setName(String name) {
        this.playerName = name;
    }
}
