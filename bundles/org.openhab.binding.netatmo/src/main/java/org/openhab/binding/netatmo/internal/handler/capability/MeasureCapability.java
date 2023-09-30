/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.config.MeasureConfiguration;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.MeasuresChannelHelper;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeasureCapability} is the base class for handler able to handle user defined measures
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class MeasureCapability extends CacheWeatherCapability {
    private final Logger logger = LoggerFactory.getLogger(MeasureCapability.class);
    private final Map<String, State> measures = new HashMap<>();

    public MeasureCapability(CommonInterface handler, List<ChannelHelper> helpers) {
        super(handler, Duration.ofMinutes(30));
        MeasuresChannelHelper measureChannelHelper = (MeasuresChannelHelper) helpers.stream()
                .filter(c -> c instanceof MeasuresChannelHelper).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "MeasureCapability must find a MeasuresChannelHelper, please file a bug report."));
        measureChannelHelper.setMeasures(measures);
    }

    private void updateMeasures(WeatherApi api, String deviceId, @Nullable String moduleId) {
        measures.clear();
        handler.getActiveChannels().filter(channel -> !channel.getConfiguration().getProperties().isEmpty())
                .forEach(channel -> {
                    ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
                    if (channelTypeUID == null) {
                        return;
                    }

                    MeasureConfiguration measureDef = channel.getConfiguration().as(MeasureConfiguration.class);
                    String descriptor = channelTypeUID.getId().split("-")[0];
                    try {
                        Object result = measureDef.limit.isBlank()
                                ? api.getMeasures(deviceId, moduleId, measureDef.period, descriptor)
                                : api.getMeasures(deviceId, moduleId, measureDef.period, descriptor, measureDef.limit);
                        MeasureClass.AS_SET.stream().filter(mc -> mc.apiDescriptor.equals(descriptor))
                                .reduce((first, second) -> second)
                                .ifPresent(mc -> measures.put(channel.getUID().getIdWithoutGroup(),
                                        result instanceof ZonedDateTime zonedDateTime ? toDateTimeType(zonedDateTime)
                                                : result instanceof Double ? toQuantityType((Double) result, mc)
                                                        : UnDefType.UNDEF));
                    } catch (NetatmoException e) {
                        logger.warn("Error getting measures for channel {}, check configuration", channel.getLabel());
                    }
                });
    }

    @Override
    protected List<NAObject> getFreshData(WeatherApi api) {
        String bridgeId = handler.getBridgeId();
        String deviceId = bridgeId != null ? bridgeId : handler.getId();
        String moduleId = bridgeId != null ? handler.getId() : null;
        updateMeasures(api, deviceId, moduleId);
        return List.of();
    }
}
