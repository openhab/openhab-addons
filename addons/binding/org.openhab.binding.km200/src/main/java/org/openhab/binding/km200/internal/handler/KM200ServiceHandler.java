/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.km200.internal.handler;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.km200.internal.KM200Device;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The KM200DataHandler is representing one service on the device
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200ServiceHandler {

    private final Logger logger = LoggerFactory.getLogger(KM200ServiceHandler.class);

    private final String service;
    private final KM200ServiceObject parent;
    private final KM200Device remoteDevice;

    private KM200ServiceObject serviceObject;
    private JsonObject nodeRoot;

    public KM200ServiceHandler(String service, KM200ServiceObject parent, KM200Device remoteDevice) {
        this.service = service;
        this.parent = parent;
        this.remoteDevice = remoteDevice;
    }

    /**
     * This function starts the object's initialization
     */
    public void initObject() {
        if (remoteDevice.getBlacklistMap().contains(service)) {
            logger.debug("Blacklisted: {}", service);
            return;
        }
        nodeRoot = remoteDevice.getServiceNode(service);
        if (nodeRoot == null) {
            logger.debug("initDevice: nodeRoot == null for service: {}", service);
            return;
        }
        createServiceObject();
        determineServiceObject();
    }

    /**
     * This function checks the flags of a service on the device and creates a KM200CommObject
     */
    public void createServiceObject() {
        String id = null, type = null;
        Integer writeable = 0;
        Integer recordable = 0;
        Integer readable = 1;
        /* check whether the node is an empty one */
        if (nodeRoot.toString().length() == 2) {
            readable = 0;
            id = service;
            type = "$$PROTECTED$$";
        } else {
            type = nodeRoot.get("type").getAsString();
            id = nodeRoot.get("id").getAsString();
        }
        /* Check the service features and set the flags */
        if (nodeRoot.has("writeable")) {
            Integer val = nodeRoot.get("writeable").getAsInt();
            logger.debug("writable: {}", val);
            writeable = val;
        }
        if (nodeRoot.has("recordable")) {
            Integer val = nodeRoot.get("recordable").getAsInt();
            logger.debug("recordable: {}", val);
            recordable = val;
        }
        logger.debug("Typ: {}", type);
        serviceObject = new KM200ServiceObject(id, type, readable, writeable, recordable, 0, null, parent);
        serviceObject.setJSONData(nodeRoot);
    }

    /**
     * This function determines the service's capabilities
     */
    public void determineServiceObject() {
        /* Check the service features and set the flags */
        String id = null;
        Object valObject = null;
        JsonObject dataObject = serviceObject.getJSONData();
        switch (serviceObject.getServiceType()) {
            case "stringValue": /*
                                 * Check whether the type is a single value containing a
                                 * string value
                                 */
                logger.debug("initDevice: type string value: {}", dataObject);
                valObject = new String(nodeRoot.get("value").getAsString());
                serviceObject.setValue(valObject);
                if (nodeRoot.has("allowedValues")) {
                    List<String> valParas = new ArrayList<String>();
                    JsonArray paras = nodeRoot.get("allowedValues").getAsJsonArray();
                    for (int i = 0; i < paras.size(); i++) {
                        String subJSON = paras.get(i).getAsString();
                        valParas.add(subJSON);
                    }
                    serviceObject.setValueParameter(valParas);
                }
                break;

            case "floatValue": /* Check whether the type is a single value containing a float value */
                logger.debug("initDevice: type float value: {}", dataObject);
                valObject = nodeRoot.get("value");
                try {
                    valObject = nodeRoot.get("value").getAsBigDecimal();
                    serviceObject.setValue(valObject);
                } catch (NumberFormatException e) {
                    logger.debug("float value is a string: {}", valObject);
                    Double tmpObj = Double.NaN;
                    serviceObject.setValue(tmpObj);
                }
                if (nodeRoot.has("minValue") && nodeRoot.has("maxValue")) {
                    List<Object> valParas = new ArrayList<Object>();
                    valParas.add(nodeRoot.get("minValue").getAsBigDecimal());
                    valParas.add(nodeRoot.get("maxValue").getAsBigDecimal());
                    if (nodeRoot.has("unitOfMeasure")) {
                        valParas.add(nodeRoot.get("unitOfMeasure").getAsString());
                    }
                    serviceObject.setValueParameter(valParas);
                }
                break;

            case "switchProgram": /* Check whether the type is a switchProgram */
                logger.debug("initDevice: type switchProgram {}", dataObject);
                KM200SwitchProgramServiceHandler sPService = new KM200SwitchProgramServiceHandler();
                sPService.setMaxNbOfSwitchPoints(nodeRoot.get("maxNbOfSwitchPoints").getAsInt());
                sPService.setMaxNbOfSwitchPointsPerDay(nodeRoot.get("maxNbOfSwitchPointsPerDay").getAsInt());
                sPService.setSwitchPointTimeRaster(nodeRoot.get("switchPointTimeRaster").getAsInt());
                JsonObject propObject = nodeRoot.get("setpointProperty").getAsJsonObject();
                sPService.setSetpointProperty(propObject.get("id").getAsString());
                serviceObject.setValueParameter(sPService);
                serviceObject.setJSONData(dataObject);
                remoteDevice.virtualList.add(serviceObject);
                break;

            case "errorList": /* Check whether the type is a errorList */
                logger.debug("initDevice: type errorList: {}", dataObject);
                KM200ErrorServiceHandler eService = new KM200ErrorServiceHandler();
                eService.updateErrors(nodeRoot);
                serviceObject.setValueParameter(eService);
                serviceObject.setJSONData(dataObject);
                remoteDevice.virtualList.add(serviceObject);
                break;

            case "refEnum": /* Check whether the type is a refEnum */
                logger.debug("initDevice: type refEnum: {}", dataObject);
                JsonArray refers = nodeRoot.get("references").getAsJsonArray();
                for (int i = 0; i < refers.size(); i++) {
                    JsonObject subJSON = refers.get(i).getAsJsonObject();
                    id = subJSON.get("id").getAsString();
                    KM200ServiceHandler serviceHandler = new KM200ServiceHandler(id, serviceObject, remoteDevice);
                    serviceHandler.initObject();

                }
                break;

            case "moduleList": /* Check whether the type is a moduleList */
                logger.debug("initDevice: type moduleList: {}", dataObject);
                JsonArray vals = nodeRoot.get("values").getAsJsonArray();
                for (int i = 0; i < vals.size(); i++) {
                    JsonObject subJSON = vals.get(i).getAsJsonObject();
                    id = subJSON.get("id").getAsString();
                    KM200ServiceHandler serviceHandler = new KM200ServiceHandler(id, serviceObject, remoteDevice);
                    serviceHandler.initObject();
                }
                break;

            case "yRecording": /* Check whether the type is a yRecording */
                logger.debug("initDevice: type yRecording: {}", dataObject);
                /* have to be completed */
                break;

            case "systeminfo": /* Check whether the type is a systeminfo */
                logger.debug("initDevice: type systeminfo: {}", dataObject);
                JsonArray sInfo = nodeRoot.get("values").getAsJsonArray();
                serviceObject.setValue(sInfo);
                /* have to be completed */
                break;
            case "arrayData":
                logger.debug("initDevice: type arrayData: {}", dataObject);
                serviceObject.setJSONData(dataObject);
                /* have to be completed */
                break;

            case "eMonitoringList":
                logger.debug("initDevice: type eMonitoringList: {}", dataObject);
                serviceObject.setJSONData(dataObject);
                /* have to be completed */
                break;

            case "$$PROTECTED$$":
                logger.debug("initDevice: readonly");
                serviceObject.setJSONData(dataObject);
                break;

            default: /* Unknown type */
                logger.info("initDevice: type: {} unknown for service: {} Data: {}", serviceObject.getServiceType(),
                        service, dataObject);
        }

        String[] servicePath = service.split("/");
        if (parent == null) {
            remoteDevice.serviceTreeMap.put(servicePath[servicePath.length - 1], serviceObject);
        } else {
            parent.serviceTreeMap.put(servicePath[servicePath.length - 1], serviceObject);
        }
    }
}
