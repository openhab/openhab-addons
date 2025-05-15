/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.freeathome.internal.handler;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeathome.internal.configuration.FreeAtHomeDeviceHandlerConfiguration;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDatapoint;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDatapointGroup;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDeviceChannel;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDeviceDescription;
import org.openhab.binding.freeathome.internal.type.FreeAtHomeChannelTypeProvider;
import org.openhab.binding.freeathome.internal.util.FreeAtHomeGeneralException;
import org.openhab.binding.freeathome.internal.util.FreeAtHomeHttpCommunicationException;
import org.openhab.binding.freeathome.internal.util.UidUtils;
import org.openhab.binding.freeathome.internal.valuestateconverter.ValueStateConverter;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeDeviceHandler} is responsible for handling the generic free@home device main communication
 * and thing updates
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeDeviceHandler extends BaseThingHandler implements FreeAtHomeDeviceStateListener {

    private static final String CHANNEL_URI = "channel-type:freeathome:config";

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDeviceHandler.class);
    private FreeAtHomeDeviceDescription device = new FreeAtHomeDeviceDescription();
    private final FreeAtHomeChannelTypeProvider channelTypeProvider;
    private final TranslationProvider i18nProvider;
    private final Locale locale;
    private Bundle bundle;

    private final Map<ChannelUID, FreeAtHomeDatapointGroup> mapChannelUID = new HashMap<ChannelUID, FreeAtHomeDatapointGroup>();
    private final Map<String, ChannelUID> mapEventToChannelUID = new HashMap<String, ChannelUID>();

    public FreeAtHomeDeviceHandler(Thing thing, FreeAtHomeChannelTypeProvider channelTypeProvider,
            TranslationProvider i18nProvider, LocaleProvider localeProvider) {
        super(thing);

        this.channelTypeProvider = channelTypeProvider;
        this.i18nProvider = i18nProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
        this.locale = localeProvider.getLocale();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            FreeAtHomeDeviceHandlerConfiguration config = getConfigAs(FreeAtHomeDeviceHandlerConfiguration.class);

            Bridge bridge = this.getBridge();
            String locDeviceId = config.deviceId;

            if (bridge != null) {
                ThingHandler handler = bridge.getHandler();

                if (handler instanceof FreeAtHomeBridgeHandler bridgeHandler) {
                    if (!locDeviceId.isBlank()) {
                        try {
                            device = bridgeHandler.getFreeatHomeDeviceDescription(locDeviceId);

                            updateChannels();
                        } catch (FreeAtHomeHttpCommunicationException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "@text/comm-error.error-in-sysap-com");
                        } catch (FreeAtHomeGeneralException e) {
                            logger.debug("General error in the binding - during initialization {}",
                                    device.getDeviceId());

                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "@text/conf-error.general-binding-error");
                        }

                        // register device for status updates
                        bridgeHandler.registerDeviceStateListener(device.getDeviceId(), this);

                        updateStatus(ThingStatus.ONLINE);

                        logger.debug("Device created - device id: {}", device.getDeviceId());
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "@text/conf-error.invalid-deviceconfig");

                        logger.debug("Device cannot be created: device ID is null!");
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "@text/conf-error.bridge-not-configured");

                logger.debug("Device cannot be created: no bridge is configured!");
                return;
            }
        });
    }

    @Override
    public void dispose() {
        Bridge bridge = this.getBridge();

        // Unregister device and specific channel for event based state updated
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler bridgeHandler) {
                bridgeHandler.unregisterDeviceStateListener(device.getDeviceId());
            }
        }

        // Remove mapping tables
        mapChannelUID.clear();

        mapEventToChannelUID.clear();

        logger.debug("Device removed - device id: {}", device.getDeviceId());
    }

    private void handleRefreshCommand(FreeAtHomeBridgeHandler freeAtHomeBridge, FreeAtHomeDatapointGroup dpg,
            ChannelUID channelUID) {
        String valueStr = "0";
        String channelID = "ch000";
        String datapointID = "0";

        // Check whether it is a INPUT only datapoint group

        if (dpg.getDirection() == FreeAtHomeDatapointGroup.DatapointGroupDirection.INPUT) {
            FreeAtHomeDatapoint datapoint = dpg.getInputDatapoint();

            if (datapoint != null) {
                channelID = datapoint.channelId;
                datapointID = datapoint.getDatapointId();
            }
        } else {
            FreeAtHomeDatapoint datapoint = dpg.getOutputDatapoint();

            if (datapoint != null) {
                channelID = datapoint.channelId;
                datapointID = datapoint.getDatapointId();
            }
        }

        try {
            valueStr = freeAtHomeBridge.getDatapoint(device.getDeviceId(), channelID, datapointID);

            ValueStateConverter vsc = dpg.getValueStateConverter();

            updateState(channelUID, vsc.convertToState(valueStr));
        } catch (FreeAtHomeHttpCommunicationException e) {
            logger.debug("Communication error during refresh command {} - at channel {} - Error string {}",
                    device.getDeviceId(), channelUID.getAsString(), e.getMessage());

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.error-in-sysap-com");
        } catch (FreeAtHomeGeneralException e) {
            logger.debug("General error in the binding - during REFRESH command {} - at channel {} - Error string {}",
                    device.getDeviceId(), channelUID.getAsString(), e.getMessage());

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.general-binding-error");
        }
    }

    private void handleSetCommand(FreeAtHomeBridgeHandler freeAtHomeBridge, FreeAtHomeDatapointGroup dpg,
            ChannelUID channelUID, Command command) {
        State state = null;
        String valueString = "0";

        // initial error handling. look for the data point group validity
        FreeAtHomeDatapoint datapoint = dpg.getInputDatapoint();

        if (datapoint == null) {
            logger.debug("Invalid parameter in handleSetCommand - DeviceId - {} - at channel {}", device.getDeviceId(),
                    channelUID.getAsString());

            return;
        }

        try {
            ValueStateConverter vsc = dpg.getValueStateConverter();

            if (command instanceof StopMoveType) {
                valueString = "0";
            } else {
                state = ((State) command);
                valueString = vsc.convertToValueString(state);
            }

            freeAtHomeBridge.setDatapoint(device.getDeviceId(), datapoint.channelId, datapoint.getDatapointId(),
                    valueString);

            if (!device.isScene()) {
                if (state != null) {
                    updateState(channelUID, state);
                } else {
                    updateState(channelUID, new StringType("STOP"));
                }
            }
        } catch (FreeAtHomeHttpCommunicationException e) {
            logger.debug(
                    "Communication error during set command {} - at channel {} - full command {} - Error string {}",
                    device.getDeviceId(), channelUID.getAsString(), command.toFullString(), e.getMessage());

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.error-in-sysap-com");
        } catch (FreeAtHomeGeneralException e) {
            logger.debug("General error in the binding - during SET command {} - at channel {} - Error string {}",
                    device.getDeviceId(), channelUID.getAsString(), e.getMessage());

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.general-binding-error");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        FreeAtHomeBridgeHandler freeAtHomeBridge = null;

        Bridge bridge = this.getBridge();

        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler bridgeHandler) {
                freeAtHomeBridge = bridgeHandler;
            }
        }

        if (freeAtHomeBridge != null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/conf-error.invalid-bridge");
            return;
        }

        FreeAtHomeDatapointGroup dpg = mapChannelUID.get(channelUID);

        // is the datapointgroup invalid
        if (dpg == null) {
            logger.debug("Handle command for device (but invalid datapointgroup) {} - at channel {} - full command {}",
                    device.getDeviceId(), channelUID.getAsString(), command.toFullString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.invalid-deviceconfig");
        } else {
            if (command instanceof RefreshType) {
                handleRefreshCommand(freeAtHomeBridge, dpg, channelUID);
            } else {
                handleSetCommand(freeAtHomeBridge, dpg, channelUID, command);
            }

            logger.debug("Handle command for device {} - at channel {} - full command {}", device.getDeviceId(),
                    channelUID.getAsString(), command.toFullString());
        }
    }

    @Override
    public void onDeviceRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.GONE);
    }

    @Override
    public void onDeviceStateChanged(String event, String valueString) {
        // Get the channle UID belonging to this event
        ChannelUID channelUID = mapEventToChannelUID.get(event);

        try {
            if (channelUID != null) {
                // get the value State Converter for the channel
                FreeAtHomeDatapointGroup dpg = mapChannelUID.get(channelUID);

                if (dpg != null) {
                    State state;
                    state = dpg.getValueStateConverter().convertToState(valueString);

                    // Handle state change
                    handleEventBasedUpdate(channelUID, state);

                    // if it is virtual device, give a feedback to free@home also
                    if (isThingHandlesVirtualDevice()) {
                        feedbackForVirtualDevice(channelUID, valueString);
                    }
                }
            }
        } catch (FreeAtHomeGeneralException e) {
            logger.debug("General error in the binding during onDeviceStateChange");

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.general-binding-error");
        }
    }

    private void handleEventBasedUpdate(ChannelUID channelUID, State state) {
        this.updateState(channelUID, state);
    }

    private void feedbackForVirtualDevice(ChannelUID channelUID, String valueString) {
        FreeAtHomeBridgeHandler freeAtHomeBridge = null;

        FreeAtHomeDatapointGroup dpg = mapChannelUID.get(channelUID);

        Bridge bridge = this.getBridge();

        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler bridgeHandler) {
                freeAtHomeBridge = bridgeHandler;
            }
        }

        if (freeAtHomeBridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/gen-error.no-bridge-avail");
            return;
        }

        if (dpg == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.datapointgroup-invalid");
            return;
        }

        FreeAtHomeDatapoint inputDatapoint = dpg.getInputDatapoint();

        if (inputDatapoint == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.inputdatapoint-invalid");
            return;
        }

        if ((dpg.getDirection() != FreeAtHomeDatapointGroup.DatapointGroupDirection.INPUT)
                || (dpg.getDirection() != FreeAtHomeDatapointGroup.DatapointGroupDirection.INPUTOUTPUT)) {
            logger.debug("Handle feedback for virtual device {} - at channel {} - but wrong config",
                    device.getDeviceId(), channelUID.getAsString());
        }

        try {
            freeAtHomeBridge.setDatapoint(device.getDeviceId(), inputDatapoint.channelId,
                    inputDatapoint.getDatapointId(), valueString);

            updateStatus(ThingStatus.ONLINE);

            logger.debug("Handle feedback for virtual device {} - at channel {} - value {}", device.getDeviceId(),
                    channelUID.getAsString(), valueString);
        } catch (FreeAtHomeHttpCommunicationException e) {
            logger.debug("Communication error during set command {} - at channel {} - value {} - Error string {}",
                    device.getDeviceId(), channelUID.getAsString(), valueString, e.getMessage());

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.not-able-open-httpconnection");
        }
    }

    public ChannelTypeUID createChannelTypeForDatapointgroup(FreeAtHomeDatapointGroup dpg,
            ChannelTypeUID channelTypeUID) throws FreeAtHomeGeneralException {
        StateDescriptionFragmentBuilder stateFragment = StateDescriptionFragmentBuilder.create();

        stateFragment.withReadOnly(dpg.isReadOnly());
        stateFragment.withPattern(dpg.getTypePattern());

        if (dpg.isDecimal() || dpg.isInteger()) {
            BigDecimal min = new BigDecimal(dpg.getMin());
            BigDecimal max = new BigDecimal(dpg.getMax());
            stateFragment.withMinimum(min).withMaximum(max);
        }

        try {
            URI configDescriptionUriChannel = new URI(CHANNEL_URI);

            ChannelTypeBuilder<?> channelTypeBuilder = ChannelTypeBuilder
                    .state(channelTypeUID,
                            String.format("%s-%s-%s-%s", dpg.getLabel(), dpg.getOpenHabItemType(),
                                    dpg.getOpenHabCategory(), "type"),
                            dpg.getOpenHabItemType())
                    .withCategory(dpg.getOpenHabCategory()).withStateDescriptionFragment(stateFragment.build());

            ChannelType channelType = channelTypeBuilder.isAdvanced(false)
                    .withConfigDescriptionURI(configDescriptionUriChannel)
                    .withDescription(String.format("Type for channel - %s ", dpg.getLabel())).build();

            channelTypeProvider.addChannelType(channelType);

            logger.debug("Channel type created {} - label: {} - category: {}", channelTypeUID.getAsString(),
                    dpg.getLabel(), dpg.getOpenHabCategory());
        } catch (URISyntaxException e) {
            logger.debug("Channel config URI cannot created for datapoint - datapoint group: {}", dpg.getLabel());
        }

        return channelTypeUID;
    }

    public void updateChannels() throws FreeAtHomeGeneralException {
        // define update policy
        AutoUpdatePolicy policy = AutoUpdatePolicy.DEFAULT;

        if (device.isScene()) {
            policy = AutoUpdatePolicy.VETO;
        }

        // Initialize channels
        List<Channel> thingChannels = new ArrayList<>(this.getThing().getChannels());

        if (thingChannels.isEmpty()) {
            ThingBuilder thingBuilder = editThing();

            ThingUID thingUID = thing.getUID();

            for (int i = 0; i < device.getNumberOfChannels(); i++) {
                FreeAtHomeDeviceChannel channel = device.getChannel(i);

                for (int j = 0; j < channel.getNumberOfDatapointGroup(); j++) {
                    FreeAtHomeDatapointGroup dpg = channel.getDatapointGroup(j);
                    Map<String, String> channelProps = new HashMap<>();

                    FreeAtHomeDatapoint inputDatapoint = dpg.getInputDatapoint();
                    FreeAtHomeDatapoint outputDatapoint = dpg.getOutputDatapoint();

                    if (inputDatapoint != null) {
                        channelProps.put("input", inputDatapoint.getDatapointId());
                    }

                    if (outputDatapoint != null) {
                        channelProps.put("output", outputDatapoint.getDatapointId());
                    }

                    ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(dpg.getValueType(),
                            dpg.isReadOnly());

                    if (channelTypeProvider.getChannelType(channelTypeUID, null) == null) {
                        channelTypeUID = createChannelTypeForDatapointgroup(dpg, channelTypeUID);
                    }

                    ChannelUID channelUID = createChannelUID(thingUID, channel.getChannelId(), dpg.getLabel());

                    String channelLabel = String.format("%s",
                            i18nProvider.getText(bundle, dpg.getLabel(), "-", locale));

                    String channelDescription = String.format("(%s) %s", channel.getChannelLabel(),
                            i18nProvider.getText(bundle, dpg.getDescription(), "-", locale));

                    Channel thingChannel = ChannelBuilder.create(channelUID)
                            .withAcceptedItemType(dpg.getOpenHabItemType()).withKind(ChannelKind.STATE)
                            .withProperties(channelProps).withLabel(capitalizeWordsInLabel(channelLabel))
                            .withDescription(channelDescription).withType(channelTypeUID).withAutoUpdatePolicy(policy)
                            .build();
                    thingChannels.add(thingChannel);

                    logger.debug("Thing channel created - device: {} - channelUID: {} - channel label: {}",
                            device.getDeviceId() + device.getDeviceLabel(), channelUID.getAsString(), channelLabel);

                    // in case of output channel, register it for updates
                    if (outputDatapoint != null) {
                        String eventDatapointID = device.getDeviceId() + "/" + channel.getChannelId() + "/"
                                + outputDatapoint.getDatapointId();

                        mapEventToChannelUID.put(eventDatapointID, channelUID);
                    }

                    // add the datapoint group to the mapping channel
                    mapChannelUID.put(channelUID, dpg);

                    if (dpg.getInputDatapoint() == null) {
                        logger.debug(
                                "Thing channel registered - device:  {} - channelUID: {} - channel label: {} - category: {}",
                                device.getDeviceId() + device.getDeviceLabel(), channelUID.getAsString(),
                                dpg.getLabel(), dpg.getOpenHabCategory());
                    } else {
                        logger.debug(
                                "Thing channel registered - device: {} - channelUID: {} - channel label: {} - category: {}",
                                device.getDeviceId() + device.getDeviceLabel(), channelUID.getAsString(),
                                dpg.getLabel(), dpg.getOpenHabCategory());
                    }
                }

                thingBuilder.withChannels(thingChannels);

                updateThing(thingBuilder.build());
            }
        } else {
            reloadChannelTypes();
        }

        thingChannels.forEach(channel -> {
            if (isLinked(channel.getUID())) {
                channelLinked(channel.getUID());
            }
        });
    }

    private void reloadChannelTypes() throws FreeAtHomeGeneralException {
        Bridge bridge = this.getBridge();

        ThingUID thingUID = thing.getUID();

        try {
            if (bridge != null) {
                ThingHandler handler = bridge.getHandler();

                if (handler instanceof FreeAtHomeBridgeHandler bridgeHandler) {
                    device = bridgeHandler.getFreeatHomeDeviceDescription(device.getDeviceId());
                }
            }
        } catch (FreeAtHomeHttpCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.error-in-sysap-com");
        }

        for (int i = 0; i < device.getNumberOfChannels(); i++) {
            FreeAtHomeDeviceChannel channel = device.getChannel(i);

            for (int j = 0; j < channel.getNumberOfDatapointGroup(); j++) {
                FreeAtHomeDatapointGroup dpg = channel.getDatapointGroup(j);

                ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(dpg.getValueType(), dpg.isReadOnly());

                if (channelTypeProvider.getChannelType(channelTypeUID, null) == null) {
                    channelTypeUID = createChannelTypeForDatapointgroup(dpg, channelTypeUID);
                }

                ChannelUID channelUID = createChannelUID(thingUID, channel.getChannelId(), dpg.getLabel());

                FreeAtHomeDatapoint outputDatapoint = dpg.getOutputDatapoint();

                // in case of output channel, register it for updates
                if (outputDatapoint != null) {
                    String eventDatapointID = device.getDeviceId() + "/" + channel.getChannelId() + "/"
                            + outputDatapoint.getDatapointId();

                    mapEventToChannelUID.put(eventDatapointID, channelUID);
                }

                // add the datapoint group to the mapping channel
                mapChannelUID.put(channelUID, dpg);

                logger.debug("Thing channelType reloaded - Device: {} - channelTypeUID: {}",
                        device.getDeviceId() + device.getDeviceLabel(), channelTypeUID.getAsString());
            }
        }
    }

    // Create a channel UID. Makes sure that the channel UID is unique and generated the same way every time
    private ChannelUID createChannelUID(ThingUID thingUID, String channelID, String dpgLabel) {
        return new ChannelUID(thingUID, channelID, dpgLabel.substring(4));
    }

    public void removeChannels() {
        Bridge bridge = this.getBridge();

        try {
            if (bridge != null) {
                ThingHandler handler = bridge.getHandler();

                if (handler instanceof FreeAtHomeBridgeHandler bridgeHandler) {
                    device = bridgeHandler.getFreeatHomeDeviceDescription(device.getDeviceId());
                }
            }
        } catch (FreeAtHomeHttpCommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.error-in-sysap-com");
        }

        mapChannelUID.clear();

        mapEventToChannelUID.clear();
    }

    private String capitalizeWordsInLabel(String label) {
        // splliting up words using split function
        String[] words = label.split(" ");

        for (int i = 0; i < words.length; i++) {

            // taking letter individually from sentences
            String firstLetter = words[i].substring(0, 1);
            String restOfWord = words[i].substring(1);

            // making first letter uppercase using toUpperCase function
            firstLetter = firstLetter.toUpperCase();
            words[i] = firstLetter + restOfWord;
        }

        // joining the words together to make a sentence
        return String.join(" ", words);
    }

    private boolean isThingHandlesVirtualDevice() {
        return device.isVirtual();
    }
}
