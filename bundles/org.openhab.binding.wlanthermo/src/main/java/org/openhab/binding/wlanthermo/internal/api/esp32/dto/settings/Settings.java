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
package org.openhab.binding.wlanthermo.internal.api.esp32.dto.settings;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Settings {

    @SerializedName("device")
    @Expose
    private Device device;
    @SerializedName("system")
    @Expose
    private System system;
    @SerializedName("hardware")
    @Expose
    private List<String> hardware = null;
    @SerializedName("api")
    @Expose
    private Api api;
    @SerializedName("sensors")
    @Expose
    private List<Sensor> sensors = null;
    @SerializedName("features")
    @Expose
    private Features features;
    @SerializedName("pid")
    @Expose
    private List<Pid> pid = null;
    @SerializedName("aktor")
    @Expose
    private List<String> aktor = null;
    @SerializedName("display")
    @Expose
    private Display display;
    @SerializedName("iot")
    @Expose
    private Iot iot;
    @SerializedName("notes")
    @Expose
    private Notes notes;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public System getSystem() {
        return system;
    }

    public void setSystem(System system) {
        this.system = system;
    }

    public List<String> getHardware() {
        return hardware;
    }

    public void setHardware(List<String> hardware) {
        this.hardware = hardware;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public List<Pid> getPid() {
        return pid;
    }

    public void setPid(List<Pid> pid) {
        this.pid = pid;
    }

    public List<String> getAktor() {
        return aktor;
    }

    public void setAktor(List<String> aktor) {
        this.aktor = aktor;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public Iot getIot() {
        return iot;
    }

    public void setIot(Iot iot) {
        this.iot = iot;
    }

    public Notes getNotes() {
        return notes;
    }

    public void setNotes(Notes notes) {
        this.notes = notes;
    }
}
