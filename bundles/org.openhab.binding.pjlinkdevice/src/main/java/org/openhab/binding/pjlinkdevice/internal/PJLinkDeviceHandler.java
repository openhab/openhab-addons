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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AuthenticationException;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;
import org.openhab.binding.pjlinkdevice.internal.device.command.input.Input;
import org.openhab.binding.pjlinkdevice.internal.device.command.mute.MuteInstructionCommand.MuteInstructionChannel;
import org.openhab.binding.pjlinkdevice.internal.device.command.mute.MuteQueryResponse.MuteQueryResponseValue;
import org.openhab.binding.pjlinkdevice.internal.device.command.power.PowerQueryResponse.PowerQueryResponseValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The {@link PJLinkDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class PJLinkDeviceHandler extends BaseThingHandler {

  private @Nullable PJLinkDeviceConfiguration config;

  private @Nullable PJLinkDevice device;

  private InputChannelStateDescriptionProvider stateDescriptionProvider;

  private final Logger logger = LoggerFactory.getLogger(PJLinkDeviceHandler.class);

  private @Nullable ScheduledFuture<?> refreshJob;

  private @Nullable ScheduledFuture<?> setupJob;

  public PJLinkDeviceHandler(Thing thing, InputChannelStateDescriptionProvider stateDescriptionProvider) {
    super(thing);
    this.stateDescriptionProvider = stateDescriptionProvider;
  }

  @Override
  public void dispose() {
    clearSetupJob();

    clearRefreshInterval();

    final PJLinkDevice device = this.device;
    if (device != null) {
      device.dispose();
    }

    this.config = null;
    this.device = null;
  }

  public void refresh(PJLinkDeviceConfiguration config) {
    // Do not poll if configuration is incomplete
    if (PJLinkDeviceHandler.this.getThing().getStatusInfo()
        .getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR) {
      PJLinkDeviceHandler.this.logger.debug("Polling device status...");
      if (config.refreshPower) {
        PJLinkDeviceHandler.this.handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_POWER), RefreshType.REFRESH);
      }
      if (config.refreshMute) {
        // this updates both CHANNEL_AUDIO_MUTE and CHANNEL_VIDEO_MUTE
        PJLinkDeviceHandler.this.handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_AUDIO_MUTE),
            RefreshType.REFRESH);
      }
      if (config.refreshInputChannel) {
        PJLinkDeviceHandler.this.handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_INPUT), RefreshType.REFRESH);
      }
    }
  }

  public PJLinkDevice getDevice() throws UnknownHostException, ConfigurationException {
    PJLinkDevice device = this.device;
    if (device == null) {
      PJLinkDeviceConfiguration config = getConfiguration();
      this.device = device = new PJLinkDevice(config.tcpPort, InetAddress.getByName(config.ipAddress),
          config.adminPassword);
    }
    return device;
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    logger.trace("Received command {} on channel {}", command, channelUID.getId());
    try {
      PJLinkDevice device = getDevice();
      switch (channelUID.getId()) {
        case CHANNEL_POWER:
          logger.trace("Received power command {}", command);
          if (command == OnOffType.ON) {
            device.powerOn();
          } else if (command == OnOffType.OFF) {
            device.powerOff();
          } else if (command == RefreshType.REFRESH) {
            updateState(PJLinkDeviceBindingConstants.CHANNEL_POWER,
                PowerQueryResponseValue.POWER_ON.equals(device.getPowerStatus().getResult()) ? OnOffType.ON
                    : OnOffType.OFF);
          } else {
            logger.debug("Received unknown power command {}", command);
          }
          break;
        case CHANNEL_INPUT:
          if (command == RefreshType.REFRESH) {
            StringType input = new StringType(device.getInputStatus().getResult().getValue());
            updateState(PJLinkDeviceBindingConstants.CHANNEL_INPUT, input);
          } else if (command instanceof StringType) {
            logger.trace("Received input command {}", command);
            Input input = new Input(((StringType) command).toString());
            device.setInput(input);
          } else {
            logger.debug("Received unknown channel command {}", command);
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
                logger.trace("Received audio mute command {}", command);
                boolean muteOn = command == OnOffType.ON;
                device.setMute(MuteInstructionChannel.AUDIO, muteOn);
              }
              if (isVideoMute) {
                logger.trace("Received video mute command {}", command);
                boolean muteOn = command == OnOffType.ON;
                device.setMute(MuteInstructionChannel.VIDEO, muteOn);
              }
            }
          }
      }

      updateStatus(ThingStatus.ONLINE);
      logger.trace("Successfully handled command {} on channel {}", command, channelUID.getId());
    } catch (IOException | ResponseException e) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
    } catch (ConfigurationException e) {
      updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
    } catch (AuthenticationException e) {
      handleAuthenticationException(e);
    }
  }

  @Override
  public void initialize() {
    this.setupJob = scheduler.schedule(() -> {
      try {
        setupDevice();
        setupRefreshInterval();
      } catch (ResponseException | IOException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
      } catch (AuthenticationException e) {
        handleAuthenticationException(e);
      } catch (ConfigurationException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
      }
    }, 0, TimeUnit.SECONDS);
  }

  protected PJLinkDeviceConfiguration getConfiguration() throws ConfigurationException {
    PJLinkDeviceConfiguration config = this.config;
    try {
      validateConfigurationParameters(getThing().getConfiguration().getProperties());
    } catch (ConfigValidationException e) {
      String message = e.getValidationMessages().entrySet().stream()
          .map((Map.Entry<String, String> a) -> (a.getKey() + ": " + a.getValue())).collect(Collectors.joining("; "));
      throw new ConfigurationException(message);
    }
    if (config == null) {
      this.config = config = getConfigAs(PJLinkDeviceConfiguration.class);
    }
    return config;
  }

  private void clearSetupJob() {
    final ScheduledFuture<?> setupJob = this.setupJob;
    if (setupJob != null) {
      setupJob.cancel(true);
      this.setupJob = null;
    }
  }

  private void clearRefreshInterval() {
    final ScheduledFuture<?> refreshJob = this.refreshJob;
    if (refreshJob != null) {
      refreshJob.cancel(true);
      this.refreshJob = null;
    }
  }

  private void handleAuthenticationException(AuthenticationException e) {
    updateProperty(PJLinkDeviceBindingConstants.PROPERTY_AUTHENTICATION_REQUIRED, Boolean.TRUE.toString());
    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
  }

  private void setupDevice() throws ConfigurationException, IOException, AuthenticationException, ResponseException {
    PJLinkDevice device = getDevice();
    device.checkAvailability();

    updateDeviceProperties(device);
    updateInputChannelStates(device);
    updateStatus(ThingStatus.ONLINE);
  }

  private void setupRefreshInterval() throws ConfigurationException {
    clearRefreshInterval();
    PJLinkDeviceConfiguration config = PJLinkDeviceHandler.this.getConfiguration();
    boolean atLeastOneChannelToBeRefreshed = config.refreshPower || config.refreshMute || config.refreshInputChannel;
    if (config.refresh > 0 && atLeastOneChannelToBeRefreshed) {
      refreshJob = scheduler.scheduleWithFixedDelay(() -> refresh(config), 0, config.refresh, TimeUnit.SECONDS);
    }
  }

  private void updateDeviceProperties(PJLinkDevice device) throws IOException, AuthenticationException {
    Map<String, String> properties = editProperties();

    properties.put(PJLinkDeviceBindingConstants.PROPERTY_AUTHENTICATION_REQUIRED,
        device.getAuthenticationRequired().toString());

    try {
      properties.put(PJLinkDeviceBindingConstants.PROPERTY_NAME, device.getName());
    } catch (ResponseException e) {
      logger.debug("Error retrieving property {}", PJLinkDeviceBindingConstants.PROPERTY_NAME, e);
    }
    try {
      properties.put(Thing.PROPERTY_VENDOR, device.getManufacturer());
    } catch (ResponseException e) {
      logger.debug("Error retrieving property {}", Thing.PROPERTY_VENDOR, e);
    }
    try {
      properties.put(Thing.PROPERTY_MODEL_ID, device.getModel());
    } catch (ResponseException e) {
      logger.debug("Error retrieving property {}", Thing.PROPERTY_MODEL_ID, e);
    }
    try {
      properties.put(PJLinkDeviceBindingConstants.PROPERTY_CLASS, device.getPJLinkClass());
    } catch (ResponseException e) {
      logger.debug("Error retrieving property {}", PJLinkDeviceBindingConstants.PROPERTY_CLASS, e);
    }
    try {
      device.getErrorStatus()
        .forEach((k,v) -> properties.put(PJLinkDeviceBindingConstants.PROPERTY_ERROR_STATUS + k.getCamelCaseText(), v.getText()));
    } catch (ResponseException e) {
      logger.debug("Error retrieving property {}", PJLinkDeviceBindingConstants.PROPERTY_ERROR_STATUS, e);
    }
    try {
      properties.put(PJLinkDeviceBindingConstants.PROPERTY_LAMP_HOURS, device.getLampHours());
    } catch (ResponseException e) {
      logger.debug("Error retrieving property {}", PJLinkDeviceBindingConstants.PROPERTY_LAMP_HOURS, e);
    }
    try {
      properties.put(PJLinkDeviceBindingConstants.PROPERTY_OTHER_INFORMATION, device.getOtherInformation());
    } catch (ResponseException e) {
      logger.debug("Error retrieving property {}", PJLinkDeviceBindingConstants.PROPERTY_OTHER_INFORMATION,
          e);
    }

    updateProperties(properties);
  }

  private void updateInputChannelStates(PJLinkDevice device)
      throws ResponseException, IOException, AuthenticationException {
    Set<Input> inputs = device.getAvailableInputs();
    List<StateOption> states = new LinkedList<>();
    for (Input input : inputs) {
      states.add(new StateOption(input.getPJLinkRepresentation(), input.getText()));
    }

    ChannelUID channelUid = new ChannelUID(getThing().getUID(), PJLinkDeviceBindingConstants.CHANNEL_INPUT);
    this.stateDescriptionProvider.setStateOptions(channelUid, states);
  }
}
