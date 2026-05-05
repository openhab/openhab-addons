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

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DRYER_DRYING_TARGET;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DRYER_WRINKLE_GUARD;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_LAUNDRY_CARE_PROCESS_PHASE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_LAUNDRY_DRUM_CLEAN_REMINDER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_LAUNDRY_LOAD_INFORMATION;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_LAUNDRY_LOAD_RECOMMENDATION;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_I_DOS_ACTIVE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_I_DOS_FILL_LEVEL_POOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_I_DOS_1_ACTIVE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_I_DOS_1_FILL_LEVEL_POOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_I_DOS_2_ACTIVE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_I_DOS_2_FILL_LEVEL_POOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_LESS_IRONING;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_PREWASH;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_RINSE_HOLD;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_RINSE_PLUS;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_SILENT_WASH;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_SOAK;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_SPEED_PERFECT;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_SPIN_SPEED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_STAINS;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_WASHER_WATER_PLUS;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DRYER_DRYING_TARGET_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DRYER_PROCESS_PHASE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DRYER_WRINKLE_GUARD_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_I_DOS_ACTIVE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_I_DOS_FILL_LEVEL_POOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.LAUNDRY_CARE_OPTION_LIST_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.LAUNDRY_CARE_PROCESS_PHASE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.LAUNDRY_DRUM_CLEAN_REMINDER_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.LAUNDRY_LOAD_INFORMATION_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.LAUNDRY_LOAD_RECOMMENDATION_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ROOT_OPTION_LIST_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_CONFIRMED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_POOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_PRESENT;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_I_DOS_1_ACTIVE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_I_DOS_1_FILL_LEVEL_POOR_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_I_DOS_2_ACTIVE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_I_DOS_2_FILL_LEVEL_POOR_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_LESS_IRONING_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_PREWASH_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_RINSE_HOLD_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_RINSE_PLUS_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_SILENT_WASH_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_SOAK_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_SPEED_PERFECT_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_SPIN_SPEED_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_STAINS_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_TEMPERATURE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.WASHER_WATER_PLUS_KEY;
import static org.openhab.core.library.unit.SIUnits.GRAM;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnectdirect.internal.configuration.HomeConnectDirectConfiguration;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.i18n.HomeConnectDirectTranslationProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicCommandDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicStateDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.DeviceDescriptionService;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.profile.ApplianceProfileService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link HomeConnectDirectWasherDryerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDirectWasherDryerHandler extends BaseHomeConnectDirectHandler {
    public HomeConnectDirectWasherDryerHandler(Thing thing, ApplianceProfileService applianceProfileService,
            HomeConnectDirectDynamicCommandDescriptionProvider commandDescriptionProvider,
            HomeConnectDirectDynamicStateDescriptionProvider stateDescriptionProvider, String deviceId,
            HomeConnectDirectConfiguration configuration, HomeConnectDirectTranslationProvider translationProvider) {
        super(thing, applianceProfileService, commandDescriptionProvider, stateDescriptionProvider, deviceId,
                configuration, translationProvider);
    }

    @Override
    protected void initializeStarted() {
        addIDosChannels();
    }

    @Override
    protected void initializeFinished() {
        initializeAllStates();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_WASHER_I_DOS_1_ACTIVE.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, WASHER_I_DOS_1_ACTIVE_KEY);
        } else if (CHANNEL_WASHER_I_DOS_2_ACTIVE.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, WASHER_I_DOS_2_ACTIVE_KEY);
        } else if (CHANNEL_WASHER_TEMPERATURE.equals(channelUID.getId()) && command instanceof StringType) {
            sendEnumOptionIfAllowed(command, WASHER_TEMPERATURE_KEY);
        } else if (CHANNEL_WASHER_SPIN_SPEED.equals(channelUID.getId()) && command instanceof StringType) {
            sendEnumOptionIfAllowed(command, WASHER_SPIN_SPEED_KEY);
        } else if (CHANNEL_WASHER_RINSE_PLUS.equals(channelUID.getId()) && command instanceof StringType) {
            sendEnumOptionIfAllowed(command, WASHER_RINSE_PLUS_KEY);
        } else if (CHANNEL_WASHER_STAINS.equals(channelUID.getId()) && command instanceof StringType) {
            sendEnumOptionIfAllowed(command, WASHER_STAINS_KEY);
        } else if (CHANNEL_DRYER_DRYING_TARGET.equals(channelUID.getId()) && command instanceof StringType) {
            sendEnumOptionIfAllowed(command, DRYER_DRYING_TARGET_KEY);
        } else if (CHANNEL_DRYER_WRINKLE_GUARD.equals(channelUID.getId()) && command instanceof StringType) {
            sendEnumOptionIfAllowed(command, DRYER_WRINKLE_GUARD_KEY);
        } else if (CHANNEL_WASHER_SPEED_PERFECT.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, WASHER_SPEED_PERFECT_KEY);
        } else if (CHANNEL_WASHER_WATER_PLUS.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, WASHER_WATER_PLUS_KEY);
        } else if (CHANNEL_WASHER_PREWASH.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, WASHER_PREWASH_KEY);
        } else if (CHANNEL_WASHER_RINSE_HOLD.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, WASHER_RINSE_HOLD_KEY);
        } else if (CHANNEL_WASHER_LESS_IRONING.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, WASHER_LESS_IRONING_KEY);
        } else if (CHANNEL_WASHER_SILENT_WASH.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, WASHER_SILENT_WASH_KEY);
        } else if (CHANNEL_WASHER_SOAK.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, WASHER_SOAK_KEY);
        }
    }

    @Override
    protected void onApplianceDescriptionChangeEvent(List<DeviceDescriptionChange> deviceDescriptionChanges) {
        super.onApplianceDescriptionChangeEvent(deviceDescriptionChanges);

        deviceDescriptionChanges.forEach(deviceDescriptionChange -> {
            var key = deviceDescriptionChange.key();
            switch (key) {
                case LAUNDRY_CARE_PROCESS_PHASE_KEY, DRYER_PROCESS_PHASE_KEY ->
                    updateReadonlyEnumOptionDescriptionIfLinked(CHANNEL_LAUNDRY_CARE_PROCESS_PHASE, key);
                case WASHER_TEMPERATURE_KEY -> updateEnumOptionDescriptionIfLinked(CHANNEL_WASHER_TEMPERATURE, key);
                case WASHER_SPIN_SPEED_KEY -> updateEnumOptionDescriptionIfLinked(CHANNEL_WASHER_SPIN_SPEED, key);
                case WASHER_RINSE_PLUS_KEY -> updateEnumOptionDescriptionIfLinked(CHANNEL_WASHER_RINSE_PLUS, key);
                case WASHER_STAINS_KEY -> updateEnumOptionDescriptionIfLinked(CHANNEL_WASHER_STAINS, key);
                case DRYER_DRYING_TARGET_KEY -> updateEnumOptionDescriptionIfLinked(CHANNEL_DRYER_DRYING_TARGET, key);
                case DRYER_WRINKLE_GUARD_KEY -> updateEnumOptionDescriptionIfLinked(CHANNEL_DRYER_WRINKLE_GUARD, key);
                case ROOT_OPTION_LIST_KEY, LAUNDRY_CARE_OPTION_LIST_KEY -> {
                    updateReadonlyEnumOptionDescriptionIfLinked(CHANNEL_LAUNDRY_CARE_PROCESS_PHASE, key);
                    updateEnumOptionDescriptionIfLinked(CHANNEL_WASHER_TEMPERATURE, key);
                    updateEnumOptionDescriptionIfLinked(CHANNEL_WASHER_SPIN_SPEED, key);
                    updateEnumOptionDescriptionIfLinked(CHANNEL_WASHER_RINSE_PLUS, key);
                    updateEnumOptionDescriptionIfLinked(CHANNEL_WASHER_STAINS, key);
                    updateEnumOptionDescriptionIfLinked(CHANNEL_DRYER_DRYING_TARGET, key);
                    updateEnumOptionDescriptionIfLinked(CHANNEL_DRYER_WRINKLE_GUARD, key);
                }
            }
        });
    }

    @Override
    protected void onApplianceValueEvent(Value value, Resource resource) {
        super.onApplianceValueEvent(value, resource);

        switch (value.key()) {
            case LAUNDRY_CARE_PROCESS_PHASE_KEY, DRYER_PROCESS_PHASE_KEY ->
                updateStateIfLinked(CHANNEL_LAUNDRY_CARE_PROCESS_PHASE, new StringType(value.getValueAsString()));
            case WASHER_I_DOS_1_FILL_LEVEL_POOR_KEY -> updateStateIfLinked(CHANNEL_WASHER_I_DOS_1_FILL_LEVEL_POOR,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_POOR.equalsIgnoreCase(value.getValueAsString())));
            case WASHER_I_DOS_2_FILL_LEVEL_POOR_KEY -> updateStateIfLinked(CHANNEL_WASHER_I_DOS_2_FILL_LEVEL_POOR,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_POOR.equalsIgnoreCase(value.getValueAsString())));
            case WASHER_I_DOS_1_ACTIVE_KEY ->
                updateStateIfLinked(CHANNEL_WASHER_I_DOS_1_ACTIVE, () -> OnOffType.from(value.getValueAsBoolean()));
            case WASHER_I_DOS_2_ACTIVE_KEY ->
                updateStateIfLinked(CHANNEL_WASHER_I_DOS_2_ACTIVE, () -> OnOffType.from(value.getValueAsBoolean()));
            case WASHER_TEMPERATURE_KEY -> getLinkedChannel(CHANNEL_WASHER_TEMPERATURE).ifPresent(channel -> {
                getKeyValueStore().put(WASHER_TEMPERATURE_KEY, value.getValueAsString());
                updateState(channel.getUID(), new StringType(value.getValueAsString()));
            });
            case WASHER_SPIN_SPEED_KEY -> getLinkedChannel(CHANNEL_WASHER_SPIN_SPEED).ifPresent(channel -> {
                getKeyValueStore().put(WASHER_SPIN_SPEED_KEY, value.getValueAsString());
                updateState(channel.getUID(), new StringType(value.getValueAsString()));
            });
            case WASHER_RINSE_PLUS_KEY -> getLinkedChannel(CHANNEL_WASHER_RINSE_PLUS).ifPresent(channel -> {
                getKeyValueStore().put(WASHER_RINSE_PLUS_KEY, value.getValueAsString());
                updateState(channel.getUID(), new StringType(value.getValueAsString()));
            });
            case WASHER_STAINS_KEY -> getLinkedChannel(CHANNEL_WASHER_STAINS).ifPresent(channel -> {
                getKeyValueStore().put(WASHER_STAINS_KEY, value.getValueAsString());
                updateState(channel.getUID(), new StringType(value.getValueAsString()));
            });
            case LAUNDRY_LOAD_INFORMATION_KEY -> updateStateIfLinked(CHANNEL_LAUNDRY_LOAD_INFORMATION,
                    () -> new QuantityType<>(value.getValueAsInt(), GRAM));
            case LAUNDRY_LOAD_RECOMMENDATION_KEY -> updateStateIfLinked(CHANNEL_LAUNDRY_LOAD_RECOMMENDATION,
                    () -> new QuantityType<>(value.getValueAsInt(), GRAM));
            case LAUNDRY_DRUM_CLEAN_REMINDER_KEY -> updateStateIfLinked(CHANNEL_LAUNDRY_DRUM_CLEAN_REMINDER,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case DRYER_DRYING_TARGET_KEY -> getLinkedChannel(CHANNEL_DRYER_DRYING_TARGET).ifPresent(channel -> {
                getKeyValueStore().put(DRYER_DRYING_TARGET_KEY, value.getValueAsString());
                updateState(channel.getUID(), new StringType(value.getValueAsString()));
            });
            case DRYER_WRINKLE_GUARD_KEY -> getLinkedChannel(CHANNEL_DRYER_WRINKLE_GUARD).ifPresent(channel -> {
                getKeyValueStore().put(DRYER_WRINKLE_GUARD_KEY, value.getValueAsString());
                updateState(channel.getUID(), new StringType(value.getValueAsString()));
            });
            case WASHER_SPEED_PERFECT_KEY ->
                updateStateIfLinked(CHANNEL_WASHER_SPEED_PERFECT, () -> OnOffType.from(value.getValueAsBoolean()));
            case WASHER_WATER_PLUS_KEY ->
                updateStateIfLinked(CHANNEL_WASHER_WATER_PLUS, () -> OnOffType.from(value.getValueAsBoolean()));
            case WASHER_PREWASH_KEY ->
                updateStateIfLinked(CHANNEL_WASHER_PREWASH, () -> OnOffType.from(value.getValueAsBoolean()));
            case WASHER_RINSE_HOLD_KEY ->
                updateStateIfLinked(CHANNEL_WASHER_RINSE_HOLD, () -> OnOffType.from(value.getValueAsBoolean()));
            case WASHER_LESS_IRONING_KEY ->
                updateStateIfLinked(CHANNEL_WASHER_LESS_IRONING, () -> OnOffType.from(value.getValueAsBoolean()));
            case WASHER_SILENT_WASH_KEY ->
                updateStateIfLinked(CHANNEL_WASHER_SILENT_WASH, () -> OnOffType.from(value.getValueAsBoolean()));
            case WASHER_SOAK_KEY ->
                updateStateIfLinked(CHANNEL_WASHER_SOAK, () -> OnOffType.from(value.getValueAsBoolean()));
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        initializeState(channelUID.getId());
    }

    private void initializeAllStates() {
        Set.of(CHANNEL_LAUNDRY_CARE_PROCESS_PHASE, CHANNEL_WASHER_TEMPERATURE, CHANNEL_WASHER_SPIN_SPEED,
                CHANNEL_WASHER_RINSE_PLUS, CHANNEL_WASHER_STAINS, CHANNEL_DRYER_DRYING_TARGET,
                CHANNEL_DRYER_WRINKLE_GUARD, CHANNEL_LAUNDRY_DRUM_CLEAN_REMINDER, CHANNEL_WASHER_SPEED_PERFECT,
                CHANNEL_LAUNDRY_LOAD_INFORMATION, CHANNEL_LAUNDRY_LOAD_RECOMMENDATION, CHANNEL_WASHER_WATER_PLUS,
                CHANNEL_WASHER_PREWASH, CHANNEL_WASHER_RINSE_HOLD, CHANNEL_WASHER_LESS_IRONING,
                CHANNEL_WASHER_SILENT_WASH, CHANNEL_WASHER_SOAK, CHANNEL_WASHER_I_DOS_1_ACTIVE,
                CHANNEL_WASHER_I_DOS_2_ACTIVE, CHANNEL_WASHER_I_DOS_1_FILL_LEVEL_POOR,
                CHANNEL_WASHER_I_DOS_2_FILL_LEVEL_POOR).forEach(this::initializeState);
    }

    private void initializeState(String channelId) {
        switch (channelId) {
            case CHANNEL_LAUNDRY_CARE_PROCESS_PHASE ->
                getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
                    if (optionKeyExists(deviceDescriptionService, LAUNDRY_CARE_PROCESS_PHASE_KEY)) {
                        updateReadonlyEnumOptionDescriptionIfLinked(channelId, LAUNDRY_CARE_PROCESS_PHASE_KEY);
                    } else if (optionKeyExists(deviceDescriptionService, DRYER_PROCESS_PHASE_KEY)) {
                        updateReadonlyEnumOptionDescriptionIfLinked(channelId, DRYER_PROCESS_PHASE_KEY);
                    }
                });
            case CHANNEL_WASHER_TEMPERATURE -> updateEnumOptionDescriptionIfLinked(channelId, WASHER_TEMPERATURE_KEY);
            case CHANNEL_WASHER_SPIN_SPEED -> updateEnumOptionDescriptionIfLinked(channelId, WASHER_SPIN_SPEED_KEY);
            case CHANNEL_WASHER_RINSE_PLUS -> updateEnumOptionDescriptionIfLinked(channelId, WASHER_RINSE_PLUS_KEY);
            case CHANNEL_WASHER_STAINS -> updateEnumOptionDescriptionIfLinked(channelId, WASHER_STAINS_KEY);
            case CHANNEL_DRYER_DRYING_TARGET -> updateEnumOptionDescriptionIfLinked(channelId, DRYER_DRYING_TARGET_KEY);
            case CHANNEL_DRYER_WRINKLE_GUARD -> updateEnumOptionDescriptionIfLinked(channelId, DRYER_WRINKLE_GUARD_KEY);
            case CHANNEL_LAUNDRY_DRUM_CLEAN_REMINDER, CHANNEL_WASHER_SPEED_PERFECT, CHANNEL_WASHER_WATER_PLUS,
                    CHANNEL_WASHER_PREWASH, CHANNEL_WASHER_RINSE_HOLD, CHANNEL_WASHER_LESS_IRONING,
                    CHANNEL_WASHER_SILENT_WASH, CHANNEL_WASHER_SOAK, CHANNEL_WASHER_I_DOS_1_ACTIVE,
                    CHANNEL_WASHER_I_DOS_2_ACTIVE, CHANNEL_WASHER_I_DOS_1_FILL_LEVEL_POOR,
                    CHANNEL_WASHER_I_DOS_2_FILL_LEVEL_POOR ->
                updateStateIfLinked(channelId, OnOffType.OFF);
            case CHANNEL_LAUNDRY_LOAD_INFORMATION, CHANNEL_LAUNDRY_LOAD_RECOMMENDATION ->
                updateStateIfLinked(channelId, () -> new QuantityType<>(0, GRAM));
        }
    }

    private void addIDosChannels() {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            var iDosAvailable = deviceDescriptionService.getPrograms(false).stream().anyMatch(program -> program
                    .options().stream().anyMatch(opt -> WASHER_I_DOS_1_ACTIVE_KEY.equals(opt.refKey())));

            if (iDosAvailable) {
                var thingBuilder = editThing();
                boolean channelsChanged = false;

                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_WASHER_I_DOS_1_ACTIVE,
                        CHANNEL_TYPE_I_DOS_ACTIVE, CoreItemFactory.SWITCH,
                        getTranslationProvider().getText(I18N_I_DOS_ACTIVE, 1));
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_WASHER_I_DOS_2_ACTIVE,
                        CHANNEL_TYPE_I_DOS_ACTIVE, CoreItemFactory.SWITCH,
                        getTranslationProvider().getText(I18N_I_DOS_ACTIVE, 2));
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_WASHER_I_DOS_1_FILL_LEVEL_POOR,
                        CHANNEL_TYPE_I_DOS_FILL_LEVEL_POOR, CoreItemFactory.SWITCH,
                        getTranslationProvider().getText(I18N_I_DOS_FILL_LEVEL_POOR, 1));
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_WASHER_I_DOS_2_FILL_LEVEL_POOR,
                        CHANNEL_TYPE_I_DOS_FILL_LEVEL_POOR, CoreItemFactory.SWITCH,
                        getTranslationProvider().getText(I18N_I_DOS_FILL_LEVEL_POOR, 2));

                if (channelsChanged) {
                    updateThing(thingBuilder.build());
                }
            }
        });
    }

    private boolean optionKeyExists(DeviceDescriptionService deviceDescriptionService, String optionKey) {
        return deviceDescriptionService.getOption(optionKey, false, false, false) != null;
    }
}
