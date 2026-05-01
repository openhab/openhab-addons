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

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DISHWASHER_BRILLIANCE_DRY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DISHWASHER_INTENSIV_ZONE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DISHWASHER_MACHINE_CARE_REMINDER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DISHWASHER_PROGRAM_PHASE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DISHWASHER_RINSE_AID_LACK;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DISHWASHER_RINSE_AID_NEARLY_EMPTY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DISHWASHER_SALT_LACK;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DISHWASHER_SALT_NEARLY_EMPTY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DISHWASHER_VARIO_SPEED_PLUS;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DISHWASHER_BRILLIANCE_DRY_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DISHWASHER_INTENSIV_ZONE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DISHWASHER_MACHINE_CARE_REMINDER_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DISHWASHER_PROGRAM_PHASE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DISHWASHER_RINSE_AID_LACK_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DISHWASHER_RINSE_AID_NEARLY_EMPTY_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DISHWASHER_SALT_LACK_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DISHWASHER_SALT_NEARLY_EMPTY_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.DISHWASHER_VARIO_SPEED_PLUS_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_CONFIRMED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_PRESENT;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnectdirect.internal.configuration.HomeConnectDirectConfiguration;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.i18n.HomeConnectDirectTranslationProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicCommandDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicStateDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.profile.ApplianceProfileService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link HomeConnectDirectDishwasherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDirectDishwasherHandler extends BaseHomeConnectDirectHandler {

    public HomeConnectDirectDishwasherHandler(Thing thing, ApplianceProfileService applianceProfileService,
            HomeConnectDirectDynamicCommandDescriptionProvider commandDescriptionProvider,
            HomeConnectDirectDynamicStateDescriptionProvider stateDescriptionProvider, String deviceId,
            HomeConnectDirectConfiguration configuration, HomeConnectDirectTranslationProvider translationProvider) {
        super(thing, applianceProfileService, commandDescriptionProvider, stateDescriptionProvider, deviceId,
                configuration, translationProvider);
    }

    @Override
    protected void initializeFinished() {
        initializeAllStates();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_DISHWASHER_VARIO_SPEED_PLUS.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, DISHWASHER_VARIO_SPEED_PLUS_KEY);
        } else if (CHANNEL_DISHWASHER_INTENSIV_ZONE.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, DISHWASHER_INTENSIV_ZONE_KEY);
        } else if (CHANNEL_DISHWASHER_BRILLIANCE_DRY.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanOptionIfAllowed(command, DISHWASHER_BRILLIANCE_DRY_KEY);
        }
    }

    @Override
    protected void onApplianceDescriptionChangeEvent(List<DeviceDescriptionChange> deviceDescriptionChanges) {
        super.onApplianceDescriptionChangeEvent(deviceDescriptionChanges);

        deviceDescriptionChanges.forEach(deviceDescriptionChange -> {
            if (deviceDescriptionChange.key().equals(DISHWASHER_PROGRAM_PHASE_KEY)) {
                updateStatusDescriptionIfLinked(CHANNEL_DISHWASHER_PROGRAM_PHASE, DISHWASHER_PROGRAM_PHASE_KEY);
            }
        });
    }

    @Override
    protected void onApplianceValueEvent(Value value, Resource resource) {
        super.onApplianceValueEvent(value, resource);

        switch (value.key()) {
            case DISHWASHER_PROGRAM_PHASE_KEY ->
                updateStateIfLinked(CHANNEL_DISHWASHER_PROGRAM_PHASE, new StringType(value.getValueAsString()));
            case DISHWASHER_SALT_LACK_KEY -> updateStateIfLinked(CHANNEL_DISHWASHER_SALT_LACK,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case DISHWASHER_RINSE_AID_LACK_KEY -> updateStateIfLinked(CHANNEL_DISHWASHER_RINSE_AID_LACK,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case DISHWASHER_SALT_NEARLY_EMPTY_KEY -> updateStateIfLinked(CHANNEL_DISHWASHER_SALT_NEARLY_EMPTY,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case DISHWASHER_RINSE_AID_NEARLY_EMPTY_KEY -> updateStateIfLinked(CHANNEL_DISHWASHER_RINSE_AID_NEARLY_EMPTY,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case DISHWASHER_MACHINE_CARE_REMINDER_KEY -> updateStateIfLinked(CHANNEL_DISHWASHER_MACHINE_CARE_REMINDER,
                    () -> OnOffType.from(STATE_PRESENT.equalsIgnoreCase(value.getValueAsString())
                            || STATE_CONFIRMED.equalsIgnoreCase(value.getValueAsString())));
            case DISHWASHER_VARIO_SPEED_PLUS_KEY -> updateStateIfLinked(CHANNEL_DISHWASHER_VARIO_SPEED_PLUS,
                    () -> OnOffType.from(value.getValueAsBoolean()));
            case DISHWASHER_INTENSIV_ZONE_KEY ->
                updateStateIfLinked(CHANNEL_DISHWASHER_INTENSIV_ZONE, () -> OnOffType.from(value.getValueAsBoolean()));
            case DISHWASHER_BRILLIANCE_DRY_KEY ->
                updateStateIfLinked(CHANNEL_DISHWASHER_BRILLIANCE_DRY, () -> OnOffType.from(value.getValueAsBoolean()));
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        initializeState(channelUID.getId());
    }

    private void initializeAllStates() {
        Arrays.asList(CHANNEL_DISHWASHER_PROGRAM_PHASE, CHANNEL_DISHWASHER_SALT_LACK, CHANNEL_DISHWASHER_RINSE_AID_LACK,
                CHANNEL_DISHWASHER_SALT_NEARLY_EMPTY, CHANNEL_DISHWASHER_RINSE_AID_NEARLY_EMPTY,
                CHANNEL_DISHWASHER_MACHINE_CARE_REMINDER, CHANNEL_DISHWASHER_VARIO_SPEED_PLUS,
                CHANNEL_DISHWASHER_INTENSIV_ZONE, CHANNEL_DISHWASHER_BRILLIANCE_DRY).forEach(this::initializeState);
    }

    private void initializeState(String channelId) {
        switch (channelId) {
            case CHANNEL_DISHWASHER_PROGRAM_PHASE ->
                updateStatusDescriptionIfLinked(channelId, DISHWASHER_PROGRAM_PHASE_KEY);
            case CHANNEL_DISHWASHER_SALT_LACK, CHANNEL_DISHWASHER_RINSE_AID_LACK, CHANNEL_DISHWASHER_SALT_NEARLY_EMPTY,
                    CHANNEL_DISHWASHER_RINSE_AID_NEARLY_EMPTY, CHANNEL_DISHWASHER_MACHINE_CARE_REMINDER,
                    CHANNEL_DISHWASHER_VARIO_SPEED_PLUS, CHANNEL_DISHWASHER_INTENSIV_ZONE,
                    CHANNEL_DISHWASHER_BRILLIANCE_DRY ->
                updateStateIfLinked(channelId, OnOffType.OFF);
        }
    }
}
