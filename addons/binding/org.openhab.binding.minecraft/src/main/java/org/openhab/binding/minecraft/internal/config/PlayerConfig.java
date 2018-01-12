/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.config;

/**
 * Configuration settings for a {@link org.openhab.binding.minecraft.handler.MinecraftPlayerHandler}.
 *
 * @author Mattias Markehed
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
