/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link AsuswrtClientList} class stores client list
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtClientList {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtClientList.class);
    private List<AsuswrtClientInfo> clientList = new ArrayList<AsuswrtClientInfo>();

    /**
     * INIT CLASS
     */
    public AsuswrtClientList() {
    }

    /**
     * 
     * INIT CLASS
     * 
     * @param jsonObject with clientinfo
     */
    public AsuswrtClientList(JsonObject jsonObject) {
        setData(jsonObject);
    }

    /**
     * SET DATA
     * from jsonData
     * 
     * @param jsonObject
     */
    public void setData(JsonObject jsonObject) {
        this.clientList.clear();
        try {
            JsonObject jsonList = jsonObject.getAsJsonObject(JSON_MEMBER_CLIENTS);
            /* get and iterate mac addresslist */
            JsonArray macList = jsonList.getAsJsonArray(JSON_MEMBER_MACLIST);
            for (JsonElement jsonElement : macList) {
                String macAddress = jsonElement.getAsString();
                if (jsonList.has(macAddress)) {
                    AsuswrtClientInfo clientInfo = new AsuswrtClientInfo(jsonList.getAsJsonObject(macAddress));
                    addClient(clientInfo);
                } else {
                    logger.trace("getClientlist: {} '{}'", ERR_JSON_UNKNOWN_MEMBER, macAddress);
                }
            }
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
     * @param clientMAC String client MAC Address
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
     * GET ONLINE CLIENT NAMES
     */
    public String getOnlineClientNames() {
        StringBuilder clients = new StringBuilder();
        for (AsuswrtClientInfo client : this.clientList) {
            clients.append(client.getName() + "; ");
        }
        return clients.toString();
    }

    /*
     * GET ONLINE CLIENT NAMES
     */
    public String getOnlineClientMACs() {
        StringBuilder clients = new StringBuilder();
        for (AsuswrtClientInfo client : this.clientList) {
            clients.append(client.getMac() + "; ");
        }
        return clients.toString();
    }
}
