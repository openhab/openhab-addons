package org.openhab.binding.boschshc.internal;

import static org.eclipse.jetty.http.HttpMethod.*;

import java.io.File;
import java.lang.reflect.Type;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.spec.RSAPrivateKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
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

    // This doesn't have to be too save. After all, it just encrypts the Java keystore stored on the local disk. And
    // that
    // keystore only stores a key for accessing the SHC.
    private static String KEYSTORE_PASSWORD = "not-very-save";

    // Path to use for pairing requests.
    private static String PATH_PAIRING = "/smarthome/clients";

    private final Logger logger = LoggerFactory.getLogger(BoschSHCBridgeHandler.class);

    private @Nullable HttpClient httpClient;

    private @Nullable ArrayList<Room> rooms;
    private @Nullable ArrayList<Device> devices;

    private @Nullable String subscriptionId;

    private SslContextFactory getSslContext(String keystore) {

        // Instantiate and configure the SslContextFactory
        // SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        SslContextFactory sslContextFactory = new SslContextFactory(true); // Accept all certificates

        // Keystore for managing the keys that have been used to pair with the SHC
        // https://www.eclipse.org/jetty/javadoc/9.4.12.v20180830/org/eclipse/jetty/util/ssl/SslContextFactory.html
        sslContextFactory.setKeyStorePath(keystore);
        sslContextFactory.setKeyStorePassword(KEYSTORE_PASSWORD);

        // Bosch is using a self signed certificate
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setValidateCerts(false);
        sslContextFactory.setValidatePeerCerts(false);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        return sslContextFactory;
    }

    private void pair(String keystore) throws Exception {

        // Instantiate HttpClient with the SslContextFactory
        HttpClient httpClient = new HttpClient(getSslContext(keystore));
        httpClient.start();

        // Attempt connecting to the SHC

        // There is probably going to be some sort of loop here. Because the user has to press the button on the
        // Smart
        // Home Controller during setup procedure.

        boolean pairingSuccessful = false;
        int maxTries = 100;
        int currTries = 0;

        while (!pairingSuccessful && currTries < maxTries) {
            ContentResponse contentResponse;
            try {

                PairRequest r = new PairRequest("OpenHab Test", "openhabtest");

                Gson gson = new Gson();
                String str_content = gson.toJson(r);

                String base64password = new String(Base64.getEncoder().encode(this.config.password.getBytes()));

                logger.debug("pairing: Sending http request to Bosch to request clients: {}", config.ipAddress);
                contentResponse = httpClient
                        .newRequest("https://" + config.ipAddress + ":8444/" + BoschSHCBridgeHandler.PATH_PAIRING)
                        .header("Content-Type", "application/json").header("Accept", "application/json")
                        .header("Systempassword", base64password).method(POST)
                        .content(new StringContentProvider(str_content)).send();

                String content = contentResponse.getContentAsString();
                logger.info("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                // TODO Mark pairing as successful after a while.

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
            }

            maxTries++;
        }

    }

    @Override
    public void initialize() {

        config = getConfigAs(BoschSHCBridgeConfiguration.class);

        String keystore = config.keystorePath;
        logger.warn("Starting with keystore at: {}", keystore);

        if (!new File(keystore).exists()) {

            logger.warn("Keystore for connecting to Bosch SHC does not exists yet, parining .. ");

            // 0. Download BSHC certificate
            // --------------------------------------------------
            // Might have to import the BSHC's self-signed certificate into the keystore too.
            // Can download it from:
            // - https://<ip-of-bshc>:8444/smarthome/rooms
            // Alternatively, it should be possible to allow unvalidated access in the Jetty instance.

            // Documentation from:
            // --------------------------------------------------
            // - https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html
            // - http://tutorials.jenkov.com/java-cryptography/keystore.html

            // Load empty keystore
            // --------------------------------------------------
            try {

                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                // Need "null" here to generate a new keystore instead of opening an existing one
                ks.load(null, KEYSTORE_PASSWORD.toCharArray());

                // Maybe the first step is to peek into the existing keystore and see what's there?

                // Generate RSA key
                // --------------------------------------------------
                // Function pairClient in bosch-smart-home-bridge.ts and according to client-key.ts, should be RSA
                // private
                // key (see ~/projects/smart-home/bosch-smart-home-bridge)
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(2048);

                KeyPair kp = kpg.genKeyPair();
                KeyFactory fact = KeyFactory.getInstance("RSA");

                // TODO: Not sure if we really need both?
                // RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
                RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
                Key privateKey = (Key) priv;

                // Store RSA key
                ks.setKeyEntry("boschshc", privateKey, BoschSHCBridgeHandler.KEYSTORE_PASSWORD.toCharArray(), null);

                // Store the keystore to a new file
                // --------------------------------------------------
                java.io.FileOutputStream fos = null;
                try {
                    fos = new java.io.FileOutputStream(keystore);
                    ks.store(fos, KEYSTORE_PASSWORD.toCharArray());
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }

                // Register the key to the Bosch SHC.
                // TODO

                pair(keystore);
            } catch (Exception e) {

                logger.warn("Pairing failed! {}", e);
            }

            System.exit(1);
        }

        // Instantiate and configure the SslContextFactory
        // SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        SslContextFactory sslContextFactory = new SslContextFactory(true); // Accept all certificates

        // Keystore for managing the keys that have been used to pair with the SHC
        // https://www.eclipse.org/jetty/javadoc/9.4.12.v20180830/org/eclipse/jetty/util/ssl/SslContextFactory.html
        sslContextFactory.setKeyStorePath(keystore);
        sslContextFactory.setKeyStorePassword(KEYSTORE_PASSWORD);

        // Bosch is using a self signed certificate
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setValidateCerts(false);
        sslContextFactory.setValidatePeerCerts(false);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        // Instantiate HttpClient with the SslContextFactory
        this.httpClient = new HttpClient(sslContextFactory);

        try {
            this.httpClient.start();
        } catch (Exception e) {
            logger.warn("Failed to start http client from: {}", keystore, e);
        }

        logger.warn("Initializing bridge: {} - HTTP client is: {} - version: 2020-02-20", config.ipAddress,
                this.httpClient);

        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {

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
                logger.info("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                Gson gson = new GsonBuilder().create();
                Type collectionType = new TypeToken<ArrayList<Device>>() {
                }.getType();
                this.devices = gson.fromJson(content, collectionType);

                if (this.devices != null) {
                    for (Device d : this.devices) {
                        Room room = this.getRoomForDevice(d);

                        // TODO keeping these as warn for the time being, until we have a better means of listing
                        // devices with their Bosch ID
                        logger.warn("Found device: name={} room={} id={}", d.name, room != null ? room.name : "", d.id);
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

            // XXX Maybe we should use a different httpClient here, to avoid a race with concurrent use from other
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
                    // body: [ [ '{"jsonrpc":"2.0","method":"RE/longPoll","params":["e71k823d0-16",20]}' ] ]

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
     * TODO Do we need to protect against concurrent execution of this method via locks etc?
     *
     * If no subscription ID is present, this function will first try to acquire one. If that fails, it will attempt to
     * retry after a small timeout.
     *
     * Return whether to retry getting a new subscription and restart polling.
     */
    private void longPoll() {
        /*
         * // TODO Change hard-coded Gateway ID
         * // TODO Change hard-coded IP address
         * // TODO Change hard-coded port
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

                            logger.info("Response complete: {} - return code: {}", content,
                                    result.getResponse().getStatus());

                            LongPollResult parsed = gson.fromJson(content, LongPollResult.class);
                            if (parsed.result != null) {
                                for (DeviceStatusUpdate update : parsed.result) {

                                    if (update != null && update.state != null) {

                                        logger.info("Got update: {} <- {}", update.deviceId, update.state.switchState);

                                        Bridge bridge = bridgeHandler.getThing();
                                        Thing thing = null;

                                        List<Thing> things = bridge.getThings();
                                        for (Thing childThing : things) {
                                            BoschSHCHandler handler = (BoschSHCHandler) childThing.getHandler();

                                            if (handler != null) {

                                                logger.debug("Registered device: {} - looking for {}",
                                                        handler.getBoschID(), update.deviceId);

                                                if (update.deviceId.equals(handler.getBoschID())) {
                                                    thing = childThing;
                                                }
                                            }

                                        }

                                        // TODO Probably should check if it is in fact, the correct handler. Depends a
                                        // little
                                        // one
                                        // whether we add more of them or if we just have one Handler for all devices.
                                        if (thing != null) {

                                            BoschSHCHandler thingHandler = (BoschSHCHandler) thing.getHandler();

                                            if (thingHandler != null) {
                                                thingHandler.processUpdate(update);
                                            } else {
                                                logger.warn("Could not convert thing handler to BoschSHCHandler");
                                            }
                                        } else {
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
                contentResponse = this.httpClient
                        .newRequest("https://" + config.ipAddress + ":8444/smarthome/remote/json-rpc")
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
     * Query the Bosch Smart Home Controller for the current power switch state.
     *
     * @param thing The thing to query the device state for
     */
    public DeviceState refreshSwitchState(@NonNull Thing thing) {

        BoschSHCHandler handler = (BoschSHCHandler) thing.getHandler();

        if (this.httpClient != null && handler != null) {

            ContentResponse contentResponse;
            try {

                String boschID = handler.getBoschID();
                logger.debug("Requesting state update from Bosch: {} via {}", boschID, config.ipAddress);

                // GET request
                // ----------------------------------------------------------------------------------

                // TODO: PowerSwitch is hard-coded
                contentResponse = this.httpClient
                        .newRequest("https://" + config.ipAddress + ":8444/smarthome/devices/" + boschID
                                + ")/services/PowerSwitch/state")
                        .header("Content-Type", "application/json").header("Accept", "application/json")
                        .header("Gateway-ID", "64-DA-A0-02-14-9B").method(GET).send();

                String content = contentResponse.getContentAsString();
                logger.info("Refresh switch state request complete: [{}] - return code: {}", content,
                        contentResponse.getStatus());

                Gson gson = new GsonBuilder().create();

                DeviceState state = gson.fromJson(content, DeviceState.class);
                return state;

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
            }
        }

        return null;
    }

    /*
     * {{shc_api}}/devices/{{device_id}}/services/PowerSwitch/state
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
