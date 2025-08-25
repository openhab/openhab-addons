/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal.type.capabilities;

import static org.openhab.binding.zwavejs.internal.BindingConstants.VIRTUAL_ROLLERSHUTTER_CHANNEL_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the capabilities of a roller shutter device, including its movement direction,
 * position, and associated channels for controlling the device.
 * <p>
 * This class provides methods to manage the movement direction (up or down),
 * track the cached position, and handle inversion logic for the roller shutter.
 * It also stores information about the associated channels for dimmer, up, and down controls.
 * Without any inversion, 100% is fully open (up position) and 0% is fully closed (down position).
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class RollerShutterCapability {
    private Logger logger = LoggerFactory.getLogger(RollerShutterCapability.class);
    public Integer endpoint;
    public String rollerShutterChannelId;
    public ChannelUID dimmerChannel;
    public ChannelUID upChannel;
    public ChannelUID downChannel;
    private int cachedPosition = 0;
    private boolean isMovingUp = false;
    private boolean isMovingDown = false;

    public boolean isMoving() {
        return isMovingUp || isMovingDown;
    }

    public boolean isMovingUp() {
        return isMovingUp;
    }

    public boolean isMovingDown() {
        return isMovingDown;
    }

    /**
     * Returns the cached position for testing purposes.
     */
    public int getCachedPosition() {
        return cachedPosition;
    }

    /**
     * Sets the direction of the roller shutter to up. Optionally apply inversion before calling this method
     * 
     * @param isMovingUp true if the roller shutter is moving up, false otherwise.
     *            If set to true, the down direction will automatically
     *            be set to false.
     */
    public void setDirectionUp(boolean isMovingUp) {
        logger.trace(
                "RollershutterCapability.setDirectionUp() (endpoint: {}, cachedPosition: {}, isMovingUp: {}, isMovingDown: {})",
                endpoint, cachedPosition, isMovingUp, this.isMovingDown);
        this.isMovingUp = isMovingUp;
        if (isMovingUp) {
            this.isMovingDown = false;
        }
    }

    /**
     * Sets the direction of the roller shutter to down. Optionally apply inversion before calling this method
     * 
     * @param isMovingDown true if the roller shutter is moving down, false otherwise.
     *            When set to true, the direction up flag will be automatically set to false.
     */
    public void setDirectionDown(boolean isMovingDown) {
        logger.trace("RollershutterCapability.isMovingDown() (endpoint: {}, cachedisMovingDown: {}, isMovingDown: {})",
                endpoint, this.isMovingDown, isMovingDown);
        this.isMovingDown = isMovingDown;
        if (isMovingDown) {
            this.isMovingUp = false;
        }
    }

    /**
     * Sets the direction of the roller shutter movement. Optionally apply inversion before calling this method.
     *
     * @param isMovingUp {@code true} if the roller shutter is moving up, {@code false} otherwise.
     * @param isMovingDown {@code true} if the roller shutter is moving down, {@code false} otherwise.
     */
    public void setDirection(boolean isMovingUp, boolean isMovingDown) {
        logger.trace(
                "RollershutterCapability.setDirection() (endpoint: {}, cachedPosition: {}, isMovingUp: {}, isMovingDown: {})",
                endpoint, cachedPosition, isMovingUp, isMovingDown);
        this.isMovingUp = isMovingUp;
        this.isMovingDown = isMovingDown;
    }

    /**
     * Sets the position of the roller shutter. Raw value, not inverted.
     * 
     * @param position
     * @param isUpDownInverted
     */
    public void setPosition(int position, boolean isUpDownInverted) {
        logger.trace(
                "RollershutterCapability.setPosition() (endpoint: {}, position: {}, isUpDownInverted: {}, cachedPosition: {})",
                endpoint, position, isUpDownInverted, cachedPosition);
        if (position == cachedPosition) {
            this.isMovingUp = false;
            this.isMovingDown = false;
            return;
        }
        this.isMovingUp = isUpDownInverted ? position > cachedPosition : position < cachedPosition;
        this.isMovingDown = isUpDownInverted ? position < cachedPosition : position > cachedPosition;
        this.cachedPosition = position;
    }

    public RollerShutterCapability(Integer endpoint, ChannelUID dimmerChannel, ChannelUID upChannel,
            ChannelUID downChannel) {
        this.endpoint = endpoint;
        this.rollerShutterChannelId = VIRTUAL_ROLLERSHUTTER_CHANNEL_ID + (endpoint == 0 ? "" : "-" + endpoint);
        this.dimmerChannel = dimmerChannel;
        this.upChannel = upChannel;
        this.downChannel = downChannel;
    }

    @Override
    public String toString() {
        return "RollerShutterCapability [dimmerChannel=" + dimmerChannel + ", upChannel=" + upChannel + ", downChannel="
                + downChannel + "]";
    }
}
