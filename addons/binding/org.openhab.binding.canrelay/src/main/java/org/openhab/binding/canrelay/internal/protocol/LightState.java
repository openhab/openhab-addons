/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.protocol;

import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.openhab.binding.canrelay.internal.CanRelayBindingConstants.nodeAsString;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;

/**
 * Wrapper for internal light. Represents one light in the CanRelay.
 * It simply consists of unique nodeID represented as an integer and its current status (whether it is now on or off)
 *
 * @author Lubos Housa - Initial Contribution
 */
@NonNullByDefault
public class LightState {

    private final int nodeID;
    private OnOffType state = OFF;

    public LightState(int nodeID) {
        this.nodeID = nodeID;
    }

    public LightState(LightState lightState) {
        this(lightState.nodeID);
        this.state = lightState.state;
    }

    public int getNodeID() {
        return nodeID;
    }

    public OnOffType getState() {
        return state;
    }

    public void setState(OnOffType state) {
        this.state = state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + nodeID;
        result = prime * result + state.hashCode();
        return result;
    }

    @Override
    @NonNullByDefault({})
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
        LightState other = (LightState) obj;
        if (nodeID != other.nodeID) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LightState [nodeID=" + nodeAsString(nodeID) + ", state=" + state + "]";
    }
}
