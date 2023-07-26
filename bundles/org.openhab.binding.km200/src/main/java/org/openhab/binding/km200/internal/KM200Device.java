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
package org.openhab.binding.km200.internal;

import static org.openhab.binding.km200.internal.KM200BindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The KM200Device representing the device with its all capabilities
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
public class KM200Device {

    private final Logger logger = LoggerFactory.getLogger(KM200Device.class);
    private final KM200Cryption comCryption;
    private final KM200Comm<KM200Device> deviceCommunicator;

    /* valid IPv4 address of the KMxxx. */
    protected String ip4Address = "";

    /* The gateway password which is provided on the type sign of the KMxxx. */
    protected String gatewayPassword = "";

    /* The private password which has been defined by the user via EasyControl. */
    protected String privatePassword = "";

    /* The returned device charset for communication */
    protected String charSet = "";

    /* Needed keys for the communication */
    protected byte[] cryptKeyInit = new byte[0];
    protected byte[] cryptKeyPriv = new byte[0];

    /* Buderus_MD5Salt */
    protected byte[] md5Salt = new byte[0];

    /* Device services */
    public Map<String, KM200ServiceObject> serviceTreeMap;

    /* Device services blacklist */
    private List<String> blacklistMap = new ArrayList<>();
    /* List of virtual services */
    public List<KM200ServiceObject> virtualList;

    /* Is the first INIT done */
    protected boolean isIited;

    public KM200Device(HttpClient httpClient) {
        serviceTreeMap = new HashMap<>();
        getBlacklistMap().add("/gateway/firmware");
        getBlacklistMap().add("/gateway/registrations");
        virtualList = new ArrayList<>();
        comCryption = new KM200Cryption(this);
        deviceCommunicator = new KM200Comm<>(this, httpClient);
    }

    public Boolean isConfigured() {
        return !ip4Address.isBlank() && cryptKeyPriv.length > 0;
    }

    public String getIP4Address() {
        return ip4Address;
    }

    public String getGatewayPassword() {
        return gatewayPassword;
    }

    public String getPrivatePassword() {
        return privatePassword;
    }

    public byte[] getMD5Salt() {
        return md5Salt;
    }

    public byte[] getCryptKeyInit() {
        return cryptKeyInit;
    }

    public byte[] getCryptKeyPriv() {
        return cryptKeyPriv;
    }

    public String getCharSet() {
        return charSet;
    }

    public boolean getInited() {
        return isIited;
    }

    public List<String> getBlacklistMap() {
        return blacklistMap;
    }

    public void setBlacklistMap(List<String> blacklistMap) {
        this.blacklistMap = blacklistMap;
    }

    public void setIP4Address(String ip) {
        ip4Address = ip;
    }

    public void setGatewayPassword(String password) {
        gatewayPassword = password;
        comCryption.recreateKeys();
    }

    public void setPrivatePassword(String password) {
        privatePassword = password;
        comCryption.recreateKeys();
    }

    public void setMD5Salt(String salt) {
        if (!salt.isEmpty()) {
            md5Salt = HexUtils.hexToBytes(salt);
            comCryption.recreateKeys();
        } else {
            md5Salt = new byte[] { 0 };
        }
    }

    public void setCryptKeyPriv(String key) {
        if (!key.isEmpty()) {
            cryptKeyPriv = HexUtils.hexToBytes(key);
        } else {
            cryptKeyPriv = new byte[] { 0 };
        }
    }

    public void setCryptKeyPriv(byte[] key) {
        cryptKeyPriv = key;
    }

    public void setCryptKeyInit(byte[] key) {
        cryptKeyInit = key;
    }

    public void setCharSet(String charset) {
        charSet = charset;
    }

    public void setInited(boolean inited) {
        isIited = inited;
    }

    public void setMaxNbrRepeats(Integer maxNbrRepeats) {
        this.deviceCommunicator.setMaxNbrRepeats(maxNbrRepeats);
    }

    /**
     * This function prepares a list of all on the device available services with its capabilities
     *
     */
    public void listAllServices() {
        logger.debug("##################################################################");
        logger.debug("List of avalible services");
        logger.debug("readable;writeable;recordable;virtual;type;service;value;allowed;min;max;unit");
        printAllServices(serviceTreeMap);
        logger.debug("##################################################################");
    }

