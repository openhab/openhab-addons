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
package org.openhab.binding.powermax.internal.state;

import static org.openhab.binding.powermax.internal.PowermaxBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.UnDefType;

/**
 * A class to store the state of a zone
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxZoneState extends PowermaxStateContainer {

    public BooleanValue tripped = new BooleanValue(this, TRIPPED, OpenClosedType.OPEN, OpenClosedType.CLOSED);
    public DateTimeValue lastTripped = new DateTimeValue(this, LAST_TRIP);
    public BooleanValue lowBattery = new BooleanValue(this, LOW_BATTERY);
    public BooleanValue bypassed = new BooleanValue(this, BYPASSED);
    public BooleanValue alarmed = new BooleanValue(this, ALARMED);
    public BooleanValue tamperAlarm = new BooleanValue(this, TAMPER_ALARM);
    public BooleanValue inactive = new BooleanValue(this, INACTIVE);
    public BooleanValue tampered = new BooleanValue(this, TAMPERED);
    public BooleanValue armed = new BooleanValue(this, ARMED);
    public StringValue lastMessage = new StringValue(this, ZONE_LAST_MESSAGE);
    public DateTimeValue lastMessageTime = new DateTimeValue(this, ZONE_LAST_MESSAGE_TIME);

    public DynamicValue<Boolean> locked = new DynamicValue<>(this, LOCKED, () -> armed.getValue(), () -> {
        Boolean isArmed = armed.getValue();
        if (isArmed == null) {
            return UnDefType.NULL;
        }
        return isArmed ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
    });

    public PowermaxZoneState(TimeZoneProvider timeZoneProvider) {
        super(timeZoneProvider);
    }

    public boolean isLastTripBeforeTime(long refTime) {
        Long lastTrippedValue = lastTripped.getValue();
        return Boolean.TRUE.equals(tripped.getValue()) && (lastTrippedValue != null) && (lastTrippedValue < refTime);
    }
}
