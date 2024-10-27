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
package org.openhab.binding.nanoleaf.internal.handler;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link NanoLeafStateDescriptionProvider} provides dynamic state description minimum and maximum vales of colour
 * temperature channels whose capabilities are dynamically determined at runtime.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = { DynamicStateDescriptionProvider.class, NanoLeafStateDescriptionProvider.class })
public class NanoLeafStateDescriptionProvider implements DynamicStateDescriptionProvider {

    private final Map<ChannelUID, StateDescriptionFragment> stateDescriptionFragments = new ConcurrentHashMap<>();

    @Activate
    public NanoLeafStateDescriptionProvider() {
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        StateDescriptionFragment stateDescriptionFragment = stateDescriptionFragments.get(channel.getUID());
        if (stateDescriptionFragment == null) {
            return null;
        }
        StateDescriptionFragmentBuilder builder = original == null ? StateDescriptionFragmentBuilder.create()
                : StateDescriptionFragmentBuilder.create(original);
        String pattern = original == null ? "%.0f K" : original.getPattern();
        if (pattern != null) {
            builder.withPattern(pattern);
        }
        BigDecimal minimum = stateDescriptionFragment.getMinimum();
        if (minimum != null) {
            builder.withMinimum(minimum);
        }
        BigDecimal maximum = stateDescriptionFragment.getMaximum();
        if (maximum != null) {
            builder.withMaximum(maximum);
        }
        return builder.build().toStateDescription();
    }

    /**
     * Set the state description minimum and maximum values for the given channel UID
     */
    public void setMinMax(ChannelUID channelUID, long min, long max) {
        stateDescriptionFragments.put(channelUID, StateDescriptionFragmentBuilder.create()
                .withMinimum(BigDecimal.valueOf(min)).withMaximum(BigDecimal.valueOf(max)).build());
    }
}
