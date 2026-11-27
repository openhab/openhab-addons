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
package org.openhab.binding.lgthinq.internal.handler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateOption;

/**
 * Helper for building and registering {@link StateOption} lists on a channel.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public final class StateDescriptionBuilder {

    private StateDescriptionBuilder() {
    }

    /**
     * Converts a {@code Map<String, V>} to {@link StateOption}s and registers them on a channel.
     *
     * @param provider the state description provider
     * @param channelUID target channel
     * @param mapping raw value map (key = option value, map-value fed to labelFn)
     * @param labelFn produces the display label for each map entry
     */
    public static <V> void setStateOptions(LGThinQStateDescriptionProvider provider, ChannelUID channelUID,
            Map<String, V> mapping, Function<Map.Entry<String, V>, String> labelFn) {
        List<StateOption> options = mapping.entrySet().stream().map(e -> new StateOption(e.getKey(), labelFn.apply(e)))
                .toList();
        provider.setStateOptions(channelUID, options);
    }

    /**
     * Convenience overload for maps where both key and value are Strings and the value is used directly as label.
     */
    public static void setStateOptions(LGThinQStateDescriptionProvider provider, ChannelUID channelUID,
            Map<String, String> mapping) {
        setStateOptions(provider, channelUID, mapping, Map.Entry::getValue);
    }

    /**
     * Convenience overload using a fallback map for i18n labels: if the value is not found in {@code i18n}, the raw
     * value is used as label.
     */
    public static void setStateOptions(LGThinQStateDescriptionProvider provider, ChannelUID channelUID,
            Map<String, String> mapping, Map<String, String> i18n) {
        setStateOptions(provider, channelUID, mapping, e -> i18n.getOrDefault(e.getValue(), e.getValue()));
    }
}
