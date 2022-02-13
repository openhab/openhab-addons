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
package org.openhab.binding.km200.internal.handler;

import static org.openhab.binding.km200.internal.KM200BindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.km200.internal.KM200Device;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
@NonNullByDefault
public class KM200DataHandler {
    private final Logger logger = LoggerFactory.getLogger(KM200DataHandler.class);

    private final KM200Device remoteDevice;

    public KM200DataHandler(KM200Device remoteDevice) {
        this.remoteDevice = remoteDevice;
    }

    /**
     * This function checks the state of a service on the device
     */
    public @Nullable State getProvidersState(String service, String itemType, Map<String, String> itemPara) {
        synchronized (remoteDevice) {
            String type = null;
            KM200ServiceObject object = null;
            JsonObject jsonNode = null;

            logger.trace("Check state of: {}  item: {}", service, itemType);
            if (remoteDevice.getBlacklistMap().contains(service)) {
                logger.warn("Service on blacklist: {}", service);
                return null;
            }
            if (remoteDevice.containsService(service)) {
                object = remoteDevice.getServiceObject(service);
                if (null == object) {
                    logger.warn("Serviceobject does not exist");
                    return null;
                }
                if (object.getReadable() == 0) {
                    logger.warn("Service is listed as protected (reading is not possible): {}", service);
                    return null;
                }
                type = object.getServiceType();
            } else {
                logger.warn("Service is not in the determined device service list: {}", service);
                return null;
            }
            /* Needs to be updated? */
            if (object.getVirtual() == 0) {
                if (!object.getUpdated()) {
                    jsonNode = remoteDevice.getServiceNode(service);
                    if (jsonNode == null || jsonNode.isJsonNull()) {
                        logger.warn("Communication is not possible!");
                        return null;
                    }
                    object.setJSONData(jsonNode);
                    object.setUpdated(true);
                } else {
                    /* If already updated then use the saved data */
                    jsonNode = object.getJSONData();
                }
            } else {
                /* For using of virtual services only one receive on the parent service is needed */
                String parent = object.getParent();
                if (null != parent) {
                    KM200ServiceObject objParent = remoteDevice.getServiceObject(parent);
                    if (null != objParent) {
                        if (!objParent.getUpdated()) {
                            /* If it's a virtual service then receive the data from parent service */
                            jsonNode = remoteDevice.getServiceNode(parent);
                            if (jsonNode == null || jsonNode.isJsonNull()) {
                                logger.warn("Communication is not possible!");
                                return null;
                            }
                            objParent.setJSONData(jsonNode);
                            objParent.setUpdated(true);
                            object.setUpdated(true);
                        } else {
                            /* If already updated then use the saved data */
                            jsonNode = objParent.getJSONData();
                        }
                    }
                }
            }
            if (null != jsonNode) {
                return parseJSONData(jsonNode, type, service, itemType, itemPara);
            } else {
                return null;
            }
        }
    }

