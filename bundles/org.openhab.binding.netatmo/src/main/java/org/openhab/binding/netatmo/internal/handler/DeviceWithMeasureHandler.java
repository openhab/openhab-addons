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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DeviceWithMeasureHandler} is the base class for all modules handling getMeasure endpoint
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class DeviceWithMeasureHandler extends DeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(DeviceWithMeasureHandler.class);
    private final Map<String, State> measures = new HashMap<>();
    private final Set<Channel> measureChannels = new HashSet<>();

    public DeviceWithMeasureHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);

        channelHelpers.stream().filter(c -> c instanceof MeasuresChannelHelper).findFirst()
                .map(MeasuresChannelHelper.class::cast).ifPresent(helper -> helper.setMeasures(measures));
    }

    @Override
    public void setNewData(NAObject newData) {
        collectMeasures();
        getBridgeHandler().ifPresentOrElse(handler -> ((DeviceWithMeasureHandler) handler)
                .callGetMeasurements(config.id, measureChannels, measures),
                () -> callGetMeasurements(null, measureChannels, measures));
        super.setNewData(newData);
    }

    private void callGetMeasurements(@Nullable String moduleId, Set<Channel> measureChannels,
            Map<String, State> localMeasures) {
        localMeasures.clear();
        WeatherApi api = apiBridge.getRestManager(WeatherApi.class);
        measureChannels.forEach(channel -> {
            Configuration measureDef = channel.getConfiguration();
            String period = (String) measureDef.get("period");
            String limit = (String) measureDef.get("limit");
            ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null) {
                String channelId = channelTypeUID.getId();
                String[] elements = channelId.split("-");
                MeasureClass.asSet.stream().filter(mt -> mt.apiDescriptor.equals(elements[0])).findFirst()
                        .ifPresent(mt -> {
                            try {
                                Object result;
                                if (limit != null) {
                                    result = api.getMeasurements(config.id, moduleId, period, mt, limit);
                                } else {
                                    result = api.getMeasurements(config.id, moduleId, period, mt);
                                }

                                State data = result instanceof ZonedDateTime ? toDateTimeType((ZonedDateTime) result)
                                        : result instanceof Double ? toQuantityType((Double) result, mt)
                                                : UnDefType.UNDEF;

                                localMeasures.put(channel.getUID().getIdWithoutGroup(), data);
                            } catch (NetatmoException e) {
                                logger.warn("Error getting measurement {} for channel {}",
                                        measureDef.values().toString(), channel.getLabel());
                            }
                        });
            }
        });
    }

    private void collectMeasures() {
        measureChannels.clear();
        measureChannels.addAll(getThing().getChannels().stream()
                .filter(channel -> !channel.getConfiguration().getProperties().isEmpty()).collect(Collectors.toSet()));
    }
}
