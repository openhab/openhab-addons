package org.openhab.binding.boschshc.internal;

import static org.eclipse.jetty.http.HttpMethod.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class BoschSHCBridgeHandler extends BaseBridgeHandler {

    public BoschSHCBridgeHandler(Bridge bridge) {
        super(bridge);

        String keystore = "/local/bosch-keystore";
        logger.warn("Starting with keystore at: {}", keystore);

        // Instantiate and configure the SslContextFactory
        // SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        SslContextFactory sslContextFactory = new SslContextFactory(true); // Accept all certificates

        // Keystore for managing the keys that have been used to pair with the SHC
        // https://www.eclipse.org/jetty/javadoc/9.4.12.v20180830/org/eclipse/jetty/util/ssl/SslContextFactory.html
        sslContextFactory.setKeyStorePath(keystore);
        sslContextFactory.setKeyStorePassword("123456");

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
    }

    private final Logger logger = LoggerFactory.getLogger(BoschSHCBridgeHandler.class);

    private @Nullable HttpClient httpClient;

    private @Nullable ArrayList<Room> rooms;
    private @Nullable ArrayList<Device> devices;

    private @Nullable String subscriptionId;

    @Override
    public void initialize() {

        config = getConfigAs(BoschSHCBridgeConfiguration.class);
        logger.warn("Initializating bridge: {} - HTTP client is: ", config.ipAddress, this.httpClient);

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

            ContentResponse contentResponse;
            try {
                logger.debug("Sending subscribe request to Bosch");

                String[] params = { "com/bosch/sh/remote/*", null }; // TODO Not sure about the tailing null, copied
                                                                     // from NodeJs
                JsonRpcRequest r = new JsonRpcRequest("2.0", "RE/subscribe", params);

                Gson gson = new Gson();
                String str_content = gson.toJson(r);

                logger.debug("Sending content: {}", str_content);

                contentResponse = this.httpClient.newRequest("https://" + config.ipAddress + ":8444/remote/json-rpc")
                        .header("Content-Type", "application/json").header("Accept", "application/json")
                        .header("Gateway-ID", "64-DA-A0-02-14-9B").method(POST)
                        .content(new StringContentProvider(str_content)).send();

                // Seems like this should yield something like:
                // content: [ [ '{"result":"e71k823d0-16","jsonrpc":"2.0"}\n' ] ]

                // The key can then be used later for longPoll like this:
                // body: [ [ '{"jsonrpc":"2.0","method":"RE/longPoll","params":["e71k823d0-16",20]}' ] ]

                String content = contentResponse.getContentAsString();
                logger.debug("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                SubscribeResult result = gson.fromJson(content, SubscribeResult.class);
                logger.info("Got subscription ID: {} {}", result.getResult(), result.getJsonrpc());

                this.subscriptionId = result.getResult();

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);

                this.subscriptionId = null;
            }
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

            this.subscribe();
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

                            for (DeviceStatusUpdate update : parsed.result) {

                                if (update != null && update.state != null) {

                                    logger.info("Got update: {} <- {}", update.deviceId, update.state.switchState);

                                    Bridge bridge = bridgeHandler.getThing();
                                    Thing thing = null;

                                    List<Thing> things = bridge.getThings();
                                    for (Thing childThing : things) {
                                        BoschSHCHandler handler = (BoschSHCHandler) childThing.getHandler();

                                        if (handler != null) {

                                            logger.debug("Registered device: {} - looking for {}", handler.getBoschID(),
                                                    update.deviceId);

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

                            logger.warn("Failed in onComplete");
                        }

                    } catch (Exception e) {

                        try {
                            // Check if we got a proper result from the SHC
                            LongPollError parsed = gson.fromJson(content, LongPollError.class);

                            if (parsed.error.code == LongPollError.SUBSCRIPTION_INVALID) {

                                // TODO Do we need to use atomic operations here?
                                bridgeHandler.subscriptionId = null;
                            }

                            logger.warn("Execption in long polling - result: {} - error: {}", parsed, e);

                        } catch (JsonSyntaxException jsonError) {
                            logger.warn(
                                    "Exception in onComplete - ignoring to avoid breaking long polling: {} - json result: {}",
                                    e, jsonError);
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
                                + "/services/PowerSwitch/state")
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
