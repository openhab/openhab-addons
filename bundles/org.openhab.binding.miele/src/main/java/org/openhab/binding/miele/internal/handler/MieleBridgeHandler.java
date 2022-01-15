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
import java.util.HashMap;
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

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.miele.internal.FullyQualifiedApplianceIdentifier;
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
public class MieleBridgeHandler extends BaseBridgeHandler {

    @NonNull
    public static final Set<@NonNull ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_XGW3000);

    private static final String MIELE_CLASS = "com.miele.xgw3000.gateway.hdm.deviceclasses.Miele";

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
    protected ScheduledFuture<?> pollingJob;
    protected ExecutorService executor;
    protected Future<?> eventListenerJob;

    @NonNull
    protected Map<String, HomeDevice> cachedHomeDevicesByApplianceId = new ConcurrentHashMap<String, HomeDevice>();
    protected Map<String, HomeDevice> cachedHomeDevicesByRemoteUid = new ConcurrentHashMap<String, HomeDevice>();

    protected URL url;
    protected Map<String, String> headers;

    // Data structures to de-JSONify whatever Miele appliances are sending us
    public class HomeDevice {

        private static final String MIELE_APPLIANCE_CLASS = "com.miele.xgw3000.gateway.hdm.deviceclasses.MieleAppliance";

        public String Name;
        public String Status;
        public String ParentUID;
        public String ProtocolAdapterName;
        public String Vendor;
        public String UID;
        public String Type;
        public JsonArray DeviceClasses;
        public String Version;
        public String TimestampAdded;
        public JsonObject Error;
        public JsonObject Properties;

        HomeDevice() {
        }

        public FullyQualifiedApplianceIdentifier getApplianceIdentifier() {
            return new FullyQualifiedApplianceIdentifier(this.UID);
        }

        @NonNull
        public String getSerialNumber() {
            return Properties.get("serial.number").getAsString();
        }

        @NonNull
        public String getFirmwareVersion() {
            return Properties.get("firmware.version").getAsString();
        }

        @NonNull
        public String getRemoteUid() {
            JsonElement remoteUid = Properties.get("remote.uid");
            if (remoteUid == null) {
                // remote.uid and serial.number seems to be the same. If remote.uid
                // is missing for some reason, it makes sense to provide fallback
                // to serial number.
                return getSerialNumber();
            }
            return remoteUid.getAsString();
        }

        public String getConnectionType() {
            JsonElement connectionType = Properties.get("connection.type");
            if (connectionType == null) {
                return null;
            }
            return connectionType.getAsString();
        }

        public String getConnectionBaudRate() {
            JsonElement baudRate = Properties.get("connection.baud.rate");
            if (baudRate == null) {
                return null;
            }
            return baudRate.getAsString();
        }

        @NonNull
        public String getApplianceModel() {
            JsonElement model = Properties.get("miele.model");
            if (model == null) {
                return "";
            }
            return model.getAsString();
        }

        public String getDeviceClass() {
            for (JsonElement dc : DeviceClasses) {
                String dcStr = dc.getAsString();
                if (dcStr.contains(MIELE_CLASS) && !dcStr.equals(MIELE_APPLIANCE_CLASS)) {
                    return dcStr.substring(MIELE_CLASS.length());
                }
            }
            return null;
        }
    }

    public class DeviceClassObject {
        public String DeviceClassType;
        public JsonArray Operations;
        public String DeviceClass;
        public JsonArray Properties;

        DeviceClassObject() {
        }
    }

    public class DeviceOperation {
        public String Name;
        public String Arguments;
        public JsonObject Metadata;

        DeviceOperation() {
        }
    }

    public class DeviceProperty {
        public String Name;
        public String Value;
        public int Polling;
        public JsonObject Metadata;

        DeviceProperty() {
        }
    }

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

        // for future usage - no headers to be set for now
        headers = new HashMap<>();

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

                @NonNull
                Set<@NonNull Entry<String, HomeDevice>> cachedEntries = cachedHomeDevicesByApplianceId.entrySet();
                @NonNull
                Iterator<@NonNull Entry<String, HomeDevice>> iterator = cachedEntries.iterator();

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
                        FullyQualifiedApplianceIdentifier applianceIdentifier = getApplianceIdentifierFromApplianceId(
                                applianceId);

                        if (applianceIdentifier == null) {
                            logger.error("The appliance with ID '{}' was not found in appliance list from bridge.",
                                    applianceId);
                            continue;
                        }

                        Object[] args = new Object[2];
                        args[0] = applianceIdentifier.getUid();
                        args[1] = true;
                        JsonElement result = invokeRPC("HDAccess/getDeviceClassObjects", args);

                        if (result != null) {
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
                }
            } catch (Exception e) {
                logger.debug("An exception occurred while polling an appliance :'{}'", e.getMessage());
            }
        }

        private boolean isReachable(String ipAddress) {
            try {
                // note that InetAddress.isReachable is unreliable, see
                // http://stackoverflow.com/questions/9922543/why-does-inetaddress-isreachable-return-false-when-i-can-ping-the-ip-address
                // That's why we do an HTTP access instead

                // If there is no connection, this line will fail
                JsonElement result = invokeRPC("system.listMethods", null);
                if (result == null) {
                    logger.debug("{} is not reachable", ipAddress);
                    return false;
                }
            } catch (Exception e) {
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
                    devices.add(hd);
                }
            } catch (Exception e) {
                logger.debug("An exception occurred while getting the home devices :'{}'", e.getMessage());
            }
        }
        return devices;
    }

    private FullyQualifiedApplianceIdentifier getApplianceIdentifierFromApplianceId(String applianceId) {
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

                                DeviceProperty dp = new DeviceProperty();
                                String id = null;

                                String[] parts = event.split("&");
                                for (String p : parts) {
                                    String[] subparts = p.split("=");
                                    switch (subparts[0]) {
                                        case "property": {
                                            dp.Name = subparts[1];
                                            break;
                                        }
                                        case "value": {
                                            dp.Value = subparts[1].strip().trim();
                                            break;
                                        }
                                        case "id": {
                                            id = subparts[1];
                                            break;
                                        }
                                    }
                                }

                                if (id == null) {
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
                                for (ApplianceStatusListener listener : applianceStatusListeners) {
                                    listener.onAppliancePropertyChanged(applianceIdentifier, dp);
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

    public JsonElement invokeOperation(String applianceId, String modelID, String methodName) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("The Bridge is offline - operations can not be invoked.");
            return null;
        }

        FullyQualifiedApplianceIdentifier applianceIdentifier = getApplianceIdentifierFromApplianceId(applianceId);
        if (applianceIdentifier == null) {
            logger.error(
                    "The appliance with ID '{}' was not found in appliance list from bridge - operations can not be invoked.",
                    applianceId);
            return null;
        }

        Object[] args = new Object[4];
        args[0] = applianceIdentifier.getUid();
        args[1] = MIELE_CLASS + modelID;
        args[2] = methodName;
        args[3] = null;

        return invokeRPC("HDAccess/invokeDCOOperation", args);
    }

    protected JsonElement invokeRPC(String methodName, Object[] args) {
        int id = rand.nextInt(Integer.MAX_VALUE);

        JsonObject req = new JsonObject();
        req.addProperty("jsonrpc", "2.0");
        req.addProperty("id", id);
        req.addProperty("method", methodName);

        JsonElement result = null;

        JsonArray params = new JsonArray();
        if (args != null) {
            for (Object o : args) {
                params.add(gson.toJsonTree(o));
            }
        }
        req.add("params", params);

        String requestData = req.toString();
        String responseData = null;
        try {
            responseData = post(url, headers, requestData);
        } catch (Exception e) {
            logger.debug("An exception occurred while posting data : '{}'", e.getMessage());
        }

        if (responseData != null) {
            logger.trace("The request '{}' yields '{}'", requestData, responseData);
            JsonObject resp = (JsonObject) JsonParser.parseReader(new StringReader(responseData));

            result = resp.get("result");
            JsonElement error = resp.get("error");

            if (error != null && !error.isJsonNull()) {
                if (error.isJsonPrimitive()) {
                    logger.debug("A remote exception occurred: '{}'", error.getAsString());
                } else if (error.isJsonObject()) {
                    JsonObject o = error.getAsJsonObject();
                    Integer code = (o.has("code") ? o.get("code").getAsInt() : null);
                    String message = (o.has("message") ? o.get("message").getAsString() : null);
                    String data = (o.has("data") ? (o.get("data") instanceof JsonObject ? o.get("data").toString()
                            : o.get("data").getAsString()) : null);
                    logger.debug("A remote exception occurred: '{}':'{}':'{}'", code, message, data);
                } else {
                    logger.debug("An unknown remote exception occurred: '{}'", error.toString());
                }
            }
        }

        return result;
    }

    protected String post(URL url, Map<String, String> headers, String data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }
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
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.trace("Scheduling the Miele polling job period is {}", POLLING_PERIOD);
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, POLLING_PERIOD, TimeUnit.SECONDS);
            logger.trace("Scheduling the Miele polling job Job is done ?{}", pollingJob.isDone());
        }
        logger.debug("Scheduling the Miele event listener job");

        if (eventListenerJob == null || eventListenerJob.isCancelled()) {
            executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("binding-miele"));
            eventListenerJob = executor.submit(eventListenerRunnable);
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
        if (applianceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null ApplianceStatusListener.");
        }
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
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        if (eventListenerJob != null) {
            eventListenerJob.cancel(true);
            eventListenerJob = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }
}
