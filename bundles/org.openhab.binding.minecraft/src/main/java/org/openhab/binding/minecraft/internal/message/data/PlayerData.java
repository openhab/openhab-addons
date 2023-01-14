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
package org.openhab.binding.minecraft.internal.message.data;

/**
 * Object representing Minecraft player.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class PlayerData {

    protected String displayName;
    protected String name;
    protected int level;
    protected int totalExperience;
    protected float experience;
    protected double health;
    protected float walkSpeed;
    protected LocationData location;
    protected String gameMode;

    /**
     * Get the display name of player.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the name of player
     *
     * @return name of player.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the player level.
     *
     * @return level of player
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get the total experience of player.
     *
     * @return total experience
     */
    public int getTotalExperience() {
        return totalExperience;
    }

    /**
     * Get player experiance.
     *
     * @return experiance of player
     */
    public float getExperience() {
        return experience;
    }

    /**
     * Get health of player.
     *
     * @return player health
     */
    public double getHealth() {
        return health;
    }

    /**
     * Get the walk speed of player.
     *
     * @return walk speed of player
     */
    public float getWalkSpeed() {
        return walkSpeed;
    }

    /**
     * Get location of player.
     *
     * @return location of player
     */
    public LocationData getLocation() {
        return location;
    }

    /**
     * Get the players game mode.
     *
     * @return game mode
     */
    public String getGameMode() {
        return gameMode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PlayerData other = (PlayerData) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
