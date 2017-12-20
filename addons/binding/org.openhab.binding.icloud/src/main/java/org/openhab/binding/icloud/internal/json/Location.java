/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json;

/**
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class Location {
    private double altitude;

    private int floorLevel;

    private double horizontalAccuracy;

    private boolean isInaccurate;

    private boolean isOld;

    private double latitude;

    private boolean locationFinished;

    private String locationType;

    private double longitude;

    private String positionType;

    private long timeStamp;

    private double verticalAccuracy;

    public double getAltitude() {
        return this.altitude;
    }

    public int getFloorLevel() {
        return this.floorLevel;
    }

    public double getHorizontalAccuracy() {
        return this.horizontalAccuracy;
    }

    public boolean getIsInaccurate() {
        return this.isInaccurate;
    }

    public boolean getIsOld() {
        return this.isOld;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public boolean getLocationFinished() {
        return this.locationFinished;
    }

    public String getLocationType() {
        return this.locationType;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getPositionType() {
        return this.positionType;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public double getVerticalAccuracy() {
        return this.verticalAccuracy;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setFloorLevel(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    public void setHorizontalAccuracy(double horizontalAccuracy) {
        this.horizontalAccuracy = horizontalAccuracy;
    }

    public void setIsInaccurate(boolean isInaccurate) {
        this.isInaccurate = isInaccurate;
    }

    public void setIsOld(boolean isOld) {
        this.isOld = isOld;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLocationFinished(boolean locationFinished) {
        this.locationFinished = locationFinished;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setVerticalAccuracy(double verticalAccuracy) {
        this.verticalAccuracy = verticalAccuracy;
    }
}
