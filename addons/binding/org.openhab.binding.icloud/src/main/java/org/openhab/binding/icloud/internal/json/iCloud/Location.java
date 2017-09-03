/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.iCloud;

public class Location {
    private boolean isOld;

    public boolean getIsOld() {
        return this.isOld;
    }

    public void setIsOld(boolean isOld) {
        this.isOld = isOld;
    }

    private boolean isInaccurate;

    public boolean getIsInaccurate() {
        return this.isInaccurate;
    }

    public void setIsInaccurate(boolean isInaccurate) {
        this.isInaccurate = isInaccurate;
    }

    private double altitude;

    public double getAltitude() {
        return this.altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    private String positionType;

    public String getPositionType() {
        return this.positionType;
    }

    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    private double latitude;

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    private int floorLevel;

    public int getFloorLevel() {
        return this.floorLevel;
    }

    public void setFloorLevel(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    private double horizontalAccuracy;

    public double getHorizontalAccuracy() {
        return this.horizontalAccuracy;
    }

    public void setHorizontalAccuracy(double horizontalAccuracy) {
        this.horizontalAccuracy = horizontalAccuracy;
    }

    private String locationType;

    public String getLocationType() {
        return this.locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    private long timeStamp;

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    private boolean locationFinished;

    public boolean getLocationFinished() {
        return this.locationFinished;
    }

    public void setLocationFinished(boolean locationFinished) {
        this.locationFinished = locationFinished;
    }

    private double verticalAccuracy;

    public double getVerticalAccuracy() {
        return this.verticalAccuracy;
    }

    public void setVerticalAccuracy(double verticalAccuracy) {
        this.verticalAccuracy = verticalAccuracy;
    }

    private double longitude;

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
