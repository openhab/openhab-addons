/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.asuswrt.internal.structures;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtErrorConstants.*;
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.isValidMacAddress;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtClientList} class stores a list of {@link AsuswrtClientInfo}.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtClientList implements Iterable<AsuswrtClientInfo> {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtClientList.class);
    private List<AsuswrtClientInfo> clientList = new ArrayList<>();

    public AsuswrtClientList() {
    }

    public AsuswrtClientList(JsonObject jsonObject) {
        setData(jsonObject);
    }

    @Override
    public Iterator<AsuswrtClientInfo> iterator() {
        return clientList.iterator();
    }

    /**
     * Sets the {@link AsuswrtClientList} using a {@link JsonObject}.
     */
    public void setData(JsonObject jsonObject) {
        clientList.clear();
        try {
            JsonObject jsonList = jsonObject.getAsJsonObject(JSON_MEMBER_CLIENTS);
            // Remove the member MAC list, it contains only online clients
            jsonList.remove(JSON_MEMBER_MACLIST);
            jsonList.remove(JSON_MEMBER_API_LEVEL);
            // Iterate over the MAC addresses
            jsonList.keySet().forEach(macAddress -> {
                if (isValidMacAddress(macAddress)) {
                    AsuswrtClientInfo clientInfo = new AsuswrtClientInfo(jsonList.getAsJsonObject(macAddress));
                    addClient(clientInfo);
                } else {
                    logger.trace("getClientlist: {} '{}'", ERR_INVALID_MAC_ADDRESS, macAddress);
                }
            });
        } catch (Exception e) {
            logger.debug("getClientlist: {} - {}'", ERR_JSON_FORMAT, e.getMessage());
        }
    }

    /**
     * Adds {@link AsuswrtClientInfo} to the list.
     */
    private void addClient(AsuswrtClientInfo clientInfo) {
        clientList.add(clientInfo);
    }

    /*
     * Getters
     */

    /**
     * Gets {@link AsuswrtClientInfo} from the list for a client based on its name.
     *
     * @param clientName the name of the client for which the info is returned
     */
    public AsuswrtClientInfo getClientByName(String clientName) {
        for (AsuswrtClientInfo client : this.clientList) {
            if (client.getName().equals(clientName)) {
                return client;
            }
        }
        return new AsuswrtClientInfo();
    }

    /**
     * Gets {@link AsuswrtClientInfo} from the list for a client based on its MAC address.
     *
     * @param clientMAC the MAC address of the client for which the info is returned
     */
    public AsuswrtClientInfo getClientByMAC(String clientMAC) {
        for (AsuswrtClientInfo client : this.clientList) {
            if (client.getMac().equals(clientMAC)) {
                return client;
            }
        }
        return new AsuswrtClientInfo();
    }

    /**
     * Gets {@link AsuswrtClientInfo} from the list for a client based on its IP address.
     *
     * @param clientIP the IP address of the client for which the info is returned
     */
    public AsuswrtClientInfo getClientByIP(String clientIP) {
        for (AsuswrtClientInfo client : this.clientList) {
            if (client.getIP().equals(clientIP)) {
                return client;
            }
        }
        return new AsuswrtClientInfo();
    }

    /*
     * Returns a <code>;</code> separated list with client names and MAC addresses.
     */
    public String getClientList() {
        StringBuilder clients = new StringBuilder();
        for (AsuswrtClientInfo client : this.clientList) {
            clients.append(client.getName() + " [" + client.getMac() + "]; ");
        }
        return clients.toString();
    }

    /*
     * Returns a <code>;</code> separated list with client names.
     */
    public String getClientNames() {
        return clientList.stream().map(AsuswrtClientInfo::getName).collect(Collectors.joining("; "));
    }

    /**
     * Returns the number of clients in the list.
     */
    public Integer getCount() {
        return clientList.size();
    }

    /*
     * Returns a <code>;</code> separated list with MAC addresses.
     */
    public String getMacAddresses() {
        StringBuilder clients = new StringBuilder();
        for (AsuswrtClientInfo client : this.clientList) {
            clients.append(client.getMac() + "; ");
        }
        return clients.toString();
    }

    /**
     * Returns a {@link AsuswrtClientList} of online clients.
     */
    public AsuswrtClientList getOnlineClients() {
        AsuswrtClientList clients = new AsuswrtClientList();
        for (AsuswrtClientInfo client : this.clientList) {
            if (client.isOnline()) {
                clients.addClient(client);
            }
        }
        return clients;
    }
}
