/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.avmfritz.internal.BindingConstants.*;
import static org.openhab.binding.avmfritz.internal.dto.DeviceModel.ETSUnitInfoModel.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicStateDescriptionProvider;
import org.openhab.binding.avmfritz.internal.BindingConstants;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract handler for a FRITZ! bridge. Handles polling of values from AHA devices.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet DECT
 * @author Christoph Weitkamp - Added support for groups
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
    private long refreshInterval = 15;
    /**
     * Interface object for querying the FRITZ!Box web interface
     */
    private @Nullable FritzAhaWebInterface connection;
    /**
     * Schedule for polling
     */
    private @Nullable ScheduledFuture<?> pollingJob;
    /**
     * shared instance of HTTP client for asynchronous calls
     */
    private final HttpClient httpClient;

    private final AVMFritzDynamicStateDescriptionProvider stateDescriptionProvider;

    protected final List<FritzAhaStatusListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * keeps track of the {@link ChannelUID} for the 'apply_tamplate' {@link Channel}
     */
    private final ChannelUID applyTemplateChannelUID;

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public AVMFritzBaseBridgeHandler(Bridge bridge, HttpClient httpClient,
            AVMFritzDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.stateDescriptionProvider = stateDescriptionProvider;

        applyTemplateChannelUID = new ChannelUID(bridge.getUID(), CHANNEL_APPLY_TEMPLATE);
    }

    @Override
    public void initialize() {
        boolean configValid = true;

        AVMFritzBoxConfiguration config = getConfigAs(AVMFritzBoxConfiguration.class);

        String localIpAddress = config.ipAddress;
        if (localIpAddress == null || localIpAddress.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'ipAddress' parameter must be configured.");
            configValid = false;
        }
        String localPassword = config.password;
        if (localPassword == null || localPassword.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No password set.");
            configValid = false;
        }
        refreshInterval = config.pollingInterval;
        if (refreshInterval < 5) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'pollingInterval' parameter must be greater then at least 5 seconds.");
            configValid = false;
        }

        if (configValid) {
            this.connection = new FritzAhaWebInterface(config, this, httpClient);

            stopPolling();
            startPolling();
        }
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
    private void startPolling() {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob == null || localPollingJob.isCancelled()) {
            logger.debug("Start polling job at interval {}s", refreshInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, INITIAL_DELAY, refreshInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * Stops the polling.
     */
    private void stopPolling() {
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
        List<StateOption> options = new ArrayList<>();
        for (TemplateModel template : templateList) {
            logger.debug("Process template model: {}", template);
            options.add(new StateOption(template.getIdentifier(), template.getName()));
        }
        stateDescriptionProvider.setStateOptions(applyTemplateChannelUID, options);
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
                final Optional<AVMFritzBaseModel> optionalDevice = Optional
                        .ofNullable(deviceIdentifierMap.get(childHandler.getIdentifier()));
                if (optionalDevice.isPresent()) {
                    final AVMFritzBaseModel device = optionalDevice.get();
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
     * Builds a {@link ThingUID} from a device model. The UID is build from the {@link BindingConstants#BINDING_ID} and
     * value of {@link AVMFritzBaseModel#getProductName()} in which all characters NOT matching the RegEx [^a-zA-Z0-9_]
     * are replaced by "_".
     *
     * @param device Discovered device model
     * @return ThingUID without illegal characters.
     */
    public @Nullable ThingUID getThingUID(AVMFritzBaseModel device) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, getThingTypeId(device));
        ThingUID bridgeUID = thing.getUID();
        String thingName = getThingName(device);

        if (SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
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
            List<String> interfaces = Arrays
                    .asList(((DeviceModel) device).getEtsiunitinfo().getInterfaces().split(","));
            if (interfaces.contains(HAN_FUN_INTERFACE_ALERT)) {
                return DEVICE_HAN_FUN_CONTACT;
            } else if (interfaces.contains(HAN_FUN_INTERFACE_SIMPLE_BUTTON)) {
                return DEVICE_HAN_FUN_SWITCH;
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
        switch (channelId) {
            case CHANNEL_APPLY_TEMPLATE:
                applyTemplate(command, fritzBox);
                break;
            default:
                logger.debug("Received unknown channel {}", channelId);
                break;
        }
    }

    protected void applyTemplate(Command command, FritzAhaWebInterface fritzBox) {
        if (command instanceof StringType) {
            fritzBox.applyTemplate(command.toString());
        }
        updateState(CHANNEL_APPLY_TEMPLATE, UnDefType.UNDEF);
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
