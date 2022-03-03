/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.config.MeasureConfiguration;
import org.openhab.binding.netatmo.internal.handler.NACommonInterface;
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
public class MeasureCapability extends RestCapability<WeatherApi> {
    private final Logger logger = LoggerFactory.getLogger(MeasureCapability.class);
    private final Map<String, State> measures = new HashMap<>();

    public MeasureCapability(NACommonInterface handler, MeasuresChannelHelper helper, ApiBridge apiBridge) {
        super(handler, apiBridge.getRestManager(WeatherApi.class));
        helper.setMeasures(measures);
    }

    @Override
    public List<NAObject> updateReadings() {
        String bridgeId = handler.getBridgeId();
        String deviceId = bridgeId != null ? bridgeId : handlerId;
        String moduleId = bridgeId != null ? handlerId : null;
        updateMeasurements(deviceId, moduleId);
        return List.of();
    }

    public void updateMeasurements(String deviceId, @Nullable String moduleId) {
        measures.clear();
        thing.getChannels().stream().filter(channel -> !channel.getConfiguration().getProperties().isEmpty())
                .forEach(channel -> {
                    ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
                    if (channelTypeUID != null) {
                        MeasureConfiguration measureDef = channel.getConfiguration().as(MeasureConfiguration.class);
                        String apiDescriptor = channelTypeUID.getId().split("-")[0];
                        try {
                            Object result = measureDef.limit.isBlank()
                                    ? api.getMeasurements(deviceId, moduleId, measureDef.period, apiDescriptor)
                                    : api.getMeasurements(deviceId, moduleId, measureDef.period, apiDescriptor,
                                            measureDef.limit);
                            MeasureClass.AS_SET.stream().filter(mt -> mt.apiDescriptor.equals(apiDescriptor))
                                    .findFirst().ifPresent(mt -> {
                                        State data = result instanceof ZonedDateTime
                                                ? toDateTimeType((ZonedDateTime) result)
                                                : result instanceof Double ? toQuantityType((Double) result, mt)
                                                        : UnDefType.UNDEF;
                                        measures.put(channel.getUID().getIdWithoutGroup(), data);
                                    });
                        } catch (NetatmoException e) {
                            logger.warn("Error getting measurements for channel {}, check configuration",
                                    channel.getLabel());
                        }
                    }
                });
    }
}
