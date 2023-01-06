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
package org.openhab.binding.miele.internal.handler;

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miele.internal.DeviceUtil;
import org.openhab.binding.miele.internal.MieleTranslationProvider;
import org.openhab.binding.miele.internal.TimeStabilizer;
import org.openhab.binding.miele.internal.api.dto.DeviceClassObject;
import org.openhab.binding.miele.internal.api.dto.DeviceMetaData;
import org.openhab.binding.miele.internal.api.dto.DeviceProperty;
import org.openhab.binding.miele.internal.api.dto.HomeDevice;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link MieleApplianceHandler} is an abstract class
 * responsible for handling commands, which are sent to one
 * of the channels of the appliance that understands/"talks"
 * the {@link ApplianceChannelSelector} datapoints
 *
 * @author Karel Goderis - Initial contribution
 * @author Martin Lepsy - Added check for JsonNull result
 * @author Jacob Laursen - Fixed multicast and protocol support (ZigBee/LAN)
 */
@NonNullByDefault
public abstract class MieleApplianceHandler<E extends Enum<E> & ApplianceChannelSelector> extends BaseThingHandler
        implements ApplianceStatusListener {

    private final Logger logger = LoggerFactory.getLogger(MieleApplianceHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_DISHWASHER, THING_TYPE_OVEN,
            THING_TYPE_FRIDGE, THING_TYPE_DRYER, THING_TYPE_HOB, THING_TYPE_FRIDGEFREEZER, THING_TYPE_HOOD,
            THING_TYPE_WASHINGMACHINE, THING_TYPE_COFFEEMACHINE);

    protected Gson gson = new Gson();

    protected @Nullable String applianceId;
    private @Nullable MieleBridgeHandler bridgeHandler;
    protected TranslationProvider i18nProvider;
    protected LocaleProvider localeProvider;
    protected MieleTranslationProvider translationProvider;
    private TimeZoneProvider timeZoneProvider;
    private TimeStabilizer startTimeStabilizer;
    private TimeStabilizer finishTimeStabilizer;
    private Class<E> selectorType;
    protected String modelID;

    protected Map<String, String> metaDataCache = new HashMap<>();

    public MieleApplianceHandler(Thing thing, TranslationProvider i18nProvider, LocaleProvider localeProvider,
            TimeZoneProvider timeZoneProvider, Class<E> selectorType, String modelID) {
        super(thing);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.selectorType = selectorType;
        this.modelID = modelID;
        this.translationProvider = new MieleTranslationProvider(i18nProvider, localeProvider);
        this.timeZoneProvider = timeZoneProvider;
        this.startTimeStabilizer = new TimeStabilizer();
        this.finishTimeStabilizer = new TimeStabilizer();
    }

    public ApplianceChannelSelector getValueSelectorFromChannelID(String valueSelectorText)
            throws IllegalArgumentException {
        E[] enumConstants = selectorType.getEnumConstants();
        if (enumConstants == null) {
            throw new IllegalArgumentException(
                    String.format("Could not get enum constants for value selector: %s", valueSelectorText));
        }
        for (ApplianceChannelSelector c : enumConstants) {
            if (c.getChannelID().equals(valueSelectorText)) {
                return c;
            }
        }

        throw new IllegalArgumentException(String.format("Not valid value selector: %s", valueSelectorText));
    }

    public ApplianceChannelSelector getValueSelectorFromMieleID(String valueSelectorText)
            throws IllegalArgumentException {
        E[] enumConstants = selectorType.getEnumConstants();
        if (enumConstants == null) {
            throw new IllegalArgumentException(
                    String.format("Could not get enum constants for value selector: %s", valueSelectorText));
        }
        for (ApplianceChannelSelector c : enumConstants) {
            if (!c.getMieleID().isEmpty() && c.getMieleID().equals(valueSelectorText)) {
                return c;
            }
        }

        throw new IllegalArgumentException(String.format("Not valid value selector: %s", valueSelectorText));
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for thing {}", getThing().getUID());
        final String applianceId = (String) getThing().getConfiguration().getProperties().get(APPLIANCE_ID);
        if (applianceId == null || applianceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.uid-not-set");
            return;
        }
        this.applianceId = applianceId;
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.bridge-missing");
            return;
        }
        initializeTranslationProvider(bridge);
        updateStatus(ThingStatus.UNKNOWN);

        MieleBridgeHandler bridgeHandler = getMieleBridgeHandler();
        if (bridgeHandler != null) {
            bridgeHandler.registerApplianceStatusListener(applianceId, this);
        }
    }

    private void initializeTranslationProvider(Bridge bridge) {
        Locale locale = null;
        String language = (String) bridge.getConfiguration().get(LANGUAGE);
        if (language != null && !language.isBlank()) {
            try {
                locale = new Locale.Builder().setLanguageTag(language).build();
            } catch (IllformedLocaleException e) {
                logger.warn("Invalid language configured: {}", e.getMessage());
            }
        }
        if (locale == null) {
            logger.debug("No language configured, using system language.");
            this.translationProvider = new MieleTranslationProvider(i18nProvider, localeProvider);
        } else {
            this.translationProvider = new MieleTranslationProvider(i18nProvider, localeProvider, locale);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposes. Unregistering listener.");
        String applianceId = this.applianceId;
        if (applianceId != null) {
            MieleBridgeHandler bridgeHandler = getMieleBridgeHandler();
            if (bridgeHandler != null) {
                bridgeHandler.unregisterApplianceStatusListener(applianceId, this);
            }
            applianceId = null;
        }
        startTimeStabilizer.clear();
        finishTimeStabilizer.clear();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Here we could handle commands that are common to all Miele Appliances, but so far I don't know of any
        if (command instanceof RefreshType) {
            // Placeholder for future refinement
            return;
        }
    }

    @Override
    public void onApplianceStateChanged(DeviceClassObject dco) {
        JsonArray properties = dco.Properties;
        if (properties == null) {
            return;
        }

        for (JsonElement prop : properties.getAsJsonArray()) {
            try {
                DeviceProperty dp = gson.fromJson(prop, DeviceProperty.class);
                if (dp == null) {
                    continue;
                }
                if (!EXTENDED_DEVICE_STATE_PROPERTY_NAME.equals(dp.Name)) {
                    dp.Value = dp.Value.trim();
                    dp.Value = dp.Value.strip();
                }
                onAppliancePropertyChanged(dp);
            } catch (Exception p) {
                // Ignore - this is due to an unrecognized and not yet reverse-engineered array property
            }
        }
    }

    @Override
    public void onAppliancePropertyChanged(DeviceProperty dp) {
        try {
            DeviceMetaData dmd = null;
            if (dp.Metadata == null) {
                String metadata = metaDataCache.get(new StringBuilder().append(dp.Name).toString().trim());
                if (metadata != null) {
                    JsonObject jsonMetadata = (JsonObject) JsonParser.parseString(metadata);
                    dmd = gson.fromJson(jsonMetadata, DeviceMetaData.class);
                    // only keep the enum, if any - that's all we care for events we receive via multicast
                    // all other fields are nulled
                    if (dmd != null) {
                        dmd.LocalizedID = null;
                        dmd.LocalizedValue = null;
                        dmd.Filter = null;
                        dmd.description = null;
                    }
                }
            }
            JsonObject jsonMetadata = dp.Metadata;
            if (jsonMetadata != null) {
                String metadata = jsonMetadata.toString().replace("enum", "MieleEnum");
                JsonObject jsonMetaData = (JsonObject) JsonParser.parseString(metadata);
                dmd = gson.fromJson(jsonMetaData, DeviceMetaData.class);
                metaDataCache.put(new StringBuilder().append(dp.Name).toString().trim(), metadata);
            }

            ThingUID thingUid = getThing().getUID();
            if (EXTENDED_DEVICE_STATE_PROPERTY_NAME.equals(dp.Name)) {
                if (!dp.Value.isEmpty()) {
                    byte[] extendedStateBytes = DeviceUtil.stringToBytes(dp.Value);
                    logger.trace("Extended device state for {}: {}", getThing().getUID(),
                            DeviceUtil.bytesToHex(extendedStateBytes));
                    if (this instanceof ExtendedDeviceStateListener) {
                        ((ExtendedDeviceStateListener) this).onApplianceExtendedStateChanged(extendedStateBytes);
                    }
                }
                return;
            } else if (START_TIME_PROPERTY_NAME.equals(dp.Name)) {
                updateStateFromTime(new ChannelUID(thingUid, START_CHANNEL_ID), dp.Value, startTimeStabilizer);
                return;
            } else if (FINISH_TIME_PROPERTY_NAME.equals(dp.Name)) {
                updateDurationState(new ChannelUID(thingUid, FINISH_CHANNEL_ID), dp.Value);
                updateStateFromTime(new ChannelUID(thingUid, END_CHANNEL_ID), dp.Value, finishTimeStabilizer);
                return;
            }

            ApplianceChannelSelector selector = null;
            try {
                selector = getValueSelectorFromMieleID(dp.Name);
            } catch (Exception h) {
                logger.trace("{} is not a valid channel for a {}", dp.Name, modelID);
            }

            String dpValue = dp.Value.strip().trim();

            if (selector != null) {
                String channelId = selector.getChannelID();
                State state = selector.getState(dpValue, dmd, this.translationProvider);
                if (selector.isProperty()) {
                    String value = state.toString();
                    logger.trace("Updating the property '{}' of '{}' to '{}'", channelId, thingUid, value);
                    updateProperty(channelId, value);
                } else {
                    ChannelUID theChannelUID = new ChannelUID(thingUid, channelId);
                    logger.trace("Update state of {} with getState '{}'", theChannelUID, state);
                    updateState(theChannelUID, state);
                    updateRawChannel(dp.Name, dpValue);
                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn("An exception occurred while processing a changed device property: '{}'", e.getMessage());
        }
    }

    protected void updateExtendedState(String channelId, State state) {
        ChannelUID channelUid = new ChannelUID(getThing().getUID(), channelId);
        logger.trace("Update state of {} with extended state '{}'", channelUid, state);
        updateState(channelUid, state);
    }

    private void updateStateFromTime(ChannelUID channelUid, String value, TimeStabilizer stabilizer) {
        try {
            long minutesFromNow = Long.valueOf(value);
            if (minutesFromNow > 0) {
                Instant rawTime = Instant.now().truncatedTo(ChronoUnit.MINUTES).plusSeconds(minutesFromNow * 60);
                ZonedDateTime correctedTime = stabilizer.apply(rawTime).atZone(timeZoneProvider.getTimeZone());
                ZonedDateTime truncatedTime = correctedTime.truncatedTo(ChronoUnit.MINUTES);
                logger.trace("Update state of {} from {} -> '{}' -> '{}' to '{}'", channelUid, minutesFromNow, rawTime,
                        correctedTime, truncatedTime);
                updateState(channelUid, new DateTimeType(truncatedTime));
                return;
            }
        } catch (NumberFormatException e) {
            // Fall through.
        }
        updateState(channelUid, UnDefType.UNDEF);
        stabilizer.clear();
    }

    private void updateDurationState(ChannelUID channelUid, String value) {
        try {
            long minutesFromNow = Long.valueOf(value);
            if (minutesFromNow > 0) {
                updateState(channelUid, new QuantityType<>(minutesFromNow, Units.MINUTE));
                return;
            }
        } catch (NumberFormatException e) {
            // Fall through.
        }
        updateState(channelUid, UnDefType.UNDEF);
    }

    protected void updateSwitchOnOffFromState(DeviceProperty dp) {
        if (!STATE_PROPERTY_NAME.equals(dp.Name)) {
            return;
        }

        // Switch is trigger channel, but current state can be deduced from state.
        ChannelUID channelUid = new ChannelUID(getThing().getUID(), SWITCH_CHANNEL_ID);
        State state = OnOffType.from(!dp.Value.equals(String.valueOf(STATE_OFF)));
        logger.trace("Update state of {} to {} through '{}'", channelUid, state, dp.Name);
        updateState(channelUid, state);
    }

    protected void updateSwitchStartStopFromState(DeviceProperty dp) {
        if (!STATE_PROPERTY_NAME.equals(dp.Name)) {
            return;
        }

        // Switch is trigger channel, but current state can be deduced from state.
        ChannelUID channelUid = new ChannelUID(getThing().getUID(), SWITCH_CHANNEL_ID);
        State state = OnOffType.from(dp.Value.equals(String.valueOf(STATE_RUNNING)));
        logger.trace("Update state of {} to {} through '{}'", channelUid, state, dp.Name);
        updateState(channelUid, state);
    }

    /**
     * Update raw value channels for properties already mapped to text channels.
     * Currently ApplianceChannelSelector only supports 1:1 mapping from property
     * to channel.
     */
    private void updateRawChannel(String propertyName, String value) {
        String channelId;
        switch (propertyName) {
            case STATE_PROPERTY_NAME:
                channelId = STATE_CHANNEL_ID;
                break;
            case PROGRAM_ID_PROPERTY_NAME:
                channelId = PROGRAM_CHANNEL_ID;
                break;
            default:
                return;
        }
        ApplianceChannelSelector selector = null;
        try {
            selector = getValueSelectorFromChannelID(channelId);
        } catch (IllegalArgumentException e) {
            logger.trace("{} is not a valid channel for a {}", channelId, modelID);
            return;
        }
        ChannelUID channelUid = new ChannelUID(getThing().getUID(), channelId);
        State state = selector.getState(value);
        logger.trace("Update state of {} with getState '{}'", channelUid, state);
        updateState(channelUid, state);
    }

    @Override
    public void onApplianceRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.GONE);
    }

    @Override
    public void onApplianceAdded(HomeDevice appliance) {
        Map<String, String> properties = editProperties();
        String vendor = appliance.Vendor;
        if (vendor != null) {
            properties.put(Thing.PROPERTY_VENDOR, vendor);
        }
        properties.put(Thing.PROPERTY_MODEL_ID, appliance.getApplianceModel());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, appliance.getSerialNumber());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, appliance.getFirmwareVersion());
        String protocolAdapterName = appliance.ProtocolAdapterName;
        if (protocolAdapterName != null) {
            properties.put(PROPERTY_PROTOCOL_ADAPTER, protocolAdapterName);
        }
        String deviceClass = appliance.getDeviceClass();
        if (deviceClass != null) {
            properties.put(PROPERTY_DEVICE_CLASS, deviceClass);
        }
        String connectionType = appliance.getConnectionType();
        if (connectionType != null) {
            properties.put(PROPERTY_CONNECTION_TYPE, connectionType);
        }
        String connectionBaudRate = appliance.getConnectionBaudRate();
        if (connectionBaudRate != null) {
            properties.put(PROPERTY_CONNECTION_BAUD_RATE, connectionBaudRate);
        }
        updateProperties(properties);
        updateStatus(ThingStatus.ONLINE);
    }

    protected synchronized @Nullable MieleBridgeHandler getMieleBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof MieleBridgeHandler) {
                this.bridgeHandler = (MieleBridgeHandler) handler;
            }
        }
        return this.bridgeHandler;
    }

    protected boolean isResultProcessable(JsonElement result) {
        return !result.isJsonNull();
    }
}
