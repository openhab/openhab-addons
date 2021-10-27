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
package org.openhab.binding.netatmo.internal.providers;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureClass;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
@Component(service = { NetatmoChannelTypeProvider.class, ChannelTypeProvider.class })
public class NetatmoChannelTypeProvider implements ChannelTypeProvider {
    public static final String MEASURE = "Measurement";
    public static final String TIMESTAMP = "Timestamp";
    private Map<ChannelTypeUID, ChannelType> channelTypes = new ConcurrentHashMap<ChannelTypeUID, ChannelType>();

    public NetatmoChannelTypeProvider() {
        MeasureClass.asSet.forEach(measure -> {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, measure.tagName + "-" + MEASURE);
            ChannelType channelType = ChannelTypeBuilder
                    .state(channelTypeUID, measure.tagName + " " + MEASURE, "Number:" + measure.dimension)
                    .withConfigDescriptionURI(buildConfigName(MEASURE))
                    .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create()
                            .withPattern(getPattern(measure)).withReadOnly(true).build())
                    .withTags(List.of(MEASURE, measure.tagName)).build();
            channelTypes.put(channelTypeUID, channelType);
            if (measure.isScalable) {
                channelTypeUID = new ChannelTypeUID(BINDING_ID, measure.tagName + "-" + TIMESTAMP);
                channelType = ChannelTypeBuilder.state(channelTypeUID, measure.tagName + " " + TIMESTAMP, "DateTime")
                        .withStateDescriptionFragment(
                                StateDescriptionFragmentBuilder.create().withReadOnly(true).build())
                        .withConfigDescriptionURI(buildConfigName(TIMESTAMP)).withCategory("time")
                        .withTags(List.of("Status", TIMESTAMP)).build();
                channelTypes.put(channelTypeUID, channelType);
            }
        });
    }

    private String getPattern(MeasureClass measure) {
        int scale = measure.measureDefinition.scale;
        return String.format("%%.%df %%unit%%", scale);
    }

    private URI buildConfigName(String variable) {
        return URI.create(String.join(":", BINDING_ID, variable, "config"));
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (channelTypes.containsKey(channelTypeUID)) {
            return channelTypes.get(channelTypeUID);
        } else {
            return null;
        }
    }
}
