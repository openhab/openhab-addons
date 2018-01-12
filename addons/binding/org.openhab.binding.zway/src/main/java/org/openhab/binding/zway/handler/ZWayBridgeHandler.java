/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zway.handler;

import static org.openhab.binding.zway.ZWayBindingConstants.*;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zway.internal.config.ZWayBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fh_zwickau.informatik.sensor.IZWayApi;
import de.fh_zwickau.informatik.sensor.IZWayApiCallbacks;
import de.fh_zwickau.informatik.sensor.ZWayApiHttp;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistory;
import de.fh_zwickau.informatik.sensor.model.devicehistory.DeviceHistoryList;
import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceCommand;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.instances.Instance;
import de.fh_zwickau.informatik.sensor.model.instances.InstanceList;
import de.fh_zwickau.informatik.sensor.model.instances.openhabconnector.OpenHABConnector;
import de.fh_zwickau.informatik.sensor.model.instances.openhabconnector.OpenHabConnectorZWayServer;
import de.fh_zwickau.informatik.sensor.model.locations.Location;
import de.fh_zwickau.informatik.sensor.model.locations.LocationList;
import de.fh_zwickau.informatik.sensor.model.modules.ModuleList;
import de.fh_zwickau.informatik.sensor.model.namespaces.NamespaceList;
import de.fh_zwickau.informatik.sensor.model.notifications.Notification;
import de.fh_zwickau.informatik.sensor.model.notifications.NotificationList;
import de.fh_zwickau.informatik.sensor.model.profiles.Profile;
import de.fh_zwickau.informatik.sensor.model.profiles.ProfileList;
import de.fh_zwickau.informatik.sensor.model.zwaveapi.controller.ZWaveController;
import de.fh_zwickau.informatik.sensor.model.zwaveapi.devices.ZWaveDevice;

