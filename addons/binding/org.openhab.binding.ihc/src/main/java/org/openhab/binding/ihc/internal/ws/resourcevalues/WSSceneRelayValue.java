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
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSSceneRelayValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSSceneRelayValue extends WSResourceValue {

    protected int delayTime;
    protected boolean relayValue;

    public WSSceneRelayValue() {
    }

    public WSSceneRelayValue(int resourceID) {
        super(resourceID);
    }

    public WSSceneRelayValue(int resourceID, int delayTime, boolean relayValue) {
        super(resourceID);
        this.delayTime = delayTime;
        this.relayValue = relayValue;
    }

    /**
     * Gets the value of the delayTime property.
     *
     */
    public int getDelayTime() {
        return delayTime;
    }

    /**
     * Sets the value of the delayTime property.
     *
     */
    public void setDelayTime(int value) {
        this.delayTime = value;
    }

    /**
     * Gets the value of the relayValue property.
     *
     */
    public boolean isRelayValue() {
        return relayValue;
    }

    /**
     * Sets the value of the relayValue property.
     *
     */
    public void setRelayValue(boolean value) {
        this.relayValue = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, value=%b, delayTime=%d]", super.resourceID, relayValue, delayTime);
    }
}
