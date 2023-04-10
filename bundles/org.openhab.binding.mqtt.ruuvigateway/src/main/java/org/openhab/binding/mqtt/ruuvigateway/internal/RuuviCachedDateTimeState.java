/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.ruuvigateway.internal;

import java.time.Instant;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.ChannelConfig;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.values.DateTimeValue;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.ChannelUID;

/**
 * Simplified state cache for purposes of caching DateTime values
 *
 * Unlike parent class {@link ChannelState}, this class by definition is not interacting with MQTT subscriptions nor
 * does it update any channels
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class RuuviCachedDateTimeState extends ChannelState {

    private static final ZoneId UTC = ZoneId.of("UTC");

    /**
     * Construct cache for DateTime values
     *
     * @param channelUID associated channel UID
     *
     */
    public RuuviCachedDateTimeState(ChannelUID channelUID) {
        super(new ChannelConfig(), channelUID, new DateTimeValue(), null);
    }

    /**
     * Update cached state with given value
     *
     * @param value instant representing value
     */
    public void update(Instant value) {
        cachedValue.update(new DateTimeType(value.atZone(UTC)));
    }
}
