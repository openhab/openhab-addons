/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.protocol;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.snapcast.internal.data.Client;
import org.openhab.binding.snapcast.internal.data.Group;
import org.openhab.binding.snapcast.internal.data.Group.GroupClients;
import org.openhab.binding.snapcast.internal.data.Params;

/**
 * Group protocol handler
 *
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
public class GroupController extends AbstractController<Group, GroupListener> {

    /**
     * @param controller The main snapcast controller
     */
    GroupController(SnapcastController controller) {
        super(controller);
        controller.registerNotifyListener("Group.OnStreamChanged", Group.class, this::handleUpdate);
    }

    /**
     * Set the stream for a group
     *
     * @param id       The internal snapcast-id of the group
     * @param streamId id of the stream
     */
    public void setStream(String id, String streamId) {
        Group params = new Group();
        params.setId(id);
        params.setStream(streamId);
        getController().sendRequest("Group.SetStream", params, Group.class, this::handleUpdate);
    }

    /**
     * Set the clients for a group
     *
     * @param id      The internal snapcast-id of the group
     * @param clients the ids of the clients
     */
    public void setClients(String id, List<String> clients) {
        ServerController serverController = getController().serverController();

        GroupClients params = new GroupClients();
        params.setId(id);
        params.setClients(clients);
        getController().sendRequest("Group.SetClients", params, Params.class, serverController::handleUpdate);
    }

    /**
     * Returns the group of a client
     *
     * @param id The internal snapcast-id (not the UID) of the client
     * @return The cached data structure or {@code null} if nothing is found
     */
    public @Nullable Group getThingStateByClientId(String id) {
        for (Group group : listThingState()) {
            List<Client> clients = group.getClients();
            if (clients != null) {
                for (Client client : clients) {
                    if (id.equals(client.getId())) {
                        return group;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Handle an incoming status update
     *
     * @param params the data structure of a group
     */
    void handleUpdate(Group params) {
        ClientController clientController = getController().clientController();
        String groupId = params.getId();

        updateThingState(params);

        // update clients
        List<Client> clients = params.getClients();
        if (clients != null) {
            for (Client client : clients) {
                clientController.handleUpdate(client);
            }
        }

        // update stream
        eachListener(groupId, listener -> listener.updateStream(groupId));
    }

}
