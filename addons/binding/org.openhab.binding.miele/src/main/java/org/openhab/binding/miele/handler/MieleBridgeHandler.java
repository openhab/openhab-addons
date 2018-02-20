/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miele.handler;

import static org.openhab.binding.miele.MieleBindingConstants.*;

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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
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
 */
public class MieleBridgeHandler extends BaseBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_XGW3000);

    private static final Pattern IP_PATTERN = Pattern
            .compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    protected final int POLLING_PERIOD = 15; // in seconds
    protected final int JSON_RPC_PORT = 2810;
    protected final String JSON_RPC_MULTICAST_IP1 = "239.255.68.139";
    protected final String JSON_RPC_MULTICAST_IP2 = "224.255.68.139";
    private boolean lastBridgeConnectionState = false;
    private boolean currentBridgeConnectionState = false;

    protected Random rand = new Random();
    protected Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(MieleBridgeHandler.class);

    protected List<ApplianceStatusListener> applianceStatusListeners = new CopyOnWriteArrayList<>();
    protected ScheduledFuture<?> pollingJob;
    protected ScheduledFuture<?> eventListenerJob;

    protected List<HomeDevice> previousHomeDevices = new CopyOnWriteArrayList<HomeDevice>();

    protected URL url;
    protected Map<String, String> headers;

    // Data structures to de-JSONify whatever Miele appliances are sending us
    public class HomeDevice {
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

    public class DeviceMetaData {
        public String Filter;
        public String description;
        public String LocalizedID;
        public String LocalizedValue;
        public JsonObject MieleEnum;
        public String access;
    }

    public MieleBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the Miele bridge handler.");

        if (getConfig().get(HOST) != null && getConfig().get(INTERFACE) != null) {
            if (IP_PATTERN.matcher((String) getConfig().get(HOST)).matches()
                    && IP_PATTERN.matcher((String) getConfig().get(INTERFACE)).matches()) {

                try {
                    url = new URL("http://" + (String) getConfig().get(HOST) + "/remote/json-rpc");
                } catch (MalformedURLException e) {
                    logger.error("An exception occurred while defining an URL :'{}'", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
                    return;
                }

                // for future usage - no headers to be set for now
                headers = new HashMap<String, String>();

                onUpdate();
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.error("Invalid IP address for the Miele@Home gateway or multicast interface : '{}'/'{}'",
                        getConfig().get(HOST), getConfig().get(INTERFACE));
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to the Miele gateway. host IP address or multicast interface are not set.");
        }

    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            if (IP_PATTERN.matcher((String) getConfig().get(HOST)).matches()) {
                try {
                    if (isReachable((String) getConfig().get(HOST))) {
                        currentBridgeConnectionState = true;
                    } else {
                        currentBridgeConnectionState = false;
                        if (lastBridgeConnectionState) {
                            lastBridgeConnectionState = false;
                            onConnectionLost();
                        }
                    }

                    if (!lastBridgeConnectionState && currentBridgeConnectionState) {
                        logger.debug("Connection to Miele Gateway {} established.", getConfig().get(HOST));
                        lastBridgeConnectionState = true;
                        onConnectionResumed();
                    }

                    if (currentBridgeConnectionState) {
                        if (getThing().getStatus() == ThingStatus.ONLINE) {
                            List<HomeDevice> currentHomeDevices = getHomeDevices();
                            for (HomeDevice hd : currentHomeDevices) {
                                boolean isExisting = false;
                                for (HomeDevice phd : previousHomeDevices) {
                                    if (phd.UID.equals(hd.UID)) {
                                        isExisting = true;
                                        break;
                                    }
                                }
                                if (!isExisting) {
                                    logger.info("A new appliance with ID '{}' has been added", hd.UID);
                                    for (ApplianceStatusListener listener : applianceStatusListeners) {
                                        listener.onApplianceAdded(hd);
                                    }
                                }
                            }

                            for (HomeDevice hd : previousHomeDevices) {
                                boolean isCurrent = false;
                                for (HomeDevice chd : currentHomeDevices) {
                                    if (chd.UID.equals(hd.UID)) {
                                        isCurrent = true;
                                        break;
                                    }
                                }
                                if (!isCurrent) {
                                    logger.info("The appliance with ID '{}' has been removed", hd);
                                    for (ApplianceStatusListener listener : applianceStatusListeners) {
                                        listener.onApplianceRemoved(hd);
                                    }
                                }
                            }

                            previousHomeDevices = currentHomeDevices;

                            for (Thing appliance : getThing().getThings()) {
                                if (appliance.getStatus() == ThingStatus.ONLINE) {
                                    String UID = "hdm:ZigBee:"
                                            + (String) appliance.getConfiguration().getProperties().get(APPLIANCE_ID);

                                    Object[] args = new Object[2];
                                    args[0] = UID;
                                    args[1] = true;
                                    JsonElement result = invokeRPC("HDAccess/getDeviceClassObjects", args);

                                    if (result != null) {
                                        for (JsonElement obj : result.getAsJsonArray()) {
                                            try {
                                                DeviceClassObject dco = gson.fromJson(obj, DeviceClassObject.class);

                                                for (ApplianceStatusListener listener : applianceStatusListeners) {
                                                    listener.onApplianceStateChanged(UID, dco);
                                                }
                                            } catch (Exception e) {
                                                logger.error("An exception occurred while quering an appliance : '{}'",
                                                        e.getMessage());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    logger.error("An exception occurred while polling an appliance :'{}'", e.getMessage());
                }
            } else {
                logger.error("Invalid IP address for the Miele@Home gateway : '{}'", getConfig().get(HOST));
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

        List<HomeDevice> devices = new ArrayList<HomeDevice>();

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
                logger.error("An exception occurred while getting the home devices :'{}'", e.getMessage());
            }
        }
        return devices;
    }

    private Runnable eventListenerRunnable = new Runnable() {

        @Override
        public void run() {
            if (IP_PATTERN.matcher((String) getConfig().get(INTERFACE)).matches()) {
                while (true) {
                    // Get the address that we are going to connect to.
                    InetAddress address1 = null;
                    InetAddress address2 = null;
                    try {
                        address1 = InetAddress.getByName(JSON_RPC_MULTICAST_IP1);
                        address2 = InetAddress.getByName(JSON_RPC_MULTICAST_IP2);
                    } catch (UnknownHostException e) {
                        logger.error("An exception occurred while setting up the multicast receiver : '{}'",
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
                                    logger.debug("Received a multicast event '{}' from '{}:{}'",
                                            new Object[] { event, packet.getAddress(), packet.getPort() });

                                    DeviceProperty dp = new DeviceProperty();
                                    String uid = null;

                                    String[] parts = StringUtils.split(event, "&");
                                    for (String p : parts) {
                                        String[] subparts = StringUtils.split(p, "=");
                                        switch (subparts[0]) {
                                            case "property": {
                                                dp.Name = subparts[1];
                                                break;
                                            }
                                            case "value": {
                                                dp.Value = subparts[1];
                                                break;
                                            }
                                            case "id": {
                                                uid = subparts[1];
                                                break;
                                            }
                                        }
                                    }

                                    for (ApplianceStatusListener listener : applianceStatusListeners) {
                                        listener.onAppliancePropertyChanged(uid, dp);
                                    }
                                } catch (SocketTimeoutException e) {
                                    Thread.sleep(500);
                                }
                            }
                        } catch (Exception ex) {
                            logger.error("An exception occurred while receiving multicast packets : '{}'",
                                    ex.getMessage());
                        }

                        // restart the cycle with a clean slate
                        try {
                            if (clientSocket != null) {
                                clientSocket.leaveGroup(address1);
                                clientSocket.leaveGroup(address2);
                            }
                        } catch (IOException e) {
                            logger.error("An exception occurred while leaving multicast group : '{}'", e.getMessage());
                        }
                        if (clientSocket != null) {
                            clientSocket.close();
                        }
                    }
                }
            } else {
                logger.error("Invalid IP address for the multicast interface : '{}'", getConfig().get(INTERFACE));
            }

        }
    };

    public JsonElement invokeOperation(String UID, String modelID, String methodName) {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            Object[] args = new Object[4];
            args[0] = "hdm:ZigBee:" + UID;
            args[1] = "com.miele.xgw3000.gateway.hdm.deviceclasses.Miele" + modelID;
            args[2] = methodName;
            args[3] = null;
            return invokeRPC("HDAccess/invokeDCOOperation", args);
        } else {
            logger.warn("The Bridge is offline - operations can not be invoked.");
            return null;
        }
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
            logger.error("An exception occurred while posting data : '{}'", e.getMessage());
        }

        if (responseData != null) {
            logger.debug("The request '{}' yields '{}'", requestData, responseData);
            JsonParser parser = new JsonParser();
            JsonObject resp = (JsonObject) parser.parse(new StringReader(responseData));

            result = resp.get("result");
            JsonElement error = resp.get("error");

            if (error != null && !error.isJsonNull()) {
                if (error.isJsonPrimitive()) {
                    logger.error("A remote exception occurred : '{}'", error.getAsString());
                } else if (error.isJsonObject()) {
                    JsonObject o = error.getAsJsonObject();
                    Integer code = (o.has("code") ? o.get("code").getAsInt() : null);
                    String message = (o.has("message") ? o.get("message").getAsString() : null);
                    String data = (o.has("data") ? (o.get("data") instanceof JsonObject ? o.get("data").toString()
                            : o.get("data").getAsString()) : null);
                    logger.error("A remote exception occurred : '{}':'{}':'{}'", new Object[] { code, message, data });
                } else {
                    logger.error("An unknown remote exception occurred : '{}'", error.toString());
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
                logger.error("An unexpected status code was returned : '{}'", statusCode);
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
            eventListenerJob = scheduler.schedule(eventListenerRunnable, 0, TimeUnit.SECONDS);
        }
    }

    /**
     * This method is called whenever the connection to the given {@link MieleBridge} is lost.
     *
     */
    public void onConnectionLost() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
    }

    /**
     * This method is called whenever the connection to the given {@link MieleBridge} is resumed.
     *
     * @param bridge the hue bridge the connection is resumed to
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
    }
}
