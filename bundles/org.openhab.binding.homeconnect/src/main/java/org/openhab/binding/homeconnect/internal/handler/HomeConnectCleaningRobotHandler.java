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
package org.openhab.binding.homeconnect.internal.handler;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.model.Data;
import org.openhab.binding.homeconnect.internal.client.model.Program;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectCleaningRobotHandler} is responsible for handling commands, which are
 * sent to one of the channels of a cleaning robot.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectCleaningRobotHandler extends AbstractHomeConnectThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HomeConnectCleaningRobotHandler.class);
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    @Nullable
    private CleaningRobotType type;

    public HomeConnectCleaningRobotHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider,
            TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        super(thing, dynamicStateDescriptionProvider);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
    }

    @Override
    protected void refreshThingStatus() {
        super.refreshThingStatus();
        if (updateCleaningRobotType()) {
            updateCleaningModeStateOptions();
            updateBasicActionsStateOptions();
            removeUnsupportedChannels();
        }
    }

    @Override
    protected void configureChannelUpdateHandlers(Map<String, ChannelUpdateHandler> handlers) {
        // register default update handlers
        handlers.put(CHANNEL_OPERATION_STATE, defaultOperationStateChannelUpdateHandler());
        handlers.put(CHANNEL_POWER_STATE, defaultPowerStateChannelUpdateHandler());
        handlers.put(CHANNEL_SELECTED_PROGRAM_STATE, defaultSelectedProgramStateUpdateHandler());
        handlers.put(CHANNEL_ACTIVE_PROGRAM_STATE, defaultActiveProgramStateUpdateHandler());
        handlers.put(CHANNEL_BATTERY_LEVEL,
                (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
                    Optional<HomeConnectApiClient> apiClient = getApiClient();
                    if (apiClient.isPresent()) {
                        Data data = apiClient.get().getBatteryLevel(getThingHaId());
                        if (data.getValue() != null) {
                            return new QuantityType<>(data.getValueAsInt(), Units.PERCENT);
                        } else {
                            return UnDefType.UNDEF;
                        }
                    } else {
                        return UnDefType.UNDEF;
                    }
                })));
        handlers.put(CHANNEL_CURRENT_MAP,
                (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
                    Optional<HomeConnectApiClient> apiClient = getApiClient();
                    if (apiClient.isPresent()) {
                        Data data = apiClient.get().getCurrentMap(getThingHaId());
                        if (data.getValue() != null) {
                            return new StringType(data.getValue());
                        } else {
                            return UnDefType.UNDEF;
                        }
                    } else {
                        return UnDefType.UNDEF;
                    }
                })));
    }

    @Override
    protected void configureEventHandlers(Map<String, EventHandler> handlers) {
        // register default SSE event handlers
        handlers.put(EVENT_OPERATION_STATE, defaultOperationStateEventHandler());
        handlers.put(EVENT_SELECTED_PROGRAM, defaultSelectedProgramStateEventHandler());
        handlers.put(EVENT_POWER_STATE, event -> getLinkedChannel(CHANNEL_POWER_STATE).ifPresent(
                channel -> updateState(channel.getUID(), OnOffType.from(STATE_POWER_ON.equals(event.getValue())))));
        handlers.put(EVENT_ACTIVE_PROGRAM, defaultActiveProgramEventHandler());
        handlers.put(EVENT_PROGRAM_PROGRESS, defaultPercentQuantityTypeEventHandler(CHANNEL_PROGRAM_PROGRESS_STATE));

        // register cleaning robot specific SSE event handlers
        handlers.put(EVENT_BATTERY_LEVEL, defaultPercentQuantityTypeEventHandler(CHANNEL_BATTERY_LEVEL));
        handlers.put(EVENT_CURRENT_MAP, defaultStringEventHandler(CHANNEL_CURRENT_MAP));
        handlers.put(OPTION_CLEANING_MODE, defaultStringEventHandler(CHANNEL_CLEANING_MODE_STATE));
        handlers.put(OPTION_SUCTION_POWER, defaultStringEventHandler(CHANNEL_SUCTION_POWER_STATE));
        handlers.put(OPTION_WATER_FLOW_RATE, defaultStringEventHandler(CHANNEL_WATER_FLOW_RATE_STATE));
        handlers.put(OPTION_CLEANING_PASSES, defaultStringEventHandler(CHANNEL_CLEANING_PASSES_STATE));
        handlers.put(OPTION_CARPET_BOOST, defaultBooleanEventHandler(CHANNEL_CARPET_BOOST));
        handlers.put(OPTION_CLEANING_SPEED, defaultStringEventHandler(CHANNEL_CLEANING_SPEED_STATE));
        handlers.put(OPTION_MOP_EXTENSION, defaultBooleanEventHandler(CHANNEL_MOP_EXTENSION));
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command,
            final HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        String channelId = channelUID.getId();
        boolean isProgramActive = OPERATION_STATE_RUN.equals(getOperationState());
        String commandString = command.toFullString();

        if (!(command instanceof StringType && CHANNEL_BASIC_ACTIONS_STATE.equals(channelId))) {
            super.handleCommand(channelUID, command, apiClient);
        }

        if (command instanceof StringType && CHANNEL_BASIC_ACTIONS_STATE.equals(channelId)) {
            updateState(channelUID, UnDefType.NULL);

            if (COMMAND_START.equalsIgnoreCase(commandString)) {
                Program selectedProgram = apiClient.getSelectedProgram(getThingHaId());
                if (selectedProgram != null) {
                    apiClient.startProgram(getThingHaId(), selectedProgram.getKey());
                }
            } else if (COMMAND_STOP.equalsIgnoreCase(commandString)) {
                apiClient.stopProgram(getThingHaId());
            } else if (COMMAND_PAUSE.equalsIgnoreCase(commandString)) {
                apiClient.putCommand(getThingHaId(), API_COMMAND_PAUSE);
            } else if (COMMAND_RESUME.equalsIgnoreCase(commandString)) {
                apiClient.putCommand(getThingHaId(), API_COMMAND_RESUME);
            } else if (COMMAND_GO_HOME.equalsIgnoreCase(commandString)) {
                apiClient.startProgram(getThingHaId(), PROGRAM_CLEANING_ROBOT_GO_HOME);
            }
        } else if (command instanceof StringType && CHANNEL_CLEANING_MODE_STATE.equals(channelId)) {
            apiClient.setProgramOptions(getThingHaId(), OPTION_CLEANING_MODE, command.toFullString(), null, false,
                    isProgramActive);
        } else if (command instanceof StringType && CHANNEL_SUCTION_POWER_STATE.equals(channelId)) {
            apiClient.setProgramOptions(getThingHaId(), OPTION_SUCTION_POWER, command.toFullString(), null, false,
                    isProgramActive);
        } else if (command instanceof StringType && CHANNEL_CURRENT_MAP.equals(channelId)) {
            apiClient.setCurrentMap(getThingHaId(), command.toFullString());
        }

        handlePowerCommand(channelUID, command, apiClient, STATE_POWER_STANDBY);
    }

    @Override
    public String toString() {
        return "HomeConnectCleaningRobotHandler [haId: " + getThingHaId() + "]";
    }

    @Override
    protected void updateSelectedProgramStateDescription() {
        if (isBridgeOffline() || isThingOffline()) {
            return;
        }

        try {
            List<StateOption> stateOptions = getPrograms().stream()
                    .filter(p -> !PROGRAM_CLEANING_ROBOT_GO_HOME.equals(p.getKey()))
                    .map(p -> new StateOption(p.getKey(), mapStringType(p.getKey()))).collect(Collectors.toList());

            getThingChannel(CHANNEL_SELECTED_PROGRAM_STATE).ifPresent(
                    channel -> getDynamicStateDescriptionProvider().setStateOptions(channel.getUID(), stateOptions));
        } catch (CommunicationException | ApplianceOfflineException | AuthorizationException e) {
            logger.debug("Could not fetch available programs. thing={}, haId={}, error={}", getThingLabel(),
                    getThingHaId(), e.getMessage());
            removeSelectedProgramStateDescription();
        }
    }

    @Override
    protected void resetProgramStateChannels(boolean offline) {
        super.resetProgramStateChannels(offline);
        getLinkedChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getLinkedChannel(CHANNEL_PROGRAM_PROGRESS_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));

        if (offline) {
            getLinkedChannel(CHANNEL_CLEANING_MODE_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            getLinkedChannel(CHANNEL_SUCTION_POWER_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            getLinkedChannel(CHANNEL_WATER_FLOW_RATE_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            getLinkedChannel(CHANNEL_CLEANING_PASSES_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            getLinkedChannel(CHANNEL_CARPET_BOOST).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            getLinkedChannel(CHANNEL_CLEANING_SPEED_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            getLinkedChannel(CHANNEL_MOP_EXTENSION).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        }
    }

    private boolean updateCleaningRobotType() {
        var previousType = this.type;
        if (isBridgeOnline() && isThingOnline() && previousType == null) {
            try {
                boolean hasCleanMap = getPrograms().stream()
                        .anyMatch(p -> PROGRAM_CLEANING_ROBOT_CLEAN_MAP.equals(p.getKey()));
                this.type = hasCleanMap ? CleaningRobotType.ROXXTER : CleaningRobotType.SPOTLESS;
                logger.debug("Detected cleaning robot type: {}. haId={}", this.type, getThingHaId());
            } catch (CommunicationException | AuthorizationException | ApplianceOfflineException e) {
                logger.debug("Could not determine cleaning robot type. haId={}, error={}", getThingHaId(),
                        e.getMessage());
            }
        }
        return this.type != previousType;
    }

    private void updateCleaningModeStateOptions() {
        var type = this.type;
        if (type == null) {
            return;
        }

        String keyPrefix = "channel-type.homeconnect.cleaning_mode_state.state.option.";
        List<StateOption> options;
        if (type == CleaningRobotType.ROXXTER) {
            options = Stream.of(CLEANING_MODE_SILENT, CLEANING_MODE_STANDARD, CLEANING_MODE_POWER)
                    .map(key -> new StateOption(key, translate(keyPrefix + key, mapStringType(key)))).toList();
        } else {
            options = Stream
                    .of(CLEANING_MODE_INTELLIGENT, CLEANING_MODE_VACUUM_ONLY, CLEANING_MODE_MOP_ONLY,
                            CLEANING_MODE_VACUUM_AND_MOP, CLEANING_MODE_MOP_AFTER_VACUUM)
                    .map(key -> new StateOption(key, translate(keyPrefix + key, mapStringType(key)))).toList();
        }

        getThingChannel(CHANNEL_CLEANING_MODE_STATE)
                .ifPresent(channel -> getDynamicStateDescriptionProvider().setStateOptions(channel.getUID(), options));
    }

    private void removeUnsupportedChannels() {
        List<String> channelsToRemove;
        if (this.type == CleaningRobotType.ROXXTER) {
            channelsToRemove = List.of(CHANNEL_SUCTION_POWER_STATE, CHANNEL_WATER_FLOW_RATE_STATE,
                    CHANNEL_CLEANING_PASSES_STATE, CHANNEL_CARPET_BOOST, CHANNEL_CLEANING_SPEED_STATE,
                    CHANNEL_MOP_EXTENSION);
        } else if (this.type == CleaningRobotType.SPOTLESS) {
            channelsToRemove = List.of(CHANNEL_CURRENT_MAP);
        } else {
            return;
        }

        ThingBuilder thingBuilder = editThing();
        boolean modified = false;

        for (String channelId : channelsToRemove) {
            var channel = getThingChannel(channelId);
            if (channel.isPresent()) {
                logger.debug("Removing channel {} (not supported by {} series).", channel.get().getUID(), this.type);
                thingBuilder.withoutChannel(channel.get().getUID());
                modified = true;
            }
        }

        if (modified) {
            updateThing(thingBuilder.build());
        }
    }

    private void updateBasicActionsStateOptions() {
        String keyPrefix = "channel-type.homeconnect.cleaning_robot_actions_state.state.option.";
        var baseOptions = List.of(COMMAND_START, COMMAND_STOP, COMMAND_PAUSE, COMMAND_RESUME, COMMAND_GO_HOME);

        var options = new ArrayList<StateOption>();
        for (String cmd : baseOptions) {
            options.add(new StateOption(cmd, translate(keyPrefix + cmd, cmd)));
        }

        getThingChannel(CHANNEL_BASIC_ACTIONS_STATE)
                .ifPresent(channel -> getDynamicStateDescriptionProvider().setStateOptions(channel.getUID(), options));
    }

    private String translate(String key, String defaultValue) {
        String translated = i18nProvider.getText(bundle, key, defaultValue, localeProvider.getLocale());
        return translated != null ? translated : defaultValue;
    }

    private enum CleaningRobotType {
        ROXXTER,
        SPOTLESS
    }
}
