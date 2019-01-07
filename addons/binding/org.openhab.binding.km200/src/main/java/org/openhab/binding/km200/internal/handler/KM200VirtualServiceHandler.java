/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.internal.handler;

import org.openhab.binding.km200.internal.KM200Device;
import org.openhab.binding.km200.internal.KM200ServiceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The KM200DataHandler is representing the virtual services inside this binding
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200VirtualServiceHandler {
    private final Logger logger = LoggerFactory.getLogger(KM200VirtualServiceHandler.class);

    private final KM200Device remoteDevice;

    public KM200VirtualServiceHandler(KM200Device remoteDevice) {
        this.remoteDevice = remoteDevice;
    }

    /**
     * This function creates the virtual services
     */
    public void initVirtualObjects() {
        KM200ServiceObject newObject = null;
        try {
            for (KM200ServiceObject object : remoteDevice.virtualList) {
                logger.debug("Full Servicename: {}", object.getFullServiceName());
                String id = object.getFullServiceName();
                String type = object.getServiceType();
                switch (type) {
                    case "switchProgram":
                        KM200SwitchProgramServiceHandler sPService = ((KM200SwitchProgramServiceHandler) object
                                .getValueParameter());
                        if (!sPService.determineSwitchNames(remoteDevice)) {
                            logger.info("No references for switch service: {}, this is not supported",
                                    object.getFullServiceName());
                            continue;
                        }
                        JsonObject nodeRoot = object.getJSONData();
                        sPService.updateSwitches(nodeRoot, remoteDevice);
                        newObject = new KM200ServiceObject(id + "/weekday", type, 1, 1, 0, 1, id, object);
                        object.serviceTreeMap.put("weekday", newObject);
                        newObject = new KM200ServiceObject(id + "/nbrCycles", type, 1, 0, 0, 1, id, object);
                        object.serviceTreeMap.put("nbrCycles", newObject);
                        newObject = new KM200ServiceObject(id + "/cycle", type, 1, 1, 0, 1, id, object);
                        object.serviceTreeMap.put("cycle", newObject);
                        logger.debug("On: {}  Of: {}", id + "/" + sPService.getPositiveSwitch(),
                                id + "/" + sPService.getNegativeSwitch());
                        newObject = new KM200ServiceObject(id + "/" + sPService.getPositiveSwitch(), type, 1,
                                object.getWriteable(), object.getRecordable(), 1, id, object);
                        object.serviceTreeMap.put(sPService.getPositiveSwitch(), newObject);
                        newObject = new KM200ServiceObject(id + "/" + sPService.getNegativeSwitch(), type, 1,
                                object.getWriteable(), object.getRecordable(), 1, id, object);
                        object.serviceTreeMap.put(sPService.getNegativeSwitch(), newObject);
                        break;
                    case "errorList":
                        newObject = new KM200ServiceObject(id + "/nbrErrors", type, 1, 0, 0, 1, id, object);
                        object.serviceTreeMap.put("nbrErrors", newObject);
                        newObject = new KM200ServiceObject(id + "/error", type, 1, 1, 0, 1, id, object);
                        object.serviceTreeMap.put("error", newObject);
                        newObject = new KM200ServiceObject(id + "/errorString", type, 1, 0, 0, 1, id, object);
                        object.serviceTreeMap.put("errorString", newObject);
                        break;
                }
            }
        } catch (JsonParseException e) {
            logger.error("Parsingexception in JSON: {}", e.getMessage());
        }
    }
}