    /**
     * This function parses the receviced JSON Data and return the right state
     */
    public @Nullable State parseJSONData(JsonObject nodeRoot, String type, String service, String itemType,
            Map<String, String> itemPara) {
        State state = null;
        KM200ServiceObject object = remoteDevice.getServiceObject(service);
        if (null == object) {
            return null;
        }
        String parent = object.getParent();

        logger.trace("parseJSONData service: {}, data: {}", service, nodeRoot);
        /* Now parsing of the JSON String depending on its type and the type of binding item */
        try {
            if (nodeRoot.toString().length() == 2) {
                logger.warn("Get empty reply");
                return null;
            }
            switch (type) {
                case DATA_TYPE_STRING_VALUE: /* Check whether the type is a single value containing a string value */
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
                    } else if (CoreItemFactory.NUMBER.equals(itemType)) {
                        try {
                            state = new DecimalType(Float.parseFloat(sVal));
                        } catch (NumberFormatException e) {
                            logger.warn("Conversion of the string value to Decimal wasn't possible, data: {} error: {}",
                                    nodeRoot, e.getMessage());
                            return null;
                        }
                        /* DateTimeItem Binding */
                    } else if (CoreItemFactory.DATETIME.equals(itemType)) {
                        try {
                            state = new DateTimeType(sVal);
                        } catch (IllegalArgumentException e) {
                            logger.warn(
                                    "Conversion of the string value to DateTime wasn't possible, data: {} error: {}",
                                    nodeRoot, e.getMessage());
                            return null;
                        }
                        /* StringItem Binding */
                    } else if (CoreItemFactory.STRING.equals(itemType)) {
                        state = new StringType(sVal);
                    } else {
                        logger.info("Bindingtype not supported for string values: {}", itemType.getClass());
                        return null;
                    }
                    return state;
                case DATA_TYPE_FLOAT_VALUE: /* Check whether the type is a single value containing a float value */
                    logger.trace("state of type float value: {}", nodeRoot);
                    Object bdVal = null;
                    try {
                        bdVal = new BigDecimal(nodeRoot.get("value").getAsString()).setScale(1, RoundingMode.HALF_UP);
                    } catch (NumberFormatException e) {
                        bdVal = Double.NaN;
                    }
                    object.setValue(bdVal);
                    /* NumberItem Binding */
                    if (CoreItemFactory.NUMBER.equals(itemType)) {
                        if (bdVal instanceof Double) { // Checking whether
                            state = new DecimalType((Double) bdVal);
                        } else {
                            state = new DecimalType(((Number) bdVal).doubleValue());
                        }
                        /* StringItem Binding */
                    } else if (CoreItemFactory.STRING.equals(itemType)) {
                        state = new StringType(bdVal.toString());
                    } else {
                        logger.info("Bindingtype not supported for float values: {}", itemType.getClass());
                        return null;
                    }
                    return state;
                case DATA_TYPE_SWITCH_PROGRAM: /* Check whether the type is a switchProgram */
                    KM200SwitchProgramServiceHandler sPService = null;
                    logger.trace("state of type switchProgram: {}", nodeRoot);
                    if (null != parent) {
                        KM200ServiceObject objParent = remoteDevice.getServiceObject(parent);
                        if (null != objParent) {
                            /* Get the KM200SwitchProgramService class object with all specific parameters */
                            if (object.getVirtual() == 0) {
                                sPService = ((KM200SwitchProgramServiceHandler) object.getValueParameter());
                            } else {
                                sPService = ((KM200SwitchProgramServiceHandler) objParent.getValueParameter());
                            }
                            if (null != sPService) {
                                /* Update the switches inside the KM200SwitchProgramService */
                                sPService.updateSwitches(nodeRoot, remoteDevice);

                                /* the parsing of switch program-services have to be outside, using json in strings */
                                if (object.getVirtual() == 1) {
                                    return this.getVirtualState(object, itemType, service);
                                } else {
                                    /*
                                     * if access to the parent non virtual service the return the switchPoints jsonarray
                                     */
                                    if (CoreItemFactory.STRING.equals(itemType)) {
                                        state = new StringType(
                                                nodeRoot.get("switchPoints").getAsJsonArray().toString());
                                    } else {
                                        logger.info(
                                                "Bindingtype not supported for switchProgram, only json over strings supported: {}",
                                                itemType.getClass());
                                        return null;
                                    }
                                    return state;
                                }
                            }
                        }
                    }
                    return null;
                case DATA_TYPE_ERROR_LIST: /* Check whether the type is a errorList */
                    KM200ErrorServiceHandler eService = null;
                    logger.trace("state of type errorList: {}", nodeRoot);
                    if (null != parent) {
                        KM200ServiceObject objParent = remoteDevice.getServiceObject(parent);
                        if (null != objParent) {
                            /* Get the KM200ErrorService class object with all specific parameters */
                            if (object.getVirtual() == 0) {
                                eService = ((KM200ErrorServiceHandler) object.getValueParameter());
                            } else {
                                eService = ((KM200ErrorServiceHandler) objParent.getValueParameter());
                            }
                            if (null != eService) {
                                /* Update the switches inside the KM200SwitchProgramService */
                                eService.updateErrors(nodeRoot);

                                /* the parsing of switch program-services have to be outside, using json in strings */
                                if (object.getVirtual() == 1) {
                                    return this.getVirtualState(object, itemType, service);
                                } else {
                                    /*
                                     * if access to the parent non virtual service the return the switchPoints jsonarray
                                     */
                                    if (CoreItemFactory.STRING.equals(itemType)) {
                                        state = new StringType(nodeRoot.get("values").getAsJsonArray().toString());
                                    } else {
                                        logger.info(
                                                "Bindingtype not supported for error list, only json over strings is supported: {}",
                                                itemType.getClass());
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                case DATA_TYPE_Y_RECORDING: /* Check whether the type is a yRecording */
                    logger.info("state of: type yRecording is not supported yet: {}", nodeRoot);
                    /* have to be completed */
                    break;
                case DATA_TYPE_SYSTEM_INFO: /* Check whether the type is a systeminfo */
                    logger.info("state of: type systeminfo is not supported yet: {}", nodeRoot);
                    /* have to be completed */
                    break;
                case DATA_TYPE_ARRAY_DATA: /* Check whether the type is a arrayData */
                    logger.info("state of: type arrayData is not supported yet: {}", nodeRoot);
                    /* have to be completed */
                    break;
                case DATA_TYPE_E_MONITORING_LIST: /* Check whether the type is a eMonitoringList */
                    logger.info("state of: type eMonitoringList is not supported yet: {}", nodeRoot);
                    /* have to be completed */
                    break;
            }
        } catch (JsonParseException e) {
            logger.warn("Parsingexception in JSON, data: {} error: {} ", nodeRoot, e.getMessage());
        }
        return null;
    }

    /**
     * This function checks the virtual state of a service
     */
    private @Nullable State getVirtualState(KM200ServiceObject object, String itemType, String service) {
        State state = null;
        String type = object.getServiceType();
        String parent = object.getParent();

        if (null != parent) {
            KM200ServiceObject objParent = remoteDevice.getServiceObject(parent);
            if (null != objParent) {
                logger.trace("Check virtual state of: {} type: {} item: {}", service, type, itemType);
                switch (type) {
                    case DATA_TYPE_SWITCH_PROGRAM:
                        KM200SwitchProgramServiceHandler sPService = ((KM200SwitchProgramServiceHandler) objParent
                                .getValueParameter());
                        if (null != sPService) {
                            String[] servicePath = service.split("/");
                            String virtService = servicePath[servicePath.length - 1];
                            if ("weekday".equals(virtService)) {
                                if (CoreItemFactory.STRING.equals(itemType)) {
                                    String actDay = sPService.getActiveDay();
                                    state = new StringType(actDay);
                                } else {
                                    logger.info("Bindingtype not supported for day service: {}", itemType.getClass());
                                    return null;
                                }
                            } else if ("nbrCycles".equals(virtService)) {
                                if (CoreItemFactory.NUMBER.equals(itemType)) {
                                    Integer nbrCycles = sPService.getNbrCycles();
                                    state = new DecimalType(nbrCycles);
                                } else {
                                    logger.info("Bindingtype not supported for nbrCycles service: {}",
                                            itemType.getClass());
                                    return null;
                                }
                            } else if ("cycle".equals(virtService)) {
                                if (CoreItemFactory.NUMBER.equals(itemType)) {
                                    Integer cycle = sPService.getActiveCycle();
                                    state = new DecimalType(cycle);
                                } else {
                                    logger.info("Bindingtype not supported for cycle service: {}", itemType.getClass());
                                    return null;
                                }
                            } else if (virtService.equals(sPService.getPositiveSwitch())) {
                                if (CoreItemFactory.NUMBER.equals(itemType)) {
                                    Integer minutes = sPService.getActivePositiveSwitch();
                                    state = new DecimalType(minutes);
                                } else if (CoreItemFactory.DATETIME.equals(itemType)) {
                                    Integer minutes = sPService.getActivePositiveSwitch();
                                    ZonedDateTime rightNow = ZonedDateTime.now();
                                    rightNow.minusHours(rightNow.getHour());
                                    rightNow.minusMinutes(rightNow.getMinute());
                                    rightNow.plusSeconds(minutes * 60 - rightNow.getOffset().getTotalSeconds());
                                    state = new DateTimeType(rightNow);
                                } else {
                                    logger.info("Bindingtype not supported for cycle service: {}", itemType);
                                    return null;
                                }
                            } else if (virtService.equals(sPService.getNegativeSwitch())) {
                                if (CoreItemFactory.NUMBER.equals(itemType)) {
                                    Integer minutes = sPService.getActiveNegativeSwitch();
                                    state = new DecimalType(minutes);
                                } else if (CoreItemFactory.DATETIME.equals(itemType)) {
                                    Integer minutes = sPService.getActiveNegativeSwitch();
                                    ZonedDateTime rightNow = ZonedDateTime.now();
                                    rightNow.minusHours(rightNow.getHour());
                                    rightNow.minusMinutes(rightNow.getMinute());
                                    rightNow.plusSeconds(minutes * 60 - rightNow.getOffset().getTotalSeconds());
                                    state = new DateTimeType(rightNow);
                                } else {
                                    logger.info("Bindingtype not supported for cycle service: {}", itemType.getClass());
                                    return null;
                                }
                            }
                            return state;
                        } else {
                            return null;
                        }
                    case DATA_TYPE_ERROR_LIST:
                        KM200ErrorServiceHandler eService = ((KM200ErrorServiceHandler) objParent.getValueParameter());
                        if (null != eService) {
                            String[] nServicePath = service.split("/");
                            String nVirtService = nServicePath[nServicePath.length - 1];
                            /* Go through the parameters and read the values */
                            switch (nVirtService) {
                                case "nbrErrors":
                                    if (CoreItemFactory.NUMBER.equals(itemType)) {
                                        Integer nbrErrors = eService.getNbrErrors();
                                        state = new DecimalType(nbrErrors);
                                    } else {
                                        logger.info("Bindingtype not supported for error number service: {}",
                                                itemType.getClass());
                                        return null;
                                    }
                                    break;
                                case "error":
                                    if (CoreItemFactory.NUMBER.equals(itemType)) {
                                        Integer actError = eService.getActiveError();
                                        state = new DecimalType(actError);
                                    } else {
                                        logger.info("Bindingtype not supported for error service: {}",
                                                itemType.getClass());
                                        return null;
                                    }
                                    break;
                                case "errorString":
                                    if (CoreItemFactory.STRING.equals(itemType)) {
                                        String errorString = eService.getErrorString();
                                        if (errorString == null) {
                                            return null;
                                        }
                                        state = new StringType(errorString);
                                    } else {
                                        logger.info("Bindingtype not supported for error string service: {}",
                                                itemType.getClass());
                                        return null;
                                    }
                                    break;
                            }
                            return state;
                        } else {
                            return null;
                        }
                }
            }
        }
        return null;
    }

    /**
     * This function sets the state of a service on the device
     */
    public @Nullable JsonObject sendProvidersState(String service, Command command, String itemType, Object itemPara) {
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
                object = remoteDevice.getServiceObject(service);
                if (null == object) {
                    logger.debug("Object is null");
                    return null;
                }
                if (object.getWriteable() == 0) {
                    logger.warn("Service is listed as read-only: {}", service);
                    return null;
                }
                type = object.getServiceType();
            } else {
                logger.warn("Service is not in the determined device service list: {}", service);
                return null;
            }
            /* The service is availible, set now the values depeding on the item and binding type */
            logger.trace("state of: {} type: {}", command, type);
            /* Binding is a NumberItem */
            if (CoreItemFactory.NUMBER.equals(itemType)) {
                BigDecimal bdVal = ((DecimalType) command).toBigDecimal();
                /* Check the capabilities of this service */
                if (object.getValueParameter() != null) {
                    // type is definitely correct here
                    @SuppressWarnings("unchecked")
                    List<BigDecimal> valParas = (List<BigDecimal>) object.getValueParameter();
                    if (null != valParas) {
                        BigDecimal minVal = valParas.get(0);
                        BigDecimal maxVal = valParas.get(1);
                        if (bdVal.compareTo(minVal) < 0) {
                            bdVal = minVal;
                        }
                        if (bdVal.compareTo(maxVal) > 0) {
                            bdVal = maxVal;
                        }
                    }
                }
                newObject = new JsonObject();
                if (DATA_TYPE_FLOAT_VALUE.equals(type)) {
                    newObject.addProperty("value", bdVal);
                } else if (DATA_TYPE_STRING_VALUE.equals(type)) {
                    newObject.addProperty("value", bdVal.toString());
                } else if (DATA_TYPE_SWITCH_PROGRAM.equals(type) && object.getVirtual() == 1) {
                    /* A switchProgram as NumberItem is always virtual */
                    newObject = sendVirtualState(object, service, command, itemType);
                } else if (DATA_TYPE_ERROR_LIST.equals(type) && object.getVirtual() == 1) {
                    /* A errorList as NumberItem is always virtual */
                    newObject = sendVirtualState(object, service, command, itemType);
                } else {
                    logger.info("Not supported type for numberItem: {}", type);
                }
                /* Binding is a StringItem */
            } else if (CoreItemFactory.STRING.equals(itemType)) {
                String val = ((StringType) command).toString();
                newObject = new JsonObject();
                /* Check the capabilities of this service */
                if (object.getValueParameter() != null) {
                    // type is definitely correct here
                    @SuppressWarnings("unchecked")
                    List<String> valParas = (List<String>) object.getValueParameter();
                    if (null != valParas) {
                        if (!valParas.contains(val)) {
                            logger.warn("Parameter is not in the service parameterlist: {}", val);
                            return null;
                        }
                    }
                }
                if (DATA_TYPE_STRING_VALUE.equals(type)) {
                    newObject.addProperty("value", val);
                } else if (DATA_TYPE_FLOAT_VALUE.equals(type)) {
                    newObject.addProperty("value", Float.parseFloat(val));
                } else if (DATA_TYPE_SWITCH_PROGRAM.equals(type)) {
                    if (object.getVirtual() == 1) {
                        newObject = sendVirtualState(object, service, command, itemType);
                    } else {
                        /* The JSONArray of switch items can be send directly */
                        try {
                            /* Check whether this input string is a valid JSONArray */
                            JsonArray userArray = (JsonArray) JsonParser.parseString(val);
                            newObject = userArray.getAsJsonObject();
                        } catch (JsonParseException e) {
                            logger.warn("The input for the switchProgram is not a valid JSONArray : {}",
                                    e.getMessage());
                            return null;
                        }
                    }
                } else {
                    logger.info("Not supported type for stringItem: {}", type);
                }
                /* Binding is a DateTimeItem */
            } else if (CoreItemFactory.DATETIME.equals(itemType)) {
                String val = ((DateTimeType) command).toString();
                newObject = new JsonObject();
                if (DATA_TYPE_STRING_VALUE.equals(type)) {
                    newObject.addProperty("value", val);
                } else if (DATA_TYPE_SWITCH_PROGRAM.equals(type)) {
                    newObject = sendVirtualState(object, service, command, itemType);
                } else {
                    logger.info("Not supported type for dateTimeItem: {}", type);
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
                    if (null != valParas) {
                        if (!valParas.contains(val)) {
                            logger.warn("Parameter is not in the service parameterlist: {}", val);
                            return null;
                        }
                    }
                } else if (switchNames.isEmpty()) {
                    logger.debug("No switch item configuration");
                    return null;
                } else {
                    logger.info("Switch-Item only on configured on/off string values {}", command);
                    return null;
                }
                if (DATA_TYPE_STRING_VALUE.equals(type)) {
                    newObject.addProperty("value", val);
                } else {
                    logger.info("Not supported type for SwitchItem:{}", type);
                }
            } else {
                logger.info("Bindingtype not supported: {}", itemType.getClass());
                return null;
            }
            /* If some data is availible then we have to send it to device */
            if (newObject != null && newObject.toString().length() > 2) {
                logger.trace("Send Data: {}", newObject);
                return newObject;
            } else {
                return null;
            }
        }
    }

    /**
     * This function sets the state of a virtual service
     */
    public @Nullable JsonObject sendVirtualState(KM200ServiceObject object, String service, Command command,
            String itemType) {
        JsonObject newObject = null;
        String type = null;
        logger.trace("Check virtual state of: {} type: {} item: {}", service, type, itemType);
        String parent = object.getParent();
        if (null != parent) {
            KM200ServiceObject objParent = remoteDevice.getServiceObject(parent);
            if (null != objParent) {
                type = object.getServiceType();
                /* Binding is a StringItem */
                if (CoreItemFactory.STRING.equals(itemType)) {
                    String val = ((StringType) command).toString();
                    switch (type) {
                        case DATA_TYPE_SWITCH_PROGRAM:
                            KM200SwitchProgramServiceHandler sPService = ((KM200SwitchProgramServiceHandler) objParent
                                    .getValueParameter());
                            if (null != sPService) {
                                String[] servicePath = service.split("/");
                                String virtService = servicePath[servicePath.length - 1];
                                if ("weekday".equals(virtService)) {
                                    /* Only parameter changing without communication to device */
                                    sPService.setActiveDay(val);
                                }
                            }
                            break;
                    }
                    /* Binding is a NumberItem */
                } else if (CoreItemFactory.NUMBER.equals(itemType)) {
                    Integer val = ((DecimalType) command).intValue();
                    switch (type) {
                        case DATA_TYPE_SWITCH_PROGRAM:
                            KM200SwitchProgramServiceHandler sPService = ((KM200SwitchProgramServiceHandler) objParent
                                    .getValueParameter());
                            if (null != sPService) {
                                String[] servicePath = service.split("/");
                                String virtService = servicePath[servicePath.length - 1];
                                if ("cycle".equals(virtService)) {
                                    /* Only parameter changing without communication to device */
                                    sPService.setActiveCycle(val);
                                } else if (virtService.equals(sPService.getPositiveSwitch())) {
                                    sPService.setActivePositiveSwitch(val);
                                    /* Create a JSON Array from current switch configuration */
                                    newObject = sPService.getUpdatedJSONData(objParent);
                                } else if (virtService.equals(sPService.getNegativeSwitch())) {
                                    sPService.setActiveNegativeSwitch(val);
                                    /* Create a JSON Array from current switch configuration */
                                    newObject = sPService.getUpdatedJSONData(objParent);
                                }
                            }
                            break;
                        case DATA_TYPE_ERROR_LIST:
                            KM200ErrorServiceHandler eService = ((KM200ErrorServiceHandler) objParent
                                    .getValueParameter());
                            if (null != eService) {
                                String[] nServicePath = service.split("/");
                                String nVirtService = nServicePath[nServicePath.length - 1];
                                if ("error".equals(nVirtService)) {
                                    /* Only parameter changing without communication to device */
                                    eService.setActiveError(val);
                                }
                            }
                            break;
                    }
                } else if (CoreItemFactory.DATETIME.equals(itemType)) {
                    ZonedDateTime swTime = ((DateTimeType) command).getZonedDateTime();
                    KM200SwitchProgramServiceHandler sPService = ((KM200SwitchProgramServiceHandler) objParent
                            .getValueParameter());
                    if (null != sPService) {
                        String[] servicePath = service.split("/");
                        String virtService = servicePath[servicePath.length - 1];
                        Integer minutes = swTime.getHour() * 60 + swTime.getMinute()
                                + swTime.getOffset().getTotalSeconds() % 60;
                        minutes = (minutes % sPService.getSwitchPointTimeRaster())
                                * sPService.getSwitchPointTimeRaster();
                        if (virtService.equals(sPService.getPositiveSwitch())) {
                            sPService.setActivePositiveSwitch(minutes);
                        }
                        if (virtService.equals(sPService.getNegativeSwitch())) {
                            sPService.setActiveNegativeSwitch(minutes);
                        }
                        /* Create a JSON Array from current switch configuration */
                        newObject = sPService.getUpdatedJSONData(objParent);
                    }
                }
                return newObject;
            }
        }
        return null;
    }
}
