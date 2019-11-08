/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nibeuplink.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.nibeuplink.internal.NibeUplinkBindingConstants;
import org.openhab.binding.nibeuplink.internal.model.ScaledChannel.ScaleFactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the custom channels which can be configured via config-file or paper-ui
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public final class CustomChannels extends AbstractChannels {
    private final Logger logger = LoggerFactory.getLogger(CustomChannels.class);

    public void registerCustomChannel(Channel customChannel) {
        ChannelTypeUID typeUID = customChannel.getChannelTypeUID();
        String type = typeUID == null ? null : typeUID.getId();
        type = type == null ? "null" : type;

        String label = customChannel.getLabel();
        label = label == null ? "no-name" : label;

        String id = customChannel.getUID().getId();

        logger.debug("Trying to add custom channel: {}, type: {}, label: {}", id, type, label);

        try {
            switch (type) {
                case NibeUplinkBindingConstants.CUSTOM_TYPE_UNSCALED:
                    this.addChannel(new NibeChannel(id, label, ChannelGroup.CUSTOM));
                    break;
                case NibeUplinkBindingConstants.CUSTOM_TYPE_DIV_10:
                    this.addChannel(new ScaledChannel(id, label, ChannelGroup.CUSTOM, ScaleFactor.DIV_10));
                    break;
                case NibeUplinkBindingConstants.CUSTOM_TYPE_DIV_100:
                    this.addChannel(new ScaledChannel(id, label, ChannelGroup.CUSTOM, ScaleFactor.DIV_100));
                    break;
                default:
                    logger.warn("'{}' is not a custom channel type, skipping this channel: {}", type,
                            customChannel.getUID().getId());
            }
        } catch (ValidationException ex) {
            logger.warn("{}", ex.getMessage());
        }
    }

    public void clearList() {
        this.channels.clear();
    }

    public static boolean isCustomChannel(Channel channel) {
        ChannelTypeUID typeUID = channel.getChannelTypeUID();
        String type = typeUID == null ? null : typeUID.getId();
        type = type == null ? "null" : type;

        switch (type) {
            case NibeUplinkBindingConstants.CUSTOM_TYPE_UNSCALED:
            case NibeUplinkBindingConstants.CUSTOM_TYPE_DIV_10:
            case NibeUplinkBindingConstants.CUSTOM_TYPE_DIV_100:
                return true;
            default:
                return false;
        }

    }

}
