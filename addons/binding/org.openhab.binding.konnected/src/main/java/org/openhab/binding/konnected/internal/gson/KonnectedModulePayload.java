/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal.gson;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link KonnectedModulePayload} is responsible to hold
 * data to send as settings for the konnected module
 *
 * @author Zachary Christiansen - Initial contribution
 *
 */
public class KonnectedModulePayload {

    private Set<KonnectedModuleGson> sensors;
    private Set<KonnectedModuleGson> actuators;
    @SerializedName("token")
    private String authToken;
    private String apiUrl;

    public KonnectedModulePayload(String authTokenPassed, String apiURLPassed) {
        this.authToken = authTokenPassed;
        this.apiUrl = apiURLPassed;
        this.sensors = new HashSet<KonnectedModuleGson>();
        this.actuators = new HashSet<KonnectedModuleGson>();
    }

    public Set<KonnectedModuleGson> getSensors() {
        return sensors;
    }

    public void addSensor(KonnectedModuleGson sensor) {
        this.sensors.add(sensor);
    }

    public void removeSensor(KonnectedModuleGson sensor) {
        this.sensors.remove(sensor);
    }

    public Set<KonnectedModuleGson> getActuators() {
        return actuators;
    }

    public void addActuators(KonnectedModuleGson actuator) {
        this.actuators.add(actuator);
    }

    public void removeActuator(KonnectedModuleGson actuator) {
        this.actuators.remove(actuator);
    }

    public String getToken() {
        return authToken;
    }

    public String getapiUrl() {
        return apiUrl;
    }
}
