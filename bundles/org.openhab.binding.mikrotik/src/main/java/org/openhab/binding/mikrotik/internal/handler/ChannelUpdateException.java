/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mikrotik.internal.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;

public class ChannelUpdateException extends RuntimeException {
    private final ThingUID thingUID;
    private final ChannelUID channelID;
    private final Throwable innerException;

    public ChannelUpdateException(ThingUID thingUID, ChannelUID channelUID, Throwable innerEx) {
        super(innerEx);
        this.innerException = innerEx;
        this.thingUID = thingUID;
        this.channelID = channelUID;
    }

    @Override
    public String getMessage() {
        return String.format("%s @ %s/%s", super.getMessage(), thingUID, channelID);
    }

    public Throwable getInnerException() {
        return this.innerException;
    }
}
