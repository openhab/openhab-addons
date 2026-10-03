/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.philipsair.internal.discovery;

import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.PROPERTY_DEV_TYPE;
import static org.openhab.core.thing.Thing.*;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.EmptyMessage;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.interceptors.MessageInterceptor;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.connection.CoapMessageLogger;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link PhilipsAirCoapDiscovery} is responsible for discovering
 * new Philips Air Purifier things for COAP protocol devices
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.philipsair")
public class PhilipsAirCoapDiscovery extends AbstractDiscoveryService {
    private static final int DISCOVERY_TIME = 15;
    private static final String PATH = "sys/dev/info";
    private static final int COAP_PORT = 5683;
    private static final long BACKGROUND_DISCOVERY_INTERVAL = 3600;

    private final Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirCoapDiscovery.class);
    private final CoapClient client;
    private final CoapEndpoint endpoint;
    private @Nullable ScheduledFuture<?> coapDiscoveryJob;
    private final NetworkAddressService networkAddressService;

    @Activate
    public PhilipsAirCoapDiscovery(@Reference NetworkAddressService networkAddressService) {
        super(DISCOVERY_TIME);

        CoapConfig.register();

        // a copy, as the standard configuration is shared with the other bindings using Californium
        Configuration netConfig = new Configuration(Configuration.getStandard()).set(CoapConfig.RESPONSE_MATCHING,
                CoapConfig.MatcherMode.RELAXED); // allow many responders

        endpoint = new CoapEndpoint.Builder().setConfiguration(netConfig).build();
        if (logger.isTraceEnabled()) {
            MessageInterceptor interceptor = new CoapMessageLogger();
            endpoint.addInterceptor(interceptor);
        }

        // Californium does not match all responses of a multicast request to the request, so the interceptor, which
        // sees every response, is the only place where the responses are processed
        endpoint.addInterceptor(new MessageInterceptor() {
            @Override
            public void receiveResponse(@Nullable Response response) {
                if (response == null) {
                    logger.debug("Response is null");
                    return;
                }
                if (response.getPayload() != null) {
                    String payload = response.getPayloadString();
                    InetSocketAddress src = response.getSourceContext().getPeerAddress();
                    logger.trace("Received coap response from {}  - {}", src, Utils.prettyPrint(response));
                    if (payload != null && !payload.isBlank()) {
                        PhilipsAirCoapDiscovery.this.discovered(payload, src.getHostString());
                    }
                }
            }

            @Override
            public void sendRequest(@Nullable Request request) {
            }

            @Override
            public void sendResponse(@Nullable Response response) {
            }

            @Override
            public void sendEmptyMessage(@Nullable EmptyMessage message) {
            }

            @Override
            public void receiveRequest(@Nullable Request request) {
            }

            @Override
            public void receiveEmptyMessage(@Nullable EmptyMessage message) {
            }
        });

        this.networkAddressService = networkAddressService;
        this.client = new CoapClient();
        this.client.setEndpoint(endpoint);
    }

    @Override
    @Activate
    protected void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(@Nullable Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    @Deactivate
    protected void deactivate() {
        super.deactivate();
        client.shutdown();
        endpoint.destroy();
    }

    @Override
    protected void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        logger.debug("Starting PhilipsAir (COAP) background discovery job");
        coapDiscoveryJob = scheduler.scheduleWithFixedDelay(this::backgroundScan, 0, BACKGROUND_DISCOVERY_INTERVAL,
                TimeUnit.SECONDS);
    }

    private void backgroundScan() {
        logger.debug("Initiated PhilipsAir (COAP) background discovery scan");
        try {
            startScan();
        } catch (RuntimeException e) {
            // an uncaught exception would cancel the scheduled background discovery
            logger.debug("PhilipsAir (COAP) background discovery scan failed: {}", e.getMessage(), e);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> coapDiscoveryJob = this.coapDiscoveryJob;
        if (coapDiscoveryJob != null) {
            coapDiscoveryJob.cancel(true);
            this.coapDiscoveryJob = null;
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return PhilipsAirBindingConstants.SUPPORTED_COAP_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        logger.debug("Start COAP discovery");
        Set<String> broadcastAddresses = new HashSet<>(NetUtil.getAllBroadcastAddresses());
        String configuredBroadcastAddress = networkAddressService.getConfiguredBroadcastAddress();
        if (configuredBroadcastAddress != null) {
            broadcastAddresses.add(configuredBroadcastAddress);
        }
        broadcastAddresses.add("224.0.1.187"); // CoAP All-Nodes multicast address
        logger.debug("Broadcast to {} addresses", broadcastAddresses.size());
        for (String host : broadcastAddresses) {
            try {
                mget(client, COAP_PORT, PATH, host);
            } catch (RuntimeException e) {
                // e.g. an invalid address, which must not prevent the requests to the other addresses
                logger.debug("Could not send the discovery request to {}: {}", host, e.getMessage());
            }
        }
    }

    void discovered(String response, String host) {
        try {
            PhilipsAirPurifierDeviceDTO info = gson.fromJson(response, PhilipsAirPurifierDeviceDTO.class);
            String deviceId = info != null ? info.getDeviceId() : null;
            if (info == null || deviceId == null) {
                logger.debug(
                        "Philips Air Purifier (COAP) discovery result from IP={} was incomplete or could not be parsed: '{}'",
                        host, response);
                return;
            }

            logger.debug("Creating Philips Air Purifier (COAP) discovery result for: IP={}, {}", host, response);
            ThingUID thingUid = new ThingUID(PhilipsAirBindingConstants.THING_TYPE_COAP, deviceId);
            Map<String, Object> properties = new HashMap<>();
            addProperty(properties, PhilipsAirConfiguration.CONFIG_HOST, host);
            addProperty(properties, PhilipsAirConfiguration.CONFIG_DEVICE_UUID, deviceId);
            addProperty(properties, PhilipsAirBindingConstants.PROPERTY_MANUFACTURER, "Philips");
            addProperty(properties, PROPERTY_VENDOR, PhilipsAirBindingConstants.VENDOR);
            addProperty(properties, PROPERTY_MODEL_ID, info.getModelId());
            addProperty(properties, PROPERTY_DEV_TYPE, info.getType());

            String label = String.format("Philips AirPurifier %s %s", info.getName(), info.getModelId());
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUid).withProperties(properties).withLabel(label)
                    .withRepresentationProperty(PhilipsAirConfiguration.CONFIG_DEVICE_UUID).build();

            logger.debug("DiscoveryResult with uid {} and label: '{}'", result.getThingUID(), result.getLabel());
            thingDiscovered(result);
        } catch (JsonSyntaxException e) {
            logger.debug("Error while processing JSON from discovery result. IP={}, Response={}", host, response, e);
        }
    }

    private static void addProperty(Map<String, Object> properties, String key, @Nullable String value) {
        properties.put(key, value != null ? value : "");
    }

    /**
     * Sends the discovery request without waiting for the responses. Responses from all devices are received
     * asynchronously and create the discovery results.
     */
    private void mget(CoapClient client, int port, String resourcePath, String host) {
        String uri = "coap://" + host + ":" + port + "/" + resourcePath;
        logger.debug("Send discovery request: {}", uri);
        Request multicastRequest = Request.newGet();
        multicastRequest.setType(Type.NON);
        multicastRequest.setURI(uri);
        client.advanced(new DiscoveryCoapHandler(), multicastRequest);
    }

    /**
     * Handles the discovery responses matched to the request. The responses are only logged here, as they are
     * processed by the message interceptor, which receives all responses, including those that Californium does not
     * match to the multicast request.
     */
    private class DiscoveryCoapHandler implements CoapHandler {

        @Override
        public void onLoad(@Nullable CoapResponse response) {
            if (response != null) {
                InetSocketAddress ip = response.advanced().getSourceContext().getPeerAddress();
                logger.trace("Received coap response from '{}' - {}", ip, Utils.prettyPrint(response));
            } else {
                logger.debug("Received NULL coap response");
            }
        }

        @Override
        public void onError() {
            logger.debug("CoAP error during discovery");
        }
    }
}
