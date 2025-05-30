/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;

/**
 * Allows a Home Assistant component to check if a channel is linked (and thus worth subscribing to)
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public interface HomeAssistantChannelLinkageChecker {
    /**
     * Returns whether at least one item is linked for the given channel ID.
     *
     * @param channelId channel ID (must not be null)
     * @return true if at least one item is linked, false otherwise
     */
    boolean isChannelLinked(ChannelUID channelUID);
}
