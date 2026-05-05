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

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_BUTTON_TONES;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COOKING_LIGHT;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_COOKING_LIGHT_BRIGHTNESS;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_HOOD_INTENSIVE_LEVEL;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_HOOD_VENTING_LEVEL;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COOKING_BUTTON_TONES_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COOKING_LIGHTING_BRIGHTNESS_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.COOKING_LIGHTING_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.HOOD_INTENSIVE_LEVEL_ENUM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.HOOD_INTENSIVE_LEVEL_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.HOOD_VENTING_LEVEL_ENUM_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.HOOD_VENTING_LEVEL_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.POWER_STATE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.ROOT_OPTION_LIST_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_FAN_OFF;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_HOOD_VENTING;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_INTENSIVE_STAGE_OFF;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_OFF;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_ACTIVE_PROGRAM;
import static org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource.RO_VALUES;
import static org.openhab.core.library.unit.Units.PERCENT;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnectdirect.internal.configuration.HomeConnectDirectConfiguration;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.i18n.HomeConnectDirectTranslationProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicCommandDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicStateDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.profile.ApplianceProfileService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.ProgramData;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.data.ValueData;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectDirectHoodHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDirectHoodHandler extends BaseHomeConnectDirectHandler {

    private final Logger logger;

    public HomeConnectDirectHoodHandler(Thing thing, ApplianceProfileService applianceProfileService,
            HomeConnectDirectDynamicCommandDescriptionProvider commandDescriptionProvider,
            HomeConnectDirectDynamicStateDescriptionProvider stateDescriptionProvider, String deviceId,
            HomeConnectDirectConfiguration configuration, HomeConnectDirectTranslationProvider translationProvider) {
        super(thing, applianceProfileService, commandDescriptionProvider, stateDescriptionProvider, deviceId,
                configuration, translationProvider);

        this.logger = LoggerFactory.getLogger(HomeConnectDirectHoodHandler.class);
    }

    @Override
    protected void initializeFinished() {
        initializeAllStates();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_COOKING_LIGHT.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanSettingIfAllowed(command, COOKING_LIGHTING_KEY);
        } else if (CHANNEL_COOKING_LIGHT_BRIGHTNESS.equals(channelUID.getId()) && command instanceof Number quantity) {
            mapSettingKey(COOKING_LIGHTING_BRIGHTNESS_KEY).ifPresent(optionUid -> {
                double value = quantity.doubleValue();

                // percent value
                if (value > 0 && value < 1) {
                    value *= 100;
                }
                if (value > 100) {
                    value = 100;
                }
                send(Action.POST, RO_VALUES, List.of(new ValueData(optionUid, (int) value)), null, 1);
            });
        } else if (CHANNEL_BUTTON_TONES.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanSettingIfAllowed(command, COOKING_BUTTON_TONES_KEY);
        } else if (CHANNEL_HOOD_VENTING_LEVEL.equals(channelUID.getId()) && command instanceof StringType) {
            var programUid = mapProgramKey(STATE_HOOD_VENTING);
            var optionUid = mapOptionKey(HOOD_VENTING_LEVEL_KEY);
            var optionEnumValue = mapEnumerationValueKey(HOOD_VENTING_LEVEL_ENUM_KEY, command.toFullString());

            logger.debug("Setting up venting level state. programUid={} optionUid={}, optionEnumValue={}", programUid,
                    optionUid, optionEnumValue);
            if (Stream.of(programUid, optionUid, optionEnumValue).allMatch(Optional::isPresent)) {
                send(Action.POST, RO_ACTIVE_PROGRAM, List.of(new ProgramData(programUid.get(),
                        List.of(new ValueData(optionUid.get(), optionEnumValue.get())))), null, 1);
            }
        } else if (CHANNEL_HOOD_INTENSIVE_LEVEL.equals(channelUID.getId()) && command instanceof StringType) {
            var programUid = mapProgramKey(STATE_HOOD_VENTING);
            var optionUid = mapOptionKey(HOOD_INTENSIVE_LEVEL_KEY);
            var optionEnumValue = mapEnumerationValueKey(HOOD_INTENSIVE_LEVEL_ENUM_KEY, command.toFullString());

            logger.debug("Setting up intensive level state. programUid={} optionUid={}, optionEnumValue={}", programUid,
                    optionUid, optionEnumValue);
            if (Stream.of(programUid, optionUid, optionEnumValue).allMatch(Optional::isPresent)) {
                send(Action.POST, RO_ACTIVE_PROGRAM, List.of(new ProgramData(programUid.get(),
                        List.of(new ValueData(optionUid.get(), optionEnumValue.get())))), null, 1);
            }
        }
    }

    @Override
    protected void onApplianceDescriptionChangeEvent(List<DeviceDescriptionChange> deviceDescriptionChanges) {
        super.onApplianceDescriptionChangeEvent(deviceDescriptionChanges);

        deviceDescriptionChanges.forEach(deviceDescriptionChange -> {
            var key = deviceDescriptionChange.key();
            switch (key) {
                case HOOD_VENTING_LEVEL_KEY -> updateEnumOptionDescriptionIfLinked(CHANNEL_HOOD_VENTING_LEVEL, key);
                case HOOD_INTENSIVE_LEVEL_KEY -> updateEnumOptionDescriptionIfLinked(CHANNEL_HOOD_INTENSIVE_LEVEL, key);
                case ROOT_OPTION_LIST_KEY -> {
                    updateEnumOptionDescriptionIfLinked(CHANNEL_HOOD_VENTING_LEVEL, key);
                    updateEnumOptionDescriptionIfLinked(CHANNEL_HOOD_INTENSIVE_LEVEL, key);
                }
            }
        });
    }

    @Override
    protected void onApplianceValueEvent(Value value, Resource resource) {
        super.onApplianceValueEvent(value, resource);

        switch (value.key()) {
            case COOKING_LIGHTING_KEY ->
                updateStateIfLinked(CHANNEL_COOKING_LIGHT, OnOffType.from(value.getValueAsBoolean()));
            case COOKING_LIGHTING_BRIGHTNESS_KEY -> updateStateIfLinked(CHANNEL_COOKING_LIGHT_BRIGHTNESS,
                    () -> new QuantityType<>(value.getValueAsInt(), PERCENT));
            case COOKING_BUTTON_TONES_KEY ->
                updateStateIfLinked(CHANNEL_BUTTON_TONES, OnOffType.from(value.getValueAsBoolean()));
            case HOOD_VENTING_LEVEL_KEY -> {
                updateStateIfLinked(CHANNEL_HOOD_VENTING_LEVEL, new StringType(value.getValueAsString()));
                if (!STATE_FAN_OFF.equals(value.value())) {
                    updateStateIfLinked(CHANNEL_HOOD_INTENSIVE_LEVEL, new StringType(STATE_INTENSIVE_STAGE_OFF));
                }
            }
            case HOOD_INTENSIVE_LEVEL_KEY -> {
                updateStateIfLinked(CHANNEL_HOOD_INTENSIVE_LEVEL, new StringType(value.getValueAsString()));
                if (!STATE_INTENSIVE_STAGE_OFF.equals(value.value())) {
                    updateStateIfLinked(CHANNEL_HOOD_VENTING_LEVEL, new StringType(STATE_FAN_OFF));
                }
            }
            case POWER_STATE_KEY -> {
                if (STATE_OFF.equalsIgnoreCase(value.getValueAsString())) {
                    updateStateIfLinked(CHANNEL_HOOD_VENTING_LEVEL, new StringType(STATE_FAN_OFF));
                    updateStateIfLinked(CHANNEL_HOOD_INTENSIVE_LEVEL, new StringType(STATE_INTENSIVE_STAGE_OFF));
                }
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        initializeState(channelUID.getId());
    }

    private void initializeAllStates() {
        Set.of(CHANNEL_BUTTON_TONES, CHANNEL_HOOD_VENTING_LEVEL, CHANNEL_HOOD_INTENSIVE_LEVEL, CHANNEL_COOKING_LIGHT,
                CHANNEL_COOKING_LIGHT_BRIGHTNESS).forEach(this::initializeState);
    }

    private void initializeState(String channelId) {
        switch (channelId) {
            case CHANNEL_BUTTON_TONES, CHANNEL_COOKING_LIGHT -> updateStateIfLinked(channelId, OnOffType.OFF);
            case CHANNEL_HOOD_VENTING_LEVEL -> {
                updateState(channelId, new StringType(STATE_FAN_OFF));
                updateEnumOptionDescriptionIfLinked(channelId, HOOD_VENTING_LEVEL_KEY);
            }
            case CHANNEL_HOOD_INTENSIVE_LEVEL -> {
                updateState(channelId, new StringType(STATE_INTENSIVE_STAGE_OFF));
                updateEnumOptionDescriptionIfLinked(channelId, HOOD_INTENSIVE_LEVEL_KEY);
            }
            case CHANNEL_COOKING_LIGHT_BRIGHTNESS -> updateStateIfLinked(channelId, new QuantityType<>(0, PERCENT));
        }
    }
}
