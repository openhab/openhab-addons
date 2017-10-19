/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.mqtt;

import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation for static access to the (central) MqttService
 * 
 * @author Tim Oberföll
 * @author Sean McGuire
 *
 */
public class MySensorsMqttService {
    
    private static MqttService mqttService;
    protected Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * Get the static instance of MqttService which holds all broker connections
     * 
     * @return static instance of the MqttService
     */
    public static MqttService getMqttService() {
        return mqttService;
    }
    
    public void initialize() {
    }
    
    /**
     * Sets the MqttService instance. Method is called by OSGI XML service
     * 
     * @param mqttService MqttService that should be set
     */
    public void setMqttService(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    /**
     * Unsets the MqttService instance. Method is called by OSGI XML service.
     * 
     * @param service MqttService that should be unset
     */
    public void unsetMqttService(MqttService service) {
        mqttService = null;
    }
    
}
