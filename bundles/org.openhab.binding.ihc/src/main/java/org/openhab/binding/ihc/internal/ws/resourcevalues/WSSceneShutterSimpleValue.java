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
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSSceneShutterSimpleValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSSceneShutterSimpleValue extends WSResourceValue {

    public final int delayTime;
    public final boolean shutterPositionIsUp;

    public WSSceneShutterSimpleValue(int resourceID, int delayTime, boolean shutterPositionIsUp) {
        super(resourceID);
        this.delayTime = delayTime;
        this.shutterPositionIsUp = shutterPositionIsUp;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, shutterPositionIsUp=%b, delayTime=%d]", super.resourceID,
                shutterPositionIsUp, delayTime);
    }
}
