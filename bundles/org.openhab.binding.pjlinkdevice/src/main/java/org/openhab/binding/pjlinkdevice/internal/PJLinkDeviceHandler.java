/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.pjlinkdevice.internal;

import static org.openhab.binding.pjlinkdevice.internal.PJLinkDeviceBindingConstants.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AuthenticationException;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;
import org.openhab.binding.pjlinkdevice.internal.device.command.errorstatus.ErrorStatusQueryResponse.ErrorStatusDevicePart;
import org.openhab.binding.pjlinkdevice.internal.device.command.errorstatus.ErrorStatusQueryResponse.ErrorStatusQueryResponseState;
import org.openhab.binding.pjlinkdevice.internal.device.command.input.Input;
import org.openhab.binding.pjlinkdevice.internal.device.command.mute.MuteInstructionCommand.MuteInstructionChannel;
import org.openhab.binding.pjlinkdevice.internal.device.command.mute.MuteQueryResponse.MuteQueryResponseValue;
import org.openhab.binding.pjlinkdevice.internal.device.command.power.PowerQueryResponse.PowerQueryResponseValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PJLinkDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class PJLinkDeviceHandler extends BaseThingHandler {

    @Nullable
    private PJLinkDeviceConfiguration config;

    private PJLinkDeviceHandlerFactory factory;

    private final Logger logger = LoggerFactory.getLogger(PJLinkDeviceHandler.class);

    @Nullable
    private ScheduledFuture<?> refreshJob;

    public PJLinkDeviceHandler(Thing thing, PJLinkDeviceHandlerFactory factory) {
        super(thing);
        this.factory = factory;
    }

    @Override
    public void dispose() {
        clearRefreshInterval();
        factory.removeChannelTypesForThing(getThing().getUID());
    }

    public void refresh() {
        // Do not poll if configuration is incomplete
        if (PJLinkDeviceHandler.this.getThing().getStatusInfo()
                .getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR ) {

            PJLinkDeviceHandler.this.logger.debug("Polling device status...");
            if (PJLinkDeviceHandler.this.getConfiguration().refreshPower) {
                PJLinkDeviceHandler.this.handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_POWER),
                        RefreshType.REFRESH);
            }
            if (PJLinkDeviceHandler.this.getConfiguration().refreshMute) {
                // this updates both CHANNEL_AUDIO_MUTE and CHANNEL_VIDEO_MUTE
                PJLinkDeviceHandler.this.handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_AUDIO_MUTE),
                        RefreshType.REFRESH);
            }
            if (PJLinkDeviceHandler.this.getConfiguration().refreshInputChannel) {
                // this updates both CHANNEL_INPUT and CHANNEL_INPUT_DYNAMIC
                PJLinkDeviceHandler.this.handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_INPUT),
                        RefreshType.REFRESH);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Received command {} on channel {}", command, channelUID.getId());
        try {
            PJLinkDevice device = this.getConfiguration().getDevice();
            switch(channelUID.getId()) {
            case CHANNEL_POWER:
                logger.trace("Received power command" + command);
                if (command == OnOffType.ON) {
                    device.powerOn();
                } else if (command == OnOffType.OFF) {
                    device.powerOff();
                } else if (command == RefreshType.REFRESH) {
                    updateState(PJLinkDeviceBindingConstants.CHANNEL_POWER,
                    PowerQueryResponseValue.POWER_ON.equals(device.getPowerStatus().getResult()) ? OnOffType.ON
                                    : OnOffType.OFF);
                } else {
                    logger.debug("Received unknown power command" + command);
                }
                break;
            case CHANNEL_INPUT:
            case CHANNEL_INPUT_DYNAMIC:
                if (command == RefreshType.REFRESH) {
                    StringType input = new StringType(device.getInputStatus().getResult().getValue());
                    updateState(PJLinkDeviceBindingConstants.CHANNEL_INPUT, input);
                    updateState(PJLinkDeviceBindingConstants.CHANNEL_INPUT_DYNAMIC, input);
                } else {
                    logger.trace("Received input command" + command);
                    Input input = new Input(((StringType) command).toString());
                    device.setInput(input);
                }
                break;
            case CHANNEL_AUDIO_MUTE:
            case CHANNEL_VIDEO_MUTE:
                boolean isAudioMute = channelUID.getId().equals(PJLinkDeviceBindingConstants.CHANNEL_AUDIO_MUTE);
                boolean isVideoMute = channelUID.getId().equals(PJLinkDeviceBindingConstants.CHANNEL_VIDEO_MUTE);
                if (isVideoMute || isAudioMute) {
                    if (command == RefreshType.REFRESH) {
                        // refresh both video and audio mute, as it's one request
                        MuteQueryResponseValue muteStatus = device.getMuteStatus();
                        updateState(PJLinkDeviceBindingConstants.CHANNEL_AUDIO_MUTE,
                                muteStatus.isAudioMuted() ? OnOffType.ON : OnOffType.OFF);
                        updateState(PJLinkDeviceBindingConstants.CHANNEL_VIDEO_MUTE,
                                muteStatus.isVideoMuted() ? OnOffType.ON : OnOffType.OFF);
                    } else {
                        if (isAudioMute) {
                            logger.trace("Received audio mute command" + command);
                            boolean muteOn = command == OnOffType.ON;
                            device.setMute(MuteInstructionChannel.AUDIO, muteOn);
                        }
                        if (isVideoMute) {
                            logger.trace("Received video mute command" + command);
                            boolean muteOn = command == OnOffType.ON;
                            device.setMute(MuteInstructionChannel.VIDEO, muteOn);
                        }
                    }
                }
            }

            updateStatus(ThingStatus.ONLINE);
            logger.trace("Successfully handled command {} on channel {}", command, channelUID.getId());
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (ResponseException e) {
            logger.error(e.getMessage());
        } catch (AuthenticationException e) {
            this.handleAuthenticationException(e);
        }

    }

    @Override
    public void initialize() {
        setupDevice();
        setupRefreshInterval();
    }

    protected PJLinkDeviceConfiguration getConfiguration() {
      PJLinkDeviceConfiguration config = this.config;
      if(config == null) {
        this.config = config = getConfigAs(PJLinkDeviceConfiguration.class);
      }
      return config;
    }

    private void clearRefreshInterval() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    private void handleAuthenticationException(AuthenticationException e) {
        updateProperty(PJLinkDeviceBindingConstants.PARAMETER_AUTHENTICATION_REQUIRED, new Boolean(true).toString());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING, e.getMessage());
    }

    private void setupDevice() {
        try {
            PJLinkDevice device = getConfiguration().getDevice();
            device.checkAvailability();

            updateDeviceProperties(device);
            updateInputChannelStates(device);
            updateStatus(ThingStatus.ONLINE);

        } catch (ResponseException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (AuthenticationException e) {
            this.handleAuthenticationException(e);
        }
    }

    private void setupRefreshInterval() {
        clearRefreshInterval();
        PJLinkDeviceConfiguration config = PJLinkDeviceHandler.this.getConfiguration();
        boolean atLeastOneChannelToBeRefreshed = config.refreshPower || config.refreshMute || config.refreshInputChannel;
        if (config.refresh > 0 && atLeastOneChannelToBeRefreshed) {
            refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refresh, TimeUnit.SECONDS);
        }
    }

    private void updateDeviceProperties(PJLinkDevice device) throws IOException, AuthenticationException {
        updateProperty(PJLinkDeviceBindingConstants.PARAMETER_AUTHENTICATION_REQUIRED,
                device.getAuthenticationRequired().toString());

        try {
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_NAME, device.getName());
        } catch (ResponseException e) {
            // okay, cannot retrieve model information
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_NAME, e.toString());
        }
        try {
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_MANUFACTURER, device.getManufacturer());
        } catch (ResponseException e) {
            // okay, cannot retrieve model information
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_MANUFACTURER, e.toString());
        }
        try {
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_MODEL, device.getModel());
        } catch (ResponseException e) {
            // okay, cannot retrieve model information
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_MODEL, e.toString());
        }
        try {
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_COMBINED_ID, device.getFullDescription());
        } catch (ResponseException e) {
            // okay, cannot retrieve model information
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_COMBINED_ID, e.toString());
        }
        try {
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_CLASS, device.getPJLinkClass());
        } catch (ResponseException e) {
            // okay, cannot retrieve class information
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_CLASS, e.toString());
        }
        try {
            Map<ErrorStatusDevicePart, ErrorStatusQueryResponseState> errorStatus = device.getErrorStatus();
            for (Map.Entry<ErrorStatusDevicePart, ErrorStatusQueryResponseState> entry : errorStatus.entrySet()) {
                String key = entry.getKey().getCamelCaseText();
                String value = entry.getValue().getText();
                updateProperty(PJLinkDeviceBindingConstants.PARAMETER_ERROR_STATUS + key, value);
            }
        } catch (ResponseException e) {
            // okay, cannot retrieve class information
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_ERROR_STATUS, e.toString());
        }
        try {
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_LAMP_HOURS, device.getLampHours());
        } catch (ResponseException e) {
            // okay, cannot retrieve class information
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_LAMP_HOURS, e.toString());
        }
        try {
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_OTHER_INFORMATION, device.getOtherInformation());
        } catch (ResponseException e) {
            // okay, cannot retrieve class information
            updateProperty(PJLinkDeviceBindingConstants.PARAMETER_OTHER_INFORMATION, e.toString());
        }
    }

    private void updateInputChannelStates(PJLinkDevice device)
            throws ResponseException, IOException, AuthenticationException {

        Set<Input> inputs = device.getAvailableInputs();
        // add our activities as channel state options
        List<StateOption> states = new LinkedList<>();
        for (Input input : inputs) {
            states.add(new StateOption(input.getPJLinkRepresentation(), input.getText()));
        }

        ChannelTypeUID channelTypeUID = new ChannelTypeUID(
                getThing().getUID() + ":" + PJLinkDeviceBindingConstants.CHANNEL_INPUT_DYNAMIC + "Type");
        ChannelType channelType = ChannelTypeBuilder.state(channelTypeUID, "Input", "String")
                .withDescription("Input channel")
                .withStateDescription(new StateDescription(null, null, null, "%s", false, states)).build();
        factory.addChannelType(channelType);

        ThingBuilder thingBuilder = editThing();

        // replace input channel with dynamic input channel
        thingBuilder
                .withoutChannel(new ChannelUID(this.getThing().getUID(), PJLinkDeviceBindingConstants.CHANNEL_INPUT))
                .withoutChannel(
                        new ChannelUID(this.getThing().getUID(), PJLinkDeviceBindingConstants.CHANNEL_INPUT_DYNAMIC));
        Channel channel = ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), PJLinkDeviceBindingConstants.CHANNEL_INPUT_DYNAMIC),
                        "String")
                .withDescription("Input").withType(channelTypeUID).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }
}
