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
package org.openhab.binding.iotawatt.internal.service;

import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClient;
import org.openhab.binding.iotawatt.internal.model.IoTaWattChannelType;
import org.openhab.binding.iotawatt.internal.model.StatusResponse;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetches data from IoTaWatt and updates the channels accordingly.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public class FetchDataService {
    static final String INPUT_CHANNEL_ID_PREFIX = "input_";

    private final Logger logger = LoggerFactory.getLogger(FetchDataService.class);

    private final DeviceHandlerCallback thingHandler;
    private @Nullable IoTaWattClient ioTaWattClient;

    public FetchDataService(DeviceHandlerCallback thingHandler) {
        this.thingHandler = thingHandler;
    }

    public void setIoTaWattClient(IoTaWattClient ioTaWattClient) {
        this.ioTaWattClient = ioTaWattClient;
    }

    /**
     * Poll the device once without retry.
     * Handles error cases and updates the Thing accordingly.
     */
    public void pollDevice() {
        final IoTaWattClient nonNullClient = ioTaWattClient;
        if (nonNullClient == null) {
            thingHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR);
            return;
        }

        try {
            final Optional<StatusResponse> statusResponse = nonNullClient.fetchStatus();
            if (statusResponse.isPresent()) {
                thingHandler.updateStatus(ThingStatus.ONLINE);
                updateChannels(statusResponse.get());
            } else {
                thingHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (InterruptedException e) {
            thingHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NOT_YET_READY);
        } catch (TimeoutException e) {
            thingHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (URISyntaxException e) {
            thingHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, getErrorMessage(e));
        } catch (ExecutionException e) {
            logger.debug("Error on getting data from IoTaWatt {}", nonNullClient.hostname);
            thingHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, getErrorMessage(e));
        }
    }

    @Nullable
    private String getErrorMessage(Throwable t) {
        final Throwable cause = t.getCause();
        return Objects.requireNonNullElse(cause, t).getMessage();
    }

    private void updateChannels(StatusResponse statusResponse) {
        for (final StatusResponse.Input input : statusResponse.inputs()) {
            final int channelNumber = input.channel();
            createAndUpdateInputChannel(channelNumber, input.watts(), IoTaWattChannelType.WATTS);
            createAndUpdateInputChannel(channelNumber, input.vrms(), IoTaWattChannelType.VOLTAGE);
            createAndUpdateInputChannel(channelNumber, input.hz(), IoTaWattChannelType.FREQUENCY);
        }
    }

    private void createAndUpdateInputChannel(int channelNumber, @Nullable Number value,
            IoTaWattChannelType ioTaWattChannelType) {
        final ChannelUID channelUID = getInputChannelUID(channelNumber, ioTaWattChannelType);
        if (value != null) {
            thingHandler.addChannelIfNotExists(channelUID, ioTaWattChannelType);
            thingHandler.updateState(channelUID, new QuantityType<>(value, ioTaWattChannelType.unit));
            // TODO removed channels are not in array anymore
        }
    }

    private ChannelUID getInputChannelUID(int channelNumber, IoTaWattChannelType ioTaWattChannelType) {
        final String channelNumberStr = channelNumber < 10 ? ("0" + channelNumber) : String.valueOf(channelNumber);
        return new ChannelUID(thingHandler.getThingUID(),
                INPUT_CHANNEL_ID_PREFIX + channelNumberStr + "#" + ioTaWattChannelType.channelIdSuffix);
    }
}
