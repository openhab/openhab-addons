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
package org.openhab.binding.netatmo.internal.channelhelper;

import static org.openhab.binding.netatmo.internal.api.NetatmoConstants.MEASUREUNITS;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureLimit;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.config.MeasureChannelConfig;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class MeasuresChannelHelper extends AbstractChannelHelper {
    private final Map<MeasureChannelConfig, Double> measures = new HashMap<>();

    public MeasuresChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Optional<MeasureChannelConfig> config = getChannelConfigIfValid2(channel);
            if (config.isPresent()) {
                MeasureChannelConfig channelConfig = config.get();
                Double measure = measures.get(channelConfig);
                if (channelConfig.limit == MeasureLimit.DATE_MAX || channelConfig.limit == MeasureLimit.DATE_MIN) {
                    return toDateTimeType(measure, zoneId);
                }
                return toQuantityType(measure, MEASUREUNITS.get(channelConfig.type));
            }
        }
        return null;
    }

    private Optional<MeasureChannelConfig> getChannelConfigIfValid2(Channel channel) {
        MeasureChannelConfig config = channel.getConfiguration().as(MeasureChannelConfig.class);
        return config.period != null && config.type != null ? Optional.of(config) : Optional.empty();
    }

    public Map<MeasureChannelConfig, Double> getMeasures() {
        return measures;
    }

    public void collectMeasuredChannels() {
        measures.clear();
        thing.getChannels().stream().map(channel -> getChannelConfigIfValid2(channel)).filter(c -> c.isPresent())
                .forEach(config -> {
                    measures.put(config.get(), Double.NaN);
                });
    }
}
