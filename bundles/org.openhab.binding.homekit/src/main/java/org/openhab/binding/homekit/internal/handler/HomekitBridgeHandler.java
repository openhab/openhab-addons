/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.handler;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.discovery.AccessoryDiscoveryService;
import org.openhab.binding.homekit.internal.dto.Accessories;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.network.SecureSession;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handler for HomeKit bridge devices.
 * It marshals the communications with multiple HomeKit child accessories within a HomeKit bridge server.
 * It uses the /accessories endpoint to discover embedded accessories and their services.
 * It notifies the {@link AccessoryDiscoveryService} when accessories are discovered.
 * It does not currently handle commands for channels, that is left to the child accessory handlers.
 * It extends {@link HomekitBaseServerHandler} to handle pairing and secure session setup.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitBridgeHandler extends HomekitBaseServerHandler implements BridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HomekitBridgeHandler.class);

    private static final Gson GSON = new Gson();

    private final AccessoryDiscoveryService discoveryService;

    public HomekitBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory,
            AccessoryDiscoveryService discoveryService) {
        super(bridge, httpClientFactory);
        this.discoveryService = discoveryService;
    }

    @Override
    public Bridge getThing() {
        return (Bridge) super.getThing();
    }

    /**
     * Creates a bridge builder, which allows to modify the bridge. The method
     * {@link BaseThingHandler#updateThing(Thing)} must be called to persist the changes.
     *
     * @return {@link BridgeBuilder} which builds an exact copy of the bridge
     */
    @Override
    protected BridgeBuilder editThing() {
        return BridgeBuilder.create(thing.getThingTypeUID(), thing.getUID()).withBridge(thing.getBridgeUID())
                .withChannels(thing.getChannels()).withConfiguration(thing.getConfiguration())
                .withLabel(thing.getLabel()).withLocation(thing.getLocation()).withProperties(thing.getProperties())
                .withSemanticEquipmentTag(thing.getSemanticEquipmentTag());
    }

    @Override
    public void initialize() {
        super.initialize();
        scheduler.submit(() -> {
            List<Accessory> accessories = getAccessories();
            discoveryService.devicesDiscovered(thing, accessories);
        });
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        // TODO Auto-generated method stub
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        // TODO Auto-generated method stub
    }

    /**
     * Get information about embedded accessories and their respective channels.
     * Uses the /accessories endpoint.
     * Returns an empty list if there was a problem.
     * Requires a valid secure session.
     *
     * @return list of accessories (may be empty)
     * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-homekit-http">HomeKit HTTP</a>
     */
    private List<Accessory> getAccessories() {
        SecureSession session = this.session;
        if (session != null) {
            try {
                byte[] encrypted = httpTransport.get(baseUrl, ENDPOINT_ACCESSORIES, CONTENT_TYPE_HAP);
                byte[] decrypted = session.decrypt(encrypted);
                Accessories result = GSON.fromJson(new String(decrypted, StandardCharsets.UTF_8),
                        Accessories.class);
                if (result != null && result.accessories != null) {
                    return result.accessories;
                }
            } catch (Exception e) {
            }
        }
        return List.of();
    }
}
