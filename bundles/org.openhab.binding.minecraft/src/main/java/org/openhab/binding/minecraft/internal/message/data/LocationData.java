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
package org.openhab.binding.minecraft.internal.message.data;

/**
 * Holds location data for Minecraft objects.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class LocationData {
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    /**
     * Get x position.
     *
     * @return x position
     */
    public double getX() {
        return x;
    }

    /**
     * Get y position.
     *
     * @return y position
     */
    public double getY() {
        return y;
    }

    /**
     * Get z position.
     *
     * @return z position
     */
    public double getZ() {
        return z;
    }

    /**
     * Get pitch of object
     *
     * @return pitch of object.
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Get yaw of object
     *
     * @return yaw of object
     */
    public float getYaw() {
        return yaw;
    }
}
