/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import java.util.Map;

/**
 * Top level data for all the Nest stuff, this is the format the Nest data comes back from Nest in.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add equals and hashCode methods
 */
public class TopLevelData {

    private NestDevices devices;
    private NestMetadata metadata;
    private Map<String, Structure> structures;

    public NestDevices getDevices() {
        return devices;
    }

    public NestMetadata getMetadata() {
        return metadata;
    }

    public Map<String, Structure> getStructures() {
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
        TopLevelData other = (TopLevelData) obj;
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
