/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link ChannelUpdateException} is used to bubble up channel update errors which are mainly
 * happens during data conversion. But those errors should not bring bridge offline and break normal
 * operation.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class ChannelUpdateException extends RuntimeException {
    static final long serialVersionUID = 1L;

    private final ThingUID thingUID;
    private final ChannelUID channelID;

    public ChannelUpdateException(ThingUID thingUID, ChannelUID channelUID, Throwable cause) {
        super(cause);
        this.thingUID = thingUID;
        this.channelID = channelUID;
    }

    @Override
    public @Nullable String getMessage() {
        return String.format("%s @ %s/%s", super.getMessage(), thingUID, channelID);
    }
}
