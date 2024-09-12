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
package org.openhab.binding.onewire.internal.device;

import static org.openhab.binding.onewire.internal.OwBindingConstants.BINDING_ID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link OwChannelConfig} class defines a map entry
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwChannelConfig {
    private static final Pattern CONFIG_PATTERN = Pattern.compile("^(.+):(.+):(.*)$");

    public String channelId;
    public ChannelTypeUID channelTypeUID;
    public @Nullable String label;

    public OwChannelConfig(String channelId, ChannelTypeUID channelTypeUID, @Nullable String label) {
        this.channelId = channelId;
        this.channelTypeUID = channelTypeUID;
        this.label = label;
    }

    public OwChannelConfig(String channelId, ChannelTypeUID channelTypeUID) {
        this(channelId, channelTypeUID, null);
    }

    public static OwChannelConfig fromString(String configString) {
        Matcher matcher = CONFIG_PATTERN.matcher(configString);
        if (matcher.matches()) {
            if (matcher.group(3).trim().isEmpty()) {
                return new OwChannelConfig(matcher.group(1).trim(),
                        new ChannelTypeUID(BINDING_ID, matcher.group(2).trim()));
            } else {
                return new OwChannelConfig(matcher.group(1).trim(),
                        new ChannelTypeUID(BINDING_ID, matcher.group(2).trim()), matcher.group(3).trim());
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return channelId + "/" + channelTypeUID.getAsString() + "/" + label;
    }
}
