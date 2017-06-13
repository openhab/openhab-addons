/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ikeatradfri.handler;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.pskstore.StaticPskStore;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.ikeatradfri.IkeaTradfriBulbConfiguration;
import org.openhab.binding.ikeatradfri.IkeaTradfriGatewayConfiguration;
import org.openhab.binding.ikeatradfri.internal.IkeaTradfriCallback;
import org.openhab.binding.ikeatradfri.internal.IkeaTradfriDiscoveryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link IkeaTradfriGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Sundberg - Initial contribution
 * @author Kai Kreuzer - refactorings
 */
public class IkeaTradfriGatewayHandler extends BaseBridgeHandler implements IkeaTradfriCallback {

    private Logger logger = LoggerFactory.getLogger(IkeaTradfriGatewayHandler.class);

    private DTLSConnector dtlsConnector;
    private CoapEndpoint endPoint;

    private static final JsonParser parser = new JsonParser();

    private List<IkeaTradfriDiscoveryListener> dataListeners = new CopyOnWriteArrayList<>();
    private Map<String, CoapObserveRelation> observeRelationMap = new ConcurrentHashMap<>();
    private Set<CoapClient> asyncClients = new HashSet<>();

    public IkeaTradfriGatewayHandler(Bridge bridge) {
        super(bridge);
        dtlsConnector = null;
        endPoint = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels on the gateway yet
    }

    @Override
    public void initialize() {
        IkeaTradfriGatewayConfiguration configuration = getConfigAs(IkeaTradfriGatewayConfiguration.class);
        if (configuration != null) {
            logger.debug("Initializing host: {}:{}", configuration.host, configuration.port);
            if (configuration.host.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Host is not set in the configuration");
                return;
            }
            if (configuration.code == null || configuration.code.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Security code must be set in the configuration!");
                return;
            }

            DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(new InetSocketAddress(0));
            builder.setPskStore(new StaticPskStore("", configuration.code.getBytes()));
            dtlsConnector = new DTLSConnector(builder.build());
            endPoint = new CoapEndpoint(dtlsConnector, NetworkConfig.getStandard());

            // we set it to UNKOWN as it can take up to a minute to figure out that observation does not work
            // (e.g. because of bad credentials)
            updateStatus(ThingStatus.UNKNOWN);

            logger.debug("Starting observe on gateway...");
            observe("15001", getThing().getUID().getId(), this);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IKEA Tradfri Gateway configuration is null");
            return;
        }
    }

    @Override
    public void dispose() {
        for (String id : observeRelationMap.keySet()) {
            observeRelationMap.get(id).proactiveCancel();
        }
        observeRelationMap.clear();

        for (CoapClient client : asyncClients) {
            client.shutdown();
        }
        asyncClients.clear();

        if (endPoint != null) {
            endPoint.destroy();
            endPoint = null;
        }
        if (dtlsConnector != null) {
            dtlsConnector.destroy();
            dtlsConnector = null;
        }
    }

