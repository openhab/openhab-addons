/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.minecraft.internal.message.data;

/**
 * Object representing Minecraft server.
 *
 * @author Mattias Markehed - Initial contribution
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
