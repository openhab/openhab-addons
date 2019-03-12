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
 * Class for WSSceneShutterSimpleValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSSceneShutterSimpleValue extends WSResourceValue {

    protected int delayTime;
    protected boolean shutterPositionIsUp;

    public WSSceneShutterSimpleValue() {
    }

    public WSSceneShutterSimpleValue(int resourceID) {
        super(resourceID);
    }

    public WSSceneShutterSimpleValue(int resourceID, int delayTime, boolean shutterPositionIsUp) {
        super(resourceID);
        this.delayTime = delayTime;
        this.shutterPositionIsUp = shutterPositionIsUp;
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
     * Gets the value of the shutterPositionIsUp property.
     *
     */
    public boolean isShutterPositionIsUp() {
        return shutterPositionIsUp;
    }

    /**
     * Sets the value of the shutterPositionIsUp property.
     *
     */
    public void setShutterPositionIsUp(boolean value) {
        this.shutterPositionIsUp = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, shutterPositionIsUp=%b, delayTime=%d]", super.resourceID,
                shutterPositionIsUp, delayTime);
    }
}
