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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.SecureSession;
import org.openhab.binding.homekit.internal.discovery.HomekitAccessoryDiscoveryService;
import org.openhab.binding.homekit.internal.dto.HomekitAccessories;
import org.openhab.binding.homekit.internal.dto.HomekitAccessory;
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
 * The {@link HomekitBridgeHandler} is an instance of a {@link BaseHomekitServerHandler} that
 * marshals communications with multiple HomeKit accessories within a HomeKit bridge server.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitBridgeHandler extends BaseHomekitServerHandler implements BridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HomekitBridgeHandler.class);

    private static final Gson GSON = new Gson();

    public HomekitBridgeHandler(Bridge bridge, HomekitAccessoryDiscoveryService discoveryService) {
        super(bridge, discoveryService);
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
            List<HomekitAccessory> accessories = getAccessories();
            discoveryService.accessoriesDscovered(thing, accessories);
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
     * Get information about embedded accessories and their respective channels
     */
    private List<HomekitAccessory> getAccessories() {
        HomekitAccessories result = null;
        SecureSession session = this.session;
        if (session != null) {
            URI uri = URI.create(accessoryAddress + "/accessories");
            HttpRequest request = HttpRequest.newBuilder().uri(uri).timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/json").GET().build();
            HttpResponse<byte[]> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 200) {
                    byte[] decrypted = session.decrypt(response.body());
                    result = GSON.fromJson(new String(decrypted, StandardCharsets.UTF_8), HomekitAccessories.class);
                }
            } catch (IOException | InterruptedException e) {
            }
        }
        return result == null ? List.of() : result.accessories == null ? List.of() : result.accessories;
    }

}
