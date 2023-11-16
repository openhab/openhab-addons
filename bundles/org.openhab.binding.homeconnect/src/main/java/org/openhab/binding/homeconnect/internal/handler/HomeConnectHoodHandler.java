/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.model.AvailableProgramOption;
import org.openhab.binding.homeconnect.internal.client.model.Data;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectHoodHandler} is responsible for handling commands, which are
 * sent to one of the channels of a hood.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectHoodHandler extends AbstractHomeConnectThingHandler {

    private static final String START_VENTING_INTENSIVE_STAGE_PAYLOAD_TEMPLATE = """

            {
                "data": {
                    "key": "Cooking.Common.Program.Hood.Venting",
                    "options": [
                        {
                            "key": "Cooking.Common.Option.Hood.IntensiveLevel",
                            "value": "%s"
                        }
                    ]
                }
            }\
            """;

    private static final String START_VENTING_STAGE_PAYLOAD_TEMPLATE = """

            {
                "data": {
                    "key": "Cooking.Common.Program.Hood.Venting",
                    "options": [
                        {
                            "key": "Cooking.Common.Option.Hood.VentingLevel",
                            "value": "%s"
                        }
                    ]
                }
            }\
            """;

    private final Logger logger = LoggerFactory.getLogger(HomeConnectHoodHandler.class);

    public HomeConnectHoodHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    protected void configureChannelUpdateHandlers(Map<String, ChannelUpdateHandler> handlers) {
        // register default update handlers
        handlers.put(CHANNEL_OPERATION_STATE, defaultOperationStateChannelUpdateHandler());
        handlers.put(CHANNEL_POWER_STATE, defaultPowerStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_START_ALLOWANCE_STATE, defaultRemoteStartAllowanceChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE, defaultRemoteControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE, defaultLocalControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_ACTIVE_PROGRAM_STATE, defaultActiveProgramStateUpdateHandler());
        handlers.put(CHANNEL_AMBIENT_LIGHT_STATE, defaultAmbientLightChannelUpdateHandler());
        handlers.put(CHANNEL_FUNCTIONAL_LIGHT_STATE,
                (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
                    Optional<HomeConnectApiClient> apiClient = getApiClient();
                    if (apiClient.isPresent()) {
                        Data data = apiClient.get().getFunctionalLightState(getThingHaId());
                        if (data.getValue() != null) {
                            boolean enabled = data.getValueAsBoolean();
                            if (enabled) {
                                Data brightnessData = apiClient.get().getFunctionalLightBrightnessState(getThingHaId());
                                getLinkedChannel(CHANNEL_FUNCTIONAL_LIGHT_BRIGHTNESS_STATE)
                                        .ifPresent(channel -> updateState(channel.getUID(),
                                                new PercentType(brightnessData.getValueAsInt())));
                            }
                            return OnOffType.from(enabled);
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
        handlers.put(EVENT_REMOTE_CONTROL_START_ALLOWED,
                defaultBooleanEventHandler(CHANNEL_REMOTE_START_ALLOWANCE_STATE));
        handlers.put(EVENT_REMOTE_CONTROL_ACTIVE, defaultBooleanEventHandler(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE));
        handlers.put(EVENT_LOCAL_CONTROL_ACTIVE, defaultBooleanEventHandler(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE));
        handlers.put(EVENT_OPERATION_STATE, defaultOperationStateEventHandler());
        handlers.put(EVENT_ACTIVE_PROGRAM, defaultActiveProgramEventHandler());
        handlers.put(EVENT_POWER_STATE, defaultPowerStateEventHandler());
        handlers.put(EVENT_FUNCTIONAL_LIGHT_STATE, defaultBooleanEventHandler(CHANNEL_FUNCTIONAL_LIGHT_STATE));
        handlers.put(EVENT_FUNCTIONAL_LIGHT_BRIGHTNESS_STATE,
                defaultPercentHandler(CHANNEL_FUNCTIONAL_LIGHT_BRIGHTNESS_STATE));
        handlers.put(EVENT_AMBIENT_LIGHT_STATE, defaultBooleanEventHandler(CHANNEL_AMBIENT_LIGHT_STATE));
        handlers.put(EVENT_AMBIENT_LIGHT_BRIGHTNESS_STATE,
                defaultPercentHandler(CHANNEL_AMBIENT_LIGHT_BRIGHTNESS_STATE));
        handlers.put(EVENT_AMBIENT_LIGHT_COLOR_STATE, defaultAmbientLightColorStateEventHandler());
        handlers.put(EVENT_AMBIENT_LIGHT_CUSTOM_COLOR_STATE, defaultAmbientLightCustomColorStateEventHandler());

        // register hood specific SSE event handlers
        handlers.put(EVENT_HOOD_INTENSIVE_LEVEL,
                event -> getLinkedChannel(CHANNEL_HOOD_INTENSIVE_LEVEL).ifPresent(channel -> {
                    String hoodIntensiveLevel = event.getValue();
                    if (hoodIntensiveLevel != null) {
                        updateState(channel.getUID(), new StringType(mapStageStringType(hoodIntensiveLevel)));
                    } else {
                        updateState(channel.getUID(), UnDefType.UNDEF);
                    }
                }));
        handlers.put(EVENT_HOOD_VENTING_LEVEL,
                event -> getLinkedChannel(CHANNEL_HOOD_VENTING_LEVEL).ifPresent(channel -> {
                    String hoodVentingLevel = event.getValue();
                    if (hoodVentingLevel != null) {
                        updateState(channel.getUID(), new StringType(mapStageStringType(hoodVentingLevel)));
                    } else {
                        updateState(channel.getUID(), UnDefType.UNDEF);
                    }
                }));
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command,
            final HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        super.handleCommand(channelUID, command, apiClient);

        handlePowerCommand(channelUID, command, apiClient, STATE_POWER_OFF);

        // light commands
        handleLightCommands(channelUID, command, apiClient);

        // program options
        if (command instanceof StringType && CHANNEL_HOOD_ACTIONS_STATE.equals(channelUID.getId())) {
            String operationState = getOperationState();
            if (OPERATION_STATE_INACTIVE.equals(operationState) || OPERATION_STATE_RUN.equals(operationState)) {
                if (COMMAND_STOP.equalsIgnoreCase(command.toFullString())) {
                    apiClient.stopProgram(getThingHaId());
                }
            } else {
                logger.debug("Device can not handle command {} in current operation state ({}). thing={}, haId={}",
                        command, operationState, getThingLabel(), getThingHaId());
            }

            // These command always start the hood - even if appliance is turned off
            if (COMMAND_AUTOMATIC.equalsIgnoreCase(command.toFullString())) {
                apiClient.startProgram(getThingHaId(), PROGRAM_HOOD_AUTOMATIC);
            } else if (COMMAND_DELAYED_SHUT_OFF.equalsIgnoreCase(command.toFullString())) {
                apiClient.startProgram(getThingHaId(), PROGRAM_HOOD_DELAYED_SHUT_OFF);
            } else if (COMMAND_VENTING_1.equalsIgnoreCase(command.toFullString())) {
                apiClient.startCustomProgram(getThingHaId(),
                        format(START_VENTING_STAGE_PAYLOAD_TEMPLATE, STAGE_FAN_STAGE_01));
            } else if (COMMAND_VENTING_2.equalsIgnoreCase(command.toFullString())) {
                apiClient.startCustomProgram(getThingHaId(),
                        format(START_VENTING_STAGE_PAYLOAD_TEMPLATE, STAGE_FAN_STAGE_02));
            } else if (COMMAND_VENTING_3.equalsIgnoreCase(command.toFullString())) {
                apiClient.startCustomProgram(getThingHaId(),
                        format(START_VENTING_STAGE_PAYLOAD_TEMPLATE, STAGE_FAN_STAGE_03));
            } else if (COMMAND_VENTING_4.equalsIgnoreCase(command.toFullString())) {
                apiClient.startCustomProgram(getThingHaId(),
                        format(START_VENTING_STAGE_PAYLOAD_TEMPLATE, STAGE_FAN_STAGE_04));
            } else if (COMMAND_VENTING_5.equalsIgnoreCase(command.toFullString())) {
                apiClient.startCustomProgram(getThingHaId(),
                        format(START_VENTING_STAGE_PAYLOAD_TEMPLATE, STAGE_FAN_STAGE_05));
            } else if (COMMAND_VENTING_INTENSIVE_1.equalsIgnoreCase(command.toFullString())) {
                apiClient.startCustomProgram(getThingHaId(),
                        format(START_VENTING_INTENSIVE_STAGE_PAYLOAD_TEMPLATE, STAGE_INTENSIVE_STAGE_1));
            } else if (COMMAND_VENTING_INTENSIVE_2.equalsIgnoreCase(command.toFullString())) {
                apiClient.startCustomProgram(getThingHaId(),
                        format(START_VENTING_INTENSIVE_STAGE_PAYLOAD_TEMPLATE, STAGE_INTENSIVE_STAGE_2));
            } else {
                logger.info("Start custom program. command={} haId={}", command.toFullString(), getThingHaId());
                apiClient.startCustomProgram(getThingHaId(), command.toFullString());
            }
        }
    }

    @Override
    protected void updateSelectedProgramStateDescription() {
        // update hood program actions
        if (isBridgeOffline() || !isThingAccessibleViaServerSentEvents()) {
            return;
        }

        Optional<HomeConnectApiClient> apiClient = getApiClient();
        if (apiClient.isPresent()) {
            try {
                ArrayList<StateOption> stateOptions = new ArrayList<>();
                getPrograms().forEach(availableProgram -> {
                    if (PROGRAM_HOOD_AUTOMATIC.equals(availableProgram.getKey())) {
                        stateOptions.add(new StateOption(COMMAND_AUTOMATIC, mapStringType(availableProgram.getKey())));
                    } else if (PROGRAM_HOOD_DELAYED_SHUT_OFF.equals(availableProgram.getKey())) {
                        stateOptions.add(
                                new StateOption(COMMAND_DELAYED_SHUT_OFF, mapStringType(availableProgram.getKey())));
                    } else if (PROGRAM_HOOD_VENTING.equals(availableProgram.getKey())) {
                        try {
                            List<AvailableProgramOption> availableProgramOptions = apiClient.get()
                                    .getProgramOptions(getThingHaId(), PROGRAM_HOOD_VENTING);
                            if (availableProgramOptions == null || availableProgramOptions.isEmpty()) {
                                throw new CommunicationException("Program " + PROGRAM_HOOD_VENTING + " is unsupported");
                            }
                            availableProgramOptions.forEach(option -> {
                                if (OPTION_HOOD_VENTING_LEVEL.equalsIgnoreCase(option.getKey())) {
                                    option.getAllowedValues().stream().filter(s -> !STAGE_FAN_OFF.equalsIgnoreCase(s))
                                            .forEach(s -> stateOptions.add(createVentingStateOption(s)));
                                } else if (OPTION_HOOD_INTENSIVE_LEVEL.equalsIgnoreCase(option.getKey())) {
                                    option.getAllowedValues().stream()
                                            .filter(s -> !STAGE_INTENSIVE_STAGE_OFF.equalsIgnoreCase(s))
                                            .forEach(s -> stateOptions.add(createVentingStateOption(s)));
                                }
                            });
                        } catch (CommunicationException | ApplianceOfflineException | AuthorizationException e) {
                            logger.warn("Could not fetch hood program options. error={}", e.getMessage());
                            stateOptions.add(createVentingStateOption(STAGE_FAN_STAGE_01));
                            stateOptions.add(createVentingStateOption(STAGE_FAN_STAGE_02));
                            stateOptions.add(createVentingStateOption(STAGE_FAN_STAGE_03));
                            stateOptions.add(createVentingStateOption(STAGE_FAN_STAGE_04));
                            stateOptions.add(createVentingStateOption(STAGE_FAN_STAGE_05));
                            stateOptions.add(createVentingStateOption(STAGE_INTENSIVE_STAGE_1));
                            stateOptions.add(createVentingStateOption(STAGE_INTENSIVE_STAGE_2));
                        }
                    }
                });
                stateOptions.add(new StateOption(COMMAND_STOP, "Stop"));

                getThingChannel(CHANNEL_HOOD_ACTIONS_STATE).ifPresent(channel -> getDynamicStateDescriptionProvider()
                        .setStateOptions(channel.getUID(), stateOptions));
            } catch (CommunicationException | ApplianceOfflineException | AuthorizationException e) {
                logger.debug("Could not fetch available programs. thing={}, haId={}, error={}", getThingLabel(),
                        getThingHaId(), e.getMessage());
                removeSelectedProgramStateDescription();
            }
        } else {
            removeSelectedProgramStateDescription();
        }
    }

    @Override
    protected void removeSelectedProgramStateDescription() {
        getThingChannel(CHANNEL_HOOD_ACTIONS_STATE).ifPresent(
                channel -> getDynamicStateDescriptionProvider().setStateOptions(channel.getUID(), emptyList()));
    }

    @Override
    public String toString() {
        return "HomeConnectHoodHandler [haId: " + getThingHaId() + "]";
    }

    @Override
    protected void resetProgramStateChannels(boolean offline) {
        super.resetProgramStateChannels(offline);
        getLinkedChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getLinkedChannel(CHANNEL_HOOD_INTENSIVE_LEVEL).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getLinkedChannel(CHANNEL_HOOD_VENTING_LEVEL).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
    }

    private StateOption createVentingStateOption(String optionKey) {
        String label = mapStringType(PROGRAM_HOOD_VENTING);

        if (STAGE_FAN_STAGE_01.equalsIgnoreCase(optionKey)) {
            return new StateOption(COMMAND_VENTING_1,
                    format("%s (Level %s)", label, mapStageStringType(STAGE_FAN_STAGE_01)));
        } else if (STAGE_FAN_STAGE_02.equalsIgnoreCase(optionKey)) {
            return new StateOption(COMMAND_VENTING_2,
                    format("%s (Level %s)", label, mapStageStringType(STAGE_FAN_STAGE_02)));
        } else if (STAGE_FAN_STAGE_03.equalsIgnoreCase(optionKey)) {
            return new StateOption(COMMAND_VENTING_3,
                    format("%s (Level %s)", label, mapStageStringType(STAGE_FAN_STAGE_03)));
        } else if (STAGE_FAN_STAGE_04.equalsIgnoreCase(optionKey)) {
            return new StateOption(COMMAND_VENTING_4,
                    format("%s (Level %s)", label, mapStageStringType(STAGE_FAN_STAGE_04)));
        } else if (STAGE_FAN_STAGE_05.equalsIgnoreCase(optionKey)) {
            return new StateOption(COMMAND_VENTING_5,
                    format("%s (Level %s)", label, mapStageStringType(STAGE_FAN_STAGE_05)));
        } else if (STAGE_INTENSIVE_STAGE_1.equalsIgnoreCase(optionKey)) {
            return new StateOption(COMMAND_VENTING_INTENSIVE_1,
                    format("%s (Intensive level %s)", label, mapStageStringType(STAGE_INTENSIVE_STAGE_1)));
        } else {
            return new StateOption(COMMAND_VENTING_INTENSIVE_2,
                    format("%s (Intensive level %s)", label, mapStageStringType(STAGE_INTENSIVE_STAGE_2)));
        }
    }
}
