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

/**
 * A class encapsulating the roller shutter capability of a roller shutter
 * endpoint
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class RollerShutterCapability {
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
     * Sets the direction of the roller shutter to up. Optionally apply inversion before calling this method
     * 
     * @param isMovingUp true if the roller shutter is moving up, false otherwise.
     *            If set to true, the down direction will automatically
     *            be set to false.
     */
    public void setDirectionUp(boolean isMovingUp) {
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
