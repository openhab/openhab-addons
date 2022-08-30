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
package org.openhab.binding.hdpowerview.internal.api.responses;

import java.time.DayOfWeek;
import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract class for scheduled event as returned by an HD PowerView hub.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public abstract class ScheduledEvent {
    // fields common to Generation 1/2 and 3 hubs
    public int id;
    public int type;
    public boolean enabled;
    public int hour;
    public int minute;
    public int sceneId;

    public abstract EnumSet<DayOfWeek> getDays();

    public abstract int getEventType();

    public abstract int version();
}
