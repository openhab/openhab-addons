/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.handler;

import static org.openhab.binding.km200.KM200BindingConstants.THING_TYPE_KMDEVICE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.km200.KM200ThingType;
import org.openhab.binding.km200.internal.KM200Device;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.openhab.binding.km200.internal.KM200Utils;
import org.openhab.binding.km200.internal.handler.KM200DataHandler;
import org.openhab.binding.km200.internal.handler.KM200ServiceHandler;
import org.openhab.binding.km200.internal.handler.KM200VirtualServiceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;


/**
 * The {@link KM200GatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200GatewayHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(KM200GatewayHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_KMDEVICE);

    private final Map<Channel, JsonObject> sendMap = Collections
            .synchronizedMap(new LinkedHashMap<Channel, JsonObject>());

    private List<KM200GatewayStatusListener> listeners = new CopyOnWriteArrayList<KM200GatewayStatusListener>();

    /**
     * shared instance of HTTP client for (a)synchronous calls
     */
    private HttpClient httpClient;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final KM200Device remoteDevice;
    private final KM200DataHandler dataHandler;
    private int readDelay;
    private int refreshInterval;

    public KM200GatewayHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        refreshInterval = 120;
        readDelay = 100;
        updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_PENDING);
        remoteDevice = new KM200Device(httpClient);
        dataHandler = new KM200DataHandler(remoteDevice);
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
            logger.debug("Update KM50/100/200 gateway configuration, it takes a minute....");
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
                logger.debug("Starting send and receive executor");
                SendKM200Runnable sendRunnable = new SendKM200Runnable(sendMap, getDevice());
                GetKM200Runnable receivingRunnable = new GetKM200Runnable(sendMap, this, getDevice());
                executor.scheduleWithFixedDelay(receivingRunnable, 30, refreshInterval, TimeUnit.SECONDS);
                executor.scheduleWithFixedDelay(sendRunnable, 60, refreshInterval * 2, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
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
                    remoteDevice.setMaxNbrRepeats(maxNbrRepeats);
                    break;
            }
        }
    }

    /**
     * Checks bridges configuration
     */
    private boolean checkConfiguration() {
        /* Get HTTP Data from device */
        JsonObject nodeRoot = remoteDevice.getServiceNode("/gateway/DateTime");
        if (nodeRoot == null || nodeRoot.isJsonNull()) {
            logger.debug("Communication is not possible!");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No communication possible with gateway");
            return false;
        }
        logger.debug("Test of the communication to the gateway was successful..");

        /* Testing the received data, is decryption working? */
        try {
            nodeRoot.get("type").getAsString();
            nodeRoot.get("id").getAsString();
        } catch (JsonParseException e) {
            logger.debug("The data is not readable, check the key and password configuration! {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Wrong gateway configuration");
            return false;
        }
        return true;
    }

    /**
     * Reads the devices capabilities and sets the data structures
     */
    private void readCapabilities() {
        logger.debug("read Capabilities..");
        KM200VirtualServiceHandler virtualServiceHandler;
        /* Checking of the device specific services and creating of a service list */
        for (KM200ThingType thing : KM200ThingType.values()) {
            String rootPath = thing.getRootPath();
            logger.debug("Rootpath: {}", rootPath);
            if (!rootPath.isEmpty() && (rootPath.indexOf("/", 0) == rootPath.lastIndexOf("/", rootPath.length() - 1))) {
                if (remoteDevice.getBlacklistMap().contains(thing.getRootPath())) {
                    logger.debug("Service on blacklist: {}", thing.getRootPath());
                    return;
                }
                KM200ServiceHandler serviceHandler = new KM200ServiceHandler(thing.getRootPath(), null, remoteDevice);
                serviceHandler.initObject();
            }
        }
        /* Now init the virtual services */
        logger.debug("init Virtual Objects");
        virtualServiceHandler = new KM200VirtualServiceHandler(remoteDevice);
        virtualServiceHandler.initVirtualObjects();
        /* Output all available services in the log file */
        logger.debug("list All Services");
        getDevice().listAllServices();
        logger.debug("... Update of the KM200 Binding configuration completed");
        updateBridgeProperties();
        getDevice().setInited(true);
    }

    /**
     * Adds a GatewayConnectedListener
     */
    public void addGatewayStatusListener(KM200GatewayStatusListener listener) {
        listeners.add(listener);
        listener.gatewayStatusChanged(getThing().getStatus());
    }

    /**
     * Removes a GatewayConnectedListener
     */
    public void removeHubStatusListener(KM200GatewayStatusListener listener) {
        listeners.remove(listener);
    }

    /**
     * Refreshes a channel
     */
    public void refreshChannel(Channel channel) {
        GetSingleKM200Runnable runnable = new GetSingleKM200Runnable(sendMap, this, getDevice(), channel);
        logger.debug("starting single runnable.");
        scheduler.submit(runnable);
    }

    /**
     * Updates bridges properties
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
            KM200ServiceObject serObj = getDevice().getServiceObject(rootPath);
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
     * Prepares a message for sending
     */
    public void prepareMessage(Thing thing, Channel channel, Command command) {
        if (getDevice() != null && getDevice().getInited() && channel != null) {
            JsonObject newObject = null;
            State state = null;
            String service = KM200Utils.checkParameterReplacement(channel, getDevice());

            logger.debug("handleCommand channel: {} service: {}", channel.getLabel(), service);
            newObject = dataHandler.sendProvidersState(service, command, channel.getAcceptedItemType(),
                    KM200Utils.getChannelConfigurationStrings(channel));
            synchronized (getDevice()) {
                if (newObject != null) {
                    sendMap.put(channel, newObject);
                } else if (getDevice().containsService(service)
                        && getDevice().getServiceObject(service).getVirtual() == 1) {
                    String parent = getDevice().getServiceObject(service).getParent();
                    for (Thing actThing : getThing().getThings()) {
                        logger.debug("Checking: {}", actThing.getUID().getAsString());
                        for (Channel tmpChannel : actThing.getChannels()) {
                            String actService = KM200Utils.checkParameterReplacement(tmpChannel, getDevice());
                            logger.debug("tmpService: {}", actService);
                            String actParent = getDevice().getServiceObject(actService).getParent();
                            if (actParent != null && actParent.equals(parent)) {
                                state = dataHandler.getProvidersState(actService, tmpChannel.getAcceptedItemType(),
                                        KM200Utils.getChannelConfigurationStrings(tmpChannel));
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
     */
    // Every thing has here a handler
    private void updateChildren(Map<Channel, JsonObject> sendMap, KM200GatewayHandler gatewayHandler,
            KM200Device remoteDevice, String parent) {
        State state;
        synchronized (remoteDevice) {
            if (parent != null) {
                remoteDevice.getServiceObject(parent).setUpdated(false);
            }
            for (Thing actThing : gatewayHandler.getThing().getThings()) {
                for (Channel actChannel : actThing.getChannels()) {
                    logger.debug("Checking: {} Root: {}", actChannel.getUID().getAsString(),
                            actChannel.getProperties().get("root"));
                    KM200ThingHandler actHandler = (KM200ThingHandler) actThing.getHandler();
                    if (actHandler != null) {
                        if (!actHandler.checkLinked(actChannel)) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    String tmpService = KM200Utils.checkParameterReplacement(actChannel, remoteDevice);
                    if (parent == null || parent.equals(remoteDevice.getServiceObject(tmpService).getParent())) {
                        synchronized (sendMap) {
                            if (sendMap.containsKey(actChannel)) {
                                state = dataHandler.parseJSONData(sendMap.get(actChannel),
                                        remoteDevice.getServiceObject(tmpService).getServiceType(), tmpService,
                                        actChannel.getAcceptedItemType(),
                                        KM200Utils.getChannelConfigurationStrings(actChannel));
                            } else {
                                state = dataHandler.getProvidersState(tmpService, actChannel.getAcceptedItemType(),
                                        KM200Utils.getChannelConfigurationStrings(actChannel));
                            }
                        }
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
     */
    public KM200Device getDevice() {
        return remoteDevice;
    }

    /**
     * The GetKM200Runnable class get the data from device to all items.
     */
    private class GetKM200Runnable implements Runnable {

        private final KM200GatewayHandler gatewayHandler;
        private final KM200Device remoteDevice;
        private final Logger logger = LoggerFactory.getLogger(GetKM200Runnable.class);
        private final Map<Channel, JsonObject> sendMap;

        public GetKM200Runnable(Map<Channel, JsonObject> sendMap, KM200GatewayHandler gatewayHandler,
                KM200Device remoteDevice) {
            this.sendMap = sendMap;
            this.gatewayHandler = gatewayHandler;
            this.remoteDevice = remoteDevice;
        }

        @Override
        public void run() {
            logger.debug("GetKM200Runnable");
            synchronized (remoteDevice) {
                if (remoteDevice.getInited()) {
                    remoteDevice.resetAllUpdates(remoteDevice.serviceTreeMap);
                    updateChildren(sendMap, gatewayHandler, remoteDevice, null);
                }
            }
        }
    }

    /**
     * The GetKM200Runnable class get the data from device for one channel.
     */
    private class GetSingleKM200Runnable implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(GetSingleKM200Runnable.class);
        private final KM200GatewayHandler gatewayHandler;
        private final KM200Device remoteDevice;
        private final Channel channel;
        private final Map<Channel, JsonObject> sendMap;

        public GetSingleKM200Runnable(Map<Channel, JsonObject> sendMap, KM200GatewayHandler gatewayHandler,
                KM200Device remoteDevice, Channel channel) {
            this.gatewayHandler = gatewayHandler;
            this.remoteDevice = remoteDevice;
            this.channel = channel;
            this.sendMap = sendMap;
        }

        @Override
        public void run() {
            logger.debug("GetKM200Runnable");
            State state = null;
            synchronized (remoteDevice) {
                synchronized (sendMap) {
                    if (sendMap.containsKey(channel)) {
                        return;
                    }
                }
                if (remoteDevice.getInited()) {
                    logger.debug("Checking: {} Root: {}", channel.getUID().getAsString(),
                            channel.getProperties().get("root"));
                    String service = KM200Utils.checkParameterReplacement(channel, remoteDevice);
                    KM200ServiceObject object = remoteDevice.getServiceObject(service);
                    if (object.getVirtual() == 1) {
                        String parent = object.getParent();
                        updateChildren(sendMap, gatewayHandler, remoteDevice, parent);
                    } else {
                        object.setUpdated(false);
                        synchronized (sendMap) {
                            if (sendMap.containsKey(channel)) {
                                state = dataHandler.parseJSONData(sendMap.get(channel),
                                        remoteDevice.getServiceObject(service).getServiceType(), service,
                                        channel.getAcceptedItemType(),
                                        KM200Utils.getChannelConfigurationStrings(channel));
                            } else {
                                state = dataHandler.getProvidersState(service, channel.getAcceptedItemType(),
                                        KM200Utils.getChannelConfigurationStrings(channel));
                            }
                        }
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
     */
    private class SendKM200Runnable implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(SendKM200Runnable.class);
        private final Map<Channel, JsonObject> newObject;
        private final KM200Device remoteDevice;

        public SendKM200Runnable(Map<Channel, JsonObject> newObject, KM200Device remoteDevice) {
            this.newObject = newObject;
            this.remoteDevice = remoteDevice;
        }

        @Override
        public void run() {
            logger.debug("Send-Executor started");
            Map.Entry<Channel, JsonObject> nextEntry;
            /* Check whether a new entry is availible, if yes then take and remove it */
            do {
                nextEntry = null;
                synchronized (remoteDevice) {
                    synchronized (newObject) {
                        Iterator<Entry<Channel, JsonObject>> i = newObject.entrySet().iterator();
                        if (i.hasNext()) {
                            logger.debug("Send-Thread, new entry");
                            nextEntry = i.next();
                            i.remove();
                        }
                    }
                    if (nextEntry != null) {
                        /* Now send the data to the device */
                        Channel channel = nextEntry.getKey();
                        JsonObject newObject = nextEntry.getValue();

                        String service = KM200Utils.checkParameterReplacement(channel, remoteDevice);
                        KM200ServiceObject object = remoteDevice.getServiceObject(service);

                        logger.debug("Sending: {} to : {}", newObject, service);
                        if (object.getVirtual() == 1) {
                            remoteDevice.setServiceNode(object.getParent(), newObject);
                        } else {
                            remoteDevice.setServiceNode(service, newObject);
                        }
                    }
                }
            } while (nextEntry != null);
        }
    }
}
