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
package org.openhab.binding.freeathome.internal.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeathome.internal.configuration.FreeAtHomeDeviceHandlerConfiguration;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDatapoint;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDatapointGroup;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDeviceChannel;
import org.openhab.binding.freeathome.internal.datamodel.FreeAtHomeDeviceDescription;
import org.openhab.binding.freeathome.internal.type.FreeAtHomeChannelTypeFactory;
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
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.util.StringUtils;
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

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDeviceHandler.class);
    private volatile FreeAtHomeDeviceDescription device = new FreeAtHomeDeviceDescription();
    private final FreeAtHomeChannelTypeProvider channelTypeProvider;
    private final TranslationProvider i18nProvider;
    private final Locale locale;
    private Bundle bundle;

    private final Map<ChannelUID, FreeAtHomeDatapointGroup> mapChannelUID = new ConcurrentHashMap<>();
    private final Map<String, ChannelUID> mapEventToChannelUID = new ConcurrentHashMap<>();

    private volatile boolean disposed = false;
    private long initializationGeneration = 0;
    private @Nullable Future<?> initializeJob;
    private final Object initializeLock = new Object();

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
        synchronized (initializeLock) {
            disposed = false;
            long generation = ++initializationGeneration;
            updateStatus(ThingStatus.UNKNOWN);

            Future<?> previousJob = initializeJob;
            if (previousJob != null) {
                previousJob.cancel(true);
            }
            initializeJob = scheduler.submit(() -> initializeDevice(generation));
        }
    }

    long currentInitializationGeneration() {
        synchronized (initializeLock) {
            return initializationGeneration;
        }
    }

    private boolean isCurrentRun(long generation) {
        return !disposed && generation == initializationGeneration;
    }

    private void publishStatus(long generation, ThingStatus status, ThingStatusDetail detail,
            @Nullable String description) {
        synchronized (initializeLock) {
            if (isCurrentRun(generation)) {
                updateStatus(status, detail, description);
            }
        }
    }

    void initializeDevice(long generation) {
        FreeAtHomeDeviceHandlerConfiguration config = getConfigAs(FreeAtHomeDeviceHandlerConfiguration.class);

        Bridge bridge = this.getBridge();
        String locDeviceId = config.deviceId;

        if (bridge == null) {
            publishStatus(generation, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.bridge-not-configured");

            logger.debug("Device cannot be created: no bridge is configured!");
            return;
        }
        if (!(bridge.getHandler() instanceof FreeAtHomeBridgeHandler bridgeHandler)) {
            publishStatus(generation, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.bridge-not-configured");
            return;
        }
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            publishStatus(generation, ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null);
            return;
        }
        if (locDeviceId.isBlank()) {
            publishStatus(generation, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.invalid-deviceconfig");

            logger.debug("Device cannot be created: device ID is null!");
            return;
        }

        try {
            FreeAtHomeDeviceDescription description = bridgeHandler.getFreeatHomeDeviceDescription(locDeviceId);

            synchronized (initializeLock) {
                if (!isCurrentRun(generation)) {
                    return;
                }
                device = description;
                updateChannels();
                bridgeHandler.registerDeviceStateListener(description.getDeviceId(), this);
                updateStatus(ThingStatus.ONLINE);
            }
            refreshLinkedChannels(generation);

            logger.debug("Device created - device id: {}", description.getDeviceId());
        } catch (FreeAtHomeHttpCommunicationException e) {
            publishStatus(generation, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error.error-in-sysap-com");
        } catch (FreeAtHomeGeneralException e) {
            logger.debug("General error in the binding - during initialization {}", locDeviceId);

            publishStatus(generation, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error.general-binding-error");
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        synchronized (initializeLock) {
            if (disposed) {
                return;
            }
            if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    initialize();
                }
            } else {
                initializationGeneration++;
                Future<?> pendingJob = initializeJob;
                if (pendingJob != null) {
                    pendingJob.cancel(true);
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void dispose() {
        String registeredDeviceId;
        synchronized (initializeLock) {
            disposed = true;

            Future<?> pendingJob = initializeJob;
            if (pendingJob != null) {
                pendingJob.cancel(true);
                initializeJob = null;
            }
            registeredDeviceId = device.getDeviceId();
        }

        Bridge bridge = this.getBridge();

        // Unregister device and specific channel for event based state updated
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof FreeAtHomeBridgeHandler bridgeHandler) {
                bridgeHandler.unregisterDeviceStateListener(registeredDeviceId, this);
            }
        }

        // Remove mapping tables
        mapChannelUID.clear();

        mapEventToChannelUID.clear();

        logger.debug("Device removed - device id: {}", registeredDeviceId);
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
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
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
        channelTypeProvider.addChannelType(FreeAtHomeChannelTypeFactory.createChannelType(dpg, channelTypeUID));

        logger.debug("Channel type created {} - label: {} - category: {}", channelTypeUID.getAsString(), dpg.getLabel(),
                dpg.getOpenHabCategory());

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
                            .withProperties(channelProps)
                            .withLabel(Objects.requireNonNull(StringUtils.capitalizeByWhitespace(channelLabel)))
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
    }

    private void refreshLinkedChannels(long generation) {
        for (Channel channel : getThing().getChannels()) {
            synchronized (initializeLock) {
                if (!isCurrentRun(generation)) {
                    return;
                }
            }
            if (isLinked(channel.getUID())) {
                channelLinked(channel.getUID());
            }
        }
    }

    private void reloadChannelTypes() throws FreeAtHomeGeneralException {
        ThingUID thingUID = thing.getUID();

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

    private boolean isThingHandlesVirtualDevice() {
        return device.isVirtual();
    }
}
