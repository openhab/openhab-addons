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
package org.openhab.binding.zwavejs.internal.config;

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
    public int cachedPosition = 0;
    public boolean isMovingUp = false;
    public boolean isMovingDown = false;

    public boolean isMoving() {
        return isMovingUp || isMovingDown;
    }

    public void setDirectionUp(boolean isMovingUp) {
        this.isMovingUp = isMovingUp;
        if (isMovingUp) {
            this.isMovingDown = false;
        }
    }

    public void setDirectionDown(boolean isMovingDown) {
        this.isMovingDown = isMovingDown;
        if (isMovingDown) {
            this.isMovingUp = false;
        }
    }

    public void setDirection(boolean isMovingUp, boolean isMovingDown) {
        this.isMovingUp = isMovingUp;
        this.isMovingDown = isMovingDown;
    }

    public void setPosition(int position) {
        if (position == cachedPosition) {
            return;
        }
        this.cachedPosition = position;
        this.isMovingUp = position < cachedPosition;
        this.isMovingDown = position > cachedPosition;
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
