/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IllformedLocaleException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miele.internal.FullyQualifiedApplianceIdentifier;
import org.openhab.binding.miele.internal.api.dto.DeviceClassObject;
import org.openhab.binding.miele.internal.api.dto.DeviceProperty;
import org.openhab.binding.miele.internal.api.dto.HomeDevice;
import org.openhab.binding.miele.internal.exceptions.MieleRpcException;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link MieleBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 * @author Kai Kreuzer - Fixed lifecycle issues
 * @author Martin Lepsy - Added protocol information to support WiFi devices & some refactoring for HomeDevice
 * @author Jacob Laursen - Fixed multicast and protocol support (ZigBee/LAN)
 **/
@NonNullByDefault
public class MieleBridgeHandler extends BaseBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_XGW3000);

    private static final Pattern IP_PATTERN = Pattern
            .compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    protected static final int POLLING_PERIOD = 15; // in seconds
    protected static final int JSON_RPC_PORT = 2810;
    protected static final String JSON_RPC_MULTICAST_IP1 = "239.255.68.139";
    protected static final String JSON_RPC_MULTICAST_IP2 = "224.255.68.139";
    private boolean lastBridgeConnectionState = false;
    private boolean currentBridgeConnectionState = false;

    protected Random rand = new Random();
    protected Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(MieleBridgeHandler.class);

    protected List<ApplianceStatusListener> applianceStatusListeners = new CopyOnWriteArrayList<>();
    protected @Nullable ScheduledFuture<?> pollingJob;
    protected @Nullable ExecutorService executor;
    protected @Nullable Future<?> eventListenerJob;

    protected Map<String, HomeDevice> cachedHomeDevicesByApplianceId = new ConcurrentHashMap<String, HomeDevice>();
    protected Map<String, HomeDevice> cachedHomeDevicesByRemoteUid = new ConcurrentHashMap<String, HomeDevice>();

    protected @Nullable URL url;

    public MieleBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the Miele bridge handler.");

        if (!validateConfig(getConfig())) {
            return;
        }

        try {
            url = new URL("http://" + (String) getConfig().get(HOST) + "/remote/json-rpc");
        } catch (MalformedURLException e) {
            logger.debug("An exception occurred while defining an URL :'{}'", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        onUpdate();
        lastBridgeConnectionState = false;
        updateStatus(ThingStatus.UNKNOWN);
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
        if (!IP_PATTERN.matcher((String) config.get(HOST)).matches()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.invalid-ip-gateway [\"" + config.get(HOST) + "\"]");
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
            if (!IP_PATTERN.matcher((String) getConfig().get(HOST)).matches()) {
                logger.debug("Invalid IP address for the Miele@Home gateway : '{}'", getConfig().get(HOST));
                return;
            }

            try {
                if (isReachable((String) getConfig().get(HOST))) {
                    currentBridgeConnectionState = true;
                } else {
                    currentBridgeConnectionState = false;
                    lastBridgeConnectionState = false;
                    onConnectionLost();
                }

                if (!lastBridgeConnectionState && currentBridgeConnectionState) {
                    logger.debug("Connection to Miele Gateway {} established.", getConfig().get(HOST));
                    lastBridgeConnectionState = true;
                    onConnectionResumed();
                }

                if (!currentBridgeConnectionState || getThing().getStatus() != ThingStatus.ONLINE) {
                    return;
                }

                List<HomeDevice> homeDevices = getHomeDevices();
                for (HomeDevice hd : homeDevices) {
                    String key = hd.getApplianceIdentifier().getApplianceId();
                    if (!cachedHomeDevicesByApplianceId.containsKey(key)) {
                        logger.debug("A new appliance with ID '{}' has been added", hd.UID);
                        for (ApplianceStatusListener listener : applianceStatusListeners) {
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
                        for (ApplianceStatusListener listener : applianceStatusListeners) {
                            listener.onApplianceRemoved(cachedHomeDevice);
                        }
                        cachedHomeDevicesByRemoteUid.remove(cachedHomeDevice.getRemoteUid());
                        iterator.remove();
                    }
                }

                for (Thing appliance : getThing().getThings()) {
                    if (appliance.getStatus() == ThingStatus.ONLINE) {
                        String applianceId = (String) appliance.getConfiguration().getProperties().get(APPLIANCE_ID);
                        FullyQualifiedApplianceIdentifier applianceIdentifier = null;
                        if (applianceId != null) {
                            applianceIdentifier = getApplianceIdentifierFromApplianceId(applianceId);
                        }

                        if (applianceIdentifier == null) {
                            logger.warn("The appliance with ID '{}' was not found in appliance list from bridge.",
                                    applianceId);
                            continue;
                        }

                        Object[] args = new Object[2];
                        args[0] = applianceIdentifier.getUid();
                        args[1] = true;
                        JsonElement result = invokeRPC("HDAccess/getDeviceClassObjects", args);

                        for (JsonElement obj : result.getAsJsonArray()) {
                            try {
                                DeviceClassObject dco = gson.fromJson(obj, DeviceClassObject.class);

                                // Skip com.prosyst.mbs.services.zigbee.hdm.deviceclasses.ReportingControl
                                if (dco == null || !dco.DeviceClass.startsWith(MIELE_CLASS)) {
                                    continue;
                                }

                                for (ApplianceStatusListener listener : applianceStatusListeners) {
                                    listener.onApplianceStateChanged(applianceIdentifier, dco);
                                }
                            } catch (Exception e) {
                                logger.debug("An exception occurred while querying an appliance : '{}'",
                                        e.getMessage());
                            }
                        }
                    }
                }
            } catch (MieleRpcException e) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    logger.debug("An exception occurred while polling an appliance: '{}'", e.getMessage());
                } else {
                    logger.debug("An exception occurred while polling an appliance: '{}' -> '{}'", e.getMessage(),
                            cause.getMessage());
                }
            }
        }

        private boolean isReachable(String ipAddress) {
            try {
                // note that InetAddress.isReachable is unreliable, see
                // http://stackoverflow.com/questions/9922543/why-does-inetaddress-isreachable-return-false-when-i-can-ping-the-ip-address
                // That's why we do an HTTP access instead

                // If there is no connection, this line will fail
                invokeRPC("system.listMethods", new Object[0]);
            } catch (MieleRpcException e) {
                logger.debug("{} is not reachable", ipAddress);
                return false;
            }

            logger.debug("{} is reachable", ipAddress);
            return true;
        }
    };

    public List<HomeDevice> getHomeDevices() {
        List<HomeDevice> devices = new ArrayList<>();

        if (getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                String[] args = new String[1];
                args[0] = "(type=SuperVision)";
                JsonElement result = invokeRPC("HDAccess/getHomeDevices", args);

                for (JsonElement obj : result.getAsJsonArray()) {
                    HomeDevice hd = gson.fromJson(obj, HomeDevice.class);
                    if (hd != null) {
                        devices.add(hd);
                    }
                }
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
        if (IP_PATTERN.matcher((String) getConfig().get(INTERFACE)).matches()) {
            while (true) {
                // Get the address that we are going to connect to.
                InetAddress address1 = null;
                InetAddress address2 = null;
                try {
                    address1 = InetAddress.getByName(JSON_RPC_MULTICAST_IP1);
                    address2 = InetAddress.getByName(JSON_RPC_MULTICAST_IP2);
                } catch (UnknownHostException e) {
                    logger.debug("An exception occurred while setting up the multicast receiver : '{}'",
                            e.getMessage());
                }

                byte[] buf = new byte[256];
                MulticastSocket clientSocket = null;

                while (true) {
                    try {
                        clientSocket = new MulticastSocket(JSON_RPC_PORT);
                        clientSocket.setSoTimeout(100);

                        clientSocket.setInterface(InetAddress.getByName((String) getConfig().get(INTERFACE)));
                        clientSocket.joinGroup(address1);
                        clientSocket.joinGroup(address2);

                        while (true) {
                            try {
                                buf = new byte[256];
                                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                                clientSocket.receive(packet);

                                String event = new String(packet.getData());
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
                                            value = subparts[1].strip().trim();
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
                                for (ApplianceStatusListener listener : applianceStatusListeners) {
                                    listener.onAppliancePropertyChanged(applianceIdentifier, deviceProperty);
                                }
                            } catch (SocketTimeoutException e) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ex) {
                                    logger.debug("Eventlistener has been interrupted.");
                                    break;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.debug("An exception occurred while receiving multicast packets : '{}'", ex.getMessage());
                    }

                    // restart the cycle with a clean slate
                    try {
                        if (clientSocket != null) {
                            clientSocket.leaveGroup(address1);
                            clientSocket.leaveGroup(address2);
                        }
                    } catch (IOException e) {
                        logger.debug("An exception occurred while leaving multicast group : '{}'", e.getMessage());
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                }
            }
        } else {
            logger.debug("Invalid IP address for the multicast interface : '{}'", getConfig().get(INTERFACE));
        }
    };

    public JsonElement invokeOperation(String applianceId, String modelID, String methodName) throws MieleRpcException {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            throw new MieleRpcException("Bridge is offline, operations can not be invoked");
        }

        FullyQualifiedApplianceIdentifier applianceIdentifier = getApplianceIdentifierFromApplianceId(applianceId);
        if (applianceIdentifier == null) {
            throw new MieleRpcException("Appliance with ID" + applianceId
                    + " was not found in appliance list from gateway - operations can not be invoked");
        }

        Object[] args = new Object[4];
        args[0] = applianceIdentifier.getUid();
        args[1] = MIELE_CLASS + modelID;
        args[2] = methodName;
        args[3] = null;

        return invokeRPC("HDAccess/invokeDCOOperation", args);
    }

    protected JsonElement invokeRPC(String methodName, Object[] args) throws MieleRpcException {
        JsonElement result = null;
        URL url = this.url;
        if (url == null) {
            throw new MieleRpcException("URL is not set");
        }

        JsonObject req = new JsonObject();
        int id = rand.nextInt(Integer.MAX_VALUE);
        req.addProperty("jsonrpc", "2.0");
        req.addProperty("id", id);
        req.addProperty("method", methodName);

        JsonArray params = new JsonArray();
        for (Object o : args) {
            params.add(gson.toJsonTree(o));
        }
        req.add("params", params);

        String requestData = req.toString();
        String responseData = null;
        try {
            responseData = post(url, Collections.emptyMap(), requestData);
        } catch (IOException e) {
            throw new MieleRpcException("Exception occurred while posting data", e);
        }

        logger.trace("The request '{}' yields '{}'", requestData, responseData);
        JsonObject parsedResponse = null;
        try {
            parsedResponse = (JsonObject) JsonParser.parseReader(new StringReader(responseData));
        } catch (JsonParseException e) {
            throw new MieleRpcException("Error parsing JSON response", e);
        }

        JsonElement error = parsedResponse.get("error");
        if (error != null && !error.isJsonNull()) {
            if (error.isJsonPrimitive()) {
                throw new MieleRpcException("Remote exception occurred: '" + error.getAsString() + "'");
            } else if (error.isJsonObject()) {
                JsonObject o = error.getAsJsonObject();
                Integer code = (o.has("code") ? o.get("code").getAsInt() : null);
                String message = (o.has("message") ? o.get("message").getAsString() : null);
                String data = (o.has("data")
                        ? (o.get("data") instanceof JsonObject ? o.get("data").toString() : o.get("data").getAsString())
                        : null);
                throw new MieleRpcException(
                        "Remote exception occurred: '" + code + "':'" + message + "':'" + data + "'");
            } else {
                throw new MieleRpcException("Unknown remote exception occurred: '" + error.toString() + "'");
            }
        }

        result = parsedResponse.get("result");
        if (result == null) {
            throw new MieleRpcException("Result is missing in response");
        }

        return result;
    }

    protected String post(URL url, Map<String, String> headers, String data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.addRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.addRequestProperty("Accept-Encoding", "gzip");

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.connect();

        OutputStream out = null;

        try {
            out = connection.getOutputStream();

            out.write(data.getBytes());
            out.flush();

            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                logger.debug("An unexpected status code was returned: '{}'", statusCode);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }

        String responseEncoding = connection.getHeaderField("Content-Encoding");
        responseEncoding = (responseEncoding == null ? "" : responseEncoding.trim());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream in = connection.getInputStream();
        try {
            in = connection.getInputStream();
            if ("gzip".equalsIgnoreCase(responseEncoding)) {
                in = new GZIPInputStream(in);
            }
            in = new BufferedInputStream(in);

            byte[] buff = new byte[1024];
            int n;
            while ((n = in.read(buff)) > 0) {
                bos.write(buff, 0, n);
            }
            bos.flush();
            bos.close();
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return bos.toString();
    }

    private synchronized void onUpdate() {
        logger.debug("Scheduling the Miele polling job");
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.trace("Scheduling the Miele polling job period is {}", POLLING_PERIOD);
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, POLLING_PERIOD, TimeUnit.SECONDS);
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

    /**
     * This method is called whenever the connection to the given {@link MieleBridge} is lost.
     *
     */
    public void onConnectionLost() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
    }

    /**
     * This method is called whenever the connection to the given {@link MieleBridge} is resumed.
     *
     * @param bridge the Miele bridge the connection is resumed to
     */
    public void onConnectionResumed() {
        updateStatus(ThingStatus.ONLINE);
        for (Thing thing : getThing().getThings()) {
            MieleApplianceHandler<?> handler = (MieleApplianceHandler<?>) thing.getHandler();
            if (handler != null) {
                handler.onBridgeConnectionResumed();
            }
        }
    }

    public boolean registerApplianceStatusListener(ApplianceStatusListener applianceStatusListener) {
        boolean result = applianceStatusListeners.add(applianceStatusListener);
        if (result && isInitialized()) {
            onUpdate();

            for (HomeDevice hd : getHomeDevices()) {
                applianceStatusListener.onApplianceAdded(hd);
            }
        }
        return result;
    }

    public boolean unregisterApplianceStatusListener(ApplianceStatusListener applianceStatusListener) {
        boolean result = applianceStatusListeners.remove(applianceStatusListener);
        if (result && isInitialized()) {
            onUpdate();
        }
        return result;
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
