/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.handler;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ABORT_PROGRAM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ACTIVE_PROGRAM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_OVEN_CAVITY_LIGHT_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_OVEN_DOOR_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_OVEN_DURATION;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_OVEN_MEAT_PROBE_PLUGGED_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_OVEN_PROGRAM_COMMAND;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_OVEN_SET_POINT_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_OVEN_TEMPERATURE_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_DOOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_OVEN_CAVITY_LIGHT;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_OVEN_CURRENT_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_OVEN_MEAT_PROBE_PLUGGED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COMMAND_PAUSE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COMMAND_RESUME;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COMMAND_START;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COMMAND_STOP;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DOOR_STATE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_OVEN_CAVITY_LIGHT;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_OVEN_CURRENT_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_OVEN_DOOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_OVEN_MEAT_PROBE_PLUGGED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_PAUSE_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_RESUME_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_START_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_STOP_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.NUMBER_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OPERATION_STATE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_CAVITY_LIGHT_KEY_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_CAVITY_SELECTOR_ENUM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_CURRENT_MEAT_PROBE_TEMPERATURE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_CURRENT_MEAT_PROBE_TEMPERATURE_KEY_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_CURRENT_TEMPERATURE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_CURRENT_TEMPERATURE_KEY_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_DOOR_STATE_KEY_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_DURATION_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_MEAT_PROBE_PLUGGED_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_MEAT_PROBE_PLUGGED_KEY_TEMPLATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.OVEN_SET_POINT_TEMPERATURE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.PAUSE_PROGRAM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.RESUME_PROGRAM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SELECTED_PROGRAM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_AJAR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_NO_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_OPEN;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_RUN;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_ACTIVE_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_ALL_MANDATORY_VALUES;
import static org.openhab.core.library.unit.ImperialUnits.FAHRENHEIT;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.SECOND;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.configuration.HomeConnectDirectConfiguration;
import org.openhab.binding.homeconnectdirect.internal.handler.model.DynamicChannel;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.i18n.HomeConnectDirectTranslationProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicCommandDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicStateDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.DeviceDescriptionService;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ContentType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescriptionType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.Enumeration;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.profile.ApplianceProfileService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.ProgramData;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectDirectOvenHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDirectOvenHandler extends BaseHomeConnectDirectHandler {

    private static final String DEFAULT_CAVITY_NAME = "Main";
    private static final int DEFAULT_CAVITY_INDEX = 0;
    private static final long POLLING_INTERVAL_RUN_SECONDS = 60;
    private static final long POLLING_INTERVAL_COOLDOWN_SECONDS = 300;
    private static final long POLLING_INTERVAL_IDLE_SECONDS = 900;
    private static final int TEMPERATURE_THRESHOLD_HIGH = 50;
    private static final int TEMPERATURE_THRESHOLD_LOW = 30;

    private final Logger logger;
    private final CopyOnWriteArraySet<DynamicChannel> doorChannels;
    private final CopyOnWriteArraySet<DynamicChannel> currentTemperatureChannels;
    private final CopyOnWriteArraySet<DynamicChannel> meatProbeChannels;
    private final CopyOnWriteArraySet<DynamicChannel> lightChannels;
    private final CopyOnWriteArraySet<DynamicChannel> meatProbePluggedChannels;
    private final AtomicInteger maxCavityTemperature;

    private @Nullable ScheduledFuture<?> pollingFuture;
    private long currentPollingIntervalSeconds;

    public HomeConnectDirectOvenHandler(Thing thing, ApplianceProfileService applianceProfileService,
            HomeConnectDirectDynamicCommandDescriptionProvider commandDescriptionProvider,
            HomeConnectDirectDynamicStateDescriptionProvider stateDescriptionProvider, String deviceId,
            HomeConnectDirectConfiguration configuration, HomeConnectDirectTranslationProvider translationProvider) {
        super(thing, applianceProfileService, commandDescriptionProvider, stateDescriptionProvider, deviceId,
                configuration, translationProvider);

        this.logger = LoggerFactory.getLogger(HomeConnectDirectOvenHandler.class);
        this.doorChannels = new CopyOnWriteArraySet<>();
        this.currentTemperatureChannels = new CopyOnWriteArraySet<>();
        this.meatProbeChannels = new CopyOnWriteArraySet<>();
        this.lightChannels = new CopyOnWriteArraySet<>();
        this.meatProbePluggedChannels = new CopyOnWriteArraySet<>();
        this.maxCavityTemperature = new AtomicInteger(0);
    }

    @Override
    protected void initializeStarted() {
        addCavityChannels();
    }

    @Override
    protected void initializeFinished() {
        initializeAllStates();
        updateValuesPolling();
    }

    @Override
    public void dispose() {
        stopValuesPolling();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_OVEN_DURATION.equals(channelUID.getId()) && command instanceof QuantityType<?> quantity) {
            var durationQuantityType = quantity.toUnit(SECOND);
            if (durationQuantityType != null) {
                sendIntegerOptionIfAllowed(durationQuantityType, OVEN_DURATION_KEY);
            } else {
                logger.warn("Could not set duration! uid={}", getThing().getUID());
            }
        } else if (CHANNEL_OVEN_SET_POINT_TEMPERATURE.equals(channelUID.getId())
                && (command instanceof QuantityType<?> || command instanceof DecimalType)) {
            var unit = getTemperatureUnitOfOption(OVEN_SET_POINT_TEMPERATURE_KEY);
            QuantityType<?> quantity = (command instanceof QuantityType<?> qt) ? qt
                    : new QuantityType<>(((DecimalType) command), unit);
            var temperatureQuantityType = quantity.toUnit(unit);
            if (temperatureQuantityType != null) {
                sendIntegerOptionIfAllowed(temperatureQuantityType, OVEN_SET_POINT_TEMPERATURE_KEY);
            } else {
                logger.warn("Could not set temperature! uid={}", getThing().getUID());
            }
        } else if (CHANNEL_OVEN_PROGRAM_COMMAND.equals(channelUID.getId()) && command instanceof StringType) {
            if (COMMAND_START.equalsIgnoreCase(command.toFullString())) {
                var selectedProgram = getKeyValueStore().get(SELECTED_PROGRAM_KEY);
                if (selectedProgram != null) {
                    getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                        if (deviceDescriptionService.getActiveProgram(true) != null) {
                            mapProgramKey(selectedProgram).ifPresent(programUid -> send(Action.POST, RO_ACTIVE_PROGRAM,
                                    List.of(new ProgramData(programUid, null)), null, 1));
                        } else {
                            logger.info(
                                    "The '{}' control is either unavailable or in read-only mode. Cannot start program.",
                                    ACTIVE_PROGRAM_KEY);
                        }
                    });

                }
            } else if (COMMAND_PAUSE.equalsIgnoreCase(command.toFullString())) {
                sendBooleanCommandIfAllowed(PAUSE_PROGRAM_KEY);
            } else if (COMMAND_RESUME.equalsIgnoreCase(command.toFullString())) {
                sendBooleanCommandIfAllowed(RESUME_PROGRAM_KEY);
            } else if (COMMAND_STOP.equalsIgnoreCase(command.toFullString())) {
                sendBooleanCommandIfAllowed(ABORT_PROGRAM_KEY);
            }
        }

        // dynamic stuff
        lightChannels.stream().filter(dynamicChannel -> channelUID.getId().equals(dynamicChannel.channelName()))
                .forEach(dynamicChannel -> sendBooleanSettingIfAllowed(command, dynamicChannel.key()));
    }

    @Override
    protected void onApplianceDescriptionChangeEvent(List<DeviceDescriptionChange> deviceDescriptionChanges) {
        super.onApplianceDescriptionChangeEvent(deviceDescriptionChanges);

        deviceDescriptionChanges.forEach(deviceDescriptionChange -> {
            if (OVEN_DURATION_KEY.equals(deviceDescriptionChange.key())) {
                updateIntegerOptionDescriptionIfLinked(CHANNEL_OVEN_DURATION, deviceDescriptionChange.key());
                getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                    var durationOption = deviceDescriptionService.getOption(deviceDescriptionChange.key(), true, true,
                            false);
                    if (durationOption == null) {
                        updateStateIfLinked(CHANNEL_OVEN_DURATION, UnDefType.UNDEF);
                    }
                });
            } else if (OVEN_SET_POINT_TEMPERATURE_KEY.equals(deviceDescriptionChange.key())) {
                updateIntegerOptionDescriptionIfLinked(CHANNEL_OVEN_SET_POINT_TEMPERATURE,
                        deviceDescriptionChange.key());
                getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                    var setPointOption = deviceDescriptionService.getOption(deviceDescriptionChange.key(), true, true,
                            false);
                    if (setPointOption == null) {
                        updateStateIfLinked(CHANNEL_OVEN_SET_POINT_TEMPERATURE, UnDefType.UNDEF);
                    }
                });
            } else if (DeviceDescriptionType.COMMAND.equals(deviceDescriptionChange.type())
                    || DeviceDescriptionType.COMMAND_LIST.equals(deviceDescriptionChange.type())) {
                updateProgramCommandDescription();
            }

            // if temperature isn't available -> set to UNDEF
            getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> Stream
                    .of(currentTemperatureChannels, meatProbeChannels).flatMap(Set::stream)
                    .filter(dynamicChannel -> deviceDescriptionChange.key().equals(dynamicChannel.key()))
                    .filter(dynamicChannel -> !deviceDescriptionService
                            .isStatusAvailableAndReadable(dynamicChannel.key()))
                    .forEach(dynamicChannel -> updateStateIfLinked(dynamicChannel.channelName(), UnDefType.UNDEF)));
        });
    }

    @Override
    protected void onApplianceValueEvent(Value value, Resource resource) {
        super.onApplianceValueEvent(value, resource);

        switch (value.key()) {
            case OVEN_DURATION_KEY ->
                updateStateIfLinked(CHANNEL_OVEN_DURATION, () -> new QuantityType<>(value.getValueAsInt(), SECOND));
            case OVEN_SET_POINT_TEMPERATURE_KEY -> updateStateIfLinked(CHANNEL_OVEN_SET_POINT_TEMPERATURE,
                    () -> new QuantityType<>(value.getValueAsInt(), getTemperatureUnitOfOption(value.key())));
            case SELECTED_PROGRAM_KEY, ACTIVE_PROGRAM_KEY -> updateProgramCommandDescription();
            case OPERATION_STATE_KEY -> updateValuesPolling();
        }

        // track current cavity temperature and re-evaluate polling
        if (currentTemperatureChannels.stream().anyMatch(dc -> value.key().equals(dc.key()))) {
            maxCavityTemperature.set(value.getValueAsInt());
            updateValuesPolling();
        }

        // dynamic stuff
        doorChannels.forEach(dynamicChannel -> {
            if (value.key().equals(dynamicChannel.key())) {
                updateStateIfLinked(dynamicChannel.channelName(),
                        () -> STATE_OPEN.equals(value.value()) || STATE_AJAR.equals(value.value()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
            }
        });
        Stream.of(currentTemperatureChannels, meatProbeChannels).flatMap(Set::stream)
                .filter(dynamicChannel -> value.key().equals(dynamicChannel.key())).forEach(dynamicChannel -> {
                    var unit = ContentType.TEMPERATURE_CELSIUS.equals(dynamicChannel.contentType()) ? CELSIUS
                            : FAHRENHEIT;
                    updateStateIfLinked(dynamicChannel.channelName(), new QuantityType<>(value.getValueAsInt(), unit));
                });
        Stream.of(lightChannels, meatProbePluggedChannels).flatMap(Set::stream)
                .filter(dynamicChannel -> value.key().equals(dynamicChannel.key()))
                .forEach(dynamicChannel -> updateStateIfLinked(dynamicChannel.channelName(),
                        () -> OnOffType.from(value.getValueAsBoolean())));
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        initializeState(channelUID.getId());
    }

    private void initializeAllStates() {
        initializeState(CHANNEL_OVEN_DURATION);
        initializeState(CHANNEL_OVEN_SET_POINT_TEMPERATURE);
        initializeState(CHANNEL_OVEN_PROGRAM_COMMAND);
        Stream.of(doorChannels, currentTemperatureChannels, meatProbeChannels, lightChannels, meatProbePluggedChannels)
                .flatMap(Set::stream).map(DynamicChannel::channelName).forEach(this::initializeState);
    }

    private void initializeState(String channelId) {
        if (CHANNEL_OVEN_DURATION.equals(channelId)) {
            updateIntegerOptionDescriptionIfLinked(channelId, OVEN_DURATION_KEY);
            updateStateIfLinked(channelId, UnDefType.UNDEF);
        } else if (CHANNEL_OVEN_SET_POINT_TEMPERATURE.equals(channelId)) {
            updateIntegerOptionDescriptionIfLinked(channelId, OVEN_SET_POINT_TEMPERATURE_KEY);
            updateStateIfLinked(channelId, UnDefType.UNDEF);
        } else if (CHANNEL_OVEN_PROGRAM_COMMAND.equals(channelId)) {
            updateProgramCommandDescription();
        } else if (isChannelInSet(doorChannels, channelId)) {
            updateStateIfLinked(channelId, OpenClosedType.CLOSED);
        } else if (isChannelInSet(currentTemperatureChannels, channelId)
                || isChannelInSet(meatProbeChannels, channelId)) {
            updateStateIfLinked(channelId, UnDefType.UNDEF);
        } else if (isChannelInSet(lightChannels, channelId)) {
            updateStateIfLinked(channelId, OnOffType.OFF);
        } else if (isChannelInSet(meatProbePluggedChannels, channelId)) {
            updateStateIfLinked(channelId, OnOffType.OFF);
        }
    }

    private boolean isChannelInSet(Set<DynamicChannel> channels, String channelId) {
        return channels.stream().anyMatch(dynamicChannel -> channelId.equals(dynamicChannel.channelName()));
    }

    private void addCavityChannels() {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            var thingBuilder = editThing();
            var channelsChanged = false;

            var cavitySelectorEnumType = deviceDescriptionService.findEnumerationType(OVEN_CAVITY_SELECTOR_ENUM_KEY);
            if (cavitySelectorEnumType != null) {
                for (Enumeration enumeration : cavitySelectorEnumType.enumerations().values()) {
                    var cavityIndex = enumeration.value();
                    var cavityKey = enumeration.valueKey();

                    // door state
                    var doorStateKey = String.format(OVEN_DOOR_STATE_KEY_TEMPLATE, cavityIndex);
                    var doorChannel = String.format(CHANNEL_OVEN_DOOR_TEMPLATE, cavityIndex);
                    var doorStateStatusDescription = deviceDescriptionService.findStatusByKey(doorStateKey);
                    if (doorStateStatusDescription != null) {
                        doorChannels.add(new DynamicChannel(doorChannel, doorStateKey,
                                doorStateStatusDescription.contentType()));
                        channelsChanged |= addChannelIfNotExist(thingBuilder, doorChannel, CHANNEL_TYPE_DOOR,
                                CoreItemFactory.CONTACT, getTranslationProvider().getText(I18N_OVEN_DOOR,
                                        getTranslationProvider().getText(cavityKey)));
                    }

                    // temperatures
                    channelsChanged |= registerTemperatureChannelWithKeyTemplate(deviceDescriptionService, thingBuilder,
                            cavityIndex, OVEN_CURRENT_TEMPERATURE_KEY_TEMPLATE, CHANNEL_OVEN_TEMPERATURE_TEMPLATE,
                            CHANNEL_TYPE_OVEN_CURRENT_TEMPERATURE, I18N_OVEN_CURRENT_TEMPERATURE,
                            currentTemperatureChannels, cavityKey);
                    channelsChanged |= registerTemperatureChannelWithKeyTemplate(deviceDescriptionService, thingBuilder,
                            cavityIndex, OVEN_CURRENT_MEAT_PROBE_TEMPERATURE_KEY_TEMPLATE,
                            CHANNEL_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE_TEMPLATE,
                            CHANNEL_TYPE_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE, I18N_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE,
                            meatProbeChannels, cavityKey);

                    // lights
                    var lightKey = String.format(OVEN_CAVITY_LIGHT_KEY_TEMPLATE, cavityIndex);
                    var lightChannel = String.format(CHANNEL_OVEN_CAVITY_LIGHT_TEMPLATE, cavityIndex);
                    var lightSettingDescription = deviceDescriptionService.findSettingByKey(lightKey);
                    if (lightSettingDescription != null) {
                        lightChannels
                                .add(new DynamicChannel(lightChannel, lightKey, lightSettingDescription.contentType()));
                        channelsChanged |= addChannelIfNotExist(thingBuilder, lightChannel,
                                CHANNEL_TYPE_OVEN_CAVITY_LIGHT, CoreItemFactory.SWITCH, getTranslationProvider()
                                        .getText(I18N_OVEN_CAVITY_LIGHT, getTranslationProvider().getText(cavityKey)));
                    }

                    // meat probe plugged
                    var meatProbePluggedKey = String.format(OVEN_MEAT_PROBE_PLUGGED_KEY_TEMPLATE, cavityIndex);
                    var meatProbePluggedChannel = String.format(CHANNEL_OVEN_MEAT_PROBE_PLUGGED_TEMPLATE, cavityIndex);
                    var meatProbePluggedStatusDescription = deviceDescriptionService
                            .findStatusByKey(meatProbePluggedKey);
                    if (meatProbePluggedStatusDescription != null) {
                        meatProbePluggedChannels.add(new DynamicChannel(meatProbePluggedChannel, meatProbePluggedKey,
                                meatProbePluggedStatusDescription.contentType()));
                        channelsChanged |= addChannelIfNotExist(thingBuilder, meatProbePluggedChannel,
                                CHANNEL_TYPE_OVEN_MEAT_PROBE_PLUGGED, CoreItemFactory.SWITCH,
                                getTranslationProvider().getText(I18N_OVEN_MEAT_PROBE_PLUGGED,
                                        getTranslationProvider().getText(cavityKey)));
                    }
                }
            }

            // temperatures (main cavity)
            channelsChanged |= registerTemperatureChannel(deviceDescriptionService, thingBuilder, DEFAULT_CAVITY_INDEX,
                    OVEN_CURRENT_TEMPERATURE_KEY, CHANNEL_OVEN_TEMPERATURE_TEMPLATE,
                    CHANNEL_TYPE_OVEN_CURRENT_TEMPERATURE, I18N_OVEN_CURRENT_TEMPERATURE, currentTemperatureChannels,
                    DEFAULT_CAVITY_NAME);
            channelsChanged |= registerTemperatureChannel(deviceDescriptionService, thingBuilder, DEFAULT_CAVITY_INDEX,
                    OVEN_CURRENT_MEAT_PROBE_TEMPERATURE_KEY, CHANNEL_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE_TEMPLATE,
                    CHANNEL_TYPE_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE, I18N_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE,
                    meatProbeChannels, DEFAULT_CAVITY_NAME);

            // meat probe plugged (main cavity)
            if (deviceDescriptionService.findStatusByKey(OVEN_MEAT_PROBE_PLUGGED_KEY) != null) {
                var meatProbePluggedChannel = String.format(CHANNEL_OVEN_MEAT_PROBE_PLUGGED_TEMPLATE,
                        DEFAULT_CAVITY_INDEX);
                channelsChanged |= addChannelIfNotExist(thingBuilder, meatProbePluggedChannel,
                        CHANNEL_TYPE_OVEN_MEAT_PROBE_PLUGGED, CoreItemFactory.SWITCH, getTranslationProvider().getText(
                                I18N_OVEN_MEAT_PROBE_PLUGGED, getTranslationProvider().getText(DEFAULT_CAVITY_NAME)));
            }

            // door fallback
            if (doorChannels.isEmpty()) {
                var doorChannel = String.format(CHANNEL_OVEN_DOOR_TEMPLATE, DEFAULT_CAVITY_INDEX);
                doorChannels.add(new DynamicChannel(doorChannel, DOOR_STATE_KEY, ContentType.ENUMERATION));
                channelsChanged |= addChannelIfNotExist(thingBuilder, doorChannel, CHANNEL_TYPE_DOOR,
                        CoreItemFactory.CONTACT, getTranslationProvider().getText(I18N_OVEN_DOOR,
                                getTranslationProvider().getText(DEFAULT_CAVITY_NAME)));
            }

            if (channelsChanged) {
                updateThing(thingBuilder.build());
            }
        });
    }

    private boolean registerTemperatureChannel(DeviceDescriptionService deviceDescriptionService,
            ThingBuilder thingBuilder, int cavityIndex, String key, String channelTemplate, String channelType,
            String i18nKey, Set<DynamicChannel> targetSet, String cavityKey) {
        var channelName = String.format(channelTemplate, cavityIndex);
        var statusDescription = deviceDescriptionService.findStatusByKey(key);

        if (statusDescription != null) {
            targetSet.add(new DynamicChannel(channelName, key, statusDescription.contentType()));
            return addChannelIfNotExist(thingBuilder, channelName, channelType, NUMBER_TEMPERATURE,
                    getTranslationProvider().getText(i18nKey, getTranslationProvider().getText(cavityKey)));
        }
        return false;
    }

    private boolean registerTemperatureChannelWithKeyTemplate(DeviceDescriptionService deviceDescriptionService,
            ThingBuilder thingBuilder, int cavityIndex, String keyTemplate, String channelTemplate, String channelType,
            String i18nKey, Set<DynamicChannel> targetSet, String cavityKey) {
        var key = String.format(keyTemplate, cavityIndex);
        return registerTemperatureChannel(deviceDescriptionService, thingBuilder, cavityIndex, key, channelTemplate,
                channelType, i18nKey, targetSet, cavityKey);
    }

    private Unit<Temperature> getTemperatureUnitOfOption(String optionKey) {
        var unit = getDeviceDescriptionServiceOptional().map(deviceDescriptionService -> {
            var option = deviceDescriptionService.findOptionByKey(optionKey);
            if (option != null) {
                return ContentType.TEMPERATURE_CELSIUS.equals(option.contentType()) ? CELSIUS : FAHRENHEIT;
            }
            return CELSIUS;
        }).orElse(null);
        return Objects.requireNonNullElse(unit, CELSIUS);
    }

    private void updateProgramCommandDescription() {
        getLinkedChannel(CHANNEL_OVEN_PROGRAM_COMMAND).ifPresent(channel -> {
            var commandOptions = new ArrayList<CommandOption>();

            if (!STATE_NO_PROGRAM.equals(getKeyValueStore().getOrDefault(SELECTED_PROGRAM_KEY, STATE_NO_PROGRAM))
                    || !STATE_NO_PROGRAM
                            .equals(getKeyValueStore().getOrDefault(ACTIVE_PROGRAM_KEY, STATE_NO_PROGRAM))) {
                if (STATE_NO_PROGRAM.equals(getKeyValueStore().getOrDefault(ACTIVE_PROGRAM_KEY, STATE_NO_PROGRAM))
                        && !STATE_NO_PROGRAM
                                .equals(getKeyValueStore().getOrDefault(SELECTED_PROGRAM_KEY, STATE_NO_PROGRAM))) {
                    // no active program and program selected -> show start option
                    commandOptions.add(
                            new CommandOption(COMMAND_START, getTranslationProvider().getText(I18N_START_PROGRAM)));
                } else {
                    commandOptions
                            .add(new CommandOption(COMMAND_STOP, getTranslationProvider().getText(I18N_STOP_PROGRAM)));
                    getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                        if (deviceDescriptionService.isCommandAvailableAndWritable(RESUME_PROGRAM_KEY)) {
                            commandOptions.add(new CommandOption(COMMAND_RESUME,
                                    getTranslationProvider().getText(I18N_RESUME_PROGRAM)));
                        } else if (deviceDescriptionService.isCommandAvailableAndWritable(PAUSE_PROGRAM_KEY)) {
                            commandOptions.add(new CommandOption(COMMAND_PAUSE,
                                    getTranslationProvider().getText(I18N_PAUSE_PROGRAM)));
                        }
                    });
                }
            }

            setCommandOptions(channel.getUID(), commandOptions);
        });
    }

    /**
     * Checks if any registered temperature status has notifyOnChange set to false.
     */
    private boolean hasTemperatureStatusWithoutNotifyOnChange() {
        var deviceDescriptionService = getDeviceDescriptionServiceOptional().orElse(null);
        if (deviceDescriptionService == null) {
            return false;
        }

        return Stream.of(currentTemperatureChannels, meatProbeChannels).flatMap(Set::stream).map(DynamicChannel::key)
                .map(deviceDescriptionService::findStatusByKey).filter(Objects::nonNull)
                .anyMatch(status -> !status.notifyOnChange());
    }

    /**
     * Determines the required polling interval based on the current operation state and cavity temperature,
     * then starts, adjusts, or stops polling accordingly.
     *
     * Polling rules:
     * - Operation state "Run" and notifyOnChange is false: poll every 60 seconds
     * - Not running, temperature > 50°C: poll every 60 seconds
     * - Not running, temperature 30-50°C: poll every 5 minutes
     * - Not running, temperature < 30°C: stop polling
     */
    private void updateValuesPolling() {
        if (!hasTemperatureStatusWithoutNotifyOnChange()) {
            stopValuesPolling();
            return;
        }

        var operationState = getKeyValueStore().get(OPERATION_STATE_KEY);
        if (STATE_RUN.equals(operationState)) {
            scheduleValuesPolling(POLLING_INTERVAL_RUN_SECONDS);
            return;
        }

        // after leaving "Run" state, keep polling based on the current cavity temperature
        // to track the cool-down phase
        var temperature = maxCavityTemperature.get();
        if (temperature > TEMPERATURE_THRESHOLD_HIGH) {
            scheduleValuesPolling(POLLING_INTERVAL_RUN_SECONDS);
        } else if (temperature >= TEMPERATURE_THRESHOLD_LOW) {
            scheduleValuesPolling(POLLING_INTERVAL_COOLDOWN_SECONDS);
        } else {
            scheduleValuesPolling(POLLING_INTERVAL_IDLE_SECONDS);
        }
    }

    private synchronized void scheduleValuesPolling(long intervalSeconds) {
        var pollingFuture = this.pollingFuture;

        // restart polling if interval changed
        if (pollingFuture != null && !pollingFuture.isCancelled() && !pollingFuture.isDone()
                && currentPollingIntervalSeconds != intervalSeconds) {
            logger.debug("Polling interval changed from {} to {} second(s), rescheduling ({}).",
                    currentPollingIntervalSeconds, intervalSeconds, getThing().getUID());
            pollingFuture.cancel(false);
            pollingFuture = null;
        }

        if (pollingFuture == null || pollingFuture.isCancelled() || pollingFuture.isDone()) {
            logger.debug("Schedule mandatory values polling every {} second(s) ({}).", intervalSeconds,
                    getThing().getUID());
            currentPollingIntervalSeconds = intervalSeconds;
            this.pollingFuture = scheduler.scheduleWithFixedDelay(() -> sendGet(RO_ALL_MANDATORY_VALUES),
                    intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        }
    }

    private synchronized void stopValuesPolling() {
        var pollingFuture = this.pollingFuture;

        if (pollingFuture != null) {
            logger.debug("Stop mandatory values polling ({}).", getThing().getUID());
            pollingFuture.cancel(true);
            this.pollingFuture = null;
            currentPollingIntervalSeconds = 0;
        }
    }
}
