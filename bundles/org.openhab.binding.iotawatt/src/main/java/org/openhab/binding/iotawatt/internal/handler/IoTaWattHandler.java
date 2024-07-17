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
package org.openhab.binding.iotawatt.internal.handler;

import static org.openhab.binding.iotawatt.internal.IoTaWattBindingConstants.BINDING_ID;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.iotawatt.internal.IoTaWattConfiguration;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClient;
import org.openhab.binding.iotawatt.internal.model.IoTaWattChannelType;
import org.openhab.binding.iotawatt.internal.service.DeviceHandlerCallback;
import org.openhab.binding.iotawatt.internal.service.FetchDataService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link IoTaWattHandler} is responsible for the communication between the external device and openHAB.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public class IoTaWattHandler extends BaseThingHandler implements DeviceHandlerCallback {
    private final IoTaWattClientProvider ioTaWattClientProvider;
    private final FetchDataService fetchDataService;
    private @Nullable IoTaWattClient ioTaWattClient;
    private @Nullable ScheduledFuture<?> fetchDataJob;

    /**
     * Creates an IoTaWattHandler
     * 
     * @param thing The Thing of the IoTaWattHandler
     * @param ioTaWattClientProvider The IoTaWattClientProvider to use
     * @param fetchDataServiceProvider The FetchDataServiceProvider to use to fetch data
     */
    public IoTaWattHandler(Thing thing, IoTaWattClientProvider ioTaWattClientProvider,
            FetchDataServiceProvider fetchDataServiceProvider) {
        super(thing);
        this.ioTaWattClientProvider = ioTaWattClientProvider;
        this.fetchDataService = fetchDataServiceProvider.getFetchDataService(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        final IoTaWattConfiguration config = getConfigAs(IoTaWattConfiguration.class);
        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/configuration-error");
            return;
        }

        final IoTaWattClient ioTaWattClient = ioTaWattClientProvider.getIoTaWattClient(config.hostname,
                config.requestTimeout);
        ioTaWattClient.start();
        fetchDataService.setIoTaWattClient(ioTaWattClient);
        this.ioTaWattClient = ioTaWattClient;

        updateStatus(ThingStatus.UNKNOWN);

        fetchDataJob = scheduler.scheduleWithFixedDelay(fetchDataService::pollDevice, 0, config.refreshInterval,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> fetchDataJobLocal = this.fetchDataJob;
        if (fetchDataJobLocal != null) {
            fetchDataJobLocal.cancel(true);
            this.fetchDataJob = null;
        }
        IoTaWattClient ioTaWattClient = this.ioTaWattClient;
        if (ioTaWattClient != null) {
            ioTaWattClient.stop();
            this.ioTaWattClient = null;
        }
        super.dispose();
    }

    // --------------------------------------------------------------------------------------------
    // Callbacks
    // --------------------------------------------------------------------------------------------
    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        super.updateStatus(status, statusDetail);
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override
    public ThingUID getThingUID() {
        return getThing().getUID();
    }

    @Override
    public void addChannelIfNotExists(ChannelUID channelUID, IoTaWattChannelType ioTaWattChannelType) {
        final ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, ioTaWattChannelType.typeId);
        if (getThing().getChannel(channelUID) == null) {
            final ThingBuilder thingBuilder = editThing();
            final Channel channel = ChannelBuilder.create(channelUID, ioTaWattChannelType.acceptedItemType)
                    .withType(channelTypeUID).build();
            thingBuilder.withChannel(channel);
            updateThing(thingBuilder.build());
        }
    }
}
