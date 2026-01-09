/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;

/**
 * Simplified state cache for purposes of caching OnOffType values
 *
 * Unlike parent class {@link ChannelState}, this class by definition is not interacting with MQTT subscriptions nor
 * does it update any channels
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class RuuviCachedSwitchState extends ChannelState {

    /**
     * Construct cache for Switch (OnOff) values
     *
     * @param channelUID associated channel UID
     *
     */
    public RuuviCachedSwitchState(ChannelUID channelUID) {
        super(new ChannelConfig(), channelUID, new OnOffValue(), null);
    }

    /**
     * Update cached state with given value
     *
     * @param value OnOffType value
     */
    public void update(OnOffType value) {
        cachedValue.update(value);
    }
}