    /**
     * This function outputs a ";" separated list of all on the device available services with its capabilities
     *
     * @param actTreeMap
     */
    public void printAllServices(Map<String, KM200ServiceObject> actTreeMap) {
        if (logger.isDebugEnabled()) {
            for (KM200ServiceObject object : actTreeMap.values()) {
                String val = "", type;
                StringBuilder valPara = new StringBuilder();
                logger.debug("List type: {} service: {}", object.getServiceType(), object.getFullServiceName());
                type = object.getServiceType();
                if (DATA_TYPE_STRING_VALUE.equals(type) || DATA_TYPE_FLOAT_VALUE.equals(type)) {
                    Object valObject = object.getValue();
                    if (null != valObject) {
                        val = valObject.toString();
                        if (object.getValueParameter() != null) {
                            if (DATA_TYPE_STRING_VALUE.equals(type)) {
                                // Type is definitely correct here
                                @SuppressWarnings("unchecked")
                                List<String> valParas = (List<String>) object.getValueParameter();
                                if (null != valParas) {
                                    for (int i = 0; i < valParas.size(); i++) {
                                        if (i > 0) {
                                            valPara.append("|");
                                        }
                                        valPara.append(valParas.get(i));
                                    }
                                    valPara.append(";;;");
                                }
                            }
                            if (DATA_TYPE_FLOAT_VALUE.equals(type)) {
                                // Type is definitely correct here
                                @SuppressWarnings("unchecked")
                                List<Object> valParas = (List<Object>) object.getValueParameter();
                                if (null != valParas) {
                                    valPara.append(";");
                                    valPara.append(valParas.get(0));
                                    valPara.append(";");
                                    valPara.append(valParas.get(1));
                                    valPara.append(";");
                                    if (valParas.size() == 3) {
                                        valPara.append(valParas.get(2));
                                    }
                                }
                            }
                        } else {
                            valPara.append(";;;");
                        }
                    } else {
                        val = "";
                        valPara.append(";");
                    }
                }
                logger.debug("{};{};{};{};{};{};{};{}", object.getReadable(), object.getWriteable(),
                        object.getRecordable(), object.getVirtual(), type, object.getFullServiceName(), val, valPara);
                printAllServices(object.serviceTreeMap);
            }
        }
    }

    /**
     * This function resets the update state on all service objects
     *
     * @param actTreeMap
     */
    public void resetAllUpdates(Map<String, KM200ServiceObject> actTreeMap) {
        for (KM200ServiceObject stmObject : actTreeMap.values()) {
            stmObject.setUpdated(false);
            resetAllUpdates(stmObject.serviceTreeMap);
        }
    }

    /**
     * This function checks whether a service is available
     *
     * @param service
     */
    public Boolean containsService(String service) {
        String[] servicePath = service.split("/");
        KM200ServiceObject object = null;
        int len = servicePath.length;
        if (len == 0) {
            return false;
        }
        if (!serviceTreeMap.containsKey(servicePath[1])) {
            return false;
        } else {
            if (len == 2) {
                return true;
            }
            object = serviceTreeMap.get(servicePath[1]);
        }
        for (int i = 2; i < len; i++) {
            if (object != null && object.serviceTreeMap.containsKey(servicePath[i])) {
                object = object.serviceTreeMap.get(servicePath[i]);
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * This function return the KM200CommObject of a service
     *
     * @param service
     */
    public @Nullable KM200ServiceObject getServiceObject(String service) {
        String[] servicePath = service.split("/");
        KM200ServiceObject object = null;
        int len = servicePath.length;
        if (len == 0) {
            return null;
        }
        if (!serviceTreeMap.containsKey(servicePath[1])) {
            return null;
        } else {
            object = serviceTreeMap.get(servicePath[1]);
            if (len == 2) {
                return object;
            }
        }
        for (int i = 2; i < len; i++) {
            if (object != null && object.serviceTreeMap.containsKey(servicePath[i])) {
                object = object.serviceTreeMap.get(servicePath[i]);
                continue;
            } else {
                return null;
            }
        }
        return object;
    }

    /**
     * This function checks the communication to a service on the device. It returns null if the communication is not
     * possible and a JSON node in opposide case.
     *
     */
    public @Nullable JsonObject getServiceNode(String service) {
        String decodedData = null;
        JsonObject nodeRoot = null;
        logger.debug("{}: trying to query information.", service);
        byte[] recData = deviceCommunicator.getDataFromService(service.toString());
        try {
            if (recData == null) {
                logger.debug("Communication to {} is not possible!", service);
                return null;
            }
            if (recData.length == 0) {
                logger.debug("No reply from KM200!");
                return null;
            }
            /* Look whether the communication was forbidden */
            if (recData.length == 1) {
                logger.debug("{}: recData.length == 1", service);
                nodeRoot = new JsonObject();
                decodedData = "";
                return nodeRoot;
            } else {
                logger.debug("{}: trying to decode: {}.", service, recData.toString());
                decodedData = comCryption.decodeMessage(recData);
                if (decodedData == null) {
                    logger.warn("Decoding of the KM200 message is not possible!");
                    return null;
                }
            }
            if (decodedData.length() > 0) {
                if ("SERVICE NOT AVAILABLE".equals(decodedData)) {
                    logger.warn("{}: SERVICE NOT AVAILABLE", service);
                    return null;
                } else {
                    logger.debug("{}: trying to parse {}", service, decodedData.toString());
                    nodeRoot = (JsonObject) JsonParser.parseString(decodedData);
                }
            } else {
                logger.debug("Get empty reply");
                return null;
            }
        } catch (JsonParseException e) {
            logger.warn("Parsingexception in JSON: {} service: {}", e.getMessage(), service);
            return null;
        }
        return nodeRoot;
    }

    /**
     * This function checks the communication to a service on the device. It returns null if the communication is not
     * possible and a JSON node in opposide case.
     *
     */
    public void setServiceNode(String service, JsonObject newObject) {
        int retVal;
        byte[] encData = comCryption.encodeMessage(newObject.toString());
        if (encData == null) {
            logger.warn("Couldn't encrypt data");
            return;
        } else {
            logger.debug("Send: {}", service);
            retVal = deviceCommunicator.sendDataToService(service, encData);
            if (retVal == 0) {
                logger.debug("Send to device failed: {}: {}", service, newObject);
            }
        }
    }
}
