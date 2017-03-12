/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.client.BinRpcClient;
import org.openhab.binding.homematic.internal.communicator.client.RpcClient;
import org.openhab.binding.homematic.internal.communicator.client.TransferMode;
import org.openhab.binding.homematic.internal.communicator.client.UnknownParameterSetException;
import org.openhab.binding.homematic.internal.communicator.client.XmlRpcClient;
import org.openhab.binding.homematic.internal.communicator.server.BinRpcServer;
import org.openhab.binding.homematic.internal.communicator.server.RpcEventListener;
import org.openhab.binding.homematic.internal.communicator.server.RpcServer;
import org.openhab.binding.homematic.internal.communicator.server.XmlRpcServer;
import org.openhab.binding.homematic.internal.communicator.virtual.BatteryTypeVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.DeleteDeviceModeVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.DeleteDeviceVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.DisplayOptionsVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.FirmwareVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.InstallModeDurationVirtualDatapoint;
import org.openhab.binding.homematic.internal.communicator.virtual.InstallModeVirtualDatapoint;
import org.openhab.binding.homematic.internal.communicator.virtual.OnTimeAutomaticVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.ReloadAllFromGatewayVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.ReloadFromGatewayVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.ReloadRssiVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.RssiVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.SignalStrengthVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.StateContactVirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.VirtualDatapointHandler;
import org.openhab.binding.homematic.internal.communicator.virtual.VirtualGateway;
import org.openhab.binding.homematic.internal.misc.DelayedExecuter;
import org.openhab.binding.homematic.internal.misc.DelayedExecuter.DelayedExecuterCallback;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmGatewayInfo;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmRssiInfo;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractHomematicGateway} is the main class for the communication with a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class AbstractHomematicGateway implements RpcEventListener, HomematicGateway, VirtualGateway {
    private static final Logger logger = LoggerFactory.getLogger(HomematicGateway.class);
    public static final double DEFAULT_DISABLE_DELAY = 2.0;
    private static final long CONNECTION_TRACKER_INTERVAL_SECONDS = 15;
    private static final String GATEWAY_POOL_NAME = "homematicGateway";

    private Map<TransferMode, RpcClient> rpcClients = new HashMap<TransferMode, RpcClient>();
    private Map<TransferMode, RpcServer> rpcServers = new HashMap<TransferMode, RpcServer>();

    protected HomematicConfig config;
    private String id;
    private HomematicGatewayListener eventListener;
    private long lastEventTime = System.currentTimeMillis();
    private DelayedExecuter delayedExecutor = new DelayedExecuter();
    private Set<HmDatapointInfo> echoEvents = Collections.synchronizedSet(new HashSet<HmDatapointInfo>());
    private ScheduledFuture<?> eventTrackerThread;
    private ScheduledFuture<?> connectionTrackerThread;
    private ScheduledFuture<?> reconnectThread;
    private Map<String, HmDevice> devices = Collections.synchronizedMap(new HashMap<String, HmDevice>());
    private Map<HmInterface, TransferMode> availableInterfaces = new TreeMap<HmInterface, TransferMode>();
    private static List<VirtualDatapointHandler> virtualDatapointHandlers = new ArrayList<VirtualDatapointHandler>();
    private boolean cancelLoadAllMetadata;
    private boolean initialized;

    static {
        // loads all virtual datapoints
        virtualDatapointHandlers.add(new BatteryTypeVirtualDatapointHandler());
        virtualDatapointHandlers.add(new FirmwareVirtualDatapointHandler());
        virtualDatapointHandlers.add(new DisplayOptionsVirtualDatapointHandler());
        virtualDatapointHandlers.add(new ReloadFromGatewayVirtualDatapointHandler());
        virtualDatapointHandlers.add(new ReloadAllFromGatewayVirtualDatapointHandler());
        virtualDatapointHandlers.add(new OnTimeAutomaticVirtualDatapointHandler());
        virtualDatapointHandlers.add(new InstallModeVirtualDatapoint());
        virtualDatapointHandlers.add(new InstallModeDurationVirtualDatapoint());
        virtualDatapointHandlers.add(new DeleteDeviceModeVirtualDatapointHandler());
        virtualDatapointHandlers.add(new DeleteDeviceVirtualDatapointHandler());
        virtualDatapointHandlers.add(new RssiVirtualDatapointHandler());
        virtualDatapointHandlers.add(new ReloadRssiVirtualDatapointHandler());
        virtualDatapointHandlers.add(new StateContactVirtualDatapointHandler());
        virtualDatapointHandlers.add(new SignalStrengthVirtualDatapointHandler());
    }

    public AbstractHomematicGateway(String id, HomematicConfig config, HomematicGatewayListener eventListener) {
        this.id = id;
        this.config = config;
        this.eventListener = eventListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() throws IOException {
        logger.debug("Initializing gateway with id '{}'", id);

        HmGatewayInfo gatewayInfo = config.getGatewayInfo();
        if (gatewayInfo.isHomegear()) {
            // Homegear
            availableInterfaces.put(HmInterface.RF, TransferMode.BIN_RPC);
        } else if (gatewayInfo.isCCU()) {
            // CCU
            availableInterfaces.put(HmInterface.RF, TransferMode.BIN_RPC);
            if (gatewayInfo.isWiredInterface()) {
                availableInterfaces.put(HmInterface.WIRED, TransferMode.BIN_RPC);
            }
            if (gatewayInfo.isHmipInterface()) {
                availableInterfaces.put(HmInterface.HMIP, TransferMode.XML_RPC);
            }
            if (gatewayInfo.isCuxdInterface()) {
                availableInterfaces.put(HmInterface.CUXD, TransferMode.BIN_RPC);
            }
            if (gatewayInfo.isGroupInterface()) {
                availableInterfaces.put(HmInterface.GROUP, TransferMode.XML_RPC);
            }
        } else {
            // other
            availableInterfaces.put(HmInterface.RF, TransferMode.XML_RPC);
            if (gatewayInfo.isWiredInterface()) {
                availableInterfaces.put(HmInterface.WIRED, TransferMode.XML_RPC);
            }
            if (gatewayInfo.isHmipInterface()) {
                availableInterfaces.put(HmInterface.HMIP, TransferMode.XML_RPC);
            }
        }

        logger.info("{}", config.getGatewayInfo());
        StringBuilder sb = new StringBuilder();
        for (Entry<HmInterface, TransferMode> entry : availableInterfaces.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(", ");
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        logger.debug("Used Homematic transfer modes: {}", sb.toString());
        startClients();
        startServers();
        startWatchdogs();
        initialized = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        initialized = false;
        stopWatchdogs();
        delayedExecutor.stop();
        stopServers();
        stopClients();
        devices.clear();
        echoEvents.clear();
        availableInterfaces.clear();
        config.setGatewayInfo(null);
    }

    /**
     * Starts the Homematic gateway client.
     */
    protected void startClients() throws IOException {
        for (TransferMode mode : availableInterfaces.values()) {
            if (!rpcClients.containsKey(mode)) {
                rpcClients.put(mode,
                        mode == TransferMode.XML_RPC ? new XmlRpcClient(config) : new BinRpcClient(config));
            }
        }
    }

    /**
     * Stops the Homematic gateway client.
     */
    protected void stopClients() {
        for (RpcClient rpcClient : rpcClients.values()) {
            rpcClient.dispose();
        }
        rpcClients.clear();
    }

    /**
     * Starts the Homematic RPC server.
     */
    private void startServers() throws IOException {
        for (TransferMode mode : availableInterfaces.values()) {
            if (!rpcServers.containsKey(mode)) {
                RpcServer rpcServer = mode == TransferMode.XML_RPC ? new XmlRpcServer(this, config)
                        : new BinRpcServer(this, config);
                rpcServers.put(mode, rpcServer);
                rpcServer.start();
            }
        }
        for (HmInterface hmInterface : availableInterfaces.keySet()) {
            getRpcClient(hmInterface).init(hmInterface, hmInterface.toString() + "-" + id);
        }
    }

    /**
     * Stops the Homematic RPC server.
     */
    private void stopServers() {
        for (HmInterface hmInterface : availableInterfaces.keySet()) {
            try {
                getRpcClient(hmInterface).release(hmInterface);
            } catch (IOException ex) {
                logger.warn(ex.getMessage(), ex);
            }
        }

        for (TransferMode mode : rpcServers.keySet()) {
            rpcServers.get(mode).shutdown();
        }
        rpcServers.clear();
    }

    /**
     * Starts the connection and event tracker threads.
     */
    private void startWatchdogs() {
        ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(GATEWAY_POOL_NAME);

        if (config.getReconnectInterval() == 0) {
            logger.debug("Starting event tracker for gateway with id '{}'", id);
            eventTrackerThread = scheduler.scheduleWithFixedDelay(new EventTrackerThread(), 1, 1, TimeUnit.MINUTES);
        } else {
            // schedule fixed delay restart
            logger.debug("Starting reconnect tracker for gateway with id '{}'", id);
            reconnectThread = scheduler.scheduleWithFixedDelay(new ReconnectThread(), config.getReconnectInterval(),
                    config.getReconnectInterval(), TimeUnit.SECONDS);
        }
        logger.debug("Starting connection tracker for gateway with id '{}'", id);
        connectionTrackerThread = scheduler.scheduleWithFixedDelay(new ConnectionTrackerThread(), 30,
                CONNECTION_TRACKER_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void stopWatchdogs() {
        if (eventTrackerThread != null) {
            eventTrackerThread.cancel(true);
        }
        if (reconnectThread != null) {
            reconnectThread.cancel(true);
        }
        if (connectionTrackerThread != null) {
            connectionTrackerThread.cancel(true);
        }
    }

    /**
     * Returns the default interface to communicate with the Homematic gateway.
     */
    protected HmInterface getDefaultInterface() {
        return HmInterface.RF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpcClient getRpcClient(HmInterface hmInterface) throws IOException {
        RpcClient rpcClient = rpcClients.get(availableInterfaces.get(hmInterface));
        if (rpcClient == null) {
            throw new IOException("RPC client for interface " + hmInterface + " not available");
        }
        return rpcClient;
    }

    /**
     * Loads all gateway variables into the given device.
     */
    protected abstract void loadVariables(HmChannel channel) throws IOException;

    /**
     * Loads all gateway scripts into the given device.
     */
    protected abstract void loadScripts(HmChannel channel) throws IOException;

    /**
     * Loads all names of the devices.
     */
    protected abstract void loadDeviceNames(Collection<HmDevice> devices) throws IOException;

    /**
     * Sets a variable on the Homematic gateway.
     */
    protected abstract void setVariable(HmDatapoint dp, Object value) throws IOException;

    /**
     * Execute a script on the Homematic gateway.
     */
    protected abstract void executeScript(HmDatapoint dp) throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public HmDatapoint getDatapoint(HmDatapointInfo dpInfo) throws HomematicClientException {
        HmDevice device = getDevice(dpInfo.getAddress());
        HmChannel channel = device.getChannel(dpInfo.getChannel());
        if (channel == null) {
            throw new HomematicClientException(String.format("Channel %s in device '%s' not found on gateway '%s'",
                    dpInfo.getChannel(), dpInfo.getAddress(), id));
        }
        HmDatapoint dp = channel.getDatapoint(dpInfo);
        if (dp == null) {
            throw new HomematicClientException(String.format("Datapoint '%s' not found on gateway '%s'", dpInfo, id));
        }
        return dp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HmDevice getDevice(String address) throws HomematicClientException {
        HmDevice device = devices.get(address);
        if (device == null) {
            throw new HomematicClientException(
                    String.format("Device with address '%s' not found on gateway '%s'", address, id));
        }
        return device;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelLoadAllDeviceMetadata() {
        cancelLoadAllMetadata = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAllDeviceMetadata() throws IOException {
        cancelLoadAllMetadata = false;
        // load all device descriptions
        List<HmDevice> deviceDescriptions = getDeviceDescriptions();

        // loading datapoints for all channels
        Set<String> loadedDevices = new HashSet<String>();
        Map<String, Collection<HmDatapoint>> datapointsByChannelIdCache = new HashMap<String, Collection<HmDatapoint>>();
        for (HmDevice device : deviceDescriptions) {
            if (!cancelLoadAllMetadata) {
                try {
                    logger.trace("Loading metadata for device '{}' of type '{}'", device.getAddress(),
                            device.getType());
                    if (device.isGatewayExtras()) {
                        loadChannelValues(device.getChannel(HmChannel.CHANNEL_NUMBER_VARIABLE));
                        loadChannelValues(device.getChannel(HmChannel.CHANNEL_NUMBER_SCRIPT));
                    } else {
                        for (HmChannel channel : device.getChannels()) {
                            logger.trace("  Loading channel {}", channel);
                            // speed up metadata generation a little bit for equal channels in the gateway devices
                            if ((DEVICE_TYPE_VIRTUAL.equals(device.getType())
                                    || DEVICE_TYPE_VIRTUAL_WIRED.equals(device.getType())) && channel.getNumber() > 1) {
                                HmChannel previousChannel = device.getChannel(channel.getNumber() - 1);
                                cloneAllDatapointsIntoChannel(channel, previousChannel.getDatapoints().values());
                            } else {
                                String channelId = String.format("%s:%s:%s", channel.getDevice().getType(),
                                        channel.getDevice().getFirmware(), channel.getNumber());
                                Collection<HmDatapoint> cachedDatapoints = datapointsByChannelIdCache.get(channelId);
                                if (cachedDatapoints != null) {
                                    // clone all datapoints
                                    cloneAllDatapointsIntoChannel(channel, cachedDatapoints);
                                } else {
                                    logger.trace("    Loading datapoints into channel {}", channel);
                                    addChannelDatapoints(channel, HmParamsetType.MASTER);
                                    addChannelDatapoints(channel, HmParamsetType.VALUES);
                                    datapointsByChannelIdCache.put(channelId, channel.getDatapoints().values());
                                }
                            }
                        }
                    }
                    prepareDevice(device);
                    loadedDevices.add(device.getAddress());
                    eventListener.onDeviceLoaded(device);
                } catch (IOException ex) {
                    logger.warn("Can't load device with address '{}' from gateway '{}': {}", device.getAddress(), id,
                            ex.getMessage());
                }
            }
        }
        if (!cancelLoadAllMetadata) {
            devices.keySet().retainAll(loadedDevices);
        }
    }

    /**
     * Loads all datapoints from the gateway.
     */
    protected void addChannelDatapoints(HmChannel channel, HmParamsetType paramsetType) throws IOException {
        try {
            getRpcClient(channel.getDevice().getHmInterface()).addChannelDatapoints(channel, paramsetType);
        } catch (UnknownParameterSetException ex) {
            logger.info(
                    "Can not load metadata for device: {}, channel: {}, paramset: {}, maybe there are no channels available",
                    channel.getDevice().getAddress(), channel.getNumber(), paramsetType);
        }
    }

    /**
     * Loads all device descriptions from the gateway.
     */
    private List<HmDevice> getDeviceDescriptions() throws IOException {
        List<HmDevice> deviceDescriptions = new ArrayList<HmDevice>();
        for (HmInterface hmInterface : availableInterfaces.keySet()) {
            deviceDescriptions.addAll(getRpcClient(hmInterface).listDevices(hmInterface));
        }
        deviceDescriptions.add(createGatewayDevice());
        loadDeviceNames(deviceDescriptions);
        return deviceDescriptions;
    }

    /**
     * Clones all datapoints into the given channel.
     */
    private void cloneAllDatapointsIntoChannel(HmChannel channel, Collection<HmDatapoint> datapoints) {
        logger.trace("    Cloning {} datapoints into channel {}", datapoints.size(), channel);
        for (HmDatapoint dp : datapoints) {
            if (!dp.isVirtual()) {
                HmDatapoint clonedDp = dp.clone();
                clonedDp.setValue(null);
                channel.addDatapoint(clonedDp);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadChannelValues(HmChannel channel) throws IOException {
        if (channel.getDevice().isGatewayExtras()) {
            if (channel.getNumber() != HmChannel.CHANNEL_NUMBER_EXTRAS) {
                Map<HmDatapointInfo, HmDatapoint> datapoints = channel.getDatapoints();
                datapoints.clear();

                if (channel.getNumber() == HmChannel.CHANNEL_NUMBER_VARIABLE) {
                    loadVariables(channel);
                    logger.debug("Loaded {} gateway variable(s)", datapoints.size());
                } else if (channel.getNumber() == HmChannel.CHANNEL_NUMBER_SCRIPT) {
                    loadScripts(channel);
                    logger.debug("Loaded {} gateway script(s)", datapoints.size());
                }
            }
        } else {
            logger.debug("Loading values for channel {} of device '{}'", channel, channel.getDevice().getAddress());
            setChannelDatapointValues(channel, HmParamsetType.MASTER);
            setChannelDatapointValues(channel, HmParamsetType.VALUES);
        }
        channel.setInitialized(true);
    }

    /**
     * Sets all datapoint values for the given channel.
     */
    protected void setChannelDatapointValues(HmChannel channel, HmParamsetType paramsetType) throws IOException {
        try {
            getRpcClient(channel.getDevice().getHmInterface()).setChannelDatapointValues(channel, paramsetType);
        } catch (UnknownParameterSetException ex) {
            logger.info(
                    "Can not load values for device: {}, channel: {}, paramset: {}, maybe there are no values available",
                    channel.getDevice().getAddress(), channel.getNumber(), paramsetType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadRssiValues() throws IOException {
        for (HmInterface hmInterface : availableInterfaces.keySet()) {
            if (hmInterface == HmInterface.RF || hmInterface == HmInterface.CUXD) {
                List<HmRssiInfo> rssiInfos = getRpcClient(hmInterface).loadRssiInfo(hmInterface);
                for (HmRssiInfo hmRssiInfo : rssiInfos) {
                    updateRssiInfo(hmRssiInfo.getAddress(), DATAPOINT_NAME_RSSI_DEVICE, hmRssiInfo.getDevice());
                    updateRssiInfo(hmRssiInfo.getAddress(), DATAPOINT_NAME_RSSI_PEER, hmRssiInfo.getPeer());
                }
            }
        }
    }

    private void updateRssiInfo(String address, String datapointName, Integer value) {
        HmDatapointInfo dpInfo = new HmDatapointInfo(address, HmParamsetType.VALUES, 0, datapointName);
        HmChannel channel;
        try {
            channel = getDevice(dpInfo.getAddress()).getChannel(0);
            if (channel != null) {
                eventReceived(dpInfo, value);
            }
        } catch (HomematicClientException e) {
            // ignore
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerDeviceValuesReload(HmDevice device) {
        logger.debug("Triggering values reload for device '{}'", device.getAddress());
        for (HmChannel channel : device.getChannels()) {
            channel.setInitialized(false);
        }
        eventListener.reloadDeviceValues(device);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendDatapointIgnoreVirtual(HmDatapoint dp, HmDatapointConfig dpConfig, Object newValue)
            throws IOException, HomematicClientException {
        sendDatapoint(dp, dpConfig, newValue, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendDatapoint(HmDatapoint dp, HmDatapointConfig dpConfig, Object newValue)
            throws IOException, HomematicClientException {
        sendDatapoint(dp, dpConfig, newValue, false);
    }

    /**
     * Main method for sending datapoints to the gateway. It handles scripts, variables, virtual datapoints, delayed
     * executions and auto disabling.
     */
    private void sendDatapoint(final HmDatapoint dp, final HmDatapointConfig dpConfig, final Object newValue,
            final boolean ignoreVirtualDatapoints) throws IOException, HomematicClientException {
        final HmDatapointInfo dpInfo = new HmDatapointInfo(dp);
        if (dp.isPressDatapoint() || (config.getGatewayInfo().isHomegear() && dp.isVariable())) {
            echoEvents.add(dpInfo);
        }
        if (dp.isReadOnly()) {
            logger.warn("Datapoint is readOnly, it is not published to the gateway with id '{}': '{}'", id, dpInfo);
        } else if (HmValueType.ACTION == dp.getType() && MiscUtils.isFalseValue(newValue)) {
            logger.warn(
                    "Datapoint of type ACTION cannot be set to false, it is not published to the gateway with id '{}': '{}'",
                    id, dpInfo);
        } else {
            final VirtualGateway gateway = this;
            delayedExecutor.start(dpInfo, dpConfig.getDelay(), new DelayedExecuterCallback() {

                @Override
                public void execute() throws IOException, HomematicClientException {
                    VirtualDatapointHandler virtualDatapointHandler = ignoreVirtualDatapoints ? null
                            : getVirtualDatapointHandler(dp, newValue);
                    if (virtualDatapointHandler != null) {
                        logger.debug("Handling virtual datapoint '{}' on gateway with id '{}'", dp.getName(), id);
                        virtualDatapointHandler.handleCommand(gateway, dp, dpConfig, newValue);
                    } else if (dp.isScript()) {
                        if (MiscUtils.isTrueValue(newValue)) {
                            logger.debug("Executing script '{}' on gateway with id '{}'", dp.getInfo(), id);
                            executeScript(dp);
                        }
                    } else if (dp.isVariable()) {
                        logger.debug("Sending variable '{}' with value '{}' to gateway with id '{}'", dp.getInfo(),
                                newValue, id);
                        setVariable(dp, newValue);
                    } else {
                        logger.debug("Sending datapoint '{}' with value '{}' to gateway with id '{}'", dpInfo, newValue,
                                id);
                        getRpcClient(dp.getChannel().getDevice().getHmInterface()).setDatapointValue(dp, newValue);
                    }
                    dp.setValue(newValue);

                    if (MiscUtils.isTrueValue(newValue)
                            && (dp.isPressDatapoint() || dp.isScript() || dp.isActionType())) {
                        disableDatapoint(dp, DEFAULT_DISABLE_DELAY);
                    }
                }
            });
        }
    }

    /**
     * Returns a VirtualDatapointHandler for the given datapoint if available.
     */
    private VirtualDatapointHandler getVirtualDatapointHandler(HmDatapoint dp, Object value) {
        for (VirtualDatapointHandler vdph : virtualDatapointHandlers) {
            if (vdph.canHandleCommand(dp, value)) {
                return vdph;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventReceived(HmDatapointInfo dpInfo, Object newValue) {
        String className = newValue == null ? "Unknown" : newValue.getClass().getSimpleName();
        logger.debug("Received new ({}) value '{}' for '{}' from gateway with id '{}'", className, newValue, dpInfo,
                id);
        lastEventTime = System.currentTimeMillis();

        if (echoEvents.remove(dpInfo)) {
            logger.debug("Echo event detected, ignoring '{}'", dpInfo);
        } else {
            try {
                HmDatapoint dp = getDatapoint(dpInfo);
                dp.setValue(newValue);

                eventListener.onStateUpdated(dp);
                if (dp.isPressDatapoint() && MiscUtils.isTrueValue(dp.getValue())) {
                    disableDatapoint(dp, DEFAULT_DISABLE_DELAY);
                }
                for (VirtualDatapointHandler vdph : virtualDatapointHandlers) {
                    if (vdph.canHandleEvent(dp)) {
                        vdph.handleEvent(this, dp);
                        eventListener.onStateUpdated(vdph.getVirtualDatapoint(dp.getChannel()));
                    }
                }
            } catch (HomematicClientException ex) {
                // ignore datapoint not found
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void newDevices(List<String> adresses) {
        if (initialized) {
            for (String address : adresses) {
                try {
                    logger.debug("New device '{}' detected on gateway with id '{}'", address, id);
                    List<HmDevice> deviceDescriptions = getDeviceDescriptions();
                    for (HmDevice device : deviceDescriptions) {
                        if (device.getAddress().equals(address)) {
                            for (HmChannel channel : device.getChannels()) {
                                addChannelDatapoints(channel, HmParamsetType.MASTER);
                                addChannelDatapoints(channel, HmParamsetType.VALUES);
                            }
                            prepareDevice(device);
                            eventListener.onNewDevice(device);
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDevices(List<String> addresses) {
        if (initialized) {
            for (String address : addresses) {
                logger.debug("Device '{}' removed from gateway with id '{}'", address, id);
                HmDevice device = devices.remove(address);
                if (device != null) {
                    eventListener.onDeviceDeleted(device);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HomematicGatewayListener getEventListener() {
        return eventListener;
    }

    /**
     * Creates a virtual device for handling variables, scripts and other special gateway functions.
     */
    private HmDevice createGatewayDevice() {
        HmDevice device = new HmDevice();
        device.setAddress(HmDevice.ADDRESS_GATEWAY_EXTRAS);
        device.setHmInterface(getDefaultInterface());
        device.setGatewayId(config.getGatewayInfo().getId());
        device.setType(String.format("%s-%s", HmDevice.TYPE_GATEWAY_EXTRAS, StringUtils.upperCase(id)));
        device.setName(HmDevice.TYPE_GATEWAY_EXTRAS);

        HmChannel channel = new HmChannel();
        channel.setNumber(HmChannel.CHANNEL_NUMBER_EXTRAS);
        channel.setType(HmChannel.TYPE_GATEWAY_EXTRAS);
        device.addChannel(channel);

        channel = new HmChannel();
        channel.setNumber(HmChannel.CHANNEL_NUMBER_VARIABLE);
        channel.setType(HmChannel.TYPE_GATEWAY_VARIABLE);
        device.addChannel(channel);

        channel = new HmChannel();
        channel.setNumber(HmChannel.CHANNEL_NUMBER_SCRIPT);
        channel.setType(HmChannel.TYPE_GATEWAY_SCRIPT);
        device.addChannel(channel);

        return device;
    }

    /**
     * Adds virtual datapoints to the device.
     */
    private void prepareDevice(HmDevice device) {
        for (VirtualDatapointHandler vdph : virtualDatapointHandlers) {
            vdph.initialize(device);

        }
        devices.put(device.getAddress(), device);
        logger.debug("Loaded device '{}' ({}) with {} datapoints", device.getAddress(), device.getType(),
                device.getDatapointCount());

        if (logger.isTraceEnabled()) {
            logger.trace("{}", device);
            for (HmChannel channel : device.getChannels()) {
                logger.trace("  {}", channel);
                for (HmDatapoint dp : channel.getDatapoints().values()) {
                    logger.trace("    {}", dp);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disableDatapoint(final HmDatapoint dp, double delay) {
        try {
            delayedExecutor.start(new HmDatapointInfo(dp), delay, new DelayedExecuterCallback() {

                @Override
                public void execute() throws IOException {
                    if (MiscUtils.isTrueValue(dp.getValue())) {
                        dp.setValue(Boolean.FALSE);
                        eventListener.onStateUpdated(dp);
                    } else if (dp.getType() == HmValueType.ENUM && dp.getValue() != null && !dp.getValue().equals(0)) {
                        dp.setValue(dp.getMinValue());
                        eventListener.onStateUpdated(dp);
                    }
                }
            });
        } catch (IOException | HomematicClientException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Thread which validates the events from the gateway and restarts the RPC server if no event receives within a
     * configurable time.
     */
    private class EventTrackerThread implements Runnable {

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            int timeSinceLastEvent = (int) ((System.currentTimeMillis() - lastEventTime) / 1000);
            if (timeSinceLastEvent >= config.getAliveInterval()) {
                logger.info("No event since {} seconds from gateway '{}', restarting RPC server", timeSinceLastEvent,
                        id);
                try {
                    stopServers();
                    startServers();
                    eventListener.onServerRestart();
                } catch (IOException ex) {
                    logger.warn(ex.getMessage());
                    logger.trace(ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Thread which validates the connection to the gateway and restarts the RPC client if necessary.
     */
    private class ConnectionTrackerThread implements Runnable {
        private boolean connectionLost = false;

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                validateConnection();
                if (connectionLost) {
                    connectionLost = false;
                    logger.info("Connection resumed on gateway '{}'", id);
                    startClients();
                    eventListener.onConnectionResumed();
                }
            } catch (IOException ex) {
                if (!connectionLost) {
                    connectionLost = true;
                    logger.warn("Connection lost on gateway '{}'", id);
                    stopClients();
                    eventListener.onConnectionLost();
                }
                // temporary disable EventTrackerThread
                lastEventTime = System.currentTimeMillis();
            }
        }

        /**
         * Validates the connection to the gateway, uses a new RPC client if connection is lost.
         */
        private void validateConnection() throws IOException {
            try {
                // normal validation
                getRpcClient(getDefaultInterface()).validateConnection(getDefaultInterface());
            } catch (IOException ex) {
                // connection lost validation
                RpcClient rpcClient = null;
                try {
                    if (config.getGatewayInfo().isHomegear() || config.getGatewayInfo().isCCU()) {
                        rpcClient = new BinRpcClient(config);
                    } else {
                        rpcClient = new XmlRpcClient(config);
                    }
                    rpcClient.validateConnection(getDefaultInterface());
                } finally {
                    if (rpcClient != null) {
                        rpcClient.dispose();
                    }
                }
            }
        }
    }

    /**
     * Threads which restarts the RPC server.
     */
    private class ReconnectThread implements Runnable {

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                stopServers();
                startServers();
            } catch (IOException ex) {
                logger.debug(ex.getMessage(), ex);
            }
        }
    }
}
