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
package org.openhab.binding.avmfritz.internal.handler;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;
import static org.openhab.binding.avmfritz.internal.dto.DeviceModel.ETSUnitInfoModel.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants;
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicCommandDescriptionProvider;
import org.openhab.binding.avmfritz.internal.config.AVMFritzBoxConfiguration;
import org.openhab.binding.avmfritz.internal.discovery.AVMFritzDiscoveryService;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.DeviceModel;
import org.openhab.binding.avmfritz.internal.dto.GroupModel;
import org.openhab.binding.avmfritz.internal.dto.templates.TemplateModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaStatusListener;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaApplyTemplateCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaUpdateCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaUpdateTemplatesCallback;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract handler for a FRITZ! bridge. Handles polling of values from AHA devices.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet DECT
 * @author Christoph Weitkamp - Added support for groups
 * @author Ulrich Mertin - Added support for HAN-FUN blinds
 */
@NonNullByDefault
public abstract class AVMFritzBaseBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzBaseBridgeHandler.class);

    /**
     * Initial delay in s for polling job.
     */
    private static final int INITIAL_DELAY = 1;

    /**
     * Refresh interval which is used to poll values from the FRITZ!Box web interface (optional, defaults to 15 s)
     */
    private long pollingInterval = 15;

    /**
     * Interface object for querying the FRITZ!Box web interface
     */
    protected @Nullable FritzAhaWebInterface connection;

    /**
     * Schedule for polling
     */
    private @Nullable ScheduledFuture<?> pollingJob;

    /**
     * Shared instance of HTTP client for asynchronous calls
     */
    protected final HttpClient httpClient;

    private final AVMFritzDynamicCommandDescriptionProvider commandDescriptionProvider;

    protected final List<FritzAhaStatusListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * keeps track of the {@link ChannelUID} for the 'apply_template' {@link Channel}
     */
    private final ChannelUID applyTemplateChannelUID;

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public AVMFritzBaseBridgeHandler(Bridge bridge, HttpClient httpClient,
            AVMFritzDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.commandDescriptionProvider = commandDescriptionProvider;

        applyTemplateChannelUID = new ChannelUID(bridge.getUID(), CHANNEL_APPLY_TEMPLATE);
    }

    @Override
    public void initialize() {
        boolean configValid = true;

        AVMFritzBoxConfiguration config = getConfigAs(AVMFritzBoxConfiguration.class);

        String localIpAddress = config.ipAddress;
        if (localIpAddress == null || localIpAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'ipAddress' parameter must be configured.");
            configValid = false;
        }
        pollingInterval = config.pollingInterval;
        if (pollingInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'pollingInterval' parameter must be greater than or equals to 1 second.");
            configValid = false;
        }

        if (configValid) {
            updateStatus(ThingStatus.UNKNOWN);
            manageConnections();
        }
    }

    protected synchronized void manageConnections() {
        AVMFritzBoxConfiguration config = getConfigAs(AVMFritzBoxConfiguration.class);
        if (this.connection == null) {
            this.connection = new FritzAhaWebInterface(config, this, httpClient);
            stopPolling();
            startPolling();
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        manageConnections();
        super.channelLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        manageConnections();
        super.channelUnlinked(channelUID);
    }

    @Override
    public void dispose() {
        stopPolling();
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof FritzAhaStatusListener) {
            registerStatusListener((FritzAhaStatusListener) childHandler);
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof FritzAhaStatusListener) {
            unregisterStatusListener((FritzAhaStatusListener) childHandler);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(AVMFritzDiscoveryService.class);
    }

    public boolean registerStatusListener(FritzAhaStatusListener listener) {
        return listeners.add(listener);
    }

    public boolean unregisterStatusListener(FritzAhaStatusListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Start the polling.
     */
    protected void startPolling() {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob == null || localPollingJob.isCancelled()) {
            logger.debug("Start polling job at interval {}s", pollingInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, INITIAL_DELAY, pollingInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * Stops the polling.
     */
    protected void stopPolling() {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob != null && !localPollingJob.isCancelled()) {
            logger.debug("Stop polling job");
            localPollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Polls the bridge.
     */
    private void poll() {
        FritzAhaWebInterface webInterface = getWebInterface();
        if (webInterface != null) {
            logger.debug("Poll FRITZ!Box for updates {}", thing.getUID());
            FritzAhaUpdateCallback updateCallback = new FritzAhaUpdateCallback(webInterface, this);
            webInterface.asyncGet(updateCallback);
            if (isLinked(applyTemplateChannelUID)) {
                logger.debug("Poll FRITZ!Box for templates {}", thing.getUID());
                FritzAhaUpdateTemplatesCallback templateCallback = new FritzAhaUpdateTemplatesCallback(webInterface,
                        this);
                webInterface.asyncGet(templateCallback);
            }
        }
    }

    /**
     * Called from {@link FritzAhaWebInterface#authenticate()} to update the bridge status because updateStatus is
     * protected.
     *
     * @param status Bridge status
     * @param statusDetail Bridge status detail
     * @param description Bridge status description
     */
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        updateStatus(status, statusDetail, description);
    }

    /**
     * Called from {@link FritzAhaApplyTemplateCallback} to provide new templates for things.
     *
     * @param templateList list of template models
     */
    public void addTemplateList(List<TemplateModel> templateList) {
        commandDescriptionProvider.setCommandOptions(applyTemplateChannelUID,
                templateList.stream().map(TemplateModel::toCommandOption).collect(Collectors.toList()));
    }

    /**
     * Called from {@link FritzAhaUpdateCallback} to provide new devices.
     *
     * @param deviceList list of devices
     */
    public void onDeviceListAdded(List<AVMFritzBaseModel> deviceList) {
        final Map<String, AVMFritzBaseModel> deviceIdentifierMap = deviceList.stream()
                .collect(Collectors.toMap(it -> it.getIdentifier(), Function.identity()));
        getThing().getThings().forEach(childThing -> {
            final AVMFritzBaseThingHandler childHandler = (AVMFritzBaseThingHandler) childThing.getHandler();
            if (childHandler != null) {
                final AVMFritzBaseModel device = deviceIdentifierMap.get(childHandler.getIdentifier());
                if (device != null) {
                    deviceList.remove(device);
                    listeners.forEach(listener -> listener.onDeviceUpdated(childThing.getUID(), device));
                } else {
                    listeners.forEach(listener -> listener.onDeviceGone(childThing.getUID()));
                }
            } else {
                logger.debug("Handler missing for thing '{}'", childThing.getUID());
            }
        });
        deviceList.forEach(device -> {
            listeners.forEach(listener -> listener.onDeviceAdded(device));
        });
    }

    /**
     * Builds a {@link ThingUID} from a device model. The UID is build from the
     * {@link AVMFritzBindingConstants#BINDING_ID} and
     * value of {@link AVMFritzBaseModel#getProductName()} in which all characters NOT matching the RegEx [^a-zA-Z0-9_]
     * are replaced by "_".
     *
     * @param device Discovered device model
     * @return ThingUID without illegal characters.
     */
    public @Nullable ThingUID getThingUID(AVMFritzBaseModel device) {
        String id = getThingTypeId(device);
        ThingTypeUID thingTypeUID = id.isEmpty() ? null : new ThingTypeUID(BINDING_ID, id);
        ThingUID bridgeUID = thing.getUID();
        String thingName = getThingName(device);

        if (thingTypeUID != null && (SUPPORTED_BUTTON_THING_TYPES_UIDS.contains(thingTypeUID)
                || SUPPORTED_HEATING_THING_TYPES.contains(thingTypeUID)
                || SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID))) {
            return new ThingUID(thingTypeUID, bridgeUID, thingName);
        } else if (device.isHeatingThermostat()) {
            return new ThingUID(GROUP_HEATING_THING_TYPE, bridgeUID, thingName);
        } else if (device.isSwitchableOutlet()) {
            return new ThingUID(GROUP_SWITCH_THING_TYPE, bridgeUID, thingName);
        } else {
            return null;
        }
    }

    /**
     *
     * @param device Discovered device model
     * @return ThingTypeId without illegal characters.
     */
    public String getThingTypeId(AVMFritzBaseModel device) {
        if (device instanceof GroupModel) {
            if (device.isHeatingThermostat()) {
                return GROUP_HEATING;
            } else if (device.isSwitchableOutlet()) {
                return GROUP_SWITCH;
            }
        } else if (device instanceof DeviceModel && device.isHANFUNUnit()) {
            if (device.isHANFUNBlinds()) {
                return DEVICE_HAN_FUN_BLINDS;
            } else if (device.isColorLight()) {
                return DEVICE_HAN_FUN_COLOR_BULB;
            } else if (device.isDimmableLight()) {
                return DEVICE_HAN_FUN_DIMMABLE_BULB;
            }
            List<String> interfaces = Arrays
                    .asList(((DeviceModel) device).getEtsiunitinfo().getInterfaces().split(","));
            if (interfaces.contains(HAN_FUN_INTERFACE_ALERT)) {
                return DEVICE_HAN_FUN_CONTACT;
            } else if (interfaces.contains(HAN_FUN_INTERFACE_SIMPLE_BUTTON)) {
                return DEVICE_HAN_FUN_SWITCH;
            } else if (interfaces.contains(HAN_FUN_INTERFACE_ON_OFF)) {
                return DEVICE_HAN_FUN_ON_OFF;
            }
        }
        return device.getProductName().replaceAll(INVALID_PATTERN, "_");
    }

    /**
     *
     * @param device Discovered device model
     * @return Thing name without illegal characters.
     */
    public String getThingName(AVMFritzBaseModel device) {
        return device.getIdentifier().replaceAll(INVALID_PATTERN, "_");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        if (command == RefreshType.REFRESH) {
            handleRefreshCommand();
            return;
        }
        FritzAhaWebInterface fritzBox = getWebInterface();
        if (fritzBox == null) {
            logger.debug("Cannot handle command '{}' because connection is missing", command);
            return;
        }
        if (CHANNEL_APPLY_TEMPLATE.equals(channelId)) {
            if (command instanceof StringType) {
                fritzBox.applyTemplate(command.toString());
            }
        } else {
            logger.debug("Received unknown channel {}", channelId);
        }
    }

    /**
     * Provides the web interface object.
     *
     * @return The web interface object
     */
    public @Nullable FritzAhaWebInterface getWebInterface() {
        return connection;
    }

    /**
     * Handles a refresh command.
     */
    public void handleRefreshCommand() {
        scheduler.submit(this::poll);
    }
}
