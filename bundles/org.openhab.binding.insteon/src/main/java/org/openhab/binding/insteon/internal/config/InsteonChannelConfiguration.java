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
package org.openhab.binding.insteon.internal.config;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.core.thing.ChannelUID;

/**
 *
 * This file contains config information needed for each channel
 *
 * @author Rob Nielsen - Initial contribution
 */
@NonNullByDefault
public class InsteonChannelConfiguration {

    private final ChannelUID channelUID;
    private final String channelName;
    private final InsteonAddress address;
    private final String feature;
    private final String productKey;
    private final Map<String, String> parameters;

    public InsteonChannelConfiguration(ChannelUID channelUID, String feature, InsteonAddress address, String productKey,
            Map<String, String> parameters) {
        this.channelUID = channelUID;
        this.feature = feature;
        this.address = address;
        this.productKey = productKey;
        this.parameters = parameters;

        this.channelName = channelUID.getAsString();
    }

    public ChannelUID getChannelUID() {
        return channelUID;
    }

    public String getChannelName() {
        return channelName;
    }

    public InsteonAddress getAddress() {
        return address;
    }

    public String getFeature() {
        return feature;
    }

    public String getProductKey() {
        return productKey;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
