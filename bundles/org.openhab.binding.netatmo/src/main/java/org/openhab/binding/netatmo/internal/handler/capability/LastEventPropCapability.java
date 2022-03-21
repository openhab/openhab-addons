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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.GROUP_LAST_EVENT;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.handler.NAAccountHandler;

/**
 * The {@link LastEventPropCapability} takes care of handling markup property of last event received
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
// TODO from UCDetector: Class "LastEventPropertyHelper" has 0 references
public class LastEventPropCapability extends Capability {
    private @Nullable ZonedDateTime maxEventTime;

    public LastEventPropCapability(NAAccountHandler handler) {
        super(handler);
    }

    public ZonedDateTime getMaxEvent() {
        ZonedDateTime eventTime = maxEventTime;
        if (eventTime == null) {
            String lastEvent = properties.get(GROUP_LAST_EVENT);
            eventTime = lastEvent != null ? ZonedDateTime.parse(lastEvent) : Instant.EPOCH.atZone(ZoneOffset.UTC);
            this.maxEventTime = eventTime;
        }
        return eventTime;
    }

    public void setMaxEvent(ZonedDateTime eventTime) {
        ZonedDateTime maxTime = eventTime.minusSeconds(1);
        this.maxEventTime = maxTime;
        thing.setProperty(GROUP_LAST_EVENT, maxTime.toString());
    }
}