    public CompletableFuture<String> coapGET(String url, IkeaTradfriCallback callback) {
        IkeaTradfriGatewayConfiguration configuration = getConfigAs(IkeaTradfriGatewayConfiguration.class);
        logger.debug("COAP GET: {}", url);
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            URI uri = new URI("coaps://" + configuration.host + ":" + configuration.port + "//" + url);
            CoapClient client = new CoapClient(uri);
            client.setEndpoint(endPoint);

            CoapHandler handler = new CoapHandler() {
                @Override
                public void onLoad(CoapResponse response) {
                    if (response.isSuccess()) {
                        String data = response.getResponseText();
                        logger.debug("COAP GET successful for: {}", url);
                        future.complete(data);
                        callback.setStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                    } else {
                        logger.debug("COAP GET error: {} for {}", response.getCode().toString(), url);
                        future.completeExceptionally(new RuntimeException("Response " + response.getCode().toString()));
                        callback.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    }
                    removeAsyncClient(client);
                }

                @Override
                public void onError() {
                    logger.debug("COAP GET error");
                    future.completeExceptionally(new RuntimeException("COAP GET resulted in an error."));
                    removeAsyncClient(client);
                    callback.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            };
            addAsyncClient(client);
            client.get(handler);
        } catch (URISyntaxException e) {
            future.completeExceptionally(e);
            callback.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return future;
    }

    public CompletableFuture<String> coapPUT(String url, String payload, IkeaTradfriCallback callback) {
        CompletableFuture<String> future = new CompletableFuture<>();
        IkeaTradfriGatewayConfiguration configuration = getConfigAs(IkeaTradfriGatewayConfiguration.class);
        try {
            logger.debug("COAP PUT {} to {}", payload, url);
            URI uri = new URI("coaps://" + configuration.host + ":" + configuration.port + "//" + url);
            CoapClient client = new CoapClient(uri);
            client.setEndpoint(endPoint);
            CoapHandler handler = new CoapHandler() {
                @Override
                public void onLoad(CoapResponse response) {
                    if (response.isSuccess()) {
                        logger.debug("COAP PUT successful to: {}", url);
                        future.complete(response.getResponseText());
                        callback.setStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                    } else {
                        logger.debug("COAP PUT error: {} for {}", response.getCode().toString(), url);
                        future.completeExceptionally(new RuntimeException("COAP PUT resulted in an error."));
                        callback.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    }
                    removeAsyncClient(client);
                }

                @Override
                public void onError() {
                    logger.debug("COAP PUT error");
                    future.completeExceptionally(new RuntimeException("COAP PUT resulted in an error."));
                    removeAsyncClient(client);
                    callback.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            };
            addAsyncClient(client);
            client.put(handler, payload, MediaTypeRegistry.TEXT_PLAIN);
            return future;
        } catch (URISyntaxException e) {
            logger.warn("COAP URI exception: {}", e.getMessage());
            future.completeExceptionally(e);
            callback.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return future;
    }

    private void addAsyncClient(CoapClient client) {
        asyncClients.add(client);
    }

    private void removeAsyncClient(CoapClient client) {
        client.shutdown();
        asyncClients.remove(client);
    }

    @Override
    public void onDataUpdate(JsonElement json) {
        logger.debug("Observation data: {}", json.toString());

        try {
            JsonArray array = json.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                deviceDiscoverHelper(array.get(i).getAsString());
            }
        } catch (JsonSyntaxException e) {
            logger.warn("JSON error: {}", e.getMessage());
        }
    }

    private void deviceDiscoverHelper(String deviceId) {
        coapGET("15001/" + deviceId, this).thenAccept(data -> {
            logger.debug("Got response {}\nListeners {}", data, dataListeners.size());
            // Trigger a new discovery of things
            JsonObject json2 = new JsonParser().parse(data).getAsJsonObject();
            for (IkeaTradfriDiscoveryListener dataListener : dataListeners) {
                dataListener.onDeviceFound(getThing().getUID(), json2);
            }
        });
    }

    private void observe(String url, String id, IkeaTradfriCallback callback) {
        logger.debug("Observing: {}", url);
        IkeaTradfriGatewayConfiguration configuration = getConfigAs(IkeaTradfriGatewayConfiguration.class);
        try {
            URI uri = new URI("coaps://" + configuration.host + ":" + configuration.port + "//" + url);

            CoapClient client = new CoapClient(uri);
            client.setEndpoint(endPoint);
            CoapHandler handler = new CoapHandler() {
                @Override
                public void onLoad(CoapResponse response) {
                    logger.debug("COAP Observe: \noptions: {}\npayload: {} ", response.getOptions().toString(),
                            response.getResponseText());
                    if (response.isSuccess()) {
                        try {
                            callback.onDataUpdate(parser.parse(response.getResponseText()));
                        } catch (JsonParseException e) {
                            logger.warn("Observed value not json: {}, {}", response.getResponseText(), e.getMessage());
                        }
                    } else {
                        logger.debug("COAP Observe Error: {} for {}", response.getCode().toString(), url);
                    }
                    callback.setStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                }

                @Override
                public void onError() {
                    logger.debug("COAP Observe error");
                    callback.setStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            };

            CoapObserveRelation relation = client.observe(handler);
            observeRelationMap.put(id, relation);

        } catch (URISyntaxException e) {
            logger.warn("COAP URL error: {}", e.getMessage());
        }
    }

    private void observeDevice(String id, IkeaTradfriCallback callback) {
        String url = "15001/" + id;
        observe(url, id, callback);
    }

    private void stopObserve(String id) {
        if (observeRelationMap.containsKey(id)) {
            CoapObserveRelation relation = observeRelationMap.get(id);
            relation.proactiveCancel();
            observeRelationMap.remove(id);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("Child handler initialized: {} handler: {}", childThing.getThingTypeUID().toString(),
                childHandler);
        if (childHandler instanceof IkeaTradfriCallback) {
            observeDevice(getId(childThing), (IkeaTradfriCallback) childHandler);
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        logger.debug("Child handler disposed: {}", childThing.getThingTypeUID().toString());
        stopObserve(getId(childThing));
    }

    public boolean registerDeviceListener(IkeaTradfriDiscoveryListener dataListener) {
        if (dataListener == null) {
            throw new IllegalArgumentException("It is not allowed to pass a null dataListener.");
        }
        return dataListeners.add(dataListener);
    }

    public boolean unregisterDeviceListener(IkeaTradfriDiscoveryListener dataListener) {
        if (dataListener == null) {
            throw new IllegalArgumentException("It is not allowed to pass a null dataListener.");
        }
        return dataListeners.remove(dataListener);
    }

    private String getId(Thing thing) {
        return Integer.toString(thing.getConfiguration().as(IkeaTradfriBulbConfiguration.class).id);
    }

    @Override
    public void setStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        updateStatus(status, statusDetail);
    }
}
