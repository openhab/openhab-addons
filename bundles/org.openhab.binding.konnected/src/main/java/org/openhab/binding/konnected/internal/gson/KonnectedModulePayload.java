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
    @SerializedName("dht_sensors")
    private Set<KonnectedModuleGson> dht22;
    @SerializedName("ds18b20_sensors")
    private Set<KonnectedModuleGson> ds18b20;
    @SerializedName("token")
    private String authToken;
    private String apiUrl;
    private Boolean blink;
    private Boolean discovery;

    public KonnectedModulePayload(String authTokenPassed, String apiURLPassed) {
        this.authToken = authTokenPassed;
        this.apiUrl = apiURLPassed;
        this.sensors = new HashSet<>();
        this.actuators = new HashSet<>();
        this.dht22 = new HashSet<>();
        this.ds18b20 = new HashSet<>();
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

    public Set<KonnectedModuleGson> getDht22() {
        return dht22;
    }

    public void addDht22(KonnectedModuleGson Dht22) {
        this.dht22.add(Dht22);
    }

    public void removeDht22(KonnectedModuleGson Dht22) {
        this.dht22.remove(Dht22);
    }

    public Set<KonnectedModuleGson> getDs18b20() {
        return ds18b20;
    }

    public void addDs18b20(KonnectedModuleGson Ds18b20) {
        this.ds18b20.add(Ds18b20);
    }

    public void removeDs18b20(KonnectedModuleGson Ds18b20) {
        this.ds18b20.remove(Ds18b20);
    }

    public void setDiscovery(Boolean setDiscovery) {
        this.discovery = setDiscovery;
    }

    public void setBlink(Boolean setBlink) {
        this.blink = setBlink;
    }
}
