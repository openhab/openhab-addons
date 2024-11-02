/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal.discovery;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myuplink.internal.MyUplinkBindingConstants;
import org.openhab.binding.myuplink.internal.Utils;
import org.openhab.binding.myuplink.internal.command.account.GetSystems;
import org.openhab.binding.myuplink.internal.connector.CommunicationStatus;
import org.openhab.binding.myuplink.internal.handler.MyUplinkAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * this class will handle discovery of wallboxes and circuits within the site configured.
 *
 * @author Alexander Friese - initial contribution
 *
 */
@NonNullByDefault
public class MyUplinkDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(MyUplinkDiscoveryService.class);
    private @NonNullByDefault({}) MyUplinkAccountHandler bridgeHandler;

    public MyUplinkDiscoveryService() throws IllegalArgumentException {
        super(Set.of(MyUplinkBindingConstants.THING_TYPE_ACCOUNT), 300, false);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof MyUplinkAccountHandler accountHandler) {
            this.bridgeHandler = accountHandler;
            this.bridgeHandler.setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    // method is defined in both implemented interface and inherited class, thus we must define a behaviour here.
    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        bridgeHandler.enqueueCommand(new GetSystems(bridgeHandler, this::processMyUplinkDiscoveryResult));
    }

    /**
     * callback that handles json result data to provide discovery result.
     *
     * @param site
     */
    void processMyUplinkDiscoveryResult(CommunicationStatus status, JsonObject json) {
        logger.debug("processMyUplinkDiscoveryResult {}", json);

        JsonArray systems = json.getAsJsonArray(JSON_KEY_SYSTEMS);
        if (systems == null || systems.isEmpty()) {
            logger.debug("System discovery finished, no systems found.");
        } else {
            systems.forEach(this::handleSystemDiscovery);
        }
    }

    void handleSystemDiscovery(JsonElement json) {
        logger.debug("handleSystemDiscovery {}", json);

        JsonObject system = json.getAsJsonObject();
        String systemId = Utils.getAsString(system, JSON_KEY_SYSTEM_ID);
        JsonArray devices = system.getAsJsonArray(JSON_KEY_DEVICES);
        if (devices == null || devices.isEmpty()) {
            logger.debug("System discovery finished, no devices found.");
        } else {
            devices.forEach(device -> handleDeviceDiscovery(device, systemId));
        }
    }

    void handleDeviceDiscovery(JsonElement json, @Nullable String systemId) {
        logger.debug("handleDeviceDiscovery {}", json);

        JsonObject device = json.getAsJsonObject();
        String id = Utils.getAsString(device, JSON_KEY_GENERIC_ID);
        String serial = Utils.getAsString(device.getAsJsonObject(JSON_KEY_PRODUCT), JSON_KEY_SERIAL);
        String name = Utils.getAsString(device.getAsJsonObject(JSON_KEY_PRODUCT), JSON_KEY_NAME);

        if (id != null && serial != null) {
            DiscoveryResultBuilder builder;
            builder = initDiscoveryResultBuilder(DEVICE_GENERIC_DEVICE, id, name);
            builder.withProperty(THING_CONFIG_SERIAL, serial);
            if (systemId != null) {
                builder.withProperty(THING_CONFIG_SYSTEM_ID, systemId);
            }
            thingDiscovered(builder.build());
        }
    }

    /**
     * sends discovery notification to the framework.
     *
     * @param deviceType
     * @param deviceId
     * @param deviceName
     */
    DiscoveryResultBuilder initDiscoveryResultBuilder(String deviceType, String deviceId, @Nullable String deviceName) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID typeUid = new ThingTypeUID(BINDING_ID, deviceType);

        ThingUID thingUID = new ThingUID(typeUid, bridgeUID, deviceId);
        String label = deviceName != null ? deviceName : deviceId;

        return DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(label)
                .withProperty(THING_CONFIG_ID, deviceId).withRepresentationProperty(THING_CONFIG_ID);
    }
}
