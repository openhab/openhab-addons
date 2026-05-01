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

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_DOOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FREEZER_DOOR_STATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FREEZER_SET_POINT_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FREEZER_SUPER_MODE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FRIDGE_CHILLER_DOOR_STATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FRIDGE_CHILLER_PRESET;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FRIDGE_CHILLER_SET_POINT_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FRIDGE_DISPENSER_ENABLED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FRIDGE_DISPENSER_PARTY_MODE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FRIDGE_DISPENSER_WATER_FILTER_SATURATION;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FRIDGE_DOOR_STATE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FRIDGE_SET_POINT_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_FRIDGE_SUPER_MODE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_DOOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_FRIDGE_CHILLER_PRESET;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_FRIDGE_DISPENSER_ENABLED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_FRIDGE_DISPENSER_PARTY_MODE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_FRIDGE_DISPENSER_WATER_FILTER_SATURATION;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_FRIDGE_FREEZER_SET_POINT_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CHANNEL_TYPE_FRIDGE_FREEZER_SUPER_MODE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FREEZER_DOOR_STATE_2_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FREEZER_DOOR_STATE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FREEZER_SET_POINT_TEMPERATURE_2_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FREEZER_SET_POINT_TEMPERATURE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FREEZER_SUPER_MODE_2_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FREEZER_SUPER_MODE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_CHILLER_DOOR_STATE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_CHILLER_PRESET_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_CHILLER_SET_POINT_TEMPERATURE_2_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_CHILLER_SET_POINT_TEMPERATURE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_DISPENSER_ENABLED_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_DISPENSER_PARTY_MODE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_DISPENSER_WATER_FILTER_SATURATION_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_DOOR_STATE_2_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_DOOR_STATE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_SET_POINT_TEMPERATURE_2_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_SET_POINT_TEMPERATURE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_SUPER_MODE_2_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.FRIDGE_SUPER_MODE_KEY;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_DOOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_FRIDGE_CHILLER_PRESET;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_FRIDGE_DISPENSER_ENABLED;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_FRIDGE_DISPENSER_PARTY_MODE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_FRIDGE_DISPENSER_WATER_FILTER_SATURATION;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_FRIDGE_FREEZER_DOOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_FRIDGE_FREEZER_SET_POINT_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.I18N_FRIDGE_FREEZER_SUPER_MODE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.NUMBER_DIMENSIONLESS;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.NUMBER_TEMPERATURE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_AJAR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_OPEN;
import static org.openhab.core.library.unit.ImperialUnits.FAHRENHEIT;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.PERCENT;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnectdirect.internal.configuration.HomeConnectDirectConfiguration;
import org.openhab.binding.homeconnectdirect.internal.handler.model.Value;
import org.openhab.binding.homeconnectdirect.internal.i18n.HomeConnectDirectTranslationProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicCommandDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.provider.HomeConnectDirectDynamicStateDescriptionProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.DeviceDescriptionService;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.ContentType;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.change.DeviceDescriptionChange;
import org.openhab.binding.homeconnectdirect.internal.service.profile.ApplianceProfileService;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectDirectFridgeFreezerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDirectFridgeFreezerHandler extends BaseHomeConnectDirectHandler {

    private static final String FREEZER = "Freezer";
    private static final String FRIDGE = "Fridge";
    private static final String CHILLER = "Chiller";

    private final Logger logger;

    public HomeConnectDirectFridgeFreezerHandler(Thing thing, ApplianceProfileService applianceProfileService,
            HomeConnectDirectDynamicCommandDescriptionProvider commandDescriptionProvider,
            HomeConnectDirectDynamicStateDescriptionProvider stateDescriptionProvider, String deviceId,
            HomeConnectDirectConfiguration configuration, HomeConnectDirectTranslationProvider translationProvider) {
        super(thing, applianceProfileService, commandDescriptionProvider, stateDescriptionProvider, deviceId,
                configuration, translationProvider);

        this.logger = LoggerFactory.getLogger(HomeConnectDirectFridgeFreezerHandler.class);
    }

    @Override
    protected void initializeStarted() {
        addDynamicChannels();
    }

    @Override
    protected void initializeFinished() {
        initializeAllStates();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_FRIDGE_SUPER_MODE.equals(channelUID.getId()) && command instanceof OnOffType) {
            if (settingKeyExists(FRIDGE_SUPER_MODE_2_KEY)) {
                sendBooleanSettingIfAllowed(command, FRIDGE_SUPER_MODE_2_KEY);
            } else {
                sendBooleanSettingIfAllowed(command, FRIDGE_SUPER_MODE_KEY);
            }
        } else if (CHANNEL_FREEZER_SUPER_MODE.equals(channelUID.getId()) && command instanceof OnOffType) {
            if (settingKeyExists(FREEZER_SUPER_MODE_2_KEY)) {
                sendBooleanSettingIfAllowed(command, FREEZER_SUPER_MODE_2_KEY);
            } else {
                sendBooleanSettingIfAllowed(command, FREEZER_SUPER_MODE_KEY);
            }
        } else if (CHANNEL_FRIDGE_SET_POINT_TEMPERATURE.equals(channelUID.getId())
                && (command instanceof QuantityType<?> || command instanceof DecimalType)) {
            if (settingKeyExists(FRIDGE_SET_POINT_TEMPERATURE_2_KEY)) {
                sendTemperatureSetting(FRIDGE_SET_POINT_TEMPERATURE_2_KEY, command);
            } else {
                sendTemperatureSetting(FRIDGE_SET_POINT_TEMPERATURE_KEY, command);
            }
        } else if (CHANNEL_FREEZER_SET_POINT_TEMPERATURE.equals(channelUID.getId())
                && (command instanceof QuantityType<?> || command instanceof DecimalType)) {
            if (settingKeyExists(FREEZER_SET_POINT_TEMPERATURE_2_KEY)) {
                sendTemperatureSetting(FREEZER_SET_POINT_TEMPERATURE_2_KEY, command);
            } else {
                sendTemperatureSetting(FREEZER_SET_POINT_TEMPERATURE_KEY, command);
            }
        } else if (CHANNEL_FRIDGE_CHILLER_SET_POINT_TEMPERATURE.equals(channelUID.getId())
                && (command instanceof QuantityType<?> || command instanceof DecimalType)) {
            if (settingKeyExists(FRIDGE_CHILLER_SET_POINT_TEMPERATURE_2_KEY)) {
                sendTemperatureSetting(FRIDGE_CHILLER_SET_POINT_TEMPERATURE_2_KEY, command);
            } else {
                sendTemperatureSetting(FRIDGE_CHILLER_SET_POINT_TEMPERATURE_KEY, command);
            }
        } else if (CHANNEL_FRIDGE_CHILLER_PRESET.equals(channelUID.getId()) && command instanceof StringType) {
            sendEnumSettingIfAllowed(command, FRIDGE_CHILLER_PRESET_KEY);
        } else if (CHANNEL_FRIDGE_DISPENSER_ENABLED.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanSettingIfAllowed(command, FRIDGE_DISPENSER_ENABLED_KEY);
        } else if (CHANNEL_FRIDGE_DISPENSER_PARTY_MODE.equals(channelUID.getId()) && command instanceof OnOffType) {
            sendBooleanSettingIfAllowed(command, FRIDGE_DISPENSER_PARTY_MODE_KEY);
        }
    }

    @Override
    protected void onApplianceDescriptionChangeEvent(List<DeviceDescriptionChange> deviceDescriptionChanges) {
        super.onApplianceDescriptionChangeEvent(deviceDescriptionChanges);

        deviceDescriptionChanges.forEach(deviceDescriptionChange -> {
            var key = deviceDescriptionChange.key();
            if (key.equals(FRIDGE_CHILLER_PRESET_KEY)) {
                updateEnumOptionDescriptionIfLinked(CHANNEL_FRIDGE_CHILLER_PRESET, key);
            }
        });
    }

    @Override
    protected void onApplianceValueEvent(Value value, Resource resource) {
        super.onApplianceValueEvent(value, resource);

        switch (value.key()) {
            case FRIDGE_SET_POINT_TEMPERATURE_KEY, FRIDGE_SET_POINT_TEMPERATURE_2_KEY ->
                updateStateIfLinked(CHANNEL_FRIDGE_SET_POINT_TEMPERATURE,
                        () -> new QuantityType<>(value.getValueAsInt(), getTemperatureUnitOfSetting(value.key())));
            case FREEZER_SET_POINT_TEMPERATURE_KEY, FREEZER_SET_POINT_TEMPERATURE_2_KEY ->
                updateStateIfLinked(CHANNEL_FREEZER_SET_POINT_TEMPERATURE,
                        () -> new QuantityType<>(value.getValueAsInt(), getTemperatureUnitOfSetting(value.key())));
            case FRIDGE_CHILLER_SET_POINT_TEMPERATURE_KEY, FRIDGE_CHILLER_SET_POINT_TEMPERATURE_2_KEY ->
                updateStateIfLinked(CHANNEL_FRIDGE_CHILLER_SET_POINT_TEMPERATURE,
                        () -> new QuantityType<>(value.getValueAsInt(), getTemperatureUnitOfSetting(value.key())));
            case FRIDGE_SUPER_MODE_KEY, FRIDGE_SUPER_MODE_2_KEY ->
                updateStateIfLinked(CHANNEL_FRIDGE_SUPER_MODE, OnOffType.from(value.getValueAsBoolean()));
            case FREEZER_SUPER_MODE_KEY, FREEZER_SUPER_MODE_2_KEY ->
                updateStateIfLinked(CHANNEL_FREEZER_SUPER_MODE, OnOffType.from(value.getValueAsBoolean()));
            case FRIDGE_DOOR_STATE_KEY,
                    FRIDGE_DOOR_STATE_2_KEY ->
                updateStateIfLinked(CHANNEL_FRIDGE_DOOR_STATE,
                        () -> STATE_OPEN.equals(value.value()) || STATE_AJAR.equals(value.value()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
            case FREEZER_DOOR_STATE_KEY,
                    FREEZER_DOOR_STATE_2_KEY ->
                updateStateIfLinked(CHANNEL_FREEZER_DOOR_STATE,
                        () -> STATE_OPEN.equals(value.value()) || STATE_AJAR.equals(value.value()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
            case FRIDGE_CHILLER_DOOR_STATE_KEY -> updateStateIfLinked(CHANNEL_FRIDGE_CHILLER_DOOR_STATE,
                    () -> STATE_OPEN.equals(value.value()) || STATE_AJAR.equals(value.value()) ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            case FRIDGE_DISPENSER_WATER_FILTER_SATURATION_KEY ->
                updateStateIfLinked(CHANNEL_FRIDGE_DISPENSER_WATER_FILTER_SATURATION,
                        () -> new QuantityType<>(value.getValueAsInt(), PERCENT));
            case FRIDGE_CHILLER_PRESET_KEY -> getLinkedChannel(CHANNEL_FRIDGE_CHILLER_PRESET)
                    .ifPresent(channel -> updateState(channel.getUID(), new StringType(value.getValueAsString())));
            case FRIDGE_DISPENSER_ENABLED_KEY ->
                updateStateIfLinked(CHANNEL_FRIDGE_DISPENSER_ENABLED, OnOffType.from(value.getValueAsBoolean()));
            case FRIDGE_DISPENSER_PARTY_MODE_KEY ->
                updateStateIfLinked(CHANNEL_FRIDGE_DISPENSER_PARTY_MODE, OnOffType.from(value.getValueAsBoolean()));
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        initializeState(channelUID.getId());
    }

    private void initializeAllStates() {
        Set.of(CHANNEL_FRIDGE_SUPER_MODE, CHANNEL_FREEZER_SUPER_MODE, CHANNEL_FRIDGE_DOOR_STATE,
                CHANNEL_FREEZER_DOOR_STATE, CHANNEL_FRIDGE_CHILLER_DOOR_STATE,
                CHANNEL_FRIDGE_DISPENSER_WATER_FILTER_SATURATION, CHANNEL_FRIDGE_CHILLER_PRESET,
                CHANNEL_FRIDGE_DISPENSER_PARTY_MODE, CHANNEL_FRIDGE_DISPENSER_ENABLED).forEach(this::initializeState);
    }

    private void initializeState(String channelId) {
        switch (channelId) {
            case CHANNEL_FRIDGE_SUPER_MODE, CHANNEL_FREEZER_SUPER_MODE, CHANNEL_FRIDGE_DISPENSER_PARTY_MODE,
                    CHANNEL_FRIDGE_DISPENSER_ENABLED ->
                updateState(channelId, OnOffType.OFF);
            case CHANNEL_FRIDGE_DOOR_STATE, CHANNEL_FREEZER_DOOR_STATE, CHANNEL_FRIDGE_CHILLER_DOOR_STATE ->
                updateState(channelId, OpenClosedType.CLOSED);
            case CHANNEL_FRIDGE_DISPENSER_WATER_FILTER_SATURATION ->
                updateState(channelId, new QuantityType<>(0, PERCENT));
            case CHANNEL_FRIDGE_CHILLER_PRESET ->
                updateEnumOptionDescriptionIfLinked(channelId, FRIDGE_CHILLER_PRESET_KEY);
        }
    }

    private void addDynamicChannels() {
        getDeviceDescriptionServiceOptional().ifPresent(deviceDescriptionService -> {
            var thingBuilder = editThing();
            boolean channelsChanged = false;

            // setpoint temperatures
            if (settingKeyExists(deviceDescriptionService, FRIDGE_SET_POINT_TEMPERATURE_KEY)
                    || settingKeyExists(deviceDescriptionService, FRIDGE_SET_POINT_TEMPERATURE_2_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FRIDGE_SET_POINT_TEMPERATURE,
                        CHANNEL_TYPE_FRIDGE_FREEZER_SET_POINT_TEMPERATURE, NUMBER_TEMPERATURE,
                        getTranslationProvider().getText(I18N_FRIDGE_FREEZER_SET_POINT_TEMPERATURE,
                                getTranslationProvider().getText(FRIDGE)));
            }

            if (settingKeyExists(deviceDescriptionService, FREEZER_SET_POINT_TEMPERATURE_KEY)
                    || settingKeyExists(deviceDescriptionService, FREEZER_SET_POINT_TEMPERATURE_2_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FREEZER_SET_POINT_TEMPERATURE,
                        CHANNEL_TYPE_FRIDGE_FREEZER_SET_POINT_TEMPERATURE, NUMBER_TEMPERATURE,
                        getTranslationProvider().getText(I18N_FRIDGE_FREEZER_SET_POINT_TEMPERATURE,
                                getTranslationProvider().getText(FREEZER)));
            }

            if (settingKeyExists(deviceDescriptionService, FRIDGE_CHILLER_SET_POINT_TEMPERATURE_KEY)
                    || settingKeyExists(deviceDescriptionService, FRIDGE_CHILLER_SET_POINT_TEMPERATURE_2_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FRIDGE_CHILLER_SET_POINT_TEMPERATURE,
                        CHANNEL_TYPE_FRIDGE_FREEZER_SET_POINT_TEMPERATURE, NUMBER_TEMPERATURE,
                        getTranslationProvider().getText(I18N_FRIDGE_FREEZER_SET_POINT_TEMPERATURE,
                                getTranslationProvider().getText(CHILLER)));
            }

            // door states
            var doorAdded = false;
            if (statusKeyExists(deviceDescriptionService, FRIDGE_DOOR_STATE_KEY)
                    || statusKeyExists(deviceDescriptionService, FRIDGE_DOOR_STATE_2_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FRIDGE_DOOR_STATE, CHANNEL_TYPE_DOOR,
                        CoreItemFactory.CONTACT, getTranslationProvider().getText(I18N_FRIDGE_FREEZER_DOOR,
                                getTranslationProvider().getText(FRIDGE)));
                doorAdded = true;
            }
            if (statusKeyExists(deviceDescriptionService, FRIDGE_CHILLER_DOOR_STATE_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FRIDGE_CHILLER_DOOR_STATE,
                        CHANNEL_TYPE_DOOR, CoreItemFactory.CONTACT, getTranslationProvider()
                                .getText(I18N_FRIDGE_FREEZER_DOOR, getTranslationProvider().getText(CHILLER)));
                doorAdded = true;
            }
            if (statusKeyExists(deviceDescriptionService, FREEZER_DOOR_STATE_KEY)
                    || statusKeyExists(deviceDescriptionService, FREEZER_DOOR_STATE_2_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FREEZER_DOOR_STATE, CHANNEL_TYPE_DOOR,
                        CoreItemFactory.CONTACT, getTranslationProvider().getText(I18N_FRIDGE_FREEZER_DOOR,
                                getTranslationProvider().getText(FREEZER)));
                doorAdded = true;
            }

            if (!doorAdded) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_DOOR, CHANNEL_TYPE_DOOR,
                        CoreItemFactory.CONTACT, getTranslationProvider().getText(I18N_DOOR));
            }

            // super modes
            if (settingKeyExists(deviceDescriptionService, FRIDGE_SUPER_MODE_KEY)
                    || settingKeyExists(deviceDescriptionService, FRIDGE_SUPER_MODE_2_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FRIDGE_SUPER_MODE,
                        CHANNEL_TYPE_FRIDGE_FREEZER_SUPER_MODE, CoreItemFactory.SWITCH, getTranslationProvider()
                                .getText(I18N_FRIDGE_FREEZER_SUPER_MODE, getTranslationProvider().getText(FRIDGE)));
            }

            if (settingKeyExists(deviceDescriptionService, FREEZER_SUPER_MODE_KEY)
                    || settingKeyExists(deviceDescriptionService, FREEZER_SUPER_MODE_2_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FREEZER_SUPER_MODE,
                        CHANNEL_TYPE_FRIDGE_FREEZER_SUPER_MODE, CoreItemFactory.SWITCH, getTranslationProvider()
                                .getText(I18N_FRIDGE_FREEZER_SUPER_MODE, getTranslationProvider().getText(FREEZER)));
            }

            // dispenser
            if (statusKeyExists(deviceDescriptionService, FRIDGE_DISPENSER_WATER_FILTER_SATURATION_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FRIDGE_DISPENSER_WATER_FILTER_SATURATION,
                        CHANNEL_TYPE_FRIDGE_DISPENSER_WATER_FILTER_SATURATION, NUMBER_DIMENSIONLESS,
                        getTranslationProvider().getText(I18N_FRIDGE_DISPENSER_WATER_FILTER_SATURATION));
            }
            if (settingKeyExists(deviceDescriptionService, FRIDGE_DISPENSER_ENABLED_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FRIDGE_DISPENSER_ENABLED,
                        CHANNEL_TYPE_FRIDGE_DISPENSER_ENABLED, CoreItemFactory.SWITCH,
                        getTranslationProvider().getText(I18N_FRIDGE_DISPENSER_ENABLED));
            }
            if (settingKeyExists(deviceDescriptionService, FRIDGE_DISPENSER_PARTY_MODE_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FRIDGE_DISPENSER_PARTY_MODE,
                        CHANNEL_TYPE_FRIDGE_DISPENSER_PARTY_MODE, CoreItemFactory.SWITCH,
                        getTranslationProvider().getText(I18N_FRIDGE_DISPENSER_PARTY_MODE));
            }

            // chiller preset
            if (settingKeyExists(deviceDescriptionService, FRIDGE_CHILLER_PRESET_KEY)) {
                channelsChanged |= addChannelIfNotExist(thingBuilder, CHANNEL_FRIDGE_CHILLER_PRESET,
                        CHANNEL_TYPE_FRIDGE_CHILLER_PRESET, CoreItemFactory.STRING,
                        getTranslationProvider().getText(I18N_FRIDGE_CHILLER_PRESET));
            }

            // update channels
            if (channelsChanged) {
                updateThing(thingBuilder.build());
            }
        });
    }

    private Unit<Temperature> getTemperatureUnitOfSetting(String settingKey) {
        var unit = getDeviceDescriptionServiceOptional().map(deviceDescriptionService -> {
            var setting = deviceDescriptionService.findSettingByKey(settingKey);
            if (setting != null) {
                return ContentType.TEMPERATURE_CELSIUS.equals(setting.contentType()) ? CELSIUS : FAHRENHEIT;
            }
            return CELSIUS;
        }).orElse(null);
        return Objects.requireNonNullElse(unit, CELSIUS);
    }

    private void sendTemperatureSetting(String settingKey, Command command) {
        var unit = getTemperatureUnitOfSetting(settingKey);
        QuantityType<?> quantity;
        if (command instanceof QuantityType<?> qt) {
            quantity = qt;
        } else if (command instanceof DecimalType dt) {
            quantity = new QuantityType<>(dt, unit);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported command type: " + command.getClass().getName() + " for setting " + settingKey);
        }
        var temperatureQuantityType = quantity.toUnit(unit);
        if (temperatureQuantityType != null) {
            sendIntegerSettingIfAllowed(temperatureQuantityType, settingKey);
        } else {
            logger.warn("Could not set temperature! uid={}", getThing().getUID());
        }
    }

    private boolean settingKeyExists(String settingKey) {
        var deviceDescriptionService = getDeviceDescriptionService();
        return deviceDescriptionService != null && settingKeyExists(deviceDescriptionService, settingKey);
    }

    private boolean settingKeyExists(DeviceDescriptionService deviceDescriptionService, String settingKey) {
        return deviceDescriptionService.getSetting(settingKey, false, false, false) != null;
    }

    private boolean statusKeyExists(DeviceDescriptionService deviceDescriptionService, String statusKey) {
        return deviceDescriptionService.getStatus(statusKey, false, false, false) != null;
    }
}
