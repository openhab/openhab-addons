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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.km200.internal.KM200Device;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The KM200DataHandler is managing the data handling between the device and items
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200DataHandler {
    private final Logger logger = LoggerFactory.getLogger(KM200DataHandler.class);
    private final JsonParser jsonParser = new JsonParser();

    private final KM200Device remoteDevice;

    public KM200DataHandler(KM200Device remoteDevice) {
        this.remoteDevice = remoteDevice;
    }

    /**
     * This function checks the state of a service on the device
     */
    public State getProvidersState(String service, String itemType, Map<String, String> itemPara) {
        synchronized (remoteDevice) {
            String type = null;
            KM200ServiceObject object = null;
            JsonObject jsonNode;

            logger.debug("Check state of: {}  item: {}", service, itemType);
            if (remoteDevice.getBlacklistMap().contains(service)) {
                logger.debug("Service on blacklist: {}", service);
                return null;
            }
            if (remoteDevice.containsService(service)) {
                object = remoteDevice.getServiceObject(service);
                if (object.getReadable() == 0) {
                    logger.warn("Service is listed as protected (reading is not possible): {}", service);
                    return null;
                }
                type = object.getServiceType();
            } else {
                logger.warn("Service is not in the determined device service list: {}", service);
                return null;
            }
            /* For using of virtual services only one receive on the parent service is needed */
            if (!object.getUpdated()
                    || (object.getVirtual() == 1 && !remoteDevice.getServiceObject(object.getParent()).getUpdated())) {
                if (object.getVirtual() == 1) {
                    logger.debug("Receive data for an virtual object");
                    /* If it's a virtual service then receive the data from parent service */
                    jsonNode = remoteDevice.getServiceNode(object.getParent());
                } else {
                    logger.debug("Receive data");
                    jsonNode = remoteDevice.getServiceNode(service);
                }

                if (jsonNode == null || jsonNode.isJsonNull()) {
                    logger.error("Communication is not possible!");
                    return null;
                }
                if (object.getVirtual() == 1) {
                    remoteDevice.getServiceObject(object.getParent()).setJSONData(jsonNode);
                    remoteDevice.getServiceObject(object.getParent()).setUpdated(true);
                } else {
                    object.setJSONData(jsonNode);
                }
                object.setUpdated(true);
            } else {
                /* If already updated then use the saved data */
                if (object.getVirtual() == 1) {
                    logger.debug("Get data for an virtual object");
                    jsonNode = remoteDevice.getServiceObject(object.getParent()).getJSONData();
                } else {
                    logger.debug("Get data");
                    jsonNode = object.getJSONData();
                }
            }
            /* Data is received, now parsing it */
            return parseJSONData(jsonNode, type, service, itemType, itemPara);
        }
    }

    /**
     * This function parses the receviced JSON Data and return the right state
     */
    public State parseJSONData(JsonObject nodeRoot, String type, String service, String itemType,
            Map<String, String> itemPara) {
        State state = null;
        KM200ServiceObject object = remoteDevice.getServiceObject(service);
        logger.debug("parseJSONData service: {}, data: {}", service, nodeRoot);
        /* Now parsing of the JSON String depending on its type and the type of binding item */
        try {
            if (nodeRoot.toString().length() == 2) {
                logger.warn("Get empty reply");
                return null;
            }
            switch (type) {
                case "stringValue": /* Check whether the type is a single value containing a string value */
                    logger.debug("parseJSONData type string value: {} Type: {}", nodeRoot, itemType.toString());
                    String sVal = nodeRoot.get("value").getAsString();
                    object.setValue(sVal);
                    /* Switch Binding */
                    if ("Switch".equals(itemType)) {
                        // type is definitely correct here
                        Map<String, String> switchNames = itemPara;
                        if (switchNames.containsKey("on")) {
                            if (sVal.equals(switchNames.get("off"))) {
                                state = OnOffType.OFF;
                            } else if (sVal.equals(switchNames.get("on"))) {
                                state = OnOffType.ON;
                            }
                        } else if (switchNames.isEmpty()) {
                            logger.debug("No switch item configuration");
                            return null;
                        } else {
                            logger.warn("Switch-Item only on configured on/off string values: {}", nodeRoot);
                            return null;
                        }
                        /* NumberItem Binding */
                    } else if ("Number".equals(itemType)) {
                        try {
                            state = new DecimalType(Float.parseFloat(sVal));
                        } catch (NumberFormatException e) {
                            logger.error(
                                    "Conversion of the string value to Decimal wasn't possible, data: {} error: {}",
                                    nodeRoot, e.getMessage());
                            return null;
                        }
                        /* DateTimeItem Binding */
                    } else if ("DateTime".equals(itemType)) {
                        try {
                            state = new DateTimeType(sVal);
                        } catch (IllegalArgumentException e) {
                            logger.error(
                                    "Conversion of the string value to DateTime wasn't possible, data: {} error: {}",
                                    nodeRoot, e.getMessage());
                            return null;
                        }
                        /* StringItem Binding */
                    } else if ("String".equals(itemType)) {
                        state = new StringType(sVal);
                    } else {
                        logger.warn("Bindingtype not supported for string values: {}", itemType.getClass());
                        return null;
                    }
                    return state;
                case "floatValue": /* Check whether the type is a single value containing a float value */
                    logger.debug("state of type float value: {}", nodeRoot);
                    Object bdVal = null;
                    bdVal = nodeRoot.get("value");
                    try {
                        bdVal = nodeRoot.get("value").getAsBigDecimal();
                    } catch (NumberFormatException e) {
                        logger.debug("float value is a string: {}", bdVal);
                        bdVal = Double.NaN;
                    }
                    object.setValue(bdVal);
                    /* NumberItem Binding */
                    if ("Number".equals(itemType)) {
                        if (bdVal instanceof Double) { // Checking whether
                            state = new DecimalType(((Double) bdVal).floatValue());
                        } else {
                            state = new DecimalType(((BigDecimal) bdVal).floatValue());
                        }
                        /* StringItem Binding */
                    } else if ("String".equals(itemType)) {
                        state = new StringType(bdVal.toString());
                    } else {
                        logger.warn("Bindingtype not supported for float values: {}", itemType.getClass());
                        return null;
                    }
                    return state;

                case "switchProgram": /* Check whether the type is a switchProgram */
                    KM200SwitchProgramServiceHandler sPService = null;
                    logger.debug("state of type switchProgram: {}", nodeRoot);
                    /* Get the KM200SwitchProgramService class object with all specific parameters */
                    if (object.getVirtual() == 0) {
                        sPService = ((KM200SwitchProgramServiceHandler) object.getValueParameter());
                    } else {
                        sPService = ((KM200SwitchProgramServiceHandler) remoteDevice
                                .getServiceObject(object.getParent()).getValueParameter());
                    }
                    /* Update the switches inside the KM200SwitchProgramService */
                    sPService.updateSwitches(nodeRoot, remoteDevice);

                    /* the parsing of switch program-services have to be outside, using json in strings */
                    if (object.getVirtual() == 1) {
                        return this.getVirtualState(object, itemType, service);
                    } else {
                        /* if access to the parent non virtual service the return the switchPoints jsonarray */
                        if ("String".equals(itemType)) {
                            state = new StringType(nodeRoot.get("switchPoints").getAsJsonArray().toString());
                        } else {
                            logger.warn(
                                    "Bindingtype not supported for switchProgram, only json over strings supported: {}",
                                    itemType.getClass());
                            return null;
                        }
                        return state;
                    }

                case "errorList": /* Check whether the type is a errorList */
                    KM200ErrorServiceHandler eService = null;
                    logger.debug("state of type errorList: {}", nodeRoot);
                    /* Get the KM200ErrorService class object with all specific parameters */
                    if (object.getVirtual() == 0) {
                        eService = ((KM200ErrorServiceHandler) object.getValueParameter());
                    } else {
                        eService = ((KM200ErrorServiceHandler) remoteDevice.getServiceObject(object.getParent())
                                .getValueParameter());
                    }
                    /* Update the switches inside the KM200SwitchProgramService */
                    eService.updateErrors(nodeRoot);

                    /* the parsing of switch program-services have to be outside, using json in strings */
                    if (object.getVirtual() == 1) {
                        return this.getVirtualState(object, itemType, service);
                    } else {
                        /* if access to the parent non virtual service the return the switchPoints jsonarray */
                        if ("String".equals(itemType)) {
                            state = new StringType(nodeRoot.get("values").getAsJsonArray().toString());
                        } else {
                            logger.warn(
                                    "Bindingtype not supported for error list, only json over strings is supported: {}",
                                    itemType.getClass());
                            return null;
                        }
                        return state;
                    }

                case "yRecording": /* Check whether the type is a yRecording */
                    logger.info("state of: type yRecording is not supported yet: {}", nodeRoot);
                    /* have to be completed */
                    break;

                case "systeminfo": /* Check whether the type is a systeminfo */
                    logger.info("state of: type systeminfo is not supported yet: {}", nodeRoot);
                    /* have to be completed */
                    break;

                case "arrayData": /* Check whether the type is a arrayData */
                    logger.info("state of: type arrayData is not supported yet: {}", nodeRoot);
                    /* have to be completed */
                    break;

                case "eMonitoringList": /* Check whether the type is a eMonitoringList */
                    logger.info("state of: type eMonitoringList is not supported yet: {}", nodeRoot);
                    /* have to be completed */
                    break;
            }
        } catch (JsonParseException e) {
            logger.error("Parsingexception in JSON, data: {} error: {} ", nodeRoot, e.getMessage());
        }
        return null;
    }

    /**
     * This function checks the virtual state of a service
     */
    private State getVirtualState(KM200ServiceObject object, String itemType, String service) {
        State state = null;
        String type = object.getServiceType();
        logger.debug("Check virtual state of: {} type: {} item: {}", service, type, itemType);
        switch (type) {
            case "switchProgram":
                KM200SwitchProgramServiceHandler sPService = ((KM200SwitchProgramServiceHandler) remoteDevice
                        .getServiceObject(object.getParent()).getValueParameter());
                String[] servicePath = service.split("/");
                String virtService = servicePath[servicePath.length - 1];
                if ("weekday".equals(virtService)) {
                    if ("String".equals(itemType)) {
                        String val = sPService.getActiveDay();
                        if (val == null) {
                            return null;
                        }
                        state = new StringType(val);
                    } else {
                        logger.warn("Bindingtype not supported for day service: {}", itemType.getClass());
                        return null;
                    }
                } else if ("nbrCycles".equals(virtService)) {
                    if ("Number".equals(itemType)) {
                        Integer val = sPService.getNbrCycles();
                        if (val == null) {
                            return null;
                        }
                        state = new DecimalType(val);
                    } else {
                        logger.warn("Bindingtype not supported for nbrCycles service: {}", itemType.getClass());
                        return null;
                    }
                } else if ("cycle".equals(virtService)) {
                    if ("Number".equals(itemType)) {
                        Integer val = sPService.getActiveCycle();
                        if (val == null) {
                            return null;
                        }
                        state = new DecimalType(val);
                    } else {
                        logger.warn("Bindingtype not supported for cycle service: {}", itemType.getClass());
                        return null;
                    }
                } else if (virtService.equals(sPService.getPositiveSwitch())) {
                    if ("Number".equals(itemType)) {
                        Integer val = sPService.getActivePositiveSwitch();
                        if (val == null) {
                            return null;
                        }
                        state = new DecimalType(val);
                    } else if ("DateTime".equals(itemType)) {
                        Integer val = sPService.getActivePositiveSwitch();
                        if (val == null) {
                            return null;
                        }
                        Calendar rightNow = Calendar.getInstance();
                        Integer hour = val % 60;
                        Integer minute = val - (hour * 60);
                        rightNow.set(Calendar.HOUR_OF_DAY, hour);
                        rightNow.set(Calendar.MINUTE, minute);
                        state = new DateTimeType(rightNow);
                    } else {
                        logger.warn("Bindingtype not supported for cycle service: {}", itemType);
                        return null;
                    }
                } else if (virtService.equals(sPService.getNegativeSwitch())) {
                    if ("Number".equals(itemType)) {
                        Integer val = sPService.getActiveNegativeSwitch();
                        if (val == null) {
                            return null;
                        }
                        state = new DecimalType(val);
                    } else if ("DateTime".equals(itemType)) {
                        Integer val = sPService.getActiveNegativeSwitch();
                        if (val == null) {
                            return null;
                        }
                        Calendar rightNow = Calendar.getInstance();
                        Integer hour = val % 60;
                        Integer minute = val - (hour * 60);
                        rightNow.set(Calendar.HOUR_OF_DAY, hour);
                        rightNow.set(Calendar.MINUTE, minute);
                        state = new DateTimeType(rightNow);
                    } else {
                        logger.warn("Bindingtype not supported for cycle service: {}", itemType.getClass());
                        return null;
                    }
                }
                break;
            case "errorList":
                KM200ErrorServiceHandler eService = ((KM200ErrorServiceHandler) remoteDevice
                        .getServiceObject(object.getParent()).getValueParameter());
                String[] nServicePath = service.split("/");
                String nVirtService = nServicePath[nServicePath.length - 1];
                /* Go through the parameters and read the values */
                switch (nVirtService) {
                    case "nbrErrors":
                        if ("Number".equals(itemType)) {
                            Integer val = eService.getNbrErrors();
                            state = new DecimalType(val);
                        } else {
                            logger.warn("Bindingtype not supported for error number service: {}", itemType.getClass());
                            return null;
                        }
                        break;
                    case "error":
                        if ("Number".equals(itemType)) {
                            Integer val = eService.getActiveError();
                            state = new DecimalType(val);
                        } else {
                            logger.warn("Bindingtype not supported for error service: {}", itemType.getClass());
                            return null;
                        }
                        break;
                    case "errorString":
                        if ("String".equals(itemType)) {
                            String val = eService.getErrorString();
                            if (val == null) {
                                return null;
                            }
                            state = new StringType(val);
                        } else {
                            logger.warn("Bindingtype not supported for error string service: {}", itemType.getClass());
                            return null;
                        }
                        break;
                }
                break;
        }
        return state;
    }

    /**
     * This function sets the state of a service on the device
     */
    public JsonObject sendProvidersState(String service, Command command, String itemType, Object itemPara) {
        synchronized (remoteDevice) {
            String type;
            KM200ServiceObject object;
            JsonObject newObject;

            logger.debug("Prepare item for send: {} zitem: {}", service, itemType);
            if (remoteDevice.getBlacklistMap().contains(service)) {
                logger.debug("Service on blacklist: {}", service);
                return null;
            }
            if (remoteDevice.containsService(service)) {
                if (remoteDevice.getServiceObject(service).getWriteable() == 0) {
                    logger.error("Service is listed as read-only: {}", service);
                    return null;
                }
                object = remoteDevice.getServiceObject(service);
                type = object.getServiceType();
            } else {
                logger.error("Service is not in the determined device service list: {}", service);
                return null;
            }
            /* The service is availible, set now the values depeding on the item and binding type */
            logger.debug("state of: {} type: {}", command, type);
            /* Binding is a NumberItem */
            if ("Number".equals(itemType)) {
                BigDecimal bdVal = ((DecimalType) command).toBigDecimal();
                logger.debug("val: {}", bdVal);
                /* Check the capabilities of this service */
                if (object.getValueParameter() != null) {
                    // type is definitely correct here
                    @SuppressWarnings("unchecked")
                    List<BigDecimal> valParas = (List<BigDecimal>) object.getValueParameter();
                    BigDecimal minVal = valParas.get(0);
                    BigDecimal maxVal = valParas.get(1);
                    if (bdVal.compareTo(minVal) < 0) {
                        bdVal = minVal;
                    }
                    if (bdVal.compareTo(maxVal) > 0) {
                        bdVal = maxVal;
                    }
                }
                newObject = new JsonObject();
                if ("floatValue".equals(type)) {
                    newObject.addProperty("value", bdVal);
                } else if ("stringValue".equals(type)) {
                    newObject.addProperty("value", bdVal.toString());
                } else if ("switchProgram".equals(type) && object.getVirtual() == 1) {
                    /* A switchProgram as NumberItem is always virtual */
                    newObject = sendVirtualState(object, service, command, itemType);
                } else if ("errorList".equals(type) && object.getVirtual() == 1) {
                    /* A errorList as NumberItem is always virtual */
                    newObject = sendVirtualState(object, service, command, itemType);
                } else {
                    logger.warn("Not supported type for numberItem: {}", type);
                }
                /* Binding is a StringItem */
            } else if ("String".equals(itemType)) {
                String val = ((StringType) command).toString();
                newObject = new JsonObject();
                /* Check the capabilities of this service */
                if (object.getValueParameter() != null) {
                    // type is definitely correct here
                    @SuppressWarnings("unchecked")
                    List<String> valParas = (List<String>) object.getValueParameter();
                    if (!valParas.contains(val)) {
                        logger.warn("Parameter is not in the service parameterlist: {}", val);
                        return null;
                    }
                }
                if ("stringValue".equals(type)) {
                    newObject.addProperty("value", val);
                } else if ("floatValue".equals(type)) {
                    newObject.addProperty("value", Float.parseFloat(val));
                } else if ("switchProgram".equals(type)) {
                    if (object.getVirtual() == 1) {
                        newObject = sendVirtualState(object, service, command, itemType);
                    } else {
                        /* The JSONArray of switch items can be send directly */
                        try {
                            /* Check whether this input string is a valid JSONArray */
                            JsonArray userArray = (JsonArray) jsonParser.parse(val);
                            newObject = userArray.getAsJsonObject();
                        } catch (JsonParseException e) {
                            logger.warn("The input for the switchProgram is not a valid JSONArray : {}",
                                    e.getMessage());
                            return null;
                        }
                    }
                } else {
                    logger.warn("Not supported type for stringItem: {}", type);
                }
                /* Binding is a DateTimeItem */
            } else if ("DateTime".equals(itemType)) {
                String val = ((DateTimeType) command).toString();
                newObject = new JsonObject();
                if ("stringValue".equals(type)) {
                    newObject.addProperty("value", val);
                } else if ("switchProgram".equals(type)) {
                    newObject = sendVirtualState(object, service, command, itemType);
                } else {
                    logger.warn("Not supported type for dateTimeItem: {}", type);
                }
            } else if ("Switch".equals(itemType)) {
                String val = null;
                newObject = new JsonObject();
                // type is definitely correct here
                @SuppressWarnings("unchecked")
                Map<String, String> switchNames = (HashMap<String, String>) itemPara;
                if (switchNames.containsKey("on")) {
                    if (command == OnOffType.OFF) {
                        val = switchNames.get("off");
                    } else if (command == OnOffType.ON) {
                        val = switchNames.get("on");
                    }
                    // type is definitely correct here
                    @SuppressWarnings("unchecked")
                    List<String> valParas = (List<String>) object.getValueParameter();
                    if (!valParas.contains(val)) {
                        logger.warn("Parameter is not in the service parameterlist: {}", val);
                        return null;
                    }
                } else if (switchNames.isEmpty()) {
                    logger.debug("No switch item configuration");
                    return null;
                } else {
                    logger.warn("Switch-Item only on configured on/off string values {}", command);
                    return null;
                }
                if ("stringValue".equals(type)) {
                    newObject.addProperty("value", val);
                } else {
                    logger.warn("Not supported type for SwitchItem:{}", type);
                }
            } else {
                logger.warn("Bindingtype not supported: {}", itemType.getClass());
                return null;
            }
            /* If some data is availible then we have to send it to device */
            if (newObject != null && newObject.toString().length() > 2) {
                logger.debug("Send Data: {}", newObject);
                return newObject;
            } else {
                return null;
            }
        }
    }

    /**
     * This function sets the state of a virtual service
     */
    public JsonObject sendVirtualState(KM200ServiceObject object, String service, Command command, String itemType) {
        JsonObject newObject = null;
        String type = null;
        logger.debug("Check virtual state of: {} type: {} item: {}", service, type, itemType);
        KM200ServiceObject parObject = remoteDevice.getServiceObject(object.getParent());
        type = object.getServiceType();
        /* Binding is a StringItem */
        if ("String".equals(itemType)) {
            String val = ((StringType) command).toString();
            switch (type) {
                case "switchProgram":
                    KM200SwitchProgramServiceHandler sPService = ((KM200SwitchProgramServiceHandler) parObject
                            .getValueParameter());
                    String[] servicePath = service.split("/");
                    String virtService = servicePath[servicePath.length - 1];
                    if ("weekday".equals(virtService)) {
                        /* Only parameter changing without communication to device */
                        sPService.setActiveDay(val);
                    }
                    break;
            }
            /* Binding is a NumberItem */
        } else if ("Number".equals(itemType)) {
            Integer val = ((DecimalType) command).intValue();
            switch (type) {
                case "switchProgram":
                    KM200SwitchProgramServiceHandler sPService = ((KM200SwitchProgramServiceHandler) parObject
                            .getValueParameter());
                    String[] servicePath = service.split("/");
                    String virtService = servicePath[servicePath.length - 1];
                    if ("cycle".equals(virtService)) {
                        /* Only parameter changing without communication to device */
                        sPService.setActiveCycle(val);
                    } else if (virtService.equals(sPService.getPositiveSwitch())) {
                        sPService.setActivePositiveSwitch(val);
                        /* Create a JSON Array from current switch configuration */
                        newObject = sPService.getUpdatedJSONData(parObject);
                    } else if (virtService.equals(sPService.getNegativeSwitch())) {
                        sPService.setActiveNegativeSwitch(val);
                        /* Create a JSON Array from current switch configuration */
                        newObject = sPService.getUpdatedJSONData(parObject);
                    }
                    break;
                case "errorList":
                    KM200ErrorServiceHandler eService = ((KM200ErrorServiceHandler) remoteDevice
                            .getServiceObject(object.getParent()).getValueParameter());
                    String[] nServicePath = service.split("/");
                    String nVirtService = nServicePath[nServicePath.length - 1];
                    if ("error".equals(nVirtService)) {
                        /* Only parameter changing without communication to device */
                        eService.setActiveError(val);
                    }
                    break;
            }
        } else if ("DateTime".equals(itemType)) {
            Calendar cal = ((DateTimeType) command).getCalendar();
            KM200SwitchProgramServiceHandler sPService = ((KM200SwitchProgramServiceHandler) parObject
                    .getValueParameter());
            String[] servicePath = service.split("/");
            String virtService = servicePath[servicePath.length - 1];
            Integer minutes;
            if (virtService.equals(sPService.getPositiveSwitch())) {
                minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
                minutes = (minutes % sPService.getSwitchPointTimeRaster()) * sPService.getSwitchPointTimeRaster();
                sPService.setActivePositiveSwitch(minutes);
                /* Create a JSON Array from current switch configuration */
                newObject = sPService.getUpdatedJSONData(parObject);
            }
            if (virtService.equals(sPService.getNegativeSwitch())) {
                minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
                minutes = (minutes % sPService.getSwitchPointTimeRaster()) * sPService.getSwitchPointTimeRaster();
                sPService.setActiveNegativeSwitch(minutes);
                /* Create a JSON Array from current switch configuration */
                newObject = sPService.getUpdatedJSONData(parObject);
            }
        }
        return newObject;
    }
}
