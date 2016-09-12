/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zway.handler;

import static org.openhab.binding.zway.ZWayBindingConstants.*;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zway.config.ZWayBridgeConfiguration;
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
import de.fh_zwickau.informatik.sensor.model.devices.zwaveapi.ZWaveDevice;
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

/**
 * The {@link ZWayBridgeHandler} manages the connection between Z-Way API and binding.
 *
 * During the initialization the following tasks are performed:
 * - load and check configuration
 * - instantiate a Z-Way API that used in the whole binding
 * - authenticate to the Z-Way server
 * - check existence of openHAB connector in Z-Way server and configure openHAB server
 * - after update, perform refresh listener command to openHAB connector
 * - initialize all containing device things
 *
 * During the removal process the following tasks are performed:
 * - clean up openHAB connector configuration
 * - important: the configured devices not changed in openHAB connector!
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayBridgeHandler extends BaseBridgeHandler implements IZWayApiCallbacks {

    public final static ThingTypeUID SUPPORTED_THING_TYPE = THING_TYPE_BRIDGE;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ZWayBridgeConfiguration mConfig = null;
    private IZWayApi mZWayApi = null;

    /**
     * Initializer authenticate the Z-Way API instance with bridge configuration.
     *
     * If Z-Way API successfully authenticated:
     * - check existence of openHAB connector in Z-Way server and configure openHAB server
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
                    updateStatus(ThingStatus.ONLINE);
                    logger.info("Z-Way bridge successfully authenticated");

                    // Register openHAB server to Z-Way if observer mechanism is enabled
                    if (mConfig.getObserverMechanismEnabled()) {
                        updateOpenHabConnector(false);
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
                    logger.error(((Exception) t).getMessage());
                } else if (t instanceof Error) {
                    logger.error(((Error) t).getMessage());
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
     * Disposer clean up openHAB connector configuration
     */
    private class Disposer implements Runnable {

        @Override
        public void run() {
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
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // possible commands: check Z-Way server, check OpenHAB connector, reconnect, ...
    }

    @Override
    public void initialize() {
        logger.info("Initializing Z-Way bridge ...");

        // Set thing status to a valid status
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Checking configuration...");

        // Configuration - thing status update with a error message
        mConfig = loadAndCheckConfiguration(getConfig());

        if (mConfig != null) {
            // Check if openHAB alias already set
            if (mConfig.getOpenHabAlias() == null) {
                Configuration config = editConfiguration();
                if (config != null) {
                    Integer shortUnixTimestamp = (int) (System.currentTimeMillis() / 1000L);
                    logger.debug("OpenHAB alias generated: {}", shortUnixTimestamp.toString());
                    config.put(BRIDGE_CONFIG_OPENHAB_ALIAS, shortUnixTimestamp.toString());
                    updateConfiguration(config);

                    // update configuration instance
                    mConfig.setOpenHabAlias(shortUnixTimestamp.toString());
                } else {
                    logger.error("Can't generate openHAB alias (editable configuration not available)");
                }
            } else {
                logger.debug("OpenHAB alias manually set");
            }

            logger.debug("Configuration complete: {}", mConfig);

            mZWayApi = new ZWayApiHttp(mConfig.getZWayIpAddress(), mConfig.getZWayPort(), mConfig.getZWayProtocol(),
                    mConfig.getZWayUsername(), mConfig.getZWayPassword(), this);

            // Start an extra thread, because it takes sometimes more
            // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
            scheduler.execute(new Initializer());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Z-Way bridge ...");

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

    /**
     * Setup the openHAB server in openHAB connector depending on configuration
     *
     * @param deleteOpenHabServer if true the configured openHAB server will be removed
     */
    private synchronized void updateOpenHabConnector(Boolean deleteOpenHabServer) {
        InstanceList instanceList = mZWayApi.getInstances();
        if (instanceList != null) {
            logger.debug("Check existence of openHAB connector in Z-Way server");

            OpenHABConnector instance = (OpenHABConnector) instanceList.getInstanceByModuleId("OpenHABConnector");

            if (instance != null) {
                OpenHabConnectorZWayServer configuredServer = new OpenHabConnectorZWayServer(mConfig.getOpenHabAlias(),
                        mConfig.getOpenHabIpAddress(), mConfig.getOpenHabPort());

                if (deleteOpenHabServer) {
                    if (instance.getParams().getCommonOptions().removeOpenHabServer(configuredServer)) {
                        logger.debug("Configured openHAB server updated");

                        Instance updatedInstance = mZWayApi.putInstance(instance);
                        if (updatedInstance != null) {
                            logger.debug("OpenHAB server successfully removed from openHAB connector");

                            refreshOpenConnector();
                        } else {
                            logger.warn("OpenHAB connector configuration update failed");
                        }
                    } // else - update not necessary, no changes
                } else {
                    if (instance.getParams().getCommonOptions().updateOpenHabServer(configuredServer)) {
                        logger.debug("Configured openHAB server updated");

                        Instance updatedInstance = mZWayApi.putInstance(instance);
                        if (updatedInstance != null) {
                            logger.info("OpenHAB server successfully configured in openHAB connector");

                            refreshOpenConnector();
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                    "OpenHAB connector configuration update failed");

                            logger.warn("OpenHAB connector configuration update failed");
                        }
                    } // else - update not necessary, no changes
                }
            } else {
                if (!deleteOpenHabServer) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "OpenHAB connector doesn't exist in Z-Way server");
                } // else - error has no impact on the binding

                logger.warn("OpenHAB connector doesn't exist in Z-Way server");
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
        scheduler.execute(new Disposer());

        // super.handleRemoval() called in every case in scheduled task ...
    }

    protected ZWayBridgeConfiguration getZWayBridgeConfiguration() {
        return mConfig;
    }

    private ZWayBridgeConfiguration loadAndCheckConfiguration(Configuration thingConfig) {
        ZWayBridgeConfiguration config = new ZWayBridgeConfiguration();

        /***********************************
         ****** openHAB configuration ******
         **********************************/

        // openHab Alias
        if (StringUtils.isNotBlank((String) thingConfig.get(BRIDGE_CONFIG_OPENHAB_ALIAS))) {
            config.setOpenHabAlias(thingConfig.get(BRIDGE_CONFIG_OPENHAB_ALIAS).toString());
        }

        // openHab IP address
        if (StringUtils.isNotBlank((String) thingConfig.get(BRIDGE_CONFIG_OPENHAB_IP_ADDRESS))) {
            config.setOpenHabIpAddress(thingConfig.get(BRIDGE_CONFIG_OPENHAB_IP_ADDRESS).toString());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The connection to the Z-Way Server can't established, because the openHAB host address is missing. Please set a openHAB host address.");
            return null;
        }

        // openHab Port
        if (StringUtils.isNotBlank(thingConfig.get(BRIDGE_CONFIG_OPENHAB_PORT).toString())) {
            try {
                config.setOpenHabPort(Integer.parseInt(thingConfig.get(BRIDGE_CONFIG_OPENHAB_PORT).toString()));
            } catch (NumberFormatException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "OpenHAB Port have to be a number.");
                return null;
            }
        } else {
            config.setOpenHabPort(8083);
        }

        // openHab Protocol
        if (StringUtils.isNotBlank((String) thingConfig.get(BRIDGE_CONFIG_OPENHAB_PROTOCOL))) {
            config.setOpenHabProtocol(thingConfig.get(BRIDGE_CONFIG_OPENHAB_PROTOCOL).toString());
        } else {
            config.setOpenHabProtocol("http");
        }

        /****************************************
         ****** Z-Way server configuration ******
         ****************************************/

        // Z-Way IP address
        if (StringUtils.isNotBlank((String) thingConfig.get(BRIDGE_CONFIG_ZWAY_SERVER_IP_ADDRESS))) {
            config.setZWayIpAddress(thingConfig.get(BRIDGE_CONFIG_ZWAY_SERVER_IP_ADDRESS).toString());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The connection to the Z-Way Server can't established, because the Z-Way host address is missing. Please set a Z-Way host address.");
            return null;
        }

        // Z-Way Port
        if (StringUtils.isNotBlank(thingConfig.get(BRIDGE_CONFIG_ZWAY_SERVER_PORT).toString())) {
            try {
                config.setZWayPort(Integer.parseInt(thingConfig.get(BRIDGE_CONFIG_ZWAY_SERVER_PORT).toString()));
            } catch (NumberFormatException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Z-Way port have to be a number.");
                return null;
            }
        } else {
            config.setZWayPort(8083);
        }

        // Z-Way Protocol
        if (StringUtils.isNotBlank((String) thingConfig.get(BRIDGE_CONFIG_ZWAY_SERVER_PROTOCOL))) {
            config.setZWayProtocol(thingConfig.get(BRIDGE_CONFIG_ZWAY_SERVER_PROTOCOL).toString());
        } else {
            config.setZWayProtocol("http");
        }

        // Z-Way Password
        if (StringUtils.isNotBlank((String) thingConfig.get(BRIDGE_CONFIG_ZWAY_SERVER_PASSWORD))) {
            config.setZWayPassword(thingConfig.get(BRIDGE_CONFIG_ZWAY_SERVER_PASSWORD).toString());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The connection to the Z-Way Server can't established, because the Z-Way password is missing. Please set a Z-Way password.");
            return null;
        }

        // Z-Way Username
        if (StringUtils.isNotBlank((String) thingConfig.get(BRIDGE_CONFIG_ZWAY_SERVER_USERNAME))) {
            config.setZWayUsername(thingConfig.get(BRIDGE_CONFIG_ZWAY_SERVER_USERNAME).toString());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The connection to the Z-Way Server can't established, because the Z-Way username is missing. Please set a Z-Way username.");
            return null;
        }

        /***********************************
         ****** General configuration ******
         **********************************/

        // Polling interval
        if (StringUtils.isNotBlank(thingConfig.get(BRIDGE_CONFIG_POLLING_INTERVAL).toString())) {
            try {
                config.setPollingInterval(Integer.parseInt(thingConfig.get(BRIDGE_CONFIG_POLLING_INTERVAL).toString()));
            } catch (NumberFormatException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Polling interval have to be a number.");
                return null;
            }
        } else {
            config.setPollingInterval(3600);
        }

        // Observer mechanism enabled
        if (thingConfig.get(BRIDGE_CONFIG_OBSERVER_MECHANISM_ENABLED) != null) {
            config.setObserverMechanismEnabled((Boolean) thingConfig.get(BRIDGE_CONFIG_OBSERVER_MECHANISM_ENABLED));
        } else {
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
    public void postDeviceResponse(Device device) {
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
        logger.warn(message);
    }

    @Override
    public void httpStatusError(int httpStatus, String message, boolean invalidateState) {
        if (invalidateState) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    message + "(HTTP status code: " + httpStatus + ").");
        }
        logger.warn("Z-Way library - {} (HTTP status code: {}).", message, httpStatus);
    }

    @Override
    public void authenticationError() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Authentication error. Please check username and password.");
        logger.warn("Z-Way library - Authentication error");
    }

    @Override
    public void responseFormatError(String message, boolean invalidateApiState) {
        if (invalidateApiState) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        }
        logger.warn("Z-Way library - Response format error: {}", message);
    }

    @Override
    public void getZWaveDeviceResponse(ZWaveDevice zwaveDevice) {
    }
}
