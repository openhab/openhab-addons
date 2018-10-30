/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.protocol;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.snapcast.internal.data.Client;
import org.openhab.binding.snapcast.internal.data.Client.ClientConfig;
import org.openhab.binding.snapcast.internal.data.Client.Volume;
import org.openhab.binding.snapcast.internal.data.Group;
import org.openhab.binding.snapcast.internal.data.Params;

/**
 * Client protocol handler
 *
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
public class ClientController extends AbstractController<Client, ClientListener> {

    /**
     * @param controller The main snapcast controller
     */
    ClientController(SnapcastController controller) {
        super(controller);

        controller.registerNotifyListener("Client.OnConnect", Params.class, this::handleUpdate);
        controller.registerNotifyListener("Client.OnDisconnect", Params.class, this::handleUpdate);
        controller.registerNotifyListener("Client.OnVolumeChanged", ClientConfig.class, this::handleUpdate);
        controller.registerNotifyListener("Client.OnLatencyChanged", ClientConfig.class, this::handleUpdate);
        controller.registerNotifyListener("Client.OnNameChanged", ClientConfig.class, this::handleUpdate);
    }

    /**
     * Request the client status
     *
     * @param id The internal snapcast-id (not the UID) of the client
     */
    public void getStatus(String id) {
        Params params = new Params();
        params.setId(id);
        getController().sendRequest("Client.GetStatus", params, Params.class, this::handleUpdate);
    }

    /**
     * Set the client volume
     *
     * @param id      The internal snapcast-id (not the UID) of the client
     * @param percent The volume
     * @param muted   {@code true} if the client is to be muted
     */
    public void setVolume(String id, @Nullable Integer percent, Boolean muted) {
        ClientConfig params = new ClientConfig();
        params.setId(id);
        params.setVolume(new Volume(percent, muted));
        getController().sendRequest("Client.SetVolume", params, ClientConfig.class, this::handleUpdate);
    }

    /**
     * Set the client latency
     *
     * @param id      The internal snapcast-id (not the UID) of the client
     * @param latency The latency in ms
     */
    public void setLatency(String id, Integer latency) {
        ClientConfig params = new ClientConfig();
        params.setId(id);
        params.setLatency(latency);
        getController().sendRequest("Client.SetLatency", params, ClientConfig.class, this::handleUpdate);
    }

    /**
     * Set the client name
     *
     * @param id   The internal snapcast-id (not the UID) of the client
     * @param name The name
     */
    public void setName(String id, String name) {
        ClientConfig params = new ClientConfig();
        params.setId(id);
        params.setName(name);
        getController().sendRequest("Client.SetName", params, ClientConfig.class, this::handleUpdate);
    }

    /**
     * Set the stream for a client
     *
     * With the current api it is not possible to set the stream for a single client. Only for all clients in a group.
     * This method will remove all clients, except the selected one, from the group and set the stream for the group.
     *
     * @param id       The internal snapcast-id (not the UID) of the client
     * @param streamId id of the stream
     *
     * @see GroupController#setStream(String, String)
     * @see GroupController#setClients(String, List)
     */
    public void setStream(String id, String streamId) {
        GroupController groupController = getController().groupController();
        Group group = groupController.getThingStateByClientId(id);
        if (group != null) {
            String groupId = group.getId();
            if (groupId != null) {
                List<Client> clients = group.getClients();
                if (clients != null && clients.size() > 1) {
                    groupController.setClients(groupId, Arrays.asList(id));
                }
                groupController.setStream(groupId, streamId);
            }
        }
    }

    /**
     * Handle an incoming status update
     *
     * @param params the data structure from a response or notification
     */
    void handleUpdate(Params params) {
        Client client = params.getClient();
        if (client != null) {
            handleUpdate(client);
        }
    }

    /**
     * Handle an incoming status update
     *
     * @param client the data structure of a client
     */
    void handleUpdate(Client client) {
        final String clientId = client.getId();

        updateThingState(client);

        // update details
        ClientConfig config = client.getConfig();
        if (config != null) {
            config.setId(clientId);
            handleUpdate(config);
        }

        // update group
        eachListener(clientId, listener -> listener.updateGroup(clientId));

        // update connection
        eachListener(clientId, listener -> listener.updateConnection(clientId));
    }

    /**
     * Handle an incoming status update
     *
     * @param params the configurable parameters of a client
     */
    void handleUpdate(ClientConfig params) {
        final String clientId = params.getId();

        // update thing state
        Client client = getThingState(clientId);
        if (client != null) {
            mergeThingState(client.getConfig(), params);
        }

        eachListener(clientId, listener -> {

            // update name
            String name = params.getName();
            if (name != null) {
                listener.updateName(clientId);
            }

            // update volume
            Volume volume = params.getVolume();
            if (volume != null) {
                listener.updateVolumn(clientId);
                listener.updateMute(clientId);
            }

            // update latency
            Integer latency = params.getLatency();
            if (latency != null) {
                listener.updateLatency(clientId);
            }

        });
    }

}
