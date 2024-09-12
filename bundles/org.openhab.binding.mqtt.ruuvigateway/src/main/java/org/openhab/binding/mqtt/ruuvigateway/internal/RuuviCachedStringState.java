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
package org.openhab.binding.mqtt.ruuvigateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.ChannelConfig;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;

/**
 * Simplified state cache for purposes of caching StringType values
 *
 * Unlike parent class {@link ChannelState}, this class by definition is not interacting with MQTT subscriptions nor
 * does it update any channels
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class RuuviCachedStringState extends ChannelState {

    /**
     * Construct cache for Strings
     *
     * @param channelUID associated channel UID
     *
     */
    public RuuviCachedStringState(ChannelUID channelUID) {
        super(new ChannelConfig(), channelUID, new TextValue(), null);
    }

    /**
     * Update cached state with given value
     *
     * @param value value. Specified as plain number with unit given in constructor
     */
    public void update(String value) {
        cachedValue.update(new StringType(value));
    }
}
