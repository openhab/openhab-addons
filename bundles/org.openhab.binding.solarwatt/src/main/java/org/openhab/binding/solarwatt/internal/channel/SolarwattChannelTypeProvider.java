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
package org.openhab.binding.solarwatt.internal.channel;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarwatt.internal.SolarwattBindingConstants;
import org.openhab.binding.solarwatt.internal.domain.SolarwattChannel;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.util.UnitUtils;
import org.osgi.service.component.annotations.Component;

/**
 * A {@link ChannelTypeProvider} that creates {@link ChannelType}s according to
 * the requested tags. It creates one {@link ChannelType} per tag value.
 *
 * @author Matthias Steigenberger - Initial contribution
 * @author Sven Carstens - Adapted to solarwatt binding
 *
 */
@NonNullByDefault
@Component(service = { ChannelTypeProvider.class, SolarwattChannelTypeProvider.class })
public class SolarwattChannelTypeProvider implements ChannelTypeProvider {

    private final Map<String, ChannelType> channelMap = new ConcurrentHashMap<>();

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return this.channelMap.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return this.channelMap.values().stream().filter(channelType -> channelType.getUID().equals(channelTypeUID))
                .findFirst().orElse(null);
    }

    /**
     * Assert that the {@link ChannelType} matching our requirements exists.
     *
     * Only create once for each tagname supplied via {@link SolarwattChannel}.
     * 
     * @param solarwattChannel channeltype requirements
     * @return UID of existing channeltype
     */
    public ChannelTypeUID assertChannelType(SolarwattChannel solarwattChannel) {
        ChannelType existingChannel = this.channelMap.get(solarwattChannel.getChannelName());
        if (existingChannel == null) {
            ChannelType createdChannel = this.getChannelType(solarwattChannel);
            this.channelMap.put(solarwattChannel.getChannelName(), createdChannel);
            return createdChannel.getUID();
        } else {
            return existingChannel.getUID();
        }
    }

    private ChannelType getChannelType(SolarwattChannel solarwattChannel) {
        StateChannelTypeBuilder stateDescriptionBuilder;
        Unit<?> unit = solarwattChannel.getUnit();
        if (unit != null) {
            if ("switch".equals(solarwattChannel.getCategory())) {
                stateDescriptionBuilder = ChannelTypeBuilder
                        .state(new ChannelTypeUID(SolarwattBindingConstants.BINDING_ID,
                                solarwattChannel.getChannelName()), solarwattChannel.getChannelName(),
                                CoreItemFactory.SWITCH)
                        .withCategory(solarwattChannel.getCategory()).isAdvanced(solarwattChannel.getAdvanced())
                        .withStateDescriptionFragment(
                                StateDescriptionFragmentBuilder.create().withReadOnly(true).build());
            } else {
                String dimension = ":" + UnitUtils.getDimensionName(unit);
                String unitString = unit.toString();

                if (Units.PERCENT.equals(unit)) {
                    // strangely it is Angle
                    dimension = ":Dimensionless";
                    unitString = "%%";
                }

                stateDescriptionBuilder = ChannelTypeBuilder
                        .state(new ChannelTypeUID(SolarwattBindingConstants.BINDING_ID,
                                solarwattChannel.getChannelName()), solarwattChannel.getChannelName(),
                                CoreItemFactory.NUMBER + dimension)
                        .withCategory(solarwattChannel.getCategory()).isAdvanced(solarwattChannel.getAdvanced())
                        .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withReadOnly(true)
                                .withPattern("%.2f " + unitString).build());
            }
        } else {
            stateDescriptionBuilder = ChannelTypeBuilder
                    .state(new ChannelTypeUID(SolarwattBindingConstants.BINDING_ID, solarwattChannel.getChannelName()),
                            solarwattChannel.getChannelName(), CoreItemFactory.STRING)
                    .withCategory(solarwattChannel.getCategory()).isAdvanced(solarwattChannel.getAdvanced())
                    .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withReadOnly(true).build());
        }
        return stateDescriptionBuilder.build();
    }
}
