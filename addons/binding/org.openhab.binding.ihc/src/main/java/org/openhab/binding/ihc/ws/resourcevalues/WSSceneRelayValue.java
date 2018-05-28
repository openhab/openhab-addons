/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws.resourcevalues;

/**
 * Java class for WSSceneRelayValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSSceneRelayValue extends WSResourceValue {

    protected int delayTime;
    protected boolean relayValue;

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
