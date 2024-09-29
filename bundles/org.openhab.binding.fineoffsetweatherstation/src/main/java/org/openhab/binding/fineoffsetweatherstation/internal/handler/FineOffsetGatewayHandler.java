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
package org.openhab.binding.fineoffsetweatherstation.internal.handler;

import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_GATEWAY;
import static org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants.THING_TYPE_SENSOR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetSensorConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.discovery.FineOffsetGatewayDiscoveryService;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.ConversionContext;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SystemInfo;
import org.openhab.binding.fineoffsetweatherstation.internal.service.GatewayQueryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FineOffsetGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class FineOffsetGatewayHandler extends BaseBridgeHandler {

    private static final String PROPERTY_FREQUENCY = "frequency";

    private final Logger logger = LoggerFactory.getLogger(FineOffsetGatewayHandler.class);
    private final Bundle bundle;
    private final ConversionContext conversionContext;

    private @Nullable GatewayQueryService gatewayQueryService;

    private final FineOffsetGatewayDiscoveryService gatewayDiscoveryService;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;

    private final ThingUID bridgeUID;

    private @Nullable Map<SensorGatewayBinding, SensorDevice> sensorDeviceMap;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> discoverJob;
    private boolean disposed;

    public FineOffsetGatewayHandler(Bridge bridge, FineOffsetGatewayDiscoveryService gatewayDiscoveryService,
            ChannelTypeRegistry channelTypeRegistry, TranslationProvider translationProvider,
            LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider) {
        super(bridge);
        this.bridgeUID = bridge.getUID();
        this.gatewayDiscoveryService = gatewayDiscoveryService;
        this.channelTypeRegistry = channelTypeRegistry;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(FineOffsetGatewayDiscoveryService.class);
        this.conversionContext = new ConversionContext(timeZoneProvider.getTimeZone());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        FineOffsetGatewayConfiguration config = getConfigAs(FineOffsetGatewayConfiguration.class);
        gatewayQueryService = config.protocol.getGatewayQueryService(config, this::updateStatus, conversionContext);

        updateStatus(ThingStatus.UNKNOWN);
        fetchAndUpdateSensors();
        disposed = false;
        updateBridgeInfo();
        startDiscoverJob();
        startPollingJob();
    }

    private void fetchAndUpdateSensors() {
        @Nullable
        Map<SensorGatewayBinding, SensorDevice> deviceMap = query(GatewayQueryService::getRegisteredSensors);
        sensorDeviceMap = deviceMap;
        updateSensors();
        if (deviceMap != null) {
            gatewayDiscoveryService.addSensors(bridgeUID, deviceMap.values());
        }
    }

    private void updateSensors() {
        ((Bridge) thing).getThings().forEach(this::updateSensorThing);
    }

    private void updateSensorThing(Thing thing) {
        Map<SensorGatewayBinding, SensorDevice> sensorMap = sensorDeviceMap;
        if (!THING_TYPE_SENSOR.equals(thing.getThingTypeUID()) || sensorMap == null) {
            return;
        }
        SensorGatewayBinding sensor = thing.getConfiguration().as(FineOffsetSensorConfiguration.class).sensor;
        Optional.ofNullable(thing.getHandler()).filter(FineOffsetSensorHandler.class::isInstance)
                .map(FineOffsetSensorHandler.class::cast)
                .ifPresent(sensorHandler -> sensorHandler.updateSensorState(sensorMap.get(sensor)));
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        updateSensorThing(childThing);
    }

    private void updateLiveData() {
        if (disposed) {
            return;
        }
        Collection<MeasuredValue> data = query(GatewayQueryService::getMeasuredValues);
        if (data == null) {
            getThing().getChannels().forEach(c -> updateState(c.getUID(), UnDefType.UNDEF));
            return;
        }

        List<Channel> channels = new ArrayList<>();
        for (MeasuredValue measuredValue : data) {
            @Nullable
            Channel channel = thing.getChannel(measuredValue.getChannelId());
            if (channel == null) {
                channel = createChannel(measuredValue);
                if (channel != null) {
                    channels.add(channel);
                }
            } else {
                State state = measuredValue.getState();
                updateState(channel.getUID(), state);
            }
        }
        if (!channels.isEmpty()) {
            updateBridgeThing(bridgeBuilder -> bridgeBuilder.withChannels(channels));
        }
    }

    private @Nullable Channel createChannel(MeasuredValue measuredValue) {
        ChannelTypeUID channelTypeId = measuredValue.getChannelTypeUID();
        if (channelTypeId == null) {
            logger.debug("cannot create channel for {}", measuredValue.getDebugName());
            return null;
        }
        ChannelBuilder builder = ChannelBuilder.create(new ChannelUID(thing.getUID(), measuredValue.getChannelId()))
                .withKind(ChannelKind.STATE).withType(channelTypeId);
        String channelKey = THING_TYPE_GATEWAY.getId() + ".dynamic-channel." + measuredValue.getChannelPrefix();
        String label = translationProvider.getText(bundle, channelKey + ".label", measuredValue.getDebugName(),
                localeProvider.getLocale(), measuredValue.getChannelNumber());
        if (label != null) {
            builder.withLabel(label);
        }
        String description = translationProvider.getText(bundle, channelKey + ".description", null,
                localeProvider.getLocale(), measuredValue.getChannelNumber());
        if (description != null) {
            builder.withDescription(description);
        }
        @Nullable
        ChannelType type = channelTypeRegistry.getChannelType(channelTypeId);
        if (type != null) {
            builder.withAcceptedItemType(type.getItemType());
        }
        return builder.build();
    }

    private void updateBridgeInfo() {
        @Nullable
        String firmware = query(GatewayQueryService::getFirmwareVersion);
        Map<String, String> properties = new HashMap<>(thing.getProperties());
        if (firmware != null) {
            var fwString = firmware.split("_?V");
            if (fwString.length > 1) {
                properties.put(Thing.PROPERTY_MODEL_ID, fwString[0]);
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, fwString[1]);
            }
        }

        SystemInfo systemInfo = query(GatewayQueryService::fetchSystemInfo);
        if (systemInfo != null && systemInfo.getFrequency() != null) {
            properties.put(PROPERTY_FREQUENCY, systemInfo.getFrequency() + " MHz");
        }
        if (!thing.getProperties().equals(properties)) {
            updateBridgeThing(bridgeBuilder -> bridgeBuilder.withProperties(properties));
        }
    }

    private void updateBridgeThing(Consumer<BridgeBuilder> customizer) {
        BridgeBuilder bridge = editThing();
        customizer.accept(bridge);
        updateThing(bridge.build());
    }

    private void startDiscoverJob() {
        ScheduledFuture<?> job = discoverJob;
        if (job == null || job.isCancelled()) {
            int discoverInterval = thing.getConfiguration().as(FineOffsetGatewayConfiguration.class).discoverInterval;
            discoverJob = scheduler.scheduleWithFixedDelay(this::fetchAndUpdateSensors, 0, discoverInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void stopDiscoverJob() {
        ScheduledFuture<?> job = this.discoverJob;
        if (job != null) {
            job.cancel(true);
        }
        this.discoverJob = null;
    }

    private void startPollingJob() {
        ScheduledFuture<?> job = pollingJob;
        if (job == null || job.isCancelled()) {
            int pollingInterval = thing.getConfiguration().as(FineOffsetGatewayConfiguration.class).pollingInterval;
            pollingJob = scheduler.scheduleWithFixedDelay(this::updateLiveData, 5, pollingInterval, TimeUnit.SECONDS);
        }
    }

    private void stopPollingJob() {
        ScheduledFuture<?> job = this.pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        this.pollingJob = null;
    }

    private <T> @Nullable T query(Function<GatewayQueryService, T> delegate) {
        @Nullable
        GatewayQueryService queryService = this.gatewayQueryService;
        if (queryService == null) {
            return null;
        }
        return delegate.apply(queryService);
    }

    @Override
    public void dispose() {
        disposed = true;
        @Nullable
        GatewayQueryService queryService = this.gatewayQueryService;
        if (queryService != null) {
            try {
                queryService.close();
            } catch (IOException e) {
                logger.debug("failed to close queryService", e);
            }
        }
        this.gatewayQueryService = null;
        this.sensorDeviceMap = null;
        stopPollingJob();
        stopDiscoverJob();
    }
}
