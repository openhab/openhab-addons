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
package org.openhab.binding.onewire.internal.owserver;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link OwfsDirectChannelConfig} defines config for owfsdirect channels
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwfsDirectChannelConfig {
    public String path = "";
    public BigDecimal refresh = new BigDecimal(300);

    public long lastRefresh = 0;
    public int refreshCycle = 300;

    public @Nullable ChannelUID channelUID;
    public String acceptedItemType = "";

    public boolean initialize(ChannelUID channelUID, @Nullable String acceptedItemType) {
        this.channelUID = channelUID;
        this.acceptedItemType = acceptedItemType != null ? acceptedItemType : "";
        refreshCycle = refresh.intValue() * 1000;

        return !path.isEmpty();
    }
}
