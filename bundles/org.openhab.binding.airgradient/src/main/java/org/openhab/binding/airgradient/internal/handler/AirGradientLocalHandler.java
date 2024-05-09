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
package org.openhab.binding.airgradient.internal.handler;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.CHANNEL_CALIBRATION;
import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.CHANNEL_LEDS_MODE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.airgradient.internal.communication.AirGradientCommunicationException;
import org.openhab.binding.airgradient.internal.communication.RemoteAPIController;
import org.openhab.binding.airgradient.internal.config.AirGradientAPIConfiguration;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link AirGradientAPIHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class AirGradientLocalHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AirGradientLocalHandler.class);

    private @Nullable ScheduledFuture<?> pollingJob;
    private final HttpClient httpClient;
    private final Gson gson;

    private @NonNullByDefault({}) RemoteAPIController apiController = null;
    private @NonNullByDefault({}) AirGradientAPIConfiguration apiConfig = null;

    public AirGradientLocalHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
        this.gson = new Gson();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel {}: {}", channelUID, command.toFullString());
        if (command instanceof RefreshType) {
            pollingCode();
        } else if (CHANNEL_LEDS_MODE.equals(channelUID.getId())) {
            if (command instanceof StringType stringCommand) {
                setLedModeOnDevice(stringCommand.toFullString());
            } else {
                logger.warn("Received command {} for channel {}, but it needs a string command", command.toString(),
                        channelUID.getId());
            }
        } else if (CHANNEL_CALIBRATION.equals(channelUID.getId())) {
            if (command instanceof StringType stringCommand) {
                if ("co2".equals(stringCommand.toFullString())) {
                    calibrateCo2OnDevice();
                } else {
                    logger.warn(
                            "Received unknown command {} for calibration on channel {}, which we don't know how to handle",
                            command.toString(), channelUID.getId());
                }
            }
        } else {
            // This is read only
            logger.warn("Received command {} for channel {}, which we don't know how to handle", command.toString(),
                    channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        apiConfig = getConfigAs(AirGradientAPIConfiguration.class);
        if (!apiConfig.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Need to set hostname to a valid URL. Refresh interval needs to be a positive integer.");
            return;
        }

        apiController = new RemoteAPIController(httpClient, gson, apiConfig);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, apiConfig.refreshInterval,
                TimeUnit.SECONDS);
    }

    protected void pollingCode() {
        try {
            List<Measure> measures = apiController.getMeasures();
            updateStatus(ThingStatus.ONLINE);

            if (measures.size() != 1) {
                logger.warn("Expecting single set of measures for local device, but got {} measures", measures.size());
                return;
            }

            updateProperties(MeasureHelper.createProperties(measures.get(0)));
            Map<String, State> states = MeasureHelper.createStates(measures.get(0));
            for (Map.Entry<String, State> entry : states.entrySet()) {
                if (isLinked(entry.getKey())) {
                    updateState(entry.getKey(), entry.getValue());
                }
            }
        } catch (AirGradientCommunicationException agce) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, agce.getMessage());
        }
    }

    private void setLedModeOnDevice(String mode) {
        try {
            apiController.setLedMode(getSerialNo(), mode);
            updateStatus(ThingStatus.ONLINE);
        } catch (AirGradientCommunicationException agce) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, agce.getMessage());
        }
    }

    private void calibrateCo2OnDevice() {
        try {
            apiController.calibrateCo2(getSerialNo());
            updateStatus(ThingStatus.ONLINE);
        } catch (AirGradientCommunicationException agce) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, agce.getMessage());
        }
    }

    /**
     * Returns the serial number of this sensor.
     *
     * @return serial number of this sensor.
     */
    public String getSerialNo() {
        String serialNo = thing.getProperties().get(Thing.PROPERTY_SERIAL_NUMBER);
        if (serialNo == null) {
            serialNo = "";
        }

        return serialNo;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    protected void setConfiguration(AirGradientAPIConfiguration config) {
        this.apiConfig = config;
    }
}
