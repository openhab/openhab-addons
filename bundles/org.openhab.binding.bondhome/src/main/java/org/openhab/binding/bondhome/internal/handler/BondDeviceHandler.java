/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bondhome.internal.handler;

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bondhome.internal.api.BondDevice;
import org.openhab.binding.bondhome.internal.api.BondDeviceAction;
import org.openhab.binding.bondhome.internal.api.BondDeviceProperties;
import org.openhab.binding.bondhome.internal.api.BondDeviceState;
import org.openhab.binding.bondhome.internal.api.BondDeviceType;
import org.openhab.binding.bondhome.internal.api.BondHttpApi;
import org.openhab.binding.bondhome.internal.config.BondDeviceConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BondDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class BondDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(BondDeviceHandler.class);

    private @NonNullByDefault({}) BondDeviceConfiguration config;
    private @Nullable BondHttpApi api;

    private @Nullable BondDevice deviceInfo;
    private @Nullable BondDeviceProperties deviceProperties;
    private @Nullable BondDeviceState deviceState;

    private @Nullable ScheduledFuture<?> pollingJob;

    private volatile boolean disposed;
    private volatile boolean fullyInitialized;

    private long latestUpdate = -1;

    /**
     * The supported thing types.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .of(THING_TYPE_BOND_FAN, THING_TYPE_BOND_SHADES, THING_TYPE_BOND_FIREPLACE, THING_TYPE_BOND_GENERIC)
            .collect(Collectors.toSet());

    public BondDeviceHandler(Thing thing) {
        super(thing);
        disposed = true;
        fullyInitialized = false;
        config = getConfigAs(BondDeviceConfiguration.class);
        logger.trace("Created handler for bond device with device id {}.", config.deviceId);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (hasConfigurationError() || disposed || !fullyInitialized) {
            logger.trace(
                    "Bond device handler for {} received command {} on channel {} but is not yet prepared to handle it.",
                    config.deviceId, command, channelUID);
            return;
        }

        logger.trace("Bond device handler for {} received command {} on channel {}", config.deviceId, command,
                channelUID);
        BondHttpApi api = this.api;
        if (api == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge API not available");
            // Re-attempt initialization
            scheduler.schedule(() -> {
                logger.trace("Re-attempting initialization");
                initialize();
            }, 30, TimeUnit.SECONDS);
            return;
        } else {
            if (command instanceof RefreshType) {
                long now = System.currentTimeMillis();
                long timePassedFromLastUpdateInSeconds = (now - latestUpdate) / 1000;
                if (latestUpdate < 0 || timePassedFromLastUpdateInSeconds > 15) {
                    logger.trace("Executing refresh command");
                    try {
                        deviceState = api.getDeviceState(config.deviceId);
                        updateChannelsFromState(deviceState);
                    } catch (IOException e) {
                        @Nullable
                        String errorMessage = e.getMessage();
                        if (errorMessage != null) {
                            if (errorMessage.contains(API_ERR_HTTP_401_UNAUTHORIZED)) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "Incorrect local token for Bond Bridge.");
                                setBridgeOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                                        "Incorrect local token for Bond Bridge.");
                            } else if (errorMessage.contains(API_ERR_HTTP_404_NOTFOUND)) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "No Bond device found with the given device id.");
                            } else {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
                            }
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        }
                    }
                } else {
                    logger.trace("It has been less than 15s since the last update.  Please retry soon.");
                }
                return;
            }

            switch (channelUID.getId()) {
                case CHANNEL_POWER_STATE:
                    logger.trace("Power state command");
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.TurnOn : BondDeviceAction.TurnOff, null);
                    break;

                case CHANNEL_STOP:
                    logger.trace("Stop command");
                    api.executeDeviceAction(config.deviceId, BondDeviceAction.Stop, null);
                    // Mark all the changing channels stopped
                    updateState(CHANNEL_LIGHT_START_STOP, OnOffType.OFF);
                    updateState(CHANNEL_LIGHT_DIRECTIONAL_INC, OnOffType.OFF);
                    updateState(CHANNEL_LIGHT_DIRECTIONAL_DECR, OnOffType.OFF);
                    updateState(CHANNEL_UP_LIGHT_START_STOP, OnOffType.OFF);
                    updateState(CHANNEL_UP_LIGHT_DIRECTIONAL_INC, OnOffType.OFF);
                    updateState(CHANNEL_UP_LIGHT_DIRECTIONAL_DECR, OnOffType.OFF);
                    updateState(CHANNEL_DOWN_LIGHT_START_STOP, OnOffType.OFF);
                    updateState(CHANNEL_DOWN_LIGHT_DIRECTIONAL_INC, OnOffType.OFF);
                    updateState(CHANNEL_DOWN_LIGHT_DIRECTIONAL_DECR, OnOffType.OFF);
                    break;

                case CHANNEL_FAN_SPEED:
                    logger.trace("Fan speed command");
                    if (command instanceof PercentType) {
                        int value = 1;
                        BondDeviceProperties devProperties = this.deviceProperties;
                        if (devProperties != null) {
                            int maxSpeed = devProperties.maxSpeed;
                            value = (int) Math.ceil(((PercentType) command).intValue() * maxSpeed / 100);
                        }
                        logger.trace("Fan speed command with speed set as {}", value);
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.SetSpeed, value);
                    } else if (command instanceof IncreaseDecreaseType) {
                        logger.trace("Fan increase/decrease speed command");
                        api.executeDeviceAction(config.deviceId,
                                ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE
                                        ? BondDeviceAction.IncreaseSpeed
                                        : BondDeviceAction.DecreaseSpeed),
                                null);
                    } else {
                        logger.info("Unsupported command on fan speed channel");
                    }
                    break;

                case CHANNEL_FAN_BREEZE_STATE:
                    logger.trace("Fan enable/disable breeze command");
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.BreezeOn : BondDeviceAction.BreezeOff, null);
                    break;

                case CHANNEL_FAN_BREEZE_MEAN:
                    // TODO(SRGDamia1): write array command fxn
                    logger.trace("Support for fan breeze settings not yet available");
                    break;

                case CHANNEL_FAN_BREEZE_VAR:
                    // TODO(SRGDamia1): write array command fxn
                    logger.trace("Support for fan breeze settings not yet available");
                    break;

                case CHANNEL_FAN_DIRECTION:
                    logger.trace("Fan direction command {}", command.toString());
                    if (command instanceof StringType) {
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.SetDirection,
                                command.toString().equals("winter") ? -1 : 1);
                    }
                    break;

                case CHANNEL_LIGHT_STATE:
                    logger.trace("Fan light state command");
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.TurnLightOn : BondDeviceAction.TurnLightOff,
                            null);
                    break;

                case CHANNEL_LIGHT_BRIGHTNESS:
                    if (command instanceof PercentType) {
                        PercentType pctCommand = (PercentType) command;
                        int value = pctCommand.intValue();
                        logger.trace("Fan light brightness command with value of {}", value);
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.SetBrightness, value);
                    } else if (command instanceof IncreaseDecreaseType) {
                        logger.trace("Fan light brightness increase/decrease command");
                        api.executeDeviceAction(config.deviceId,
                                ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE
                                        ? BondDeviceAction.IncreaseBrightness
                                        : BondDeviceAction.DecreaseBrightness),
                                null);
                        updateState(CHANNEL_STOP, OnOffType.ON);
                    } else {
                        logger.info("Unsupported command on fan light brightness channel");
                    }
                    break;

                case CHANNEL_LIGHT_START_STOP:
                    logger.trace("Fan light dimmer start/stop command");
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.StartDimmer : BondDeviceAction.Stop, null);
                    updateState(CHANNEL_STOP, OnOffType.ON);
                    // Unset in 30 seconds when this times out
                    scheduler.schedule(() -> {
                        logger.trace("Fan light dimmer start/stop command run for 30s");
                        updateState(CHANNEL_STOP, OnOffType.OFF);
                        updateState(CHANNEL_LIGHT_START_STOP, OnOffType.ON);
                    }, 30, TimeUnit.SECONDS);
                    break;

                case CHANNEL_LIGHT_DIRECTIONAL_INC:
                    logger.trace("Fan light brightness increase start/stop command");
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.StartIncreasingBrightness
                                    : BondDeviceAction.Stop,
                            null);
                    updateState(CHANNEL_STOP, OnOffType.ON);
                    // Unset in 30 seconds when this times out
                    scheduler.schedule(() -> {
                        logger.trace("Fan light brightness increase start/stop command run for 30s");
                        updateState(CHANNEL_STOP, OnOffType.OFF);
                        updateState(CHANNEL_LIGHT_DIRECTIONAL_INC, OnOffType.ON);
                    }, 30, TimeUnit.SECONDS);
                    break;

                case CHANNEL_LIGHT_DIRECTIONAL_DECR:
                    logger.trace("Fan light brightness decrease start/stop command");
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.StartDecreasingBrightness
                                    : BondDeviceAction.Stop,
                            null);
                    updateState(CHANNEL_STOP, OnOffType.ON);
                    // Unset in 30 seconds when this times out
                    scheduler.schedule(() -> {
                        logger.trace("Fan light brightness decrease start/stop command run for 30s");
                        updateState(CHANNEL_STOP, OnOffType.OFF);
                        updateState(CHANNEL_LIGHT_DIRECTIONAL_DECR, OnOffType.ON);
                    }, 30, TimeUnit.SECONDS);
                    break;

                case CHANNEL_UP_LIGHT_ENABLE:
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.TurnUpLightOn : BondDeviceAction.TurnUpLightOff,
                            null);
                    break;

                case CHANNEL_UP_LIGHT_STATE:
                    // To turn on the up light, we first have to enable it and then turn on the lights
                    api.executeDeviceAction(config.deviceId, BondDeviceAction.TurnUpLightOn, null);
                    if (command == OnOffType.ON) {
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.TurnLightOn, null);
                    } else {
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.TurnLightOff, null);
                    }
                    break;

                case CHANNEL_UP_LIGHT_BRIGHTNESS:
                    if (command instanceof PercentType) {
                        PercentType pctCommand = (PercentType) command;
                        int value = pctCommand.intValue();
                        logger.trace("Fan up light brightness command with value of {}", value);
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.SetUpLightBrightness, value);
                    } else if (command instanceof IncreaseDecreaseType) {
                        logger.trace("Fan uplight brightness increase/decrease command");
                        api.executeDeviceAction(config.deviceId,
                                ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE
                                        ? BondDeviceAction.IncreaseUpLightBrightness
                                        : BondDeviceAction.DecreaseUpLightBrightness),
                                null);
                    } else {
                        logger.info("Unsupported command on fan up light brightness channel");
                    }
                    break;

                case CHANNEL_UP_LIGHT_START_STOP:
                    logger.trace("Fan up light dimmer change command");
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.StartDimmer : BondDeviceAction.Stop, null);
                    updateState(CHANNEL_STOP, OnOffType.ON);
                    // Unset in 30 seconds when this times out
                    scheduler.schedule(() -> {
                        logger.trace("Fan up light dimmer change command run for 30s");
                        updateState(CHANNEL_STOP, OnOffType.OFF);
                        updateState(CHANNEL_UP_LIGHT_START_STOP, OnOffType.ON);
                    }, 30, TimeUnit.SECONDS);
                    break;

                case CHANNEL_UP_LIGHT_DIRECTIONAL_INC:
                case CHANNEL_UP_LIGHT_DIRECTIONAL_DECR:
                    // TODO(SRGDamia1): Command format not documented by Bond for up light directional brightness
                    logger.info("Bi-direction brightness control for up-lights not yet enabled!");
                    break;

                case CHANNEL_DOWN_LIGHT_ENABLE:
                    api.executeDeviceAction(config.deviceId, command == OnOffType.ON ? BondDeviceAction.TurnDownLightOn
                            : BondDeviceAction.TurnDownLightOff, null);
                    break;

                case CHANNEL_DOWN_LIGHT_STATE:
                    // To turn on the down light, we first have to enable it and then turn on the lights
                    api.executeDeviceAction(config.deviceId, BondDeviceAction.TurnDownLightOn, null);
                    if (command == OnOffType.ON) {
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.TurnLightOn, null);
                    } else {
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.TurnLightOff, null);
                    }
                    break;

                case CHANNEL_DOWN_LIGHT_BRIGHTNESS:
                    if (command instanceof PercentType) {
                        PercentType pctCommand = (PercentType) command;
                        int value = pctCommand.intValue();
                        logger.trace("Fan down light brightness command with value of {}", value);
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.SetDownLightBrightness, value);
                    } else if (command instanceof IncreaseDecreaseType) {
                        logger.trace("Fan down light brightness increase/decrease command");
                        api.executeDeviceAction(config.deviceId,
                                ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE
                                        ? BondDeviceAction.IncreaseDownLightBrightness
                                        : BondDeviceAction.DecreaseDownLightBrightness),
                                null);
                    } else {
                        logger.debug("Unsupported command on fan down light brightness channel");
                    }
                    break;

                case CHANNEL_DOWN_LIGHT_START_STOP:
                    logger.trace("Fan down light dimmer change command");
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.StartDimmer : BondDeviceAction.Stop, null);
                    updateState(CHANNEL_STOP, OnOffType.ON);
                    // Unset in 30 seconds when this times out
                    scheduler.schedule(() -> {
                        logger.trace("Fan down light dimmer change command run for 30s");
                        updateState(CHANNEL_STOP, OnOffType.OFF);
                        updateState(CHANNEL_DOWN_LIGHT_START_STOP, OnOffType.ON);
                    }, 30, TimeUnit.SECONDS);
                    break;

                case CHANNEL_DOWN_LIGHT_DIRECTIONAL_INC:
                case CHANNEL_DOWN_LIGHT_DIRECTIONAL_DECR:
                    // TODO(SRGDamia1): Command format not documented by Bond for down light directional brightness
                    logger.debug("Bi-direction brightness control for up-lights not yet enabled!");
                    break;

                case CHANNEL_FLAME:
                    if (command instanceof PercentType) {
                        PercentType pctCommand = (PercentType) command;
                        int value = pctCommand.intValue();
                        logger.trace("Fireplace flame command with value of {}", value);
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.SetFlame, value);
                    } else if (command instanceof IncreaseDecreaseType) {
                        logger.trace("Fireplace flame increase/decrease command");
                        api.executeDeviceAction(config.deviceId,
                                ((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE
                                        ? BondDeviceAction.IncreaseFlame
                                        : BondDeviceAction.DecreaseFlame),
                                null);
                    } else {
                        logger.info("Unsupported command on flame channel");
                    }
                    break;

                case CHANNEL_FP_FAN_STATE:
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.TurnFpFanOn : BondDeviceAction.TurnFpFanOff,
                            null);
                    break;

                case CHANNEL_FP_FAN_SPEED:
                    if (command instanceof PercentType) {
                        PercentType pctCommand = (PercentType) command;
                        int value = pctCommand.intValue();
                        logger.trace("Fireplace fan command with value of {}", value);
                        api.executeDeviceAction(config.deviceId, BondDeviceAction.SetFpFan, value);
                    } else {
                        logger.info("Unsupported command on fireplace fan channel");
                    }
                    break;

                case CHANNEL_OPEN_CLOSE:
                    api.executeDeviceAction(config.deviceId,
                            command == OnOffType.ON ? BondDeviceAction.Open : BondDeviceAction.Close, null);
                    break;

                case CHANNEL_HOLD:
                    api.executeDeviceAction(config.deviceId, BondDeviceAction.Hold, null);
                    break;

                case CHANNEL_PRESET:
                    api.executeDeviceAction(config.deviceId, BondDeviceAction.Preset, null);
                    break;

                default:
                    logger.info("Command {} on unknown channel {}, {}", command.toFullString(), channelUID.getId(),
                            channelUID.toString());
                    return;
            }
        }
    }

    @Override
    public void initialize() {
        logger.trace("Starting initialization for Bond device!");
        config = getConfigAs(BondDeviceConfiguration.class);
        fullyInitialized = false;
        disposed = false;

        // set the thing status to UNKNOWN temporarily
        updateStatus(ThingStatus.UNKNOWN);

        // Schedule initialization for a bit in the future to make sure the bridge finishes first
        scheduler.schedule((() -> {
            if (getBridgeAndAPI()) {
                initializeThing();
            }
        }), 15, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void dispose() {
        logger.debug("Disposing thing handler for {}.", this.getThing().getUID());
        // Mark handler as disposed as soon as possible to halt updates
        disposed = true;
        fullyInitialized = false;

        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
        }
        this.pollingJob = null;
    }

    private void initializeThing() {
        BondHttpApi api = this.api;
        if (api == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Bridge API not available");
            return;
        }

        try {
            logger.trace("Getting device information for {} ({})", config.deviceId, this.getThing().getLabel());
            deviceInfo = api.getDevice(config.deviceId);
            logger.trace("Getting device properties for {} ({})", config.deviceId, this.getThing().getLabel());
            deviceProperties = api.getDeviceProperties(config.deviceId);
        } catch (IOException e) {
            @Nullable
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains(API_ERR_HTTP_401_UNAUTHORIZED)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Incorrect local token for Bond Bridge.");
                    setBridgeOffline(ThingStatusDetail.CONFIGURATION_ERROR, "Incorrect local token for Bond Bridge.");
                } else if (errorMessage.contains(API_ERR_HTTP_404_NOTFOUND)) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No Bond device found with the given device id.");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        BondDevice devInfo = this.deviceInfo;
        BondDeviceProperties devProperties = this.deviceProperties;
        if (devInfo == null || devProperties == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to get device properties from Bond");
            return;
        }

        // Anytime the configuration has changed or the binding has been updated,
        // recreate the thing to make sure all possible channels are available
        // NOTE: This will cause the thing to be disposed and re-initialized
        if (wasBindingUpdated()) {
            recreateAllChannels(devInfo.type, devInfo.hash);
            return;
        }

        // Anytime the configuration has changed or the binding has been updated,
        // recreate the thing to make sure all possible channels are available
        // NOTE: This will cause the thing to be disposed and re-initialized
        if (wasThingUpdatedExternally(devInfo)) {
            recreateAllChannels(devInfo.type, devInfo.hash);
            return;
        }

        updateDevicePropertiesFromBond(devInfo, devProperties);

        deleteExtraChannels(devInfo.actions);

        startPollingJob();

        // Now we're online!
        updateStatus(ThingStatus.ONLINE);
        fullyInitialized = true;
        logger.debug("Finished initializing device!");
    }

    private void updateDevicePropertiesFromBond(BondDevice devInfo, BondDeviceProperties devProperties) {
        if (hasConfigurationError() || disposed) {
            logger.trace("Don't update properties, I've been disposed!");
            return;
        }

        // Update all the thing properties based on the result
        Map<String, String> thingProperties = new HashMap<String, String>();
        thingProperties.put(PROPERTIES_BINDING_VERSION, CURRENT_BINDING_VERSION);
        thingProperties.put(CONFIG_DEVICE_ID, config.deviceId);
        logger.trace("Updating device name to {}", devInfo.name);
        thingProperties.put(PROPERTIES_DEVICE_NAME, devInfo.name);
        logger.trace("Updating other device properties for {} ({})", config.deviceId, this.getThing().getLabel());
        thingProperties.put(PROPERTIES_TEMPLATE_NAME, devInfo.template);
        thingProperties.put(PROPERTIES_MAX_SPEED, String.valueOf(devProperties.maxSpeed));
        thingProperties.put(PROPERTIES_TRUST_STATE, String.valueOf(devProperties.trustState));
        thingProperties.put(PROPERTIES_ADDRESS, String.valueOf(devProperties.addr));
        thingProperties.put(PROPERTIES_RF_FREQUENCY, String.valueOf(devProperties.freq));
        logger.trace("Saving properties for {} ({})", config.deviceId, this.getThing().getLabel());
        updateProperties(thingProperties);
    }

    private synchronized void recreateAllChannels(BondDeviceType currentType, String currentHash) {
        if (hasConfigurationError() || disposed) {
            logger.trace("Don't recreate channels, I've been disposed!");
            return;
        }

        logger.debug("Recreating all possible channels for a {} for {} ({})",
                currentType.getThingTypeUID().getAsString(), config.deviceId, this.getThing().getLabel());

        // Create a new configuration
        final Map<String, Object> map = new HashMap<>();
        map.put(CONFIG_DEVICE_ID, config.deviceId);
        map.put(CONFIG_LATEST_HASH, currentHash);
        Configuration newConfiguration = new Configuration(map);

        // Change the thing type back to itself to force all channels to be re-created from XML
        changeThingType(currentType.getThingTypeUID(), newConfiguration);
    }

    private synchronized void deleteExtraChannels(BondDeviceAction[] currentActions) {
        if (hasConfigurationError() || disposed) {
            logger.trace("Don't delete channels, I've been disposed!");
            return;
        }

        logger.trace("Deleting channels based on the available actions");
        // Get the thing to edit
        ThingBuilder thingBuilder = editThing();

        // Now, look at the whole list of possible channels
        List<BondDeviceAction> availableActions = Arrays.asList(currentActions);
        List<Channel> possibleChannels = this.getThing().getChannels();
        List<String> availableChannelIds = new ArrayList<>();
        // Always have the last update time channel
        availableChannelIds.add(CHANNEL_LAST_UPDATE);

        for (BondDeviceAction action : availableActions) {
            if (action != null) {
                String actionType = action.getChannelTypeId();
                if (actionType != null) {
                    availableChannelIds.add(actionType);
                    logger.trace(" Action: {}, Relevant Channel Type Id: {}", action.getActionId(), actionType);
                }
            }
        }

        for (Channel channel : possibleChannels) {
            if (availableChannelIds.contains(channel.getUID().getId())) {
                logger.trace("      ++++ Keeping: {}", channel.getUID().getId());
            } else {
                thingBuilder.withoutChannel(channel.getUID());
                logger.trace("      ---- Dropping: {}", channel.getUID().getId());
            }
        }

        // Add all the channels
        logger.trace("Saving the thing with extra channels removed");
        updateThing(thingBuilder.build());
    }

    public String getDeviceId() {
        return config.deviceId;
    }

    public synchronized void updateChannelsFromState(@Nullable BondDeviceState updateState) {
        if (hasConfigurationError() || disposed) {
            return;
        }

        if (updateState != null) {
            logger.debug("Updating channels from state for {} ({})", config.deviceId, this.getThing().getLabel());

            updateStatus(ThingStatus.ONLINE);
            updateState(CHANNEL_LAST_UPDATE, new DateTimeType());
            latestUpdate = System.currentTimeMillis();
            logger.trace("Update Time for {}: {}", this.getThing().getLabel(), (new DateTimeType()).toFullString());

            updateState(CHANNEL_POWER_STATE, updateState.power == 0 ? OnOffType.OFF : OnOffType.ON);
            updateState("timer", new DecimalType(updateState.timer));
            int value = 1;
            BondDeviceProperties devProperties = this.deviceProperties;
            if (devProperties != null) {
                double maxSpeed = devProperties.maxSpeed;
                value = (int) (((double) updateState.speed / maxSpeed) * 100);
                logger.trace("Raw fan speed: {}, Percent: {}", updateState.speed, value);
            } else if (updateState.speed != 0 && this.getThing().getThingTypeUID().equals(THING_TYPE_BOND_FAN)) {
                logger.info("Unable to convert fan speed to a percent for {}!", this.getThing().getLabel());
            }
            updateState(CHANNEL_FAN_SPEED, new PercentType(value));
            updateState(CHANNEL_FAN_BREEZE_STATE, updateState.breeze[0] == 0 ? OnOffType.OFF : OnOffType.ON);
            updateState(CHANNEL_FAN_BREEZE_MEAN, new DecimalType(updateState.breeze[1]));
            updateState(CHANNEL_FAN_BREEZE_VAR, new DecimalType(updateState.breeze[2]));
            updateState(CHANNEL_FAN_DIRECTION,
                    updateState.direction == 1 ? new StringType("summer") : new StringType("winter"));
            updateState(CHANNEL_TIMER, new DecimalType(updateState.timer));

            updateState(CHANNEL_LIGHT_STATE, updateState.light == 0 ? OnOffType.OFF : OnOffType.ON);
            updateState(CHANNEL_LIGHT_BRIGHTNESS, new DecimalType(updateState.brightness));

            updateState(CHANNEL_UP_LIGHT_ENABLE, updateState.upLight == 0 ? OnOffType.OFF : OnOffType.ON);
            updateState(CHANNEL_UP_LIGHT_STATE,
                    (updateState.upLight == 1 && updateState.light == 1) ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_UP_LIGHT_BRIGHTNESS, new DecimalType(updateState.upLightBrightness));

            updateState(CHANNEL_DOWN_LIGHT_ENABLE, updateState.downLight == 0 ? OnOffType.OFF : OnOffType.ON);
            updateState(CHANNEL_DOWN_LIGHT_STATE,
                    (updateState.downLight == 1 && updateState.light == 1) ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_DOWN_LIGHT_BRIGHTNESS, new DecimalType(updateState.downLightBrightness));

            updateState(CHANNEL_FLAME, new DecimalType(updateState.flame));
            updateState(CHANNEL_FP_FAN_STATE, updateState.fpfanPower == 0 ? OnOffType.OFF : OnOffType.ON);
            updateState(CHANNEL_FP_FAN_SPEED, new DecimalType(updateState.fpfanSpeed));

            updateState(CHANNEL_OPEN_CLOSE, updateState.open == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN);

            // Mark all the stateless channels stopped
            updateState(CHANNEL_LIGHT_START_STOP, OnOffType.OFF);
            updateState(CHANNEL_LIGHT_DIRECTIONAL_INC, OnOffType.OFF);
            updateState(CHANNEL_LIGHT_DIRECTIONAL_DECR, OnOffType.OFF);
            updateState(CHANNEL_UP_LIGHT_START_STOP, OnOffType.OFF);
            updateState(CHANNEL_UP_LIGHT_DIRECTIONAL_INC, OnOffType.OFF);
            updateState(CHANNEL_UP_LIGHT_DIRECTIONAL_DECR, OnOffType.OFF);
            updateState(CHANNEL_DOWN_LIGHT_START_STOP, OnOffType.OFF);
            updateState(CHANNEL_DOWN_LIGHT_DIRECTIONAL_INC, OnOffType.OFF);
            updateState(CHANNEL_DOWN_LIGHT_DIRECTIONAL_DECR, OnOffType.OFF);
            updateState(CHANNEL_STOP, OnOffType.OFF);
            updateState(CHANNEL_HOLD, OnOffType.OFF);

        } else {
            logger.debug("No state information provided to update channels with");
        }
    }

    private boolean hasConfigurationError() {
        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        return statusInfo.getStatus() == ThingStatus.OFFLINE
                && statusInfo.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR;
    }

    private synchronized boolean wasBindingUpdated() {
        // Check if the binding has been updated
        boolean updatedBinding = true;
        @Nullable
        String lastBindingVersion = this.getThing().getProperties().get(PROPERTIES_BINDING_VERSION);
        updatedBinding = !CURRENT_BINDING_VERSION.equals(lastBindingVersion);
        if (updatedBinding) {
            logger.info("Bond Home binding has been updated.");
            logger.info("Current version is {}, prior version was {}.", CURRENT_BINDING_VERSION, lastBindingVersion);

            // Update the thing with the new property value
            final Map<String, String> newProperties = new HashMap<>(thing.getProperties());
            newProperties.put(PROPERTIES_BINDING_VERSION, CURRENT_BINDING_VERSION);

            final ThingBuilder thingBuilder = editThing();
            thingBuilder.withProperties(newProperties);
            updateThing(thingBuilder.build());
        }
        return updatedBinding;
    }

    private synchronized boolean wasThingUpdatedExternally(BondDevice devInfo) {
        // Check if the Bond hash tree has changed
        config = getConfigAs(BondDeviceConfiguration.class);
        final String lastDeviceConfigurationHash = config.lastDeviceConfigurationHash;
        boolean updatedHashTree = !devInfo.hash.equals(lastDeviceConfigurationHash);
        if (updatedHashTree) {
            logger.info("Hash tree of device has been updated by Bond.");
            logger.info("Current state is {}, prior state was {}.", devInfo.hash, lastDeviceConfigurationHash);
        }
        return updatedHashTree;
    }

    private boolean getBridgeAndAPI() {
        Bridge myBridge = this.getBridge();
        if (myBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No Bond bridge is associated with this Bond device");

            return false;
        } else {
            BondBridgeHandler myBridgeHandler = (BondBridgeHandler) myBridge.getHandler();
            if (myBridgeHandler != null) {
                this.api = myBridgeHandler.getBridgeAPI();
                return true;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Cannot access API for Bridge associated with this Bond device");
                return false;
            }
        }
    }

    private void setBridgeOffline(ThingStatusDetail detail, String description) {
        Bridge myBridge = this.getBridge();
        if (myBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No Bond bridge is associated with this Bond device");
            logger.error("No Bond bridge is associated with this Bond device - cannot create device!");
            return;
        } else {
            BondBridgeHandler myBridgeHandler = (BondBridgeHandler) myBridge.getHandler();
            if (myBridgeHandler != null) {
                myBridgeHandler.setBridgeOffline(detail, description);
                return;
            }
        }
    }

    // Start polling for state
    private synchronized void startPollingJob() {
        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob == null || pollingJob.isCancelled()) {
            Runnable pollingCommand = () -> {
                BondHttpApi api = this.api;
                if (api == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Bridge API not available");
                    initialize();
                    return;
                } else {
                    logger.trace("Polling for current state for {} ({})", config.deviceId, this.getThing().getLabel());
                    try {
                        deviceState = api.getDeviceState(config.deviceId);
                        updateChannelsFromState(deviceState);
                    } catch (IOException e) {
                        @Nullable
                        String errorMessage = e.getMessage();
                        if (errorMessage != null) {
                            if (errorMessage.contains(API_ERR_HTTP_401_UNAUTHORIZED)) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "Incorrect local token for Bond Bridge.");
                                setBridgeOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                                        "Incorrect local token for Bond Bridge.");
                            } else if (errorMessage.contains(API_ERR_HTTP_404_NOTFOUND)) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "No Bond device found with the given device id.");
                            }
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    }
                }
            };
            this.pollingJob = scheduler.scheduleWithFixedDelay(pollingCommand, 60, 300, TimeUnit.SECONDS);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            // restart the polling job when the bridge goes back online
            startPollingJob();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            // stop the polling job when the bridge goes offline
            ScheduledFuture<?> pollingJob = this.pollingJob;
            if (pollingJob != null) {
                pollingJob.cancel(true);
                this.pollingJob = null;
            }
        }
    }
}
