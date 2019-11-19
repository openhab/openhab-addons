/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal;

import static org.eclipse.jetty.http.HttpMethod.*;
import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.THING_TYPE_SAMPLE;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link BoschSHCHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.boschshc", service = ThingHandlerFactory.class)
public class BoschSHCHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(BoschSHCHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SAMPLE);
    private @Nullable HttpClient httpClient;

    private @Nullable ArrayList<Room> rooms;
    private @Nullable ArrayList<Device> devices;

    private @Nullable String subscriptionId;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SAMPLE.equals(thingTypeUID)) {

            logger.warn("Thing createHandler - http client is: {}", this.httpClient);

            // TODO Make this an asynchronous request
            // TODO Don't think we need to disable all these checks here.

            // Instantiate and configure the SslContextFactory
            // SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
            SslContextFactory sslContextFactory = new SslContextFactory(true); // Accept all certificates

            // Keystore for managing the keys that have been used to pair with the SHC
            // https://www.eclipse.org/jetty/javadoc/9.4.12.v20180830/org/eclipse/jetty/util/ssl/SslContextFactory.html
            sslContextFactory.setKeyStorePath("/home/skaestle/projects/smart-home/bosch/keystore");
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
                logger.warn("Failed to start http client", e);
            }

            this.getRooms();
            this.getDevices();
            this.subscribe();

            for (int i = 0; i < 10; i++) {
                this.longPoll();
            }

            return new BoschSHCHandler(thing);
        }

        return null;
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
    private void getDevices() {

        if (this.httpClient != null) {

            ContentResponse contentResponse;
            try {
                logger.warn("Sending http request to Bosch to request clients");
                contentResponse = this.httpClient.newRequest("https://192.168.178.128:8444/smarthome/devices")
                        .header("Content-Type", "application/json").header("Accept", "application/json").method(GET)
                        .send();

                String content = contentResponse.getContentAsString();
                logger.warn("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                Gson gson = new GsonBuilder().create();
                Type collectionType = new TypeToken<ArrayList<Device>>() {
                }.getType();
                this.devices = gson.fromJson(content, collectionType);

                if (this.devices != null) {
                    for (Device d : this.devices) {
                        Room room = this.getRoomForDevice(d);
                        logger.warn("Found device: name={} room={} id={}", d.name, room != null ? room.name : "", d.id);
                    }
                }

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
            }
        }
    }

    /**
     * Subscribe to events and store the subscription ID needed for long polling
     *
     */
    private void subscribe() {

        if (this.httpClient != null) {

            ContentResponse contentResponse;
            try {
                logger.warn("Sending subscribe request to Bosch");

                String[] params = { "com/bosch/sh/remote/*", null }; // TODO Not sure about the tailing null, copied
                                                                     // from NodeJs
                JsonRpcRequest r = new JsonRpcRequest("2.0", "RE/subscribe", params);

                Gson gson = new Gson();
                String str_content = gson.toJson(r);

                logger.warn("Sending content: {}", str_content);

                contentResponse = this.httpClient.newRequest("https://192.168.178.128:8444/remote/json-rpc")
                        .header("Content-Type", "application/json").header("Accept", "application/json")
                        .header("Gateway-ID", "64-DA-A0-02-14-9B").method(POST)
                        .content(new StringContentProvider(str_content)).send();

                // Seems like this should yield something like:
                // content: [ [ '{"result":"e71k823d0-16","jsonrpc":"2.0"}\n' ] ]

                // The key can then be used later for longPoll like this:
                // body: [ [ '{"jsonrpc":"2.0","method":"RE/longPoll","params":["e71k823d0-16",20]}' ] ]

                String content = contentResponse.getContentAsString();
                logger.warn("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                SubscribeResult result = gson.fromJson(content, SubscribeResult.class);
                logger.warn("Got subscription ID: {} {}", result.getResult(), result.getJsonrpc());

                this.subscriptionId = result.getResult();

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
            }
        }
    }

    /**
     * Long polling
     *
     */
    private void longPoll() {
        /*
         * // TODO Change hard-coded Gateway ID
         * // TODO Change hard-coded IP address
         * // TODO Change hard-coded port
         */

        if (this.httpClient != null && this.subscriptionId != null) {

            ContentResponse contentResponse;
            try {
                logger.warn("Sending long poll request to Bosch");

                String[] params = { this.subscriptionId, "20" };
                JsonRpcRequest r = new JsonRpcRequest("2.0", "RE/longPoll", params);

                Gson gson = new Gson();
                String str_content = gson.toJson(r);

                logger.warn("Sending content: {}", str_content);

                contentResponse = this.httpClient.newRequest("https://192.168.178.128:8444/remote/json-rpc")
                        .header("Content-Type", "application/json").header("Accept", "application/json")
                        .header("Gateway-ID", "64-DA-A0-02-14-9B").method(POST)
                        .content(new StringContentProvider(str_content)).send();

                // Seems like this should yield something like:
                // content: [ [ '{"result":"e71k823d0-16","jsonrpc":"2.0"}\n' ] ]

                // The key can then be used later for longPoll like this:
                // body: [ [ '{"jsonrpc":"2.0","method":"RE/longPoll","params":["e71k823d0-16",20]}' ] ]

                String content = contentResponse.getContentAsString();
                logger.warn("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                LongPollResult result = gson.fromJson(content, LongPollResult.class);

                for (DeviceStatusUpdate update : result.result) {

                    logger.warn("Got update: {} <- {}", update.deviceId, update.state.switchState);
                }

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
            }
        }
    }

    /**
     * Get a list of rooms from the Smart-Home controller
     */
    private void getRooms() {

        if (this.httpClient != null) {

            ContentResponse contentResponse;
            try {
                logger.warn("Sending http request to Bosch to request rooms");
                contentResponse = this.httpClient.newRequest("https://192.168.178.128:8444/smarthome/remote/json-rpc")
                        .header("Content-Type", "application/json").header("Accept", "application/json").method(GET)
                        .send();

                String content = contentResponse.getContentAsString();
                logger.warn("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                Gson gson = new GsonBuilder().create();
                Type collectionType = new TypeToken<ArrayList<Room>>() {
                }.getType();

                this.rooms = gson.fromJson(content, collectionType);

                if (this.rooms != null) {
                    for (Room r : this.rooms) {
                        logger.warn("Found room: {}", r.name);
                    }
                }

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
            }
        }
    }

}
