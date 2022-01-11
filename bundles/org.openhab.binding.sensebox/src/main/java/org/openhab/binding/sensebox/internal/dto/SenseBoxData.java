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
package org.openhab.binding.sensebox.internal.dto;

import java.util.List;

import org.openhab.core.thing.ThingStatus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseBoxData} holds a de-serialized representation
 * of the API response and the data therein...
 *
 * @author Hakan Tandogan - Initial contribution
 */
public class SenseBoxData {

    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("exposure")
    private String exposure;

    @SerializedName("image")
    private String image;

    @SerializedName("loc")
    private List<SenseBoxLoc> locs;

    @SerializedName("sensors")
    private List<SenseBoxSensor> sensors;

    @Expose(deserialize = false)
    private ThingStatus status;

    @Expose(deserialize = false)
    private SenseBoxDescriptor descriptor;

    @Expose(deserialize = false)
    private SenseBoxLocation location;

    @Expose(deserialize = false)
    private SenseBoxSensor uvIntensity;

    @Expose(deserialize = false)
    private SenseBoxSensor luminance;

    @Expose(deserialize = false)
    private SenseBoxSensor pressure;

    @Expose(deserialize = false)
    private SenseBoxSensor humidity;

    @Expose(deserialize = false)
    private SenseBoxSensor temperature;

    @Expose(deserialize = false)
    private SenseBoxSensor particulateMatter2dot5;

    @Expose(deserialize = false)
    private SenseBoxSensor particulateMatter10;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExposure() {
        return exposure;
    }

    public void setExposure(String exposure) {
        this.exposure = exposure;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public SenseBoxDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(SenseBoxDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public SenseBoxLocation getLocation() {
        return location;
    }

    public void setLocation(SenseBoxLocation location) {
        this.location = location;
    }

    public List<SenseBoxLoc> getLocs() {
        return locs;
    }

    public void setLocs(List<SenseBoxLoc> locs) {
        this.locs = locs;
    }

    public List<SenseBoxSensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<SenseBoxSensor> sensors) {
        this.sensors = sensors;
    }

    public ThingStatus getStatus() {
        return status;
    }

    public void setStatus(ThingStatus status) {
        this.status = status;
    }

    public SenseBoxSensor getUvIntensity() {
        return uvIntensity;
    }

    public void setUvIntensity(SenseBoxSensor uvIntensity) {
        this.uvIntensity = uvIntensity;
    }

    public SenseBoxSensor getLuminance() {
        return luminance;
    }

    public void setLuminance(SenseBoxSensor luminance) {
        this.luminance = luminance;
    }

    public SenseBoxSensor getPressure() {
        return pressure;
    }

    public void setPressure(SenseBoxSensor pressure) {
        this.pressure = pressure;
    }

    public SenseBoxSensor getHumidity() {
        return humidity;
    }

    public void setHumidity(SenseBoxSensor humidity) {
        this.humidity = humidity;
    }

    public SenseBoxSensor getTemperature() {
        return temperature;
    }

    public void setTemperature(SenseBoxSensor temperature) {
        this.temperature = temperature;
    }

    public SenseBoxSensor getParticulateMatter2dot5() {
        return particulateMatter2dot5;
    }

    public void setParticulateMatter2dot5(SenseBoxSensor particulateMatter2dot5) {
        this.particulateMatter2dot5 = particulateMatter2dot5;
    }

    public SenseBoxSensor getParticulateMatter10() {
        return particulateMatter10;
    }

    public void setParticulateMatter10(SenseBoxSensor particulateMatter10) {
        this.particulateMatter10 = particulateMatter10;
    }
}
