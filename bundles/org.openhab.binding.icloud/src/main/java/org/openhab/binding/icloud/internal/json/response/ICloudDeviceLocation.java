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
package org.openhab.binding.icloud.internal.json.response;

/**
 * Serializable class to parse json response received from the Apple server.
 * Contains device location information.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class ICloudDeviceLocation {
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
}
