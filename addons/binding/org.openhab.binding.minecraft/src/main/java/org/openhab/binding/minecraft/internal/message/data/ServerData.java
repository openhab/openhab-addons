/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.message.data;

/**
 * Object representing Minecraft server.
 *
 * @author Mattias Markehed
 */
public class ServerData {

    private int maxPlayers;
    private int players;

    /**
     * Get max number of players.
     *
     * @return max number of players.
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Get the number of players.
     *
     * @return number of players.
     */
    public int getPlayers() {
        return players;
    }
}
