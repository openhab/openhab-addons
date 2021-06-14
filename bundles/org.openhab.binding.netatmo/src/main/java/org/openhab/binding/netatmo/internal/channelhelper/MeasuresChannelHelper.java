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

import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureScale;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.MeasureType;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.config.MeasureChannelConfig;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class MeasuresChannelHelper extends AbstractChannelHelper {
    private final Map<MeasureChannelConfig, Object> measures = new HashMap<>();
    private final Map<String, MeasureChannelConfig> thingChannels = new HashMap<>();

    public MeasuresChannelHelper() {
        super();
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        MeasureChannelConfig channelConfig = thingChannels.get(channelId);
        if (channelConfig != null) {
            Object measure = measures.get(channelConfig);
            return measure instanceof ZonedDateTime ? toDateTimeType((ZonedDateTime) measure)
                    : measure instanceof Double ? toQuantityType((Double) measure, channelConfig.type.getUnit())
                            : UnDefType.NULL;
        }
        return null;
    }

    private boolean isChannelConfigIfValid(Channel channel) {
        MeasureChannelConfig config = channel.getConfiguration().as(MeasureChannelConfig.class);
        return config.period != MeasureScale.UNKNOWN && config.type != MeasureType.UNKNOWN;
    }

    public Map<MeasureChannelConfig, Object> getMeasures() {
        return measures;
    }

    public void collectMeasuredChannels(List<Channel> channels) {
        measures.clear();
        thingChannels.clear();
        channels.stream().filter(channel -> isChannelConfigIfValid(channel)).filter(Objects::nonNull)
                .forEach(channel -> {
                    MeasureChannelConfig config = channel.getConfiguration().as(MeasureChannelConfig.class);
                    thingChannels.put(channel.getUID().getIdWithoutGroup(), config);
                    measures.put(config, Double.NaN);
                });
    }
}
