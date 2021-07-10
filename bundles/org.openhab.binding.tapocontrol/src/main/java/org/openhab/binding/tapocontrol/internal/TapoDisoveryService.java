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
package org.openhab.binding.tapocontrol.internal;

import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.api.TapoCloudApi;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Handler class for TAPO Smart Home thing discovery
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoDisoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(TapoDisoveryService.class);
    protected final TapoCloudApi connector;
    protected final TapoCredentials credentials;

    /**
     * INIT CLASS
     * 
     * @param credentials TapoCredentials
     */
    public TapoDisoveryService(TapoCredentials credentials) {
        super(Collections.unmodifiableSet(Stream.of(new ThingTypeUID(BINDING_ID, "-")).collect(Collectors.toSet())),
                TAPO_DISCOVERY_TIMEOUT_MS, false);
        logger.debug("Initializing TapoDiscoveryService");
        this.credentials = credentials;
        this.connector = new TapoCloudApi();
    }

    /**
     * START SCAN MANUALLY
     */
    @Override
    public void startScan() {
        logger.debug("startScan");
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        if (username != "" && password != "") {
            connector.login(username, password);
            JsonArray jsonArray = connector.getDeviceList();
            handleCloudDevices(jsonArray);
        }
    }

    /**
     * STOP SCAN
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        logger.debug("startScan");
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * ABORT SCAN
     */
    @Override
    public synchronized void abortScan() {
        super.abortScan();
        logger.debug("abortScan");
    }

    /**
     * 
     * @param device JsonObject deviceInfo
     * @return ThingUID
     */
    public ThingUID getThingUID(JsonObject device) {
        try {
            String deviceModel = device.get("deviceModel").getAsString();
            String deviceId = device.get("deviceId").getAsString();
            ThingUID thingUID = new ThingUID(BINDING_ID, deviceId);
            return thingUID;
        } catch (Exception e) {
            return new ThingUID(BINDING_ID, "DUMMY");
        }
    }

    public DiscoveryResult createResult(JsonObject device) {
        ThingUID thingUID = getThingUID(device);
        String deviceModel = device.get("deviceModel").getAsString();
        String deviceId = device.get("deviceId").getAsString();

        /* create properties */
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(Thing.PROPERTY_MAC_ADDRESS, device.get("deviceMac").getAsString());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.get("fwVer").getAsString());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, device.get("deviceHwVer").getAsString());
        properties.put(Thing.PROPERTY_MODEL_ID, deviceModel);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceId);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties).build();
        return discoveryResult;
    }

    /**
     * work with result from get devices from cloud devices
     * 
     * @param deviceList
     */
    protected void handleCloudDevices(JsonArray deviceList) {
        for (JsonElement deviceElement : deviceList) {
            if (deviceElement.isJsonObject()) {
                JsonObject device = deviceElement.getAsJsonObject();
                ThingUID thingUID = getThingUID(device);

                /* create thing */
                if (SUPPORTED_THING_TYPES_UIDS.contains(thingUID)) {
                    DiscoveryResult discoveryResult = createResult(device);
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }

    /*
     * private static final String ARP_GET_IP_HW = "arp -a";
     * 
     * public String getARPTable(String cmd) throws IOException {
     * Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
     * return s.hasNext() ? s.next() : "";
     * }
     * 
     * System.out.println(getARPTable(ARP_GET_IP_HW ));
     */
}
