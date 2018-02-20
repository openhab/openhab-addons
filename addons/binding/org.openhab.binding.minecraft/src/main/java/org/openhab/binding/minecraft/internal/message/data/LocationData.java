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
 * Holds location data for Minecraft objects.
 *
 * @author Mattias Markehed
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
