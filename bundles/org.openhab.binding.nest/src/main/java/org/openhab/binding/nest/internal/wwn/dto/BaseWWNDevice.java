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
package org.openhab.binding.nest.internal.wwn.dto;

import java.util.Date;

/**
 * Default properties shared across all WWN devices.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add equals and hashCode methods
 */
public class BaseWWNDevice implements WWNIdentifiable {

    private String deviceId;
    private String name;
    private String nameLong;
    private Date lastConnection;
    private Boolean isOnline;
    private String softwareVersion;
    private String structureId;

    private String whereId;

    @Override
    public String getId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Date getLastConnection() {
        return lastConnection;
    }

    public Boolean isOnline() {
        return isOnline;
    }

    public String getNameLong() {
        return nameLong;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getStructureId() {
        return structureId;
    }

    public String getWhereId() {
        return whereId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BaseWWNDevice other = (BaseWWNDevice) obj;
        if (deviceId == null) {
            if (other.deviceId != null) {
                return false;
            }
        } else if (!deviceId.equals(other.deviceId)) {
            return false;
        }
        if (isOnline == null) {
            if (other.isOnline != null) {
                return false;
            }
        } else if (!isOnline.equals(other.isOnline)) {
            return false;
        }
        if (lastConnection == null) {
            if (other.lastConnection != null) {
                return false;
            }
        } else if (!lastConnection.equals(other.lastConnection)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (nameLong == null) {
            if (other.nameLong != null) {
                return false;
            }
        } else if (!nameLong.equals(other.nameLong)) {
            return false;
        }
        if (softwareVersion == null) {
            if (other.softwareVersion != null) {
                return false;
            }
        } else if (!softwareVersion.equals(other.softwareVersion)) {
            return false;
        }
        if (structureId == null) {
            if (other.structureId != null) {
                return false;
            }
        } else if (!structureId.equals(other.structureId)) {
            return false;
        }
        if (whereId == null) {
            if (other.whereId != null) {
                return false;
            }
        } else if (!whereId.equals(other.whereId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
        result = prime * result + ((isOnline == null) ? 0 : isOnline.hashCode());
        result = prime * result + ((lastConnection == null) ? 0 : lastConnection.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((nameLong == null) ? 0 : nameLong.hashCode());
        result = prime * result + ((softwareVersion == null) ? 0 : softwareVersion.hashCode());
        result = prime * result + ((structureId == null) ? 0 : structureId.hashCode());
        result = prime * result + ((whereId == null) ? 0 : whereId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseNestDevice [deviceId=").append(deviceId).append(", name=").append(name)
                .append(", nameLong=").append(nameLong).append(", lastConnection=").append(lastConnection)
                .append(", isOnline=").append(isOnline).append(", softwareVersion=").append(softwareVersion)
                .append(", structureId=").append(structureId).append(", whereId=").append(whereId).append("]");
        return builder.toString();
    }
}
