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
package org.openhab.binding.nest.internal.wwn.dto;

import java.util.Map;

/**
 * The top level WWN data that is sent by Nest.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add equals and hashCode methods
 */
public class WWNTopLevelData {

    private WWNDevices devices;
    private WWNMetadata metadata;
    private Map<String, WWNStructure> structures;

    public WWNDevices getDevices() {
        return devices;
    }

    public WWNMetadata getMetadata() {
        return metadata;
    }

    public Map<String, WWNStructure> getStructures() {
        return structures;
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
        WWNTopLevelData other = (WWNTopLevelData) obj;
        if (devices == null) {
            if (other.devices != null) {
                return false;
            }
        } else if (!devices.equals(other.devices)) {
            return false;
        }
        if (metadata == null) {
            if (other.metadata != null) {
                return false;
            }
        } else if (!metadata.equals(other.metadata)) {
            return false;
        }
        if (structures == null) {
            if (other.structures != null) {
                return false;
            }
        } else if (!structures.equals(other.structures)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((devices == null) ? 0 : devices.hashCode());
        result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
        result = prime * result + ((structures == null) ? 0 : structures.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TopLevelData [devices=").append(devices).append(", metadata=").append(metadata)
                .append(", structures=").append(structures).append("]");
        return builder.toString();
    }
}
