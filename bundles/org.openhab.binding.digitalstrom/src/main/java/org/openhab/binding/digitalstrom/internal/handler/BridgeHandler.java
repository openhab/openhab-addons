/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.handler;

import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants;
import org.openhab.binding.digitalstrom.internal.lib.climate.jsonresponsecontainer.impl.TemperatureControlStatus;
import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.event.EventListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.ConnectionListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.ManagerStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.SceneStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.TemperatureControlStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.TotalPowerConsumptionListener;
import org.openhab.binding.digitalstrom.internal.lib.listener.stateenums.ManagerStates;
import org.openhab.binding.digitalstrom.internal.lib.listener.stateenums.ManagerTypes;
import org.openhab.binding.digitalstrom.internal.lib.manager.ConnectionManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.DeviceStatusManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.SceneManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.StructureManager;
import org.openhab.binding.digitalstrom.internal.lib.manager.impl.ConnectionManagerImpl;
import org.openhab.binding.digitalstrom.internal.lib.manager.impl.DeviceStatusManagerImpl;
import org.openhab.binding.digitalstrom.internal.lib.manager.impl.SceneManagerImpl;
import org.openhab.binding.digitalstrom.internal.lib.manager.impl.StructureManagerImpl;
import org.openhab.binding.digitalstrom.internal.lib.manager.impl.TemperatureControlManager;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceStateUpdate;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringTypeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringUnitsEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.scene.InternalScene;
import org.openhab.binding.digitalstrom.internal.providers.DsChannelTypeProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeHandler} is the handler for a digitalSTROM-Server and connects it to
 * the framework.<br>
 * All {@link DeviceHandler}s and {@link SceneHandler}s use the {@link BridgeHandler} to execute the actual
 * commands.<br>
 * <br>
 * The {@link BridgeHandler} also:
 * <ul>
 * <li>manages the {@link DeviceStatusManager} (starts, stops, register {@link DeviceStatusListener},
 * register {@link SceneStatusListener} and so on)</li>
 * <li>creates and load the configurations in the {@link Config}.</li>
 * <li>implements {@link ManagerStatusListener} to manage the expiration of the Thing initializations</li>
 * <li>implements the {@link ConnectionListener} to manage the {@link ThingStatus} of this {@link BridgeHandler}</li>
 * <li>and implements the {@link TotalPowerConsumptionListener} to update his Channels.</li>
 * </ul>
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class BridgeHandler extends BaseBridgeHandler
        implements ConnectionListener, TotalPowerConsumptionListener, ManagerStatusListener {

    private final Logger logger = LoggerFactory.getLogger(BridgeHandler.class);

    /**
     * Contains all supported thing types of this handler
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_DSS_BRIDGE);

    private static final long RECONNECT_TRACKER_INTERVAL = 15;

    /* DS-Manager */
    private ConnectionManager connMan;
    private StructureManager structMan;
    private SceneManager sceneMan;
    private DeviceStatusManager devStatMan;
    private TemperatureControlManager tempContMan;

    private EventListener eventListener;
    private ScheduledFuture<?> reconnectTracker;

    private DeviceStatusListener deviceDiscovery;
    private SceneStatusListener sceneDiscovery;
    private TemperatureControlStatusListener temperatureControlDiscovery;
    private Config config;

    List<SceneStatusListener> unregisterSceneStatusListeners;
    private short connectionTimeoutCounter = 0;
    private final short ignoredTimeouts = 5;

    private class Initializer implements Runnable {

        BridgeHandler bridge;
        Config config;

        public Initializer(BridgeHandler bridge, Config config) {
            this.bridge = bridge;
            this.config = config;
        }

        @Override
        public void run() {
            logger.debug("Checking connection");
            if (connMan == null) {
                connMan = new ConnectionManagerImpl(config, bridge, true);
            } else {
                connMan.registerConnectionListener(bridge);
                connMan.configHasBeenUpdated();
            }

            logger.debug("Initializing digitalSTROM Manager ");
            if (eventListener == null) {
                eventListener = new EventListener(connMan);
            }
            if (structMan == null) {
                structMan = new StructureManagerImpl();
            }
            if (sceneMan == null) {
                sceneMan = new SceneManagerImpl(connMan, structMan, bridge, eventListener);
            }
            if (devStatMan == null) {
                devStatMan = new DeviceStatusManagerImpl(connMan, structMan, sceneMan, bridge, eventListener);
            } else {
                devStatMan.registerStatusListener(bridge);
            }

            devStatMan.registerTotalPowerConsumptionListener(bridge);

            if (connMan.checkConnection()) {
                logger.debug("connection established, start services");
                if (TemperatureControlManager.isHeatingControllerInstallated(connMan)) {
                    if (tempContMan == null) {
                        tempContMan = new TemperatureControlManager(connMan, eventListener,
                                temperatureControlDiscovery);
                        temperatureControlDiscovery = null;
                    } else {
                        if (temperatureControlDiscovery != null) {
                            tempContMan.registerTemperatureControlStatusListener(temperatureControlDiscovery);
                        }
                    }
                }
                structMan.generateZoneGroupNames(connMan);
                devStatMan.start();
                eventListener.start();
            }

            boolean configChanged = false;
            Configuration configuration = bridge.getConfig();
            if (connMan.getApplicationToken() != null) {
                configuration.remove(USER_NAME);
                configuration.remove(PASSWORD);
                logger.debug("Application-Token is: {}", connMan.getApplicationToken());
                configuration.put(APPLICATION_TOKEN, connMan.getApplicationToken());
                configChanged = true;
            }
            Map<String, String> properties = editProperties();
            String dSSname = connMan.getDigitalSTROMAPI().getInstallationName(connMan.getSessionToken());
            if (dSSname != null) {
                properties.put(DS_NAME, dSSname);
            }
            Map<String, String> dsidMap = connMan.getDigitalSTROMAPI().getDSID(connMan.getSessionToken());
            if (dsidMap != null) {
                logger.debug("{}", dsidMap);
                properties.putAll(dsidMap);
            }
            Map<String, String> versions = connMan.getDigitalSTROMAPI().getSystemVersion();
            if (versions != null) {
                properties.putAll(versions);
            }
            String certProperty = getThing().getProperties().get(DigitalSTROMBindingConstants.SERVER_CERT);
            String certConfig = config.getCert();
            if ((certProperty == null || certProperty.isBlank()) && (certConfig != null && !certConfig.isBlank())) {
                properties.put(DigitalSTROMBindingConstants.SERVER_CERT, certConfig);
            }
            logger.debug("update properties");
            updateProperties(properties);

            if (configChanged) {
                updateConfiguration(configuration);
            }
        }
    }

    /**
     * Creates a new {@link BridgeHandler}.
     *
     * @param bridge must not be null
     */
    public BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing digitalSTROM-BridgeHandler");
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Checking configuration...");
        // Start an extra thread to readout the configuration and check the connection, because it takes sometimes more
        // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
        Config config = loadAndCheckConfig();

        if (config != null) {
            logger.debug("{}", config.toString());
            scheduler.execute(new Initializer(this, config));
        }
    }

    private boolean checkLoginConfig(Config config) {
        String userName = config.getUserName();
        String password = config.getPassword();
        String appToken = config.getAppToken();

        if (((userName != null && !userName.isBlank()) && (password != null && !password.isBlank()))
                || (appToken != null && !appToken.isBlank())) {
            return true;
        }
        onConnectionStateChange(CONNECTION_LOST, NO_USER_PASSWORD);
        return false;
    }

    private Config loadAndCheckConfig() {
        Configuration thingConfig = super.getConfig();
        Config config = loadAndCheckConnectionData(thingConfig);
        if (config == null) {
            return null;
        }
        logger.debug("Loading configuration");
        List<String> numberExc = new ArrayList<>();
        // Parameters can't be null, because of an existing default value.
        if (thingConfig.get(DigitalSTROMBindingConstants.SENSOR_DATA_UPDATE_INTERVAL) instanceof BigDecimal) {
            config.setSensordataRefreshInterval(
                    ((BigDecimal) thingConfig.get(DigitalSTROMBindingConstants.SENSOR_DATA_UPDATE_INTERVAL)).intValue()
                            * 1000);
        } else {
            numberExc.add("\"Sensor update interval\" ( "
                    + thingConfig.get(DigitalSTROMBindingConstants.SENSOR_DATA_UPDATE_INTERVAL) + ")");
        }
        if (thingConfig.get(DigitalSTROMBindingConstants.TOTAL_POWER_UPDATE_INTERVAL) instanceof BigDecimal) {
            config.setTotalPowerUpdateInterval(
                    ((BigDecimal) thingConfig.get(DigitalSTROMBindingConstants.TOTAL_POWER_UPDATE_INTERVAL)).intValue()
                            * 1000);
        } else {
            numberExc.add("\"Total power update interval\" ("
                    + thingConfig.get(DigitalSTROMBindingConstants.TOTAL_POWER_UPDATE_INTERVAL) + ")");
        }
        if (thingConfig.get(DigitalSTROMBindingConstants.SENSOR_WAIT_TIME) instanceof BigDecimal) {
            config.setSensorReadingWaitTime(
                    ((BigDecimal) thingConfig.get(DigitalSTROMBindingConstants.SENSOR_WAIT_TIME)).intValue() * 1000);
        } else {
            numberExc.add("\"Wait time sensor reading\" ("
                    + thingConfig.get(DigitalSTROMBindingConstants.SENSOR_WAIT_TIME) + ")");
        }
        if (thingConfig.get(DigitalSTROMBindingConstants.DEFAULT_TRASH_DEVICE_DELETE_TIME_KEY) instanceof BigDecimal) {
            config.setTrashDeviceDeleteTime(
                    ((BigDecimal) thingConfig.get(DigitalSTROMBindingConstants.DEFAULT_TRASH_DEVICE_DELETE_TIME_KEY))
                            .intValue());
        } else {
            numberExc.add("\"Days to be slaked trash bin devices\" ("
                    + thingConfig.get(DigitalSTROMBindingConstants.DEFAULT_TRASH_DEVICE_DELETE_TIME_KEY) + ")");
        }
        if (!numberExc.isEmpty()) {
            String excText = "The field ";
            for (int i = 0; i < numberExc.size(); i++) {
                excText = excText + numberExc.get(i);
                if (i < numberExc.size() - 2) {
                    excText = excText + ", ";
                } else if (i < numberExc.size() - 1) {
                    excText = excText + " and ";
                }
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, excText + " have to be a number.");
            return null;
        }
        String servertCert = getThing().getProperties().get(DigitalSTROMBindingConstants.SERVER_CERT);
        if (servertCert != null && !servertCert.isBlank()) {
            config.setCert(servertCert);
        }
        return config;
    }

    private Config loadAndCheckConnectionData(Configuration thingConfig) {
        if (this.config == null) {
            this.config = new Config();
        }
        // load and check connection and authorization data
        String host = (String) thingConfig.get(HOST);
        if (host != null && !host.isBlank()) {
            config.setHost(host);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The connection to the digitalSTROM-Server can't established, because the host address is missing. Please set the host address.");
            return null;
        }
        if (thingConfig.get(USER_NAME) != null) {
            config.setUserName(thingConfig.get(USER_NAME).toString());
        } else {
            config.setUserName(null);
        }
        if (thingConfig.get(PASSWORD) != null) {
            config.setPassword(thingConfig.get(PASSWORD).toString());
        } else {
            config.setPassword(null);
        }
        if (thingConfig.get(APPLICATION_TOKEN) != null) {
            config.setAppToken(thingConfig.get(APPLICATION_TOKEN).toString());
        } else {
            config.setAppToken(null);
        }

        if (!checkLoginConfig(config)) {
            return null;
        }
        return config;
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed");
        if (reconnectTracker != null && !reconnectTracker.isCancelled()) {
            reconnectTracker.cancel(true);
        }
        if (eventListener != null) {
            eventListener.stop();
        }
        if (devStatMan != null) {
            devStatMan.unregisterTotalPowerConsumptionListener();
            devStatMan.unregisterStatusListener();
            this.devStatMan.stop();
        }
        if (connMan != null) {
            connMan.unregisterConnectionListener();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            channelLinked(channelUID);
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public void handleRemoval() {
        String applicationToken = (String) super.getConfig().get(APPLICATION_TOKEN);
        if (applicationToken != null && !applicationToken.isEmpty()) {
            if (connMan == null) {
                Config config = loadAndCheckConnectionData(this.getConfig());
                if (config != null) {
                    this.connMan = new ConnectionManagerImpl(config, null, false);
                } else {
                    updateStatus(ThingStatus.REMOVED);
                    return;
                }
            }
            if (connMan.removeApplicationToken()) {
                logger.debug("Application-Token deleted");
            }
        }
        updateStatus(ThingStatus.REMOVED);
    }

    /* methods to store listener */

    /**
     * Registers a new {@link DeviceStatusListener} on the {@link DeviceStatusManager}.
     *
     * @param deviceStatusListener (must not be null)
     */
    public synchronized void registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (this.devStatMan != null) {
            if (deviceStatusListener == null) {
                throw new IllegalArgumentException("It's not allowed to pass null.");
            }

            if (deviceStatusListener.getDeviceStatusListenerID() != null) {
                if (devStatMan.getManagerState().equals(ManagerStates.RUNNING)) {
                    devStatMan.registerDeviceListener(deviceStatusListener);
                } else if (deviceStatusListener.getDeviceStatusListenerID()
                        .equals(DeviceStatusListener.DEVICE_DISCOVERY)) {
                    devStatMan.registerDeviceListener(deviceStatusListener);
                }
            } else {
                throw new IllegalArgumentException("It's not allowed to pass a DeviceStatusListener with ID = null.");
            }
        } else {
            if (deviceStatusListener.getDeviceStatusListenerID().equals(DeviceStatusListener.DEVICE_DISCOVERY)) {
                deviceDiscovery = deviceStatusListener;
            }
        }
    }

    /**
     * Unregisters a new {@link DeviceStatusListener} on the {@link BridgeHandler}.
     *
     * @param deviceStatusListener (must not be null)
     */
    public void unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (this.devStatMan != null) {
            if (deviceStatusListener.getDeviceStatusListenerID() != null) {
                this.devStatMan.unregisterDeviceListener(deviceStatusListener);
            } else {
                throw new IllegalArgumentException("It's not allowed to pass a DeviceStatusListener with ID = null.");
            }
        }
    }

    /**
     * Registers a new {@link SceneStatusListener} on the {@link BridgeHandler}.
     *
     * @param sceneStatusListener (must not be null)
     */
    public synchronized void registerSceneStatusListener(SceneStatusListener sceneStatusListener) {
        if (this.sceneMan != null) {
            if (sceneStatusListener == null) {
                throw new IllegalArgumentException("It's not allowed to pass null.");
            }

            if (sceneStatusListener.getSceneStatusListenerID() != null) {
                this.sceneMan.registerSceneListener(sceneStatusListener);
            } else {
                throw new IllegalArgumentException("It's not allowed to pass a SceneStatusListener with ID = null.");
            }
        } else {
            if (sceneStatusListener.getSceneStatusListenerID().equals(SceneStatusListener.SCENE_DISCOVERY)) {
                sceneDiscovery = sceneStatusListener;
            }
        }
    }

    /**
     * Unregisters a new {@link SceneStatusListener} on the {@link DeviceStatusManager}.
     *
     * @param sceneStatusListener (must not be null)
     */
    public void unregisterSceneStatusListener(SceneStatusListener sceneStatusListener) {
        if (this.sceneMan != null) {
            if (sceneStatusListener.getSceneStatusListenerID() != null) {
                this.sceneMan.unregisterSceneListener(sceneStatusListener);
            } else {
                throw new IllegalArgumentException("It's not allowed to pass a SceneStatusListener with ID = null..");
            }
        }
    }

    /**
     * Has to be called from a removed Thing-Child to rediscovers the Thing.
     *
     * @param id = scene or device id (must not be null)
     */
    public void childThingRemoved(String id) {
        if (id != null && id.split("-").length == 3) {
            InternalScene scene = sceneMan.getInternalScene(id);
            if (scene != null) {
                sceneMan.removeInternalScene(id);
                sceneMan.addInternalScene(scene);
            }
        } else {
            devStatMan.removeDevice(id);
        }
    }

    /**
     * Delegate a stop command from a Thing to the {@link DeviceStatusManager#sendStopComandsToDSS(Device)}.
     *
     * @param device can be null
     */
    public void stopOutputValue(Device device) {
        this.devStatMan.sendStopComandsToDSS(device);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (devStatMan != null) {
            MeteringTypeEnum meteringType = DsChannelTypeProvider.getMeteringType(channelUID.getId());
            if (meteringType != null) {
                if (meteringType.equals(MeteringTypeEnum.ENERGY)) {
                    onEnergyMeterValueChanged(devStatMan.getTotalEnergyMeterValue());
                } else {
                    onTotalPowerConsumptionChanged(devStatMan.getTotalPowerConsumption());
                }
            } else {
                logger.warn("Channel with id {} is not known for the thing with id {}.", channelUID.getId(),
                        getThing().getUID());
            }
        }
    }

    @Override
    public void onTotalPowerConsumptionChanged(int newPowerConsumption) {
        updateChannelState(MeteringTypeEnum.CONSUMPTION, MeteringUnitsEnum.WH, newPowerConsumption);
    }

    @Override
    public void onEnergyMeterValueChanged(int newEnergyMeterValue) {
        updateChannelState(MeteringTypeEnum.ENERGY, MeteringUnitsEnum.WH, newEnergyMeterValue * 0.001);
    }

    @Override
    public void onEnergyMeterWsValueChanged(int newEnergyMeterValue) {
        // not needed
    }

    private void updateChannelState(MeteringTypeEnum meteringType, MeteringUnitsEnum meteringUnit, double value) {
        String channelID = DsChannelTypeProvider.getMeteringChannelID(meteringType, meteringUnit, true);
        if (getThing().getChannel(channelID) != null) {
            updateState(channelID, new DecimalType(value));
        }
    }

    @Override
    public void onConnectionStateChange(String newConnectionState) {
        switch (newConnectionState) {
            case CONNECTION_LOST:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "The connection to the digitalSTROM-Server cannot be established.");
                startReconnectTracker();
                return;
            case CONNECTION_RESUMED:
                if (connectionTimeoutCounter > 0) {
                    // reset connection timeout counter
                    connectionTimeoutCounter = 0;
                    if (connMan.checkConnection()) {
                        restartServices();
                        setStatus(ThingStatus.ONLINE);
                    }
                }
                return;
            case APPLICATION_TOKEN_GENERATED:
                if (connMan != null) {
                    Configuration config = this.getConfig();
                    config.remove(USER_NAME);
                    config.remove(PASSWORD);
                    config.put(APPLICATION_TOKEN, connMan.getApplicationToken());
                    this.updateConfiguration(config);
                }
                return;
            default:
                return;
        }
    }

    private void setStatus(ThingStatus status) {
        logger.debug("set status to: {}", status);
        updateStatus(status);
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler != null) {
                handler.bridgeStatusChanged(getThing().getStatusInfo());
            }
        }
    }

    private void startReconnectTracker() {
        if (reconnectTracker == null || reconnectTracker.isCancelled()) {
            logger.debug("Connection lost, stop all services and start reconnectTracker.");
            stopServices();
            reconnectTracker = scheduler.scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {
                    if (connMan != null) {
                        boolean conStat = connMan.checkConnection();
                        logger.debug("check connection = {}", conStat);
                        if (conStat) {
                            restartServices();
                            reconnectTracker.cancel(false);
                        }
                    }
                }
            }, RECONNECT_TRACKER_INTERVAL, RECONNECT_TRACKER_INTERVAL, TimeUnit.SECONDS);
        }
    }

    private void stopServices() {
        if (devStatMan != null && !devStatMan.getManagerState().equals(ManagerStates.STOPPED)) {
            devStatMan.stop();
        }
        if (eventListener != null && eventListener.isStarted()) {
            eventListener.stop();
        }
    }

    private void restartServices() {
        logger.debug("reconnect, stop reconnection tracker and restart services");
        if (reconnectTracker != null && !reconnectTracker.isCancelled()) {
            reconnectTracker.cancel(true);
        }
        stopServices();
        if (devStatMan != null) {
            devStatMan.start();
        }
        if (eventListener != null) {
            eventListener.start();
        }
    }

    @Override
    public void onConnectionStateChange(String newConnectionState, String reason) {
        if (newConnectionState.equals(NOT_AUTHENTICATED) || newConnectionState.equals(CONNECTION_LOST)) {
            switch (reason) {
                case WRONG_APP_TOKEN:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            """
                                    User defined Application-Token is wrong. \
                                    Please set user name and password to generate an Application-Token or set a valid Application-Token.\
                                    """);
                    stopServices();
                    return;
                case WRONG_USER_OR_PASSWORD:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "The set username or password is wrong.");
                    stopServices();
                    return;
                case NO_USER_PASSWORD:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No username or password is set to generate Application-Token. Please set user name and password or Application-Token.");
                    stopServices();
                    return;
                case CONNECTON_TIMEOUT:
                    // ignore the first connection timeout
                    if (connectionTimeoutCounter++ > ignoredTimeouts) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Connection lost because connection timeout to Server.");
                        break;
                    } else {
                        return;
                    }
                case HOST_NOT_FOUND:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, """
                            Server not found! Please check these points:
                             - Is digitalSTROM-Server turned on?
                             - Is the host address correct?
                             - Is the ethernet cable connection established?\
                            """);
                    break;
                case UNKNOWN_HOST:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unknown host name, please check the set host name!");
                    break;
                case INVALID_URL:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid URL is set.");
                    break;
                case CONNECTION_LOST:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "IOException / Connection lost.");
                    break;
                case SSL_HANDSHAKE_ERROR:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "SSL Handshake error / Connection lost.");
                    break;
                default:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
            }
            // reset connection timeout counter
            connectionTimeoutCounter = 0;
            startReconnectTracker();
        }
    }

    /**
     * Returns a list of all {@link Device}'s.
     *
     * @return device list (cannot be null)
     */
    public List<Device> getDevices() {
        return this.structMan != null && this.structMan.getDeviceMap() != null
                ? new LinkedList<>(this.structMan.getDeviceMap().values())
                : null;
    }

    /**
     * Returns the {@link StructureManager}.
     *
     * @return StructureManager
     */
    public StructureManager getStructureManager() {
        return this.structMan;
    }

    /**
     * Delegates a scene command of a Thing to the
     * {@link DeviceStatusManager#sendSceneComandsToDSS(InternalScene, boolean)}
     *
     * @param scene the called scene
     * @param call_undo (true = call scene | false = undo scene)
     */
    public void sendSceneComandToDSS(InternalScene scene, boolean call_undo) {
        if (devStatMan != null) {
            devStatMan.sendSceneComandsToDSS(scene, call_undo);
        }
    }

    /**
     * Delegates a device command of a Thing to the
     * {@link DeviceStatusManager#sendComandsToDSS(Device, DeviceStateUpdate)}
     *
     * @param device can be null
     * @param deviceStateUpdate can be null
     */
    public void sendComandsToDSS(Device device, DeviceStateUpdate deviceStateUpdate) {
        if (devStatMan != null) {
            devStatMan.sendComandsToDSS(device, deviceStateUpdate);
        }
    }

    /**
     * Returns a list of all {@link InternalScene}'s.
     *
     * @return Scene list (cannot be null)
     */
    public List<InternalScene> getScenes() {
        return sceneMan != null ? sceneMan.getScenes() : new LinkedList<>();
    }

    /**
     * Returns the {@link ConnectionManager}.
     *
     * @return ConnectionManager
     */
    public ConnectionManager getConnectionManager() {
        return this.connMan;
    }

    @Override
    public void onStatusChanged(ManagerTypes managerType, ManagerStates state) {
        if (managerType.equals(ManagerTypes.DEVICE_STATUS_MANAGER)) {
            switch (state) {
                case INITIALIZING:
                    if (deviceDiscovery != null) {
                        devStatMan.registerDeviceListener(deviceDiscovery);
                        deviceDiscovery = null;
                    }
                    logger.debug("Building digitalSTROM model");
                    break;
                case RUNNING:
                    updateStatus(ThingStatus.ONLINE);
                    break;
                case STOPPED:
                    if (!getThing().getStatusInfo().getStatusDetail().equals(ThingStatusDetail.COMMUNICATION_ERROR)
                            && !getThing().getStatusInfo().getStatusDetail()
                                    .equals(ThingStatusDetail.CONFIGURATION_ERROR)) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "DeviceStatusManager is stopped.");
                        devStatMan.start();
                    }
                    break;
                default:
                    break;
            }
        }
        if (managerType.equals(ManagerTypes.SCENE_MANAGER)) {
            switch (state) {
                case GENERATING_SCENES:
                    logger.debug("SceneManager reports that he is generating scenes");
                    if (sceneDiscovery != null) {
                        sceneMan.registerSceneListener(sceneDiscovery);
                        sceneDiscovery = null;
                    }
                    break;
                case RUNNING:
                    logger.debug("SceneManager reports that he is running");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Returns a {@link List} of all {@link Circuit}'s.
     *
     * @return circuit list
     */
    public List<Circuit> getCircuits() {
        logger.debug("circuits: {}", structMan.getCircuitMap().values().toString());
        return structMan != null && structMan.getCircuitMap() != null
                ? new LinkedList<>(structMan.getCircuitMap().values())
                : null;
    }

    /**
     * Returns the {@link TemperatureControlManager} or null if no one exist.
     *
     * @return {@link TemperatureControlManager}
     */
    public TemperatureControlManager getTemperatureControlManager() {
        return tempContMan;
    }

    /**
     * Registers the given {@link TemperatureControlStatusListener} to the {@link TemperatureControlManager}.
     *
     * @param temperatureControlStatusListener can be null
     */
    public void registerTemperatureControlStatusListener(
            TemperatureControlStatusListener temperatureControlStatusListener) {
        if (tempContMan != null) {
            tempContMan.registerTemperatureControlStatusListener(temperatureControlStatusListener);
        } else if (TemperatureControlStatusListener.DISCOVERY
                .equals(temperatureControlStatusListener.getTemperationControlStatusListenrID())) {
            this.temperatureControlDiscovery = temperatureControlStatusListener;
        }
    }

    /**
     * Unregisters the given {@link TemperatureControlStatusListener} from the {@link TemperatureControlManager}.
     *
     * @param temperatureControlStatusListener can be null
     */
    public void unregisterTemperatureControlStatusListener(
            TemperatureControlStatusListener temperatureControlStatusListener) {
        if (tempContMan != null) {
            tempContMan.unregisterTemperatureControlStatusListener(temperatureControlStatusListener);
        }
    }

    /**
     * see {@link TemperatureControlManager#getTemperatureControlStatusFromAllZones()}
     *
     * @return all temperature control status objects
     */
    public Collection<TemperatureControlStatus> getTemperatureControlStatusFromAllZones() {
        return tempContMan != null ? tempContMan.getTemperatureControlStatusFromAllZones() : new LinkedList<>();
    }
}
