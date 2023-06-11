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

/**
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Extract Where object from Structure
 * @author Wouter Born - Add equals, hashCode, toString methods
 */
public class WWNWhere {
    private String whereId;
    private String name;

    public String getWhereId() {
        return whereId;
    }

    public String getName() {
        return name;
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
        WWNWhere other = (WWNWhere) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
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
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((whereId == null) ? 0 : whereId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Where [whereId=").append(whereId).append(", name=").append(name).append("]");
        return builder.toString();
    }
}
