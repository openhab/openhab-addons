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
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_BEAN_CONTAINER_EMPTY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_COUNTDOWN_CALC_AND_CLEAN;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_COUNTDOWN_CLEANING;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_COUNTDOWN_DESCALING;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_COUNTDOWN_WATER_FILTER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_DRIP_TRAY_FULL;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_EMPTY_MILK_TANK;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_PROCESS_PHASE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_PROGRAM_COMMAND;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_WATER_TANK_EMPTY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COFFEE_MAKER_WATER_TANK_NEARLY_EMPTY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COFFEE_MAKER_BEAN_CONTAINER_EMPTY_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COFFEE_MAKER_COUNTDOWN_CALC_AND_CLEAN_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COFFEE_MAKER_COUNTDOWN_CLEANING_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COFFEE_MAKER_COUNTDOWN_DESCALING_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COFFEE_MAKER_COUNTDOWN_WATER_FILTER_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COFFEE_MAKER_DRIP_TRAY_FULL_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COFFEE_MAKER_EMPTY_MILK_TANK_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COFFEE_MAKER_PROCESS_PHASE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COFFEE_MAKER_WATER_TANK_EMPTY_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COFFEE_MAKER_WATER_TANK_NEARLY_EMPTY_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COMMAND_START;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COMMAND_STOP;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_START_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_STOP_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SELECTED_PROGRAM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_CONFIRMED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_PRESENT;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_ACTIVE_PROGRAM;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnectdirect.internal.configuration.HomeConnectDirectConfiguration;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.i18n.HomeConnectDirectTranslationProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicCommandDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicStateDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.DeviceDescriptionType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.profile.ApplianceProfileService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.ProgramData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectDirectCoffeeMakerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDirectCoffeeMakerHandler extends BaseHomeConnectDirectHandler {

    private final Logger logger;

    public HomeConnectDirectCoffeeMakerHandler(Thing thing, ApplianceProfileService applianceProfileService,
            HomeConnectDirectDynamicCommandDescriptionProvider commandDescriptionProvider,
            HomeConnectDirectDynamicStateDescriptionProvider stateDescriptionProvider, String deviceId,
            HomeConnectDirectConfiguration configuration, HomeConnectDirectTranslationProvider translationProvider) {
        super(thing, applianceProfileService, commandDescriptionProvider, stateDescriptionProvider, deviceId,
                configuration, translationProvider);

        this.logger = LoggerFactory.getLogger(HomeConnectDirectCoffeeMakerHandler.class);
    }

    @Override
    protected void initializeFinished() {
        initializeAllStates();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_COFFEE_MAKER_PROGRAM_COMMAND.equals(channelUID.getId()) && command instanceof StringType) {
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
            } else if (COMMAND_STOP.equalsIgnoreCase(command.toFullString())) {
                sendBooleanCommandIfAllowed(ABORT_PROGRAM_KEY);
            }
        }
    }

    @Override
    protected void onApplianceDescriptionChangeEvent(List<DeviceDescriptionChange> deviceDescriptionChanges) {
        super.onApplianceDescriptionChangeEvent(deviceDescriptionChanges);

        deviceDescriptionChanges.forEach(deviceDescriptionChange -> {
            if (DeviceDescriptionType.COMMAND.equals(deviceDescriptionChange.type())
                    || DeviceDescriptionType.COMMAND_LIST.equals(deviceDescriptionChange.type())) {
                updateProgramCommandDescription();
            } else if (deviceDescriptionChange.key().equals(COFFEE_MAKER_PROCESS_PHASE_KEY)) {
                updateStatusDescriptionIfLinked(CHANNEL_COFFEE_MAKER_PROCESS_PHASE, COFFEE_MAKER_PROCESS_PHASE_KEY);
            }
        });
    }

    @Override
    protected void onApplianceValueEvent(Value value, Resource resource) {
        super.onApplianceValueEvent(value, resource);

        switch (value.key()) {
            case COFFEE_MAKER_PROCESS_PHASE_KEY ->
                updateStateIfLinked(CHANNEL_COFFEE_MAKER_PROCESS_PHASE, new StringType(value.getValueAsString()));
            case COFFEE_MAKER_COUNTDOWN_CLEANING_KEY -> updateStateIfLinked(CHANNEL_COFFEE_MAKER_COUNTDOWN_CLEANING,
                    () -> new DecimalType(value.getValueAsInt()));
            case COFFEE_MAKER_COUNTDOWN_CALC_AND_CLEAN_KEY -> updateStateIfLinked(
                    CHANNEL_COFFEE_MAKER_COUNTDOWN_CALC_AND_CLEAN, () -> new DecimalType(value.getValueAsInt()));
            case COFFEE_MAKER_COUNTDOWN_DESCALING_KEY -> updateStateIfLinked(CHANNEL_COFFEE_MAKER_COUNTDOWN_DESCALING,
                    () -> new DecimalType(value.getValueAsInt()));
            case COFFEE_MAKER_COUNTDOWN_WATER_FILTER_KEY -> updateStateIfLinked(
                    CHANNEL_COFFEE_MAKER_COUNTDOWN_WATER_FILTER, () -> new DecimalType(value.getValueAsInt()));
            case COFFEE_MAKER_WATER_TANK_EMPTY_KEY -> updateStateIfLinked(CHANNEL_COFFEE_MAKER_WATER_TANK_EMPTY,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case COFFEE_MAKER_WATER_TANK_NEARLY_EMPTY_KEY ->
                updateStateIfLinked(CHANNEL_COFFEE_MAKER_WATER_TANK_NEARLY_EMPTY,
                        () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                                || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case COFFEE_MAKER_DRIP_TRAY_FULL_KEY -> updateStateIfLinked(CHANNEL_COFFEE_MAKER_DRIP_TRAY_FULL,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case COFFEE_MAKER_EMPTY_MILK_TANK_KEY -> updateStateIfLinked(CHANNEL_COFFEE_MAKER_EMPTY_MILK_TANK,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case COFFEE_MAKER_BEAN_CONTAINER_EMPTY_KEY -> updateStateIfLinked(CHANNEL_COFFEE_MAKER_BEAN_CONTAINER_EMPTY,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case SELECTED_PROGRAM_KEY, ACTIVE_PROGRAM_KEY -> updateProgramCommandDescription();
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        initializeState(channelUID.getId());
    }

    private void initializeAllStates() {
        Set.of(CHANNEL_COFFEE_MAKER_PROCESS_PHASE, CHANNEL_COFFEE_MAKER_WATER_TANK_EMPTY,
                CHANNEL_COFFEE_MAKER_WATER_TANK_NEARLY_EMPTY, CHANNEL_COFFEE_MAKER_DRIP_TRAY_FULL,
                CHANNEL_COFFEE_MAKER_EMPTY_MILK_TANK, CHANNEL_COFFEE_MAKER_BEAN_CONTAINER_EMPTY,
                CHANNEL_COFFEE_MAKER_PROGRAM_COMMAND).forEach(this::initializeState);
    }

    private void initializeState(String channelId) {
        switch (channelId) {
            case CHANNEL_COFFEE_MAKER_PROCESS_PHASE ->
                updateStatusDescriptionIfLinked(channelId, COFFEE_MAKER_PROCESS_PHASE_KEY);
            case CHANNEL_COFFEE_MAKER_PROGRAM_COMMAND -> updateProgramCommandDescription();
            case CHANNEL_COFFEE_MAKER_WATER_TANK_EMPTY, CHANNEL_COFFEE_MAKER_WATER_TANK_NEARLY_EMPTY,
                    CHANNEL_COFFEE_MAKER_DRIP_TRAY_FULL, CHANNEL_COFFEE_MAKER_EMPTY_MILK_TANK,
                    CHANNEL_COFFEE_MAKER_BEAN_CONTAINER_EMPTY ->
                updateStateIfLinked(channelId, OnOffType.OFF);
        }
    }

    private void updateProgramCommandDescription() {
        getLinkedChannel(CHANNEL_COFFEE_MAKER_PROGRAM_COMMAND).ifPresent(channel -> {
            var commandOptions = new ArrayList<CommandOption>();

            getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                if (deviceDescriptionService.isCommandAvailableAndWritable(ABORT_PROGRAM_KEY)) {
                    commandOptions
                            .add(new CommandOption(COMMAND_STOP, getTranslationProvider().getText(I18N_STOP_PROGRAM)));
                } else {
                    commandOptions.add(
                            new CommandOption(COMMAND_START, getTranslationProvider().getText(I18N_START_PROGRAM)));
                }
            });

            setCommandOptions(channel.getUID(), commandOptions);
        });
    }
}
