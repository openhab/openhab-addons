/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.io.IOException;
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
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link PhilipsAirUpnpDiscoveryParticipantTest} is responsible for discovering
 * new Philips Air Purifier things for COAP protocol devices
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */

@NonNullByDefault

@Component(service = DiscoveryService.class, configurationPid = "discovery.philipsair")
public class PhilipsAirCoapDiscovery extends AbstractDiscoveryService {
    private static final int DISCOVERY_TIME = 10;
    private static final String PATH = "sys/dev/info";
    private static final int COAP_PORT = 5683;
    private static final long BACKGROUND_DISCOVERY_INTERVAL_ = 600;

    private final Gson gson = new Gson();

    private final Logger logger = LoggerFactory.getLogger(PhilipsAirCoapDiscovery.class);
    private CoapClient client = new CoapClient();
    private @Nullable ScheduledFuture<?> coapDiscoveryJob;
    private NetworkAddressService networkAddressService;

    @Activate
    public PhilipsAirCoapDiscovery(@Reference ConfigurationAdmin configAdmin,
            @Reference NetworkAddressService networkAddressService) throws IllegalArgumentException {
        super(DISCOVERY_TIME);
        NetworkConfig netConfig = NetworkConfig.createStandardWithoutFile();
        CoapEndpoint endpoint = new CoapEndpoint.Builder().setNetworkConfig(netConfig).build();
        this.networkAddressService = networkAddressService;

        client = new CoapClient();
        client.setEndpoint(endpoint);
    }

    @Override
    protected void startBackgroundDiscovery() {
        stopBackgroundDiscovery();
        ScheduledFuture<?> coapDiscoveryJob = this.coapDiscoveryJob;
        if (coapDiscoveryJob == null || coapDiscoveryJob.isCancelled()) {
            logger.debug("Starting PhilipsAir (COAP) background discovery job");
            coapDiscoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, BACKGROUND_DISCOVERY_INTERVAL_,
                    TimeUnit.SECONDS);
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
    public Set<@NonNull ThingTypeUID> getSupportedThingTypes() {
        return PhilipsAirBindingConstants.SUPPORTED_COAP_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        logger.debug("Start COAP discovery");
        HashSet<String> broadcastAddresses = new HashSet(NetUtil.getAllBroadcastAddresses());
        String configuredBroadcastAddress = networkAddressService.getConfiguredBroadcastAddress();
        if (configuredBroadcastAddress != null) {
            broadcastAddresses.add(configuredBroadcastAddress);
        }
        broadcastAddresses.add("224.0.1.187");
        logger.debug("Broadcast to {} addresses", broadcastAddresses.size());
        for (String host : broadcastAddresses) {
            try {
                mget(client, COAP_PORT, PATH, host);
            } catch (ConnectorException | IOException e) {
                logger.debug("Error while discovering: {}", e.getMessage(), e);
            }
        }
    }

    public void discovered(String response, String host) {
        try {
            PhilipsAirPurifierDeviceDTO info = gson.fromJson(response, PhilipsAirPurifierDeviceDTO.class);
            if (info == null) {
                logger.debug(
                        "Philips Air Purifier (COAP protocol) discovery result could not parse response from IP={}, '{}'",
                        host, response);
                return;
            }
            if (info.getDeviceId() == null) {
                logger.debug(
                        "Philips Air Purifier (COAP protocol) discovery result could not find deviceId  IP={}, '{}'",
                        host, response);
                return;
            }
            logger.debug("Creating Philips Air Purifier (COAP protocol) discovery result for: IP={}, {}", host,
                    response);
            ThingUID thingUid = new ThingUID(PhilipsAirBindingConstants.THING_TYPE_COAP, info.getDeviceId());
            Map<String, Object> properties = new HashMap<>();
            addProperty(properties, PhilipsAirConfiguration.CONFIG_HOST, host);
            addProperty(properties, PhilipsAirConfiguration.CONFIG_DEF_DEVICE_UUID, info.getDeviceId());
            addProperty(properties, PhilipsAirBindingConstants.PROPERTY_MANUFACTURER, "Philips");
            addProperty(properties, PROPERTY_VENDOR, PhilipsAirBindingConstants.VENDOR);
            addProperty(properties, PROPERTY_MODEL_ID, info.getModelId());
            addProperty(properties, PROPERTY_DEV_TYPE, info.getType());
            String label = String.format("Philips AirPurifier %s %s", info.getName(), info.getModelId());
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUid).withProperties(properties).withLabel(label)
                    .withRepresentationProperty(PhilipsAirConfiguration.CONFIG_DEF_DEVICE_UUID).build();
            logger.debug("DiscoveryResult with uid {} label : {} ", result.getThingUID().getAsString(),
                    result.getLabel());
            thingDiscovered(result);
        } catch (JsonSyntaxException e) {
            logger.debug("Error while processing discovery result from IP={}, {}", host, response);
        }
    }

    private static void addProperty(Map<String, Object> properties, String key, @Nullable String value) {
        properties.put(key, value != null ? value : "");
    }

    private void mget(CoapClient client, int port, String resourcePath, String host)
            throws ConnectorException, IOException {
        String uri;
        uri = "coap://" + host + ":" + port + "/" + resourcePath;
        logger.debug("Send discovery request: {}", uri);
        client.setURI(uri);
        Request multicastRequest = Request.newGet();
        multicastRequest.setType(Type.NON);
        // sends a multicast request
        MultiCoapHandler handler = new MultiCoapHandler(this, logger);
        client.advanced(handler, multicastRequest);
        while (handler.waitOn(5000)) {
            ;
        }
    }

    // private static final MultiCoapHandler handler = new MultiCoapHandler();

    private static class MultiCoapHandler implements CoapHandler {

        private boolean on;
        private final PhilipsAirCoapDiscovery philipsAirCoapDiscovery;
        private final Logger logger;

        public MultiCoapHandler(PhilipsAirCoapDiscovery philipsAirCoapDiscovery, Logger logger) {
            this.philipsAirCoapDiscovery = philipsAirCoapDiscovery;
            this.logger = logger;
        }

        public synchronized boolean waitOn(long timeout) {
            on = false;
            try {
                wait(timeout);
            } catch (InterruptedException e) {
            }
            return on;
        }

        private synchronized void on() {
            on = true;
            notifyAll();
        }

        @Override
        public void onLoad(@Nullable CoapResponse response) {
            on();
            if (response != null) {
                InetSocketAddress ip = response.advanced().getSourceContext().getPeerAddress();
                logger.trace("Received coap response from '{}' - {}", ip, Utils.prettyPrint(response));
                String resTxt = response.getResponseText();
                if (resTxt != null && !resTxt.isBlank()) {
                    philipsAirCoapDiscovery.discovered(response.getResponseText(), ip.getHostString());
                }
            } else {
                logger.debug("Received NULL coap response ");
            }
        }

        @Override
        public void onError() {
            logger.info("Unspecified COAP error while running discovery");
        }
    };
}
