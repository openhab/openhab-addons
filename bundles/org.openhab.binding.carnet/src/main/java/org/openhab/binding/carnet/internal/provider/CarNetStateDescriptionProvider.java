/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.carnet.internal.provider;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.CarNetTextResources;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper;
import org.openhab.binding.carnet.internal.api.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link CarNetStateDescriptionProvider} class is a dynamic provider of state options while leaving other state
 * description fields as original.
 *
 * @author Markus Michels - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, CarNetStateDescriptionProvider.class })
@NonNullByDefault
public class CarNetStateDescriptionProvider implements DynamicStateDescriptionProvider {
    private final Map<ChannelUID, @Nullable List<StateOption>> channelOptionsMap = new ConcurrentHashMap<>();
    private final CarNetIChanneldMapper channelIdMapper;

    @Activate
    public CarNetStateDescriptionProvider(@Reference CarNetTextResources resources,
            @Reference CarNetIChanneldMapper channelIdMapper) {
        this.channelIdMapper = channelIdMapper;
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {
        ChannelTypeUID ctu = channel.getChannelTypeUID();
        if (ctu == null) {
            return null;
        }
        String channelId = ctu.getId();
        StateDescriptionFragmentBuilder builder = buildStateDescriptor(channelIdMapper, channelId);
        if (builder == null) {
            builder = original == null ? StateDescriptionFragmentBuilder.create()
                    : StateDescriptionFragmentBuilder.create(original);
        }

        List<StateOption> options = channelOptionsMap.get(channel.getUID());
        if (options != null) {
            return builder.withOptions(options).build().toStateDescription();
        } else {
            return builder.build().toStateDescription();
        }
    }

    public @Nullable static StateDescriptionFragmentBuilder buildStateDescriptor(CarNetIChanneldMapper channelIdMapper,
            String channelId) {
        ChannelIdMapEntry channelDef = channelIdMapper.find(channelId);
        if (channelDef == null) {
            return null;
        }

        StateDescriptionFragmentBuilder state = StateDescriptionFragmentBuilder.create()
                .withReadOnly(channelDef.readOnly);
        String itemType = channelDef.itemType;
        int min = channelDef.getMin();
        int max = channelDef.getMax();
        int step = channelDef.getStep();
        String pattern = channelDef.getPattern();

        if (pattern.isEmpty()) {
            switch (channelDef.itemType) {
                case ITEMT_SWITCH:
                case ITEMT_CONTACT:
                    break;
                case ITEMT_STRING:
                    pattern = "%s";
                    break;
                case ITEMT_NUMBER:
                case ITEMT_PERCENT:
                default:
                    if ((channelDef.unit != null) && itemType.contains("Number")) {
                        pattern = "%.1f %unit%";
                    }
            }
        }
        if (!pattern.isEmpty()) {
            state = state.withPattern(channelDef.pattern);
        }

        if (min != -1) {
            state = state.withMinimum(new BigDecimal(min));
        }
        if (max != -1) {
            state = state.withMaximum(new BigDecimal(max));
        }
        if (step != -1) {
            state = state.withStep(new BigDecimal(step));
        }
        if (!channelDef.options.isEmpty()) {
            for (String opt : channelDef.options.split(",")) {
                String option = channelDef.getChannelAttribute("state.option." + opt);
                state.withOption(new StateOption(opt, option));
            }
        }
        return state;
    }
}
