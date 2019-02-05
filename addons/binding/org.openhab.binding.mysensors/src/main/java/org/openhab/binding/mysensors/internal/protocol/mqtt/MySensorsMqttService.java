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
        MySensorsMqttService.mqttService = mqttService;
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
