/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.handler;

import static org.openhab.binding.km200.KM200BindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.km200.KM200ServiceTypes;
import org.openhab.binding.km200.internal.KM200Comm;
import org.openhab.binding.km200.internal.KM200CommObject;
import org.openhab.binding.km200.internal.KM200Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

/**
 * The {@link KM200GatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200GatewayHandler extends BaseBridgeHandler {

    private static Logger logger = LoggerFactory.getLogger(KM200GatewayHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_KMDEVICE);

    private Map<Channel, byte[]> sendMap = Collections.synchronizedMap(new LinkedHashMap<Channel, byte[]>());

    private ScheduledFuture<?> pollingJob;
    public KM200Device device = null;
    private KM200Comm<KM200Device> comm = null;
    private SendKM200Thread sThread = null;

    public KM200GatewayHandler(Bridge bridge) {
        super(bridge);
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, ""));

        if (device == null) {
            device = new KM200Device();
        }
        if (comm == null) {
            comm = new KM200Comm<KM200Device>(device);
        }
    }

    private List<KM200GatewayStatusListener> listeners = new CopyOnWriteArrayList<KM200GatewayStatusListener>();

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = getThing().getChannel(channelUID.getId());
        Class<?> commandType = command.getClass();

        if (commandType.isAssignableFrom(DateTimeType.class) || commandType.isAssignableFrom(DecimalType.class)
                || commandType.isAssignableFrom(StringType.class)) {
            prepareMessage(thing, channel, command);
        } else {
            logger.warn("Unsupported Command: {} Class: {}", command.toFullString(), command.getClass());
        }
    }

    @Override
    public void initialize() {
        Boolean isIniting = false;
        if (!device.getInited() && !isIniting) {
            isIniting = true;
            logger.info("Update KM50/100/200 gateway configuration, it takes a minute....");
            updateStatus(ThingStatus.UNKNOWN);

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
                                logger.error("IP4_address is not valid!");
                            }
                            device.setIP4Address(ip);
                        } else {
                            logger.error("No ip4_address configured!");
                        }
                        break;
                    case "privateKey":
                        String PrivKey = (String) configuration.get("privateKey");
                        if (StringUtils.isNotBlank(PrivKey)) {
                            device.setCryptKeyPriv(PrivKey);
                        }
                        break;
                    case "md5Salt":
                        String MD5Salt = (String) configuration.get("md5Salt");
                        if (StringUtils.isNotBlank(MD5Salt)) {
                            device.setMD5Salt(MD5Salt);
                        }
                        break;
                    case "gatewayPassword":
                        String gpassword = (String) configuration.get("gatewayPassword");
                        if (StringUtils.isNotBlank(gpassword)) {
                            device.setGatewayPassword(gpassword);
                        }
                        break;
                    case "privatePassword":
                        String ppassword = (String) configuration.get("privatePassword");
                        if (StringUtils.isNotBlank(ppassword)) {
                            device.setPrivatePassword(ppassword);
                        }
                        break;
                }
            }

            if (device.isConfigured()) {

                /* Get HTTP Data from device */
                byte[] recData = comm.getDataFromService("/gateway/DateTime");
                if (recData == null) {
                    logger.error("Communication is not possible!");
                    return;
                }
                if (recData.length == 0) {
                    logger.error("No reply from KM200!");
                    return;
                }
                logger.info("Received data..");
                /* Decrypt the message */
                String decodedData = comm.decodeMessage(recData);
                if (decodedData == null) {
                    logger.error("Decoding of the KM200 message is not possible!");
                    return;
                }

                if (decodedData == "SERVICE NOT AVAILABLE") {
                    logger.error("/gateway/DateTime: SERVICE NOT AVAILABLE");
                } else {
                    logger.info("Test of the communication to the gateway was successful..");
                }
                /* Testing the received data, is decryption working? */
                try {
                    JSONObject nodeRoot = new JSONObject(decodedData);
                    @SuppressWarnings("unused")
                    String type = nodeRoot.getString("type");
                    @SuppressWarnings("unused")
                    String id = nodeRoot.getString("id");
                } catch (JSONException e) {
                    logger.error("The data is not readable, check the key and password configuration!", e.getMessage(),
                            decodedData);
                    updateStatus(ThingStatus.OFFLINE);
                    return;
                }

                logger.info("Init services..");
                /* communication is working */
                /* Checking of the device specific services and creating of a service list */
                for (KM200ServiceTypes service : KM200ServiceTypes.values()) {
                    try {
                        logger.debug(service.getRootPath());
                        comm.initObjects(service.getRootPath(), null);
                    } catch (Exception e) {
                        logger.error("Couldn't init service: {} error: {}", service, e.getMessage());
                    }
                }
                /* Now init the virtual services */
                logger.debug("init Virtual Objects");
                try {
                    comm.initVirtualObjects();
                } catch (Exception e) {
                    logger.error("Couldn't init virtual services: {}", e.getMessage());
                }
                /* Output all available services in the log file */
                /* Now init the virtual services */
                logger.debug("list All Services");
                device.listAllServices();
                logger.info("... Update of the KM200 Binding configuration completed");

                device.setInited(true);

                updateStatus(ThingStatus.ONLINE);

            } else {
                updateStatus(ThingStatus.OFFLINE);
                logger.info("The KM50/100/200 gateway configuration is not complete");
                return;
            }

            if (device != null) {
                logger.debug("Starting send thread");
                sThread = new SendKM200Thread(sendMap, this, device, comm);
                sThread.start();
            }

        }
        if (pollingJob == null) {
            GetKM200Runnable runnable = new GetKM200Runnable(this, device, comm);
            logger.debug("starting runnable.");
            pollingJob = scheduler.scheduleAtFixedRate(runnable, 0, 60, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        if (sThread != null) {
            logger.debug("Interrupt send thread");
            sThread.interrupt();
        }

        if (device != null) {
            synchronized (device) {
                device.setInited(false);
                device.setIP4Address("");
                device.setCryptKeyPriv("");
                device.setMD5Salt("");
                device.setGatewayPassword("");
                device.setPrivatePassword("");
                device.serviceTreeMap.clear();
            }
        }
    }

    @Override
    public void handleRemoval() {
        for (Thing tmpThing : getThing().getThings()) {
            tmpThing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ""));
        }
        this.updateStatus(ThingStatus.REMOVED);
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
     * Translates a service name to a service path (Replaces # through /)
     *
     * @param name
     */
    public static String translatesNameToPath(String name) {
        return name.replace("#", "/");
    }

    /**
     * Translates a service path to a service name (Replaces / through #)
     *
     * @param name
     */
    public static String translatesPathToName(String path) {
        return path.replace("/", "#");
    }

    /**
     * Refreshes a channel
     *
     * @param channel
     */
    public void refreshChannel(Channel channel) {
        GetSingleKM200Runnable runnable = new GetSingleKM200Runnable(this, device, comm, channel);
        logger.debug("starting single runnable.");
        scheduler.schedule(runnable, 0, TimeUnit.SECONDS);
    }

    /**
     * This function checks whether the service has a replacement parameter
     *
     * @param channel
     * @param device
     */
    public static String checkParameterReplacement(Channel channel, KM200Device device) {
        String service = translatesNameToPath(channel.getProperties().get("root"));
        if (service.contains(SWITCH_PROGRAM_REPLACEMENT)) {
            String currentService = translatesNameToPath(channel.getProperties().get(SWITCH_PROGRAM_CURRENT_PATH_NAME));
            if (device.containsService(currentService)) {
                if (device.getServiceObject(currentService).getServiceType().equals("stringValue")) {
                    String val = (String) device.getServiceObject(currentService).getValue();
                    service = service.replace("__current__", val);
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
    public static HashMap<String, String> getChannelConfigurationStrings(Channel channel) {

        HashMap<String, String> paraNames = new HashMap<String, String>();
        if (channel.getConfiguration() != null) {
            if (channel.getConfiguration().containsKey("on")) {
                paraNames.put("on", channel.getConfiguration().get("on").toString());
                logger.debug("Added ON: " + channel.getConfiguration().get("on").toString());
            }
        }

        if (channel.getConfiguration() != null) {
            if (channel.getConfiguration().containsKey("off")) {
                paraNames.put("off", channel.getConfiguration().get("off").toString());
                logger.debug("Added OFF: " + channel.getConfiguration().get("off").toString());
            }
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
        if (device != null && device.getInited() && channel != null) {
            byte[] sendData = null;
            State state = null;
            String service = checkParameterReplacement(channel, device);

            logger.debug("handleCommand channel: {} service: {}", channel.getLabel(), service);
            try {
                sendData = comm.sendProvidersState(service, command, channel.getAcceptedItemType(),
                        getChannelConfigurationStrings(channel));
            } catch (Exception e) {
                logger.error("Could not send item state {}", e);
            }

            synchronized (device) {
                if (sendData != null) {
                    sendMap.put(channel, sendData);
                } else if (device.containsService(service)) {
                    if (device.getServiceObject(service).getVirtual() == 1) {
                        String parent = device.getServiceObject(service).getParent();
                        for (Thing tmpThing : getThing().getThings()) {
                            logger.debug("Checking: {}", tmpThing.getUID().getAsString());
                            for (Channel tmpChannel : tmpThing.getChannels()) {
                                String tmpService = checkParameterReplacement(tmpChannel, device);
                                logger.debug("tmpService: {}", tmpService);
                                String tmpParent = device.getServiceObject(tmpService).getParent();
                                if (tmpParent != null && tmpParent.equals(parent)) {
                                    try {
                                        state = comm.getProvidersState(tmpService, tmpChannel.getAcceptedItemType(),
                                                getChannelConfigurationStrings(tmpChannel));
                                        if (state != null) {
                                            updateState(tmpChannel.getUID(), state);
                                        }
                                    } catch (Exception e) {
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
     * The GetKM200Runnable class get the data from device to all items.
     *
     * @author Markus Eckhardt
     *
     * @since 2.1.0
     */
    private static void updateChildren(KM200GatewayHandler gatewayHandler, KM200Device device,
            KM200Comm<KM200Device> comm, String parent) {
        State state = null;
        synchronized (device) {
            if (parent != null) {
                device.getServiceObject(parent).setUpdated(false);
            }
            for (Thing tmpThing : gatewayHandler.getThing().getThings()) {
                for (Channel tmpChannel : tmpThing.getChannels()) {
                    logger.debug("Checking: {} Root: {}", tmpChannel.getUID().getAsString(),
                            tmpChannel.getProperties().get("root"));
                    if (!((KM200ThingHandler) tmpThing.getHandler()).checkLinked(tmpChannel)) {
                        continue;
                    }
                    String tmpService = checkParameterReplacement(tmpChannel, device);
                    if (parent == null || parent.equals(device.getServiceObject(tmpService).getParent())) {
                        try {
                            state = comm.getProvidersState(tmpService, tmpChannel.getAcceptedItemType(),
                                    getChannelConfigurationStrings(tmpChannel));
                            if (state != null) {
                                gatewayHandler.updateState(tmpChannel.getUID(), state);
                            }
                        } catch (Exception e) {
                            logger.error("Could not get updated item state, Error: {}", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * The GetKM200Runnable class get the data from device to all items.
     *
     * @author Markus Eckhardt
     *
     * @since 2.1.0
     */
    private static class GetKM200Runnable implements Runnable {

        public GetKM200Runnable(KM200GatewayHandler gatewayHandler, KM200Device device, KM200Comm<KM200Device> comm) {
            super();
            this.gatewayHandler = gatewayHandler;
            this.device = device;
            this.comm = comm;
        }

        private Logger logger = LoggerFactory.getLogger(GetKM200Runnable.class);

        private KM200GatewayHandler gatewayHandler;
        private KM200Device device;
        private KM200Comm<KM200Device> comm;

        @Override
        public void run() {
            logger.debug("GetKM200Runnable");
            synchronized (device) {
                if (device.getInited()) {
                    device.resetAllUpdates(device.serviceTreeMap);
                    updateChildren(gatewayHandler, device, comm, null);
                }
            }
        }
    }

    /**
     * The GetKM200Runnable class get the data from device for one channel.
     *
     * @author Markus Eckhardt
     *
     * @since 2.1.0
     */
    private static class GetSingleKM200Runnable implements Runnable {

        public GetSingleKM200Runnable(KM200GatewayHandler gatewayHandler, KM200Device device,
                KM200Comm<KM200Device> comm, Channel channel) {
            super();
            this.gatewayHandler = gatewayHandler;
            this.device = device;
            this.comm = comm;
            this.channel = channel;
        }

        private Logger logger = LoggerFactory.getLogger(GetKM200Runnable.class);

        private KM200GatewayHandler gatewayHandler;
        private KM200Device device;
        private KM200Comm<KM200Device> comm;
        private Channel channel;

        @Override
        public void run() {
            logger.debug("GetKM200Runnable");
            State state = null;
            synchronized (device) {
                if (device.getInited()) {
                    logger.debug("Checking: {} Root: {}", channel.getUID().getAsString(),
                            channel.getProperties().get("root"));
                    KM200CommObject object = device.getServiceObject(checkParameterReplacement(channel, device));
                    if (object.getVirtual() == 1) {
                        String parent = object.getParent();
                        updateChildren(gatewayHandler, device, comm, parent);
                    } else {
                        object.setUpdated(false);
                        state = comm.getProvidersState(checkParameterReplacement(channel, device),
                                channel.getAcceptedItemType(), getChannelConfigurationStrings(channel));
                        if (state != null) {
                            gatewayHandler.updateState(channel.getUID(), state);
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
     *
     * @since 2.1.0
     */
    private static class SendKM200Thread extends Thread {

        public SendKM200Thread(Map<Channel, byte[]> sendMap, KM200GatewayHandler gatewayHandler, KM200Device device,
                KM200Comm<KM200Device> comm) {
            super();
            this.sendMap = sendMap;
            this.gatewayHandler = gatewayHandler;
            this.device = device;
            this.comm = comm;
        }

        private Map<Channel, byte[]> sendMap = null;
        private KM200GatewayHandler gatewayHandler;
        private KM200Device device;
        private KM200Comm<KM200Device> comm;

        @Override
        public void run() {
            try {
                logger.debug("Send-Thread started");
                while (!isInterrupted()) {
                    Map.Entry<Channel, byte[]> nextEntry = null;
                    {
                        /* Check whether a new entry is availible, if yes then take and remove it */
                        synchronized (sendMap) {
                            Iterator<Entry<Channel, byte[]>> i = sendMap.entrySet().iterator();

                            if (i.hasNext()) {
                                logger.debug("Send-Thread, new entry");
                                nextEntry = i.next();
                                i.remove();
                            }
                        }
                    }

                    if (nextEntry != null) {
                        /* Now send the data to the device */
                        Integer rCode;
                        org.eclipse.smarthome.core.types.State state = null;
                        Channel channel = nextEntry.getKey();
                        byte[] encData = nextEntry.getValue();

                        String service = checkParameterReplacement(channel, device);
                        KM200CommObject object = device.getServiceObject(service);

                        logger.debug("Sending: {}", service);

                        if (object.getVirtual() == 1) {
                            rCode = comm.sendDataToService(object.getParent(), encData);
                        } else {
                            rCode = comm.sendDataToService(service, encData);
                        }
                        logger.debug("Returncode: {}", rCode);
                        /* set all update flags to zero */

                        logger.debug("Data sended, reset und updated providers");

                        /* Now update the set values and for all virtual values depending on same parent */
                        if (object.getVirtual() == 1) {
                            String parent = object.getParent();
                            updateChildren(gatewayHandler, device, comm, parent);
                        } else {
                            try {
                                object.setUpdated(false);
                                state = comm.getProvidersState(service, channel.getAcceptedItemType(),
                                        getChannelConfigurationStrings(channel));
                                if (state != null) {
                                    gatewayHandler.updateState(channel.getUID(), state);
                                }
                                for (Thing tmpThing : gatewayHandler.getThing().getThings()) {
                                    for (Channel tmpChannel : tmpThing.getChannels()) {
                                        if (!((KM200ThingHandler) tmpThing.getHandler()).checkLinked(tmpChannel)) {
                                            continue;
                                        }
                                        if (tmpChannel.getProperties() != null) {
                                            String tempPName = tmpChannel.getProperties()
                                                    .get(SWITCH_PROGRAM_CURRENT_PATH_NAME);
                                            String tmpService = checkParameterReplacement(tmpChannel, device);

                                            if ((tempPName != null && tempPName.equals(service))
                                                    || tmpService.equals(service)) {
                                                try {
                                                    state = comm.getProvidersState(tmpService,
                                                            tmpChannel.getAcceptedItemType(),
                                                            getChannelConfigurationStrings(tmpChannel));
                                                    if (state != null) {
                                                        gatewayHandler.updateState(tmpChannel.getUID(), state);
                                                    }
                                                } catch (Exception e) {
                                                    logger.error("Could not get updated item state, Error: {}", e);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("Could not get item state, Error: {}", e);
                            }
                        }

                    }
                    /*
                     * We have time, all changes on same item in this time are overwritten in memory and we need send
                     * only the last state
                     */
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        interrupt();
                    }
                }
            } catch (

            Exception e) {
                logger.warn("Error processing command", e);
            }
        }

    }

}
