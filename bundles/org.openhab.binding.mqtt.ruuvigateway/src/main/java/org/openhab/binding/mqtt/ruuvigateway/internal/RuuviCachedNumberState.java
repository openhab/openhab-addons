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

import java.util.Optional;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.ChannelConfig;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;

/**
 * Simplified state cache for purposes of caching QuantityType and DecimalType values
 *
 * Unlike parent class {@link ChannelState}, this class by definition is not interacting with MQTT subscriptions nor
 * does it update any channels
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class RuuviCachedNumberState<T extends Quantity<T>> extends ChannelState {

    private final Optional<Unit<T>> unit;

    /**
     * Construct cache for numbers with unit
     *
     * @param channelUID associated channel UID
     * @param unit unit associated with updated numbers
     *
     */
    public RuuviCachedNumberState(ChannelUID channelUID, Unit<T> unit) {
        super(new ChannelConfig(), channelUID, new NumberValue(null, null, null, unit), null);
        this.unit = Optional.of(unit);
    }

    /**
     * Construct cache for numbers without unit
     *
     * @param channelUID associated channeld UID
     */
    public RuuviCachedNumberState(ChannelUID channelUID) {
        super(new ChannelConfig(), channelUID, new NumberValue(null, null, null, null), null);
        this.unit = Optional.empty();
    }

    /**
     * Update cached state with given value
     *
     * @param value value. Specified as plain number with unit given in constructor
     */
    public void update(Number value) {
        unit.ifPresentOrElse(unit -> cachedValue.update(new QuantityType<>(value, unit)),
                () -> cachedValue.update(new DecimalType(value)));
    }

    /**
     * Get associated unit with this cache
     *
     * @return unit associated with this (if applicable)
     */
    public Optional<Unit<T>> getUnit() {
        return unit;
    }
}
