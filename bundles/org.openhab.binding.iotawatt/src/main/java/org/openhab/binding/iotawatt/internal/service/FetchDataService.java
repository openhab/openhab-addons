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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClient;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClientCommunicationException;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClientConfigurationException;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClientException;
import org.openhab.binding.iotawatt.internal.client.IoTaWattClientInterruptedException;
import org.openhab.binding.iotawatt.internal.model.IoTaWattChannelType;
import org.openhab.binding.iotawatt.internal.model.StatusResponse;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Fetches data from IoTaWatt and updates the channels accordingly.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public class FetchDataService {
    static final String INPUT_CHANNEL_ID_PREFIX = "input_";
    static final String OUTPUT_CHANNEL_ID_PREFIX = "output_";

    private final DeviceHandlerCallback deviceHandlerCallback;
    private @Nullable IoTaWattClient ioTaWattClient;

    /**
     * Creates a FetchDataService.
     * 
     * @param deviceHandlerCallback The ThingHandler used for callbacks
     */
    public FetchDataService(DeviceHandlerCallback deviceHandlerCallback) {
        this.deviceHandlerCallback = deviceHandlerCallback;
    }

    /**
     * Setter for the IoTaWattClient
     * 
     * @param ioTaWattClient The IoTaWattClient to use
     */
    public void setIoTaWattClient(IoTaWattClient ioTaWattClient) {
        this.ioTaWattClient = ioTaWattClient;
    }

    /**
     * Poll the device once without retry.
     * Handles error cases and updates the Thing accordingly.
     */
    public void pollDevice() {
        Optional.ofNullable(ioTaWattClient).ifPresentOrElse(this::pollDevice, () -> deviceHandlerCallback
                .updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR));
    }

    private void pollDevice(IoTaWattClient client) {
        try {
            final Optional<StatusResponse> statusResponse = client.fetchStatus();
            if (statusResponse.isPresent()) {
                deviceHandlerCallback.updateStatus(ThingStatus.ONLINE);
                updateChannels(statusResponse.get());
            } else {
                deviceHandlerCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (IoTaWattClientInterruptedException e) {
            deviceHandlerCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NOT_YET_READY);
        } catch (IoTaWattClientCommunicationException e) {
            deviceHandlerCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (IoTaWattClientConfigurationException e) {
            deviceHandlerCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    getErrorMessage(e));
        } catch (IoTaWattClientException e) {
            deviceHandlerCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, getErrorMessage(e));
        }
    }

    @Nullable
    private String getErrorMessage(Throwable t) {
        final Throwable cause = t.getCause();
        return Objects.requireNonNullElse(cause, t).getMessage();
    }

    private void updateChannels(StatusResponse statusResponse) {
        Optional.ofNullable(statusResponse.inputs()).ifPresent(this::updateInputs);
        Optional.ofNullable(statusResponse.outputs()).ifPresent(this::updateOutputs);
    }

    private void updateInputs(List<StatusResponse.Input> inputs) {
        for (final StatusResponse.Input input : inputs) {
            final int channelNumber = input.channel();
            createAndUpdateInputChannel(channelNumber, input.watts(), IoTaWattChannelType.WATTS);
            createAndUpdateInputChannel(channelNumber, input.vrms(), IoTaWattChannelType.VOLTAGE);
            createAndUpdateInputChannel(channelNumber, input.hz(), IoTaWattChannelType.FREQUENCY);
            createAndUpdateInputChannel(channelNumber, input.pf(), IoTaWattChannelType.POWER_FACTOR);
            createAndUpdateInputChannel(channelNumber, input.phase(), IoTaWattChannelType.PHASE);
        }
    }

    private void updateOutputs(List<StatusResponse.Output> outputs) {
        int index = 0;
        for (final StatusResponse.Output output : outputs) {
            final ChannelUID channelUID = new ChannelUID(deviceHandlerCallback.getThingUID(),
                    OUTPUT_CHANNEL_ID_PREFIX + toTwoDigits(index++) + "#" + output.name());
            final Float value = output.value();
            final IoTaWattChannelType ioTaWattChannelType = IoTaWattChannelType.fromOutputUnits(output.units());
            deviceHandlerCallback.addChannelIfNotExists(channelUID, ioTaWattChannelType);
            deviceHandlerCallback.updateState(channelUID, new QuantityType<>(value, ioTaWattChannelType.unit));
            // TODO removed channels are not in array anymore
        }
    }

    private void createAndUpdateInputChannel(int channelNumber, @Nullable Number value,
            IoTaWattChannelType ioTaWattChannelType) {
        final ChannelUID channelUID = getInputChannelUID(channelNumber, ioTaWattChannelType);
        if (value != null) {
            deviceHandlerCallback.addChannelIfNotExists(channelUID, ioTaWattChannelType);
            deviceHandlerCallback.updateState(channelUID, new QuantityType<>(value, ioTaWattChannelType.unit));
            // TODO removed channels are not in array anymore
        }
    }

    private ChannelUID getInputChannelUID(int channelNumber, IoTaWattChannelType ioTaWattChannelType) {
        return new ChannelUID(deviceHandlerCallback.getThingUID(),
                INPUT_CHANNEL_ID_PREFIX + toTwoDigits(channelNumber) + "#" + ioTaWattChannelType.channelIdSuffix);
    }

    private String toTwoDigits(int value) {
        return value < 10 ? ("0" + value) : String.valueOf(value);
    }
}
