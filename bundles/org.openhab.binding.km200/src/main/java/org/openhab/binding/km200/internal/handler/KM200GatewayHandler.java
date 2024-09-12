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
package org.openhab.binding.km200.internal.handler;

import static org.openhab.binding.km200.internal.KM200BindingConstants.*;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
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

import javax.crypto.Cipher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.km200.internal.KM200Device;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.openhab.binding.km200.internal.KM200ThingType;
import org.openhab.binding.km200.internal.KM200Utils;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link KM200GatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
public class KM200GatewayHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(KM200GatewayHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_KMDEVICE);

    private final Map<Channel, JsonObject> sendMap = Collections.synchronizedMap(new LinkedHashMap<>());

    private List<KM200GatewayStatusListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * shared instance of HTTP client for (a)synchronous calls
     */
    private ScheduledExecutorService executor;
    private final KM200Device remoteDevice;
    private final KM200DataHandler dataHandler;
    private int readDelay;
    private int refreshInterval;

    public KM200GatewayHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        refreshInterval = 120;
        readDelay = 100;
        remoteDevice = new KM200Device(httpClient);
        dataHandler = new KM200DataHandler(remoteDevice);
        executor = Executors.newScheduledThreadPool(2, new NamedThreadFactory("org.openhab.binding.km200", true));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = getThing().getChannel(channelUID.getId());
        if (null != channel) {
            if (command instanceof DateTimeType || command instanceof DecimalType || command instanceof StringType) {
                prepareMessage(thing, channel, command);
            } else {
                logger.warn("Unsupported Command: {} Class: {}", command.toFullString(), command.getClass());
            }
        }
    }

    @Override
    public void initialize() {
        try {
            int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES/ECB/NoPadding");
            if (maxKeyLen <= 128) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Java Cryptography Extension (JCE) have to be installed");
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "AES encoding not supported");
            return;
        }
        if (!getDevice().getInited()) {
            logger.info("Update KM50/100/200 gateway configuration, it takes a minute....");
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
                logger.debug("The KM50/100/200 gateway configuration is not complete");
                return;
            }

            SendKM200Runnable sendRunnable = new SendKM200Runnable(sendMap, getDevice());
            GetKM200Runnable receivingRunnable = new GetKM200Runnable(sendMap, this, getDevice());
            if (!executor.isTerminated()) {
                executor = Executors.newScheduledThreadPool(2,
                        new NamedThreadFactory("org.openhab.binding.km200", true));
                executor.scheduleWithFixedDelay(receivingRunnable, 30, refreshInterval, TimeUnit.SECONDS);
                executor.scheduleWithFixedDelay(sendRunnable, 60, refreshInterval * 2, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60000, TimeUnit.SECONDS)) {
                logger.debug("Services didn't finish in 60000 seconds!");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        synchronized (getDevice()) {
            getDevice().setInited(false);
            getDevice().setIP4Address("");
            getDevice().setCryptKeyPriv("");
            getDevice().setMD5Salt("");
            getDevice().setGatewayPassword("");
            getDevice().setPrivatePassword("");
            getDevice().serviceTreeMap.clear();
        }
        updateStatus(ThingStatus.OFFLINE);
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
            logger.trace("initialize Key: {} Value: {}", key, configuration.get(key));
            switch (key) {
                case "ip4Address":
                    String ip = (String) configuration.get("ip4Address");
                    if (ip != null && !ip.isBlank()) {
                        try {
                            InetAddress.getByName(ip);
                        } catch (UnknownHostException e) {
                            logger.warn("IP4_address is not valid!: {}", ip);
                        }
                        getDevice().setIP4Address(ip);
                    } else {
                        logger.debug("No ip4_address configured!");
                    }
                    break;
                case "privateKey":
                    String privateKey = (String) configuration.get("privateKey");
                    if (privateKey != null && !privateKey.isBlank()) {
                        getDevice().setCryptKeyPriv(privateKey);
                    }
                    break;
                case "md5Salt":
                    String md5Salt = (String) configuration.get("md5Salt");
                    if (md5Salt != null && !md5Salt.isBlank()) {
                        getDevice().setMD5Salt(md5Salt);
                    }
                    break;
                case "gatewayPassword":
                    String gatewayPassword = (String) configuration.get("gatewayPassword");
                    if (gatewayPassword != null && !gatewayPassword.isBlank()) {
                        getDevice().setGatewayPassword(gatewayPassword);
                    }
                    break;
                case "privatePassword":
                    String privatePassword = (String) configuration.get("privatePassword");
                    if (privatePassword != null && !privatePassword.isBlank()) {
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
        KM200VirtualServiceHandler virtualServiceHandler;
        /* Checking of the device specific services and creating of a service list */
        for (KM200ThingType thing : KM200ThingType.values()) {
            String rootPath = thing.getRootPath();
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
        virtualServiceHandler = new KM200VirtualServiceHandler(remoteDevice);
        virtualServiceHandler.initVirtualObjects();
        /* Output all available services in the log file */
        getDevice().listAllServices();
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
        List<String> propertyServices = new ArrayList<>();
        propertyServices.add(KM200ThingType.GATEWAY.getRootPath());
        propertyServices.add(KM200ThingType.SYSTEM.getRootPath());
        Map<String, String> bridgeProperties = editProperties();

        for (KM200ThingType tType : KM200ThingType.values()) {
            List<String> asProperties = tType.asBridgeProperties();
            String rootPath = tType.getRootPath();
            if (rootPath.isEmpty()) {
                continue;
            }
            KM200ServiceObject serObj = getDevice().getServiceObject(rootPath);
            if (null != serObj) {
                for (String subKey : asProperties) {
                    if (serObj.serviceTreeMap.containsKey(subKey)) {
                        KM200ServiceObject subKeyObj = serObj.serviceTreeMap.get(subKey);
                        if (subKeyObj != null) {
                            String subKeyType = subKeyObj.getServiceType();
                            if (!DATA_TYPE_STRING_VALUE.equals(subKeyType)
                                    && !DATA_TYPE_FLOAT_VALUE.equals(subKeyType)) {
                                continue;
                            }
                            if (bridgeProperties.containsKey(subKey)) {
                                bridgeProperties.remove(subKey);
                            }
                            Object value = subKeyObj.getValue();
                            logger.trace("Add Property: {}  :{}", subKey, value);
                            if (null != value) {
                                bridgeProperties.put(subKey, value.toString());
                            }
                        }
                    }
                }
            }
        }
        updateProperties(bridgeProperties);
    }

    /**
     * Prepares a message for sending
     */
    public void prepareMessage(Thing thing, Channel channel, Command command) {
        if (getDevice().getInited()) {
            JsonObject newObject = null;
            State state = null;
            String service = KM200Utils.checkParameterReplacement(channel, getDevice());
            String chTypes = channel.getAcceptedItemType();
            if (null == chTypes) {
                logger.warn("Channel {} has not accepted item types", channel.getLabel());
                return;
            }
            logger.trace("handleCommand channel: {} service: {}", channel.getLabel(), service);
            newObject = dataHandler.sendProvidersState(service, command, chTypes,
                    KM200Utils.getChannelConfigurationStrings(channel));
            synchronized (getDevice()) {
                KM200ServiceObject serObjekt = getDevice().getServiceObject(service);
                if (null != serObjekt) {
                    if (newObject != null) {
                        sendMap.put(channel, newObject);
                    } else if (getDevice().containsService(service) && serObjekt.getVirtual() == 1) {
                        String parent = serObjekt.getParent();
                        for (Thing actThing : getThing().getThings()) {
                            logger.trace("Checking: {}", actThing.getUID().getAsString());
                            for (Channel tmpChannel : actThing.getChannels()) {
                                String tmpChTypes = tmpChannel.getAcceptedItemType();
                                if (null == tmpChTypes) {
                                    logger.warn("Channel {} has not accepted item types", tmpChannel.getLabel());
                                    return;
                                }
                                String actService = KM200Utils.checkParameterReplacement(tmpChannel, getDevice());
                                KM200ServiceObject actSerObjekt = getDevice().getServiceObject(actService);
                                if (null != actSerObjekt) {
                                    String actParent = actSerObjekt.getParent();
                                    if (actParent != null && actParent.equals(parent)) {
                                        state = dataHandler.getProvidersState(actService, tmpChTypes,
                                                KM200Utils.getChannelConfigurationStrings(tmpChannel));
                                        if (state != null) {
                                            try {
                                                updateState(tmpChannel.getUID(), state);
                                            } catch (IllegalStateException e) {
                                                logger.warn("Could not get updated item state", e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        logger.debug("Service is not availible: {}", service);
                    }
                }
            }
        }
    }

    /**
     * Update the children
     */
    // Every thing has here a handler
    private void updateChildren(Map<Channel, JsonObject> sendMap, KM200GatewayHandler gatewayHandler,
            KM200Device remoteDevice, @Nullable String parent) {
        State state;
        synchronized (remoteDevice) {
            if (parent != null) {
                KM200ServiceObject serParObjekt = remoteDevice.getServiceObject(parent);
                if (null != serParObjekt) {
                    serParObjekt.setUpdated(false);
                }
            }
            for (Thing actThing : gatewayHandler.getThing().getThings()) {
                for (Channel actChannel : actThing.getChannels()) {
                    String actChTypes = actChannel.getAcceptedItemType();
                    if (null == actChTypes) {
                        logger.warn("Channel {} has not accepted item types", actChannel.getLabel());
                        return;
                    }
                    logger.trace("Checking: {} Root: {}", actChannel.getUID().getAsString(),
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
                    KM200ServiceObject tmpSerObjekt = remoteDevice.getServiceObject(tmpService);
                    if (null != tmpSerObjekt) {
                        if (parent == null || parent.equals(tmpSerObjekt.getParent())) {
                            synchronized (sendMap) {
                                JsonObject obj = sendMap.get(actChannel);
                                if (obj != null) {
                                    state = dataHandler.parseJSONData(obj, tmpSerObjekt.getServiceType(), tmpService,
                                            actChTypes, KM200Utils.getChannelConfigurationStrings(actChannel));
                                } else {
                                    state = dataHandler.getProvidersState(tmpService, actChTypes,
                                            KM200Utils.getChannelConfigurationStrings(actChannel));
                                }
                            }
                            if (state != null) {
                                try {
                                    gatewayHandler.updateState(actChannel.getUID(), state);
                                } catch (IllegalStateException e) {
                                    logger.warn("Could not get updated item state", e);
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
                    logger.trace("Checking: {} Root: {}", channel.getUID().getAsString(),
                            channel.getProperties().get("root"));
                    String chTypes = channel.getAcceptedItemType();
                    if (null == chTypes) {
                        logger.warn("Channel {} has not accepted item types", channel.getLabel());
                        return;
                    }
                    String service = KM200Utils.checkParameterReplacement(channel, remoteDevice);
                    KM200ServiceObject object = remoteDevice.getServiceObject(service);
                    if (null != object) {
                        if (object.getVirtual() == 1) {
                            String parent = object.getParent();
                            updateChildren(sendMap, gatewayHandler, remoteDevice, parent);
                        } else {
                            object.setUpdated(false);
                            synchronized (sendMap) {
                                KM200ServiceObject serObjekt = remoteDevice.getServiceObject(service);
                                if (null != serObjekt) {
                                    JsonObject obj = sendMap.get(channel);
                                    if (obj != null) {
                                        state = dataHandler.parseJSONData(obj, serObjekt.getServiceType(), service,
                                                chTypes, KM200Utils.getChannelConfigurationStrings(channel));
                                    } else {
                                        state = dataHandler.getProvidersState(service, chTypes,
                                                KM200Utils.getChannelConfigurationStrings(channel));
                                    }
                                }
                                if (state != null) {
                                    try {
                                        gatewayHandler.updateState(channel.getUID(), state);
                                    } catch (IllegalStateException e) {
                                        logger.warn("Could not get updated item state", e);
                                    }
                                }
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
                        if (null != object) {
                            if (object.getVirtual() == 0) {
                                remoteDevice.setServiceNode(service, newObject);
                            } else {
                                String parent = object.getParent();
                                if (null != parent) {
                                    logger.trace("Sending: {} to : {}", newObject, service);
                                    remoteDevice.setServiceNode(parent, newObject);
                                }
                            }
                        }
                    }
                }
            } while (nextEntry != null);
        }
    }
}
