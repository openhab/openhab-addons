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
package org.openhab.binding.gpstracker.internal.message.life360;

import com.google.gson.annotations.SerializedName;
import org.eclipse.smarthome.core.library.types.PointType;

import java.math.BigDecimal;

/**
 * The {@link PlacesItem} is a Life360 message POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class PlacesItem {

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("name")
    private String name;

    @SerializedName("typeLabel")
    private String typeLabel;

    @SerializedName("id")
    private String id;

    @SerializedName("circleId")
    private String circleId;

    @SerializedName("ownerId")
    private String ownerId;

    @SerializedName("radius")
    private String radius;

    @SerializedName("type")
    private int type;

    @SerializedName("longitude")
    private String longitude;

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PointType getLocation() {
        return new PointType(getLatitude() + "," + getLongitude());
    }

    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }

    public String getCircleId() {
        return circleId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public BigDecimal getRadius() {
        return new BigDecimal(radius);
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return
                "PlacesItem{" +
                        "latitude = '" + latitude + '\'' +
                        ",name = '" + name + '\'' +
                        ",typeLabel = '" + typeLabel + '\'' +
                        ",id = '" + id + '\'' +
                        ",circleId = '" + circleId + '\'' +
                        ",ownerId = '" + ownerId + '\'' +
                        ",radius = '" + radius + '\'' +
                        ",type = '" + type + '\'' +
                        ",longitude = '" + longitude + '\'' +
                        "}";
    }

    public boolean isChanged(String name, String location, BigDecimal radius) {
        return this.name == null || this.latitude == null || this.longitude == null || this.radius == null || !this.name.equals(name) || !(this.latitude + "," + this.longitude).equals(location) || !this.radius.equals(radius.toString());
    }
}
