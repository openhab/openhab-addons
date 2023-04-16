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
import static org.openhab.binding.asuswrt.internal.helpers.AsuswrtUtils.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtClientList} class stores client list
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtClientList implements Iterable<AsuswrtClientInfo> {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtClientList.class);
    private List<AsuswrtClientInfo> clientList = new ArrayList<AsuswrtClientInfo>();

    /**
     * INIT CLASS
     */
    public AsuswrtClientList() {
    }

    /**
     * INIT CLASS
     * 
     * @param jsonObject with clientinfo
     */
    public AsuswrtClientList(JsonObject jsonObject) {
        setData(jsonObject);
    }

    /**
     * ITERATOR
     * 
     * @return clientInfo
     */
    @Override
    public Iterator<AsuswrtClientInfo> iterator() {
        return clientList.iterator();
    }

    /**
     * Generate new AsuswrtClientlist from jsonData
     */
    public void setData(JsonObject jsonObject) {
        this.clientList.clear();
        try {
            JsonObject jsonList = jsonObject.getAsJsonObject(JSON_MEMBER_CLIENTS);
            /* remove member maclist - only online clients in there */
            jsonList.remove(JSON_MEMBER_MACLIST);
            jsonList.remove(JSON_MEMBER_API_LEVEL);
            /* iterate MAC-Addresslist */
            jsonList.keySet().forEach(macAddress -> {
                if (isValidMacAddress(macAddress)) {
                    AsuswrtClientInfo clientInfo = new AsuswrtClientInfo(jsonList.getAsJsonObject(macAddress));
                    addClient(clientInfo);
                } else {
                    logger.trace("getClientlist: {} '{}'", ERR_INVALID_MAC_ADDRESS, macAddress);
                }
            });
        } catch (Exception e) {
            logger.debug("getClientlist: {} - {}'", ERR_JSON_FOMRAT, e.getMessage());
        }
    }

    /**
     * ADD CLIENT TO LIST
     * 
     * @param clientInfo AsuswrtClientInfo
     */
    private void addClient(AsuswrtClientInfo clientInfo) {
        this.clientList.add(clientInfo);
    }

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    /**
     * GET CLIENT BY NAME
     * 
     * @param clientName String clientName
     * @return AsuswrtClientInfo
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
     * GET CLIENT BY MAC
     * 
     * @param clientMAC String client MAC-Address
     * @return AsuswrtClientInfo
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
     * GET CLIENT BY IP
     * 
     * @param clientMAC String client IP-Address
     * @return AsuswrtClientInfo
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
     * Return ; seperated list with clientNames and macAddresses
     */
    public String getClientList() {
        StringBuilder clients = new StringBuilder();
        for (AsuswrtClientInfo client : this.clientList) {
            clients.append(client.getName() + " [" + client.getMac() + "]; ");
        }
        return clients.toString();
    }

    /*
     * Return ; seperated list with clientNames
     */
    public String getClientNames() {
        StringBuilder clients = new StringBuilder();
        for (AsuswrtClientInfo client : this.clientList) {
            clients.append(client.getName() + "; ");
        }
        return clients.toString();
    }

    /**
     * Return count of clients in list
     */
    public Integer getCount() {
        return clientList.size();
    }

    /*
     * Return ; seperated list with macAddresses
     */
    public String getMacAddresses() {
        StringBuilder clients = new StringBuilder();
        for (AsuswrtClientInfo client : this.clientList) {
            clients.append(client.getMac() + "; ");
        }
        return clients.toString();
    }

    /**
     * Get online clients
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
