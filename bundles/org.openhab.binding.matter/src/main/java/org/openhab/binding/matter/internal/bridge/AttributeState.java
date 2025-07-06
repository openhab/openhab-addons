/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.bridge;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class that represents a state of an attribute of a cluster.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class AttributeState implements Comparable<AttributeState> {
    public final String clusterName;
    public final String attributeName;
    public final Object state;

    public AttributeState(String clusterName, String attributeName, Object state) {
        this.clusterName = clusterName;
        this.attributeName = attributeName;
        this.state = state;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AttributeState other = (AttributeState) obj;
        return Objects.equals(clusterName, other.clusterName) && Objects.equals(attributeName, other.attributeName)
                && Objects.equals(state, other.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterName, attributeName, state);
    }

    @Override
    public int compareTo(@Nullable AttributeState other) {
        if (other == null) {
            return 1;
        }
        int clusterCmp = clusterName.compareTo(other.clusterName);
        if (clusterCmp != 0) {
            return clusterCmp;
        }
        int attrCmp = attributeName.compareTo(other.attributeName);
        if (attrCmp != 0) {
            return attrCmp;
        }
        return state.toString().compareTo(other.state.toString());
    }
}
