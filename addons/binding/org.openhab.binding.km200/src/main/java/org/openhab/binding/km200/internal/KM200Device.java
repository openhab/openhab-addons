/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.client.HttpClient;
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
public class KM200Device {

    private final Logger logger = LoggerFactory.getLogger(KM200Device.class);
    private final JsonParser jsonParser = new JsonParser();
    private final KM200Cryption comCryption;
    private final KM200Comm<KM200Device> deviceCommunicator;

    /**
     * shared instance of HTTP client for asynchronous calls
     */
    private HttpClient httpClient;

    /* valid IPv4 address of the KMxxx. */
    protected String ip4Address;

    /* The gateway password which is provided on the type sign of the KMxxx. */
    protected String gatewayPassword;

    /* The private password which has been defined by the user via EasyControl. */
    protected String privatePassword;

    /* The returned device charset for communication */
    protected String charSet;

    /* Needed keys for the communication */
    protected byte[] cryptKeyInit;
    protected byte[] cryptKeyPriv;

    /* Buderus_MD5Salt */
    protected byte[] md5Salt;

    /* Device services */
    public Map<String, KM200ServiceObject> serviceTreeMap;

    /* Device services blacklist */
    private List<String> blacklistMap;
    /* List of virtual services */
    public List<KM200ServiceObject> virtualList;

    /* Is the first INIT done */
    protected boolean isIited;

    public KM200Device(HttpClient httpClient) {
        this.httpClient = httpClient;
        serviceTreeMap = new HashMap<String, KM200ServiceObject>();
        setBlacklistMap(new ArrayList<String>());
        getBlacklistMap().add("/gateway/firmware");
        virtualList = new ArrayList<KM200ServiceObject>();
        comCryption = new KM200Cryption(this);
        deviceCommunicator = new KM200Comm<KM200Device>(this, httpClient);
    }

    public Boolean isConfigured() {
        return StringUtils.isNotBlank(ip4Address) && cryptKeyPriv != null;
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
        md5Salt = DatatypeConverter.parseHexBinary(salt);
        comCryption.recreateKeys();
    }

    public void setCryptKeyPriv(String key) {
        cryptKeyPriv = DatatypeConverter.parseHexBinary(key);
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
        if (serviceTreeMap != null) {
            logger.debug("##################################################################");
            logger.debug("List of avalible services");
            logger.debug("readable;writeable;recordable;virtual;type;service;value;allowed;min;max;unit");
            printAllServices(serviceTreeMap);
            logger.debug("##################################################################");
        }

    }

    /**
     * This function outputs a ";" separated list of all on the device available services with its capabilities
     *
     * @param actTreeMap
     */
    public void printAllServices(Map<String, KM200ServiceObject> actTreeMap) {
        if (actTreeMap != null) {
            for (KM200ServiceObject object : actTreeMap.values()) {
                if (object != null) {
                    String val = "", type, valPara = "";
                    logger.debug("List type: {} service: {}", object.getServiceType(), object.getFullServiceName());
                    type = object.getServiceType();
                    if (type == null) {
                        type = new String();
                    }
                    if ("stringValue".equals(type) || "floatValue".equals(type)) {
                        val = object.getValue().toString();
                        if (object.getValueParameter() != null) {
                            if ("stringValue".equals(type)) {
                                // Type is definitely correct here
                                @SuppressWarnings("unchecked")
                                List<String> valParas = (List<String>) object.getValueParameter();
                                for (int i = 0; i < valParas.size(); i++) {
                                    if (i > 0) {
                                        valPara += "|";
                                    }
                                    valPara += valParas.get(i);
                                }
                                valPara += ";;;";
                            }
                            if ("floatValue".equals(type)) {
                                // Type is definitely correct here
                                @SuppressWarnings("unchecked")
                                List<Object> valParas = (List<Object>) object.getValueParameter();
                                valPara += ";";
                                valPara += valParas.get(0);
                                valPara += ";";
                                valPara += valParas.get(1);
                                valPara += ";";
                                if (valParas.size() == 3) {
                                    valPara += valParas.get(2);
                                }
                            }
                        } else {
                            valPara += ";;;";
                        }
                    } else {
                        val = "";
                        valPara = ";";
                    }
                    logger.debug("{};{};{};{};{};{};{};{}", object.getReadable(), object.getWriteable(),
                            object.getRecordable(), object.getVirtual(), type, object.getFullServiceName(), val,
                            valPara);
                    printAllServices(object.serviceTreeMap);
                }
            }
        }
    }

    /**
     * This function resets the update state on all service objects
     *
     * @param actTreeMap
     */
    public void resetAllUpdates(Map<String, KM200ServiceObject> actTreeMap) {
        if (actTreeMap != null) {
            for (KM200ServiceObject stmObject : actTreeMap.values()) {
                if (stmObject != null) {
                    stmObject.setUpdated(false);
                    resetAllUpdates(stmObject.serviceTreeMap);
                }
            }
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
            if (object.serviceTreeMap.containsKey(servicePath[i])) {
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
    public KM200ServiceObject getServiceObject(String service) {
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
            if (object.serviceTreeMap.containsKey(servicePath[i])) {
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
    public JsonObject getServiceNode(String service) {
        String decodedData = null;
        JsonObject nodeRoot = null;
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
                decodedData = new String();
                return nodeRoot;
            } else {
                decodedData = comCryption.decodeMessage(recData);
                if (decodedData == null) {
                    logger.error("Decoding of the KM200 message is not possible!");
                    return null;
                }
            }
            if (decodedData.length() > 0) {
                if ("SERVICE NOT AVAILABLE".equals(decodedData)) {
                    logger.debug("{}: SERVICE NOT AVAILABLE", service);
                    return null;
                } else {
                    nodeRoot = (JsonObject) jsonParser.parse(decodedData);
                }
            } else {
                logger.warn("Get empty reply");
                return null;
            }
        } catch (JsonParseException e) {
            logger.error("Parsingexception in JSON: {} service: {}", e.getMessage(), service);
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
        logger.debug("Encoding: {}", newObject);
        int retVal;
        byte[] encData = comCryption.encodeMessage(newObject.toString());
        if (encData == null) {
            logger.error("Couldn't encrypt data");
            return;
        }
        logger.debug("Send: {}", service);
        retVal = deviceCommunicator.sendDataToService(service, encData);
        if (retVal == 0) {
            logger.debug("Send to device failed: {}: {}", service, newObject);
        }
    }
}
