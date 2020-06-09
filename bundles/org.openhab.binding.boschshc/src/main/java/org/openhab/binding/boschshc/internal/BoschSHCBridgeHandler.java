package org.openhab.binding.boschshc.internal;

import static org.eclipse.jetty.http.HttpMethod.*;

import java.io.File;
import java.lang.reflect.Type;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

//import org.bouncycastle.cert.X509CertificateHolder;
//import org.bouncycastle.cert.X509v1CertificateBuilder;
//import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
//import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
//import org.bouncycastle.operator.ContentSigner;
//import org.bouncycastle.operator.OperatorCreationException;
//import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class BoschSHCBridgeHandler extends BaseBridgeHandler {

    public BoschSHCBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    private final Logger logger = LoggerFactory.getLogger(BoschSHCBridgeHandler.class);

    private @Nullable HttpClient httpClient;

    private @Nullable ArrayList<Room> rooms;
    private @Nullable ArrayList<Device> devices;

    private @Nullable String subscriptionId;

    private SslContextFactory getSslContext(String keystore, String keystorePassword) {

        // Instantiate and configure the SslContextFactory
        // SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        SslContextFactory sslContextFactory = new SslContextFactory(true); // Accept all certificates

        // Keystore for managing the keys that have been used to pair with the SHC
        // https://www.eclipse.org/jetty/javadoc/9.4.12.v20180830/org/eclipse/jetty/util/ssl/SslContextFactory.html
        sslContextFactory.setKeyStorePath(keystore);
        sslContextFactory.setKeyStorePassword(keystorePassword);

        // Bosch is using a self signed certificate
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setValidateCerts(false);
        sslContextFactory.setValidatePeerCerts(false);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        return sslContextFactory;
    }

    // https://stackoverflow.com/questions/13894699/java-how-to-store-a-key-in-keystore
    public X509Certificate generateCertificate(KeyPair keyPair) {

        return null;
    }

    @Override
    public void initialize() {

        config = getConfigAs(BoschSHCBridgeConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {

            String keystore = config.keystorePath;
            String keystorePassword = config.keystorePassword;

            logger.warn("Starting with keystore at: {}", keystore);

            try {
                if (!new File(keystore).exists()) {

                    logger.warn("Initial pairing for the smart home controller not yet supported: {}");
                    updateStatus(ThingStatus.OFFLINE);

                    throw new UnsupportedOperationException();
                }

                // Instantiate HttpClient with the SslContextFactory
                this.httpClient = new HttpClient(this.getSslContext(keystore, keystorePassword));

                try {
                    this.httpClient.start();
                } catch (Exception e) {
                    logger.warn("Failed to start http client from: {}", keystore, e);
                }

                logger.warn("Initializing bridge: {} - HTTP client is: {} - version: 2020-04-05", config.ipAddress,
                        this.httpClient);

                Boolean thingReachable = true;
                thingReachable &= this.getRooms();
                thingReachable &= this.getDevices();

                if (thingReachable) {
                    updateStatus(ThingStatus.ONLINE);

                    // Start long polling to receive updates from Bosch SHC.
                    this.longPoll();

                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }

            } catch (Exception e) {
                logger.warn("Failed to initialize Bosch Smart Home Controller: {}", e);
                updateStatus(ThingStatus.OFFLINE);
            }

        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Handle command on bridge: {}", config.ipAddress);

    }

    private @Nullable Room getRoomForDevice(Device d) {

        if (this.rooms != null) {

            for (Room r : this.rooms) {

                if (r.id.equals(d.roomId)) {
                    return r;
                }
            }
        }

        return null;
    }

    /**
     * Get a list of connected devices from the Smart-Home Controller
     */
    private Boolean getDevices() {

        if (this.httpClient != null) {

            ContentResponse contentResponse;
            try {
                logger.debug("Sending http request to Bosch to request clients: {}", config.ipAddress);
                contentResponse = this.httpClient.newRequest("https://" + config.ipAddress + ":8444/smarthome/devices")
                        .header("Content-Type", "application/json").header("Accept", "application/json").method(GET)
                        .send();

                String content = contentResponse.getContentAsString();
                logger.debug("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                Gson gson = new GsonBuilder().create();
                Type collectionType = new TypeToken<ArrayList<Device>>() {
                }.getType();
                this.devices = gson.fromJson(content, collectionType);

                if (this.devices != null) {
                    for (Device d : this.devices) {
                        // TODO keeping these as warn for the time being, until we have a better means
                        // of listing
                        // devices with their Bosch ID
                        logger.warn("Found device: name={} id={}", d.name, d.id);
                        if (d.deviceSerivceIDs != null) {
                            for (String s : d.deviceSerivceIDs) {
                                logger.warn(".... service: " + s);
                            }
                        }
                    }
                }

                return true;

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
                return false;
            }
        } else {

            return false;
        }
    }

    /**
     * Subscribe to events and store the subscription ID needed for long polling
     *
     * Method is synchronous.
     */
    private void subscribe() {

        if (this.httpClient != null) {

            logger.info("Sending subscribe request to Bosch");

            String[] params = { "com/bosch/sh/remote/*", null }; // TODO Not sure about the tailing null, copied
                                                                 // from NodeJs
            JsonRpcRequest r = new JsonRpcRequest("2.0", "RE/subscribe", params);

            Gson gson = new Gson();
            String str_content = gson.toJson(r);

            // XXX Maybe we should use a different httpClient here, to avoid a race with
            // concurrent use from other
            // functions.
            logger.info("Subscribe: Sending content: {} - using httpClient {}", str_content, this.httpClient);

            class SubscribeListener extends BufferingResponseListener {
                private BoschSHCBridgeHandler bridgeHandler;

                public SubscribeListener(BoschSHCBridgeHandler bridgeHandler) {

                    super();
                    this.bridgeHandler = bridgeHandler;
                }

                @Override
                public void onComplete(@Nullable Result result) {

                    // Seems like this should yield something like:
                    // content: [ [ '{"result":"e71k823d0-16","jsonrpc":"2.0"}\n' ] ]

                    // The key can then be used later for longPoll like this:
                    // body: [ [
                    // '{"jsonrpc":"2.0","method":"RE/longPoll","params":["e71k823d0-16",20]}' ] ]

                    byte[] responseContent = getContent();
                    String content = new String(responseContent);

                    logger.info("Subscribe: response complete: {} - return code: {}", content,
                            result.getResponse().getStatus());

                    SubscribeResult subscribeResult = gson.fromJson(content, SubscribeResult.class);
                    logger.info("Subscribe: Got subscription ID: {} {}", subscribeResult.getResult(),
                            subscribeResult.getJsonrpc());

                    bridgeHandler.subscriptionId = subscribeResult.getResult();
                    longPoll();
                }
            }

            this.httpClient.newRequest("https://" + config.ipAddress + ":8444/remote/json-rpc")
                    .header("Content-Type", "application/json").header("Accept", "application/json")
                    .header("Gateway-ID", "64-DA-A0-02-14-9B").method(POST) // TODO What's this Gateway ID
                    .content(new StringContentProvider(str_content)).send(new SubscribeListener(this));

        }

    }

    /**
     * Long polling
     *
     * TODO Do we need to protect against concurrent execution of this method via
     * locks etc?
     *
     * If no subscription ID is present, this function will first try to acquire
     * one. If that fails, it will attempt to retry after a small timeout.
     *
     * Return whether to retry getting a new subscription and restart polling.
     */
    private void longPoll() {
        /*
         * // TODO Change hard-coded Gateway ID // TODO Change hard-coded port
         */

        if (this.subscriptionId == null) {

            logger.info("longPoll: Subscription outdated, requesting .. ");
            this.subscribe();
            return;
        }

        if (this.httpClient != null && this.subscriptionId != null) {

            logger.debug("Sending long poll request to Bosch");

            String[] params = { this.subscriptionId, "20" };
            JsonRpcRequest r = new JsonRpcRequest("2.0", "RE/longPoll", params);

            Gson gson = new Gson();
            String str_content = gson.toJson(r);

            logger.debug("Sending content: {}", str_content);

            /**
             * TODO Move this to separate file?
             */
            class LongPollListener extends BufferingResponseListener {

                private BoschSHCBridgeHandler bridgeHandler;

                public LongPollListener(BoschSHCBridgeHandler bridgeHandler) {

                    super();
                    this.bridgeHandler = bridgeHandler;
                }

                @Override
                public void onComplete(@Nullable Result result) {

                    String content = null;

                    try {
                        if (result != null && !result.isFailed()) {

                            byte[] responseContent = getContent();
                            content = new String(responseContent);

                            logger.debug("Response complete: {} - return code: {}", content,
                                    result.getResponse().getStatus());

                            LongPollResult parsed = gson.fromJson(content, LongPollResult.class);
                            if (parsed.result != null) {
                                for (DeviceStatusUpdate update : parsed.result) {

                                    if (update != null && update.state != null) {

                                        logger.info("Got update for {}", update.deviceId);

                                        Bridge bridge = bridgeHandler.getThing();
                                        boolean handled = false;

                                        for (Thing childThing : bridge.getThings()) {

                                            // All children of this should implement BoschSHCHandler
                                            ThingHandler baseHandler = childThing.getHandler();
                                            if (BoschSHCHandler.class.isInstance(baseHandler)) {
                                                BoschSHCHandler handler = (BoschSHCHandler) baseHandler;

                                                if (handler != null) {

                                                    handled = true;
                                                    logger.debug("Registered device: {} - looking for {}",
                                                            handler.getBoschID(), update.deviceId);

                                                    if (update.deviceId.equals(handler.getBoschID())) {

                                                        logger.info("Found child: {} - calling processUpdate with {}",
                                                                handler, update.state);
                                                        handler.processUpdate(update.id, update.state);
                                                    }
                                                } else {
                                                    logger.warn("longPoll: handler is null");
                                                }
                                            } else {
                                                logger.warn(
                                                        "longPoll: child handler for {} does not implement Bosch SHC handler",
                                                        baseHandler);
                                            }

                                        }

                                        if (!handled) {
                                            logger.info("Could not find a thing for device ID: {}", update.deviceId);
                                        }
                                    }
                                }

                            } else {
                                logger.warn("Could not parse in onComplete: {}", content);

                                // Check if we got a proper result from the SHC
                                LongPollError parsedError = gson.fromJson(content, LongPollError.class);

                                if (parsedError.error != null) {

                                    logger.warn("Got error from SHC: {}", parsedError.error.hashCode());

                                    if (parsedError.error.code == LongPollError.SUBSCRIPTION_INVALID) {

                                        bridgeHandler.subscriptionId = null;
                                        logger.warn("Invalidating subscription ID!");
                                    }
                                }

                                // Timeout before retry
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException sleepError) {
                                    logger.warn("Failed to sleep in longRun()");
                                }
                            }

                        } else {
                            logger.warn("Failed in onComplete");
                        }

                    } catch (Exception e) {

                        logger.warn("Execption in long polling - error: {}", e);

                        // Timeout before retry
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException sleepError) {
                            logger.warn("Failed to sleep in longRun()");
                        }
                    }

                    // TODO Is this call okay? Should we use scheduler.execute instead?
                    bridgeHandler.longPoll();
                }
            }

            this.httpClient.newRequest("https://" + config.ipAddress + ":8444/remote/json-rpc")
                    .header("Content-Type", "application/json").header("Accept", "application/json")
                    .header("Gateway-ID", "64-DA-A0-02-14-9B").method(POST)
                    .content(new StringContentProvider(str_content)).send(new LongPollListener(this));

        } else {

            logger.warn("Unable to long poll. Subscription ID or http client undefined.");

            // Timeout before retry
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.warn("Failed to sleep in longRun()");
            }
        }

    }

    /**
     * Get a list of rooms from the Smart-Home controller
     */
    private Boolean getRooms() {

        if (this.httpClient != null) {

            ContentResponse contentResponse;
            try {
                logger.debug("Sending http request to Bosch to request rooms");
                contentResponse = this.httpClient.newRequest("https://" + config.ipAddress + ":8444/smarthome/rooms")
                        .header("Content-Type", "application/json").header("Accept", "application/json").method(GET)
                        .send();

                String content = contentResponse.getContentAsString();
                logger.info("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                Gson gson = new GsonBuilder().create();
                Type collectionType = new TypeToken<ArrayList<Room>>() {
                }.getType();

                this.rooms = gson.fromJson(content, collectionType);

                if (this.rooms != null) {
                    for (Room r : this.rooms) {
                        logger.warn("Found room: {}", r.name);
                    }
                }

                return true;

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
                return false;
            }
        } else {

            return false;
        }
    }

    private BoschSHCBridgeConfiguration config;

    /**
     * Query the Bosch Smart Home Controller for the state of the given thing.
     *
     * @param thing     Thing to query the device state for
     * @param stateName Name of the state to query
     * @param classOfT  Class to convert the resulting JSON to
     */

    public <T extends Object> T refreshState(@NonNull Thing thing, String stateName, Class<T> classOfT) {

        BoschSHCHandler handler = (BoschSHCHandler) thing.getHandler();

        if (this.httpClient != null && handler != null) {

            ContentResponse contentResponse;
            try {

                String boschID = handler.getBoschID();
                String url = "https://" + config.ipAddress + ":8444/smarthome/devices/" + boschID + "/services/"
                        + stateName + "/state";

                logger.warn("refreshState: Requesting \"{}\" from Bosch: {} via {}", stateName, boschID, url);

                // GET request
                // ----------------------------------------------------------------------------------

                // TODO: Gateway ID is hard-coded!
                contentResponse = this.httpClient.newRequest(url).header("Content-Type", "application/json")
                        .header("Accept", "application/json").header("Gateway-ID", "64-DA-A0-02-14-9B").method(GET)
                        .send();

                String content = contentResponse.getContentAsString();
                logger.warn("refreshState: Request complete: [{}] - return code: {}", content,
                        contentResponse.getStatus());

                // TODO Perhaps we should have only one single gson builder per thing?
                Gson gson = new GsonBuilder().create();

                T state = gson.fromJson(content, classOfT);
                return state;

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("refreshState: HTTP request failed: {}", e);
            }
        }

        return null;
    }

    /**
     * Sends a state change for a device to the controller
     * 
     * @param deviceId    Id of device to change state for
     * @param serviceName Name of service of device to change state for
     * @param state       New state data to set for service
     */
    public <T extends Object> void putState(@NonNull String deviceId, String serviceName, T state) {
        if (this.httpClient == null) {
            return;
        }

        // Create request
        String url = "https://" + config.ipAddress + ":8444/smarthome/devices/" + deviceId + "/services/" + serviceName
                + "/state";
        Gson gson = new Gson();
        String body = gson.toJson(state);
        Request request = this.httpClient.newRequest(url).header("Content-Type", "application/json")
                .header("Accept", "application/json").header("Gateway-ID", "64-DA-A0-02-14-9B").method(PUT)
                .content(new StringContentProvider(body));

        // Send request
        try {
            request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("HTTP request failed: {}", e);
        }
    }

    /*
     * TODO: The only place from which we currently send updates is the PowerSwitch,
     * might have to extend this over time if we want to enable the alarm system
     * etc.
     */
    public void updateSwitchState(@NonNull Thing thing, String command) {

        BoschSHCHandler handler = (BoschSHCHandler) thing.getHandler();

        if (this.httpClient != null && handler != null) {

            ContentResponse contentResponse;
            try {

                String boschID = handler.getBoschID();
                logger.info("Sending update request to Bosch device {}: update: {}", boschID, command);

                // PUT request
                // ----------------------------------------------------------------------------------

                // From:
                // https://github.com/philbuettner/bosch-shc-api-docs/blob/90913cc8a6fe5f322c0d819d269566e8e3708080/postman/Bosch%20Smart%20Home%20v0.3.postman_collection.json#L949
                // TODO This should be different for other kinds of devices.
                PowerSwitchStateUpdate state = new PowerSwitchStateUpdate("powerSwitchState", command);

                Gson gson = new Gson();
                String str_content = gson.toJson(state);

                // hdm:HomeMaticIP:3014F711A0001916D859A8A9
                logger.warn("Sending content: {}", str_content);

                // TODO Path should be different for other kinds of device updates
                contentResponse = this.httpClient
                        .newRequest("https://" + config.ipAddress + ":8444/smarthome/devices/" + boschID
                                + "/services/PowerSwitch/state")
                        .header("Content-Type", "application/json").header("Accept", "application/json")
                        .header("Gateway-ID", "64-DA-A0-02-14-9B").method(PUT)
                        .content(new StringContentProvider(str_content)).send();

                String content = contentResponse.getContentAsString();
                logger.info("Response complete: [{}] - return code: {}", content, contentResponse.getStatus());

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
            }
        }
    }

}
