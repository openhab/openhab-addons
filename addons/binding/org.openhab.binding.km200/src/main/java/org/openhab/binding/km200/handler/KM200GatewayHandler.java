/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.handler;

import static org.openhab.binding.km200.KM200BindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.km200.KM200ThingType;
import org.openhab.binding.km200.internal.KM200Comm;
import org.openhab.binding.km200.internal.KM200CommObject;
import org.openhab.binding.km200.internal.KM200Device;
import org.openhab.binding.km200.internal.KM200Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link KM200GatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200GatewayHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(KM200GatewayHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_KMDEVICE);

    private final Map<Channel, byte[]> sendMap = Collections.synchronizedMap(new LinkedHashMap<Channel, byte[]>());

    private List<KM200GatewayStatusListener> listeners = new CopyOnWriteArrayList<KM200GatewayStatusListener>();

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private GetKM200Runnable receivingRunnable;
    private ScheduledFuture<?> pollingJob;
    private final KM200Device remoteDevice;
    private final KM200Comm<KM200Device> deviceCommunicator;
    private int readDelay;
    private int refreshInterval;
    private final JsonParser jsonParser = new JsonParser();

    public KM200GatewayHandler(Bridge bridge) {
        super(bridge);
        refreshInterval = 30;
        readDelay = 100;
        updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_PENDING);
        remoteDevice = new KM200Device();
        deviceCommunicator = new KM200Comm<KM200Device>(getDevice());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = getThing().getChannel(channelUID.getId());
        if (command instanceof DateTimeType || command instanceof DecimalType || command instanceof StringType) {
            prepareMessage(thing, channel, command);
        } else {
            logger.warn("Unsupported Command: {} Class: {}", command.toFullString(), command.getClass());
        }
    }

    @Override
    public void initialize() {
        if (!getDevice().getInited() && !isInitialized()) {
            logger.info("Update KM50/100/200 gateway configuration, it takes a minute....");
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING);
            getConfiguration();

            if (getDevice().isConfigured()) {
                if (!checkConfiguration()) {
                    return;
                }
                /* configuration and communication seems to be ok */
                readCapabilities();
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
                logger.info("The KM50/100/200 gateway configuration is not complete");
                return;
            }

            if (getDevice() != null) {
                logger.debug("Starting send executor");
                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread sThread, Throwable e) {
                        logger.debug(sThread.getName() + " throws exception: " + e);
                    }
                });
                SendKM200Runnable sendRunnable = new SendKM200Runnable(sendMap, this, getDevice(), deviceCommunicator);
                executor.scheduleWithFixedDelay(sendRunnable, 60, 30, TimeUnit.SECONDS);
            }
        }
        receivingRunnable = new GetKM200Runnable(this, getDevice(), deviceCommunicator);
        logger.debug("starting runnable.");
        pollingJob = scheduler.scheduleWithFixedDelay(receivingRunnable, 30, refreshInterval, TimeUnit.SECONDS);

    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        logger.debug("Shutdown send executor");
        executor.shutdown();

        if (getDevice() != null) {
            synchronized (getDevice()) {
                getDevice().setInited(false);
                getDevice().setIP4Address("");
                getDevice().setCryptKeyPriv("");
                getDevice().setMD5Salt("");
                getDevice().setGatewayPassword("");
                getDevice().setPrivatePassword("");
                getDevice().serviceTreeMap.clear();
            }
        }
    }

    @Override
    public void handleRemoval() {
        for (Thing actThing : getThing().getThings()) {
            actThing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ""));
        }
        this.updateStatus(ThingStatus.REMOVED);
    }

    /**
     * Gets bridges configuration
     *
     */
    private void getConfiguration() {
        Configuration configuration = getConfig();
        for (String key : configuration.keySet()) {
            logger.debug("initialize Key: {} Value: {}", key, configuration.get(key));
            switch (key) {
                case "ip4Address":
                    String ip = (String) configuration.get("ip4Address");
                    if (StringUtils.isNotBlank(ip)) {
                        try {
                            InetAddresses.forString(ip);
                        } catch (IllegalArgumentException e) {
                            logger.debug("IP4_address is not valid!: {}", ip);
                        }
                        getDevice().setIP4Address(ip);
                    } else {
                        logger.debug("No ip4_address configured!");
                    }
                    break;
                case "privateKey":
                    String privateKey = (String) configuration.get("privateKey");
                    if (StringUtils.isNotBlank(privateKey)) {
                        getDevice().setCryptKeyPriv(privateKey);
                    }
                    break;
                case "md5Salt":
                    String md5Salt = (String) configuration.get("md5Salt");
                    if (StringUtils.isNotBlank(md5Salt)) {
                        getDevice().setMD5Salt(md5Salt);
                    }
                    break;
                case "gatewayPassword":
                    String gatewayPassword = (String) configuration.get("gatewayPassword");
                    if (StringUtils.isNotBlank(gatewayPassword)) {
                        getDevice().setGatewayPassword(gatewayPassword);
                    }
                    break;
                case "privatePassword":
                    String privatePassword = (String) configuration.get("privatePassword");
                    if (StringUtils.isNotBlank(privatePassword)) {
                        getDevice().setPrivatePassword(privatePassword);
                    }
                    break;
                case "refreshInterval":
                    refreshInterval = ((BigDecimal) configuration.get("refreshInterval")).intValue();
                    logger.debug("Set refresh interval to: {} seconds.", refreshInterval);
                    break;
                case "readDelay":
                    readDelay = ((BigDecimal) configuration.get("readDelay")).intValue();
                    logger.debug("Set read delay to: {} seconds.", readDelay);
                    break;
                case "maxNbrRepeats":
                    Integer maxNbrRepeats = ((BigDecimal) configuration.get("maxNbrRepeats")).intValue();
                    logger.debug("Set max. number of repeats to: {} seconds.", maxNbrRepeats);
                    deviceCommunicator.setMaxNbrRepeats(maxNbrRepeats);
                    break;
            }
        }
    }

    /**
     * Checks bridges configuration
     *
     */
    private boolean checkConfiguration() {
        /* Get HTTP Data from device */
        byte[] recData = deviceCommunicator.getDataFromService("/gateway/DateTime");
        if (recData == null) {
            logger.debug("Communication is not possible!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No communication possible with gateway");
            return false;
        }
        if (recData.length == 0) {
            logger.debug("No reply from KM200!");
            updateStatus(ThingStatus.OFFLINE);
            return false;
        }
        logger.info("Received data..");
        /* Decrypt the message */
        String decodedData = deviceCommunicator.decodeMessage(recData);
        if (decodedData == null) {
            logger.debug("Decoding of the KM200 message is not possible!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Decoding of the KM200 message is not possible!");
            return false;
        }

        if ("SERVICE NOT AVAILABLE".equals(decodedData)) {
            logger.debug("/gateway/DateTime: SERVICE NOT AVAILABLE");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No communication possible with gateway");
            return false;
        } else {
            logger.info("Test of the communication to the gateway was successful..");
        }
        /* Testing the received data, is decryption working? */
        try {
            JsonObject nodeRoot = (JsonObject) jsonParser.parse(decodedData);
            nodeRoot.get("type").getAsString();
            nodeRoot.get("id").getAsString();
        } catch (JsonParseException e) {
            logger.debug("The data is not readable, check the key and password configuration! {} {}", e.getMessage(),
                    decodedData);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Wrong gateway configuration");
            return false;
        }
        return true;
    }

    /**
     * Reads the devices capabilities and sets the data structures
     *
     */
    private void readCapabilities() {
        logger.debug("Init services..");
        /* Checking of the device specific services and creating of a service list */
        for (KM200ThingType thing : KM200ThingType.values()) {
            String rootPath = thing.getRootPath();
            logger.debug("Rootpath: {}", rootPath);
            if (!rootPath.isEmpty() && (rootPath.indexOf("/", 0) == rootPath.lastIndexOf("/", rootPath.length() - 1))) {
                // Init the main services only
                deviceCommunicator.initObjects(thing.getRootPath(), null);
            }
        }
        /* Now init the virtual services */
        logger.debug("init Virtual Objects");
        deviceCommunicator.initVirtualObjects();
        /* Output all available services in the log file */
        /* Now init the virtual services */
        logger.debug("list All Services");
        getDevice().listAllServices();
        logger.debug("... Update of the KM200 Binding configuration completed");
        updateBridgeProperties();
        getDevice().setInited(true);
    }

    /**
     * Adds a GatewayConnectedListener
     *
     * @param listener
     */
    public void addGatewayStatusListener(KM200GatewayStatusListener listener) {
        listeners.add(listener);
        listener.gatewayStatusChanged(getThing().getStatus());
    }

    /**
     * Removes a GatewayConnectedListener
     *
     * @param listener
     */
    public void removeHubStatusListener(KM200GatewayStatusListener listener) {
        listeners.remove(listener);
    }

    /**
     * Refreshes a channel
     *
     * @param channel
     */
    public void refreshChannel(Channel channel) {
        GetSingleKM200Runnable runnable = new GetSingleKM200Runnable(this, getDevice(), deviceCommunicator, channel);
        logger.debug("starting single runnable.");
        scheduler.submit(runnable);
    }

    /**
     * Updates bridges properties
     *
     */
    private void updateBridgeProperties() {
        List<String> propertyServices = new ArrayList<String>();
        propertyServices.add(KM200ThingType.GATEWAY.getRootPath());
        propertyServices.add(KM200ThingType.SYSTEM.getRootPath());
        Map<String, String> bridgeProperties = editProperties();

        for (KM200ThingType tType : KM200ThingType.values()) {
            List<String> asProperties = tType.asBridgeProperties();
            String rootPath = tType.getRootPath();
            if (rootPath.isEmpty()) {
                continue;
            }
            logger.debug("Add Property rootPath: {}", rootPath);
            KM200CommObject serObj = getDevice().getServiceObject(rootPath);
            for (String subKey : asProperties) {
                if (serObj.serviceTreeMap.containsKey(subKey)) {
                    String subKeyType = serObj.serviceTreeMap.get(subKey).getServiceType();
                    if (!"stringValue".equals(subKeyType) && !"floatValue".equals(subKeyType)) {
                        continue;
                    }
                    if (bridgeProperties.containsKey(subKey)) {
                        bridgeProperties.remove(subKey);
                    }
                    logger.debug("Add Property: {}  :{}", subKey,
                            serObj.serviceTreeMap.get(subKey).getValue().toString());
                    bridgeProperties.put(subKey, serObj.serviceTreeMap.get(subKey).getValue().toString());
                }
            }
        }
        updateProperties(bridgeProperties);
    }

    /**
     * This function checks whether the service has a replacement parameter
     *
     * @param channel
     * @param device
     */
    public static String checkParameterReplacement(Channel channel, KM200Device device) {
        String service = KM200Utils.translatesNameToPath(channel.getProperties().get("root"));
        if (service.contains(SWITCH_PROGRAM_REPLACEMENT)) {
            String currentService = KM200Utils
                    .translatesNameToPath(channel.getProperties().get(SWITCH_PROGRAM_CURRENT_PATH_NAME));
            if (device.containsService(currentService)) {
                if ("stringValue".equals(device.getServiceObject(currentService).getServiceType())) {
                    String val = (String) device.getServiceObject(currentService).getValue();
                    service = service.replace(SWITCH_PROGRAM_REPLACEMENT, val);
                    return service;
                }
            }
        }
        return service;
    }

    /**
     * This function checks whether the channel has channel parameters
     *
     * @param channel
     */
    public Map<String, String> getChannelConfigurationStrings(Channel channel) {
        Map<String, String> paraNames = new HashMap<String, String>();
        if (channel.getConfiguration().containsKey("on")) {
            paraNames.put("on", channel.getConfiguration().get("on").toString());
            logger.debug("Added ON: {}", channel.getConfiguration().get("on"));
        }

        if (channel.getConfiguration().containsKey("off")) {
            paraNames.put("off", channel.getConfiguration().get("off").toString());
            logger.debug("Added OFF: {}", channel.getConfiguration().get("off"));
        }
        return paraNames;
    }

    /**
     * Prepares a message for sending
     *
     * @param thing
     * @param channel
     * @param command
     */
    public void prepareMessage(Thing thing, Channel channel, Command command) {
        if (getDevice() != null && getDevice().getInited() && channel != null) {
            byte[] sendData = null;
            State state = null;
            String service = checkParameterReplacement(channel, getDevice());

            logger.debug("handleCommand channel: {} service: {}", channel.getLabel(), service);
            sendData = deviceCommunicator.sendProvidersState(service, command, channel.getAcceptedItemType(),
                    getChannelConfigurationStrings(channel));
            synchronized (getDevice()) {
                if (sendData != null) {
                    sendMap.put(channel, sendData);
                } else if (getDevice().containsService(service)
                        && getDevice().getServiceObject(service).getVirtual() == 1) {
                    String parent = getDevice().getServiceObject(service).getParent();
                    for (Thing actThing : getThing().getThings()) {
                        logger.debug("Checking: {}", actThing.getUID().getAsString());
                        for (Channel tmpChannel : actThing.getChannels()) {
                            String actService = checkParameterReplacement(tmpChannel, getDevice());
                            logger.debug("tmpService: {}", actService);
                            String actParent = getDevice().getServiceObject(actService).getParent();
                            if (actParent != null && actParent.equals(parent)) {
                                state = deviceCommunicator.getProvidersState(actService,
                                        tmpChannel.getAcceptedItemType(), getChannelConfigurationStrings(tmpChannel));
                                if (state != null) {
                                    try {
                                        updateState(tmpChannel.getUID(), state);
                                    } catch (IllegalStateException e) {
                                        logger.error("Could not get updated item state, Error: {}", e);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    logger.warn("Service is not availible: {}", service);
                }
            }
        }

    }

    /**
     * Update the children
     *
     * @param gatewayHandler
     * @param remoteDevice
     * @param deviceCommunicator
     * @param parent
     */
    // Every thing has here a handler
    @SuppressWarnings("null")
    private void updateChildren(KM200GatewayHandler gatewayHandler, KM200Device remoteDevice,
            KM200Comm<KM200Device> deviceCommunicator, String parent) {
        synchronized (remoteDevice) {
            if (parent != null) {
                remoteDevice.getServiceObject(parent).setUpdated(false);
            }
            for (Thing actThing : gatewayHandler.getThing().getThings()) {
                for (Channel actChannel : actThing.getChannels()) {
                    logger.debug("Checking: {} Root: {}", actChannel.getUID().getAsString(),
                            actChannel.getProperties().get("root"));
                    if (!((KM200ThingHandler) actThing.getHandler()).checkLinked(actChannel)) {
                        continue;
                    }
                    String tmpService = checkParameterReplacement(actChannel, remoteDevice);
                    if (parent == null || parent.equals(remoteDevice.getServiceObject(tmpService).getParent())) {
                        State state = deviceCommunicator.getProvidersState(tmpService, actChannel.getAcceptedItemType(),
                                getChannelConfigurationStrings(actChannel));
                        if (state != null) {
                            try {
                                gatewayHandler.updateState(actChannel.getUID(), state);
                            } catch (IllegalStateException e) {
                                logger.error("Could not get updated item state, Error: {}", e);
                            }
                        }
                    }
                    try {
                        Thread.sleep(readDelay);
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
        }
    }

    /**
     * Return the device instance.
     *
     * @author Markus Eckhardt
     */
    public KM200Device getDevice() {
        return remoteDevice;
    }

    /**
     * The GetKM200Runnable class get the data from device to all items.
     *
     * @author Markus Eckhardt
     */
    private class GetKM200Runnable implements Runnable {

        private final KM200GatewayHandler gatewayHandler;
        private final KM200Device remoteDevice;
        private final KM200Comm<KM200Device> deviceCommunicator;
        private final Logger logger = LoggerFactory.getLogger(GetKM200Runnable.class);

        public GetKM200Runnable(KM200GatewayHandler gatewayHandler, KM200Device remoteDevice,
                KM200Comm<KM200Device> deviceCommunicator) {
            this.gatewayHandler = gatewayHandler;
            this.remoteDevice = remoteDevice;
            this.deviceCommunicator = deviceCommunicator;
        }

        @Override
        public void run() {
            logger.debug("GetKM200Runnable");
            synchronized (remoteDevice) {
                if (remoteDevice.getInited()) {
                    remoteDevice.resetAllUpdates(remoteDevice.serviceTreeMap);
                    updateChildren(gatewayHandler, remoteDevice, deviceCommunicator, null);
                }
            }
        }
    }

    /**
     * The GetKM200Runnable class get the data from device for one channel.
     *
     * @author Markus Eckhardt
     */
    private class GetSingleKM200Runnable implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(GetSingleKM200Runnable.class);
        private final KM200GatewayHandler gatewayHandler;
        private final KM200Device remoteDevice;
        private final KM200Comm<KM200Device> deviceCommunicator;
        private final Channel channel;

        public GetSingleKM200Runnable(KM200GatewayHandler gatewayHandler, KM200Device remoteDevice,
                KM200Comm<KM200Device> deviceCommunicator, Channel channel) {
            this.gatewayHandler = gatewayHandler;
            this.remoteDevice = remoteDevice;
            this.deviceCommunicator = deviceCommunicator;
            this.channel = channel;
        }

        @Override
        public void run() {
            logger.debug("GetKM200Runnable");
            State state = null;
            synchronized (remoteDevice) {
                if (remoteDevice.getInited()) {
                    logger.debug("Checking: {} Root: {}", channel.getUID().getAsString(),
                            channel.getProperties().get("root"));
                    KM200CommObject object = remoteDevice
                            .getServiceObject(checkParameterReplacement(channel, remoteDevice));
                    if (object.getVirtual() == 1) {
                        String parent = object.getParent();
                        updateChildren(gatewayHandler, remoteDevice, deviceCommunicator, parent);
                    } else {
                        object.setUpdated(false);
                        state = deviceCommunicator.getProvidersState(checkParameterReplacement(channel, remoteDevice),
                                channel.getAcceptedItemType(), getChannelConfigurationStrings(channel));
                        if (state != null) {
                            try {
                                gatewayHandler.updateState(channel.getUID(), state);
                            } catch (IllegalStateException e) {
                                logger.error("Could not get updated item state, Error: {}", e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The sendKM200Thread class sends the data to the device.
     *
     * @author Markus Eckhardt
     */
    private class SendKM200Runnable implements Runnable {

        private final Map<Channel, byte[]> sendMap;
        private final KM200GatewayHandler gatewayHandler;
        private final KM200Device remoteDevice;
        private final KM200Comm<KM200Device> deviceCommunicator;

        public SendKM200Runnable(Map<Channel, byte[]> sendMap, KM200GatewayHandler gatewayHandler,
                KM200Device remoteDevice, KM200Comm<KM200Device> deviceCommunicator) {
            this.sendMap = sendMap;
            this.gatewayHandler = gatewayHandler;
            this.remoteDevice = remoteDevice;
            this.deviceCommunicator = deviceCommunicator;
        }

        @Override
        public void run() {
            logger.debug("Send-Executor started");
            Map.Entry<Channel, byte[]> nextEntry = null;
            /* Check whether a new entry is availible, if yes then take and remove it */
            synchronized (sendMap) {
                Iterator<Entry<Channel, byte[]>> i = sendMap.entrySet().iterator();
                if (i.hasNext()) {
                    logger.debug("Send-Thread, new entry");
                    nextEntry = i.next();
                    i.remove();
                }
            }

            if (nextEntry != null) {
                /* Now send the data to the device */
                Channel channel = nextEntry.getKey();
                byte[] encData = nextEntry.getValue();

                String service = checkParameterReplacement(channel, remoteDevice);
                KM200CommObject object = remoteDevice.getServiceObject(service);

                logger.debug("Sending: {}", service);

                if (object.getVirtual() == 1) {
                    deviceCommunicator.sendDataToService(object.getParent(), encData);
                } else {
                    deviceCommunicator.sendDataToService(service, encData);
                }

                logger.debug("Data sended, reset und updated providers");
                /* Now update the set values and for all virtual values depending on same parent */
                if (object.getVirtual() == 1) {
                    String parent = object.getParent();
                    updateChildren(gatewayHandler, remoteDevice, deviceCommunicator, parent);
                } else {
                    object.setUpdated(false);
                    org.eclipse.smarthome.core.types.State state = deviceCommunicator.getProvidersState(service,
                            channel.getAcceptedItemType(), getChannelConfigurationStrings(channel));
                    if (state != null) {
                        try {
                            gatewayHandler.updateState(channel.getUID(), state);
                        } catch (IllegalStateException e) {
                            logger.error("Could not get updated item state, Error: {}", e);
                        }
                    }
                    for (Thing actThing : gatewayHandler.getThing().getThings()) {
                        for (Channel actChannel : actThing.getChannels()) {
                            ThingHandler thingHandler = actThing.getHandler();
                            if (thingHandler != null && !((KM200ThingHandler) thingHandler).checkLinked(actChannel)) {
                                continue;
                            }
                            String pathName = actChannel.getProperties().get(SWITCH_PROGRAM_CURRENT_PATH_NAME);
                            String actService = checkParameterReplacement(actChannel, remoteDevice);
                            if (pathName.equals(service) || pathName.equals(service)) {
                                state = deviceCommunicator.getProvidersState(actService,
                                        actChannel.getAcceptedItemType(), getChannelConfigurationStrings(actChannel));
                                if (state != null) {
                                    try {
                                        gatewayHandler.updateState(actChannel.getUID(), state);
                                    } catch (IllegalStateException e) {
                                        logger.error("Could not get updated item state, Error: {}", e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
