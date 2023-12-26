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
package org.openhab.binding.miele.internal.handler;

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.IllformedLocaleException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.miele.internal.FullyQualifiedApplianceIdentifier;
import org.openhab.binding.miele.internal.MieleGatewayCommunicationController;
import org.openhab.binding.miele.internal.api.dto.DeviceClassObject;
import org.openhab.binding.miele.internal.api.dto.DeviceProperty;
import org.openhab.binding.miele.internal.api.dto.HomeDevice;
import org.openhab.binding.miele.internal.exceptions.MieleRpcException;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * The {@link MieleBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Fixed lifecycle issues
 * @author Martin Lepsy - Added protocol information to support WiFi devices and some refactoring for HomeDevice
 * @author Jacob Laursen - Fixed multicast and protocol support (Zigbee/LAN)
 **/
@NonNullByDefault
public class MieleBridgeHandler extends BaseBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_XGW3000);

    private static final Pattern IP_PATTERN = Pattern
            .compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static final int POLLING_PERIOD_SECONDS = 15;
    private static final int JSON_RPC_PORT = 2810;
    private static final String JSON_RPC_MULTICAST_IP1 = "239.255.68.139";
    private static final String JSON_RPC_MULTICAST_IP2 = "224.255.68.139";
    private static final int MULTICAST_TIMEOUT_MILLIS = 100;
    private static final int MULTICAST_SLEEP_MILLIS = 500;

    private final Logger logger = LoggerFactory.getLogger(MieleBridgeHandler.class);

    private boolean lastBridgeConnectionState = false;

    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private @NonNullByDefault({}) MieleGatewayCommunicationController gatewayCommunication;

    private Set<DiscoveryListener> discoveryListeners = ConcurrentHashMap.newKeySet();
    private Map<String, ApplianceStatusListener> applianceStatusListeners = new ConcurrentHashMap<>();
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ExecutorService executor;
    private @Nullable Future<?> eventListenerJob;

    private Map<String, HomeDevice> cachedHomeDevicesByApplianceId = new ConcurrentHashMap<>();
    private Map<String, HomeDevice> cachedHomeDevicesByRemoteUid = new ConcurrentHashMap<>();

    public MieleBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for bridge {}", getThing().getUID());

        if (!validateConfig(getConfig())) {
            return;
        }

        try {
            gatewayCommunication = new MieleGatewayCommunicationController(httpClient, (String) getConfig().get(HOST));
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        lastBridgeConnectionState = false;
        schedulePollingAndEventListener();
    }

    private boolean validateConfig(Configuration config) {
        if (config.get(HOST) == null || ((String) config.get(HOST)).isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.ip-address-not-set");
            return false;
        }
        if (config.get(INTERFACE) == null || ((String) config.get(INTERFACE)).isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.ip-multicast-interface-not-set");
            return false;
        }
        if (!IP_PATTERN.matcher((String) config.get(INTERFACE)).matches()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.invalid-ip-multicast-interface [\"" + config.get(INTERFACE)
                            + "\"]");
            return false;
        }
        String language = (String) config.get(LANGUAGE);
        if (language != null && !language.isBlank()) {
            try {
                new Locale.Builder().setLanguageTag(language).build();
            } catch (IllformedLocaleException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "@text/offline.configuration-error.invalid-language [\"" + language + "\"]");
                return false;
            }
        }
        return true;
    }

    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            String host = (String) getConfig().get(HOST);
            try {
                List<HomeDevice> homeDevices = getHomeDevices();

                if (!lastBridgeConnectionState) {
                    logger.debug("Connection to Miele Gateway {} established.", host);
                    lastBridgeConnectionState = true;
                }
                updateStatus(ThingStatus.ONLINE);

                refreshHomeDevices(homeDevices);

                for (Entry<String, ApplianceStatusListener> entry : applianceStatusListeners.entrySet()) {
                    String applianceId = entry.getKey();
                    ApplianceStatusListener listener = entry.getValue();
                    FullyQualifiedApplianceIdentifier applianceIdentifier = getApplianceIdentifierFromApplianceId(
                            applianceId);
                    if (applianceIdentifier == null) {
                        logger.debug("The appliance with ID '{}' was not found in appliance list from bridge.",
                                applianceId);
                        listener.onApplianceRemoved();
                        continue;
                    }

                    Object[] args = new Object[2];
                    args[0] = applianceIdentifier.getUid();
                    args[1] = true;
                    JsonElement result = gatewayCommunication.invokeRPC("HDAccess/getDeviceClassObjects", args);

                    for (JsonElement obj : result.getAsJsonArray()) {
                        try {
                            DeviceClassObject dco = gson.fromJson(obj, DeviceClassObject.class);

                            // Skip com.prosyst.mbs.services.zigbee.hdm.deviceclasses.ReportingControl
                            if (dco == null || !dco.DeviceClass.startsWith(MIELE_CLASS)) {
                                continue;
                            }

                            listener.onApplianceStateChanged(dco);
                        } catch (Exception e) {
                            logger.debug("An exception occurred while querying an appliance : '{}'", e.getMessage());
                        }
                    }
                }
            } catch (MieleRpcException e) {
                Throwable cause = e.getCause();
                String message;
                if (cause == null) {
                    message = e.getMessage();
                    logger.debug("An exception occurred while polling an appliance: '{}'", message);
                } else {
                    message = cause.getMessage();
                    logger.debug("An exception occurred while polling an appliance: '{}' -> '{}'", e.getMessage(),
                            message);
                }
                if (lastBridgeConnectionState) {
                    logger.debug("Connection to Miele Gateway {} lost.", host);
                    lastBridgeConnectionState = false;
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
            }
        }
    };

    private synchronized void refreshHomeDevices(List<HomeDevice> homeDevices) {
        for (HomeDevice hd : homeDevices) {
            String key = hd.getApplianceIdentifier().getApplianceId();
            if (!cachedHomeDevicesByApplianceId.containsKey(key)) {
                logger.debug("A new appliance with ID '{}' has been added", hd.UID);
                for (DiscoveryListener listener : discoveryListeners) {
                    listener.onApplianceAdded(hd);
                }
                ApplianceStatusListener listener = applianceStatusListeners
                        .get(hd.getApplianceIdentifier().getApplianceId());
                if (listener != null) {
                    listener.onApplianceAdded(hd);
                }
            }
            cachedHomeDevicesByApplianceId.put(key, hd);
            cachedHomeDevicesByRemoteUid.put(hd.getRemoteUid(), hd);
        }

        Set<Entry<String, HomeDevice>> cachedEntries = cachedHomeDevicesByApplianceId.entrySet();
        Iterator<Entry<String, HomeDevice>> iterator = cachedEntries.iterator();

        while (iterator.hasNext()) {
            Entry<String, HomeDevice> cachedEntry = iterator.next();
            HomeDevice cachedHomeDevice = cachedEntry.getValue();
            if (!homeDevices.stream().anyMatch(d -> d.UID.equals(cachedHomeDevice.UID))) {
                logger.debug("The appliance with ID '{}' has been removed", cachedHomeDevice.UID);
                for (DiscoveryListener listener : discoveryListeners) {
                    listener.onApplianceRemoved(cachedHomeDevice);
                }
                ApplianceStatusListener listener = applianceStatusListeners
                        .get(cachedHomeDevice.getApplianceIdentifier().getApplianceId());
                if (listener != null) {
                    listener.onApplianceRemoved();
                }
                cachedHomeDevicesByRemoteUid.remove(cachedHomeDevice.getRemoteUid());
                iterator.remove();
            }
        }
    }

    public List<HomeDevice> getHomeDevicesEmptyOnFailure() {
        try {
            return getHomeDevices();
        } catch (MieleRpcException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                logger.debug("An exception occurred while getting the home devices: '{}'", e.getMessage());
            } else {
                logger.debug("An exception occurred while getting the home devices: '{}' -> '{}", e.getMessage(),
                        cause.getMessage());
            }
            return new ArrayList<>();
        }
    }

    private List<HomeDevice> getHomeDevices() throws MieleRpcException {
        List<HomeDevice> devices = new ArrayList<>();

        if (!isInitialized()) {
            return devices;
        }

        String[] args = new String[1];
        args[0] = "(type=SuperVision)";
        JsonElement result = gatewayCommunication.invokeRPC("HDAccess/getHomeDevices", args);

        for (JsonElement obj : result.getAsJsonArray()) {
            HomeDevice hd = gson.fromJson(obj, HomeDevice.class);
            if (hd != null) {
                devices.add(hd);
            }
        }
        return devices;
    }

    private @Nullable FullyQualifiedApplianceIdentifier getApplianceIdentifierFromApplianceId(String applianceId) {
        HomeDevice homeDevice = this.cachedHomeDevicesByApplianceId.get(applianceId);
        if (homeDevice == null) {
            return null;
        }

        return homeDevice.getApplianceIdentifier();
    }

    private Runnable eventListenerRunnable = () -> {
        String interfaceIpAddress = (String) getConfig().get(INTERFACE);
        if (!IP_PATTERN.matcher(interfaceIpAddress).matches()) {
            logger.debug("Invalid IP address for the multicast interface: '{}'", interfaceIpAddress);
            return;
        }

        // Get the address that we are going to connect to.
        InetSocketAddress address1 = null;
        InetSocketAddress address2 = null;
        try {
            address1 = new InetSocketAddress(InetAddress.getByName(JSON_RPC_MULTICAST_IP1), JSON_RPC_PORT);
            address2 = new InetSocketAddress(InetAddress.getByName(JSON_RPC_MULTICAST_IP2), JSON_RPC_PORT);
        } catch (UnknownHostException e) {
            // This can only happen if the hardcoded literal IP addresses are invalid.
            logger.debug("An exception occurred while setting up the multicast receiver: '{}'", e.getMessage());
            return;
        }

        while (!Thread.currentThread().isInterrupted()) {
            MulticastSocket clientSocket = null;
            try {
                clientSocket = new MulticastSocket(JSON_RPC_PORT);
                clientSocket.setSoTimeout(MULTICAST_TIMEOUT_MILLIS);

                NetworkInterface networkInterface = getMulticastInterface(interfaceIpAddress);
                if (networkInterface == null) {
                    logger.warn("Unable to find network interface for address {}", interfaceIpAddress);
                    return;
                }
                clientSocket.setNetworkInterface(networkInterface);
                clientSocket.joinGroup(address1, null);
                clientSocket.joinGroup(address2, null);

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        byte[] buf = new byte[256];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        clientSocket.receive(packet);

                        String event = new String(packet.getData(), packet.getOffset(), packet.getLength(),
                                StandardCharsets.ISO_8859_1);
                        logger.debug("Received a multicast event '{}' from '{}:{}'", event, packet.getAddress(),
                                packet.getPort());

                        String[] parts = event.split("&");
                        String id = null, name = null, value = null;
                        for (String p : parts) {
                            String[] subparts = p.split("=");
                            switch (subparts[0]) {
                                case "property": {
                                    name = subparts[1];
                                    break;
                                }
                                case "value": {
                                    value = subparts[1];
                                    break;
                                }
                                case "id": {
                                    id = subparts[1];
                                    break;
                                }
                            }
                        }

                        if (id == null || name == null || value == null) {
                            continue;
                        }

                        // In XGW 3000 firmware 2.03 this was changed from UID (hdm:ZigBee:0123456789abcdef#210)
                        // to serial number (001234567890)
                        FullyQualifiedApplianceIdentifier applianceIdentifier;
                        if (id.startsWith("hdm:")) {
                            applianceIdentifier = new FullyQualifiedApplianceIdentifier(id);
                        } else {
                            HomeDevice device = cachedHomeDevicesByRemoteUid.get(id);
                            if (device == null) {
                                logger.debug("Multicast event not handled as id {} is unknown.", id);
                                continue;
                            }
                            applianceIdentifier = device.getApplianceIdentifier();
                        }
                        var deviceProperty = new DeviceProperty();
                        deviceProperty.Name = name;
                        deviceProperty.Value = value;
                        ApplianceStatusListener listener = applianceStatusListeners
                                .get(applianceIdentifier.getApplianceId());
                        if (listener != null) {
                            listener.onAppliancePropertyChanged(deviceProperty);
                        }
                    } catch (SocketTimeoutException e) {
                        try {
                            Thread.sleep(MULTICAST_SLEEP_MILLIS);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            logger.debug("Event listener has been interrupted.");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                logger.debug("An exception occurred while receiving multicast packets: '{}'", e.getMessage());
            } finally {
                // restart the cycle with a clean slate
                try {
                    if (clientSocket != null) {
                        clientSocket.leaveGroup(address1, null);
                        clientSocket.leaveGroup(address2, null);
                    }
                } catch (IOException e) {
                    logger.debug("An exception occurred while leaving multicast group: '{}'", e.getMessage());
                }
                if (clientSocket != null) {
                    clientSocket.close();
                }
            }
        }
    };

    private @Nullable NetworkInterface getMulticastInterface(String interfaceIpAddress) throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        @Nullable
        NetworkInterface networkInterface;
        while (networkInterfaces.hasMoreElements()) {
            networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isLoopback()) {
                continue;
            }
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Found interface address {} -> {}", interfaceAddress.toString(),
                            interfaceAddress.getAddress().toString());
                }
                if (interfaceAddress.getAddress().toString().endsWith("/" + interfaceIpAddress)) {
                    return networkInterface;
                }
            }
        }

        return null;
    }

    public JsonElement invokeOperation(String applianceId, String modelID, String methodName) throws MieleRpcException {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            throw new MieleRpcException("Bridge is offline, operations can not be invoked");
        }

        FullyQualifiedApplianceIdentifier applianceIdentifier = getApplianceIdentifierFromApplianceId(applianceId);
        if (applianceIdentifier == null) {
            throw new MieleRpcException("Appliance with ID" + applianceId
                    + " was not found in appliance list from gateway - operations can not be invoked");
        }

        return gatewayCommunication.invokeOperation(applianceIdentifier, modelID, methodName);
    }

    private synchronized void schedulePollingAndEventListener() {
        logger.debug("Scheduling the Miele polling job");
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.trace("Scheduling the Miele polling job period is {}", POLLING_PERIOD_SECONDS);
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, POLLING_PERIOD_SECONDS, TimeUnit.SECONDS);
            this.pollingJob = pollingJob;
            logger.trace("Scheduling the Miele polling job Job is done ?{}", pollingJob.isDone());
        }

        logger.debug("Scheduling the Miele event listener job");
        Future<?> eventListenerJob = this.eventListenerJob;
        if (eventListenerJob == null || eventListenerJob.isCancelled()) {
            ExecutorService executor = Executors
                    .newSingleThreadExecutor(new NamedThreadFactory("binding-" + BINDING_ID));
            this.executor = executor;
            this.eventListenerJob = executor.submit(eventListenerRunnable);
        }
    }

    public boolean registerApplianceStatusListener(String applianceId,
            ApplianceStatusListener applianceStatusListener) {
        ApplianceStatusListener existingListener = applianceStatusListeners.get(applianceId);
        if (existingListener != null) {
            if (!existingListener.equals(applianceStatusListener)) {
                logger.warn("Unsupported configuration: appliance with ID '{}' referenced by multiple things",
                        applianceId);
            } else {
                logger.debug("Duplicate listener registration attempted for '{}'", applianceId);
            }
            return false;
        }
        applianceStatusListeners.put(applianceId, applianceStatusListener);

        HomeDevice cachedHomeDevice = cachedHomeDevicesByApplianceId.get(applianceId);
        if (cachedHomeDevice != null) {
            applianceStatusListener.onApplianceAdded(cachedHomeDevice);
        } else {
            try {
                refreshHomeDevices(getHomeDevices());
            } catch (MieleRpcException e) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    logger.debug("An exception occurred while getting the home devices: '{}'", e.getMessage());
                } else {
                    logger.debug("An exception occurred while getting the home devices: '{}' -> '{}", e.getMessage(),
                            cause.getMessage());
                }
            }
        }

        return true;
    }

    public boolean unregisterApplianceStatusListener(String applianceId,
            ApplianceStatusListener applianceStatusListener) {
        return applianceStatusListeners.remove(applianceId) != null;
    }

    public boolean registerDiscoveryListener(DiscoveryListener discoveryListener) {
        if (!discoveryListeners.add(discoveryListener)) {
            return false;
        }
        if (cachedHomeDevicesByApplianceId.isEmpty()) {
            try {
                refreshHomeDevices(getHomeDevices());
            } catch (MieleRpcException e) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    logger.debug("An exception occurred while getting the home devices: '{}'", e.getMessage());
                } else {
                    logger.debug("An exception occurred while getting the home devices: '{}' -> '{}'", e.getMessage(),
                            cause.getMessage());
                }
            }
        } else {
            for (Entry<String, HomeDevice> entry : cachedHomeDevicesByApplianceId.entrySet()) {
                discoveryListener.onApplianceAdded(entry.getValue());
            }
        }
        return true;
    }

    public boolean unregisterDiscoveryListener(DiscoveryListener discoveryListener) {
        return discoveryListeners.remove(discoveryListener);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do here - the XGW bridge does not handle commands, for now
        if (command instanceof RefreshType) {
            // Placeholder for future refinement
            return;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
        Future<?> eventListenerJob = this.eventListenerJob;
        if (eventListenerJob != null) {
            eventListenerJob.cancel(true);
            this.eventListenerJob = null;
        }
        ExecutorService executor = this.executor;
        if (executor != null) {
            executor.shutdownNow();
            this.executor = null;
        }
    }
}
