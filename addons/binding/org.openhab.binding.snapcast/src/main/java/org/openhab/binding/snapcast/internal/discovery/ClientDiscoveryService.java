/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.snapcast.internal.SnapcastBindingConstants;
import org.openhab.binding.snapcast.internal.data.Client;
import org.openhab.binding.snapcast.internal.data.Client.ClientConfig;
import org.openhab.binding.snapcast.internal.data.Host;
import org.openhab.binding.snapcast.internal.handler.SnapcastServerHandler;
import org.openhab.binding.snapcast.internal.protocol.ClientController;
import org.openhab.binding.snapcast.internal.protocol.ClientListener;

/**
 * Discovery service for snapcast clients
 *
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
public class ClientDiscoveryService extends AbstractDiscoveryService {

    private final ClientProtocolHandler clientProtocolHandler = new ClientProtocolHandler();
    private final SnapcastServerHandler serverHandler;

    public ClientDiscoveryService(SnapcastServerHandler serverHandler) {
        super(Collections.singleton(SnapcastBindingConstants.THING_TYPE_CLIENT), 10);
        this.serverHandler = serverHandler;
    }

    @Override
    protected void startScan() {
        for (Client c : clientController().listThingState()) {
            discover(c);
        }
    }

    public void activate() {
        clientController().addListener(null, clientProtocolHandler);
    }

    @Override
    public void deactivate() {
        clientController().removeListener(null, clientProtocolHandler);
        super.deactivate();
    }

    private void discover(Client client) {
        ThingUID uid = getThingUID(client);
        ThingUID bridge = getBridgeUID();
        String label = getLabel(client);
        Map<String, Object> properties = getProperties(client);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withProperties(properties)
                .withRepresentationProperty(SnapcastBindingConstants.CONFIG_CLIENT_ID).withLabel(label)
                .withBridge(bridge).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID getThingUID(Client client) {
        return new ThingUID(SnapcastBindingConstants.THING_TYPE_CLIENT, getBridgeUID(),
                client.getId().replaceAll(":", ""));
    }

    private Map<String, Object> getProperties(Client client) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(SnapcastBindingConstants.CONFIG_CLIENT_ID, client.getId());
        return properties;
    }

    private String getLabel(Client client) {
        String result = "Snapclient";

        ClientConfig config;
        Host host;
        String name;

        if ((config = client.getConfig()) != null && (name = config.getName()) != null && !name.isEmpty()) {
            result = result + " " + name;
        } else if ((host = client.getHost()) != null && (name = host.getName()) != null && !name.isEmpty()) {
            result = result + " " + name;
        }
        return result;
    }

    private ClientController clientController() {
        return serverHandler.getSnapcastController().clientController();
    }

    private ThingUID getBridgeUID() {
        return serverHandler.getThing().getUID();
    }

    /**
     * @author Steffen Brandemann - Initial contribution
     */
    private class ClientProtocolHandler implements ClientListener {

        @Override
        public void updateConnection(String clientId) {
            Client client = clientController().getThingState(clientId);
            if (client != null) {
                Boolean connected = client.getConnected();
                if (connected != null && connected.booleanValue()) {
                    discover(client);
                }
            }
        }

        @Override
        public void updateName(String clientId) {
        }

        @Override
        public void updateVolumn(String clientId) {
        }

        @Override
        public void updateMute(String clientId) {
        }

        @Override
        public void updateLatency(String clientId) {
        }

        @Override
        public void updateGroup(String clientId) {
        }
    }

}