/**
 * The {@link ZWayBridgeHandler} manages the connection between Z-Way API and binding.
 *
 * During the initialization the following tasks are performed:
 * - load and check configuration
 * - instantiate a Z-Way API that used in the whole binding
 * - authenticate to the Z-Way server
 * - check existence of openHAB Connector in Z-Way server and configure openHAB server
 * - after update, perform refresh listener command to openHAB Connector
 * - initialize all containing device things
 *
 * During the removal process the following tasks are performed:
 * - clean up openHAB Connector configuration
 * - important: the configured devices not changed in openHAB Connector!
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayBridgeHandler extends BaseBridgeHandler implements IZWayApiCallbacks {

    public static final ThingTypeUID SUPPORTED_THING_TYPE = THING_TYPE_BRIDGE;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private BridgePolling bridgePolling;
    private ScheduledFuture<?> pollingJob;

    private ResetInclusionExclusion resetInclusionExclusion;
    private ScheduledFuture<?> resetInclusionExclusionJob;

    private ZWayBridgeConfiguration mConfig = null;
    private IZWayApi mZWayApi = null;

    /**
     * Initializer authenticate the Z-Way API instance with bridge configuration.
     *
     * If Z-Way API successfully authenticated:
     * - check existence of openHAB Connector in Z-Way server and configure openHAB server
     * - initialize all containing device things
     */
    private class Initializer implements Runnable {

        @Override
        public void run() {
            logger.debug("Authenticate to the Z-Way server ...");

            // https://community.openhab.org/t/oh2-major-bug-with-scheduled-jobs/12350/11
            // If any execution of the task encounters an exception, subsequent executions are
            // suppressed. Otherwise, the task will only terminate via cancellation or
            // termination of the executor.
            try {
                // Authenticate - thing status update with a error message
                if (mZWayApi.getLogin() != null) {
                    // Thing status set to online in login callback
                    logger.info("Z-Way bridge successfully authenticated");

                    // Register openHAB server to Z-Way if observer mechanism is enabled
                    if (mConfig.getObserverMechanismEnabled()) {
                        updateOpenHabConnector(false);
                    }

                    // Initialize bridge polling
                    if (pollingJob == null || pollingJob.isCancelled()) {
                        logger.debug("Starting polling job at intervall {}", mConfig.getPollingInterval());
                        pollingJob = scheduler.scheduleWithFixedDelay(bridgePolling, 10, mConfig.getPollingInterval(),
                                TimeUnit.SECONDS);
                    } else {
                        // Called when thing or bridge updated ...
                        logger.debug("Polling is allready active");
                    }

                    // Initializing all containing device things
                    logger.debug("Initializing all configured devices ...");
                    for (Thing thing : getThing().getThings()) {
                        ThingHandler handler = thing.getHandler();
                        if (handler != null) {
                            logger.debug("Initializing device: {}", thing.getLabel());
                            handler.initialize();
                        } else {
                            logger.warn("Initializing device failed (DeviceHandler is null): {}", thing.getLabel());
                        }
                    }
                } else {
                    logger.warn("Z-Way bridge couldn't authenticated");
                }
            } catch (Throwable t) {
                if (t instanceof Exception) {
                    logger.error("{}", t.getMessage());
                } else if (t instanceof Error) {
                    logger.error("{}", t.getMessage());
                } else {
                    logger.error("Unexpected error");
                }
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Error occurred when initialize bridge.");
                }
            }
        }
    };

    /**
     * Disposer clean up openHAB Connector configuration
     */
    private class Remover implements Runnable {

        @Override
        public void run() {
            // Removing all containing device things
            logger.debug("Removing all configured devices ...");
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler != null) {
                    logger.debug("Removing device: {}", thing.getLabel());
                    handler.handleRemoval();
                } else {
                    logger.warn("Removing device failed (DeviceHandler is null): {}", thing.getLabel());
                }
            }

            // Remove openHAB server to Z-Way if observer mechanism is enabled
            if (mConfig.getObserverMechanismEnabled()) {
                updateOpenHabConnector(true);
            }

            // status update will finally remove the thing
            updateStatus(ThingStatus.REMOVED);
        }
    };

    public ZWayBridgeHandler(Bridge bridge) {
        super(bridge);

        bridgePolling = new BridgePolling();
        resetInclusionExclusion = new ResetInclusionExclusion();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // possible commands: check Z-Way server, check openHAB Connector, reconnect, ...
        logger.debug("Handle command for channel: {} with command: {}", channelUID.getId(), command.toString());

        if (channelUID.getId().equals(ACTIONS_CHANNEL)) {
            if (command.toString().equals(ACTIONS_CHANNEL_OPTION_REFRESH)) {
                logger.debug("Handle bridge refresh command for all configured devices ...");
                for (Thing thing : getThing().getThings()) {
                    ZWayDeviceHandler handler = (ZWayDeviceHandler) thing.getHandler();
                    if (handler != null) {
                        logger.debug("Refreshing device: {}", thing.getLabel());
                        handler.refreshAllChannels();
                    } else {
                        logger.warn("Refreshing device failed (DeviceHandler is null): {}", thing.getLabel());
                    }
                }
            }
        } else if (channelUID.getId().equals(SECURE_INCLUSION_CHANNEL)) {
            if (command.equals(OnOffType.ON)) {
                logger.debug("Enable bridge secure inclusion ...");
                mZWayApi.updateControllerData("secureInclusion", "true");
            } else if (command.equals(OnOffType.OFF)) {
                logger.debug("Disable bridge secure inclusion ...");
                mZWayApi.updateControllerData("secureInclusion", "false");
            }
        } else if (channelUID.getId().equals(INCLUSION_CHANNEL)) {
            if (command.equals(OnOffType.ON)) {
                logger.debug("Handle bridge start inclusion command ...");
                mZWayApi.getZWaveInclusion(1);

                // Start reset job
                if (resetInclusionExclusionJob == null || resetInclusionExclusionJob.isCancelled()) {
                    logger.debug("Starting reset inclusion and exclusion job in 30 seconds");
                    resetInclusionExclusionJob = scheduler.schedule(resetInclusionExclusion, 30, TimeUnit.SECONDS);
                }
            } else if (command.equals(OnOffType.OFF)) {
                logger.debug("Handle bridge stop inclusion command ...");
                mZWayApi.getZWaveInclusion(0);
            }
        } else if (channelUID.getId().equals(EXCLUSION_CHANNEL)) {
            if (command.equals(OnOffType.ON)) {
                logger.debug("Handle bridge start exclusion command ...");
                mZWayApi.getZWaveExclusion(1);

                // Start reset job
                if (resetInclusionExclusionJob == null || resetInclusionExclusionJob.isCancelled()) {
                    logger.debug("Starting reset inclusion and exclusion job in 30 seconds");
                    resetInclusionExclusionJob = scheduler.schedule(resetInclusionExclusion, 30, TimeUnit.SECONDS);
                }
            } else if (command.equals(OnOffType.OFF)) {
                logger.debug("Handle bridge stop exclusion command ...");
                mZWayApi.getZWaveExclusion(0);
            }
        }
    }

    @Override
    public void initialize() {
        logger.info("Initializing Z-Way bridge ...");

        // Set thing status to a valid status
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Checking configuration...");

        // Configuration - thing status update with a error message
        mConfig = loadAndCheckConfiguration();

        if (mConfig != null) {
            // Check if openHAB alias already set
            if (mConfig.getOpenHabAlias() == null) {
                Configuration config = editConfiguration();
                if (config != null) {
                    Integer shortUnixTimestamp = (int) (System.currentTimeMillis() / 1000L);
                    logger.debug("openHAB alias generated: {}", shortUnixTimestamp.toString());
                    config.put(BRIDGE_CONFIG_OPENHAB_ALIAS, shortUnixTimestamp.toString());
                    updateConfiguration(config);

                    // update configuration instance
                    mConfig.setOpenHabAlias(shortUnixTimestamp.toString());
                } else {
                    logger.error("Can't generate openHAB alias (editable configuration not available)");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Can't generate openHAB alias (editable configuration not available)");
                }
            } else {
                logger.debug("openHAB alias manually set");
            }

            if (mConfig.getOpenHabAlias() != null) {
                logger.debug("Configuration complete: {}", mConfig);

                mZWayApi = new ZWayApiHttp(mConfig.getZWayIpAddress(), mConfig.getZWayPort(), mConfig.getZWayProtocol(),
                        mConfig.getZWayUsername(), mConfig.getZWayPassword(), -1, false, this);

                // Start an extra thread, because it takes sometimes more
                // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
                scheduler.execute(new Initializer());
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Z-Way bridge ...");

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        if (resetInclusionExclusionJob != null && !resetInclusionExclusionJob.isCancelled()) {
            resetInclusionExclusionJob.cancel(true);
            resetInclusionExclusionJob = null;
        }

        super.dispose();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Handle Z-Way bridge configuration update ...");

        Boolean observerMechanismEnabledOld = null;
        Boolean observerMechanismEnabledNew = null;

        if (mConfig != null) {
            observerMechanismEnabledOld = mConfig.getObserverMechanismEnabled();
            observerMechanismEnabledNew = (Boolean) configurationParameters
                    .get(BRIDGE_CONFIG_OBSERVER_MECHANISM_ENABLED);
        }

        if (observerMechanismEnabledOld != null && observerMechanismEnabledNew != null) {
            logger.debug("Observer mechanism enabled changed from {} to {}", observerMechanismEnabledOld,
                    observerMechanismEnabledNew);

            if (observerMechanismEnabledOld == true && observerMechanismEnabledNew == false) {
                updateOpenHabConnector(true);
            } else if (observerMechanismEnabledOld == false && observerMechanismEnabledNew == true) {
                updateOpenHabConnector(false);
            }
        } // if no old configuration available it's not an update

        super.handleConfigurationUpdate(configurationParameters);
    }

    private class BridgePolling implements Runnable {
        @Override
        public void run() {
            logger.debug("Starting polling for bridge: {}", getThing().getLabel());
            if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateControllerData();
            } else {
                logger.debug("Polling not possible, bridge isn't ONLINE");
            }
        }
    };

    private void updateControllerData() {
        // Add additional information as properties or update channels

        ZWaveController zwaveController = mZWayApi.getZWaveController();
        if (zwaveController != null) {
            Map<String, String> properties = editProperties();
            // ESH default properties
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, zwaveController.getData().getAPIVersion().getValue());
            properties.put(Thing.PROPERTY_HARDWARE_VERSION, zwaveController.getData().getZWaveChip().getValue());
            // Thing.PROPERTY_MODEL_ID not available, only manufacturerProductId
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, zwaveController.getData().getUuid().getValue());
            properties.put(Thing.PROPERTY_VENDOR, zwaveController.getData().getVendor().getValue());

            // Custom properties
            properties.put(BRIDGE_PROP_SOFTWARE_REVISION_VERSION,
                    zwaveController.getData().getSoftwareRevisionVersion().getValue());
            properties.put(BRIDGE_PROP_SOFTWARE_REVISION_DATE,
                    zwaveController.getData().getSoftwareRevisionDate().getValue());
            properties.put(BRIDGE_PROP_SDK, zwaveController.getData().getSDK().getValue());
            properties.put(BRIDGE_PROP_MANUFACTURER_ID, zwaveController.getData().getManufacturerId().getValue());
            properties.put(BRIDGE_PROP_SECURE_INCLUSION, zwaveController.getData().getSecureInclusion().getValue());
            properties.put(BRIDGE_PROP_FREQUENCY, zwaveController.getData().getFrequency().getValue());
            updateProperties(properties);

            // Update channels
            if (zwaveController.getData().getSecureInclusion().getValue().equals("true")) {
                updateState(SECURE_INCLUSION_CHANNEL, OnOffType.ON);
            } else {
                updateState(SECURE_INCLUSION_CHANNEL, OnOffType.OFF);
            }
        }
    }

    /**
     * Inclusion/Exclusion must be reset manually, also channel states.
     */
    private class ResetInclusionExclusion implements Runnable {
        @Override
        public void run() {
            logger.debug("Reset inclusion and exclusion for bridge: {}", getThing().getLabel());
            if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                mZWayApi.getZWaveInclusion(0);
                mZWayApi.getZWaveExclusion(0);

                updateState(INCLUSION_CHANNEL, OnOffType.OFF);
                updateState(EXCLUSION_CHANNEL, OnOffType.OFF);
            } else {
                logger.debug("Reset inclusion and exclusion not possible, bridge isn't ONLINE");
            }
        }
    };

    /**
     * Setup the openHAB server in openHAB Connector depending on configuration
     *
     * @param deleteOpenHabServer if true the configured openHAB server will be removed
     */
    private synchronized void updateOpenHabConnector(Boolean deleteOpenHabServer) {
        InstanceList instanceList = mZWayApi.getInstances();
        if (instanceList != null) {
            logger.debug("Check existence of openHAB Connector in Z-Way server");

            OpenHABConnector instance = (OpenHABConnector) instanceList.getInstanceByModuleId("OpenHABConnector");

            if (instance != null) {
                OpenHabConnectorZWayServer configuredServer = new OpenHabConnectorZWayServer(mConfig.getOpenHabAlias(),
                        mConfig.getOpenHabIpAddress(), mConfig.getOpenHabPort());

                if (deleteOpenHabServer) {
                    if (instance.getParams().getCommonOptions().removeOpenHabServer(configuredServer)) {
                        logger.debug("Configured openHAB server updated");

                        Instance updatedInstance = mZWayApi.putInstance(instance);
                        if (updatedInstance != null) {
                            logger.debug("openHAB server successfully removed from openHAB Connector");

                            refreshOpenConnector();
                        } else {
                            logger.warn("openHAB Connector configuration update failed");
                        }
                    } // else - update not necessary, no changes
                } else {
                    if (instance.getParams().getCommonOptions().updateOpenHabServer(configuredServer)) {
                        logger.debug("Configured openHAB server updated");

                        Instance updatedInstance = mZWayApi.putInstance(instance);
                        if (updatedInstance != null) {
                            logger.info("openHAB server successfully configured in openHAB Connector");

                            refreshOpenConnector();
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                    "openHAB Connector configuration update failed");

                            logger.warn("openHAB Connector configuration update failed");
                        }
                    } // else - update not necessary, no changes
                }
            } else {
                if (!deleteOpenHabServer) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "openHAB Connector doesn't exist in Z-Way server");
                } // else - error has no impact on the binding

                logger.warn("openHAB Connector doesn't exist in Z-Way server");
            }
        } else {
            if (!deleteOpenHabServer) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Instances not loaded");
            } // else - error has no impact on the binding

            logger.warn("Instances not loaded");
        }
    }

    /**
     * Refresh listener in Z-Way server (otherwise the listener will only be refreshed at server restart)
     */
    private synchronized void refreshOpenConnector() {
        DeviceCommand command = new DeviceCommand("OpenHabConnector", "refreshListener");

        String message = mZWayApi.getDeviceCommand(command);
        if (message != null) {
            logger.debug("Refresh listener finished successfully: {}", message);
        } else {
            logger.warn("Refresh listener failed.");
        }
    }

    @Override
    public void handleRemoval() {
        logger.debug("Handle removal Z-Way bridge ...");

        // Start an extra thread, because it takes sometimes more
        // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
        scheduler.execute(new Remover());

        // super.handleRemoval() called in every case in scheduled task ...
    }

    protected ZWayBridgeConfiguration getZWayBridgeConfiguration() {
        return mConfig;
    }

    private ZWayBridgeConfiguration loadAndCheckConfiguration() {
        ZWayBridgeConfiguration config = getConfigAs(ZWayBridgeConfiguration.class);

        /***********************************
         ****** openHAB configuration ******
         **********************************/

        // openHAB Alias
        // not required

        // openHAB IP address
        if (StringUtils.trimToNull(config.getOpenHabIpAddress()) == null) {
            config.setOpenHabIpAddress("localhost"); // default value
        }

        // openHAB Port
        if (config.getOpenHabPort() == null) {
            config.setOpenHabPort(8080);
        }

        // openHAB Protocol
        if (StringUtils.trimToNull(config.getOpenHabProtocol()) == null) {
            config.setOpenHabProtocol("http");
        }

        /****************************************
         ****** Z-Way server configuration ******
         ****************************************/

        // Z-Way IP address
        if (StringUtils.trimToNull(config.getZWayIpAddress()) == null) {
            config.setZWayIpAddress("localhost"); // default value
        }

        // Z-Way Port
        if (config.getZWayPort() == null) {
            config.setZWayPort(8083);
        }

        // Z-Way Protocol
        if (StringUtils.trimToNull(config.getZWayProtocol()) == null) {
            config.setZWayProtocol("http");
        }

        // Z-Way Password
        if (StringUtils.trimToNull(config.getZWayPassword()) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The connection to the Z-Way Server can't established, because the Z-Way password is missing. Please set a Z-Way password.");
            return null;
        }

        // Z-Way Username
        if (StringUtils.trimToNull(config.getZWayUsername()) == null) {
            config.setZWayUsername("admin"); // default value
        }

        /***********************************
         ****** General configuration ******
         **********************************/

        // Polling interval
        if (config.getPollingInterval() == null) {
            config.setPollingInterval(3600);
        }

        // Observer mechanism enabled
        if (config.getObserverMechanismEnabled() == null) {
            config.setObserverMechanismEnabled(true);
        }

        return config;
    }

    /**
     * @return Z-Way API instance
     */
    public IZWayApi getZWayApi() {
        return mZWayApi;
    }

    /********************************
     ****** Z-Way API callback ******
     *******************************/

    @Override
    public void getStatusResponse(String message) {
    }

    @Override
    public void getRestartResponse(Boolean status) {
    }

    @Override
    public void getLoginResponse(String sessionId) {
        logger.debug("New session id: {}", sessionId);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void getNamespacesResponse(NamespaceList namespaces) {
    }

    @Override
    public void getModulesResponse(ModuleList moduleList) {
    }

    @Override
    public void getInstancesResponse(InstanceList instanceList) {
    }

    @Override
    public void postInstanceResponse(Instance instance) {
    }

    @Override
    public void getInstanceResponse(Instance instance) {
    }

    @Override
    public void putInstanceResponse(Instance instance) {
    }

    @Override
    public void deleteInstanceResponse(boolean status) {
    }

    @Override
    public void getDevicesResponse(DeviceList deviceList) {
    }

    @Override
    public void putDeviceResponse(Device device) {
    }

    @Override
    public void getDeviceResponse(Device device) {
    }

    @Override
    public void getDeviceCommandResponse(String message) {
    }

    @Override
    public void getLocationsResponse(LocationList locationList) {
    }

    @Override
    public void postLocationResponse(Location location) {
    }

    @Override
    public void getLocationResponse(Location location) {
    }

    @Override
    public void putLocationResponse(Location location) {
    }

    @Override
    public void deleteLocationResponse(boolean status) {
    }

    @Override
    public void getProfilesResponse(ProfileList profileList) {
    }

    @Override
    public void postProfileResponse(Profile profile) {
    }

    @Override
    public void getProfileResponse(Profile profile) {
    }

    @Override
    public void putProfileResponse(Profile profile) {
    }

    @Override
    public void deleteProfileResponse(boolean status) {
    }

    @Override
    public void getNotificationsResponse(NotificationList notificationList) {
    }

    @Override
    public void getNotificationResponse(Notification notification) {
    }

    @Override
    public void putNotificationResponse(Notification notification) {
    }

    @Override
    public void getDeviceHistoriesResponse(DeviceHistoryList deviceHistoryList) {
    }

    @Override
    public void getDeviceHistoryResponse(DeviceHistory deviceHistory) {
    }

    @Override
    public void apiError(String message, boolean invalidateState) {
        if (invalidateState) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
    }

    @Override
    public void httpStatusError(int httpStatus, String message, boolean invalidateState) {
        if (invalidateState) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    message + "(HTTP status code: " + httpStatus + ").");
        }
    }

    @Override
    public void authenticationError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Authentication error. Please check username and password.");
    }

    @Override
    public void responseFormatError(String message, boolean invalidateApiState) {
        if (invalidateApiState) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
    }

    @Override
    public void message(int code, String message) {
    }

    @Override
    public void getZWaveDeviceResponse(ZWaveDevice zwaveDevice) {
    }

    @Override
    public void getZWaveControllerResponse(ZWaveController zwaveController) {
    }
}
