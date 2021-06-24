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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.config.MeasureChannelConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DeviceWithMeasureHandler} is the base class for all current Netatmo
 * weather station equipments (both modules and devices)
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class DeviceWithMeasureHandler extends DeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);
    private final Optional<WeatherApi> weatherApi;
    private final Map<String, State> measures = new HashMap<>();

    public DeviceWithMeasureHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);

        channelHelpers.stream().filter(c -> c instanceof MeasuresChannelHelper).findFirst()
                .map(MeasuresChannelHelper.class::cast).ifPresent(helper -> helper.setMeasures(measures));

        weatherApi = apiBridge.getWeatherApi();
    }

    @Override
    public void setNewData(NAObject newData) {
        weatherApi.ifPresent(api -> {
            Stream<Channel> measureChannels = getThing().getChannels().stream()
                    .filter(channel -> channel.getConfiguration().as(MeasureChannelConfig.class).isValid());
            getBridgeHandler().ifPresentOrElse(handler -> ((DeviceWithMeasureHandler) handler).callGetMeasurements(api,
                    config.id, measureChannels), () -> callGetMeasurements(api, null, measureChannels));
        });
        super.setNewData(newData);
    }

    private void callGetMeasurements(WeatherApi api, @Nullable String moduleId, Stream<Channel> measureChannels) {
        measures.clear();
        measureChannels.forEach(channel -> {
            MeasureChannelConfig measureDef = channel.getConfiguration().as(MeasureChannelConfig.class);
            try {
                Object result = api.getMeasurements(config.id, moduleId, measureDef.period, measureDef.type,
                        measureDef.limit);

                State data = result instanceof ZonedDateTime ? toDateTimeType((ZonedDateTime) result)
                        : result instanceof Double ? toQuantityType((Double) result, measureDef.type.getUnit())
                                : UnDefType.UNDEF;

                measures.put(channel.getUID().getIdWithoutGroup(), data);
            } catch (NetatmoException e) {
                logger.warn("Error getting measurement {} on period {} for module {} : {}", measureDef.type,
                        measureDef.period, moduleId, e);
            }
        });
    }
}
